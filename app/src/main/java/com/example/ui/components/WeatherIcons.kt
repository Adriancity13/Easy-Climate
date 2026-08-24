package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WeatherConditionIcon(
    iconType: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            when (iconType) {
                "sun" -> drawSun(tint, w, h)
                "moon" -> drawMoon(tint, w, h)
                "cloud-sun" -> drawCloudSun(tint, w, h)
                "cloud-moon" -> drawCloudMoon(tint, w, h)
                "cloud-rain" -> drawCloudRain(tint, w, h)
                "wind", "air", "cloud-rain-wind" -> drawWind(tint, w, h)
                "droplet", "water-drop", "humidity", "cloud-drizzle" -> drawDroplet(tint, w, h)
                "snowflake" -> drawSnowflake(tint, w, h)
                "zap", "cloud-lightning" -> drawZap(tint, w, h)
                "cloud-fog" -> drawCloudFog(tint, w, h)
                "sunrise" -> drawSunrise(tint, w, h)
                "sunset" -> drawSunset(tint, w, h)
                else -> drawCloud(tint, w, h)
            }
        }
    }
}

private fun DrawScope.drawSun(tint: Color, w: Float, h: Float) {
    val center = Offset(w / 2f, h / 2f)
    val r = w * 0.22f
    val strokeWidth = w * 0.08f

    // Center disc / ring
    drawCircle(
        color = tint,
        radius = r,
        center = center,
        style = Stroke(width = strokeWidth)
    )

    // Sun rays
    val rayLength = w * 0.12f
    val rayDist = r + w * 0.08f
    for (i in 0 until 8) {
        val angle = (i * 45f) * (Math.PI / 180f).toFloat()
        val startX = center.x + kotlin.math.cos(angle) * rayDist
        val startY = center.y + kotlin.math.sin(angle) * rayDist
        val endX = center.x + kotlin.math.cos(angle) * (rayDist + rayLength)
        val endY = center.y + kotlin.math.sin(angle) * (rayDist + rayLength)

        drawLine(
            color = tint,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawMoon(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val path = Path().apply {
        moveTo(w * 0.85f, h * 0.52f)
        cubicTo(
            w * 0.85f, h * 0.75f,
            w * 0.67f, h * 0.90f,
            w * 0.44f, h * 0.90f
        )
        cubicTo(
            w * 0.21f, h * 0.90f,
            w * 0.10f, h * 0.72f,
            w * 0.10f, h * 0.48f
        )
        cubicTo(
            w * 0.10f, h * 0.32f,
            w * 0.18f, h * 0.17f,
            w * 0.32f, h * 0.10f
        )
        cubicTo(
            w * 0.28f, h * 0.22f,
            w * 0.28f, h * 0.38f,
            w * 0.38f, h * 0.50f
        )
        cubicTo(
            w * 0.48f, h * 0.62f,
            w * 0.66f, h * 0.68f,
            w * 0.85f, h * 0.52f
        )
        close()
    }

    drawPath(
        path = path,
        color = tint,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawCloud(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val path = Path().apply {
        moveTo(w * 0.25f, h * 0.75f)
        lineTo(w * 0.75f, h * 0.75f)
        cubicTo(w * 0.90f, h * 0.75f, w * 0.95f, h * 0.58f, w * 0.85f, h * 0.48f)
        cubicTo(w * 0.88f, h * 0.30f, w * 0.68f, h * 0.22f, w * 0.55f, h * 0.32f)
        cubicTo(w * 0.48f, h * 0.20f, w * 0.28f, h * 0.22f, w * 0.25f, h * 0.38f)
        cubicTo(w * 0.10f, h * 0.42f, w * 0.10f, h * 0.68f, w * 0.25f, h * 0.75f)
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawCloudSun(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    // Mini sun rays top-right
    drawLine(
        color = tint,
        start = Offset(w * 0.50f, h * 0.10f),
        end = Offset(w * 0.50f, h * 0.18f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = tint,
        start = Offset(w * 0.78f, h * 0.18f),
        end = Offset(w * 0.72f, h * 0.24f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = tint,
        start = Offset(w * 0.88f, h * 0.45f),
        end = Offset(w * 0.80f, h * 0.45f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Sun arc behind cloud
    drawArc(
        color = tint,
        startAngle = 200f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(w * 0.40f, h * 0.18f),
        size = Size(w * 0.40f, h * 0.40f),
        style = Stroke(strokeWidth, cap = StrokeCap.Round)
    )

    // Cloud in front
    val path = Path().apply {
        moveTo(w * 0.22f, h * 0.85f)
        lineTo(w * 0.68f, h * 0.85f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.86f, h * 0.70f, w * 0.76f, h * 0.60f)
        cubicTo(w * 0.78f, h * 0.45f, w * 0.60f, h * 0.38f, w * 0.48f, h * 0.48f)
        cubicTo(w * 0.40f, h * 0.36f, w * 0.22f, h * 0.40f, w * 0.20f, h * 0.54f)
        cubicTo(w * 0.08f, h * 0.60f, w * 0.08f, h * 0.80f, w * 0.22f, h * 0.85f)
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawCloudMoon(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f

    // Moon arc
    val moonPath = Path().apply {
        moveTo(w * 0.75f, h * 0.20f)
        cubicTo(w * 0.60f, h * 0.20f, w * 0.50f, h * 0.30f, w * 0.52f, h * 0.45f)
        cubicTo(w * 0.65f, h * 0.45f, w * 0.78f, h * 0.35f, w * 0.75f, h * 0.20f)
    }
    drawPath(moonPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round))

    // Cloud
    val cloudPath = Path().apply {
        moveTo(w * 0.22f, h * 0.85f)
        lineTo(w * 0.68f, h * 0.85f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.86f, h * 0.70f, w * 0.76f, h * 0.60f)
        cubicTo(w * 0.78f, h * 0.45f, w * 0.60f, h * 0.38f, w * 0.48f, h * 0.48f)
        cubicTo(w * 0.40f, h * 0.36f, w * 0.22f, h * 0.40f, w * 0.20f, h * 0.54f)
        cubicTo(w * 0.08f, h * 0.60f, w * 0.08f, h * 0.80f, w * 0.22f, h * 0.85f)
        close()
    }
    drawPath(cloudPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawCloudRain(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val path = Path().apply {
        moveTo(w * 0.22f, h * 0.60f)
        lineTo(w * 0.78f, h * 0.60f)
        cubicTo(w * 0.92f, h * 0.60f, w * 0.95f, h * 0.44f, w * 0.84f, h * 0.35f)
        cubicTo(w * 0.86f, h * 0.18f, w * 0.66f, h * 0.12f, w * 0.52f, h * 0.20f)
        cubicTo(w * 0.45f, h * 0.10f, w * 0.26f, h * 0.12f, w * 0.24f, h * 0.26f)
        cubicTo(w * 0.08f, h * 0.30f, w * 0.08f, h * 0.54f, w * 0.22f, h * 0.60f)
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Rain lines
    val rainDropW = strokeWidth * 0.9f
    drawLine(tint, Offset(w * 0.32f, h * 0.72f), Offset(w * 0.28f, h * 0.92f), rainDropW, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.52f, h * 0.72f), Offset(w * 0.48f, h * 0.92f), rainDropW, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.72f, h * 0.72f), Offset(w * 0.68f, h * 0.92f), rainDropW, StrokeCap.Round)
}

private fun DrawScope.drawCloudDrizzle(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val path = Path().apply {
        moveTo(w * 0.22f, h * 0.60f)
        lineTo(w * 0.78f, h * 0.60f)
        cubicTo(w * 0.92f, h * 0.60f, w * 0.95f, h * 0.44f, w * 0.84f, h * 0.35f)
        cubicTo(w * 0.86f, h * 0.18f, w * 0.66f, h * 0.12f, w * 0.52f, h * 0.20f)
        cubicTo(w * 0.45f, h * 0.10f, w * 0.26f, h * 0.12f, w * 0.24f, h * 0.26f)
        cubicTo(w * 0.08f, h * 0.30f, w * 0.08f, h * 0.54f, w * 0.22f, h * 0.60f)
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Drizzle dots / short dashes
    val rainDropW = strokeWidth * 0.8f
    drawLine(tint, Offset(w * 0.32f, h * 0.72f), Offset(w * 0.30f, h * 0.82f), rainDropW, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.52f, h * 0.78f), Offset(w * 0.50f, h * 0.88f), rainDropW, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.72f, h * 0.72f), Offset(w * 0.70f, h * 0.82f), rainDropW, StrokeCap.Round)
}

private fun DrawScope.drawSnowflake(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.07f
    val center = Offset(w / 2f, h / 2f)

    for (i in 0 until 6) {
        val angle = (i * 60f) * (Math.PI / 180f).toFloat()
        val len = w * 0.40f
        val endX = center.x + kotlin.math.cos(angle) * len
        val endY = center.y + kotlin.math.sin(angle) * len

        drawLine(tint, center, Offset(endX, endY), strokeWidth, StrokeCap.Round)

        // Branching
        val branchLen = len * 0.35f
        val branchPos = len * 0.65f
        val branchBaseX = center.x + kotlin.math.cos(angle) * branchPos
        val branchBaseY = center.y + kotlin.math.sin(angle) * branchPos

        val branchAngle1 = angle + 45f * (Math.PI / 180f).toFloat()
        val branchAngle2 = angle - 45f * (Math.PI / 180f).toFloat()

        drawLine(
            tint,
            Offset(branchBaseX, branchBaseY),
            Offset(branchBaseX + kotlin.math.cos(branchAngle1) * branchLen, branchBaseY + kotlin.math.sin(branchAngle1) * branchLen),
            strokeWidth * 0.8f,
            StrokeCap.Round
        )
        drawLine(
            tint,
            Offset(branchBaseX, branchBaseY),
            Offset(branchBaseX + kotlin.math.cos(branchAngle2) * branchLen, branchBaseY + kotlin.math.sin(branchAngle2) * branchLen),
            strokeWidth * 0.8f,
            StrokeCap.Round
        )
    }
}

private fun DrawScope.drawZap(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val path = Path().apply {
        moveTo(w * 0.55f, h * 0.08f)
        lineTo(w * 0.25f, h * 0.55f)
        lineTo(w * 0.52f, h * 0.55f)
        lineTo(w * 0.38f, h * 0.92f)
        lineTo(w * 0.78f, h * 0.45f)
        lineTo(w * 0.52f, h * 0.45f)
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawWind(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.085f

    // Top wind stream with loop
    val topPath = Path().apply {
        moveTo(w * 0.12f, h * 0.32f)
        lineTo(w * 0.62f, h * 0.32f)
        cubicTo(
            w * 0.74f, h * 0.32f,
            w * 0.82f, h * 0.22f,
            w * 0.74f, h * 0.14f
        )
        cubicTo(
            w * 0.64f, h * 0.06f,
            w * 0.52f, h * 0.16f,
            w * 0.54f, h * 0.24f
        )
    }
    drawPath(topPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Middle wind stream with lower loop
    val midPath = Path().apply {
        moveTo(w * 0.08f, h * 0.55f)
        lineTo(w * 0.72f, h * 0.55f)
        cubicTo(
            w * 0.84f, h * 0.55f,
            w * 0.92f, h * 0.65f,
            w * 0.84f, h * 0.76f
        )
        cubicTo(
            w * 0.74f, h * 0.84f,
            w * 0.62f, h * 0.74f,
            w * 0.65f, h * 0.66f
        )
    }
    drawPath(midPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Bottom short trailing breeze line
    val botPath = Path().apply {
        moveTo(w * 0.18f, h * 0.78f)
        lineTo(w * 0.50f, h * 0.78f)
    }
    drawPath(botPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawDroplet(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.085f
    val path = Path().apply {
        moveTo(w * 0.50f, h * 0.12f)
        cubicTo(
            w * 0.38f, h * 0.32f,
            w * 0.18f, h * 0.54f,
            w * 0.18f, h * 0.68f
        )
        cubicTo(
            w * 0.18f, h * 0.88f,
            w * 0.32f, h * 0.92f,
            w * 0.50f, h * 0.92f
        )
        cubicTo(
            w * 0.68f, h * 0.92f,
            w * 0.82f, h * 0.88f,
            w * 0.82f, h * 0.68f
        )
        cubicTo(
            w * 0.82f, h * 0.54f,
            w * 0.62f, h * 0.32f,
            w * 0.50f, h * 0.12f
        )
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Subtle inner specular arc
    val innerPath = Path().apply {
        moveTo(w * 0.34f, h * 0.62f)
        cubicTo(
            w * 0.32f, h * 0.72f,
            w * 0.38f, h * 0.80f,
            w * 0.46f, h * 0.82f
        )
    }
    drawPath(innerPath, tint.copy(alpha = 0.7f), style = Stroke(strokeWidth * 0.7f, cap = StrokeCap.Round))
}

private fun DrawScope.drawCloudFog(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val path = Path().apply {
        moveTo(w * 0.22f, h * 0.50f)
        lineTo(w * 0.78f, h * 0.50f)
        cubicTo(w * 0.92f, h * 0.50f, w * 0.95f, h * 0.34f, w * 0.84f, h * 0.25f)
        cubicTo(w * 0.86f, h * 0.10f, w * 0.66f, h * 0.06f, w * 0.52f, h * 0.12f)
        cubicTo(w * 0.45f, h * 0.04f, w * 0.26f, h * 0.06f, w * 0.24f, h * 0.18f)
        cubicTo(w * 0.08f, h * 0.22f, w * 0.08f, h * 0.44f, w * 0.22f, h * 0.50f)
        close()
    }
    drawPath(path, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Fog horizontal lines
    drawLine(tint, Offset(w * 0.20f, h * 0.68f), Offset(w * 0.80f, h * 0.68f), strokeWidth, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.28f, h * 0.84f), Offset(w * 0.72f, h * 0.84f), strokeWidth, StrokeCap.Round)
}

private fun DrawScope.drawSunrise(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.085f
    val horizonY = h * 0.76f

    // Horizon line
    drawLine(
        color = tint.copy(alpha = 0.85f),
        start = Offset(w * 0.10f, horizonY),
        end = Offset(w * 0.90f, horizonY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Sun arc rising above horizon
    val sunRadius = w * 0.24f
    val sunCenter = Offset(w * 0.50f, horizonY)
    val sunPath = Path().apply {
        moveTo(sunCenter.x - sunRadius, horizonY)
        cubicTo(
            sunCenter.x - sunRadius, horizonY - sunRadius * 1.35f,
            sunCenter.x + sunRadius, horizonY - sunRadius * 1.35f,
            sunCenter.x + sunRadius, horizonY
        )
    }
    drawPath(sunPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round))

    // Top ray pointing straight up
    drawLine(
        color = tint,
        start = Offset(w * 0.50f, h * 0.32f),
        end = Offset(w * 0.50f, h * 0.12f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Left diagonal ray
    drawLine(
        color = tint,
        start = Offset(w * 0.26f, h * 0.44f),
        end = Offset(w * 0.14f, h * 0.30f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Right diagonal ray
    drawLine(
        color = tint,
        start = Offset(w * 0.74f, h * 0.44f),
        end = Offset(w * 0.86f, h * 0.30f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Arrowhead indicating sunrise (going up)
    val arrowPath = Path().apply {
        moveTo(w * 0.40f, h * 0.20f)
        lineTo(w * 0.50f, h * 0.08f)
        lineTo(w * 0.60f, h * 0.20f)
    }
    drawPath(arrowPath, tint, style = Stroke(strokeWidth * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawSunset(tint: Color, w: Float, h: Float) {
    val strokeWidth = w * 0.085f
    val horizonY = h * 0.76f

    // Horizon line
    drawLine(
        color = tint.copy(alpha = 0.85f),
        start = Offset(w * 0.10f, horizonY),
        end = Offset(w * 0.90f, horizonY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Sun arc setting at horizon
    val sunRadius = w * 0.24f
    val sunCenter = Offset(w * 0.50f, horizonY)
    val sunPath = Path().apply {
        moveTo(sunCenter.x - sunRadius, horizonY)
        cubicTo(
            sunCenter.x - sunRadius, horizonY - sunRadius * 1.35f,
            sunCenter.x + sunRadius, horizonY - sunRadius * 1.35f,
            sunCenter.x + sunRadius, horizonY
        )
    }
    drawPath(sunPath, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round))

    // Top ray
    drawLine(
        color = tint,
        start = Offset(w * 0.50f, h * 0.34f),
        end = Offset(w * 0.50f, h * 0.18f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Left diagonal ray
    drawLine(
        color = tint,
        start = Offset(w * 0.26f, h * 0.44f),
        end = Offset(w * 0.14f, h * 0.30f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Right diagonal ray
    drawLine(
        color = tint,
        start = Offset(w * 0.74f, h * 0.44f),
        end = Offset(w * 0.86f, h * 0.30f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Arrowhead indicating sunset (going down)
    val arrowPath = Path().apply {
        moveTo(w * 0.40f, h * 0.22f)
        lineTo(w * 0.50f, h * 0.34f)
        lineTo(w * 0.60f, h * 0.22f)
    }
    drawPath(arrowPath, tint, style = Stroke(strokeWidth * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
