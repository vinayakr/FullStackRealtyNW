package com.fullstackrealtynw.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ChatSessions : Table("chat_sessions") {
    val id = uuid("id")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ChatMessages : Table("chat_messages") {
    val id = integer("id").autoIncrement()
    val sessionId = uuid("session_id").references(ChatSessions.id)
    val role = varchar("role", 20)
    val content = text("content")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ChatSessionDto(val id: String, val createdAt: String)

@Serializable
data class ChatMessageDto(
    val id: Int,
    val sessionId: String,
    val role: String,
    val content: String,
    val createdAt: String,
)

@Serializable
data class SendMessageRequest(val content: String)

@Serializable
data class AnthropicMessage(val role: String, val content: String)

@Serializable
data class ContactRequest(val name: String, val email: String, val message: String)
