package com.example.engine

import com.example.data.api.NominatimClient
import com.example.data.api.NominatimMicroclimateResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class UrbanEnvironmentType(val label: String) {
    NARROW_STREET("Calle estrecha / Residencial (Efecto Venturi y sombra de edificios)"),
    WIDE_AVENUE("Avenida ancha / Plaza abierta (Mayor irradiancia solar directa)"),
    PARK_FOREST("Parque / Zona arbolada (Evapotranspiración vegetal -1.5 °C)"),
    WATER_BODY("Ribera / Lago / Masa de agua (+10% Humedad relativa)"),
    STANDARD("Entorno urbano estándar")
}

data class MicroclimateContext(
    val environmentType: UrbanEnvironmentType = UrbanEnvironmentType.STANDARD,
    val summary: String = "Entorno estándar sin corrección microclimática",
    val tempDelta: Double = 0.0,
    val humidityDelta: Int = 0,
    val windFactor: Double = 1.0,
    val solarFactor: Double = 1.0,
    val rawTags: Map<String, String> = emptyMap()
)

data class AdjustedWeatherValues(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val context: MicroclimateContext
)

/**
 * Motor puro de microclima urbano basado en OpenStreetMap (Nominatim).
 * Aplica ajustes de temperatura, humedad y viento antes de alimentar el motor bioclimático.
 */
object MicroclimateAdjuster {

    private const val NOMINATIM_TIMEOUT_MS = 1500L

    /**
     * Resuelve el microclima exacto por GPS (Reverse Geocoding)
     */
    suspend fun resolveGpsMicroclimate(lat: Double, lon: Double): MicroclimateContext = withContext(Dispatchers.IO) {
        val devMetrics = com.example.utils.DevToolsTelemetry.nominatimMetrics.value
        if (devMetrics.isOsmFallbackForced) {
            com.example.utils.DevToolsTelemetry.recordNominatimFallback("GPS_EXACTO", "Simulación de Fallback forzada por DevTools")
            return@withContext MicroclimateContext(summary = "Perfil neutro (Fallback OSM forzado por DevTools)")
        }
        devMetrics.manualOverrideEnvironment?.let { forcedEnv ->
            return@withContext createOverriddenContext(forcedEnv, "GPS_EXACTO (Override Manual)")
        }

        val t0 = System.currentTimeMillis()
        try {
            val response = withTimeoutOrNull(NOMINATIM_TIMEOUT_MS) {
                NominatimClient.api.reverseGeocode(lat, lon)
            }
            val latency = System.currentTimeMillis() - t0

            if (response == null) {
                com.example.utils.DevToolsTelemetry.recordNominatimTimeout("GPS_EXACTO")
                return@withContext MicroclimateContext()
            }

            val ctx = parseResponseToContext(response, isSearch = false)
            val displayName = response.displayName ?: "Lat: $lat, Lon: $lon"
            com.example.utils.DevToolsTelemetry.recordNominatimSuccess(
                latencyMs = latency,
                mode = "GPS_EXACTO",
                placeName = displayName,
                tags = response.extratags ?: emptyMap(),
                envType = ctx.environmentType
            )
            ctx
        } catch (e: Exception) {
            com.example.utils.DevToolsTelemetry.recordNominatimFallback("GPS_EXACTO", e.message ?: "Error desconocido")
            MicroclimateContext()
        }
    }

    /**
     * Resuelve el microclima promedio por búsqueda textual (Forward Geocoding)
     */
    suspend fun resolveSearchMicroclimate(query: String, fallbackLat: Double? = null, fallbackLon: Double? = null): MicroclimateContext = withContext(Dispatchers.IO) {
        val devMetrics = com.example.utils.DevToolsTelemetry.nominatimMetrics.value
        if (devMetrics.isOsmFallbackForced) {
            com.example.utils.DevToolsTelemetry.recordNominatimFallback("BUSQUEDA_ZONA", "Simulación de Fallback forzada por DevTools")
            return@withContext MicroclimateContext(summary = "Perfil neutro (Fallback OSM forzado por DevTools)")
        }
        devMetrics.manualOverrideEnvironment?.let { forcedEnv ->
            return@withContext createOverriddenContext(forcedEnv, "BUSQUEDA_ZONA (Override Manual)")
        }

        val t0 = System.currentTimeMillis()
        try {
            val results = withTimeoutOrNull(NOMINATIM_TIMEOUT_MS) {
                NominatimClient.api.searchLocations(query)
            }
            val latency = System.currentTimeMillis() - t0

            if (results.isNullOrEmpty()) {
                if (fallbackLat != null && fallbackLon != null) {
                    return@withContext resolveGpsMicroclimate(fallbackLat, fallbackLon)
                }
                com.example.utils.DevToolsTelemetry.recordNominatimTimeout("BUSQUEDA_ZONA")
                return@withContext MicroclimateContext()
            }

            val first = results.first()
            val ctx = parseResponseToContext(first, isSearch = true)
            val displayName = first.displayName ?: query
            com.example.utils.DevToolsTelemetry.recordNominatimSuccess(
                latencyMs = latency,
                mode = "BUSQUEDA_ZONA",
                placeName = displayName,
                tags = first.extratags ?: emptyMap(),
                envType = ctx.environmentType
            )
            ctx
        } catch (e: Exception) {
            com.example.utils.DevToolsTelemetry.recordNominatimFallback("BUSQUEDA_ZONA", e.message ?: "Error desconocido")
            MicroclimateContext()
        }
    }

