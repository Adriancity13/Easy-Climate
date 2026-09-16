package com.example.widget

import android.content.Context
import android.graphics.Color as AndroidColor
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color as ComposeColor
import com.example.R
import java.util.Calendar

/**
 * Franjas horarias para el tema visual dinámico del Widget.
 * El fondo cambia EXCLUSIVAMENTE por la hora del día, independientemente del estado del tiempo.
 */
enum class WidgetTimeSlot(
    val id: String,
    val title: String,
    val hourRange: String,
    val icon: String,
    val isDaytime: Boolean
) {
    SUNRISE(
        id = "sunrise",
        title = "Amanecer",
        hourRange = "06:00 - 08:30",
        icon = "🌅",
        isDaytime = true
    ),
    DAY(
        id = "day",
        title = "Día / Mediodía",
        hourRange = "08:30 - 19:30",
        icon = "☀️",
        isDaytime = true
    ),
    SUNSET(
        id = "sunset",
        title = "Atardecer",
        hourRange = "19:30 - 21:30",
        icon = "🌇",
        isDaytime = false
    ),
    NIGHT(
        id = "night",
        title = "Noche (AMOLED Black)",
        hourRange = "21:30 - 06:00",
        icon = "🌙",
        isDaytime = false
    )
}

/**
 * Configuración visual del tema del widget para la franja horaria correspondiente.
 */
data class WidgetThemeConfig(
    val timeSlot: WidgetTimeSlot,
    @DrawableRes val backgroundDrawableRes: Int,
    val bgGradientColors: List<ComposeColor>,
    val isAmoledBlack: Boolean,
    val primaryTextColorInt: Int,
    val secondaryTextColorInt: Int,
    val rainTextColorInt: Int,
    val badgeBgColorInt: Int,
    val timeBadgeText: String,
    val timeBadgeColorInt: Int,
    val hexColorDesc: String
)

object WidgetTimeTheme {

    // Configuración para cada una de las 4 franjas horarias con distinción y contraste máximo
    private val sunriseConfig = WidgetThemeConfig(
        timeSlot = WidgetTimeSlot.SUNRISE,
        backgroundDrawableRes = R.drawable.widget_bg_sunrise,
        bgGradientColors = listOf(ComposeColor(0xFFC2410C), ComposeColor(0xFF7C2D12), ComposeColor(0xFF1E0E08)),
        isAmoledBlack = false,
        primaryTextColorInt = AndroidColor.parseColor("#FFFFFF"),
        secondaryTextColorInt = AndroidColor.parseColor("#FED7AA"), // Warm peach/gold
        rainTextColorInt = AndroidColor.parseColor("#38BDF8"),
        badgeBgColorInt = AndroidColor.parseColor("#66C2410C"),
        timeBadgeText = "🌅 Amanecer",
        timeBadgeColorInt = AndroidColor.parseColor("#FDBA74"),
        hexColorDesc = "Amanecer Dorado Radiante (#C2410C → #7C2D12 → #1E0E08)"
    )

    private val dayConfig = WidgetThemeConfig(
        timeSlot = WidgetTimeSlot.DAY,
        backgroundDrawableRes = R.drawable.widget_bg_day,
        bgGradientColors = listOf(ComposeColor(0xFF1D4ED8), ComposeColor(0xFF1E3A8A), ComposeColor(0xFF0F172A)),
        isAmoledBlack = false,
        primaryTextColorInt = AndroidColor.parseColor("#FFFFFF"),
        secondaryTextColorInt = AndroidColor.parseColor("#E0F2FE"), // Bright sky-white
        rainTextColorInt = AndroidColor.parseColor("#38BDF8"),
        badgeBgColorInt = AndroidColor.parseColor("#661D4ED8"),
        timeBadgeText = "☀️ Día",
        timeBadgeColorInt = AndroidColor.parseColor("#93C5FD"),
        hexColorDesc = "Día Azul Cielo Intenso (#1D4ED8 → #1E3A8A → #0F172A)"
    )

    private val sunsetConfig = WidgetThemeConfig(
        timeSlot = WidgetTimeSlot.SUNSET,
        backgroundDrawableRes = R.drawable.widget_bg_sunset,
        bgGradientColors = listOf(ComposeColor(0xFF86198F), ComposeColor(0xFF581C87), ComposeColor(0xFF1E0B2B)),
        isAmoledBlack = false,
        primaryTextColorInt = AndroidColor.parseColor("#FFFFFF"),
        secondaryTextColorInt = AndroidColor.parseColor("#FBCFE8"), // Twilight pink/lavender
        rainTextColorInt = AndroidColor.parseColor("#38BDF8"),
        badgeBgColorInt = AndroidColor.parseColor("#6686198F"),
        timeBadgeText = "🌇 Ocaso",
        timeBadgeColorInt = AndroidColor.parseColor("#F472B6"),
        hexColorDesc = "Atardecer Magenta Violeta (#86198F → #581C87 → #1E0B2B)"
    )

