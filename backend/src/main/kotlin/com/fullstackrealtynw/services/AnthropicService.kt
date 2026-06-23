package com.fullstackrealtynw.services

import com.fullstackrealtynw.models.AnthropicMessage
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

private const val SYSTEM_PROMPT = """
You are a knowledgeable and warm real estate advisor for Full Stack Realty NW, led by Vinny Rao — a licensed real estate agent AND active real estate investor in the Pacific Northwest.

Your goal is to have a natural, friendly conversation to understand what the user is looking for in a home, then provide thoughtful, personalized recommendations.

CONVERSATION FLOW:
1. Start with a warm greeting and ask about their family situation or real estate goal (buying, selling, or investing)
2. Based on their answer, ask targeted follow-up questions — ONE question at a time, not a list
3. Gather key information naturally through conversation:
   - Family size and ages (especially children for school considerations)
   - Budget range (comfortable monthly payment or purchase price)
   - Timeline (when they need to move)
   - Work location and commute preferences
   - Must-have features (bedrooms, yard, garage, home office, etc.)
   - Lifestyle priorities (walkability, nature access, schools, nightlife, etc.)
   - Whether they're renting now or own a home to sell
4. Once you have location, budget, and minimum bedrooms — call the search_listings tool to fetch real listings.
5. Present the listings naturally: describe each one, why it fits their needs, and invite them to schedule a tour.

KEY POINTS TO WEAVE IN NATURALLY:
- Full Stack Realty NW charges just 2% listing commission (vs. the typical 3%), saving sellers thousands
- Vinny's background as a real estate investor gives clients unique insight into property value and investment potential
- Vinny can be reached at vinny@fullstackrealtynw.com

STYLE:
- Be conversational and warm, not robotic or salesy
- Ask one focused question at a time to keep the conversation flowing naturally
- Use specific Pacific Northwest neighborhood knowledge
- Be honest about market conditions

Remember: You are having a conversation, not filling out a form. Let it flow naturally.
"""

private val LISTING_TOOL = buildJsonObject {
    put("name", "search_listings")
    put("description", "Search for active real estate listings matching the buyer's criteria. Call this once you have gathered location, maximum budget, and minimum bedrooms from the conversation.")
    putJsonObject("input_schema") {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("location") {
                put("type", "string")
                put("description", "City or neighborhood in the Pacific Northwest, e.g. 'Kirkland WA' or 'Bellevue WA'")
            }
            putJsonObject("price_min") {
                put("type", "integer")
                put("description", "Minimum listing price in dollars")
            }
            putJsonObject("price_max") {
                put("type", "integer")
                put("description", "Maximum listing price in dollars")
            }
            putJsonObject("beds_min") {
                put("type", "integer")
                put("description", "Minimum number of bedrooms")
            }
            putJsonObject("baths_min") {
                put("type", "number")
                put("description", "Minimum number of bathrooms")
            }
        }
        putJsonArray("required") {
            add("location"); add("price_max"); add("beds_min")
        }
    }
}

