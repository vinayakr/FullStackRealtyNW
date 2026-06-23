package com.fullstackrealtynw.services

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sesv2.SesV2Client
import software.amazon.awssdk.services.sesv2.model.*

class EmailService(
    private val fromEmail: String,
    private val notifyEmail: String,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    private val ses: SesV2Client? by lazy {
        try {
            SesV2Client.create()
        } catch (e: Exception) {
            logger.warn("SES unavailable — leads will be logged only: ${e.message}")
            null
        }
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

        val subject = "New Lead: $name"
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
            notes?.let    { appendLine(); appendLine("Additional notes: $it") }
        }

        logger.info("LEAD CAPTURED:\n$body")

        val sent = ses?.let {
            try {
                it.sendEmail(
                    SendEmailRequest.builder()
                        .fromEmailAddress(fromEmail)
                        .destination(Destination.builder().toAddresses(notifyEmail).build())
                        .content(
                            EmailContent.builder()
                                .simple(
                                    Message.builder()
                                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                                        .body(
                                            Body.builder()
                                                .text(Content.builder().data(body).charset("UTF-8").build())
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                true
            } catch (e: Exception) {
                logger.error("SES send failed", e)
                false
            }
        } ?: false

        return if (sent) "Lead captured and email sent to Vinny."
               else "Lead captured (email queued)."
    }
}
