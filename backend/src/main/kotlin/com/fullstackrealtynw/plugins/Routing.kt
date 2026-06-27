package com.fullstackrealtynw.plugins

import com.fullstackrealtynw.routes.articleRoutes
import com.fullstackrealtynw.routes.chatRoutes
import com.fullstackrealtynw.secrets.SecretsLoader
import com.fullstackrealtynw.services.AnthropicService
import com.fullstackrealtynw.services.ArticleService
import com.fullstackrealtynw.services.ChatService
import com.fullstackrealtynw.services.EmailService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.serialization.json.*

fun Application.configureRouting() {
    install(SSE)

    val anthropicApiKey = SecretsLoader.resolve("ANTHROPIC_API_KEY", "")
    val smtpUser        = SecretsLoader.resolve("SMTP_USER", "vinny@fullstackrealtynw.com")
    val smtpPassword    = SecretsLoader.resolve("SMTP_PASSWORD", "")
    val notifyEmail     = SecretsLoader.resolve("NOTIFY_EMAIL", "vinny@fullstackrealtynw.com")

    val emailService    = EmailService(smtpUser = smtpUser, smtpPassword = smtpPassword, notifyEmail = notifyEmail)

    val anthropicService = AnthropicService(
        apiKey = anthropicApiKey,
        toolExecutor = { toolName, toolInput ->
            when (toolName) {
                "capture_lead" -> emailService.sendLead(toolInput)
                else           -> "Unknown tool: $toolName"
            }
        }
    )

    val articleService = ArticleService()
    val chatService    = ChatService(anthropicService)

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "Full Stack Realty NW API"))
        }

        route("/api") {
            articleRoutes(articleService)
            chatRoutes(chatService)
        }
    }
}
