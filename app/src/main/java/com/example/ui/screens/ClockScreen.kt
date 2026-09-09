package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityLocation
import com.example.data.model.ClockZone
import com.example.ui.components.AnalogClock
import com.example.ui.components.DigitalClockCard
import com.example.ui.components.HappyCommunityFooter
import com.example.ui.components.HappyCommunityHeaderBanner
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ClockScreen(
    currentCity: CityLocation,
    worldClocks: List<ClockZone>,
    currentTimeMs: Long,
    onAddClockRequested: () -> Unit,
    onRemoveClock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var clockSubTab by remember { mutableStateOf(0) } // 0 = World Clock, 1 = Stopwatch

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("clock_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // HappyCommunity Header Banner
        item {
            HappyCommunityHeaderBanner()
        }

        // Sub-tabs: World Clock vs Stopwatch
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                TabRow(
                    selectedTabIndex = clockSubTab,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("clock_sub_tabs")
                ) {
                    Tab(
                        selected = clockSubTab == 0,
                        onClick = { clockSubTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("World Clock")
                            }
                        },
                        modifier = Modifier.testTag("tab_world_clock")
                    )
                    Tab(
                        selected = clockSubTab == 1,
                        onClick = { clockSubTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stopwatch")
                            }
                        },
                        modifier = Modifier.testTag("tab_stopwatch")
                    )
                }
            }
        }

        if (clockSubTab == 0) {
            // Main Clock Section (Analog + Digital)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnalogClock(
                        size = 230.dp,
                        currentTimeMs = currentTimeMs,
                        timeZoneId = currentCity.timezone.takeIf { it != "auto" } ?: ""
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DigitalClockCard(
                        currentTimeMs = currentTimeMs,
                        timeZoneId = currentCity.timezone.takeIf { it != "auto" } ?: "",
                        cityName = currentCity.name
                    )
                }
            }

            // World Clocks Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global Timezones",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedButton(
                        onClick = onAddClockRequested,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("add_clock_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add City", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // World Clocks List
            items(worldClocks, key = { it.id }) { clock ->
                WorldClockItemCard(
                    clock = clock,
                    currentTimeMs = currentTimeMs,
                    onDelete = if (!clock.isPrimary) { { onRemoveClock(clock.id) } } else null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else {
            // Stopwatch Tool
            item {
                StopwatchView(modifier = Modifier.padding(16.dp))
            }
        }

        // HappyCommunity Footer
        item {
            HappyCommunityFooter()
        }
    }
}

@Composable
fun WorldClockItemCard(
    clock: ClockZone,
    currentTimeMs: Long,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val tz = if (clock.timeZoneId.isNotBlank()) TimeZone.getTimeZone(clock.timeZoneId) else TimeZone.getDefault()
    val cal = Calendar.getInstance(tz).apply { timeInMillis = currentTimeMs }

    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
    val isDay = hour24 in 6..18

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault()).apply { timeZone = tz }
    val amPmFormat = SimpleDateFormat("a", Locale.getDefault()).apply { timeZone = tz }
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply { timeZone = tz }

    // Offset relative to local device time
    val localOffset = TimeZone.getDefault().getOffset(currentTimeMs)
    val targetOffset = tz.getOffset(currentTimeMs)
    val diffHours = (targetOffset - localOffset) / 3600000.0

    val diffStr = when {
        clock.isPrimary -> "Local"
        diffHours == 0.0 -> "Same time"
        diffHours > 0 -> "+${String.format(Locale.US, "%.1f", diffHours).replace(".0", "")} hrs"
        else -> "${String.format(Locale.US, "%.1f", diffHours).replace(".0", "")} hrs"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("world_clock_item_${clock.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: City & Country & Offset
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = clock.cityName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isDay) Icons.Default.WbSunny else Icons.Outlined.Brightness2,
                        contentDescription = null,
                        tint = if (isDay) AccentAmber else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${clock.countryName} • $diffStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Time & AM/PM
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeFormat.format(cal.time),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = amPmFormat.format(cal.time),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = dateFormat.format(cal.time),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_clock_${clock.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Clock",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StopwatchView(
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var lastStartTimestamp by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            lastStartTimestamp = System.currentTimeMillis() - elapsedMillis
            while (isActive && isRunning) {
                elapsedMillis = System.currentTimeMillis() - lastStartTimestamp
                delay(16) // ~60fps
            }
        }
    }

    val totalSeconds = elapsedMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (elapsedMillis % 1000) / 10

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stopwatch_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Precision Stopwatch",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stopwatch digits display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = String.format(Locale.US, ".%02d", hundredths),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = AccentCoral
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRunning) {
                    Button(
                        onClick = { isRunning = false },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                        modifier = Modifier.testTag("stopwatch_pause_button")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop")
                    }
                } else {
                    Button(
                        onClick = { isRunning = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        modifier = Modifier.testTag("stopwatch_start_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start")
                    }
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        elapsedMillis = 0L
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("stopwatch_reset_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }
            }
        }
    }
}
