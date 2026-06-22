package com.fullstackrealtynw.services

import com.fullstackrealtynw.models.ArticleDto
import com.fullstackrealtynw.models.ArticleSummaryDto
import com.fullstackrealtynw.models.Articles
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ArticleService {

    fun getAllArticles(): List<ArticleSummaryDto> = transaction {
        Articles.selectAll()
            .orderBy(Articles.publishedAt)
            .map(::toSummaryDto)
    }

    fun getArticleBySlug(slug: String): ArticleDto? = transaction {
        Articles.selectAll()
            .where { Articles.slug eq slug }
            .singleOrNull()
            ?.let { row ->
                ArticleDto(
                    id = row[Articles.id],
                    title = row[Articles.title],
                    slug = row[Articles.slug],
                    excerpt = row[Articles.excerpt],
                    content = row[Articles.content],
                    author = row[Articles.author],
                    category = row[Articles.category],
                    imageUrl = row[Articles.imageUrl],
                    readTimeMinutes = row[Articles.readTimeMinutes],
                    publishedAt = row[Articles.publishedAt].toString(),
                )
            }
    }

    fun getArticlesByCategory(category: String): List<ArticleSummaryDto> = transaction {
        Articles.selectAll()
            .where { Articles.category eq category }
            .orderBy(Articles.publishedAt)
            .map(::toSummaryDto)
    }

    private fun toSummaryDto(row: org.jetbrains.exposed.sql.ResultRow) = ArticleSummaryDto(
        id = row[Articles.id],
        title = row[Articles.title],
        slug = row[Articles.slug],
        excerpt = row[Articles.excerpt],
        author = row[Articles.author],
        category = row[Articles.category],
        imageUrl = row[Articles.imageUrl],
        readTimeMinutes = row[Articles.readTimeMinutes],
        publishedAt = row[Articles.publishedAt].toString(),
    )
}
