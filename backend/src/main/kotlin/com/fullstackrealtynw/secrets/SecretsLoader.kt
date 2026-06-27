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
 *   FOO_SECRET — one of:
 *     "my/secret/name"          → fetches plain-text secret, returns whole value
 *     "my/secret/name#key-name" → fetches JSON key-value secret, returns value at "key-name"
 *
 * FOO always wins if set; FOO_SECRET is only consulted when FOO is absent.
 * The raw secret string is cached so AWS is hit at most once per secret name per run.
 */
object SecretsLoader {
    private val log = LoggerFactory.getLogger(SecretsLoader::class.java)
    private val cache = ConcurrentHashMap<String, String>()

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

    fun resolve(name: String): String? {
        val direct = System.getenv(name)
        if (!direct.isNullOrBlank()) return direct

        val ref = System.getenv("${name}_SECRET")?.takeIf { it.isNotBlank() } ?: return null

        val hashIdx = ref.indexOf('#')
        return if (hashIdx >= 0) {
            val secretId = ref.substring(0, hashIdx)
            val jsonKey  = ref.substring(hashIdx + 1)
            val raw = cache.getOrPut(secretId) { fetchFromAws(secretId) ?: return null }
            extractJsonKey(raw, jsonKey, secretId)
        } else {
            cache.getOrPut(ref) { fetchFromAws(ref) ?: return null }
        }
    }

    fun resolve(name: String, default: String): String = resolve(name) ?: default

    private fun extractJsonKey(json: String, key: String, secretId: String): String? {
        // Simple JSON key extraction — avoids pulling in a full JSON library at this layer.
        // Handles the standard AWS key-value format: {"key":"value",...}
        val pattern = Regex(""""${Regex.escape(key)}"\s*:\s*"((?:[^"\\]|\\.)*)"""")
        val match = pattern.find(json)
        if (match == null) {
            log.error("Key '$key' not found in secret '$secretId'")
            return null
        }
        return match.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
    }

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
