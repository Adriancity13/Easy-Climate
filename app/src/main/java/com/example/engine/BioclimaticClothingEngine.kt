package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.data.models.AdviceModifier
import com.example.data.models.ClothingAdvice
import com.example.data.models.HourlyItem
import java.util.Locale
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Escala de 7 niveles térmicos bioclimáticos basados en Sensación Térmica Real.
 */
enum class ThermalLevel(
    val levelName: String,
    val minTemp: Double,
    val maxTemp: Double,
    val clothingRule: String,
    val color: Color
) {
    GELIDO(
        levelName = "Gélido / Frío Extremo",
        minTemp = -99.0,
        maxTemp = 3.0,
        clothingRule = "Abrigo o jersey.",
        color = Color(0xFF60A5FA)
    ),
    FRIO_INTENSO(
        levelName = "Frío Intenso",
        minTemp = 3.0,
        maxTemp = 8.0,
        clothingRule = "Abrigo o jersey.",
        color = Color(0xFF93C5FD)
    ),
    FRIO_MODERADO(
        levelName = "Frío Moderado",
        minTemp = 8.0,
        maxTemp = 15.0,
        clothingRule = "Abrigo o jersey.",
        color = Color(0xFF6EE7B7)
    ),
    FRESCO_ENTRETIEMPO(
        levelName = "Fresco / Entretiempo",
        minTemp = 15.0,
        maxTemp = 20.0,
        clothingRule = "Manga corta + chaqueta fina de respaldo.",
        color = Color(0xFF34D399)
    ),
    TEMPLADO_SUAVE(
        levelName = "Templado Suave",
        minTemp = 20.0,
        maxTemp = 24.0,
        clothingRule = "Manga corta estándar.",
        color = Color(0xFFFCD34D)
    ),
    CALIDO(
        levelName = "Cálido",
        minTemp = 24.0,
        maxTemp = 28.0,
        clothingRule = "Manga corta estándar.",
        color = Color(0xFFF59E0B)
    ),
    CALOR_INTENSO(
        levelName = "Calor Intenso",
        minTemp = 28.0,
        maxTemp = 99.0,
        clothingRule = "Manga corta ligera / ropa transpirable.",
        color = Color(0xFFEF4444)
    );

    companion object {
        fun fromTemp(perceivedTemp: Double): ThermalLevel {
            return when {
                perceivedTemp < 3.0 -> GELIDO
                perceivedTemp < 8.0 -> FRIO_INTENSO
                perceivedTemp < 15.0 -> FRIO_MODERADO
                perceivedTemp < 20.0 -> FRESCO_ENTRETIEMPO
                perceivedTemp < 24.0 -> TEMPLADO_SUAVE
                perceivedTemp < 28.0 -> CALIDO
                else -> CALOR_INTENSO
            }
        }
    }
}

/**
 * Bloques de Franjas Horarias (4 Segmentos diarios).
 */
enum class TimeSlotBlock(
    val title: String,
    val startHour: Int,
    val endHour: Int,
    val defaultIcon: String
) {
    MADRUGADA("Madrugada", 0, 6, "🌙"),
    MANANA("Mañana", 6, 12, "🌅"),
    TARDE("Tarde", 12, 18, "☀️"),
    NOCHE("Noche", 18, 24, "🌆");

    companion object {
        fun fromHour(hour: Int): TimeSlotBlock {
            val h = (hour % 24 + 24) % 24
            return when (h) {
                in 0..5 -> MADRUGADA
                in 6..11 -> MANANA
                in 12..17 -> TARDE
                else -> NOCHE
            }
        }
    }
}

/**
 * Sistema de Capas Modulares (Método Cebolla 3 Capas Técnica).
 */
data class ModularLayerStrategy(
    val baseLayer: String,               // Capa Base (Contacto): Transpiración
    val midLayer: String?,               // Capa Intermedia (Aislamiento): Retención de calor
    val outerLayer: String?,             // Capa Exterior (Protección): Cortavientos / Chubasquero / Abrigo
    val totalLayers: Int,                // 1, 2 o 3 capas
    val isThermalOscillationHigh: Boolean, // true si oscilación térmica > 7°C
    val layerTransitionAdvice: String?,  // Indicación exacta de qué prenda quitarse o ponerse según la hora
    val isActive: Boolean = true         // Solo se activa si la temperatura mínima cae por debajo de 14 °C
)

/**
 * Factor de aislamiento por calzado.
 */
data class DynamicFootwearPill(
    val icon: String,       // 👟 o 🥾
    val title: String,      // "Calzado impermeable" o "Calzado transpirable"
    val reason: String      // Motivo fisiológico y atmosférico
)

/**
 * Complemento dinámico clave (Gafas, Paraguas, Gorra, Bufanda).
 */
data class DynamicComplementPill(
    val icon: String,
    val label: String,
    val isCrucial: Boolean = false
)

/**
 * Línea de Tiempo Visual Rápida (4 Hitos de lectura en < 2 segundos).
 */
data class TimelineMilestone(
    val id: String,           // "SALIDA", "MEDIODIA", "TARDE", "REGRESO"
    val icon: String,         // "🌅", "☀️", "🌆", "🌙"
    val title: String,        // "Salida de casa (~08:00)", "Mediodía (~14:00)", etc.
    val temp: Int,            // Temperatura esperada
    val perceivedTemp: Int,   // Sensación bioclimática
    val keyGarment: String,   // Prenda clave concreta (micro-etiqueta)
    val actionAdvice: String  // Acción de transición concisa
)

