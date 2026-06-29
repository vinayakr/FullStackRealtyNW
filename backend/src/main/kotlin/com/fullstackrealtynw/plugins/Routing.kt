package com.fullstackrealtynw.plugins

import com.fullstackrealtynw.models.ContactRequest
import com.fullstackrealtynw.routes.articleRoutes
import com.fullstackrealtynw.routes.chatRoutes
import com.fullstackrealtynw.secrets.SecretsLoader
import com.fullstackrealtynw.services.AnthropicService
import com.fullstackrealtynw.services.ArticleService
import com.fullstackrealtynw.services.ChatService
import com.fullstackrealtynw.services.EmailService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.serialization.json.*

fun Application.configureRouting() {
    install(SSE)

    val anthropicApiKey = SecretsLoader.resolve("ANTHROPIC_API_KEY", "")
    val fromEmail       = SecretsLoader.resolve("SMTP_USER", "vinny@fullstackrealtynw.com")
    val notifyEmail     = SecretsLoader.resolve("NOTIFY_EMAIL", "vinny@fullstackrealtynw.com")
    val awsRegion       = System.getenv("AWS_REGION") ?: "us-west-2"

    val emailService    = EmailService(fromEmail = fromEmail, notifyEmail = notifyEmail, region = awsRegion)

    val anthropicService = AnthropicService(
        apiKey = anthropicApiKey,
        toolExecutor = { toolName, toolInput ->
            when (toolName) {
                "capture_lead" -> emailService.sendLead(toolInput)
                else           -> "Unknown tool: $toolName"
            }
        }
    )

    val siteUrl        = SecretsLoader.resolve("SITE_URL", "https://fullstackrealtynw.com")
    val articleService = ArticleService()
    val chatService    = ChatService(anthropicService)

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "Full Stack Realty NW API"))
        }

        get("/sitemap.xml") {
            val articles = articleService.getAllArticles()
            val staticUrls = listOf("", "/articles", "/chat")
            val xml = buildString {
                appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
                appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
                staticUrls.forEach { path ->
                    appendLine("  <url><loc>$siteUrl$path</loc><changefreq>weekly</changefreq></url>")
                }
                articles.forEach { article ->
                    appendLine("  <url><loc>$siteUrl/articles/${article.slug}</loc><changefreq>monthly</changefreq></url>")
                }
                append("</urlset>")
            }
            call.respondText(xml, ContentType.Text.Xml)
        }

        route("/api") {
            articleRoutes(articleService)
            chatRoutes(chatService)

            post("/contact") {
                val req = try {
                    call.receive<ContactRequest>()
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "name, email, and message are required"))
                }
                if (req.name.isBlank() || req.email.isBlank() || req.message.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "name, email, and message are required"))
                }
                val result = emailService.sendContact(req.name, req.email, req.message)
                call.respond(mapOf("status" to result))
            }
        }
    }
}