class AnthropicService(
    private val apiKey: String,
    private val toolExecutor: (suspend (JsonObject) -> String)? = null,
) {
    private val logger = LoggerFactory.getLogger(AnthropicService::class.java)
    private val client = HttpClient(CIO) {
        engine { requestTimeout = 120_000 }
    }

    suspend fun streamCompletion(
        messages: List<AnthropicMessage>,
        onChunk: suspend (String) -> Unit,
        onComplete: suspend (String) -> Unit,
    ) {
        val apiMessages = messages.map { buildJsonObject {
            put("role", it.role)
            put("content", it.content)
        }}
        streamInternal(apiMessages, onChunk, onComplete, withTools = toolExecutor != null)
    }

    private suspend fun streamInternal(
        messages: List<JsonObject>,
        onChunk: suspend (String) -> Unit,
        onComplete: suspend (String) -> Unit,
        withTools: Boolean,
    ) {
        if (apiKey.isBlank()) {
            val fallback = "I'd love to help you find your perfect Pacific Northwest home! " +
                "Unfortunately, the AI service isn't configured yet. " +
                "Please contact Vinny directly at vinny@fullstackrealtynw.com to get started."
            onChunk(fallback); onComplete(fallback); return
        }

        val requestBody = buildJsonObject {
            put("model", "claude-sonnet-4-6")
            put("max_tokens", 1024)
            put("stream", true)
            put("system", SYSTEM_PROMPT)
            put("messages", buildJsonArray { messages.forEach { add(it) } })
            if (withTools) put("tools", buildJsonArray { add(LISTING_TOOL) })
        }

        // Streaming state machine
        var currentBlockType = ""
        var currentToolId = ""
        var currentToolName = ""
        val toolInputJson = StringBuilder()
        val assistantTextBeforeTool = StringBuilder()
        val fullText = StringBuilder()
        val lineBuffer = StringBuilder()
        var stopReason = ""

        try {
            val response = client.post("https://api.anthropic.com/v1/messages") {
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", "2023-06-01")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    append(HttpHeaders.Accept, "text/event-stream")
                }
                setBody(requestBody.toString())
            }

            val channel: ByteReadChannel = response.bodyAsChannel()

            while (!channel.isClosedForRead) {
                val available = channel.availableForRead
                if (available == 0) { channel.awaitContent(); continue }
                val bytes = ByteArray(available)
                channel.readFully(bytes, 0, available)
                for (char in bytes.toString(Charsets.UTF_8)) {
                    if (char == '\n') {
                        val line = lineBuffer.toString(); lineBuffer.clear()
                        if (!line.startsWith("data: ")) continue
                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]" || data.isEmpty()) continue

                        val event = try { Json.parseToJsonElement(data).jsonObject } catch (_: Exception) { continue }
                        when (event["type"]?.jsonPrimitive?.content) {

                            "content_block_start" -> {
                                val block = event["content_block"]?.jsonObject ?: continue
                                currentBlockType = block["type"]?.jsonPrimitive?.content ?: ""
                                if (currentBlockType == "tool_use") {
                                    currentToolId   = block["id"]?.jsonPrimitive?.content ?: ""
                                    currentToolName = block["name"]?.jsonPrimitive?.content ?: ""
                                    toolInputJson.clear()
                                }
                            }

                            "content_block_delta" -> {
                                val delta = event["delta"]?.jsonObject ?: continue
                                when (delta["type"]?.jsonPrimitive?.content) {
                                    "text_delta" -> {
                                        val text = delta["text"]?.jsonPrimitive?.content ?: ""
                                        fullText.append(text)
                                        assistantTextBeforeTool.append(text)
                                        onChunk(text)
                                    }
                                    "input_json_delta" -> {
                                        toolInputJson.append(delta["partial_json"]?.jsonPrimitive?.content ?: "")
                                    }
                                }
                            }

                            "message_delta" -> {
                                stopReason = event["delta"]?.jsonObject
                                    ?.get("stop_reason")?.jsonPrimitive?.content ?: ""
                            }
                        }
                    } else {
                        lineBuffer.append(char)
                    }
                }
            }

            if (stopReason == "tool_use" && withTools && toolExecutor != null && currentToolName == "search_listings") {
                val toolInput = try { Json.parseToJsonElement(toolInputJson.toString()).jsonObject }
                    catch (_: Exception) { buildJsonObject {} }

                val toolResult = try { toolExecutor(toolInput) }
                    catch (e: Exception) { "Error fetching listings: ${e.message}" }

                // Build follow-up messages including the tool result
                val assistantContent = buildJsonArray {
                    if (assistantTextBeforeTool.isNotEmpty()) addJsonObject {
                        put("type", "text"); put("text", assistantTextBeforeTool.toString())
                    }
                    addJsonObject {
                        put("type", "tool_use")
                        put("id", currentToolId)
                        put("name", currentToolName)
                        put("input", toolInput)
                    }
                }
                val followUpMessages = messages + listOf(
                    buildJsonObject { put("role", "assistant"); put("content", assistantContent) },
                    buildJsonObject {
                        put("role", "user")
                        put("content", buildJsonArray {
                            addJsonObject {
                                put("type", "tool_result")
                                put("tool_use_id", currentToolId)
                                put("content", toolResult)
                            }
                        })
                    }
                )
                // Second streaming call — no tools to avoid loops
                streamInternal(followUpMessages, onChunk, onComplete, withTools = false)
            } else {
                onComplete(fullText.toString())
            }
        } catch (e: Exception) {
            logger.error("Anthropic API error", e)
            val errorMsg = "I'm having trouble connecting right now. " +
                "Please try again or reach out to Vinny at vinny@fullstackrealtynw.com."
            try { onChunk(errorMsg); onComplete(errorMsg) } catch (_: Exception) {}
        }
    }

    fun close() = client.close()
}
