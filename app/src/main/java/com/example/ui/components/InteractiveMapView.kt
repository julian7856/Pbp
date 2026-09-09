package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CityLocation
import com.example.data.model.WeatherApiResponse
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.defaultCities
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sinh
import kotlin.math.tan

object MapProjection {
    fun lonToTileX(lon: Double, zoom: Int): Double {
        val n = 1 shl zoom
        return ((lon + 180.0) / 360.0) * n
    }

    fun latToTileY(lat: Double, zoom: Int): Double {
        val latRad = Math.toRadians(lat.coerceIn(-85.0511, 85.0511))
        val n = 1 shl zoom
        return ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / Math.PI) / 2.0) * n
    }

    fun tileXToLon(x: Double, zoom: Int): Double {
        val n = 1 shl zoom
        return (x / n) * 360.0 - 180.0
    }

    fun tileYToLat(y: Double, zoom: Int): Double {
        val n = 1 shl zoom
        val latRad = atan(sinh(Math.PI * (1.0 - 2.0 * y / n)))
        return Math.toDegrees(latRad)
    }
}

@Composable
fun InteractiveMapView(
    centerLat: Double,
    centerLon: Double,
    zoomLevel: Float,
    selectedLocation: CityLocation?,
    weatherPreview: WeatherApiResponse?,
    isLoadingPreview: Boolean,
    isCelsius: Boolean,
    currentTimeMs: Long,
    onMapTapped: (Double, Double) -> Unit,
    onCenterChanged: (Double, Double, Float?) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCitySelected: (CityLocation) -> Unit,
    onCurrentLocationRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val intZoom = zoomLevel.roundToInt().coerceIn(2, 18)
    val tileSizePx = with(density) { 256.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_map_container")
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val centerTileX = MapProjection.lonToTileX(centerLon, intZoom)
        val centerTileY = MapProjection.latToTileY(centerLat, intZoom)

        val tilesRange = remember(centerTileX, centerTileY, intZoom, widthPx, heightPx) {
            val halfTilesX = (widthPx / tileSizePx / 2f).toInt() + 2
            val halfTilesY = (heightPx / tileSizePx / 2f).toInt() + 2
            val n = 1 shl intZoom

            val minX = (floor(centerTileX).toInt() - halfTilesX)
            val maxX = (floor(centerTileX).toInt() + halfTilesX)
            val minY = (floor(centerTileY).toInt() - halfTilesY).coerceIn(0, n - 1)
            val maxY = (floor(centerTileY).toInt() + halfTilesY).coerceIn(0, n - 1)

            val tiles = mutableListOf<Triple<Int, Int, Int>>()
            for (ty in minY..maxY) {
                for (rawX in minX..maxX) {
                    val normalizedX = ((rawX % n) + n) % n
                    tiles.add(Triple(intZoom, normalizedX, ty))
                }
            }
            tiles
        }

        // Map Tile Canvas & Gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE2E8F0))
                .pointerInput(intZoom, centerLat, centerLon) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dTileX = dragAmount.x / tileSizePx
                        val dTileY = dragAmount.y / tileSizePx

                        val newTileX = centerTileX - dTileX
                        val newTileY = centerTileY - dTileY

                        val newLon = MapProjection.tileXToLon(newTileX, intZoom)
                        val newLat = MapProjection.tileYToLat(newTileY, intZoom)
                        onCenterChanged(newLat, newLon, null)
                    }
                }
                .pointerInput(intZoom, centerLat, centerLon) {
                    detectTapGestures { tapOffset ->
                        val tapTileX = centerTileX + (tapOffset.x - widthPx / 2f) / tileSizePx
                        val tapTileY = centerTileY + (tapOffset.y - heightPx / 2f) / tileSizePx
                        val tappedLat = MapProjection.tileYToLat(tapTileY, intZoom)
                        val tappedLon = MapProjection.tileXToLon(tapTileX, intZoom)
                        onMapTapped(tappedLat, tappedLon)
                    }
                }
        ) {
            // Render Tiles
            tilesRange.forEach { (z, tx, ty) ->
                val tilePixelOffsetX = (widthPx / 2f) + (tx - centerTileX).toFloat() * tileSizePx
                val tilePixelOffsetY = (heightPx / 2f) + (ty - centerTileY).toFloat() * tileSizePx

                val tileUrl = "https://tile.openstreetmap.org/$z/$tx/$ty.png"

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(tileUrl)
                        .addHeader("User-Agent", "MapsClockWeatherApp/1.0 (HappyCommunity)")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(256.dp)
                        .offset {
                            IntOffset(
                                tilePixelOffsetX.roundToInt(),
                                tilePixelOffsetY.roundToInt()
                            )
                        }
                )
            }

            // Render City Marker Pins
            defaultCities.forEach { city ->
                val cityTileX = MapProjection.lonToTileX(city.longitude, intZoom)
                val cityTileY = MapProjection.latToTileY(city.latitude, intZoom)
                val pinPxX = (widthPx / 2f) + (cityTileX - centerTileX).toFloat() * tileSizePx
                val pinPxY = (heightPx / 2f) + (cityTileY - centerTileY).toFloat() * tileSizePx

                if (pinPxX in -50f..(widthPx + 50f) && pinPxY in -50f..(heightPx + 50f)) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (pinPxX - 16.dp.toPx()).roundToInt(),
                                    (pinPxY - 32.dp.toPx()).roundToInt()
                                )
                            }
                            .clickable { onCitySelected(city) }
                            .testTag("map_marker_${city.name.lowercase().replace(" ", "_")}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shadowElevation = 3.dp
                            ) {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = city.name,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Dropped pin indicator for selected location
            selectedLocation?.let { loc ->
                val selTileX = MapProjection.lonToTileX(loc.longitude, intZoom)
                val selTileY = MapProjection.latToTileY(loc.latitude, intZoom)
                val pinPxX = (widthPx / 2f) + (selTileX - centerTileX).toFloat() * tileSizePx
                val pinPxY = (heightPx / 2f) + (selTileY - centerTileY).toFloat() * tileSizePx

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (pinPxX - 18.dp.toPx()).roundToInt(),
                                (pinPxY - 36.dp.toPx()).roundToInt()
                            )
                        }
                        .testTag("selected_pin_marker")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Selected Pin",
                        tint = AccentCoral,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Top Quick Jump City Chips
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(defaultCities) { city ->
                    val isSelected = selectedLocation?.name == city.name
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
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_${city.name.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // Map Control Floating Buttons (+ / - Zoom and GPS Location)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = onZoomIn,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("map_zoom_in_button"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
            }

            FloatingActionButton(
                onClick = onZoomOut,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("map_zoom_out_button"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = onCurrentLocationRequested,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("map_my_location_button"),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }

        // Bottom Map Location Weather & Clock Overlay Card
        selectedLocation?.let { loc ->
            MapLocationOverlayCard(
                location = loc,
                weather = weatherPreview,
                isLoading = isLoadingPreview,
                isCelsius = isCelsius,
                currentTimeMs = currentTimeMs,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun MapLocationOverlayCard(
    location: CityLocation,
    weather: WeatherApiResponse?,
    isLoading: Boolean,
    isCelsius: Boolean,
    currentTimeMs: Long,
    modifier: Modifier = Modifier
) {
    val current = weather?.current
    val tz = if (location.timezone.isNotBlank() && location.timezone != "auto") {
        TimeZone.getTimeZone(location.timezone)
    } else {
        TimeZone.getDefault()
    }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply { timeZone = tz }
    val timeDisplay = timeFormat.format(Date(currentTimeMs))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .testTag("map_location_overlay_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AccentCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Local Time: $timeDisplay",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Made by HappyCommunity",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (current != null) {
                val condition = WeatherCodeMapper.getCondition(current.weatherCode, current.isDay == 1)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherConditionIcon(
                        iconType = condition.iconType,
                        size = 32
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatTemperature(current.temperature, isCelsius),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = condition.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
