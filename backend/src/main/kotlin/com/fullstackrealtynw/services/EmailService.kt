package com.fullstackrealtynw.services

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*

class EmailService(
    private val fromEmail: String,
    private val notifyEmail: String,
    private val region: String,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    private val ses by lazy {
        SesClient.builder().region(Region.of(region)).build()
    }

    private fun send(subject: String, body: String): String {
        return try {
            ses.sendEmail(
                SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(notifyEmail).build())
                    .message(
                        Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder().text(Content.builder().data(body).build()).build())
                            .build()
                    )
                    .build()
            )
            "Email sent."
        } catch (e: Exception) {
            logger.error("Failed to send email", e)
            "Email delivery failed — check backend logs."
        }
    }

    fun sendContact(name: String, email: String, message: String): String {
        val body = buildString {
            appendLine("New contact form submission from fullstackrealtynw.com")
            appendLine("=".repeat(50))
            appendLine()
            appendLine("From: $name <$email>")
            appendLine()
            appendLine("Message:")
            appendLine(message)
        }
        logger.info("CONTACT FORM:\n$body")
        return send("Contact from $name", body)
    }

    fun sendLead(input: JsonObject): String {
        val name     = input["name"]?.jsonPrimitive?.content ?: "Unknown"
        val email    = input["email"]?.jsonPrimitive?.content ?: "No email provided"
        val phone    = input["phone"]?.jsonPrimitive?.content
        val summary  = input["summary"]?.jsonPrimitive?.content ?: ""
        val budget   = input["budget"]?.jsonPrimitive?.content
        val location = input["location"]?.jsonPrimitive?.content
        val bedrooms = input["bedrooms"]?.jsonPrimitive?.content
        val timeline = input["timeline"]?.jsonPrimitive?.content
        val notes    = input["additional_notes"]?.jsonPrimitive?.content

        val body = buildString {
            appendLine("New lead from fullstackrealtynw.com AI chat")
            appendLine("=".repeat(50))
            appendLine()
            appendLine("CONTACT INFO")
            appendLine("Name:   $name")
            appendLine("Email:  $email")
            phone?.let    { appendLine("Phone:  $it") }
            appendLine()
            appendLine("WHAT THEY'RE LOOKING FOR")
            appendLine(summary)
            appendLine()
            budget?.let   { appendLine("Budget:    $it") }
            location?.let { appendLine("Location:  $it") }
            bedrooms?.let { appendLine("Bedrooms:  $it") }
            timeline?.let { appendLine("Timeline:  $it") }
            notes?.let    { appendLine(); appendLine("Notes: $it") }
        }

        logger.info("LEAD CAPTURED:\n$body")
        return send("New Lead: $name", body)
    }
}