    private val nightConfig = WidgetThemeConfig(
        timeSlot = WidgetTimeSlot.NIGHT,
        backgroundDrawableRes = R.drawable.widget_bg_night,
        bgGradientColors = listOf(ComposeColor(0xFF000000), ComposeColor(0xFF030712), ComposeColor(0xFF000000)),
        isAmoledBlack = true,
        primaryTextColorInt = AndroidColor.parseColor("#FFFFFF"),
        secondaryTextColorInt = AndroidColor.parseColor("#E2E8F0"), // High contrast white-silver
        rainTextColorInt = AndroidColor.parseColor("#38BDF8"),
        badgeBgColorInt = AndroidColor.parseColor("#4038BDF8"),
        timeBadgeText = "🌙 Noche",
        timeBadgeColorInt = AndroidColor.parseColor("#38BDF8"),
        hexColorDesc = "Noche Negro Puro AMOLED #000000 (Píxeles OLED Apagados)"
    )

    /**
     * Determina la franja horaria en función de la hora y minuto del día,
     * adaptándose opcionalmente a las horas astronómicas reales de sunrise/sunset de Open-Meteo.
     */
    fun resolveTimeSlot(
        hourOfDay: Int,
        minute: Int = 0,
        sunriseIsoOrTime: String? = null,
        sunsetIsoOrTime: String? = null
    ): WidgetTimeSlot {
        val totalMinutes = hourOfDay * 60 + minute

        val sunriseMinutes = parseTimeToMinutes(sunriseIsoOrTime)
        val sunsetMinutes = parseTimeToMinutes(sunsetIsoOrTime)

        // Si tenemos datos astronómicos precisos de Open-Meteo
        if (sunriseMinutes != null && sunsetMinutes != null) {
            val sunriseStart = (sunriseMinutes - 45).coerceAtLeast(0)
            val sunriseEnd = sunriseMinutes + 75
            val sunsetStart = sunsetMinutes - 60
            val sunsetEnd = sunsetMinutes + 60

            return when {
                totalMinutes in sunriseStart until sunriseEnd -> WidgetTimeSlot.SUNRISE
                totalMinutes in sunriseEnd until sunsetStart -> WidgetTimeSlot.DAY
                totalMinutes in sunsetStart until sunsetEnd -> WidgetTimeSlot.SUNSET
                else -> WidgetTimeSlot.NIGHT
            }
        }

        // Regla estándar por intervalos de reloj (definidos en especificación)
        // 1. Amanecer: 06:00 (360) a 08:30 (510)
        // 2. Día / Mediodía: 08:30 (510) a 19:30 (1170)
        // 3. Atardecer: 19:30 (1170) a 21:30 (1290)
        // 4. Noche: 21:30 (1290) a 06:00 (360)
        return when {
            totalMinutes in 360 until 510 -> WidgetTimeSlot.SUNRISE
            totalMinutes in 510 until 1170 -> WidgetTimeSlot.DAY
            totalMinutes in 1170 until 1290 -> WidgetTimeSlot.SUNSET
            else -> WidgetTimeSlot.NIGHT
        }
    }

    /**
     * Obtiene el tema visual para la hora actual o la hora simulada.
     */
    fun getThemeConfig(
        simulatedHour: Int? = null,
        simulatedMinute: Int? = null,
        sunrise: String? = null,
        sunset: String? = null
    ): WidgetThemeConfig {
        val (hour, minute) = if (simulatedHour != null) {
            Pair(simulatedHour, simulatedMinute ?: 0)
        } else {
            val cal = Calendar.getInstance()
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }

        val slot = resolveTimeSlot(hour, minute, sunrise, sunset)
        return when (slot) {
            WidgetTimeSlot.SUNRISE -> sunriseConfig
            WidgetTimeSlot.DAY -> dayConfig
            WidgetTimeSlot.SUNSET -> sunsetConfig
            WidgetTimeSlot.NIGHT -> nightConfig
        }
    }

    /**
     * Aplica los colores de fondo y tipografía adaptados al RemoteViews del Widget.
     */
    fun applyThemeToRemoteViews(views: RemoteViews, config: WidgetThemeConfig) {
        // Fondo dinámico según la hora (incluye AMOLED Black #000000 en noche)
        views.setInt(R.id.widget_root, "setBackgroundResource", config.backgroundDrawableRes)

        // Adaptación de colores de texto para contraste óptimo en la diagramación de 3 filas
        views.setTextColor(R.id.widget_location, config.primaryTextColorInt)
        views.setTextColor(R.id.widget_high_low, config.secondaryTextColorInt)
        views.setTextColor(R.id.widget_temperature, config.primaryTextColorInt)
        views.setTextColor(R.id.widget_feels_like, config.secondaryTextColorInt)
    }

    private fun parseTimeToMinutes(timeStr: String?): Int? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            val clean = if (timeStr.contains("T")) timeStr.substringAfter("T") else timeStr
            val parts = clean.trim().split(":")
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts.getOrNull(1)?.substring(0, 2)?.toIntOrNull() ?: 0
            h * 60 + m
        } catch (_: Exception) {
            null
        }
    }
}
