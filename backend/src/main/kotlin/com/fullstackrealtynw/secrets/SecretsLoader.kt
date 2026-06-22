package com.fullstackrealtynw.secrets

import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves secret values from either a plain environment variable or AWS Secrets Manager.
 *
 * Convention:
 *   FOO        — the secret value itself (local dev / simple deployments)
 *   FOO_SECRET — an AWS Secrets Manager secret name/ARN whose value is the secret
 *
 * FOO always wins if set; FOO_SECRET is only consulted when FOO is absent.
 * Results are cached in-process so AWS is hit at most once per secret per run.
 */
object SecretsLoader {
    private val log = LoggerFactory.getLogger(SecretsLoader::class.java)
    private val cache = ConcurrentHashMap<String, String>()

    // Lazily create the AWS client only when a _SECRET var is actually present.
    private val awsClient: SecretsManagerClient? by lazy {
        val needsAws = System.getenv().keys.any { it.endsWith("_SECRET") && !System.getenv(it).isNullOrBlank() }
        if (!needsAws) return@lazy null

        try {
            SecretsManagerClient.create().also {
                log.info("AWS Secrets Manager client initialised (region: ${System.getenv("AWS_REGION") ?: "default"})")
            }
        } catch (e: Exception) {
            log.error("Could not create AWS Secrets Manager client — check AWS credentials/region: ${e.message}")
            null
        }
    }

    /**
     * Resolve [name] using:
     *  1. env[name]         — plain value, returned as-is
     *  2. env[name_SECRET]  — treated as a Secrets Manager secret name; fetched from AWS
     *
     * Returns null if neither is set.
     */
    fun resolve(name: String): String? {
        val direct = System.getenv(name)
        if (!direct.isNullOrBlank()) return direct

        val secretId = System.getenv("${name}_SECRET")
            ?.takeIf { it.isNotBlank() } ?: return null

        return cache.getOrPut(secretId) {
            fetchFromAws(secretId) ?: return null
        }
    }

    /** Convenience wrapper that returns [default] when the secret is absent. */
    fun resolve(name: String, default: String): String = resolve(name) ?: default

    private fun fetchFromAws(secretId: String): String? {
        val client = awsClient ?: run {
            log.error("AWS client unavailable; cannot fetch secret '$secretId'")
            return null
        }
        return try {
            log.info("Fetching secret '$secretId' from AWS Secrets Manager")
            client.getSecretValue { it.secretId(secretId) }.secretString()
        } catch (e: ResourceNotFoundException) {
            log.error("Secret '$secretId' not found in AWS Secrets Manager")
            null
        } catch (e: Exception) {
            log.error("Failed to fetch secret '$secretId': ${e.message}")
            null
        }
    }
}
