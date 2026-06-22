package com.fullstackrealtynw.services

import com.fullstackrealtynw.models.AnthropicMessage
import com.fullstackrealtynw.models.ChatMessageDto
import com.fullstackrealtynw.models.ChatMessages
import com.fullstackrealtynw.models.ChatSessionDto
import com.fullstackrealtynw.models.ChatSessions
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class ChatService(private val anthropicService: AnthropicService) {

    fun createSession(): ChatSessionDto = transaction {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        ChatSessions.insert {
            it[ChatSessions.id] = id
            it[createdAt] = now
        }
        ChatSessionDto(id = id.toString(), createdAt = now.toString())
    }

    fun getSessionHistory(sessionId: String): List<ChatMessageDto> = transaction {
        val uuid = UUID.fromString(sessionId)
        ChatMessages.selectAll()
            .where { ChatMessages.sessionId eq uuid }
            .orderBy(ChatMessages.createdAt, SortOrder.ASC)
            .map { row ->
                ChatMessageDto(
                    id = row[ChatMessages.id],
                    sessionId = sessionId,
                    role = row[ChatMessages.role],
                    content = row[ChatMessages.content],
                    createdAt = row[ChatMessages.createdAt].toString(),
                )
            }
    }

    fun saveMessage(sessionId: String, role: String, content: String): ChatMessageDto = transaction {
        val uuid = UUID.fromString(sessionId)
        val now = LocalDateTime.now()
        val id = ChatMessages.insert {
            it[ChatMessages.sessionId] = uuid
            it[ChatMessages.role] = role
            it[ChatMessages.content] = content
            it[createdAt] = now
        } get ChatMessages.id
        ChatMessageDto(id = id, sessionId = sessionId, role = role, content = content, createdAt = now.toString())
    }

    fun getAnthropicHistory(sessionId: String): List<AnthropicMessage> = transaction {
        val uuid = UUID.fromString(sessionId)
        ChatMessages.selectAll()
            .where { ChatMessages.sessionId eq uuid }
            .orderBy(ChatMessages.createdAt, SortOrder.ASC)
            .map { row ->
                AnthropicMessage(
                    role = row[ChatMessages.role],
                    content = row[ChatMessages.content],
                )
            }
    }

    suspend fun streamReply(
        sessionId: String,
        userMessage: String,
        onChunk: suspend (String) -> Unit,
    ) {
        saveMessage(sessionId, "user", userMessage)
        val history = getAnthropicHistory(sessionId)

        anthropicService.streamCompletion(
            messages = history,
            onChunk = onChunk,
            onComplete = { fullResponse ->
                saveMessage(sessionId, "assistant", fullResponse)
            },
        )
    }
}