    private fun createOverriddenContext(env: UrbanEnvironmentType, modeName: String): MicroclimateContext {
        val ctx = when (env) {
            UrbanEnvironmentType.WATER_BODY -> MicroclimateContext(
                environmentType = UrbanEnvironmentType.WATER_BODY,
                summary = "Ribera / Lago / Masa de agua (Override Manual: +10% HR)",
                tempDelta = -0.5,
                humidityDelta = 10,
                windFactor = 1.1,
                solarFactor = 1.05,
                rawTags = mapOf("waterway" to "river", "override" to "true")
            )
            UrbanEnvironmentType.PARK_FOREST -> MicroclimateContext(
                environmentType = UrbanEnvironmentType.PARK_FOREST,
                summary = "Parque / Zona arbolada (Override Manual: -1.5 °C evapotranspiración)",
                tempDelta = -1.5,
                humidityDelta = 5,
                windFactor = 0.85,
                solarFactor = 0.80,
                rawTags = mapOf("leisure" to "park", "override" to "true")
            )
            UrbanEnvironmentType.NARROW_STREET -> MicroclimateContext(
                environmentType = UrbanEnvironmentType.NARROW_STREET,
                summary = "Calle estrecha / Residencial (Override Manual: Venturi x1.25)",
                tempDelta = 0.0,
                humidityDelta = 0,
                windFactor = 1.25,
                solarFactor = 0.85,
                rawTags = mapOf("highway" to "residential", "override" to "true")
            )
            UrbanEnvironmentType.WIDE_AVENUE -> MicroclimateContext(
                environmentType = UrbanEnvironmentType.WIDE_AVENUE,
                summary = "Avenida ancha / Plaza abierta (Override Manual: +0.8 °C y sol directo)",
                tempDelta = +0.8,
                humidityDelta = -2,
                windFactor = 1.10,
                solarFactor = 1.20,
                rawTags = mapOf("highway" to "primary", "override" to "true")
            )
            UrbanEnvironmentType.STANDARD -> MicroclimateContext(
                environmentType = UrbanEnvironmentType.STANDARD,
                summary = "Entorno urbano estándar (Override Manual)",
                tempDelta = 0.0,
                humidityDelta = 0,
                windFactor = 1.0,
                solarFactor = 1.0,
                rawTags = mapOf("override" to "standard")
            )
        }
        com.example.utils.DevToolsTelemetry.recordNominatimSuccess(
            latencyMs = 0L,
            mode = modeName,
            placeName = "Override Manual DevTools (${env.name})",
            tags = ctx.rawTags,
            envType = env
        )
        return ctx
    }

    /**
     * Ajusta los valores crudos de Open-Meteo aplicando el contexto microclimático.
     */
    fun applyAdjustment(
        rawTemp: Double,
        rawHumidity: Int,
        rawWindSpeed: Double,
        context: MicroclimateContext
    ): AdjustedWeatherValues {
        val finalTemp = rawTemp + context.tempDelta
        val finalHumidity = (rawHumidity + context.humidityDelta).coerceIn(0, 100)
        val finalWind = (rawWindSpeed * context.windFactor).coerceAtLeast(0.0)

        com.example.utils.DevToolsTelemetry.recordMicroclimateAdjustment(
            rawTemp = rawTemp,
            adjTemp = finalTemp,
            rawHum = rawHumidity,
            adjHum = finalHumidity,
            rawWind = rawWindSpeed,
            adjWind = finalWind,
            context = context
        )

        return AdjustedWeatherValues(
            temperature = finalTemp,
            humidity = finalHumidity,
            windSpeed = finalWind,
            context = context
        )
    }

