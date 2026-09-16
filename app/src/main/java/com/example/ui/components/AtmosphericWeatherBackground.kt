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
import com.example.utils.SceneType
import com.example.utils.WeatherUtils
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
    isSunrise: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Nocturnal and sunrise gradient fallback protection
    val effectiveColors = remember(gradientColors, isDay, isSunrise) {
        if (!isDay) {
            val first = gradientColors.firstOrNull() ?: Color(0xFF030712)
            val isDaytimeBlue = first.blue > 0.55f && first.red < 0.35f && first.green < 0.55f
            if (isDaytimeBlue) {
                listOf(Color(0xFF030712), Color(0xFF0B1220), Color(0xFF131E35))
            } else {
                gradientColors
            }
        } else if (isSunrise) {
            // Forzar paleta cálida de tonos dorados y anaranjados suaves para el amanecer
            WeatherUtils.defaultGradSunrise
        } else {
            gradientColors
        }
    }

    val topColor by animateColorAsState(
        targetValue = effectiveColors.firstOrNull() ?: if (isDay) Color(0xFF2563EB) else Color(0xFF030712),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "topGrad"
    )
    val midColor by animateColorAsState(
        targetValue = effectiveColors.getOrNull(1) ?: if (isDay) Color(0xFF3B82F6) else Color(0xFF0B1220),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "midGrad"
    )
    val bottomColor by animateColorAsState(
        targetValue = effectiveColors.lastOrNull() ?: if (isDay) Color(0xFF60A5FA) else Color(0xFF131E35),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "botGrad"
    )

    val transition = rememberInfiniteTransition(label = "AtmosphereAnim")

    // Slow continuous atmospheric loop
    val progressLoop by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressLoop"
    )

    // Gentle sun/moon radiance pulsing
    val glowPulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Twinkle for stars
    val starTwinkle by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    // Lightning cycle
    val stormCycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stormCycle"
    )

    // Pre-calculated star & particle specs
    val stars = remember {
        val r = Random(42)
        List(36) {
            StarSpec(
                xRatio = r.nextFloat() * 0.96f + 0.02f,
                yRatio = r.nextFloat() * 0.52f + 0.02f,
                sizeDp = r.nextFloat() * 2.2f + 1.2f,
                phaseOffset = r.nextFloat() * 6.28f,
                periodSec = r.nextFloat() * 2.5f + 2f
            )
        }
    }

    val rainDrops = remember {
        val r = Random(123)
        List(36) {
            RainDropSpec(
                xRatio = r.nextFloat(),
                speed = r.nextFloat() * 0.35f + 0.75f,
                length = r.nextFloat() * 10f + 12f,
                delayRatio = r.nextFloat()
            )
        }
    }

    val snowFlakes = remember {
        val r = Random(456)
        List(28) {
            SnowflakeSpec(
                xRatio = r.nextFloat(),
                speed = r.nextFloat() * 0.25f + 0.45f,
                size = r.nextFloat() * 3f + 2f,
                driftAmp = r.nextFloat() * 16f + 8f,
                driftSpeed = r.nextFloat() * 2f + 1.2f,
                delayRatio = r.nextFloat()
            )
        }
    }

    // Safety fallback: if not day, CLEAR_DAY MUST become CLEAR_NIGHT
    val effectiveSceneType = if (!isDay && sceneType == SceneType.CLEAR_DAY) {
        SceneType.CLEAR_NIGHT
    } else {
        sceneType
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

            when (effectiveSceneType) {
                SceneType.CLEAR_DAY -> {
                    if (isDay) {
                        drawAtmosphericSunGlow(canvasW, canvasH, glowPulse, isSunrise = isSunrise)
                    } else {
                        drawAtmosphericMoon(canvasW, canvasH, glowPulse)
                        drawAtmosphericStars(canvasW, canvasH, stars, progressLoop, starTwinkle)
                    }
                }
                SceneType.CLEAR_NIGHT -> {
                    drawAtmosphericMoon(canvasW, canvasH, glowPulse)
                    drawAtmosphericStars(canvasW, canvasH, stars, progressLoop, starTwinkle)
                }
                SceneType.PARTLY_CLOUDY -> {
                    if (isDay) {
                        drawAtmosphericSunGlow(canvasW, canvasH, glowPulse, scale = 0.8f, isSunrise = isSunrise)
                    } else {
                        drawAtmosphericMoon(canvasW, canvasH, glowPulse, scale = 0.85f)
                        drawAtmosphericStars(canvasW, canvasH, stars.take(18), progressLoop, starTwinkle)
                    }
                    drawContinuousCloudLayer(canvasW, canvasH, progressLoop, density = 0.5f, isNight = !isDay)
                }
                SceneType.CLOUDY -> {
                    if (!isDay) {
                        drawAtmosphericMoon(canvasW, canvasH, glowPulse, scale = 0.65f)
                        drawAtmosphericStars(canvasW, canvasH, stars.take(10), progressLoop, starTwinkle * 0.6f)
                    }
                    drawContinuousCloudLayer(canvasW, canvasH, progressLoop, density = 0.85f, isNight = !isDay)
                }
                SceneType.DRIZZLE -> {
                    if (!isDay) {
                        drawAtmosphericStars(canvasW, canvasH, stars.take(8), progressLoop, starTwinkle * 0.4f)
                    }
                    drawContinuousCloudLayer(canvasW, canvasH, progressLoop, density = 0.7f, isNight = !isDay)
                    drawAtmosphericRain(canvasW, canvasH, rainDrops, progressLoop * 4f, isDrizzle = true)
                }
                SceneType.RAIN -> {
                    drawContinuousCloudLayer(canvasW, canvasH, progressLoop, density = 0.85f, isNight = !isDay)
                    drawAtmosphericRain(canvasW, canvasH, rainDrops, progressLoop * 5.5f, isDrizzle = false)
                }
                SceneType.STORM -> {
                    drawAtmosphericStorm(canvasW, canvasH, stormCycle)
                    drawContinuousCloudLayer(canvasW, canvasH, progressLoop, density = 0.95f, isNight = !isDay)
                    drawAtmosphericRain(canvasW, canvasH, rainDrops, progressLoop * 6.5f, isDrizzle = false)
                }
                SceneType.SNOW -> {
                    if (!isDay) {
                        drawAtmosphericMoon(canvasW, canvasH, glowPulse, scale = 0.75f)
                        drawAtmosphericStars(canvasW, canvasH, stars.take(14), progressLoop, starTwinkle * 0.7f)
                    }
                    drawContinuousCloudLayer(canvasW, canvasH, progressLoop, density = 0.65f, isNight = !isDay)
                    drawAtmosphericSnow(canvasW, canvasH, snowFlakes, progressLoop * 2.5f)
                }
                SceneType.FOG -> {
                    if (!isDay) {
                        drawAtmosphericMoon(canvasW, canvasH, glowPulse, scale = 0.65f)
                    }
                    drawAtmosphericFog(canvasW, canvasH, progressLoop)
                }
            }
        }
    }
}

