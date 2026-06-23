package com.fullstackrealtynw.plugins

import com.fullstackrealtynw.models.ListingSearchParams
import com.fullstackrealtynw.routes.articleRoutes
import com.fullstackrealtynw.routes.chatRoutes
import com.fullstackrealtynw.secrets.SecretsLoader
import com.fullstackrealtynw.services.AnthropicService
import com.fullstackrealtynw.services.ArticleService
import com.fullstackrealtynw.services.ChatService
import com.fullstackrealtynw.services.EmailService
import com.fullstackrealtynw.services.ListingsService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

fun Application.configureRouting() {
    install(SSE)

    val anthropicApiKey = SecretsLoader.resolve("ANTHROPIC_API_KEY", "")
    val rapidApiKey     = SecretsLoader.resolve("RAPIDAPI_KEY", "dummy")
    val fromEmail       = SecretsLoader.resolve("SES_FROM_EMAIL", "vinny@fullstackrealtynw.com")
    val notifyEmail     = SecretsLoader.resolve("NOTIFY_EMAIL", "vinny@fullstackrealtynw.com")

    val emailService    = EmailService(fromEmail = fromEmail, notifyEmail = notifyEmail)
    val listingsService = ListingsService(rapidApiKey)

    val anthropicService = AnthropicService(
        apiKey = anthropicApiKey,
        toolExecutor = { toolName, toolInput ->
            when (toolName) {
                "capture_lead"    -> emailService.sendLead(toolInput)
                "search_listings" -> {
                    val params = ListingSearchParams(
                        location  = toolInput["location"]?.jsonPrimitive?.content ?: "",
                        priceMin  = toolInput["price_min"]?.jsonPrimitive?.intOrNull,
                        priceMax  = toolInput["price_max"]?.jsonPrimitive?.intOrNull,
                        bedsMin   = toolInput["beds_min"]?.jsonPrimitive?.intOrNull ?: 1,
                        bathsMin  = toolInput["baths_min"]?.jsonPrimitive?.doubleOrNull,
                    )
                    Json.encodeToString(listingsService.search(params))
                }
                else -> "Unknown tool: $toolName"
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
