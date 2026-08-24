package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.utils.SceneType
import kotlin.math.sin
import kotlin.random.Random

data class StarSpec(
    val xRatio: Float,
    val yRatio: Float,
    val sizeDp: Float,
    val phaseOffset: Float,
    val periodSec: Float
)

data class RainDropSpec(
    val xRatio: Float,
    val speed: Float,
    val length: Float,
    val delayRatio: Float
)

data class SnowflakeSpec(
    val xRatio: Float,
    val speed: Float,
    val size: Float,
    val driftAmp: Float,
    val driftSpeed: Float,
    val delayRatio: Float
)

@Composable
fun AtmosphericWeatherBackground(
    sceneType: SceneType,
    gradientColors: List<Color>,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val topColor by animateColorAsState(
        targetValue = gradientColors.firstOrNull() ?: Color(0xFF2B79E0),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "topGrad"
    )
    val midColor by animateColorAsState(
        targetValue = gradientColors.getOrNull(1) ?: Color(0xFF68B4F8),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "midGrad"
    )
    val bottomColor by animateColorAsState(
        targetValue = gradientColors.lastOrNull() ?: Color(0xFFBDE1FF),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "botGrad"
    )

    val transition = rememberInfiniteTransition(label = "AtmosphereAnim")

    // General continuous time float (0f .. 1f) for cycling animations
    val progressLoop by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressLoop"
    )

    // Sun / Moon breath
    val glowBreath by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBreath"
    )

    // Twinkle for stars
    val starTwinkle by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    // Storm lightning cycle (0..1 over 8 seconds)
    val stormCycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stormCycle"
    )

    // Fog sway
    val fogSway by transition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fogSway"
    )

    // Pre-calculated random particles for performance
    val stars = remember {
        val r = Random(42)
        List(35) {
            StarSpec(
                xRatio = r.nextFloat() * 0.96f + 0.02f,
                yRatio = r.nextFloat() * 0.50f + 0.02f,
                sizeDp = r.nextFloat() * 2.2f + 1.2f,
                phaseOffset = r.nextFloat() * 6.28f,
                periodSec = r.nextFloat() * 2.5f + 2f
            )
        }
    }

    val rainDrops = remember {
        val r = Random(123)
        List(42) {
            RainDropSpec(
                xRatio = r.nextFloat(),
                speed = r.nextFloat() * 0.4f + 0.8f,
                length = r.nextFloat() * 12f + 14f,
                delayRatio = r.nextFloat()
            )
        }
    }

    val snowFlakes = remember {
        val r = Random(456)
        List(32) {
            SnowflakeSpec(
                xRatio = r.nextFloat(),
                speed = r.nextFloat() * 0.3f + 0.5f,
                size = r.nextFloat() * 3.5f + 2.5f,
                driftAmp = r.nextFloat() * 18f + 10f,
                driftSpeed = r.nextFloat() * 2f + 1.5f,
                delayRatio = r.nextFloat()
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, midColor, bottomColor)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            when (sceneType) {
                SceneType.CLEAR_DAY -> {
                    drawClearDaySun(canvasW, canvasH, glowBreath)
                }
                SceneType.CLEAR_NIGHT -> {
                    drawClearNightMoon(canvasW, canvasH, glowBreath)
                    drawStars(canvasW, canvasH, stars, progressLoop, starTwinkle)
                }
                SceneType.PARTLY_CLOUDY -> {
                    if (isDay) {
                        drawClearDaySun(canvasW, canvasH, glowBreath, scale = 0.8f)
                    } else {
                        drawClearNightMoon(canvasW, canvasH, glowBreath, scale = 0.8f)
                    }
                    drawFloatingClouds(canvasW, canvasH, progressLoop, count = 2)
                }
                SceneType.CLOUDY -> {
                    drawFloatingClouds(canvasW, canvasH, progressLoop, count = 3)
                }
                SceneType.DRIZZLE -> {
                    drawFloatingClouds(canvasW, canvasH, progressLoop, count = 2, opacity = 0.65f)
                    drawRain(canvasW, canvasH, rainDrops, progressLoop * 4f, isDrizzle = true)
                }
                SceneType.RAIN -> {
                    drawFloatingClouds(canvasW, canvasH, progressLoop, count = 2, opacity = 0.8f)
                    drawRain(canvasW, canvasH, rainDrops, progressLoop * 5f, isDrizzle = false)
                }
                SceneType.STORM -> {
                    drawStormLightning(canvasW, canvasH, stormCycle)
                    drawFloatingClouds(canvasW, canvasH, progressLoop, count = 2, opacity = 0.85f, cloudColor = Color(0xB0DCE6F2))
                    drawRain(canvasW, canvasH, rainDrops, progressLoop * 6f, isDrizzle = false)
                }
                SceneType.SNOW -> {
                    drawFloatingClouds(canvasW, canvasH, progressLoop, count = 1, opacity = 0.7f)
                    drawSnow(canvasW, canvasH, snowFlakes, progressLoop * 2.5f)
                }
                SceneType.FOG -> {
                    drawFog(canvasW, canvasH, fogSway)
                }
            }
        }
    }
}

