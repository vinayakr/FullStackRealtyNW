package com.fullstackrealtynw.services

import com.fullstackrealtynw.models.Listing
import com.fullstackrealtynw.models.ListingSearchParams
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

// ── Schema ────────────────────────────────────────────────────────────────────

object ListingCache : Table("listing_cache") {
    val cacheKey  = varchar("cache_key", 255)
    val responseJson = text("response_json")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(cacheKey)
}

// ── Service ───────────────────────────────────────────────────────────────────

class ListingsService(private val rapidApiKey: String) {
    private val logger = LoggerFactory.getLogger(ListingsService::class.java)
    private val isStub = rapidApiKey.isBlank() || rapidApiKey == "dummy"

    private val client by lazy {
        HttpClient(CIO) { engine { requestTimeout = 15_000 } }
    }

    suspend fun search(params: ListingSearchParams): List<Listing> {
        if (isStub) {
            logger.info("ListingsService stub mode — returning hardcoded listings")
            return stubListings(params)
        }
        val cacheKey = params.toCacheKey()
        getCached(cacheKey)?.let { return it }
        val fresh = fetchFromApi(params)
        putCached(cacheKey, fresh)
        return fresh
    }

    private fun getCached(key: String): List<Listing>? = transaction {
        val cutoff = LocalDateTime.now().minusHours(24)
        ListingCache.selectAll()
            .where { (ListingCache.cacheKey eq key) and (ListingCache.createdAt greaterEq cutoff) }
            .firstOrNull()
            ?.let { Json.decodeFromString(it[ListingCache.responseJson]) }
    }

    private fun putCached(key: String, listings: List<Listing>) = transaction {
        ListingCache.upsert {
            it[cacheKey] = key
            it[responseJson] = Json.encodeToString(listings)
            it[createdAt] = LocalDateTime.now()
        }
    }

    // ── Real API call ─────────────────────────────────────────────────────────
    // TODO: Update endpoint + response parsing once Realtor.com API docs confirmed.
    // Host: realtor-com4.p.rapidapi.com
    private suspend fun fetchFromApi(params: ListingSearchParams): List<Listing> {
        logger.info("Calling Realtor.com API for: $params")
        return try {
            val response = client.get("https://realtor-com4.p.rapidapi.com/properties/search") {
                headers {
                    append("X-RapidAPI-Key", rapidApiKey)
                    append("X-RapidAPI-Host", "realtor-com4.p.rapidapi.com")
                }
                url {
                    parameters.append("location", params.location)
                    params.bedsMin.let { parameters.append("beds_min", it.toString()) }
                    params.priceMin?.let { parameters.append("price_min", it.toString()) }
                    params.priceMax?.let { parameters.append("price_max", it.toString()) }
                    params.bathsMin?.let { parameters.append("baths_min", it.toString()) }
                    parameters.append("status", "for_sale")
                }
            }
            parseApiResponse(response.bodyAsText())
        } catch (e: Exception) {
            logger.error("Realtor.com API error", e)
            emptyList()
        }
    }

    // TODO: Update field names once API response shape is confirmed from docs.
    private fun parseApiResponse(json: String): List<Listing> = try {
        val root = Json.parseToJsonElement(json).jsonObject
        val results = root["data"]?.jsonObject?.get("results")?.jsonArray
            ?: root["results"]?.jsonArray
            ?: return emptyList()

        results.take(6).mapNotNull { el ->
            val obj = el.jsonObject
            val location = obj["location"]?.jsonObject
            val address = location?.get("address")?.jsonObject
            val price = obj["list_price"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            Listing(
                address   = address?.get("line")?.jsonPrimitive?.content ?: "",
                city      = address?.get("city")?.jsonPrimitive?.content ?: "",
                state     = address?.get("state_code")?.jsonPrimitive?.content ?: "",
                zip       = address?.get("postal_code")?.jsonPrimitive?.content ?: "",
                price     = price,
                beds      = obj["description"]?.jsonObject?.get("beds")?.jsonPrimitive?.intOrNull ?: 0,
                baths     = obj["description"]?.jsonObject?.get("baths_consolidated")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                sqft      = obj["description"]?.jsonObject?.get("sqft")?.jsonPrimitive?.intOrNull,
                photoUrl  = obj["primary_photo"]?.jsonObject?.get("href")?.jsonPrimitive?.content,
                listingUrl = "https://www.realtor.com/realestateandhomes-detail/${obj["permalink"]?.jsonPrimitive?.content ?: ""}",
                daysOnMarket = obj["list_date"]?.jsonPrimitive?.content?.let { null },
            )
        }
    } catch (e: Exception) {
        logger.error("Failed to parse Realtor.com response", e)
        emptyList()
    }

    // ── Stub data ─────────────────────────────────────────────────────────────

    private fun stubListings(params: ListingSearchParams): List<Listing> {
        val all = listOf(
            Listing("4821 102nd Ave NE", "Kirkland",  "WA", "98033", 749000, 3, 2.0,  1920, null, "https://www.realtor.com/realestateandhomes-detail/stub-1", 8),
            Listing("11203 NE 24th St",  "Bellevue",  "WA", "98004", 1150000, 4, 2.5, 2640, null, "https://www.realtor.com/realestateandhomes-detail/stub-2", 3),
            Listing("2214 228th St SE",  "Bothell",   "WA", "98021", 624000, 3, 1.75, 1680, null, "https://www.realtor.com/realestateandhomes-detail/stub-3", 14),
            Listing("8833 Stone Ave N",  "Seattle",   "WA", "98103", 872000, 3, 2.0,  1540, null, "https://www.realtor.com/realestateandhomes-detail/stub-4", 5),
            Listing("7102 Education Hill Dr", "Redmond","WA","98052", 895000, 4, 2.5, 2280, null, "https://www.realtor.com/realestateandhomes-detail/stub-5", 11),
            Listing("3401 S 374th Pl",   "Auburn",    "WA", "98001", 498000, 3, 2.0,  1440, null, "https://www.realtor.com/realestateandhomes-detail/stub-6", 20),
            Listing("5219 N Shirley St", "Tacoma",    "WA", "98407", 425000, 3, 1.0,  1320, null, "https://www.realtor.com/realestateandhomes-detail/stub-7", 7),
            Listing("14822 Interurban Ave S", "Tukwila","WA","98168", 559000, 4, 2.0, 1860, null, "https://www.realtor.com/realestateandhomes-detail/stub-8", 9),
        )
        val loc = params.location.lowercase()
        val filtered = all.filter { l ->
            val cityMatch = loc.isEmpty() || l.city.lowercase().contains(loc) || loc.contains(l.city.lowercase())
            val priceMatch = (params.priceMin == null || l.price >= params.priceMin) &&
                             (params.priceMax == null || l.price <= params.priceMax)
            val bedsMatch  = l.beds >= params.bedsMin
            cityMatch && priceMatch && bedsMatch
        }
        return filtered.ifEmpty { all.take(4) }
    }

    private fun ListingSearchParams.toCacheKey() =
        "${location.lowercase().trim()}|${priceMin ?: 0}|${priceMax ?: 9999999}|${bedsMin}|${bathsMin ?: 0}"
}
