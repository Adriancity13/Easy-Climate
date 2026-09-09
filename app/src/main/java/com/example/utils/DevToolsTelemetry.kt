package com.example.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class DevLogEntry(
    val timestamp: String,
    val tag: String,
    val message: String,
    val isError: Boolean = false
)

data class NetworkMetrics(
    val lastOpenMeteoLatencyMs: Long = 0L,
    val lastGroqLatencyMs: Long = 0L,
    val groqTokensPerSecond: Double = 0.0,
    val totalTokensConsumed: Int = 0,
    val lastGroqPromptJson: String = "",
    val lastGroqRawResponse: String = "",
    val groqCallsCount: Int = 0,
    val fallbackCount: Int = 0
)

data class NominatimMetrics(
    val lastNominatimLatencyMs: Long = 0L,
    val httpStatus: String = "IDLE",
    val locationMode: String = "GPS_EXACTO",
    val detectedPlaceName: String = "Ninguno",
    val activeOsmTags: Map<String, String> = emptyMap(),
    val assignedEnvironmentType: com.example.engine.UrbanEnvironmentType = com.example.engine.UrbanEnvironmentType.STANDARD,
    val rawTempBase: Double = 0.0,
    val adjustedTemp: Double = 0.0,
    val rawHumidityBase: Int = 0,
    val adjustedHumidity: Int = 0,
    val rawWindBase: Double = 0.0,
    val adjustedWind: Double = 0.0,
    val tempDelta: Double = 0.0,
    val humidityDelta: Int = 0,
    val windFactor: Double = 1.0,
    val solarFactor: Double = 1.0,
    val isOsmFallbackForced: Boolean = false,
    val manualOverrideEnvironment: com.example.engine.UrbanEnvironmentType? = null
)

data class CacheMetrics(
    val lastRefreshTimeMs: Long = System.currentTimeMillis(),
    val cachedLat: Double? = null,
    val cachedLon: Double? = null,
    val cachedCityName: String? = null,
    val currentGpsLat: Double? = null,
    val currentGpsLon: Double? = null,
    val distanceKm: Double = 0.0,
    val cachedTempC: Double? = null,
    val currentTempC: Double? = null,
    val deltaTempC: Double = 0.0,
    val isSimulatedOffline: Boolean = false
)

data class ClimateSimulationConfig(
    val isActive: Boolean = false,
    val tempC: Double = 22.0,
    val humidity: Int = 55,
    val windSpeedKmH: Double = 14.0,
    val cloudCover: Int = 40,
    val weatherCode: Int = 0,
    val isDay: Boolean = true,
    val simulatedHour: Int = 14,
    val cityName: String = "Madrid (Sandbox)"
)

object DevToolsTelemetry {

    private val _networkMetrics = MutableStateFlow(NetworkMetrics())
    val networkMetrics: StateFlow<NetworkMetrics> = _networkMetrics.asStateFlow()

    private val _nominatimMetrics = MutableStateFlow(NominatimMetrics())
    val nominatimMetrics: StateFlow<NominatimMetrics> = _nominatimMetrics.asStateFlow()

    private val _cacheMetrics = MutableStateFlow(CacheMetrics())
    val cacheMetrics: StateFlow<CacheMetrics> = _cacheMetrics.asStateFlow()

    private val _simulationConfig = MutableStateFlow(ClimateSimulationConfig())
    val simulationConfig: StateFlow<ClimateSimulationConfig> = _simulationConfig.asStateFlow()

    private val _logs = MutableStateFlow<List<DevLogEntry>>(emptyList())
    val logs: StateFlow<List<DevLogEntry>> = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun log(tag: String, message: String, isError: Boolean = false) {
        val entry = DevLogEntry(
            timestamp = timeFormat.format(Date()),
            tag = tag,
            message = message,
            isError = isError
        )
        val current = _logs.value.toMutableList()
        if (current.size >= 100) {
            current.removeAt(0)
        }
        current.add(entry)
        _logs.value = current
    }

    fun recordOpenMeteoLatency(latencyMs: Long) {
        _networkMetrics.value = _networkMetrics.value.copy(
            lastOpenMeteoLatencyMs = latencyMs
        )
        log("OpenMeteo", "Respuesta recibida en ${latencyMs}ms")
    }

