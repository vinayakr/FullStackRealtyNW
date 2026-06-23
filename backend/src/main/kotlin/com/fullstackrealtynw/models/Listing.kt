package com.fullstackrealtynw.models

import kotlinx.serialization.Serializable

@Serializable
data class Listing(
    val address: String,
    val city: String,
    val state: String,
    val zip: String,
    val price: Int,
    val beds: Int,
    val baths: Double,
    val sqft: Int?,
    val photoUrl: String?,
    val listingUrl: String,
    val daysOnMarket: Int?,
)

@Serializable
data class ListingSearchParams(
    val location: String,
    val priceMin: Int? = null,
    val priceMax: Int? = null,
    val bedsMin: Int,
    val bathsMin: Double? = null,
)
