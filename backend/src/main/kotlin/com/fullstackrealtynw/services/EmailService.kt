package com.fullstackrealtynw.services

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

class EmailService(
    private val smtpUser: String,
    private val smtpPassword: String,
    private val notifyEmail: String,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    private val enabled = smtpPassword.isNotBlank()

    private val mailer by lazy {
        MailerBuilder
            .withSMTPServer("smtp.gmail.com", 587, smtpUser, smtpPassword)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withSessionTimeout(10_000)
            .buildMailer()
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

        if (!enabled) return "Message received (email not configured — check backend logs)."

        return try {
            val mail = EmailBuilder.startingBlank()
                .from("Full Stack Realty NW", smtpUser)
                .to(notifyEmail)
                .withSubject("Contact from $name")
                .withPlainText(body)
                .buildEmail()
            mailer.sendMail(mail)
            "Message sent."
        } catch (e: Exception) {
            logger.error("Failed to send contact email", e)
            "Message received (email delivery failed — check backend logs)."
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

        if (!enabled) return "Lead captured (email not configured — check backend logs)."

        return try {
            val mail = EmailBuilder.startingBlank()
                .from("Full Stack Realty NW", smtpUser)
                .to(notifyEmail)
                .withSubject("New Lead: $name")
                .withPlainText(body)
                .buildEmail()
            mailer.sendMail(mail)
            "Lead captured and email sent to Vinny."
        } catch (e: Exception) {
            logger.error("Failed to send lead email", e)
            "Lead captured (email delivery failed — check backend logs)."
        }
    }
}
