package com.fullstackrealtynw.plugins

import com.fullstackrealtynw.secrets.SecretsLoader
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

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

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    Database.connect(dataSource)

    log.info("Database connected and migrations applied: $dbUrl")
}