data class TimeSlotAdvice(
    val block: TimeSlotBlock,
    val hourLabel: String,
    val fullTitle: String,
    val temp: Int,
    val perceivedTemp: Int,
    val conditionTag: String,
    val adviceText: String,
    val icon: String
)

data class ComplementAlert(
    val icon: String,
    val title: String,
    val description: String,
    val isWarning: Boolean = false
)

data class BioclimaticMetrics(
    val dryTemp: Double,
    val apparentTemp: Double,
    val bioclimaticPerceivedTemp: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windGusts: Double,
    val maxUv: Double,
    val rainProb: Int,
    val precipitationMm: Double,
    val isDampCold: Boolean,
    val isMuggyHeat: Boolean
)

data class AdvancedBioclimaticMetrics(
    val solarBoostSun: Double,          // +3.0 a +5.0 °C al sol directo vs 0.0 °C sombra
    val solarPerceivedTemp: Double,      // Sensación al sol directo
    val shadePerceivedTemp: Double,      // Sensación a la sombra / cubierto
    val cloudCover: Int,
    val isDay: Boolean,
    val thermalOscillation: Double,     // Oscilación térmica máxima en el período
    val sunsetTime: String?,            // Hora oficial de puesta de sol
    val nocturnalInversionRisk: Boolean, // Caída térmica rápida post-sunset
    val minTempPeriod: Double = 20.0
)

data class ClothingRecommendation(
    val headline: String,
    val thermalLevel: ThermalLevel,
    val baseSummary: String,
    val layerStrategy: ModularLayerStrategy,
    val footwearPill: DynamicFootwearPill,
    val complementPills: List<DynamicComplementPill>,
    val milestones: List<TimelineMilestone>,
    val timeSlots: List<TimeSlotAdvice>,
    val complementAlerts: List<ComplementAlert>,
    val metrics: BioclimaticMetrics,
    val advancedMetrics: AdvancedBioclimaticMetrics
) {
    /**
     * Convierte a modelo compatible con interfaces heredadas.
     */
    fun toClothingAdvice(): ClothingAdvice {
        val modifiers = mutableListOf<AdviceModifier>()
        modifiers.add(
            AdviceModifier(
                icon = footwearPill.icon,
                title = footwearPill.title,
                text = footwearPill.reason
            )
        )
        complementPills.forEach { pill ->
            modifiers.add(
                AdviceModifier(
                    icon = pill.icon,
                    title = "Complemento",
                    text = pill.label
                )
            )
        }
        complementAlerts.forEach { alert ->
            modifiers.add(
                AdviceModifier(
                    icon = alert.icon,
                    title = alert.title,
                    text = alert.description
                )
            )
        }
        val timeSlotAlert = layerStrategy.layerTransitionAdvice ?: timeSlots.firstOrNull()?.adviceText
        return ClothingAdvice(
            baseAdvice = headline,
            modifiers = modifiers,
            timeSlotAlert = timeSlotAlert
        )
    }
}

/**
 * Motor determinista y 100% local de recomendación de vestimenta basado en
 * Sensación Térmica Bioclimática Real, Inercia Solar vs. Sombra,
 * Aislamiento por Calzado, Tasa de Ventilación y Método Cebolla Modular.
 */
object BioclimaticClothingEngine {

    /**
     * Cálculo de presión de vapor e (hPa) mediante fórmula de Magnus-Tetens.
     */
    fun calculateVaporPressureHpa(tempC: Double, relativeHumidity: Double): Double {
        val clampedRh = relativeHumidity.coerceIn(0.0, 100.0)
        return 6.112 * exp((17.67 * tempC) / (tempC + 243.5)) * (clampedRh / 100.0)
    }

    /**
     * Cálculo del índice de humedad Humidex canadiense.
     * Humidex = T + (5/9) * (e - 10)
     */
    fun calculateHumidex(tempC: Double, relativeHumidity: Double): Double {
        val e = calculateVaporPressureHpa(tempC, relativeHumidity)
        return tempC + (5.0 / 9.0) * (e - 10.0)
    }

    /**
     * Factor de enfriamiento por viento (Wind Chill - fórmula JAG/TI estándar internacional).
     * Aplicable cuando T <= 10°C y V >= 4.8 km/h.
     */
    fun calculateWindChill(tempC: Double, windSpeedKmH: Double): Double {
        if (windSpeedKmH < 4.8 || tempC > 10.0) return tempC
        val vExp = windSpeedKmH.pow(0.16)
        return 13.12 + 0.6215 * tempC - 11.37 * vExp + 0.3965 * tempC * vExp
    }