private fun DrawScope.drawClearDaySun(canvasW: Float, canvasH: Float, breath: Float, scale: Float = 1.0f) {
    val sunCenterX = canvasW * 0.85f
    val sunCenterY = 90f
    val baseRadius = 42f * scale

    // Outer aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x60FFF59D),
                Color(0x30FFF59D),
                Color.Transparent
            ),
            center = Offset(sunCenterX, sunCenterY),
            radius = baseRadius * 2.8f * breath
        ),
        radius = baseRadius * 2.8f * breath,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Inner glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF9C4),
                Color(0xFFFBC02D)
            ),
            center = Offset(sunCenterX, sunCenterY),
            radius = baseRadius * breath
        ),
        radius = baseRadius * breath,
        center = Offset(sunCenterX, sunCenterY)
    )
}

private fun DrawScope.drawClearNightMoon(canvasW: Float, canvasH: Float, breath: Float, scale: Float = 1.0f) {
    val moonCenterX = canvasW * 0.84f
    val moonCenterY = 90f
    val radius = 34f * scale

    // Moon soft aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x40FFFFFF),
                Color(0x15E2E8F0),
                Color.Transparent
            ),
            center = Offset(moonCenterX, moonCenterY),
            radius = radius * 2.2f * breath
        ),
        radius = radius * 2.2f * breath,
        center = Offset(moonCenterX, moonCenterY)
    )

    // Moon disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFE2E8F0),
                Color(0xFFCBD5E1)
            ),
            center = Offset(moonCenterX - radius * 0.2f, moonCenterY - radius * 0.2f),
            radius = radius
        ),
        radius = radius,
        center = Offset(moonCenterX, moonCenterY)
    )
}

