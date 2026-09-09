package com.example.data.model

data class CityLocation(
    val name: String,
    val country: String = "",
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "auto"
) {
    val displayName: String
        get() = if (country.isNotBlank()) "$name, $country" else name
}

val defaultCities = listOf(
    CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
    CityLocation("Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
    CityLocation("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
    CityLocation("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
    CityLocation("San Francisco", "United States", 37.7749, -122.4194, "America/Los_Angeles"),
    CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
    CityLocation("Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo")
)
