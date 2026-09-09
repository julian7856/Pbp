package com.example.data.repository

import com.example.data.model.CityLocation
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherApiResponse
import com.example.data.model.WeatherCodeMapper
import com.example.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository {
    private val apiService = NetworkClient.weatherApiService

    suspend fun getForecast(latitude: Double, longitude: Double): Result<WeatherApiResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecast(latitude, longitude)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun searchCities(query: String): List<CityLocation> =
        withContext(Dispatchers.IO) {
            if (query.trim().length < 2) return@withContext emptyList()
            try {
                val response = apiService.searchCity(name = query.trim())
                response.results?.map { it.toCityLocation() } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    fun parseHourlyForecast(response: WeatherApiResponse): List<HourlyForecastItem> {
        val hourly = response.hourly ?: return emptyList()
        val times = hourly.time
        val temps = hourly.temperature
        val codes = hourly.weatherCode

        val list = mutableListOf<HourlyForecastItem>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h a", Locale.getDefault())

        val nowMs = System.currentTimeMillis() - 3600_000L // show starting from around current hour

        for (i in times.indices) {
            if (i >= temps.size || i >= codes.size) break
            try {
                val parsedDate = inputFormat.parse(times[i])
                if (parsedDate != null && parsedDate.time >= nowMs) {
                    val displayTime = if (list.isEmpty()) "Now" else outputFormat.format(parsedDate)
                    val cond = WeatherCodeMapper.getCondition(codes[i], isDay = true)
                    list.add(
                        HourlyForecastItem(
                            timeDisplay = displayTime,
                            temperature = temps[i],
                            weatherCode = codes[i],
                            conditionDescription = cond.description
                        )
                    )
                    if (list.size >= 24) break
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }
        return list
    }

    fun parseDailyForecast(response: WeatherApiResponse): List<DailyForecastItem> {
        val daily = response.daily ?: return emptyList()
        val times = daily.time
        val codes = daily.weatherCode
        val maxTemps = daily.temperatureMax
        val minTemps = daily.temperatureMin

        val list = mutableListOf<DailyForecastItem>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        for (i in times.indices) {
            if (i >= codes.size || i >= maxTemps.size || i >= minTemps.size) break
            try {
                val date = inputFormat.parse(times[i]) ?: Date()
                val dayName = if (i == 0) "Today" else dayFormat.format(date)
                val dateDisplay = dateFormat.format(date)
                val cond = WeatherCodeMapper.getCondition(codes[i], isDay = true)

                list.add(
                    DailyForecastItem(
                        dayName = dayName,
                        dateDisplay = dateDisplay,
                        maxTemp = maxTemps[i],
                        minTemp = minTemps[i],
                        weatherCode = codes[i],
                        conditionDescription = cond.description
                    )
                )
            } catch (_: Exception) {
                // Ignore
            }
        }
        return list
    }
}
