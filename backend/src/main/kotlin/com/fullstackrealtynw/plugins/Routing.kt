package com.fullstackrealtynw.plugins

import com.fullstackrealtynw.routes.articleRoutes
import com.fullstackrealtynw.routes.chatRoutes
import com.fullstackrealtynw.secrets.SecretsLoader
import com.fullstackrealtynw.services.AnthropicService
import com.fullstackrealtynw.services.ArticleService
import com.fullstackrealtynw.services.ChatService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*

fun Application.configureRouting() {
    install(SSE)

    val anthropicApiKey = SecretsLoader.resolve("ANTHROPIC_API_KEY", "")
    val anthropicService = AnthropicService(anthropicApiKey)
    val articleService = ArticleService()
    val chatService = ChatService(anthropicService)

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
