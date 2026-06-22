package com.fullstackrealtynw.plugins

import com.fullstackrealtynw.models.Articles
import com.fullstackrealtynw.models.ChatMessages
import com.fullstackrealtynw.models.ChatSessions
import com.fullstackrealtynw.secrets.SecretsLoader
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val dbUrl = SecretsLoader.resolve("DATABASE_URL", "jdbc:postgresql://localhost:5432/fullstackrealtynw")
    val dbUser = SecretsLoader.resolve("DATABASE_USER", "realty_user")
    val dbPassword = SecretsLoader.resolve("DATABASE_PASSWORD", "realty_password")

    val config = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    // Ensure tables exist (init.sql handles seeding via Docker, but this handles schema for non-Docker)
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Articles, ChatSessions, ChatMessages)
    }

    log.info("Database connected: $dbUrl")
}
