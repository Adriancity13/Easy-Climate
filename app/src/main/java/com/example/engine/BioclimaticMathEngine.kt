package com.example.engine

import com.example.data.models.HourlyItem
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

/**
 * Indicadores físicos, matemáticos e higrotérmicos calculados en memoria RAM (< 0.1 ms)
 * a partir de datos meteorológicos brutos de Open-Meteo.
 */
data class BioclimaticPhysicalIndicators(
    val vaporPressureHpa: Double,
    val humidex: Double,
    val isSweatEvaporationHard: Boolean,
    val windChill: Double,
    val isWindChillActive: Boolean,
    val dewPoint: Double,
    val isStickyAtmosphere: Boolean,
    val isExtremelyDryAir: Boolean,
    val isHighSweatRisk: Boolean,           // Td >= 16°C o Humedad > 70% (Fuerza sintéticos / prohíbe algodón)
    val urbanWindSpeed: Double,             // Viento base * 1.2 (Efecto Venturi cañón urbano)
    val urbanWindChill: Double,             // Sensación eólica con viento urbano corregido
    val deltaWindChill: Double,             // T_real - WindChill_urbano (Pérdida de calor en tórax)
    val isMandatoryChestProtection: Boolean, // Delta T >= 3°C con viento corregido >= 12 km/h
    val isRespiratoryMucosaRisk: Boolean,   // T <= 12°C y (RH < 40% o ráfagas >= 20 km/h) -> Braga cuello
    val solarRadiationWm2: Double,          // Radiación solar directa estimada en W/m2
    val solarGainTemp: Double,              // Ganancia térmica al sol (+2°C a +4°C si >= 500 W/m2)
    val isSunShadeContrastHigh: Boolean,    // Radiación >= 600 W/m2 -> Prenda modular con cremallera
    val sunsetTempDrop: Double,             // Caída térmica post-ocaso
    val isSunsetColdSweatRisk: Boolean,     // Caída > 5°C o (T - Td <= 2°C) -> Riesgo sudor frío
    val requiredClo: Double,                // Nivel de aislamiento térmico requerido (0.3 a 1.3+ CLO)
    val cloDescription: String,             // Descripción técnica del nivel CLO
    val solarBoostSun: Double,
    val sunPerceivedTemp: Double,
    val shadePerceivedTemp: Double,
    val thermalOscillation: Double,
    val minTempPeriod: Double,
    val maxTempPeriod: Double,
    val nocturnalDropRisk: Boolean
)

/**
 * Módulo Matemático Bioclimático Local (Kotlin Math Engine).
 * Calcula en memoria RAM de forma ultra-rápida (< 0.1 ms) los indicadores científicos avanzados:
 * 1. Presión de Vapor, Humidex y Tasa de Evaporación (Tetens / Magnus).
 * 2. Convección Urbana (Venturi x1.2) y Pérdida Térmica Pectoral (Wind Chill Delta).
 * 3. Protección de Mucosa Respiratoria (Aire Frío/Seco).
 * 4. Radiación Solar Directa (W/m2), Ganancia Térmica y Contraste Sol/Sombra.
 * 5. Caída Térmica de Ocaso (Sunset Drop) y Riesgo de Sudor Frío por Punto de Rocío.
 * 6. Nivel Óptimo de Aislamiento Térmico (Unidades CLO).
 */
object BioclimaticMathEngine {

    /**
     * 1. Cálculo de presión de vapor e (hPa) mediante fórmula de Magnus-Tetens.
     * e = 6.112 * exp((17.67 * T) / (T + 243.5)) * (RH / 100)
     */
    fun calculateVaporPressureHpa(tempC: Double, relativeHumidity: Double): Double {
        val clampedRh = relativeHumidity.coerceIn(0.0, 100.0)
        return 6.112 * exp((17.67 * tempC) / (tempC + 243.5)) * (clampedRh / 100.0)
    }

    /**
     * Cálculo del índice de humedad Humidex canadiense:
     * Humidex = T + (5/9) * (e - 10)
     */
    fun calculateHumidex(tempC: Double, relativeHumidity: Double): Double {
        val e = calculateVaporPressureHpa(tempC, relativeHumidity)
        return tempC + (5.0 / 9.0) * (e - 10.0)
    }

