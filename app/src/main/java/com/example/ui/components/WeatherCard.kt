package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrentWeatherUnits
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherIconType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.PrimaryBlue
import java.util.Locale

fun formatTemperature(celsius: Double, isCelsius: Boolean): String {
    return if (isCelsius) {
        "${Math.round(celsius)}°C"
    } else {
        val fahrenheit = celsius * 9.0 / 5.0 + 32.0
        "${Math.round(fahrenheit)}°F"
    }
}

@Composable
fun WeatherConditionIcon(
    iconType: WeatherIconType,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val (vector, tint) = when (iconType) {
        WeatherIconType.CLEAR_DAY -> Icons.Default.WbSunny to AccentAmber
        WeatherIconType.CLEAR_NIGHT -> Icons.Outlined.Brightness2 to Color(0xFF93C5FD)
        WeatherIconType.CLOUDY_DAY -> Icons.Outlined.CloudQueue to Color(0xFF60A5FA)
        WeatherIconType.CLOUDY_NIGHT -> Icons.Outlined.Cloud to Color(0xFF94A3B8)
        WeatherIconType.OVERCAST -> Icons.Outlined.Cloud to Color(0xFF64748B)
        WeatherIconType.FOG -> Icons.Default.Air to Color(0xFF94A3B8)
        WeatherIconType.RAIN_LIGHT -> Icons.Outlined.Water to PrimaryBlue
        WeatherIconType.RAIN_HEAVY -> Icons.Default.Grain to Color(0xFF2563EB)
        WeatherIconType.SNOW -> Icons.Default.Grain to Color(0xFFBAE6FD)
        WeatherIconType.THUNDERSTORM -> Icons.Outlined.Thunderstorm to AccentAmber
    }

    Icon(
        imageVector = vector,
        contentDescription = "Weather Icon",
        tint = tint,
        modifier = modifier.size(size.dp)
    )
}

@Composable
fun PrimaryWeatherCard(
    current: CurrentWeatherUnits,
    cityName: String,
    countryName: String,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    val condition = WeatherCodeMapper.getCondition(current.weatherCode, current.isDay == 1)
    val isDay = current.isDay == 1

    val backgroundGradient = if (isDay) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0284C7),
                Color(0xFF0369A1),
                Color(0xFF075985)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF0B1120)
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("primary_weather_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // City & Country
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cityName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        if (countryName.isNotBlank()) {
                            Text(
                                text = countryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    WeatherConditionIcon(
                        iconType = condition.iconType,
                        size = 56
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Temperature and condition text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = formatTemperature(current.temperature, isCelsius),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-2).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = condition.description,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Feels like ${formatTemperature(current.apparentTemperature, isCelsius)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        if (current.precipitation > 0) {
                            Text(
                                text = "Precip: ${String.format(Locale.US, "%.1f", current.precipitation)} mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMetricsRow(
    current: CurrentWeatherUnits,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_metrics_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WeatherMetricCard(
            title = "Wind",
            value = "${Math.round(current.windSpeed)} km/h",
            icon = Icons.Default.Air,
            modifier = Modifier.weight(1f)
        )
        WeatherMetricCard(
            title = "Humidity",
            value = "${current.relativeHumidity}%",
            icon = Icons.Default.WaterDrop,
            modifier = Modifier.weight(1f)
        )
        WeatherMetricCard(
            title = "Pressure",
            value = "${Math.round(current.surfacePressure)} hPa",
            icon = Icons.Default.Compress,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WeatherMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
