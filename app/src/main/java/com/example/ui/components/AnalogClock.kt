package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.PrimaryBlue
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    currentTimeMs: Long = System.currentTimeMillis(),
    timeZoneId: String = ""
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val accentColor = AccentCoral

    // Calculate hours, minutes, seconds for timezone
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentTimeMs
        if (timeZoneId.isNotBlank()) {
            timeZone = TimeZone.getTimeZone(timeZoneId)
        }
    }

    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)
    val millis = calendar.get(Calendar.MILLISECOND)

    val smoothSeconds = seconds + millis / 1000f
    val smoothMinutes = minutes + smoothSeconds / 60f
    val smoothHours = hours + smoothMinutes / 60f

    Canvas(
        modifier = modifier
            .size(size)
            .testTag("analog_clock_canvas")
    ) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Draw outer clock face ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(surfaceColor, surfaceVariantColor),
                center = center,
                radius = radius
            ),
            radius = radius - 4.dp.toPx(),
            center = center
        )

        // Outer rim border
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.3f)),
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius)
            ),
            radius = radius - 4.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Hour and Minute ticks
        for (i in 0 until 60) {
            val angleDeg = i * 6f
            val angleRad = Math.toRadians(angleDeg.toDouble() - 90)
            val isHourTick = i % 5 == 0

            val tickLength = if (isHourTick) 12.dp.toPx() else 6.dp.toPx()
            val tickWidth = if (isHourTick) 3.dp.toPx() else 1.5.dp.toPx()
            val tickColor = if (isHourTick) primaryColor else onSurfaceColor.copy(alpha = 0.35f)

            val startRadius = radius - 16.dp.toPx()
            val endRadius = startRadius - tickLength

            val startX = center.x + (startRadius * cos(angleRad)).toFloat()
            val startY = center.y + (startRadius * sin(angleRad)).toFloat()
            val endX = center.x + (endRadius * cos(angleRad)).toFloat()
            val endY = center.y + (endRadius * sin(angleRad)).toFloat()

            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = tickWidth,
                cap = StrokeCap.Round
            )
        }

        // Draw 12, 3, 6, 9 Numerals
        val numerals = listOf("12" to 0, "3" to 3, "6" to 6, "9" to 9)
        val textPaint = Paint().apply {
            color = onSurfaceColor.toArgb()
            textSize = 14.dp.toPx()
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        numerals.forEach { (text, hour) ->
            val angleRad = Math.toRadians(hour * 30.0 - 90)
            val numRadius = radius - 36.dp.toPx()
            val x = center.x + (numRadius * cos(angleRad)).toFloat()
            val y = center.y + (numRadius * sin(angleRad)).toFloat() + 5.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(text, x, y, textPaint)
        }

        // Hour Hand
        val hourAngleRad = Math.toRadians(smoothHours * 30.0 - 90)
        val hourLength = radius * 0.5f
        val hourEndX = center.x + (hourLength * cos(hourAngleRad)).toFloat()
        val hourEndY = center.y + (hourLength * sin(hourAngleRad)).toFloat()

        drawLine(
            color = onSurfaceColor,
            start = center,
            end = Offset(hourEndX, hourEndY),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minAngleRad = Math.toRadians(smoothMinutes * 6.0 - 90)
        val minLength = radius * 0.70f
        val minEndX = center.x + (minLength * cos(minAngleRad)).toFloat()
        val minEndY = center.y + (minLength * sin(minAngleRad)).toFloat()

        drawLine(
            color = primaryColor,
            start = center,
            end = Offset(minEndX, minEndY),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second Hand (Accent Coral / Red) with counter-weight
        val secAngleRad = Math.toRadians(smoothSeconds * 6.0 - 90)
        val secLength = radius * 0.82f
        val secTailLength = radius * 0.20f

        val secEndX = center.x + (secLength * cos(secAngleRad)).toFloat()
        val secEndY = center.y + (secLength * sin(secAngleRad)).toFloat()

        val secTailX = center.x - (secTailLength * cos(secAngleRad)).toFloat()
        val secTailY = center.y - (secTailLength * sin(secAngleRad)).toFloat()

        drawLine(
            color = accentColor,
            start = Offset(secTailX, secTailY),
            end = Offset(secEndX, secEndY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center pivot cap
        drawCircle(
            color = accentColor,
            radius = 6.dp.toPx(),
            center = center
        )
        drawCircle(
            color = surfaceColor,
            radius = 2.5.dp.toPx(),
            center = center
        )
    }
}