    /**
     * Inercia Térmica por Radiación Solar Directa vs. Sombra:
     * A igual temperatura (ej. 19 °C), el sol directo genera un calor percibido de +3 °C a +5 °C,
     * mientras que un cielo cubierto (cloud cover > 80%) o la sombra se siente a la temperatura real.
     */
    fun calculateSolarRadiationBoost(
        isDay: Boolean,
        cloudCoverPercent: Int,
        uvIndex: Double
    ): Double {
        if (!isDay) return 0.0
        val clearSkyFactor = ((100 - cloudCoverPercent.coerceIn(0, 100)) / 100.0)
        if (clearSkyFactor < 0.20) return 0.0 // Cielo muy nublado o cubierto = sin ganancia solar directa

        val uvClamped = uvIndex.coerceIn(1.0, 10.0)
        // Escalado entre 3.0°C y 5.0°C proporcional al despeje del cielo e índice UV
        val boost = 3.0 + (2.0 * (uvClamped / 10.0) * clearSkyFactor)
        return (boost * clearSkyFactor).coerceIn(0.0, 5.0)
    }

    /**
     * Sensación Térmica Bioclimática Real cruzando Humidex, Wind Chill,
     * penalización por "Frío Calado" y sensación aparente calibrada.
     */
    fun calculateBioclimaticPerceivedTemp(
        tempC: Double,
        relativeHumidity: Double,
        windSpeedKmH: Double,
        apparentTempApi: Double? = null
    ): Double {
        var calculated = tempC

        if (tempC <= 10.0 && windSpeedKmH >= 4.8) {
            calculated = calculateWindChill(tempC, windSpeedKmH)
        } else if (tempC >= 20.0 && relativeHumidity >= 35.0) {
            calculated = calculateHumidex(tempC, relativeHumidity)
        }

        // Humedad Alta (> 75%) + Frío (<= 13°C): Genera "frío calado"
        if (relativeHumidity > 75.0 && tempC <= 13.0) {
            val dampPenalty = ((relativeHumidity - 75.0) / 25.0) * 2.2
            calculated -= dampPenalty
        }

        return if (apparentTempApi != null) {
            (calculated * 0.65) + (apparentTempApi * 0.35)
        } else {
            calculated
        }
    }