// Gentle ambient sun aura without overwhelming foreground content
private fun DrawScope.drawAtmosphericSunGlow(
    canvasW: Float,
    canvasH: Float,
    pulse: Float,
    scale: Float = 1.0f,
    isSunrise: Boolean = false
) {
    val sunCenterX = canvasW * 0.85f
    val sunCenterY = canvasH * 0.12f
    val baseRadius = 55f * scale

    val haloColors = if (isSunrise) {
        listOf(
            Color(0x45F97316),
            Color(0x28FBBF24),
            Color(0x0CFDE047),
            Color.Transparent
        )
    } else {
        listOf(
            Color(0x35FEF08A),
            Color(0x18FDE047),
            Color(0x05FDE047),
            Color.Transparent
        )
    }

    val coreColors = if (isSunrise) {
        listOf(
            Color(0xFFFFFBEB),
            Color(0xEEFB923C),
            Color(0x00F97316)
        )
    } else {
        listOf(
            Color(0xEEFFFBEB),
            Color(0xBAFDE047),
            Color(0x00FDE047)
        )
    }

    // Atmospheric wide halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = haloColors,
            center = Offset(sunCenterX, sunCenterY),
            radius = baseRadius * 3.5f * pulse
        ),
        radius = baseRadius * 3.5f * pulse,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Radiant soft sun core
    drawCircle(
        brush = Brush.radialGradient(
            colors = coreColors,
            center = Offset(sunCenterX, sunCenterY),
            radius = baseRadius * pulse
        ),
        radius = baseRadius * pulse,
        center = Offset(sunCenterX, sunCenterY)
    )
}

