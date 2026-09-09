package com.example.data.model

data class ClockZone(
    val id: String,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val isPrimary: Boolean = false
)

val defaultWorldClocks = listOf(
    ClockZone(id = "local", cityName = "Local Time", countryName = "Device Time", timeZoneId = "", isPrimary = true),
    ClockZone(id = "utc", cityName = "UTC / GMT", countryName = "Universal", timeZoneId = "UTC"),
    ClockZone(id = "nyc", cityName = "New York", countryName = "United States", timeZoneId = "America/New_York"),
    ClockZone(id = "lon", cityName = "London", countryName = "United Kingdom", timeZoneId = "Europe/London"),
    ClockZone(id = "par", cityName = "Paris", countryName = "France", timeZoneId = "Europe/Paris"),
    ClockZone(id = "dxb", cityName = "Dubai", countryName = "UAE", timeZoneId = "Asia/Dubai"),
    ClockZone(id = "tyo", cityName = "Tokyo", countryName = "Japan", timeZoneId = "Asia/Tokyo"),
    ClockZone(id = "syd", cityName = "Sydney", countryName = "Australia", timeZoneId = "Australia/Sydney"),
    ClockZone(id = "sfo", cityName = "San Francisco", countryName = "United States", timeZoneId = "America/Los_Angeles"),
    ClockZone(id = "cai", cityName = "Cairo", countryName = "Egypt", timeZoneId = "Africa/Cairo")
)
