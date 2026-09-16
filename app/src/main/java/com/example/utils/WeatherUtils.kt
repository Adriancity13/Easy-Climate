package com.example.utils

import androidx.compose.ui.graphics.Color
import com.example.data.models.AdviceModifier
import com.example.data.models.ClothingAdvice
import com.example.data.models.HourlyItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SceneType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    CLOUDY,
    DRIZZLE,
    RAIN,
    STORM,
    SNOW,
    FOG
}

data class WmoConfig(
    val code: Int,
    val desc: String,
    val sceneDay: SceneType,
    val sceneNight: SceneType,
    val iconDay: String,
    val iconNight: String,
    val gradDay: List<Color>,
    val gradNight: List<Color>
)

object WeatherUtils {

    val defaultGradSunrise = listOf(
        Color(0xFFC2410C),
        Color(0xFFD97706),
        Color(0xFFFDE047)
    )

    private val defaultGradDay = listOf(
        Color(0xFF2563EB),
        Color(0xFF3B82F6),
        Color(0xFF60A5FA)
    )

    private val defaultGradSunset = listOf(
        Color(0xFF4C1D95),
        Color(0xFF9333EA),
        Color(0xFFF97316)
    )

    private val defaultGradNight = listOf(
        Color(0xFF0F172A),
        Color(0xFF1E293B),
        Color(0xFF334155)
    )