// Clean elegant night moon with lunar texture and radiant silver halo
private fun DrawScope.drawAtmosphericMoon(canvasW: Float, canvasH: Float, pulse: Float, scale: Float = 1.0f) {
    val moonCenterX = canvasW * 0.85f
    val moonCenterY = canvasH * 0.12f
    val radius = 32f * scale

    // Atmospheric wide silver/blue lunar halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x3894A3B8),
                Color(0x1864748B),
                Color(0x0538BDF8),
                Color.Transparent
            ),
            center = Offset(moonCenterX, moonCenterY),
            radius = radius * 3.4f * pulse
        ),
        radius = radius * 3.4f * pulse,
        center = Offset(moonCenterX, moonCenterY)
    )

    // Moon disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF1F5F9),
                Color(0xFF94A3B8)
            ),
            center = Offset(moonCenterX - radius * 0.25f, moonCenterY - radius * 0.25f),
            radius = radius
        ),
        radius = radius,
        center = Offset(moonCenterX, moonCenterY)
    )

    // Subtle craters
    drawCircle(
        color = Color(0x22475569),
        radius = radius * 0.20f,
        center = Offset(moonCenterX - radius * 0.15f, moonCenterY + radius * 0.18f)
    )
    drawCircle(
        color = Color(0x1C475569),
        radius = radius * 0.14f,
        center = Offset(moonCenterX + radius * 0.26f, moonCenterY - radius * 0.10f)
    )
}

// Subtle twinkle stars
private fun DrawScope.drawAtmosphericStars(
    canvasW: Float,
    canvasH: Float,
    stars: List<StarSpec>,
    loopTime: Float,
    twinkle: Float
) {
    for (s in stars) {
        val x = s.xRatio * canvasW
        val y = s.yRatio * canvasH
        val alpha = ((sin(loopTime * s.periodSec * 6.28f + s.phaseOffset) + 1f) / 2f * 0.6f + 0.2f) * twinkle
        drawCircle(
            color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 0.95f)),
            radius = s.sizeDp,
            center = Offset(x, y)
        )
    }
}