    /**
     * Genera la recomendación integral determinista y de alta precisión
     * optimizada en tiempo de ejecución (< 1 ms).
     */
    fun calculate(
        currentTemp: Double,
        currentHumidity: Int,
        currentWindSpeed: Double,
        currentWindGusts: Double? = null,
        currentApparentTemp: Double? = null,
        currentRainProb: Int = 0,
        currentPrecipitation: Double? = null,
        currentUvIndex: Double? = null,
        currentCloudCover: Int? = null,
        isDay: Boolean = true,
        hourlyItems: List<HourlyItem> = emptyList(),
        dailyMaxUv: Double? = null,
        dailyPrecipSum: Double? = null,
        sunsetTime: String? = null
    ): ClothingRecommendation {
        val lookaheadHours = hourlyItems.take(18)

        // 1. Inercia Térmica Solar vs. Sombra
        val effectiveCloud = currentCloudCover ?: lookaheadHours.firstOrNull()?.cloudCover ?: 40
        val effectiveUv = currentUvIndex ?: dailyMaxUv ?: 3.0
        val solarBoost = calculateSolarRadiationBoost(
            isDay = isDay,
            cloudCoverPercent = effectiveCloud,
            uvIndex = effectiveUv
        )

        val effectiveGusts = currentWindGusts ?: (currentWindSpeed * 1.35)
        val shadePerceived = calculateBioclimaticPerceivedTemp(
            tempC = currentTemp,
            relativeHumidity = currentHumidity.toDouble(),
            windSpeedKmH = currentWindSpeed,
            apparentTempApi = currentApparentTemp
        )
        val sunPerceived = shadePerceived + solarBoost

        val isDampCold = currentHumidity > 75 && currentTemp <= 13.0
        // Tasa de Ventilación por Humedad / Bochorno (Sweat Efficiency Factor)
        val isMuggyHeat = currentHumidity > 70 && currentTemp >= 24.0

        // Variables máximas y mínimas del período 12-18h
        val maxWind = maxOf(currentWindSpeed, lookaheadHours.maxOfOrNull { it.windSpeed ?: it.temp.toDouble() } ?: currentWindSpeed)
        val maxGusts = maxOf(effectiveGusts, lookaheadHours.maxOfOrNull { it.windGusts ?: 0.0 } ?: effectiveGusts)
        val maxRainProb = maxOf(currentRainProb, lookaheadHours.maxOfOrNull { it.rainProb } ?: currentRainProb)
        val maxPrecipMm = maxOf(currentPrecipitation ?: 0.0, lookaheadHours.maxOfOrNull { it.precipitation ?: 0.0 } ?: 0.0)
        val precipSum = dailyPrecipSum ?: (maxPrecipMm * 2.0)
        val maxUv = dailyMaxUv ?: maxOf(currentUvIndex ?: 0.0, lookaheadHours.maxOfOrNull { it.uvIndex ?: 0.0 } ?: 0.0)

        val minTempPeriod = minOf(currentTemp, lookaheadHours.minOfOrNull { it.temp.toDouble() } ?: currentTemp)
        val maxTempPeriod = maxOf(currentTemp, lookaheadHours.maxOfOrNull { it.temp.toDouble() } ?: currentTemp)
        val thermalOscillation = maxTempPeriod - minTempPeriod
        val isHighOscillation = thermalOscillation >= 7.0

        val thermalLevel = ThermalLevel.fromTemp(sunPerceived)

        // 2. Factor de Aislamiento por Calzado
        // Si acumulación de lluvia > 2 mm o probabilidad de precipitación > 60%
        val hasRainOrPuddles = precipSum >= 2.0 || maxRainProb >= 60 || (currentPrecipitation ?: 0.0) >= 0.8 || maxPrecipMm >= 1.5
        val footwearPill = when {
            hasRainOrPuddles -> DynamicFootwearPill(
                icon = "🥾",
                title = "Calzado impermeable / antideslizante",
                reason = "Lluvia prevista. Suela antideslizante y tejido hidrófugo para pies secos."
            )
            shadePerceived < 6.0 && minTempPeriod < 14.0 -> DynamicFootwearPill(
                icon = "🥾",
                title = "Calzado térmico / calcetín grueso",
                reason = "Frío en superficie. Suela gruesa aislante para retener el calor corporal."
            )
            currentTemp >= 23.0 && !hasRainOrPuddles -> DynamicFootwearPill(
                icon = "👟",
                title = "Calzado ultra-transpirable",
                reason = "Zapatillas ligeras para favorecer la ventilación natural y evitar sudor."
            )
            else -> DynamicFootwearPill(
                icon = "👟",
                title = "Calzado cómodo estándar",
                reason = "Superficie seca y temperatura favorable. Calzado habitual."
            )
        }

        // 3. Píldoras de Complementos Dinámicos
        val complementPills = mutableListOf<DynamicComplementPill>()
        val uvPeakHour = lookaheadHours.maxByOrNull { it.uvIndex ?: 0.0 }?.let { extractHour(it) } ?: 14
        if (maxUv >= 5.5) {
            val uvText = "UV ${"%.1f".format(Locale.US, maxUv)} a las $uvPeakHour:00"
            complementPills.add(
                DynamicComplementPill(
                    icon = "🕶️",
                    label = "Gafas de sol ($uvText)",
                    isCrucial = maxUv >= 7.0
                )
            )
        }
        if (hasRainOrPuddles || maxRainProb >= 40) {
            complementPills.add(
                DynamicComplementPill(
                    icon = "🌂",
                    label = if (maxPrecipMm >= 2.0) "Paraguas resistente" else "Paraguas compacto",
                    isCrucial = hasRainOrPuddles
                )
            )
        }
        if (maxUv >= 6.5 || currentTemp >= 27.0) {
            complementPills.add(
                DynamicComplementPill(
                    icon = "🧢",
                    label = "Gorra o sombrero transpirable"
                )
            )
        }
        if ((shadePerceived < 8.0 || isDampCold) && minTempPeriod < 14.0) {
            complementPills.add(
                DynamicComplementPill(
                    icon = "🧣",
                    label = "Bufanda / braga de cuello",
                    isCrucial = shadePerceived < 4.0
                )
            )
        }

        // 4. Sistema de Capas Modulares Reales (Método Cebolla 3 Capas)
        val peakItem = lookaheadHours.maxByOrNull { it.temp }
        val peakHour = peakItem?.let { extractHour(it) } ?: 14
        val sunsetHourStr = extractSunsetHour(sunsetTime)

        val hasWindRisk = currentWindSpeed > 20.0 || maxWind > 22.0 || effectiveGusts > 35.0 || maxGusts > 35.0
        val hasRainRisk = hasRainOrPuddles || maxRainProb >= 40

        val layerStrategy = buildModularLayerStrategy(
            currentTemp = currentTemp,
            perceivedTemp = sunPerceived,
            minTempPeriod = minTempPeriod,
            thermalOscillation = thermalOscillation,
            isHighOscillation = isHighOscillation,
            peakHour = peakHour,
            isMuggyHeat = isMuggyHeat,
            hasWindRisk = hasWindRisk,
            hasRainRisk = hasRainRisk,
            sunsetHourStr = sunsetHourStr
        )

        // 5. Línea de Tiempo Visual Rápida (4 Hitos: Salida, Mediodía, Tarde, Regreso/Noche)
        val milestones = buildTimelineMilestones(
            currentTemp = currentTemp,
            lookaheadHours = lookaheadHours,
            minTempPeriod = minTempPeriod,
            layerStrategy = layerStrategy,
            isHighOscillation = isHighOscillation,
            sunsetHourStr = sunsetHourStr,
            hasRainRisk = hasRainRisk
        )

        // 6. Alertas de complementos detalladas
        val complementAlerts = mutableListOf<ComplementAlert>()
        if (hasRainRisk) {
            val title = if (hasRainOrPuddles) "Lluvia continua o charcos" else "Chubascos probables"
            complementAlerts.add(
                ComplementAlert(
                    icon = "☂️",
                    title = title,
                    description = "Probabilidad del $maxRainProb%. Lleva paraguas y usa ${footwearPill.title.lowercase(Locale.ROOT)}.",
                    isWarning = hasRainOrPuddles
                )
            )
        }
        if (hasWindRisk) {
            val windValue = maxOf(currentWindSpeed, maxWind).roundToInt()
            val gustValue = maxOf(effectiveGusts, maxGusts).roundToInt()
            val windTitle = if (minTempPeriod >= 18.0) "Viento moderado" else "Cortaaires / Cortavientos"
            val windDesc = if (minTempPeriod >= 18.0) {
                "Rachas de hasta $gustValue km/h. Viento cálido sin sensación de frío."
            } else {
                "Viento de $windValue km/h (rachas hasta $gustValue km/h). Capa cortavientos aconsejada."
            }
            complementAlerts.add(
                ComplementAlert(
                    icon = if (minTempPeriod >= 18.0) "💨" else "🧥",
                    title = windTitle,
                    description = windDesc,
                    isWarning = maxWind > 35.0
                )
            )
        }
        if (isDampCold && minTempPeriod < 14.0) {
            complementAlerts.add(
                ComplementAlert(
                    icon = "❄️",
                    title = "Frío Calado",
                    description = "Sensación térmica de ${"%.1f".format(Locale.US, shadePerceived)}°C; requiere capa cortavientos.",
                    isWarning = false
                )
            )
        } else if (isMuggyHeat) {
            complementAlerts.add(
                ComplementAlert(
                    icon = "💧",
                    title = "Bochorno (Humedad ${currentHumidity}%)",
                    description = "El sudor no se evapora fácilmente; viste tejidos naturales claros y holgados.",
                    isWarning = false
                )
            )
        }

        // 7. Franjas Horarias (12-18h)
        val timeSlots = buildTimeSlotsAdvice(currentTemp, lookaheadHours, minTempPeriod)

        // 8. Titular Hiper-Preciso en 1 frase de lectura ultra-rápida (< 2 seg)
        val headline = generatePrecisionHeadline(
            currentTemp = currentTemp.roundToInt(),
            perceivedTemp = sunPerceived.roundToInt(),
            minTempPeriod = minTempPeriod,
            thermalOscillation = thermalOscillation,
            isHighOscillation = isHighOscillation,
            peakHour = peakHour,
            layerStrategy = layerStrategy,
            isMuggyHeat = isMuggyHeat,
            isDampCold = isDampCold,
            hasRainRisk = hasRainRisk,
            sunsetHourStr = sunsetHourStr
        )

        val metrics = BioclimaticMetrics(
            dryTemp = currentTemp,
            apparentTemp = currentApparentTemp ?: currentTemp,
            bioclimaticPerceivedTemp = sunPerceived,
            humidity = currentHumidity,
            windSpeed = currentWindSpeed,
            windGusts = effectiveGusts,
            maxUv = maxUv,
            rainProb = maxRainProb,
            precipitationMm = maxPrecipMm,
            isDampCold = isDampCold,
            isMuggyHeat = isMuggyHeat
        )

        val advancedMetrics = AdvancedBioclimaticMetrics(
            solarBoostSun = solarBoost,
            solarPerceivedTemp = sunPerceived,
            shadePerceivedTemp = shadePerceived,
            cloudCover = effectiveCloud,
            isDay = isDay,
            thermalOscillation = thermalOscillation,
            sunsetTime = sunsetHourStr,
            nocturnalInversionRisk = !isDay || (sunsetHourStr != null && shadePerceived < currentTemp - 2.0),
            minTempPeriod = minTempPeriod
        )

        return ClothingRecommendation(
            headline = headline,
            thermalLevel = thermalLevel,
            baseSummary = thermalLevel.clothingRule,
            layerStrategy = layerStrategy,
            footwearPill = footwearPill,
            complementPills = complementPills,
            milestones = milestones,
            timeSlots = timeSlots,
            complementAlerts = complementAlerts,
            metrics = metrics,
            advancedMetrics = advancedMetrics
        )
    }