    /**
     * 2. Enfriamiento por Viento (Wind Chill - fórmula JAG/Siple-Passel).
     * Aplicable cuando T <= 15 °C y velocidad de viento >= 4.8 km/h.
     */
    fun calculateWindChill(tempC: Double, windSpeedKmH: Double): Double {
        if (tempC > 15.0 || windSpeedKmH < 4.8) return tempC
        val vExp = windSpeedKmH.pow(0.16)
        val wc = 13.12 + 0.6215 * tempC - 11.37 * vExp + 0.3965 * tempC * vExp
        return minOf(tempC, wc)
    }

    /**
     * 3. Punto de Rocío (Dew Point) mediante Magnus-Tetens:
     * alpha = (17.27 * T) / (237.7 + T) + ln(RH / 100)
     * Td = (237.7 * alpha) / (17.27 - alpha)
     */
    fun calculateDewPoint(tempC: Double, relativeHumidity: Double): Double {
        val a = 17.27
        val b = 237.7
        val clampedRh = max(1.0, relativeHumidity.coerceIn(0.0, 100.0))
        val alpha = (a * tempC) / (b + tempC) + ln(clampedRh / 100.0)
        return (b * alpha) / (a - alpha)
    }

    /**
     * 4. Radiación Solar Directa estimada (W/m2) y Ganancia Térmica al Sol Directo vs Sombra.
     */
    fun calculateSolarRadiationWm2(
        isDay: Boolean,
        cloudCoverPercent: Int,
        uvIndex: Double
    ): Double {
        if (!isDay) return 0.0
        val clearSkyFactor = (100 - cloudCoverPercent.coerceIn(0, 100)) / 100.0
        val uvFactor = uvIndex.coerceIn(0.5, 11.0) / 10.0
        return (900.0 * clearSkyFactor * uvFactor).coerceIn(0.0, 1000.0)
    }

    /**
     * Ganancia térmica (°C) por radiación directa.
     * Si radiación >= 500 W/m2, suma entre +2.0 °C y +4.0 °C en horas centrales.
     */
    fun calculateSolarGainTemp(radiationWm2: Double): Double {
        if (radiationWm2 < 300.0) return 0.0
        return when {
            radiationWm2 >= 750.0 -> 4.0
            radiationWm2 >= 600.0 -> 3.5
            radiationWm2 >= 500.0 -> 2.5
            else -> 1.5
        }
    }

    /**
     * 6. Cálculo de Aislamiento Térmico Requerido en Unidades CLO.
     */
    fun calculateRequiredClo(perceivedTemp: Double): Pair<Double, String> {
        return when {
            perceivedTemp >= 28.0 -> 0.3 to "0.3 CLO (Ultraligero / Transpirable)"
            perceivedTemp >= 24.0 -> 0.4 to "0.4 CLO (Manga corta estándar)"
            perceivedTemp >= 20.0 -> 0.5 to "0.5 CLO (Manga corta / Confort suave)"
            perceivedTemp >= 15.0 -> 0.7 to "0.7 CLO (Capa base + Cortavientos fino)"
            perceivedTemp >= 10.0 -> 0.9 to "0.9 CLO (Multicapa 2 capas: térmica + cortavientos)"
            perceivedTemp >= 5.0  -> 1.1 to "1.1 CLO (Multicapa 3 capas: térmica + polar + cazadora)"
            else                  -> 1.3 to "1.3+ CLO (Aislamiento térmico invernal completo)"
        }
    }

