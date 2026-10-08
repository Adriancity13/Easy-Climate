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
    val minTempPeriod: Double = 20.0,
    val dewPoint: Double = 12.0,
    val humidex: Double = 20.0,
    val urbanWindSpeed: Double = 10.0,
    val deltaWindChill: Double = 0.0,
    val isMandatoryChestProtection: Boolean = false,
    val isRespiratoryMucosaRisk: Boolean = false,
    val isHighSweatRisk: Boolean = false,
    val solarRadiationWm2: Double = 0.0,
    val isSunShadeContrastHigh: Boolean = false,
    val isSunsetColdSweatRisk: Boolean = false,
    val requiredClo: Double = 0.5,
    val cloDescription: String = "0.5 CLO (Manga corta / Confort)"
)

enum class RecommendationSource(val label: String) {
    AI_BIOCLIMATIC("IA Bioclimática"),
    GEMINI_AI("IA Bioclimática"),
    LOCAL_ENGINE("Motor Local")
}

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
    val advancedMetrics: AdvancedBioclimaticMetrics,
    val source: RecommendationSource = RecommendationSource.LOCAL_ENGINE,
    val rainRisk: com.example.data.models.RainRiskLevel = com.example.data.models.RainRiskLevel.NONE,
    val userPreferences: com.example.data.models.UserPreferences = com.example.data.models.UserPreferences(),
    val quickLeavingAdvice: String? = null
) {
    /**
     * Fusiona la recomendación determinista base con el asesoramiento enriquecido de la IA de Groq (Llama 3.1 8B Instant).
     */
    fun mergeWithGroq(groqResult: GroqBioclimaticResult): ClothingRecommendation {
        val updatedMilestones = milestones.map { milestone ->
            when (milestone.id) {
                "SALIDA" -> if (groqResult.franjas.salida.isNotBlank()) milestone.copy(keyGarment = groqResult.franjas.salida) else milestone
                "MEDIODIA" -> if (groqResult.franjas.mediodia.isNotBlank()) milestone.copy(keyGarment = groqResult.franjas.mediodia) else milestone
                "TARDE" -> if (groqResult.franjas.tarde.isNotBlank()) milestone.copy(keyGarment = groqResult.franjas.tarde) else milestone
                "REGRESO" -> if (groqResult.franjas.regreso.isNotBlank()) milestone.copy(keyGarment = groqResult.franjas.regreso) else milestone
                else -> milestone
            }
        }
        val updatedFootwear = if (groqResult.calzado.isNotBlank()) {
            footwearPill.copy(reason = groqResult.calzado)
        } else footwearPill

        return this.copy(
            headline = if (groqResult.titular.isNotBlank()) groqResult.titular else headline,
            milestones = updatedMilestones,
            footwearPill = updatedFootwear,
            source = RecommendationSource.AI_BIOCLIMATIC
        )
    }

    /**
     * Fusiona la recomendación determinista base con el asesoramiento enriquecido de Gemini.
     */
    fun mergeWithGemini(geminiResult: GeminiBioclimaticResult): ClothingRecommendation {
        val updatedMilestones = milestones.map { milestone ->
            when (milestone.id) {
                "SALIDA" -> if (geminiResult.franjas.salida.isNotBlank()) milestone.copy(keyGarment = geminiResult.franjas.salida) else milestone
                "MEDIODIA" -> if (geminiResult.franjas.mediodia.isNotBlank()) milestone.copy(keyGarment = geminiResult.franjas.mediodia) else milestone
                "TARDE" -> if (geminiResult.franjas.tarde.isNotBlank()) milestone.copy(keyGarment = geminiResult.franjas.tarde) else milestone
                "REGRESO" -> if (geminiResult.franjas.regreso.isNotBlank()) milestone.copy(keyGarment = geminiResult.franjas.regreso) else milestone
                else -> milestone
            }
        }
        val updatedFootwear = if (geminiResult.calzado.isNotBlank()) {
            footwearPill.copy(reason = geminiResult.calzado)
        } else footwearPill

        return this.copy(
            headline = if (geminiResult.titular.isNotBlank()) geminiResult.titular else headline,
            milestones = updatedMilestones,
            footwearPill = updatedFootwear,
            source = RecommendationSource.AI_BIOCLIMATIC
        )
    }

    /**
     * Convierte a modelo compatible con interfaces heredadas y tarjetas diarias.
     */
    fun toClothingAdvice(): ClothingAdvice {
        val modifiers = mutableListOf<AdviceModifier>()
        if (advancedMetrics.isMandatoryChestProtection) {
            modifiers.add(
                AdviceModifier(
                    icon = "🛡️",
                    title = "Protección Pectoral",
                    text = "Viento urbano (${"%.0f".format(Locale.US, advancedMetrics.urbanWindSpeed)} km/h). Mantén el pecho cubierto."
                )
            )
        }
        if (advancedMetrics.isHighSweatRisk) {
            modifiers.add(
                AdviceModifier(
                    icon = "💧",
                    title = "Alta Humedad (Td ${"%.1f".format(Locale.US, advancedMetrics.dewPoint)}°C)",
                    text = "Fuerza tejidos sintéticos de secado rápido. Cero algodón absorbente."
                )
            )
        }
        if (advancedMetrics.isSunsetColdSweatRisk) {
            modifiers.add(
                AdviceModifier(
                    icon = "⚠️",
                    title = "Caída Térmica Vespertina",
                    text = "Bajan las temperaturas al anochecer. Ten un cortavientos fino a mano."
                )
            )
        }
        if (layerStrategy.isActive && layerStrategy.layerTransitionAdvice != null) {
            modifiers.add(
                AdviceModifier(
                    icon = "🧅",
                    title = "${layerStrategy.totalLayers} Capas Modulares",
                    text = layerStrategy.layerTransitionAdvice
                )
            )
        }
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
        if (clearSkyFactor < 0.25 || uvIndex < 2.5) return 0.0 // Cielo muy nublado o baja radiación = sin ganancia térmica directa perceptible

        val uvClamped = uvIndex.coerceIn(1.0, 10.0)
        // Estimación prudente y moderada (máximo 2.0 °C a 2.5 °C al sol directo con cielo despejado)
        val boost = 1.0 + (1.5 * (uvClamped / 10.0) * clearSkyFactor)
        return (boost * clearSkyFactor).coerceIn(0.0, 2.5)
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
        sunsetTime: String? = null,
        indicators: BioclimaticPhysicalIndicators? = null,
        preferences: com.example.data.models.UserPreferences = com.example.data.models.UserPreferences()
    ): ClothingRecommendation {
        val isWindowActive = preferences.isScheduledWindow && preferences.departureHour != null && preferences.returnHour != null
        val lookaheadHours = if (isWindowActive) {
            val dep = preferences.departureHour!!
            val ret = preferences.returnHour!!
            val filtered = hourlyItems.filter { item ->
                val hour = extractHour(item)
                if (dep <= ret) {
                    hour in dep..ret
                } else {
                    hour >= dep || hour <= ret
                }
            }
            if (filtered.isNotEmpty()) filtered else hourlyItems.take(5)
        } else {
            when (preferences.duration) {
                com.example.data.models.OutingDuration.SHORT -> hourlyItems.take(2)
                com.example.data.models.OutingDuration.MEDIUM -> hourlyItems.take(5)
                com.example.data.models.OutingDuration.ALL_DAY -> hourlyItems.take(18)
            }
        }

        val windowStartTemp = if (isWindowActive && lookaheadHours.isNotEmpty()) {
            lookaheadHours.first().temp.toDouble()
        } else currentTemp

        val windowReturnTemp = if (isWindowActive && lookaheadHours.isNotEmpty()) {
            lookaheadHours.last().temp.toDouble()
        } else (lookaheadHours.lastOrNull()?.temp?.toDouble() ?: currentTemp)

        // 1. Inercia Térmica Solar vs. Sombra
        val effectiveCloud = currentCloudCover ?: lookaheadHours.firstOrNull()?.cloudCover ?: 40
        val effectiveUv = currentUvIndex ?: dailyMaxUv ?: 3.0
        val solarBoost = calculateSolarRadiationBoost(
            isDay = isDay,
            cloudCoverPercent = effectiveCloud,
            uvIndex = effectiveUv
        )

        val cyclingBoost = if (preferences.activity == com.example.data.models.ActivityType.CYCLING) 12.0 else 0.0
        val effectiveWindSpeed = currentWindSpeed + cyclingBoost
        val effectiveGusts = (currentWindGusts ?: (currentWindSpeed * 1.35)) + cyclingBoost
        val shadePerceived = calculateBioclimaticPerceivedTemp(
            tempC = currentTemp,
            relativeHumidity = currentHumidity.toDouble(),
            windSpeedKmH = effectiveWindSpeed,
            apparentTempApi = currentApparentTemp
        )
        val sunPerceived = shadePerceived + solarBoost
        val personalizedPerceived = sunPerceived + preferences.sensitivity.tempOffset

        val isDampCold = currentHumidity > 75 && currentTemp <= 13.0
        val isMuggyHeat = currentHumidity > 70 && currentTemp >= 24.0

        // Variables máximas y mínimas del período seleccionado
        val maxWind = maxOf(effectiveWindSpeed, lookaheadHours.maxOfOrNull { it.windSpeed ?: 0.0 } ?: effectiveWindSpeed)
        val maxGusts = maxOf(effectiveGusts, lookaheadHours.maxOfOrNull { it.windGusts ?: 0.0 } ?: effectiveGusts)
        val maxRainProb = maxOf(currentRainProb, lookaheadHours.maxOfOrNull { it.rainProb } ?: currentRainProb)
        val maxPrecipMm = maxOf(currentPrecipitation ?: 0.0, lookaheadHours.maxOfOrNull { it.precipitation ?: 0.0 } ?: 0.0)
        val precipSum = dailyPrecipSum ?: (maxPrecipMm * 2.0)
        val maxUv = dailyMaxUv ?: maxOf(currentUvIndex ?: 0.0, lookaheadHours.maxOfOrNull { it.uvIndex ?: 0.0 } ?: 0.0)

        val minTempPeriod = minOf(currentTemp, lookaheadHours.minOfOrNull { it.temp.toDouble() } ?: currentTemp)
        val maxTempPeriod = maxOf(currentTemp, lookaheadHours.maxOfOrNull { it.temp.toDouble() } ?: currentTemp)
        val thermalOscillation = maxTempPeriod - minTempPeriod
        val isHighOscillation = thermalOscillation >= 7.0

        val thermalLevel = ThermalLevel.fromTemp(personalizedPerceived)

        // 2. Lógica precisa y sin contradicciones de lluvia
        val isCurrentlyRaining = (currentPrecipitation ?: 0.0) >= 0.2 || currentRainProb >= 75
        val rainRisk = when {
            isCurrentlyRaining -> com.example.data.models.RainRiskLevel.ACTIVE
            maxRainProb > 65 || maxPrecipMm >= 1.5 -> com.example.data.models.RainRiskLevel.HIGH
            maxRainProb in 36..65 || maxPrecipMm >= 0.5 -> com.example.data.models.RainRiskLevel.MODERATE
            maxRainProb in 15..35 -> com.example.data.models.RainRiskLevel.LOW
            maxRainProb > 0 -> com.example.data.models.RainRiskLevel.VERY_LOW
            else -> com.example.data.models.RainRiskLevel.NONE
        }

        // 3. Factor de Calzado coherente con lluvia y suelo
        val footwearPill = when {
            rainRisk == com.example.data.models.RainRiskLevel.ACTIVE || rainRisk == com.example.data.models.RainRiskLevel.HIGH -> DynamicFootwearPill(
                icon = "🥾",
                title = "Calzado impermeable / antideslizante",
                reason = "Lluvia prevista o suelo mojado; suela con buen agarre para pies secos."
            )
            rainRisk == com.example.data.models.RainRiskLevel.MODERATE -> DynamicFootwearPill(
                icon = "👟",
                title = "Calzado cerrado resistente",
                reason = "Posibilidad de lluvia; calzado cómodo y cerrado."
            )
            shadePerceived < 6.0 && minTempPeriod < 14.0 -> DynamicFootwearPill(
                icon = "🥾",
                title = "Calzado térmico / calcetín grueso",
                reason = "Suelo y ambiente frío; suela gruesa aislante para retener el calor."
            )
            currentTemp >= 23.0 && rainRisk == com.example.data.models.RainRiskLevel.NONE -> DynamicFootwearPill(
                icon = "👟",
                title = "Calzado ultra-transpirable",
                reason = "Superficie seca y temperatura favorable; favorece la ventilación natural."
            )
            else -> DynamicFootwearPill(
                icon = "👟",
                title = "Calzado cómodo estándar",
                reason = "Superficie seca y condiciones estables; calzado habitual."
            )
        }

        // 4. Complementos dinámicos (sin sugerencias contradictorias)
        val complementPills = mutableListOf<DynamicComplementPill>()
        val uvPeakHour = lookaheadHours.maxByOrNull { it.uvIndex ?: 0.0 }?.let { extractHour(it) } ?: 14
        if (maxUv >= 5.5 && isDay) {
            val uvText = "UV ${"%.1f".format(Locale.US, maxUv)} a las $uvPeakHour:00"
            complementPills.add(
                DynamicComplementPill(
                    icon = "🕶️",
                    label = "Gafas de sol ($uvText)",
                    isCrucial = maxUv >= 7.0
                )
            )
        }
        // Paraguas: estricto según riesgo real de lluvia
        when (rainRisk) {
            com.example.data.models.RainRiskLevel.ACTIVE,
            com.example.data.models.RainRiskLevel.HIGH -> {
                complementPills.add(
                    DynamicComplementPill(
                        icon = "☂️",
                        label = if (maxPrecipMm >= 2.0) "Paraguas resistente" else "Paraguas o impermeable",
                        isCrucial = true
                    )
                )
            }
            com.example.data.models.RainRiskLevel.MODERATE -> {
                complementPills.add(
                    DynamicComplementPill(
                        icon = "🌂",
                        label = "Paraguas compacto",
                        isCrucial = false
                    )
                )
            }
            com.example.data.models.RainRiskLevel.LOW -> {
                if (preferences.duration == com.example.data.models.OutingDuration.ALL_DAY) {
                    complementPills.add(
                        DynamicComplementPill(
                            icon = "🌂",
                            label = "Paraguas plegable por precaución",
                            isCrucial = false
                        )
                    )
                }
            }
            com.example.data.models.RainRiskLevel.VERY_LOW,
            com.example.data.models.RainRiskLevel.NONE -> {
                // Paraguas no se añade bajo ninguna circunstancia
            }
        }

        if ((maxUv >= 6.5 || currentTemp >= 27.0) && isDay) {
            complementPills.add(
                DynamicComplementPill(
                    icon = "🧢",
                    label = "Gorra o sombrero transpirable"
                )
            )
        }
        if ((shadePerceived < 8.0 || isDampCold) && minTempPeriod < 12.0) {
            complementPills.add(
                DynamicComplementPill(
                    icon = "🧣",
                    label = "Bufanda / braga de cuello",
                    isCrucial = shadePerceived < 4.0
                )
            )
        }

        // 5. Sistema de Capas Modulares Reales (Método Cebolla 3 Capas)
        val peakItem = lookaheadHours.maxByOrNull { it.temp }
        val peakHour = peakItem?.let { extractHour(it) } ?: 14
        val sunsetHourStr = extractSunsetHour(sunsetTime)

        val hasWindRisk = effectiveWindSpeed > 22.0 || maxWind > 24.0 || effectiveGusts > 35.0 || maxGusts > 35.0
        val hasRainRisk = rainRisk == com.example.data.models.RainRiskLevel.HIGH || rainRisk == com.example.data.models.RainRiskLevel.ACTIVE || rainRisk == com.example.data.models.RainRiskLevel.MODERATE

        val layerStrategy = buildModularLayerStrategy(
            currentTemp = currentTemp,
            perceivedTemp = personalizedPerceived,
            minTempPeriod = minTempPeriod,
            thermalOscillation = thermalOscillation,
            isHighOscillation = isHighOscillation,
            peakHour = peakHour,
            isMuggyHeat = isMuggyHeat,
            hasWindRisk = hasWindRisk,
            hasRainRisk = hasRainRisk,
            sunsetHourStr = sunsetHourStr
        )

        // 6. Línea de Tiempo Visual Rápida (4 Hitos Diarios)
        val milestones = buildTimelineMilestones(
            currentTemp = currentTemp,
            lookaheadHours = lookaheadHours,
            minTempPeriod = minTempPeriod,
            layerStrategy = layerStrategy,
            isHighOscillation = isHighOscillation,
            sunsetHourStr = sunsetHourStr,
            hasRainRisk = hasRainRisk
        )

        // 7. Alertas relevantes (Regla: no inventar alertas si no hay nada anormal)
        val complementAlerts = mutableListOf<ComplementAlert>()
        if (rainRisk == com.example.data.models.RainRiskLevel.ACTIVE || rainRisk == com.example.data.models.RainRiskLevel.HIGH) {
            complementAlerts.add(
                ComplementAlert(
                    icon = "☂️",
                    title = if (rainRisk == com.example.data.models.RainRiskLevel.ACTIVE) "Lluvia activa" else "Lluvia probable",
                    description = "Probabilidad del $maxRainProb%. Lleva paraguas o chaqueta impermeable.",
                    isWarning = true
                )
            )
        }
        if (hasWindRisk) {
            val windValue = maxOf(effectiveWindSpeed, maxWind).roundToInt()
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
                    description = "El sudor no se evapora fácilmente; viste ropa holgada y transpirable.",
                    isWarning = false
                )
            )
        }

        // Indicadores físicos para confort térmico
        val physical = indicators ?: BioclimaticMathEngine.calculateIndicators(
            temp = currentTemp,
            humidity = currentHumidity,
            windSpeed = effectiveWindSpeed,
            cloudCover = effectiveCloud,
            uvIndex = effectiveUv,
            isDay = isDay,
            hourlyItems = hourlyItems,
            apparentTemp = currentApparentTemp,
            sunsetTime = sunsetTime,
            windGusts = effectiveGusts
        )

        // Alerta de caída de temperatura tras el ocaso (solo si es real y significativa)
        if (physical.isSunsetColdSweatRisk && minTempPeriod < 16.0) {
            complementAlerts.add(
                ComplementAlert(
                    icon = "⚠️",
                    title = "Caída térmica al anochecer",
                    description = "Descenso notable tras el ocaso (~${physical.sunsetTempDrop.roundToInt()} °C menos). Ten a mano una prenda de respaldo para la vuelta.",
                    isWarning = true
                )
            )
        }

        // 8. Franjas Horarias
        val timeSlots = buildTimeSlotsAdvice(currentTemp, lookaheadHours, minTempPeriod)

        // 9. Titular Práctico y Humano para salir de casa (1-3 frases concisas)
        val headline = generatePracticalHeadline(
            currentTemp = currentTemp.roundToInt(),
            perceivedTemp = personalizedPerceived.roundToInt(),
            minTempPeriod = minTempPeriod,
            maxTempPeriod = maxTempPeriod,
            thermalOscillation = thermalOscillation,
            isHighOscillation = isHighOscillation,
            peakHour = peakHour,
            layerStrategy = layerStrategy,
            rainRisk = rainRisk,
            hasWindRisk = hasWindRisk,
            sunsetHourStr = sunsetHourStr,
            preferences = preferences,
            physical = physical,
            windowStartTemp = windowStartTemp.roundToInt(),
            windowReturnTemp = windowReturnTemp.roundToInt()
        )

        val quickAdvice = buildQuickLeavingAdvice(
            currentTemp = currentTemp.roundToInt(),
            minTempPeriod = minTempPeriod,
            rainRisk = rainRisk,
            hasWindRisk = hasWindRisk
        )

        val metrics = BioclimaticMetrics(
            dryTemp = currentTemp,
            apparentTemp = currentApparentTemp ?: currentTemp,
            bioclimaticPerceivedTemp = personalizedPerceived,
            humidity = currentHumidity,
            windSpeed = effectiveWindSpeed,
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
            minTempPeriod = minTempPeriod,
            dewPoint = physical.dewPoint,
            humidex = physical.humidex,
            urbanWindSpeed = physical.urbanWindSpeed,
            deltaWindChill = physical.deltaWindChill,
            isMandatoryChestProtection = physical.isMandatoryChestProtection,
            isRespiratoryMucosaRisk = physical.isRespiratoryMucosaRisk,
            isHighSweatRisk = physical.isHighSweatRisk,
            solarRadiationWm2 = physical.solarRadiationWm2,
            isSunShadeContrastHigh = physical.isSunShadeContrastHigh,
            isSunsetColdSweatRisk = physical.isSunsetColdSweatRisk,
            requiredClo = physical.requiredClo,
            cloDescription = physical.cloDescription
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
            advancedMetrics = advancedMetrics,
            rainRisk = rainRisk,
            userPreferences = preferences,
            quickLeavingAdvice = quickAdvice
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
     * Titular práctico y conciso para salir de casa (1 a 3 frases claras),
     * respondiendo directamente a "¿Qué me pongo para salir?" según temperatura,
     * duración prevista y evolución térmica.
     */
    private fun generatePracticalHeadline(
        currentTemp: Int,
        perceivedTemp: Int,
        minTempPeriod: Double,
        maxTempPeriod: Double,
        thermalOscillation: Double,
        isHighOscillation: Boolean,
        peakHour: Int,
        layerStrategy: ModularLayerStrategy,
        rainRisk: com.example.data.models.RainRiskLevel,
        hasWindRisk: Boolean,
        sunsetHourStr: String,
        preferences: com.example.data.models.UserPreferences,
        physical: BioclimaticPhysicalIndicators? = null,
        windowStartTemp: Int = currentTemp,
        windowReturnTemp: Int = currentTemp
    ): String {
        val isWindow = preferences.isScheduledWindow && preferences.departureHour != null && preferences.returnHour != null
        val depStr = if (isWindow) "${preferences.departureHour}:00" else ""
        val retStr = if (isWindow) "${preferences.returnHour}:00" else ""

        val effectiveMinT = if (isWindow) minOf(minTempPeriod, minOf(windowStartTemp.toDouble(), windowReturnTemp.toDouble())) else minTempPeriod
        val startT = if (isWindow) windowStartTemp else currentTemp
        val returnT = if (isWindow) windowReturnTemp else currentTemp

        // Regla estricta cálida: si la mínima es >= 18 °C, prohibido mencionar abrigo, jersey o chaqueta
        if (effectiveMinT >= 18.0 || (startT >= 24 && returnT >= 20)) {
            val warmGarment = if (startT >= 28 || perceivedTemp >= 28) {
                "Prendas muy ligeras y transpirables de manga corta."
            } else {
                "Manga corta estándar."
            }

            val warmEvolution = if (isWindow) {
                "Temperatura cálida y agradable ($startT °C a las $depStr, ~$returnT °C a las $retStr)."
            } else if (isHighOscillation && maxTempPeriod > currentTemp + 3) {
                "Ambiente agradable ahora ($currentTemp °C) que irá subiendo hasta unos ${maxTempPeriod.roundToInt()} °C."
            } else {
                "Temperatura cálida y estable en torno a $currentTemp °C."
            }

            val rainNote = when (rainRisk) {
                com.example.data.models.RainRiskLevel.ACTIVE -> " Lleva paraguas o chubasquero fino por lluvia activa."
                com.example.data.models.RainRiskLevel.HIGH -> " Posibilidad alta de chubasco; lleva paraguas o chubasquero fino."
                com.example.data.models.RainRiskLevel.MODERATE -> " Algún chubasco aislado posible; considera llevar paraguas compacto."
                else -> ""
            }

            return "$warmGarment $warmEvolution$rainNote".trim()
        }

        // Recomendación principal de prenda
        val primaryGarment = when {
            effectiveMinT < 7 || startT < 7 -> "🧥 Abrigo o cazadora gruesa con capa base cálida."
            effectiveMinT in 7.0..13.0 || startT in 7..13 -> "🧥 Chaqueta de entretiempo o abrigo ligero."
            returnT <= 14 && startT >= 18 -> "🧥 Chaqueta fina fácil de quitar."
            effectiveMinT in 14.0..18.0 || startT in 14..18 -> "🧥 Chaqueta fina sobre camiseta de manga corta."
            startT in 19..23 -> "👕 Manga corta con una capa ligera de respaldo si refresca."
            else -> "👕 Manga corta transpirable."
        }

        // Evolución térmica según la ventana o duración
        val evolutionText = if (isWindow) {
            when {
                returnT <= startT - 3 -> {
                    "Saldrás con $startT °C ($depStr), pero refrescará hasta unos $returnT °C al regresar ($retStr)."
                }
                maxTempPeriod.roundToInt() >= startT + 3 -> {
                    "Fresco al salir ($startT °C a las $depStr), alcanzando unos ${maxTempPeriod.roundToInt()} °C y quedando en $returnT °C a las $retStr."
                }
                else -> {
                    "Ambiente estable de $depStr a $retStr en torno a $startT °C (regreso a ~$returnT °C)."
                }
            }
        } else {
            when (preferences.duration) {
                com.example.data.models.OutingDuration.SHORT -> {
                    "Fresco ahora ($currentTemp °C); abrígate lo justo para salir."
                }
                com.example.data.models.OutingDuration.MEDIUM -> {
                    if (isHighOscillation && maxTempPeriod > currentTemp + 2) {
                        "Fresco ahora ($currentTemp °C), pero irá calentando durante el día hasta unos ${maxTempPeriod.roundToInt()} °C."
                    } else if (minTempPeriod < currentTemp - 2) {
                        "Sensación fresca de $currentTemp °C que tenderá a bajar ligeramente."
                    } else {
                        "Ambiente fresco y estable en torno a $currentTemp °C durante la salida."
                    }
                }
                com.example.data.models.OutingDuration.ALL_DAY -> {
                    if (minTempPeriod < 15.0 && (physical?.isSunsetColdSweatRisk == true || physical?.nocturnalDropRisk == true)) {
                        "Tarde templada (~${maxTempPeriod.roundToInt()} °C), pero refrescará bastante al anochecer (~${minTempPeriod.roundToInt()} °C); conserva la chaqueta para la vuelta."
                    } else if (isHighOscillation) {
                        "Fresco a primera hora y al anochecer, con mediodía templado (~${maxTempPeriod.roundToInt()} °C)."
                    } else {
                        "Fresco continuado durante la jornada; mantén las capas a mano."
                    }
                }
            }
        }

        // Nota de lluvia (estricta y sin contradicciones)
        val rainText = when (rainRisk) {
            com.example.data.models.RainRiskLevel.ACTIVE -> " Lloviendo ahora; imprescindible paraguas o impermeable."
            com.example.data.models.RainRiskLevel.HIGH -> " Lluvia probable; lleva paraguas o impermeable."
            com.example.data.models.RainRiskLevel.MODERATE -> " Posibilidad de lluvia; considera llevar paraguas si vas a estar fuera."
            com.example.data.models.RainRiskLevel.LOW -> if (preferences.duration == com.example.data.models.OutingDuration.ALL_DAY || isWindow) {
                " Baja probabilidad de lluvia; paraguas plegable por precaución."
            } else {
                " No parece necesario llevar paraguas."
            }
            com.example.data.models.RainRiskLevel.VERY_LOW,
            com.example.data.models.RainRiskLevel.NONE -> ""
        }

        // Nota contextual personalizada
        val personalNote = when {
            preferences.sensitivity == com.example.data.models.ThermalSensitivity.FRIOLERO -> {
                " (Si sueles tener frío, una capa fina adicional te dará mayor confort)."
            }
            preferences.activity == com.example.data.models.ActivityType.CYCLING -> {
                " (En bici el viento aumenta el fresco; cortavientos frontal aconsejado)."
            }
            else -> ""
        }

        return "$primaryGarment $evolutionText$rainText$personalNote".trim()
    }

    /**
     * Sintetiza la tendencia térmica interdiaria solo cuando los datos
     * realmente lo justifiquen (|ΔT| >= 2.5 °C o tendencia progresiva de 3+ días).
     */
    fun calculateTrendSummary(dailyItems: List<com.example.data.models.DailyItem>): String? {
        if (dailyItems.size < 2) return null
        val today = dailyItems[0]
        val tomorrow = dailyItems[1]
        val deltaTomorrow = tomorrow.maxTemp - today.maxTemp

        // Tendencia progresiva en los próximos 3-4 días
        if (dailyItems.size >= 4) {
            val day2 = dailyItems[2]
            val day3 = dailyItems[3]
            val isProgressiveDrop = tomorrow.maxTemp < today.maxTemp &&
                    day2.maxTemp <= tomorrow.maxTemp &&
                    day3.maxTemp <= day2.maxTemp &&
                    (today.maxTemp - day3.maxTemp) >= 4

            if (isProgressiveDrop) {
                return "📉 Descenso progresivo de temperaturas durante los próximos días."
            }

            val isProgressiveRise = tomorrow.maxTemp > today.maxTemp &&
                    day2.maxTemp >= tomorrow.maxTemp &&
                    day3.maxTemp >= day2.maxTemp &&
                    (day3.maxTemp - today.maxTemp) >= 4

            if (isProgressiveRise) {
                return "📈 Ascenso progresivo de temperaturas durante los próximos días."
            }
        }

        // Variación notable entre hoy y mañana
        return when {
            deltaTomorrow <= -3 -> "📉 Mañana refresca unos ${kotlin.math.abs(deltaTomorrow)} °C respecto a hoy."
            deltaTomorrow >= 3 -> "📈 Mañana subirán unos $deltaTomorrow °C respecto a hoy."
            else -> null
        }
    }

    /**
     * Resumen ultracorto para lectura rápida.
     */
    private fun buildQuickLeavingAdvice(
        currentTemp: Int,
        minTempPeriod: Double,
        rainRisk: com.example.data.models.RainRiskLevel,
        hasWindRisk: Boolean
    ): String {
        val garment = when {
            minTempPeriod >= 18.0 || currentTemp >= 24 -> "Manga corta"
            currentTemp < 7 -> "Abrigo grueso"
            currentTemp in 7..13 -> "Chaqueta / Jersey"
            currentTemp in 14..18 -> "Chaqueta fina"
            else -> "Manga corta"
        }
        val icon = when {
            rainRisk == com.example.data.models.RainRiskLevel.ACTIVE || rainRisk == com.example.data.models.RainRiskLevel.HIGH -> "☂️"
            hasWindRisk -> "💨"
            minTempPeriod >= 18.0 -> "☀️"
            currentTemp < 14 -> "🧥"
            else -> "👕"
        }
        return "$icon $garment ($currentTemp °C)"
    }

    /**
     * Procesa la previsión de 7 días 100% en memoria RAM (< 0.1 ms por día)
     * calculando indicadores matemáticos y generando icono de vestimenta,
     * resumen técnico de 3-5 palabras y distintivo de alerta bioclimática.
     */
    fun process7DayForecast(dailyItems: List<com.example.data.models.DailyItem>): List<com.example.data.models.DailyItem> {
        return dailyItems.map { item ->
            val dMax = item.maxTemp.toDouble()
            val dMin = item.minTemp.toDouble()
            val dWind = item.windSpeed.toDouble()
            val dRain = item.rainProb
            val dFeels = item.feelsLikeMax.toDouble()

            // Indicadores calculados en RAM instantáneamente
            val indicators = BioclimaticMathEngine.calculateIndicators(
                temp = dMax,
                humidity = if (dRain > 50) 80 else 60,
                windSpeed = dWind,
                cloudCover = if (dRain > 40) 80 else 30,
                uvIndex = item.uvIndexMax ?: 4.0,
                isDay = true,
                hourlyItems = item.hourlyList,
                apparentTemp = dFeels,
                sunsetTime = item.sunset
            )

            val clothingIcon = when {
                dRain >= 50 || (item.precipitationSum ?: 0.0) >= 2.0 -> "🌧️"
                indicators.isMandatoryChestProtection -> "🛡️"
                indicators.isRespiratoryMucosaRisk -> "🧣"
                indicators.isHighSweatRisk || dMax >= 28.0 -> "🎽"
                dMax >= 20.0 -> "👕"
                dMax >= 15.0 -> "🧥"
                else -> "❄️"
            }

            val technicalSummary = when {
                dRain >= 50 || (item.precipitationSum ?: 0.0) >= 2.0 -> "Cortavientos impermeable / Lluvia"
                indicators.isMandatoryChestProtection -> "Capa transpirable + cortavientos pectoral"
                indicators.isRespiratoryMucosaRisk -> "Protección mucosa / Aire frío"
                indicators.isHighSweatRisk && dMax >= 22.0 -> "Sintético ligero / Alta humedad"
                indicators.isSunsetColdSweatRisk || (dMax - dMin >= 7.0) -> "Multicapa modular / Caída térmica"
                dMax >= 24.0 -> "Sintético ultraligero / Transpirable"
                dMax >= 20.0 -> "Manga corta / Confort térmico"
                dMax >= 15.0 -> "Capa base + Chaqueta fina"
                else -> "Multicapa invernal / Abrigo"
            }

            val bioclimaticAlert = when {
                indicators.isMandatoryChestProtection -> "⚠️ Riesgo Pectoral"
                indicators.isSunsetColdSweatRisk -> "⚠️ Sudor Frío"
                indicators.isRespiratoryMucosaRisk -> "🧣 Cuello Protegido"
                indicators.isHighSweatRisk && dMax >= 22.0 -> "💧 Alta Humedad"
                dRain >= 50 -> "☂️ Lluvia Prevista"
                else -> null
            }

            val dayRec = calculate(
                currentTemp = dMax,
                currentHumidity = if (dRain > 50) 80 else 60,
                currentWindSpeed = dWind,
                currentApparentTemp = dFeels,
                currentRainProb = dRain,
                currentPrecipitation = item.precipitationSum,
                currentUvIndex = item.uvIndexMax,
                isDay = true,
                hourlyItems = item.hourlyList,
                sunsetTime = item.sunset,
                indicators = indicators
            )

            item.copy(
                clothingIcon = clothingIcon,
                technicalSummary = technicalSummary,
                bioclimaticAlert = bioclimaticAlert,
                advice = dayRec.toClothingAdvice()
            )
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
