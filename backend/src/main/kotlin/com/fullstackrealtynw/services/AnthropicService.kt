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
4. After gathering enough context (typically 4-6 exchanges), provide specific recommendations:
   - Specific neighborhoods or cities in the Pacific Northwest that match their needs
   - Why each neighborhood fits their profile
   - Realistic price ranges in those areas
   - Tips for their specific situation
   - Offer to schedule a free consultation with Vinny

KEY POINTS TO WEAVE IN NATURALLY:
- Full Stack Realty NW charges just 2% listing commission (vs. the typical 3%), saving sellers thousands
- Vinny's background as a real estate investor gives clients unique insight into property value and investment potential
- Vinny can be reached at vinny@fullstackrealtynw.com

STYLE:
- Be conversational and warm, not robotic or salesy
- Ask one focused question at a time to keep the conversation flowing naturally
- Use specific Pacific Northwest neighborhood knowledge
- Be honest about market conditions
- If asked about price, be specific with real market data where possible

Remember: You are having a conversation, not filling out a form. Let it flow naturally.
"""

class AnthropicService(private val apiKey: String) {
    private val logger = LoggerFactory.getLogger(AnthropicService::class.java)
    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 120_000
        }
    }

    suspend fun streamCompletion(
        messages: List<AnthropicMessage>,
        onChunk: suspend (String) -> Unit,
        onComplete: suspend (String) -> Unit,
    ) {
        if (apiKey.isBlank()) {
            val fallback = "I'd love to help you find your perfect Pacific Northwest home! " +
                "Unfortunately, the AI service isn't configured yet. " +
                "Please contact Vinny directly at vinny@fullstackrealtynw.com to get started."
            onChunk(fallback)
            onComplete(fallback)
            return
        }

        val requestMessages = buildJsonArray {
            for (msg in messages) {
                addJsonObject {
                    put("role", msg.role)
                    put("content", msg.content)
                }
            }
        }

        val requestBody = buildJsonObject {
            put("model", "claude-sonnet-4-6")
            put("max_tokens", 1024)
            put("stream", true)
            put("system", SYSTEM_PROMPT)
            put("messages", requestMessages)
        }

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
            val fullText = StringBuilder()
            val lineBuffer = StringBuilder()

            while (!channel.isClosedForRead) {
                val available = channel.availableForRead
                if (available == 0) {
                    channel.awaitContent()
                    continue
                }
                val bytes = ByteArray(available)
                channel.readFully(bytes, 0, available)
                val text = bytes.toString(Charsets.UTF_8)

                for (char in text) {
                    if (char == '\n') {
                        val line = lineBuffer.toString()
                        lineBuffer.clear()
                        processLine(line, fullText, onChunk)
                    } else {
                        lineBuffer.append(char)
                    }
                }
            }

            // flush remaining buffer
            if (lineBuffer.isNotEmpty()) {
                processLine(lineBuffer.toString(), fullText, onChunk)
            }

            onComplete(fullText.toString())
        } catch (e: Exception) {
            logger.error("Anthropic API error", e)
            val errorMsg = "I'm having trouble connecting right now. " +
                "Please try again or reach out to Vinny at vinny@fullstackrealtynw.com."
            try {
                onChunk(errorMsg)
                onComplete(errorMsg)
            } catch (_: Exception) {
                // Client already disconnected, nothing to send
            }
        }
    }

    private suspend fun processLine(
        line: String,
        fullText: StringBuilder,
        onChunk: suspend (String) -> Unit,
    ) {
        if (!line.startsWith("data: ")) return
        val data = line.removePrefix("data: ").trim()
        if (data == "[DONE]") return

        try {
            val parsed = Json.parseToJsonElement(data).jsonObject
            val type = parsed["type"]?.jsonPrimitive?.content ?: return
            if (type == "content_block_delta") {
                val text = parsed["delta"]?.jsonObject?.get("text")?.jsonPrimitive?.content
                if (text != null) {
                    fullText.append(text)
                    onChunk(text)
                }
            }
        } catch (_: Exception) {
            // Skip unparseable events
        }
    }

    fun close() = client.close()
}
