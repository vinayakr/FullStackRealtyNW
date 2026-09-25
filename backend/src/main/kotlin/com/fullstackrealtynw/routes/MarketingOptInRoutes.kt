package com.fullstackrealtynw.routes

import com.fullstackrealtynw.models.MarketingOptInRequest
import com.fullstackrealtynw.models.MarketingOptIns
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.marketingOptInRoutes() {
    post("/marketing/opt-in") {
        val req = try {
            call.receive<MarketingOptInRequest>()
        } catch (e: Exception) {
            return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "email is required"))
        }

        val email = req.email.trim().lowercase()
        if (email.isBlank() || !email.contains("@")) {
            return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "a valid email is required"))
        }

        try {
            transaction {
                MarketingOptIns.insert {
                    it[MarketingOptIns.email] = email
                    it[MarketingOptIns.name] = req.name?.trim()?.takeIf { n -> n.isNotBlank() }
                    it[MarketingOptIns.interests] = req.interests.joinToString(",")
                    it[MarketingOptIns.optInSource] = "website"
                    it[MarketingOptIns.optedInAt] = LocalDateTime.now()
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("status" to "subscribed"))
        } catch (e: ExposedSQLException) {
            // unique constraint violation = already subscribed
            if (e.message?.contains("unique", ignoreCase = true) == true ||
                e.message?.contains("duplicate", ignoreCase = true) == true) {
                call.respond(HttpStatusCode.Conflict, mapOf("status" to "already_subscribed"))
            } else {
                throw e
            }
        }
    }
}