// Continuous stylized atmospheric cloud layers (integrates smoothly across the canvas)
private fun DrawScope.drawContinuousCloudLayer(
    canvasW: Float,
    canvasH: Float,
    loop: Float,
    density: Float,
    isNight: Boolean = false
) {
    val waveOffset1 = sin(loop * 6.28f) * 15f
    val waveOffset2 = sin((loop + 0.33f) * 6.28f) * 18f

    val cloud1Color = if (isNight) Color(0xFF1E293B).copy(alpha = 0.20f * density) else Color.White.copy(alpha = 0.08f * density)
    val cloud2Color = if (isNight) Color(0xFF0F172A).copy(alpha = 0.24f * density) else Color.White.copy(alpha = 0.10f * density)

    // Layer 1 (Back atmospheric cloud horizon)
    val path1 = Path().apply {
        moveTo(0f, canvasH * 0.28f + waveOffset1)
        cubicTo(
            canvasW * 0.25f, canvasH * 0.22f + waveOffset1,
            canvasW * 0.50f, canvasH * 0.32f - waveOffset1,
            canvasW * 0.75f, canvasH * 0.24f + waveOffset1
        )
        cubicTo(
            canvasW * 0.90f, canvasH * 0.26f,
            canvasW, canvasH * 0.20f,
            canvasW, canvasH * 0.28f + waveOffset1
        )
        lineTo(canvasW, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(
        path = path1,
        color = cloud1Color
    )

    // Layer 2 (Mid atmospheric cloud contour)
    val path2 = Path().apply {
        moveTo(0f, canvasH * 0.18f + waveOffset2)
        cubicTo(
            canvasW * 0.30f, canvasH * 0.24f - waveOffset2,
            canvasW * 0.65f, canvasH * 0.14f + waveOffset2,
            canvasW, canvasH * 0.22f - waveOffset2
        )
        lineTo(canvasW, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(
        path = path2,
        color = cloud2Color
    )
}

// Subtle rain streaks
private fun DrawScope.drawAtmosphericRain(
    canvasW: Float,
    canvasH: Float,
    drops: List<RainDropSpec>,
    timeLoop: Float,
    isDrizzle: Boolean
) {
    val strokeW = if (isDrizzle) 1.2f else 1.8f
    val baseAlpha = if (isDrizzle) 0.35f else 0.55f

    for (d in drops) {
        val cycle = (timeLoop * d.speed + d.delayRatio) % 1.0f
        val y = cycle * (canvasH + 40f) - 20f
        val x = (d.xRatio * canvasW) - (y * 0.12f)
        val len = if (isDrizzle) d.length * 0.7f else d.length

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = baseAlpha)),
                startY = y,
                endY = y + len
            ),
            start = Offset(x, y),
            end = Offset(x - len * 0.14f, y + len),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

// Subtle falling snow
private fun DrawScope.drawAtmosphericSnow(
    canvasW: Float,
    canvasH: Float,
    flakes: List<SnowflakeSpec>,
    timeLoop: Float
) {
    for (f in flakes) {
        val cycle = (timeLoop * f.speed + f.delayRatio) % 1.0f
        val y = cycle * (canvasH + 30f) - 15f
        val drift = sin(cycle * f.driftSpeed * 6.28f) * f.driftAmp
        val x = (f.xRatio * canvasW) + drift

        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = f.size,
            center = Offset(x, y)
        )
    }
}

// Lightning strike effect for storm
private fun DrawScope.drawAtmosphericStorm(canvasW: Float, canvasH: Float, cycle: Float) {
    val isFlashing = cycle in 0.940f..0.965f
    if (isFlashing) {
        val flashAlpha = if (cycle in 0.942f..0.948f || cycle in 0.954f..0.960f) 0.22f else 0.06f
        drawRect(Color.White.copy(alpha = flashAlpha))

        val boltX = canvasW * 0.38f
        val boltY = 50f
        val path = Path().apply {
            moveTo(boltX + 25f, boltY)
            lineTo(boltX + 5f, boltY + 50f)
            lineTo(boltX + 22f, boltY + 50f)
            lineTo(boltX + 12f, boltY + 110f)
            lineTo(boltX + 38f, boltY + 65f)
            lineTo(boltX + 24f, boltY + 65f)
            close()
        }
        drawPath(path, Color(0xD0FEF08A))
    }
}

// Gentle continuous fog layer
private fun DrawScope.drawAtmosphericFog(canvasW: Float, canvasH: Float, loop: Float) {
    val sway = sin(loop * 6.28f) * 25f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            startY = canvasH * 0.15f,
            endY = canvasH * 0.85f
        ),
        topLeft = Offset(sway, 0f),
        size = Size(canvasW + 50f, canvasH)
    )
}
