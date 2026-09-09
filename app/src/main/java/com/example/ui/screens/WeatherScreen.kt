package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CityLocation
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.HappyCommunityFooter
import com.example.ui.components.HappyCommunityHeaderBanner
import com.example.ui.components.HourlyForecastStrip
import com.example.ui.components.PrimaryWeatherCard
import com.example.ui.components.WeatherMetricsRow
import com.example.ui.viewmodel.WeatherUiState

@Composable
fun WeatherScreen(
    currentCity: CityLocation,
    savedCities: List<CityLocation>,
    weatherState: WeatherUiState,
    isCelsius: Boolean,
    onCitySelected: (CityLocation) -> Unit,
    onToggleUnit: () -> Unit,
    onRefresh: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("weather_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // HappyCommunity Header Banner
        item {
            HappyCommunityHeaderBanner()
        }

        // Quick City Switcher and Search
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedCities.take(6)) { city ->
                        val isSelected = city.name == currentCity.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCitySelected(city) },
                            label = {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("weather_city_chip_${city.name.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.testTag("weather_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Locations",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedButton(
                    onClick = onToggleUnit,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("weather_unit_toggle_button")
                ) {
                    Text(
                        text = if (isCelsius) "°C" else "°F",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Main Weather Content or Loading/Error State
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (weatherState.isLoading && weatherState.weather == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("weather_loading_indicator"))
                    }
                } else if (weatherState.weather != null && weatherState.weather.current != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PrimaryWeatherCard(
                            current = weatherState.weather.current,
                            cityName = currentCity.name,
                            countryName = currentCity.country,
                            isCelsius = isCelsius
                        )

                        WeatherMetricsRow(current = weatherState.weather.current)

                        if (weatherState.hourly.isNotEmpty()) {
                            HourlyForecastStrip(
                                hourlyItems = weatherState.hourly,
                                isCelsius = isCelsius
                            )
                        }

                        if (weatherState.daily.isNotEmpty()) {
                            DailyForecastCard(
                                dailyItems = weatherState.daily,
                                isCelsius = isCelsius
                            )
                        }
                    }
                } else {
                    // Error state with retry
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = weatherState.errorMessage ?: "Unable to fetch live weather forecast.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ElevatedButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("weather_retry_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }

        // HappyCommunity Footer
        item {
            HappyCommunityFooter()
        }
    }
}
