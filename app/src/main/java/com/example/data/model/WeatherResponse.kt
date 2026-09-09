package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherApiResponse(
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val timezone: String? = "UTC",
    val current: CurrentWeatherUnits? = null,
    val hourly: HourlyWeatherData? = null,
    val daily: DailyWeatherData? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherUnits(
    val time: String? = "",
    @Json(name = "temperature_2m") val temperature: Double = 0.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity: Int = 0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
    @Json(name = "is_day") val isDay: Int = 1,
    val precipitation: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "wind_speed_10m") val windSpeed: Double = 0.0,
    @Json(name = "surface_pressure") val surfacePressure: Double = 1013.25
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherData(
    val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyWeatherData(
    val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double> = emptyList(),
    val sunrise: List<String>? = emptyList(),
    val sunset: List<String>? = emptyList()
)

// UI representation for hourly entry
data class HourlyForecastItem(
    val timeDisplay: String,
    val temperature: Double,
    val weatherCode: Int,
    val conditionDescription: String
)

// UI representation for daily entry
data class DailyForecastItem(
    val dayName: String,
    val dateDisplay: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val conditionDescription: String
)

data class WeatherConditionInfo(
    val description: String,
    val iconType: WeatherIconType
)

enum class WeatherIconType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    CLOUDY_DAY,
    CLOUDY_NIGHT,
    OVERCAST,
    FOG,
    RAIN_LIGHT,
    RAIN_HEAVY,
    SNOW,
    THUNDERSTORM
}

object WeatherCodeMapper {
    fun getCondition(code: Int, isDay: Boolean = true): WeatherConditionInfo {
        return when (code) {
            0 -> WeatherConditionInfo(
                "Clear Sky",
                if (isDay) WeatherIconType.CLEAR_DAY else WeatherIconType.CLEAR_NIGHT
            )
            1, 2 -> WeatherConditionInfo(
                "Partly Cloudy",
                if (isDay) WeatherIconType.CLOUDY_DAY else WeatherIconType.CLOUDY_NIGHT
            )
            3 -> WeatherConditionInfo("Overcast", WeatherIconType.OVERCAST)
            45, 48 -> WeatherConditionInfo("Foggy", WeatherIconType.FOG)
            51, 53, 55 -> WeatherConditionInfo("Drizzle", WeatherIconType.RAIN_LIGHT)
            61, 63 -> WeatherConditionInfo("Moderate Rain", WeatherIconType.RAIN_LIGHT)
            65 -> WeatherConditionInfo("Heavy Rain", WeatherIconType.RAIN_HEAVY)
            71, 73, 75, 77 -> WeatherConditionInfo("Snow", WeatherIconType.SNOW)
            80, 81, 82 -> WeatherConditionInfo("Rain Showers", WeatherIconType.RAIN_HEAVY)
            85, 86 -> WeatherConditionInfo("Snow Showers", WeatherIconType.SNOW)
            95 -> WeatherConditionInfo("Thunderstorm", WeatherIconType.THUNDERSTORM)
            96, 99 -> WeatherConditionInfo("Severe Thunderstorm", WeatherIconType.THUNDERSTORM)
            else -> WeatherConditionInfo(
                "Partly Cloudy",
                if (isDay) WeatherIconType.CLOUDY_DAY else WeatherIconType.CLOUDY_NIGHT
            )
        }
    }
}