    /**
     * Evaluación completa y ultra-precisa (< 0.1 ms en RAM) de todos los indicadores bioclimáticos.
     */
    fun calculateIndicators(
        temp: Double,
        humidity: Int,
        windSpeed: Double,
        cloudCover: Int,
        uvIndex: Double,
        isDay: Boolean,
        hourlyItems: List<HourlyItem>,
        apparentTemp: Double? = null,
        sunsetTime: String? = null,
        windGusts: Double? = null
    ): BioclimaticPhysicalIndicators {
        val rh = humidity.toDouble()
        val gusts = windGusts ?: (windSpeed * 1.3)

        // 1. Evaporación, Punto de Rocío y Tasa de Sudoración
        val vp = calculateVaporPressureHpa(temp, rh)
        val humidex = calculateHumidex(temp, rh)
        val dewPoint = calculateDewPoint(temp, rh)
        val isHighSweat = dewPoint >= 16.0 || rh > 70.0
        val isSticky = dewPoint >= 20.0
        val isExtremelyDry = dewPoint < 5.0
        val sweatHard = temp >= 20.0 && (dewPoint >= 16.0 || rh >= 65.0 || humidex >= 27.0)

        // 2. Convección Urbana (Efecto Venturi x1.2) y Pérdida Pectoral
        val urbanWind = windSpeed * 1.2
        val urbanWindChill = calculateWindChill(temp, urbanWind)
        val baseWindChill = calculateWindChill(temp, windSpeed)
        val deltaWind = maxOf(0.0, temp - urbanWindChill)
        val isChestProtection = deltaWind >= 3.0 && urbanWind >= 12.0
        val isWindChillActive = (temp <= 15.0 && windSpeed >= 4.8) || isChestProtection

        // 3. Protección de Mucosa Respiratoria (Aire Frío/Seco)
        val isMucosaRisk = temp <= 12.0 && (rh < 40.0 || gusts >= 20.0 || urbanWind >= 18.0)

        // 4. Radiación Solar Directa y Contraste Sol/Sombra
        val radiationWm2 = calculateSolarRadiationWm2(isDay, cloudCover, uvIndex)
        val solarGain = calculateSolarGainTemp(radiationWm2)
        val isSunShadeContrastHigh = radiationWm2 >= 600.0 || solarGain >= 3.5

        val basePerceived = apparentTemp ?: (if (isWindChillActive) urbanWindChill else if (temp >= 20.0) humidex else temp)
        val sunPerceived = basePerceived + solarGain

        // 5. Oscilación Térmica y 'Sunset Drop' (Caída Térmica + Riesgo Sudor Frío)
        val lookahead = hourlyItems.take(18)
        val minTemp = if (lookahead.isNotEmpty()) minOf(temp, lookahead.minOf { it.temp.toDouble() }) else temp
        val maxTemp = if (lookahead.isNotEmpty()) maxOf(temp, lookahead.maxOf { it.temp.toDouble() }) else temp
        val osc = maxTemp - minTemp

        val sunsetHour = sunsetTime?.split(":")?.firstOrNull()?.toIntOrNull() ?: 20
        val nightItems = lookahead.filter { h ->
            val hInt = h.label.split(":").firstOrNull()?.toIntOrNull() ?: -1
            hInt >= sunsetHour
        }
        val sunsetTempDrop = if (nightItems.isNotEmpty()) {
            val dayMax = lookahead.filter { h ->
                val hInt = h.label.split(":").firstOrNull()?.toIntOrNull() ?: -1
                hInt in 12..sunsetHour
            }.maxOfOrNull { it.temp.toDouble() } ?: maxTemp
            val nightMin = nightItems.minOf { it.temp.toDouble() }
            maxOf(0.0, dayMax - nightMin)
        } else {
            0.0
        }

        // Riesgo de sudor frío: caída > 5°C o cercanía al punto de rocío (T - Td <= 2°C)
        val isSunsetColdSweatRisk = sunsetTempDrop > 5.0 || ((temp - dewPoint) <= 2.0 && sunsetTempDrop >= 3.0)
        val nocturnalDropRisk = isSunsetColdSweatRisk || sunsetTempDrop >= 4.5 || (osc >= 7.0 && minTemp < 16.0)

        // 6. Cálculo CLO
        val (cloValue, cloDesc) = calculateRequiredClo(sunPerceived)

        return BioclimaticPhysicalIndicators(
            vaporPressureHpa = vp,
            humidex = humidex,
            isSweatEvaporationHard = sweatHard,
            windChill = baseWindChill,
            isWindChillActive = isWindChillActive,
            dewPoint = dewPoint,
            isStickyAtmosphere = isSticky,
            isExtremelyDryAir = isExtremelyDry,
            isHighSweatRisk = isHighSweat,
            urbanWindSpeed = urbanWind,
            urbanWindChill = urbanWindChill,
            deltaWindChill = deltaWind,
            isMandatoryChestProtection = isChestProtection,
            isRespiratoryMucosaRisk = isMucosaRisk,
            solarRadiationWm2 = radiationWm2,
            solarGainTemp = solarGain,
            isSunShadeContrastHigh = isSunShadeContrastHigh,
            sunsetTempDrop = sunsetTempDrop,
            isSunsetColdSweatRisk = isSunsetColdSweatRisk,
            requiredClo = cloValue,
            cloDescription = cloDesc,
            solarBoostSun = solarGain,
            sunPerceivedTemp = sunPerceived,
            shadePerceivedTemp = basePerceived,
            thermalOscillation = osc,
            minTempPeriod = minTemp,
            maxTempPeriod = maxTemp,
            nocturnalDropRisk = nocturnalDropRisk
        )
    }
}

