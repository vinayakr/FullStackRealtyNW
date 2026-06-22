package com.fullstackrealtynw.routes

import com.fullstackrealtynw.services.ArticleService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.articleRoutes(articleService: ArticleService) {
    route("/articles") {
        get {
            val category = call.request.queryParameters["category"]
            val articles = if (category != null) {
                articleService.getArticlesByCategory(category)
            } else {
                articleService.getAllArticles()
            }
            call.respond(articles)
        }

        get("/{slug}") {
            val slug = call.parameters["slug"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing slug")
            )
            val article = articleService.getArticleBySlug(slug)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Article not found"))
            call.respond(article)
        }
    }
}