    fun recordGroqPrompt(promptJson: String) {
        _networkMetrics.value = _networkMetrics.value.copy(
            lastGroqPromptJson = promptJson
        )
    }

    fun recordGroqSuccess(latencyMs: Long, tokensPerSec: Double, newTokens: Int, rawResponse: String) {
        val prev = _networkMetrics.value
        val totalTok = prev.totalTokensConsumed + newTokens
        _networkMetrics.value = prev.copy(
            lastGroqLatencyMs = latencyMs,
            groqTokensPerSecond = tokensPerSec,
            totalTokensConsumed = totalTok,
            lastGroqRawResponse = rawResponse,
            groqCallsCount = prev.groqCallsCount + 1
        )
        log("GroqCloud", "Inferencia completada en ${latencyMs}ms (${String.format(Locale.US, "%.1f", tokensPerSec)} tok/s, +$newTokens tokens)")
    }

    fun recordGroqError(errorMsg: String) {
        val prev = _networkMetrics.value
        _networkMetrics.value = prev.copy(
            fallbackCount = prev.fallbackCount + 1
        )
        log("GroqCloud", "Fallo de API: $errorMsg -> Fallback activado", isError = true)
    }

    fun recordFallback(reason: String) {
        val prev = _networkMetrics.value
        _networkMetrics.value = prev.copy(
            fallbackCount = prev.fallbackCount + 1
        )
        log("EngineFallback", "Fallback ejecutado: $reason", isError = false)
    }

    fun updateCacheMetrics(
        lastRefreshMs: Long,
        cachedLat: Double?,
        cachedLon: Double?,
        cachedName: String?,
        gpsLat: Double?,
        gpsLon: Double?,
        cachedTemp: Double?,
        currentTemp: Double?
    ) {
        val distance = if (cachedLat != null && cachedLon != null && gpsLat != null && gpsLon != null) {
            calculateHaversineDistance(cachedLat, cachedLon, gpsLat, gpsLon)
        } else {
            0.0
        }
        val deltaT = if (cachedTemp != null && currentTemp != null) {
            kotlin.math.abs(cachedTemp - currentTemp)
        } else {
            0.0
        }

        _cacheMetrics.value = _cacheMetrics.value.copy(
            lastRefreshTimeMs = lastRefreshMs,
            cachedLat = cachedLat,
            cachedLon = cachedLon,
            cachedCityName = cachedName,
            currentGpsLat = gpsLat,
            currentGpsLon = gpsLon,
            distanceKm = distance,
            cachedTempC = cachedTemp,
            currentTempC = currentTemp,
            deltaTempC = deltaT
        )
    }

    fun setOsmFallbackForced(forced: Boolean) {
        _nominatimMetrics.value = _nominatimMetrics.value.copy(
            isOsmFallbackForced = forced,
            httpStatus = if (forced) "FALLBACK_FORZADO" else "IDLE"
        )
        log("Nominatim", if (forced) "Simulación de Fallback OSM ACTIVADA (Perfil Neutro)" else "Fallback OSM Desactivado (Nominatim Real)")
    }

    fun setManualOverrideEnvironment(env: com.example.engine.UrbanEnvironmentType?) {
        _nominatimMetrics.value = _nominatimMetrics.value.copy(
            manualOverrideEnvironment = env,
            locationMode = if (env != null) "OVERRIDE_MANUAL" else "GPS_EXACTO"
        )
        log("Microclima", if (env != null) "Microclima Forzado: ${env.label}" else "Microclima Manual Restablecido a Detección Real")
    }

    fun recordNominatimSuccess(
        latencyMs: Long,
        mode: String,
        placeName: String,
        tags: Map<String, String>,
        envType: com.example.engine.UrbanEnvironmentType
    ) {
        _nominatimMetrics.value = _nominatimMetrics.value.copy(
            lastNominatimLatencyMs = latencyMs,
            httpStatus = "EXITOSA (200 OK)",
            locationMode = mode,
            detectedPlaceName = placeName,
            activeOsmTags = tags,
            assignedEnvironmentType = envType
        )
        log("Nominatim", "Geocodificación OSM exitosa en ${latencyMs}ms ($mode) -> $placeName [${envType.name}]")
    }

