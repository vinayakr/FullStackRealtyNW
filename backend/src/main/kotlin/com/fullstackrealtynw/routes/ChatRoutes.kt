package com.fullstackrealtynw.routes

import com.fullstackrealtynw.models.SendMessageRequest
import com.fullstackrealtynw.services.ChatService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun Route.chatRoutes(chatService: ChatService) {
    route("/chat") {
        post("/sessions") {
            val session = chatService.createSession()
            call.respond(HttpStatusCode.Created, session)
        }

        get("/sessions/{sessionId}/messages") {
            val sessionId = call.parameters["sessionId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing session ID"))
            val messages = chatService.getSessionHistory(sessionId)
            call.respond(messages)
        }

        sse("/sessions/{sessionId}/stream") {
            // SSE GET endpoint — client opens with ?message=...
            val sessionId = call.parameters["sessionId"]
                ?: run { close(); return@sse }
            val userMessage = call.request.queryParameters["message"]
                ?: run { close(); return@sse }

            try {
                chatService.streamReply(sessionId, userMessage) { chunk ->
                    val data = Json.encodeToString(buildJsonObject { put("text", chunk) })
                    send(ServerSentEvent(data = data))
                }
                send(ServerSentEvent(data = "[DONE]"))
            } catch (e: Exception) {
                val errData = Json.encodeToString(buildJsonObject { put("error", e.message ?: "Stream error") })
                send(ServerSentEvent(data = errData))
                send(ServerSentEvent(data = "[DONE]"))
            }
        }

        // POST to save message, then client opens SSE stream
        post("/sessions/{sessionId}/messages") {
            val sessionId = call.parameters["sessionId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing session ID"))

            val request = try {
                call.receive<SendMessageRequest>()
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
            }

            if (request.content.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Message content cannot be empty"))
            }

            // Save user message and stream response
            call.response.header(HttpHeaders.ContentType, "text/event-stream; charset=utf-8")
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.response.header(HttpHeaders.Connection, "keep-alive")
            call.response.header("X-Accel-Buffering", "no")

            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                try {
                    chatService.streamReply(sessionId, request.content) { chunk ->
                        val data = Json.encodeToString(buildJsonObject { put("text", chunk) })
                        write("data: $data\n\n")
                        flush()
                    }
                    write("data: [DONE]\n\n")
                    flush()
                } catch (e: Exception) {
                    val errData = Json.encodeToString(buildJsonObject { put("error", e.message ?: "Stream error") })
                    write("data: $errData\n\n")
                    write("data: [DONE]\n\n")
                    flush()
                }
            }
        }
    }
}
