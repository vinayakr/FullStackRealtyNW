package com.fullstackrealtynw.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object MarketingOptIns : Table("marketing_opt_ins") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 100).nullable()
    val interests = text("interests").default("")
    val optInSource = varchar("source", 100).default("website")
    val optedInAt = datetime("opted_in_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class MarketingOptInRequest(
    val email: String,
    val name: String? = null,
    val interests: List<String> = emptyList(),
)