    /**
     * Construye el sistema técnico de 3 capas modulares (Método Cebolla).
     * Umbral de Capas: La 'Estrategia de Capas' SOLO se activa si la temperatura mínima cae por debajo de 14 °C.
     * Si la temperatura mínima es ≥ 18 °C, queda prohibido mencionar abrigos, jerseys o chaquetas.
     */
    private fun buildModularLayerStrategy(
        currentTemp: Double,
        perceivedTemp: Double,
        minTempPeriod: Double,
        thermalOscillation: Double,
        isHighOscillation: Boolean,
        peakHour: Int,
        isMuggyHeat: Boolean,
        hasWindRisk: Boolean,
        hasRainRisk: Boolean,
        sunsetHourStr: String
    ): ModularLayerStrategy {
        val isLayerStrategyActive = minTempPeriod < 14.0

        // Si la temperatura mínima es >= 18 °C, estrictamente prohibido abrigos, jerseys o chaquetas
        if (minTempPeriod >= 18.0) {
            val base = if (currentTemp >= 28.0 || perceivedTemp >= 28.0) {
                "Manga corta ligera / ropa transpirable"
            } else {
                "Manga corta estándar"
            }
            return ModularLayerStrategy(
                baseLayer = base,
                midLayer = null,
                outerLayer = if (hasRainRisk) "Chubasquero impermeable fino" else null,
                totalLayers = 1,
                isThermalOscillationHigh = false,
                layerTransitionAdvice = null,
                isActive = false
            )
        }

        // Si la temperatura mínima no cae por debajo de 14 °C (14.0 a 17.9 °C), la estrategia de capas NO se activa
        if (!isLayerStrategyActive) {
            val base = when {
                currentTemp >= 28.0 || perceivedTemp >= 28.0 -> "Manga corta ligera / ropa transpirable"
                currentTemp >= 20.0 || perceivedTemp >= 20.0 -> "Manga corta estándar"
                else -> "Manga corta o manga larga fina"
            }
            val outer = when {
                hasRainRisk -> "Chubasquero ligero con capucha"
                hasWindRisk -> "Cortavientos ligero"
                currentTemp < 20.0 || perceivedTemp < 20.0 -> "Chaqueta fina de respaldo"
                else -> null
            }
            return ModularLayerStrategy(
                baseLayer = base,
                midLayer = null,
                outerLayer = outer,
                totalLayers = if (outer != null) 2 else 1,
                isThermalOscillationHigh = false,
                layerTransitionAdvice = if (outer != null) "Manga corta de día con chaqueta fina de respaldo si refresca." else null,
                isActive = false
            )
        }

        // minTempPeriod < 14.0: Estrategia de Capas (Método Cebolla) ACTIVA
        val base = when {
            isMuggyHeat -> "Prendas holgadas de algodón/lino (ventilación máxima)"
            perceivedTemp >= 20.0 -> "Camiseta de manga corta"
            perceivedTemp in 10.0..19.9 -> "Camiseta básica de manga corta o larga"
            else -> "Camiseta térmica pegada al cuerpo"
        }

        val mid = when {
            minTempPeriod < 8.0 -> "Jersey de lana o forro polar aislante"
            minTempPeriod < 12.0 -> "Jersey o sudadera de algodón"
            else -> "Jersey fino o cárdigan ligero"
        }

        val outer = when {
            hasRainRisk -> "Chubasquero o impermeable con capucha"
            hasWindRisk -> "Chaqueta cortavientos transpirable"
            minTempPeriod < 6.0 || perceivedTemp <= 6.0 -> "Abrigo de invierno o cazadora gruesa"
            minTempPeriod < 10.0 -> "Cazadora o abrigo ligero"
            else -> "Chaqueta fina o cazadora de entretiempo"
        }

        val totalLayers = listOfNotNull(base, mid, outer).size

        val transitionAdvice = if (isHighOscillation) {
            "Oscilación de ${thermalOscillation.roundToInt()}°C: sal con las capas y retira la chaqueta en las horas centrales."
        } else {
            "Mantén la chaqueta o jersey a mano para las horas más frescas."
        }

        return ModularLayerStrategy(
            baseLayer = base,
            midLayer = mid,
            outerLayer = outer,
            totalLayers = totalLayers,
            isThermalOscillationHigh = isHighOscillation,
            layerTransitionAdvice = transitionAdvice,
            isActive = true
        )
    }