private fun DrawScope.drawStars(
    canvasW: Float,
    canvasH: Float,
    stars: List<StarSpec>,
    loopTime: Float,
    twinkle: Float
) {
    for (s in stars) {
        val x = s.xRatio * canvasW
        val y = s.yRatio * canvasH
        val alpha = ((sin(loopTime * s.periodSec * 6.28f + s.phaseOffset) + 1f) / 2f * 0.7f + 0.2f) * twinkle
        drawCircle(
            color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 0.95f)),
            radius = s.sizeDp,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawFloatingClouds(
    canvasW: Float,
    canvasH: Float,
    loop: Float,
    count: Int,
    opacity: Float = 0.7f,
    cloudColor: Color = Color(0xAEFFFFFF)
) {
    val cloudsConfig = listOf(
        Triple(0.08f, 0.55f * opacity, 180f),  // yRatio, alpha, width
        Triple(0.15f, 0.75f * opacity, 230f),
        Triple(0.24f, 0.65f * opacity, 190f)
    )

    for (i in 0 until count.coerceAtMost(cloudsConfig.size)) {
        val (yRatio, alpha, cWidth) = cloudsConfig[i]
        val speedFactor = when (i) {
            0 -> 0.7f
            1 -> 1.0f
            else -> 1.3f
        }
        val totalDistance = canvasW + cWidth * 2f
        val currentX = ((loop * speedFactor * totalDistance) % totalDistance) - cWidth
        val y = canvasH * yRatio

        drawSingleCloud(currentX, y, cWidth, cloudColor.copy(alpha = alpha))
    }
}

private fun DrawScope.drawSingleCloud(x: Float, y: Float, width: Float, color: Color) {
    val height = width * 0.45f
    // High-performance zero-allocation cloud rendering for 120Hz displays
    drawRoundRect(
        color = color,
        topLeft = Offset(x + width * 0.12f, y + height * 0.42f),
        size = Size(width * 0.76f, height * 0.48f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(height * 0.24f, height * 0.24f)
    )
    drawCircle(
        color = color,
        radius = height * 0.32f,
        center = Offset(x + width * 0.28f, y + height * 0.52f)
    )
    drawCircle(
        color = color,
        radius = height * 0.44f,
        center = Offset(x + width * 0.50f, y + height * 0.42f)
    )
    drawCircle(
        color = color,
        radius = height * 0.34f,
        center = Offset(x + width * 0.72f, y + height * 0.50f)
    )
}

private fun DrawScope.drawRain(
    canvasW: Float,
    canvasH: Float,
    drops: List<RainDropSpec>,
    timeLoop: Float,
    isDrizzle: Boolean
) {
    val dropWidth = if (isDrizzle) 1.5f else 2.2f
    val dropColor = if (isDrizzle) Color(0x55FFFFFF) else Color(0x88FFFFFF)

    for (d in drops) {
        val cycle = (timeLoop * d.speed + d.delayRatio) % 1.0f
        val y = cycle * (canvasH + 50f) - 30f
        val x = (d.xRatio * canvasW) - (y * 0.15f) // -10 degree tilt
        val len = if (isDrizzle) d.length * 0.65f else d.length

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, dropColor),
                startY = y,
                endY = y + len
            ),
            start = Offset(x, y),
            end = Offset(x - len * 0.17f, y + len),
            strokeWidth = dropWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSnow(
    canvasW: Float,
    canvasH: Float,
    flakes: List<SnowflakeSpec>,
    timeLoop: Float
) {
    for (f in flakes) {
        val cycle = (timeLoop * f.speed + f.delayRatio) % 1.0f
        val y = cycle * (canvasH + 40f) - 20f
        val drift = sin(cycle * f.driftSpeed * 6.28f) * f.driftAmp
        val x = (f.xRatio * canvasW) + drift

        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = f.size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawStormLightning(canvasW: Float, canvasH: Float, cycle: Float) {
    // Lightning strikes at specific timing intervals in the 8s cycle (e.g. 0.94 .. 0.96)
    val isFlashing = cycle in 0.938f..0.965f
    if (isFlashing) {
        // Flash overlay
        val flashAlpha = if (cycle in 0.94f..0.946f || cycle in 0.952f..0.958f) 0.28f else 0.08f
        drawRect(Color.White.copy(alpha = flashAlpha))

        // Bolt
        val boltX = canvasW * 0.35f
        val boltY = 60f
        val path = Path().apply {
            moveTo(boltX + 30f, boltY)
            lineTo(boltX + 5f, boltY + 60f)
            lineTo(boltX + 26f, boltY + 60f)
            lineTo(boltX + 15f, boltY + 130f)
            lineTo(boltX + 45f, boltY + 75f)
            lineTo(boltX + 28f, boltY + 75f)
            close()
        }
        drawPath(path, Color(0xF2FFF9C4))
    }
}

private fun DrawScope.drawFog(canvasW: Float, canvasH: Float, sway: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x35FFFFFF),
                Color(0x45FFFFFF),
                Color(0x20FFFFFF),
                Color.Transparent
            ),
            startY = canvasH * 0.1f,
            endY = canvasH * 0.9f
        ),
        topLeft = Offset(sway, 0f),
        size = Size(canvasW + 60f, canvasH)
    )
}