    private val wmoConfigs = mapOf(
        0 to WmoConfig(
            code = 0,
            desc = "Cielo despejado",
            sceneDay = SceneType.CLEAR_DAY,
            sceneNight = SceneType.CLEAR_NIGHT,
            iconDay = "sun",
            iconNight = "moon",
            gradDay = listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFF60A5FA)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        1 to WmoConfig(
            code = 1,
            desc = "Principalmente despejado",
            sceneDay = SceneType.PARTLY_CLOUDY,
            sceneNight = SceneType.PARTLY_CLOUDY,
            iconDay = "cloud-sun",
            iconNight = "cloud-moon",
            gradDay = listOf(Color(0xFF2563EB), Color(0xFF4B90FA), Color(0xFF74B4FB)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        2 to WmoConfig(
            code = 2,
            desc = "Intervalos nubosos",
            sceneDay = SceneType.PARTLY_CLOUDY,
            sceneNight = SceneType.PARTLY_CLOUDY,
            iconDay = "cloud-sun",
            iconNight = "cloud-moon",
            gradDay = listOf(Color(0xFF2B6DEB), Color(0xFF5296F8), Color(0xFF80BAFA)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        3 to WmoConfig(
            code = 3,
            desc = "Nublado",
            sceneDay = SceneType.CLOUDY,
            sceneNight = SceneType.CLOUDY,
            iconDay = "cloud",
            iconNight = "cloud",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF0B0F19), Color(0xFF111827), Color(0xFF1F2937))
        ),
        45 to WmoConfig(
            code = 45,
            desc = "Niebla",
            sceneDay = SceneType.FOG,
            sceneNight = SceneType.FOG,
            iconDay = "cloud-fog",
            iconNight = "cloud-fog",
            gradDay = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        48 to WmoConfig(
            code = 48,
            desc = "Niebla con escarcha",
            sceneDay = SceneType.FOG,
            sceneNight = SceneType.FOG,
            iconDay = "cloud-fog",
            iconNight = "cloud-fog",
            gradDay = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        51 to WmoConfig(
            code = 51,
            desc = "Llovizna ligera",
            sceneDay = SceneType.DRIZZLE,
            sceneNight = SceneType.DRIZZLE,
            iconDay = "cloud-drizzle",
            iconNight = "cloud-drizzle",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        53 to WmoConfig(
            code = 53,
            desc = "Llovizna moderada",
            sceneDay = SceneType.DRIZZLE,
            sceneNight = SceneType.DRIZZLE,
            iconDay = "cloud-drizzle",
            iconNight = "cloud-drizzle",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        55 to WmoConfig(
            code = 55,
            desc = "Llovizna densa",
            sceneDay = SceneType.DRIZZLE,
            sceneNight = SceneType.DRIZZLE,
            iconDay = "cloud-drizzle",
            iconNight = "cloud-drizzle",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        61 to WmoConfig(
            code = 61,
            desc = "Lluvia débil",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
        ),
        63 to WmoConfig(
            code = 63,
            desc = "Lluvia moderada",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF172554), Color(0xFF1E293B), Color(0xFF334155)),
            gradNight = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
        ),
        65 to WmoConfig(
            code = 65,
            desc = "Lluvia fuerte",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
            gradNight = listOf(Color(0xFF05080E), Color(0xFF0B0F19), Color(0xFF111827))
        ),
        71 to WmoConfig(
            code = 71,
            desc = "Nevada ligera",
            sceneDay = SceneType.SNOW,
            sceneNight = SceneType.SNOW,
            iconDay = "snowflake",
            iconNight = "snowflake",
            gradDay = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        73 to WmoConfig(
            code = 73,
            desc = "Nevada moderada",
            sceneDay = SceneType.SNOW,
            sceneNight = SceneType.SNOW,
            iconDay = "snowflake",
            iconNight = "snowflake",
            gradDay = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B)),
            gradNight = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        ),
        75 to WmoConfig(
            code = 75,
            desc = "Nevada intensa",
            sceneDay = SceneType.SNOW,
            sceneNight = SceneType.SNOW,
            iconDay = "snowflake",
            iconNight = "snowflake",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
        ),
        80 to WmoConfig(
            code = 80,
            desc = "Chubascos suaves",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
        ),
        81 to WmoConfig(
            code = 81,
            desc = "Chubascos moderados",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)),
            gradNight = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
        ),
        82 to WmoConfig(
            code = 82,
            desc = "Chubascos violentos",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
            gradNight = listOf(Color(0xFF030712), Color(0xFF0B0F19), Color(0xFF111827))
        ),
        95 to WmoConfig(
            code = 95,
            desc = "Tormenta eléctrica",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
            gradNight = listOf(Color(0xFF030712), Color(0xFF0B0F19), Color(0xFF111827))
        ),
        96 to WmoConfig(
            code = 96,
            desc = "Tormenta con granizo débil",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
            gradNight = listOf(Color(0xFF030712), Color(0xFF0B0F19), Color(0xFF111827))
        ),
        99 to WmoConfig(
            code = 99,
            desc = "Tormenta con granizo fuerte",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
            gradNight = listOf(Color(0xFF030712), Color(0xFF0B0F19), Color(0xFF111827))
        )
    )

    fun parseTimeToMinutes(timeStr: String?): Int? {
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

    /**
     * Determines whether it is daytime based on real astronomical sunrise/sunset times,
     * API flag if available, or device local clock / simulated hour.
     */
    fun isDaytime(
        sunriseIsoOrTime: String?,
        sunsetIsoOrTime: String?,
        apiIsDay: Int? = null,
        simulatedHour: Int? = null,
        simulatedMinute: Int? = null
    ): Boolean {
        val currentMinutes = if (simulatedHour != null) {
            simulatedHour * 60 + (simulatedMinute ?: 0)
        } else {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }
        val sunriseMinutes = parseTimeToMinutes(sunriseIsoOrTime)
        val sunsetMinutes = parseTimeToMinutes(sunsetIsoOrTime)

        // 1. Primary: Exact astronomical window
        if (sunriseMinutes != null && sunsetMinutes != null) {
            return currentMinutes in sunriseMinutes until sunsetMinutes
        }

        // 2. Secondary: API explicit flag
        if (apiIsDay != null && simulatedHour == null) {
            return apiIsDay == 1
        }

        // 3. Fallback: Local standard daylight hours (07:00 to 20:30)
        val currentHour = currentMinutes / 60
        return currentHour in 7..20
    }

    /**
     * Quick check for whether current local time is daytime.
     */
    fun isDaytimeNow(): Boolean {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        return currentHour in 7..20
    }

    /**
     * Rango de amanecer: entre sunrise y sunrise + 1.5 horas (90 minutos),
     * o franja de 06:00 a 08:30 si no hay ephemeris.
     */
    fun isSunrisePeriod(
        sunriseIsoOrTime: String?,
        simulatedHour: Int? = null,
        simulatedMinute: Int? = null
    ): Boolean {
        val currentMinutes = if (simulatedHour != null) {
            simulatedHour * 60 + (simulatedMinute ?: 0)
        } else {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }

        val sunriseMinutes = parseTimeToMinutes(sunriseIsoOrTime)
        return if (sunriseMinutes != null) {
            // Entre sunrise (-15 min de clarear) y sunrise + 1.5 horas (90 min)
            currentMinutes in (sunriseMinutes - 15)..(sunriseMinutes + 90)
        } else {
            // Franja por defecto de 06:00 (360) a 08:30 (510)
            currentMinutes in 360..510
        }
    }

    /**
     * Rango de atardecer / ocaso: alrededor de sunset,
     * o franja de 19:30 a 21:30 si no hay ephemeris.
     */
    fun isSunsetPeriod(
        sunsetIsoOrTime: String?,
        simulatedHour: Int? = null,
        simulatedMinute: Int? = null
    ): Boolean {
        val currentMinutes = if (simulatedHour != null) {
            simulatedHour * 60 + (simulatedMinute ?: 0)
        } else {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }

        val sunsetMinutes = parseTimeToMinutes(sunsetIsoOrTime)
        return if (sunsetMinutes != null) {
            // Sunset window: 45 min antes hasta 60 min después
            currentMinutes in (sunsetMinutes - 45)..(sunsetMinutes + 60)
        } else {
            // Franja por defecto: 19:30 (1170) a 21:30 (1290)
            currentMinutes in 1170..1290
        }
    }

    fun getWmoConfig(code: Int): WmoConfig {
        return wmoConfigs[code] ?: wmoConfigs[0]!!
    }

    fun getGradient(
        code: Int,
        isDay: Boolean,
        isSunset: Boolean = false,
        isSunrise: Boolean = false
    ): List<Color> {
        val config = getWmoConfig(code)
        if (!isDay) {
            return config.gradNight
        }
        if (isSunrise && (code <= 2)) {
            return defaultGradSunrise
        }
        if (isSunset && (code <= 2)) {
            return defaultGradSunset
        }
        return config.gradDay
    }

    fun getSceneType(code: Int, isDay: Boolean): SceneType {
        val config = getWmoConfig(code)
        return if (isDay) config.sceneDay else config.sceneNight
    }

    fun getIconType(code: Int, isDay: Boolean): String {
        val config = getWmoConfig(code)
        return if (isDay) config.iconDay else config.iconNight
    }

    fun getDescription(code: Int): String {
        return getWmoConfig(code).desc
    }

    fun generateClothingAdvice(
        temp: Int,
        feels: Int,
        hum: Int,
        wind: Int,
        rainProb: Int,
        tempDropNight: Int = 0
    ): ClothingAdvice {
        val baseAdvice = when {
            feels < 6 || (wind > 25 && temp < 10) -> {
                "Hace bastante frío. Lleva abrigo medio o cortavientos sobre una sudadera transpirable y una braga/bufanda fina para cubrirte la boca/garganta del aire frío. Es clave vestir por capas para no acalorarte al caminar y luego enfriarte con el sudor."
            }
            feels in 6..12 -> {
                "Clima fresco. Lo ideal es una chaqueta o cazadora ligera fácil de abrir o quitar, sobre una camiseta técnica/transpirable. Evita los jerséis muy gruesos para no sudar mientras te mueves, pero mantén el pecho protegido si sopla viento."
            }
            feels in 13..17 -> {
                "Temperatura suave pero engañosa. Lleva una sudadera ligera, sobrecamisa o cortavientos fino sobre camiseta de manga corta. Si caminas rápido te sobrará la chaqueta, pero tenla a mano para cuando te pares y no coger frío."
            }
            feels in 18..23 -> {
                "Clima ideal. Camiseta de manga corta de algodón fino o tejido transpirable. Guarda una rebeca muy fina o cortavientos en la mochila por si refresca al atardecer o al entrar en sitios con aire acondicionado."
            }
            else -> {
                "Hace calor. Apuesta por ropa muy suelta, holgada y transpirable de manga corta para evitar acumular sudor. Intenta no llevar peso a la espalda si vas a caminar y busca la sombra si el aire está muy seco."
            }
        }

        val modifiers = mutableListOf<AdviceModifier>()

        if (hum > 80 && temp > 18) {
            modifiers.add(
                AdviceModifier(
                    icon = "💧",
                    title = "Humedad muy alta",
                    text = "Vas a romper a sudar fácilmente. Elige telas extremadamente transpirables y evita prendas ajustadas."
                )
            )
        }

        if (wind > 20 && temp < 15) {
            modifiers.add(
                AdviceModifier(
                    icon = "💨",
                    title = "Ojo al viento",
                    text = "Aunque no haga extremo frío, el viento directo puede enfriar el sudor del pecho rápido. Una prenda cortavientos o un cuello fino marcarán la diferencia para proteger tus vías respiratorias."
                )
            )
        }

        if (tempDropNight > 5) {
            modifiers.add(
                AdviceModifier(
                    icon = "🌡️",
                    title = "Precaución por bajada térmica",
                    text = "La temperatura caerá bastante hacia la noche. No salgas solo en camiseta aunque ahora haga calor."
                )
            )
        }

        if (rainProb > 40) {
            modifiers.add(
                AdviceModifier(
                    icon = "🌧️",
                    title = "Riesgo de lluvia",
                    text = "Lleva chubasquero transpirable o paraguas. Evita mojarte para no quedar expuesto al frío con la ropa húmeda."
                )
            )
        }

        return ClothingAdvice(baseAdvice = baseAdvice, modifiers = modifiers)
    }

    /**
     * Generates context-aware clothing recommendations based on upcoming 12-hour thermal oscillation
     * and precipitation probability.
     */
    fun generateTimeSlotClothingAlert(
        currentTemp: Int,
        nextHours: List<HourlyItem>
    ): String? {
        if (nextHours.isEmpty()) return null
        val items = nextHours.take(12)

        fun formatHour(item: HourlyItem): String {
            return if (item.label.contains(":")) {
                "${item.label} h"
            } else {
                try {
                    val h = item.rawTime.substringAfter("T").substringBefore(":")
                    "$h:00 h"
                } catch (_: Exception) {
                    item.label
                }
            }
        }

        fun extractHourInt(item: HourlyItem): Int {
            return try {
                item.rawTime.substringAfter("T").substringBefore(":").toInt()
            } catch (_: Exception) {
                -1
            }
        }

        // 1. Rain Alert Priority: check if rain starts within the next 12 hours
        val rainItem = items.firstOrNull { it.rainProb >= 40 || it.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99) }
        if (rainItem != null) {
            val hourStr = if (rainItem == items.first() && rainItem.label == "Ahora") "las próximas horas" else "las ${formatHour(rainItem)}"
            return "Lleva paraguas o chubasquero: probabilidad de lluvia (${rainItem.rainProb}%) a partir de $hourStr."
        }

        // 2. Morning cool/cold -> Afternoon warm
        val firstHourInt = extractHourInt(items.first())
        val isMorning = firstHourInt in 6..12 || (items.first().label == "Ahora" && currentTemp <= 15)
        val afternoonItem = items.filter { extractHourInt(it) in 13..17 }.maxByOrNull { it.temp }
        if (isMorning && afternoonItem != null && afternoonItem.temp >= 19 && (afternoonItem.temp - currentTemp) >= 5) {
            return "Por la mañana (${currentTemp}°C) necesitarás abrigo, pero a partir de las ${formatHour(afternoonItem)} (${afternoonItem.temp}°C) te sobrará."
        }

        // 3. Evening/Night temperature drop
        val eveningDropItem = items.filter { extractHourInt(it) >= 18 || extractHourInt(it) < 4 }
            .firstOrNull { it.temp <= 14 && (currentTemp - it.temp) >= 4 }
        if (eveningDropItem != null && currentTemp >= 16) {
            return "Lleva chaqueta fina: la temperatura caerá a ${eveningDropItem.temp}°C a partir de las ${formatHour(eveningDropItem)}."
        }

        // 4. Sharp general drop in the next 12 hours
        val minTempItem = items.minByOrNull { it.temp }
        if (minTempItem != null && (currentTemp - minTempItem.temp) >= 6) {
            return "Precaución con la bajada térmica: caerá a ${minTempItem.temp}°C a partir de las ${formatHour(minTempItem)}."
        }

        // 5. Significant heating up in the coming hours
        val maxTempItem = items.maxByOrNull { it.temp }
        if (maxTempItem != null && (maxTempItem.temp - currentTemp) >= 6 && maxTempItem.temp >= 24) {
            return "Subida térmica notable: alcanzará ${maxTempItem.temp}°C hacia las ${formatHour(maxTempItem)}."
        }

        // 6. Cold plateau (all hours cold)
        if (items.all { it.temp <= 10 }) {
            val minT = minTempItem?.temp ?: currentTemp
            return "Frío continuo (mín ${minT}°C): mantén el abrigo y viste por capas en todo momento."
        }

        return null
    }

    fun getFormattedCurrentDate(): String {
        val locale = Locale("es", "ES")
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", locale)
        val formatted = sdf.format(Date())
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun getDayNameForIndex(index: Int, dateStr: String): String {
        if (index == 0) return "Hoy"
        if (index == 1) return "Mañana"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            val cal = Calendar.getInstance()
            if (date != null) cal.time = date
            val daysWeek = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
            daysWeek[dayOfWeek]
        } catch (e: Exception) {
            "Día $index"
        }
    }

    fun formatSunTime(isoTime: String?): String {
        if (isoTime.isNullOrBlank()) return "--:--"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = sdf.parse(isoTime)
            if (date != null) {
                val outFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                outFormat.format(date)
            } else {
                isoTime.substringAfter("T", "--:--")
            }
        } catch (e: Exception) {
            isoTime.substringAfter("T", "--:--")
        }
    }

    fun calculateDaylightDuration(sunriseIso: String?, sunsetIso: String?): String {
        if (sunriseIso.isNullOrBlank() || sunsetIso.isNullOrBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val riseDate = sdf.parse(sunriseIso)
            val setDate = sdf.parse(sunsetIso)
            if (riseDate != null && setDate != null) {
                val diffMs = setDate.time - riseDate.time
                val diffMinutes = (diffMs / (1000 * 60)).coerceAtLeast(0)
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                "${hours}h ${mins}m"
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