    /**
     * Construye la Línea de Tiempo Visual Rápida (4 Hitos de lectura en < 2 segundos).
     * Aplica la Regla del Ocaso y los Niveles Térmicos Directos con micro-etiquetas concisas.
     */
    private fun buildTimelineMilestones(
        currentTemp: Double,
        lookaheadHours: List<HourlyItem>,
        minTempPeriod: Double,
        layerStrategy: ModularLayerStrategy,
        isHighOscillation: Boolean,
        sunsetHourStr: String,
        hasRainRisk: Boolean
    ): List<TimelineMilestone> {
        val salidaItem = lookaheadHours.firstOrNull { extractHour(it) in 7..9 } ?: lookaheadHours.firstOrNull()
        val mediodiaItem = lookaheadHours.firstOrNull { extractHour(it) in 13..15 } ?: lookaheadHours.maxByOrNull { it.temp } ?: salidaItem
        val tardeItem = lookaheadHours.firstOrNull { extractHour(it) in 17..19 } ?: mediodiaItem
        val regresoItem = lookaheadHours.firstOrNull { extractHour(it) in 20..23 } ?: lookaheadHours.lastOrNull() ?: tardeItem

        fun tempOf(item: HourlyItem?, fallback: Double): Int = item?.temp ?: fallback.roundToInt()
        fun perceivedOf(item: HourlyItem?, fallback: Double): Int {
            if (item == null) return fallback.roundToInt()
            return calculateBioclimaticPerceivedTemp(
                tempC = item.temp.toDouble(),
                relativeHumidity = (item.humidity ?: 60).toDouble(),
                windSpeedKmH = item.windSpeed ?: 10.0,
                apparentTempApi = item.apparentTemp
            ).roundToInt()
        }

        val tSalida = tempOf(salidaItem, currentTemp)
        val pSalida = perceivedOf(salidaItem, currentTemp)

        val tMediodia = tempOf(mediodiaItem, currentTemp + 4.0)
        val pMediodia = perceivedOf(mediodiaItem, currentTemp + 5.0)

        val tTarde = tempOf(tardeItem, currentTemp + 2.0)
        val pTarde = perceivedOf(tardeItem, currentTemp + 2.0)

        val tRegreso = tempOf(regresoItem, currentTemp - 2.0)
        val pRegreso = perceivedOf(regresoItem, currentTemp - 3.0)

        // Micro-etiquetas concisas siguiendo los Niveles Térmicos Directos:
        // >= 28 °C: Manga corta ligera / ropa transpirable
        // 20 °C a 27 °C: Manga corta estándar
        // 15 °C a 19 °C: Manga corta + chaqueta fina de respaldo
        // < 15 °C: Abrigo o jersey
        // EXCEPCIÓN: Si minTempPeriod >= 18 °C, prohibido abrigos, jerseys o chaquetas.
        fun getMilestoneGarment(temp: Int, isNight: Boolean = false): String {
            if (minTempPeriod >= 18.0) {
                return if (temp >= 28) "Manga corta ligera" else "Manga corta"
            }
            if (isNight) {
                // Regla del Ocaso:
                // Si la noche está a >= 22 °C -> ropa de verano / manga corta
                // Solo sugiere abrigo si < 16 °C
                return when {
                    temp >= 22 -> "Manga corta"
                    temp in 16..21 -> "Chaqueta fina"
                    else -> "Abrigo o jersey"
                }
            }
            return when {
                temp >= 28 -> "Manga corta ligera"
                temp in 20..27 -> "Manga corta"
                temp in 15..19 -> "Chaqueta fina"
                else -> "Abrigo o jersey"
            }
        }

        val salidaGarment = getMilestoneGarment(tSalida)
        val salidaAction = when {
            tSalida >= 22 -> "Mañana cálida"
            tSalida in 16..21 -> "Mañana suave"
            else -> "Mañana fresca"
        }

        val mediodiaGarment = getMilestoneGarment(tMediodia)
        val mediodiaAction = when {
            tMediodia >= 28 -> "Calor diurno"
            tMediodia in 20..27 -> "Horas centrales"
            else -> "Máxima templada"
        }

        val tardeGarment = getMilestoneGarment(tTarde)
        val tardeAction = when {
            tTarde >= 24 -> "Tarde cálida"
            tTarde in 18..23 -> "Tarde agradable"
            else -> "Descenso térmico"
        }

        val regresoGarment = getMilestoneGarment(tRegreso, isNight = true)
        val regresoAction = when {
            tRegreso >= 22 -> "Noche cálida (≥22°C)"
            tRegreso in 16..21 -> "Noche suave"
            else -> "Refresca al anochecer"
        }

        return listOf(
            TimelineMilestone(
                id = "SALIDA",
                icon = "🌅",
                title = "Salida (~08:00 h)",
                temp = tSalida,
                perceivedTemp = pSalida,
                keyGarment = salidaGarment,
                actionAdvice = salidaAction
            ),
            TimelineMilestone(
                id = "MEDIODIA",
                icon = "☀️",
                title = "Mediodía (~14:00 h)",
                temp = tMediodia,
                perceivedTemp = pMediodia,
                keyGarment = mediodiaGarment,
                actionAdvice = mediodiaAction
            ),
            TimelineMilestone(
                id = "TARDE",
                icon = "🌆",
                title = "Tarde (~18:00 h)",
                temp = tTarde,
                perceivedTemp = pTarde,
                keyGarment = tardeGarment,
                actionAdvice = tardeAction
            ),
            TimelineMilestone(
                id = "REGRESO",
                icon = "🌙",
                title = "Regreso (~21:00 h)",
                temp = tRegreso,
                perceivedTemp = pRegreso,
                keyGarment = regresoGarment,
                actionAdvice = regresoAction
            )
        )
    }

