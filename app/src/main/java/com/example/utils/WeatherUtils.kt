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

    private val defaultGradDay = listOf(
        Color(0xFF2B79E0),
        Color(0xFF68B4F8),
        Color(0xFFBDE1FF)
    )

    private val defaultGradNight = listOf(
        Color(0xFF090F26),
        Color(0xFF151F42),
        Color(0xFF1E2C5E)
    )

    private val wmoConfigs = mapOf(
        0 to WmoConfig(
            code = 0,
            desc = "Cielo despejado",
            sceneDay = SceneType.CLEAR_DAY,
            sceneNight = SceneType.CLEAR_NIGHT,
            iconDay = "sun",
            iconNight = "moon",
            gradDay = listOf(Color(0xFF2B79E0), Color(0xFF68B4F8), Color(0xFFBDE1FF)),
            gradNight = listOf(Color(0xFF090F26), Color(0xFF151F42), Color(0xFF1E2C5E))
        ),
        1 to WmoConfig(
            code = 1,
            desc = "Principalmente despejado",
            sceneDay = SceneType.PARTLY_CLOUDY,
            sceneNight = SceneType.PARTLY_CLOUDY,
            iconDay = "cloud-sun",
            iconNight = "cloud-moon",
            gradDay = listOf(Color(0xFF3782E4), Color(0xFF76BBF8), Color(0xFFC4E4FF)),
            gradNight = listOf(Color(0xFF0E1533), Color(0xFF1C2752), Color(0xFF293A73))
        ),
        2 to WmoConfig(
            code = 2,
            desc = "Intervalos nubosos",
            sceneDay = SceneType.PARTLY_CLOUDY,
            sceneNight = SceneType.PARTLY_CLOUDY,
            iconDay = "cloud-sun",
            iconNight = "cloud-moon",
            gradDay = listOf(Color(0xFF4489DF), Color(0xFF7DBEF7), Color(0xFFCAE5FD)),
            gradNight = listOf(Color(0xFF101938), Color(0xFF1E2C5A), Color(0xFF2B3D75))
        ),
        3 to WmoConfig(
            code = 3,
            desc = "Nublado",
            sceneDay = SceneType.CLOUDY,
            sceneNight = SceneType.CLOUDY,
            iconDay = "cloud",
            iconNight = "cloud",
            gradDay = listOf(Color(0xFF4D6782), Color(0xFF708AA8), Color(0xFFA6BBD1)),
            gradNight = listOf(Color(0xFF141B29), Color(0xFF202B3E), Color(0xFF303E54))
        ),
        45 to WmoConfig(
            code = 45,
            desc = "Niebla",
            sceneDay = SceneType.FOG,
            sceneNight = SceneType.FOG,
            iconDay = "cloud-fog",
            iconNight = "cloud-fog",
            gradDay = listOf(Color(0xFF647585), Color(0xFF8B9BAA), Color(0xFFBCC7D1)),
            gradNight = listOf(Color(0xFF19202A), Color(0xFF293340), Color(0xFF3C4856))
        ),
        48 to WmoConfig(
            code = 48,
            desc = "Niebla con escarcha",
            sceneDay = SceneType.FOG,
            sceneNight = SceneType.FOG,
            iconDay = "cloud-fog",
            iconNight = "cloud-fog",
            gradDay = listOf(Color(0xFF617382), Color(0xFF8797A7), Color(0xFFB8C5CE)),
            gradNight = listOf(Color(0xFF181F29), Color(0xFF26303D), Color(0xFF384552))
        ),
        51 to WmoConfig(
            code = 51,
            desc = "Llovizna ligera",
            sceneDay = SceneType.DRIZZLE,
            sceneNight = SceneType.DRIZZLE,
            iconDay = "cloud-drizzle",
            iconNight = "cloud-drizzle",
            gradDay = listOf(Color(0xFF3D5670), Color(0xFF5D7792), Color(0xFF8EA5BD)),
            gradNight = listOf(Color(0xFF131A26), Color(0xFF1D2737), Color(0xFF2A384E))
        ),
        53 to WmoConfig(
            code = 53,
            desc = "Llovizna moderada",
            sceneDay = SceneType.DRIZZLE,
            sceneNight = SceneType.DRIZZLE,
            iconDay = "cloud-drizzle",
            iconNight = "cloud-drizzle",
            gradDay = listOf(Color(0xFF39506B), Color(0xFF57708B), Color(0xFF879EB5)),
            gradNight = listOf(Color(0xFF121824), Color(0xFF1B2433), Color(0xFF273448))
        ),
        55 to WmoConfig(
            code = 55,
            desc = "Llovizna densa",
            sceneDay = SceneType.DRIZZLE,
            sceneNight = SceneType.DRIZZLE,
            iconDay = "cloud-drizzle",
            iconNight = "cloud-drizzle",
            gradDay = listOf(Color(0xFF344A63), Color(0xFF516982), Color(0xFF7E94AB)),
            gradNight = listOf(Color(0xFF101621), Color(0xFF18212E), Color(0xFF232F41))
        ),
        61 to WmoConfig(
            code = 61,
            desc = "Lluvia débil",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF30455C), Color(0xFF4C647C), Color(0xFF758CA2)),
            gradNight = listOf(Color(0xFF0E141E), Color(0xFF161E2B), Color(0xFF212C3D))
        ),
        63 to WmoConfig(
            code = 63,
            desc = "Lluvia moderada",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF2B3E52), Color(0xFF44596E), Color(0xFF6A7F94)),
            gradNight = listOf(Color(0xFF0B1019), Color(0xFF131924), Color(0xFF1D2534))
        ),
        65 to WmoConfig(
            code = 65,
            desc = "Lluvia fuerte",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF243547), Color(0xFF3A4D5F), Color(0xFF5D6F80)),
            gradNight = listOf(Color(0xFF090E15), Color(0xFF10161F), Color(0xFF18202C))
        ),
        71 to WmoConfig(
            code = 71,
            desc = "Nevada ligera",
            sceneDay = SceneType.SNOW,
            sceneNight = SceneType.SNOW,
            iconDay = "snowflake",
            iconNight = "snowflake",
            gradDay = listOf(Color(0xFF4F6880), Color(0xFF7A95AE), Color(0xFFB8CBDB)),
            gradNight = listOf(Color(0xFF151D29), Color(0xFF232F3F), Color(0xFF354457))
        ),
        73 to WmoConfig(
            code = 73,
            desc = "Nevada moderada",
            sceneDay = SceneType.SNOW,
            sceneNight = SceneType.SNOW,
            iconDay = "snowflake",
            iconNight = "snowflake",
            gradDay = listOf(Color(0xFF4A637A), Color(0xFF728CA4), Color(0xFFADBFCF)),
            gradNight = listOf(Color(0xFF131A25), Color(0xFF202B3A), Color(0xFF303E50))
        ),
        75 to WmoConfig(
            code = 75,
            desc = "Nevada intensa",
            sceneDay = SceneType.SNOW,
            sceneNight = SceneType.SNOW,
            iconDay = "snowflake",
            iconNight = "snowflake",
            gradDay = listOf(Color(0xFF42596E), Color(0xFF687F96), Color(0xFF9FAEC0)),
            gradNight = listOf(Color(0xFF101720), Color(0xFF1B2532), Color(0xFF293645))
        ),
        80 to WmoConfig(
            code = 80,
            desc = "Chubascos suaves",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF354C64), Color(0xFF546D87), Color(0xFF8198B0)),
            gradNight = listOf(Color(0xFF111822), Color(0xFF1A2330), Color(0xFF263345))
        ),
        81 to WmoConfig(
            code = 81,
            desc = "Chubascos moderados",
            sceneDay = SceneType.RAIN,
            sceneNight = SceneType.RAIN,
            iconDay = "cloud-rain",
            iconNight = "cloud-rain",
            gradDay = listOf(Color(0xFF2D4156), Color(0xFF496078), Color(0xFF70869D)),
            gradNight = listOf(Color(0xFF0D131C), Color(0xFF151C27), Color(0xFF202B3B))
        ),
        82 to WmoConfig(
            code = 82,
            desc = "Chubascos violentos",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF212C38), Color(0xFF344252), Color(0xFF526274)),
            gradNight = listOf(Color(0xFF080C12), Color(0xFF0E141C), Color(0xFF161E29))
        ),
        95 to WmoConfig(
            code = 95,
            desc = "Tormenta eléctrica",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF1B2430), Color(0xFF2C3848), Color(0xFF465568)),
            gradNight = listOf(Color(0xFF070A0F), Color(0xFF0C1117), Color(0xFF131922))
        ),
        96 to WmoConfig(
            code = 96,
            desc = "Tormenta con granizo débil",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF17202B), Color(0xFF26323F), Color(0xFF3F4C5C)),
            gradNight = listOf(Color(0xFF05080C), Color(0xFF0A0D13), Color(0xFF10151C))
        ),
        99 to WmoConfig(
            code = 99,
            desc = "Tormenta con granizo fuerte",
            sceneDay = SceneType.STORM,
            sceneNight = SceneType.STORM,
            iconDay = "zap",
            iconNight = "zap",
            gradDay = listOf(Color(0xFF141C26), Color(0xFF212A36), Color(0xFF374352)),
            gradNight = listOf(Color(0xFF040609), Color(0xFF080B0F), Color(0xFF0D1117))
        )
    )

    fun getWmoConfig(code: Int): WmoConfig {
        return wmoConfigs[code] ?: wmoConfigs[0]!!
    }

    fun getGradient(code: Int, isDay: Boolean): List<Color> {
        val config = getWmoConfig(code)
        return if (isDay) config.gradDay else config.gradNight
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
