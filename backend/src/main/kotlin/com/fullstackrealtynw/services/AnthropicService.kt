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
You are a warm, knowledgeable home advisor for Full Stack Realty NW, led by Vinny Rao — a licensed agent and active real estate investor in the Pacific Northwest.

YOUR GOAL: Have a natural conversation to understand exactly what the client is looking for, then collect their contact info so Vinny can personally send them curated listings.

CONVERSATION FLOW:
1. Warm greeting — ask what brings them here today (buying, selling, or investing)
2. Ask follow-up questions ONE AT A TIME to build a clear picture:
   - Family situation (size, ages of kids — important for schools)
   - Budget (purchase price or comfortable monthly payment)
   - Preferred area or neighborhoods
   - Must-have features (bedrooms, yard, garage, home office, etc.)
   - Timeline — when do they need to be in a place?
   - Current situation — renting, own a home to sell?
   - Commute or work location
   - Lifestyle priorities (walkability, schools, outdoor access, etc.)
3. After 4–6 exchanges when you have a solid picture, say something like:
   "I have a great sense of what you're looking for. Vinny personally curates listings for each client — can I grab your name and best email so he can send you some options?"
4. Ask for phone number too: "And a phone number in case he wants to reach out quickly?"
5. Once you have name + email (phone optional), call the capture_lead tool.
6. After the tool runs, tell them: "Perfect — Vinny will review your profile and send you some curated options within 24 hours. Feel free to reach out to him directly at vinny@fullstackrealtynw.com if you have questions in the meantime."

KEY FACTS TO WEAVE IN NATURALLY:
- Full Stack Realty NW charges 2% listing commission vs. the typical 3% — saves sellers thousands
- Vinny is both a licensed agent AND an active investor, giving clients unique market insight
- Direct contact: vinny@fullstackrealtynw.com

STYLE:
- Conversational and warm, never robotic or salesy
- One question at a time — never fire a list of questions
- Be genuinely curious about their situation
- If they ask about price ranges or neighborhoods, give real, specific PNW knowledge
"""

private val CAPTURE_LEAD_TOOL = buildJsonObject {
    put("name", "capture_lead")
    put("description", "Save the client's contact information and home search profile, then notify Vinny so he can follow up with curated listings. Call this once you have their name, email, and a good understanding of what they're looking for.")
    putJsonObject("input_schema") {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("name")     { put("type", "string"); put("description", "Client's full name") }
            putJsonObject("email")    { put("type", "string"); put("description", "Client's email address") }
            putJsonObject("phone")    { put("type", "string"); put("description", "Client's phone number (optional)") }
            putJsonObject("summary")  { put("type", "string"); put("description", "2–3 sentence summary of what they're looking for, written for Vinny") }
            putJsonObject("budget")   { put("type", "string"); put("description", "Their budget range") }
            putJsonObject("location") { put("type", "string"); put("description", "Preferred area or neighborhoods") }
            putJsonObject("bedrooms") { put("type", "string"); put("description", "Bedroom requirements") }
            putJsonObject("timeline") { put("type", "string"); put("description", "When they need to move") }
            putJsonObject("additional_notes") { put("type", "string"); put("description", "Anything else Vinny should know — family situation, must-haves, dealbreakers") }
        }
        putJsonArray("required") { add("name"); add("email"); add("summary") }
    }
}

class AnthropicService(
    private val apiKey: String,
    private val toolExecutor: (suspend (String, JsonObject) -> String)? = null,
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
                "Please contact Vinny directly at vinny@fullstackrealtynw.com."
            onChunk(fallback); onComplete(fallback); return
        }

        val requestBody = buildJsonObject {
            put("model", "claude-sonnet-4-6")
            put("max_tokens", 1024)
            put("stream", true)
            put("system", SYSTEM_PROMPT)
            put("messages", buildJsonArray { messages.forEach { add(it) } })
            if (withTools) put("tools", buildJsonArray { add(CAPTURE_LEAD_TOOL) })
        }

        var currentBlockType = ""
        var currentToolId    = ""
        var currentToolName  = ""
        val toolInputJson            = StringBuilder()
        val assistantTextBeforeTool  = StringBuilder()
        val fullText                 = StringBuilder()
        val lineBuffer               = StringBuilder()
        var stopReason               = ""

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
                                    "input_json_delta" ->
                                        toolInputJson.append(delta["partial_json"]?.jsonPrimitive?.content ?: "")
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

            if (stopReason == "tool_use" && withTools && toolExecutor != null) {
                val toolInput = try {
                    Json.parseToJsonElement(toolInputJson.toString()).jsonObject
                } catch (_: Exception) { buildJsonObject {} }

                val toolResult = try {
                    toolExecutor(currentToolName, toolInput)
                } catch (e: Exception) {
                    "Tool error: ${e.message}"
                }

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