    /**
     * Titular Hiper-Preciso en 1 frase de máximo 1-2 líneas (Cero Parrafadas).
     */
    private fun generatePrecisionHeadline(
        currentTemp: Int,
        perceivedTemp: Int,
        minTempPeriod: Double,
        thermalOscillation: Double,
        isHighOscillation: Boolean,
        peakHour: Int,
        layerStrategy: ModularLayerStrategy,
        isMuggyHeat: Boolean,
        isDampCold: Boolean,
        hasRainRisk: Boolean,
        sunsetHourStr: String
    ): String {
        return when {
            minTempPeriod >= 18.0 -> {
                if (currentTemp >= 28 || perceivedTemp >= 28) {
                    "Manga corta ligera y ropa transpirable. Jornada puramente veraniega."
                } else {
                    "Manga corta durante toda la jornada. Ambiente cálido y agradable."
                }
            }
            hasRainRisk -> {
                "Calzado antideslizante impermeable y paraguas para chubascos previstos."
            }
            isMuggyHeat -> {
                "Prendas de lino o algodón transpirable para mitigar el bochorno."
            }
            isDampCold && minTempPeriod < 14.0 -> {
                "Capa cortavientos imprescindible ante la sensación de frío calado por humedad."
            }
            layerStrategy.isActive && isHighOscillation -> {
                "Estrategia de capas: manga corta diurna y chaqueta para mañana y anochecer."
            }
            layerStrategy.isActive -> {
                "Estrategia de capas: prenda de abrigo o jersey para las horas más frescas."
            }
            currentTemp >= 28 || perceivedTemp >= 28 -> {
                "Manga corta ligera y protección solar. Calor intenso durante el día."
            }
            currentTemp in 20..27 || perceivedTemp in 20..27 -> {
                "Manga corta estándar. Temperatura confortable durante toda la jornada."
            }
            currentTemp in 15..19 || perceivedTemp in 15..19 -> {
                "Manga corta con chaqueta fina de respaldo si refresca al anochecer."
            }
            else -> {
                "Abrigo o jersey necesario para mantener el confort térmico."
            }
        }
    }

