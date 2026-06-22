package com.fullstackrealtynw.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Articles : Table("articles") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val slug = varchar("slug", 255).uniqueIndex()
    val excerpt = text("excerpt").nullable()
    val content = text("content")
    val author = varchar("author", 100).default("Vinny Rao")
    val category = varchar("category", 100).nullable()
    val imageUrl = varchar("image_url", 500).nullable()
    val readTimeMinutes = integer("read_time_minutes").default(5)
    val publishedAt = datetime("published_at")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ArticleDto(
    val id: Int,
    val title: String,
    val slug: String,
    val excerpt: String?,
    val content: String,
    val author: String,
    val category: String?,
    val imageUrl: String?,
    val readTimeMinutes: Int,
    val publishedAt: String,
)

@Serializable
data class ArticleSummaryDto(
    val id: Int,
    val title: String,
    val slug: String,
    val excerpt: String?,
    val author: String,
    val category: String?,
    val imageUrl: String?,
    val readTimeMinutes: Int,
    val publishedAt: String,
)
