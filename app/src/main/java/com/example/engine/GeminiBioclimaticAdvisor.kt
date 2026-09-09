package com.example.engine

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiBioclimaticResult(
    val titular: String,
    val calzado: String,
    val franjas: GeminiFranjas
)

data class GeminiFranjas(
    val salida: String,
    val mediodia: String,
    val tarde: String,
    val regreso: String
)

/**
 * Cliente de integración con la IA Bioclimática Gemini en la nube.
 * Formulado bajo directrices estrictas de perfil fisiológico (asma, transpiración, sudoración rápida).
 */
object GeminiBioclimaticAdvisor {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(750, TimeUnit.MILLISECONDS)
        .readTimeout(750, TimeUnit.MILLISECONDS)
        .writeTimeout(750, TimeUnit.MILLISECONDS)
        .callTimeout(800, TimeUnit.MILLISECONDS)
        .build()

    private const val SYSTEM_PROMPT = """Eres el asesor personal bioclimático de Easy-Climate. Evalúas confort térmico, sudoración y prevención de cambios bruscos de temperatura para un usuario con propensión alta a sudar con esfuerzo y con sensibilidad asmática/respiratoria.
Reglas de Salud y Estilo:
- Aconseja telas transpirables y capas de ajuste rápido para evitar acumulación de humedad corporal.
- Advierte de caídas de temperatura que puedan enfriar el sudor sobre la piel y afectar a las vías respiratorias.
- Restricción de Calor: Si la temperatura mínima del día es >= 18 °C, está estrictamente prohibido sugerir abrigos, jerseys o chaquetas gruesas.
- Noches Cálidas: Si tras el ocaso la temperatura se mantiene en >= 21 °C, mantener recomendación de ropa fresca de verano.
- Formato de Salida (JSON estricto): Responde EXCLUSIVAMENTE con un objeto JSON válido con las claves "titular", "calzado" y "franjas" ("salida", "mediodia", "tarde", "regreso"). Sin markdown ni texto adicional."""

    suspend fun getBioclimaticRecommendation(
        cityName: String,
        temp: Double,
        apparentTemp: Double,
        humidity: Int,
        windSpeed: Double,
        indicators: BioclimaticPhysicalIndicators,
        sunsetTime: String?
    ): GeminiBioclimaticResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val userPrompt = buildString {
            append("Ciudad: $cityName. ")
            append("Temperatura actual: ${temp.toInt()} °C, Sensación: ${apparentTemp.toInt()} °C. ")
            append("Humedad relativa: $humidity%, Viento: ${windSpeed.toInt()} km/h. ")
            append("Humidex: ${indicators.humidex.toInt()}, Presión de vapor: ${String.format("%.1f", indicators.vaporPressureHpa)} hPa. ")
            append("Punto de rocío: ${indicators.dewPoint.toInt()} °C (${if (indicators.isStickyAtmosphere) "bochornoso/pegajoso" else if (indicators.isExtremelyDryAir) "aire muy seco" else "equilibrado"}). ")
            append("Inercia radiación solar: +${String.format("%.1f", indicators.solarBoostSun)} °C. ")
            append("Rango diario: mín ${indicators.minTempPeriod.toInt()} °C, máx ${indicators.maxTempPeriod.toInt()} °C (Oscilación: ${indicators.thermalOscillation.toInt()} °C). ")
            append("Ocaso: ${sunsetTime ?: "20:30"}. Caída térmica nocturna: ${indicators.sunsetTempDrop.toInt()} °C. ")
            if (indicators.minTempPeriod >= 18.0) {
                append("RESTRICCIÓN OBLIGATORIA: Día puramente veraniego con mínima >= 18 °C. Prohibido sugerir abrigos, jerseys o chaquetas.")
            }
        }

        // Probamos con gemini-2.5-flash / gemini-flash-latest
        val models = listOf("gemini-2.5-flash", "gemini-flash-latest")
        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val requestJson = JSONObject().apply {
                    put("system_instruction", JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("text", SYSTEM_PROMPT) })
                        })
                    })
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply { put("text", userPrompt) })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.2)
                        put("response_mime_type", "application/json")
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val responseStr = response.body?.string() ?: return@use null
                    val jsonResp = JSONObject(responseStr)
                    val candidates = jsonResp.optJSONArray("candidates") ?: return@use null
                    if (candidates.length() == 0) return@use null

                    val firstCand = candidates.getJSONObject(0)
                    val content = firstCand.optJSONObject("content") ?: return@use null
                    val parts = content.optJSONArray("parts") ?: return@use null
                    if (parts.length() == 0) return@use null

                    val rawText = parts.getJSONObject(0).optString("text", "")
                    val cleanedText = rawText
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val resultObj = JSONObject(cleanedText)
                    val titular = resultObj.optString("titular", "")
                    val calzado = resultObj.optString("calzado", "")
                    val franjasObj = resultObj.optJSONObject("franjas")

                    val salida = franjasObj?.optString("salida", "") ?: ""
                    val mediodia = franjasObj?.optString("mediodia", "") ?: ""
                    val tarde = franjasObj?.optString("tarde", "") ?: ""
                    val regreso = franjasObj?.optString("regreso", "") ?: ""

                    if (titular.isNotBlank()) {
                        return@withContext GeminiBioclimaticResult(
                            titular = titular,
                            calzado = calzado,
                            franjas = GeminiFranjas(
                                salida = salida,
                                mediodia = mediodia,
                                tarde = tarde,
                                regreso = regreso
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Siguiente modelo o fallback instantáneo
            }
        }
        null
    }
}