    private fun parseResponseToContext(
        item: NominatimMicroclimateResponse,
        isSearch: Boolean
    ): MicroclimateContext {
        val extra = item.extratags ?: emptyMap()
        val placeClass = item.placeClass?.lowercase() ?: ""
        val type = item.type?.lowercase() ?: ""

        val highway = extra["highway"]?.lowercase() ?: (if (placeClass == "highway") type else null)
        val leisure = extra["leisure"]?.lowercase() ?: (if (placeClass == "leisure") type else null)
        val waterway = extra["waterway"]?.lowercase() ?: (if (placeClass == "waterway") type else null)
        val natural = extra["natural"]?.lowercase() ?: (if (placeClass == "natural") type else null)
        val landuse = extra["landuse"]?.lowercase() ?: (if (placeClass == "landuse") type else null)
        val water = extra["water"]?.lowercase() ?: (if (placeClass == "water") type else null)

        val isWater = waterway != null ||
                water != null ||
                natural in listOf("water", "bay", "coastline", "beach", "wetland") ||
                type in listOf("river", "canal", "stream", "lake", "water", "reservoir", "coastline", "dock")

        val isPark = leisure in listOf("park", "garden", "nature_reserve", "pitch", "playground", "golf_course", "recreation_ground") ||
                landuse in listOf("forest", "grass", "meadow", "recreation_ground", "orchard", "village_green", "allotments", "cemetery") ||
                natural in listOf("wood", "tree_row", "scrub", "heath", "grassland") ||
                type in listOf("park", "forest", "wood", "garden")

        val isNarrowStreet = highway in listOf("residential", "living_street", "service", "pedestrian", "footway", "alley", "path", "cycleway", "steps", "track") ||
                placeClass == "building" ||
                type in listOf("residential", "house", "apartments", "terrace", "pedestrian")

        val isWideAvenue = highway in listOf("primary", "secondary", "tertiary", "trunk", "motorway", "unclassified") ||
                type in listOf("square", "plaza") ||
                extra["place"] in listOf("square", "plaza") ||
                leisure == "square"

        return when {
            isWater -> {
                val humDelta = if (isSearch) 6 else 10
                MicroclimateContext(
                    environmentType = UrbanEnvironmentType.WATER_BODY,
                    summary = "Junto a masa de agua / río (Humedad relativa base +$humDelta%)",
                    tempDelta = if (isSearch) -0.3 else -0.5,
                    humidityDelta = humDelta,
                    windFactor = 1.1,
                    solarFactor = 1.05,
                    rawTags = extra
                )
            }
            isPark -> {
                val tempD = if (isSearch) -0.8 else -1.5
                MicroclimateContext(
                    environmentType = UrbanEnvironmentType.PARK_FOREST,
                    summary = "Zona verde / parque (Descuento térmico por evapotranspiración $tempD °C)",
                    tempDelta = tempD,
                    humidityDelta = if (isSearch) 3 else 5,
                    windFactor = 0.85,
                    solarFactor = 0.80,
                    rawTags = extra
                )
            }
            isNarrowStreet -> {
                MicroclimateContext(
                    environmentType = UrbanEnvironmentType.NARROW_STREET,
                    summary = "Calle estrecha / residencial (Efecto Venturi en viento y sombra de edificios)",
                    tempDelta = 0.0,
                    humidityDelta = 0,
                    windFactor = if (isSearch) 1.15 else 1.25,
                    solarFactor = 0.85,
                    rawTags = extra
                )
            }
            isWideAvenue -> {
                MicroclimateContext(
                    environmentType = UrbanEnvironmentType.WIDE_AVENUE,
                    summary = "Avenida ancha / plaza despejada (Mayor irradiancia solar directa)",
                    tempDelta = if (isSearch) +0.4 else +0.8,
                    humidityDelta = -2,
                    windFactor = 1.10,
                    solarFactor = 1.20,
                    rawTags = extra
                )
            }
            else -> {
                MicroclimateContext(
                    environmentType = UrbanEnvironmentType.STANDARD,
                    summary = "Entorno urbano estándar",
                    tempDelta = 0.0,
                    humidityDelta = 0,
                    windFactor = 1.0,
                    solarFactor = 1.0,
                    rawTags = extra
                )
            }
        }
    }
}