    fun recordNominatimTimeout(mode: String) {
        _nominatimMetrics.value = _nominatimMetrics.value.copy(
            httpStatus = "TIMEOUT (> 1.5s)",
            locationMode = mode
        )
        log("Nominatim", "Timeout de 1.5s excedido en Nominatim ($mode) -> Fallback neutro", isError = true)
    }

    fun recordNominatimFallback(mode: String, reason: String) {
        _nominatimMetrics.value = _nominatimMetrics.value.copy(
            httpStatus = "FALLBACK DEFAULT",
            locationMode = mode
        )
        log("Nominatim", "Fallback activado en Nominatim ($mode): $reason")
    }

    fun recordMicroclimateAdjustment(
        rawTemp: Double,
        adjTemp: Double,
        rawHum: Int,
        adjHum: Int,
        rawWind: Double,
        adjWind: Double,
        context: com.example.engine.MicroclimateContext
    ) {
        _nominatimMetrics.value = _nominatimMetrics.value.copy(
            rawTempBase = rawTemp,
            adjustedTemp = adjTemp,
            rawHumidityBase = rawHum,
            adjustedHumidity = adjHum,
            rawWindBase = rawWind,
            adjustedWind = adjWind,
            tempDelta = context.tempDelta,
            humidityDelta = context.humidityDelta,
            windFactor = context.windFactor,
            solarFactor = context.solarFactor,
            assignedEnvironmentType = context.environmentType
        )
        log("Microclima", "Ajuste aplicado: T: ${String.format(Locale.US, "%.1f", rawTemp)}°C -> ${String.format(Locale.US, "%.1f", adjTemp)}°C (${if (context.tempDelta >= 0) "+" else ""}${String.format(Locale.US, "%.1f", context.tempDelta)}°C), HR: $rawHum% -> $adjHum% (${if (context.humidityDelta >= 0) "+" else ""}${context.humidityDelta}%), Viento: ${String.format(Locale.US, "%.1f", rawWind)} -> ${String.format(Locale.US, "%.1f", adjWind)} km/h (x${context.windFactor})")
    }

    fun setSimulatedOffline(isOffline: Boolean) {
        _cacheMetrics.value = _cacheMetrics.value.copy(isSimulatedOffline = isOffline)
        log("Network", if (isOffline) "Simulación de Modo Offline ACTIVADA" else "Modo Offline DESACTIVADO")
    }

    fun setSimulation(config: ClimateSimulationConfig) {
        _simulationConfig.value = config
        log("Sandbox", "Escenario simulado aplicado: ${config.tempC}°C, ${config.humidity}% HR, ${config.windSpeedKmH} km/h, WMO ${config.weatherCode}")
    }

    fun clearSimulation() {
        _simulationConfig.value = ClimateSimulationConfig(isActive = false)
        log("Sandbox", "Escenario simulado restablecido. Retornando a telemetría real.")
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun exportAllLogs(): String {
        return buildString {
            append("=== EASY-CLIMATE DEVTOOLS DIAGNOSTICS LOG ===\n")
            append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
            append("Network Metrics:\n")
            append("  Open-Meteo Latency: ${_networkMetrics.value.lastOpenMeteoLatencyMs} ms\n")
            append("  Groq Cloud Latency: ${_networkMetrics.value.lastGroqLatencyMs} ms\n")
            append("  Groq Tokens/s: ${String.format(Locale.US, "%.1f", _networkMetrics.value.groqTokensPerSecond)}\n")
            append("  Total Tokens: ${_networkMetrics.value.totalTokensConsumed}\n")
            append("  Fallbacks to Local Engine: ${_networkMetrics.value.fallbackCount}\n")
            append("\nDouble-Barrier Cache Metrics:\n")
            append("  Distance GPS vs Cache: ${String.format(Locale.US, "%.3f", _cacheMetrics.value.distanceKm)} km\n")
            append("  Delta T: ${String.format(Locale.US, "%.1f", _cacheMetrics.value.deltaTempC)} °C\n")
            append("  Offline Simulated: ${_cacheMetrics.value.isSimulatedOffline}\n")
            append("\nRecent System Logs:\n")
            for (entry in _logs.value) {
                append("[${entry.timestamp}] [${entry.tag}] ${if (entry.isError) "ERROR: " else ""}${entry.message}\n")
            }
        }
    }

    /**
     * Fórmula Haversine para calcular distancia en kilómetros entre dos coordenadas GPS.
     */
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
