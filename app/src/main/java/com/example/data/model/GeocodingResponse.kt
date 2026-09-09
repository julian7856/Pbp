package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingSearchResponse(
    val results: List<GeocodingResultItem>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResultItem(
    val id: Long? = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = "",
    @Json(name = "country_code") val countryCode: String? = "",
    val admin1: String? = "",
    val timezone: String? = "UTC"
) {
    fun toCityLocation(): CityLocation {
        val region = if (!admin1.isNullOrBlank() && admin1 != name) "$admin1, " else ""
        val countryStr = country ?: countryCode ?: ""
        return CityLocation(
            name = name,
            country = "$region$countryStr".trimEnd(',', ' '),
            latitude = latitude,
            longitude = longitude,
            timezone = timezone ?: "auto"
        )
    }
}