    /**
     * Agrupa y calcula el diagnóstico para cada una de las franjas horarias activas en las próximas 12-18 horas.
     */
    private fun buildTimeSlotsAdvice(
        currentTemp: Double,
        lookaheadHours: List<HourlyItem>,
        minTempPeriod: Double
    ): List<TimeSlotAdvice> {
        if (lookaheadHours.isEmpty()) return emptyList()

        val distinctBlocks = linkedMapOf<String, MutableList<HourlyItem>>()

        lookaheadHours.forEach { item ->
            val hour = extractHour(item)
            val dateKey = item.rawTime.substringBefore("T")
            val block = TimeSlotBlock.fromHour(hour)
            val key = "${dateKey}_${block.name}"
            distinctBlocks.getOrPut(key) { mutableListOf() }.add(item)
        }

        val result = mutableListOf<TimeSlotAdvice>()

        distinctBlocks.values.take(4).forEach { blockItems ->
            val firstItem = blockItems.first()
            val hour = extractHour(firstItem)
            val block = TimeSlotBlock.fromHour(hour)

            val repItem = when (block) {
                TimeSlotBlock.MANANA -> blockItems.minByOrNull { kotlin.math.abs(extractHour(it) - 9) } ?: firstItem
                TimeSlotBlock.TARDE -> blockItems.maxByOrNull { it.temp } ?: firstItem
                TimeSlotBlock.NOCHE -> blockItems.minByOrNull { kotlin.math.abs(extractHour(it) - 21) } ?: firstItem
                TimeSlotBlock.MADRUGADA -> blockItems.minByOrNull { it.temp } ?: firstItem
            }

            val repHour = extractHour(repItem)
            val hourLabel = String.format(Locale.US, "%02d:00", repHour)
            val fullTitle = "${block.title} ($hourLabel)"

            val itemHumidity = repItem.humidity ?: 60
            val itemWind = repItem.windSpeed ?: 10.0
            val itemPerceived = calculateBioclimaticPerceivedTemp(
                tempC = repItem.temp.toDouble(),
                relativeHumidity = itemHumidity.toDouble(),
                windSpeedKmH = itemWind,
                apparentTempApi = repItem.apparentTemp
            ).roundToInt()

            val isRaining = (repItem.rainProb >= 40) || ((repItem.precipitation ?: 0.0) >= 0.3)
            val isWindy = (itemWind >= 20.0) || ((repItem.windGusts ?: 0.0) >= 30.0)

            val conditionTag = when {
                isRaining -> "Lluvia"
                isWindy -> "Viento"
                repItem.temp <= 8 -> "Frío"
                repItem.temp in 9..14 -> "Fresco"
                repItem.temp in 15..21 -> "Suave"
                repItem.temp in 22..27 -> "Cálido"
                else -> "Caluroso"
            }

            val slotThermalLevel = ThermalLevel.fromTemp(itemPerceived.toDouble())
            val adviceText = formatSlotAdvice(
                fullTitle = fullTitle,
                temp = repItem.temp,
                conditionTag = conditionTag,
                block = block,
                thermalLevel = slotThermalLevel,
                currentTemp = currentTemp.roundToInt(),
                minTempPeriod = minTempPeriod,
                isRaining = isRaining,
                isWindy = isWindy
            )

            val icon = when {
                isRaining -> "🌧️"
                isWindy -> "💨"
                block == TimeSlotBlock.MADRUGADA -> "🌙"
                block == TimeSlotBlock.MANANA -> "🌅"
                block == TimeSlotBlock.TARDE -> "☀️"
                else -> "🌆"
            }

            result.add(
                TimeSlotAdvice(
                    block = block,
                    hourLabel = hourLabel,
                    fullTitle = fullTitle,
                    temp = repItem.temp,
                    perceivedTemp = itemPerceived,
                    conditionTag = conditionTag,
                    adviceText = adviceText,
                    icon = icon
                )
            )
        }

        return result
    }

    private fun formatSlotAdvice(
        fullTitle: String,
        temp: Int,
        conditionTag: String,
        block: TimeSlotBlock,
        thermalLevel: ThermalLevel,
        currentTemp: Int,
        minTempPeriod: Double,
        isRaining: Boolean,
        isWindy: Boolean
    ): String {
        val clothingTip = when {
            minTempPeriod >= 18.0 -> {
                if (temp >= 28) "Manga corta ligera y transpirable." else "Manga corta estándar."
            }
            block == TimeSlotBlock.NOCHE -> {
                when {
                    temp >= 22 -> "Ropa de verano / manga corta."
                    temp in 16..21 -> "Manga corta o chaqueta fina."
                    else -> "Abrigo o jersey."
                }
            }
            temp >= 28 -> "Manga corta ligera / transpirable."
            temp in 20..27 -> "Manga corta estándar."
            temp in 15..19 -> "Manga corta con chaqueta fina de respaldo."
            else -> "Abrigo o jersey."
        }

        val extraTip = when {
            isRaining -> " Paraguas recomendado."
            isWindy && minTempPeriod < 18.0 -> " Cortavientos aconsejado."
            else -> ""
        }

        return "$fullTitle: ${temp}°C. $clothingTip$extraTip"
    }

    private fun extractHour(item: HourlyItem): Int {
        return try {
            item.rawTime.substringAfter("T").substringBefore(":").toInt()
        } catch (_: Exception) {
            12
        }
    }

    private fun extractSunsetHour(sunsetTime: String?): String {
        if (sunsetTime.isNullOrBlank()) return "20:30"
        return try {
            if (sunsetTime.contains("T")) {
                sunsetTime.substringAfter("T").take(5)
            } else if (sunsetTime.contains(":")) {
                sunsetTime.trim().take(5)
            } else {
                "20:30"
            }
        } catch (_: Exception) {
            "20:30"
        }
    }
}
