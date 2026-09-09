package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GroqBioclimaticResult(
    val titular: String,
    val calzado: String,
    val franjas: GroqFranjas
)

data class GroqFranjas(
    val salida: String,
    val mediodia: String,
    val tarde: String,
    val regreso: String
)

/**
 * Cliente de integración ultrarrápido con la API de Groq (Llama 3.1 8B Instant).
 * Incorpora perfil fisiológico específico (alta sudoración, sensibilidad asmática/respiratoria)
 * y los 5 indicadores matemáticos bioclimáticos calculados localmente.
 */
object GroqBioclimaticAdvisor {

    private const val GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val GROQ_API_KEY = "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"
    private const val GROQ_MODEL = "openai/gpt-oss-20b"
    private const val GROQ_FALLBACK_MODEL = "qwen/qwen3.8-27b"

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .callTimeout(2500, TimeUnit.MILLISECONDS)
        .build()

    private const val SYSTEM_PROMPT = """Eres el asesor bioclimático experto para Easy-Climate.
Perfil fisiológico del usuario:
- Alta tasa de sudoración ante esfuerzo físico o calor.
- Alta sensibilidad respiratoria y asma: riesgo de espasmos bronquiales o enfriamiento pectoral si el sudor se enfría en el pecho ante corrientes de viento o bajas temperaturas.

INDICADORES CIENTÍFICOS Y TÁCTICAS BIOCLIMÁTICAS:
1. EVAPORACIÓN Y CAPILARIDAD (Punto de Rocío Td y Humedad):
   - Si Td ≥ 16 °C o HR > 70%: Obligatorio recomendar tejidos sintéticos de alta capilaridad / rejilla 3D de secado rápido (poliéster/poliamida). Queda totalmente prohibido el algodón absorbente por riesgo de saturación dérmica.
2. CONVECCIÓN URBANA Y PROTECCIÓN PECTORAL (Efecto Venturi y Delta Wind Chill):
   - Si Delta Wind Chill ≥ 3 °C con viento urbano ≥ 12 km/h: Exige protección pectoral mandatoria (cortavientos cerrado, capa frontal cortafuegos o cuello alto) para prevenir enfriamiento torácico o crisis asmática.
3. PROTECCIÓN DE MUCOSA RESPIRATORIA (Aire Frío/Seco):
   - Si T ≤ 12 °C y (HR < 40% o viento frío): Recomienda braga técnica o cuello protector de tejido poroso para atemperar e humidificar el aire antes de la inhalación.
4. RADIACIÓN SOLAR DIRECTA Y CONTRASTE SOL/SOMBRA:
   - Si Radiación ≥ 500 W/m² (aporte +2 °C a +4 °C al sol): Propón prendas modulares de fácil apertura o cremallera para transicionar entre sol y sombra sin sudar ni enfriarse.
5. ALERTA DE SUDOR FRÍO Y 'SUNSET DROP':
   - Si caída térmica de ocaso > 5 °C: Alerta sobre el riesgo de sudor frío y manda incluir cortavientos modular ligero fácil de guardar en la mochila.
6. AISLAMIENTO TÉRMICO EN UNIDADES CLO:
   - Respeta el nivel CLO indicado (0.3 CLO ligero hasta 1.1+ CLO multicapa).

REGLAS TÉRMICAS FUNDAMENTALES (CUMPLIMIENTO ESTRICTO):
1. REGLA FRÍA (Temperatura real < 15 °C):
   - TOTALMENTE PROHIBIDO hablar de riesgo de acaloramiento, sobrecalentamiento, bochorno o sugerir prendas de verano.
   - PRIORIDAD OBLIGATORIA: Contención del calor corporal, abrigo en capas modulares (primera capa térmica transpirable, capa intermedia y cortavientos cerrado) y protección estricta del pecho y vías respiratorias contra el viento frío para prevenir enfriamiento o crisis asmática.
2. REGLA CÁLIDA (Temperatura mínima ≥ 18 °C o real ≥ 22 °C):
   - TOTALMENTE PROHIBIDO sugerir abrigos gruesos, chaquetas pesadas o jerseys.
   - PRIORIDAD: Ropa ligera de verano de alta transpirabilidad (manga corta, tejidos sintéticos de secado rápido) para facilitar la evaporación del sudor.
3. REGLA TEMPLADA / ENTRETIEMPO (15 °C a 17.9 °C):
   - Capas intermedias versátiles y transpirables fáciles de abrir o quitar al caminar para termorregular.

ESTILO DE REDACCIÓN ESTRICTO (FLUIDO, HUMANO Y NATURAL):
- Estructura la respuesta de forma completamente fluida y orgánica en español, evitando frases estilo bot, listas telegráficas o el uso abusivo de puntos y comas.
- Conecta las prendas recomendadas con su motivo físico y médico mediante nexos causa-efecto naturales ('Para los X °C de hoy en Y, opta por A en tejido B. Te ayudará a C y a evitar D').
- Justifica la recomendación basándote en la evacuación del sudor, la protección del pecho/respiración por viento y el contraste sol/sombra o caída de temperatura.

ESTRUCTURA DEL TITULAR (OBLIGATORIO: ENTRE 20 Y 30 PALABRAS):
- La propiedad "titular" DEBE ser un consejo explicativo completo y razonado de EXACTAMENTE entre 20 y 30 palabras.
- Debe explicar con precisión QUÉ VESTIR y POR QUÉ con nexo causa-efecto natural.

SALIDA EN JSON ESTRICTO:
Devuelve EXCLUSIVAMENTE un objeto JSON válido con la siguiente estructura (sin etiquetas markdown ```json, sin explicaciones adicionales):
{
  "titular": "Consejo explicativo fluido y natural de entre 20 y 30 palabras explicando qué vestir y por qué con motivo fisiológico.",
  "calzado": "Calzado o complemento en 3-4 palabras.",
  "franjas": {
    "salida": "Prenda o consejo fluido para la mañana",
    "mediodia": "Prenda o consejo fluido para el mediodía",
    "tarde": "Prenda o consejo fluido para la tarde",
    "regreso": "Prenda o consejo fluido para la noche"
  }
}"""

    private fun executeGroqRequest(model: String, userPrompt: String): String {
        val requestJson = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
            put("temperature", 0.2)
            if (model.contains("oss")) {
                put("reasoning_effort", "low")
            }
            put("response_format", JSONObject().apply {
                put("type", "json_object")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(GROQ_ENDPOINT)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val responseStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = "HTTP ${response.code} ${response.message}: $responseStr"
                android.util.Log.e("GroqAPI", "Error HTTP devuelto por Groq ($model): $errorMsg")
                throw java.io.IOException(errorMsg)
            }
            return responseStr
        }
    }

    suspend fun getBioclimaticRecommendation(
        cityName: String,
        temp: Double,
        apparentTemp: Double,
        humidity: Int,
        windSpeed: Double,
        indicators: BioclimaticPhysicalIndicators,
        sunsetTime: String?
    ): GroqBioclimaticResult = withContext(Dispatchers.IO) {
        val userPrompt = buildString {
            append("Ciudad: $cityName. ")
            append("Temperatura real: ${String.format(Locale.US, "%.1f", temp)} °C. ")
            append("Sensación térmica aparente: ${String.format(Locale.US, "%.1f", apparentTemp)} °C. ")
            append("Humedad relativa: $humidity%. ")
            append("Velocidad de viento base: ${String.format(Locale.US, "%.1f", windSpeed)} km/h. ")
            append("\n--- INDICADORES FÍSICOS Y MATEMÁTICOS BIOCLIMÁTICOS --- \n")
            append("1. Tasa de Evaporación y Capilaridad: Punto de Rocío Td = ${String.format(Locale.US, "%.1f", indicators.dewPoint)} °C, Humidex = ${String.format(Locale.US, "%.1f", indicators.humidex)}. ")
            if (indicators.isHighSweatRisk) {
                append("ALERTA: Td ≥ 16 °C o HR > 70%. OBLIGATORIO recomendar tejidos sintéticos técnicos / rejilla 3D de secado rápido (poliéster/poliamida) y PROHIBIR algodón absorbente. ")
            }
            append("2. Convección Urbana y Pérdida Pectoral (Venturi x1.2): Viento corregido = ${String.format(Locale.US, "%.1f", indicators.urbanWindSpeed)} km/h, Wind Chill = ${String.format(Locale.US, "%.1f", indicators.urbanWindChill)} °C, Delta Wind Chill = ${String.format(Locale.US, "%.1f", indicators.deltaWindChill)} °C. ")
            if (indicators.isMandatoryChestProtection) {
                append("ALERTA: Pérdida pectoral Delta T ≥ 3 °C con viento urbano. PROTECCIÓN PECTORAL MANDATORIA (cortavientos cerrado / cuello alto para evitar espasmos por asma). ")
            }
            if (indicators.isRespiratoryMucosaRisk) {
                append("3. Factor de Mucosa Respiratoria: RIESGO ACTIVO por aire frío/seco (${temp.toInt()} °C, $humidity% HR). Recomienda braga técnica o cuello protector de tejido poroso. ")
            }
            append("4. Radiación Solar Directa: Estimada ${indicators.solarRadiationWm2.toInt()} W/m² (Sol ${String.format(Locale.US, "%.1f", indicators.sunPerceivedTemp)} °C vs Sombra ${String.format(Locale.US, "%.1f", indicators.shadePerceivedTemp)} °C, Ganancia +${String.format(Locale.US, "%.1f", indicators.solarGainTemp)} °C). ")
            if (indicators.isSunShadeContrastHigh) {
                append("Estrategia: Prenda modular con cremallera fácil de abrir al sol y cerrar a la sombra. ")
            }
            append("5. Oscilación y Sunset Drop: Mínima ${indicators.minTempPeriod.toInt()} °C, Máxima ${indicators.maxTempPeriod.toInt()} °C. Ocaso: ${sunsetTime ?: "20:30"} con caída térmica vespertina de ${indicators.sunsetTempDrop.toInt()} °C. ")
            if (indicators.isSunsetColdSweatRisk) {
                append("ALERTA: Caída de temperatura brusca al anochecer. Riesgo de sudor frío: exige cortavientos ligero modular en mochila. ")
            }
            append("6. Nivel de Aislamiento Térmico Requerido: ${indicators.cloDescription} (CLO: ${String.format(Locale.US, "%.1f", indicators.requiredClo)}). \n")

            if (temp < 15.0) {
                append("OBLIGATORIO: Temperatura real ${temp.toInt()} °C (< 15 °C). APLICA REGLA FRÍA. Totalmente prohibido hablar de acaloramiento, sobrecalentamiento, bochorno o ropa estival. Prioriza abrigo en capas modulares, retención térmica y protección de pecho y vías respiratorias contra el viento.")
            } else if (indicators.minTempPeriod >= 18.0) {
                append("OBLIGATORIO: Mínima diaria ${indicators.minTempPeriod.toInt()} °C (>= 18 °C). APLICA REGLA CÁLIDA. CERO abrigos, chaquetas o jerseys. Ropa sintética ligera de alta evaporación.")
            } else {
                append("OBLIGATORIO: Condición templada/entretiempo (${temp.toInt()} °C). Capas intermedias versátiles y transpirables.")
            }
        }

        var responseStr: String? = null
        try {
            responseStr = executeGroqRequest(GROQ_MODEL, userPrompt)
        } catch (e: Exception) {
            android.util.Log.e("GroqAPI", "Error en llamada con $GROQ_MODEL: ${e.message}. Reintentando con $GROQ_FALLBACK_MODEL...")
            try {
                responseStr = executeGroqRequest(GROQ_FALLBACK_MODEL, userPrompt)
            } catch (fallbackEx: Exception) {
                android.util.Log.e("GroqAPI", "Error en fallback con $GROQ_FALLBACK_MODEL: ${fallbackEx.message}")
                throw fallbackEx
            }
        }

        val jsonResp = JSONObject(responseStr)
        val choices = jsonResp.optJSONArray("choices")
            ?: throw org.json.JSONException("No choices found in Groq response: $responseStr")
        if (choices.length() == 0) {
            throw org.json.JSONException("Choices array is empty in Groq response: $responseStr")
        }

        val messageObj = choices.getJSONObject(0).optJSONObject("message")
            ?: throw org.json.JSONException("No message in choice 0 of Groq response")
        val rawContent = messageObj.optString("content", "")
        if (rawContent.isBlank()) {
            throw org.json.JSONException("Campo choices[0].message.content está vacío en respuesta de Groq")
        }

        android.util.Log.d("GroqAPI", "Contenido extraído de choices[0].message.content: $rawContent")

        val cleaned = rawContent
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val resultObj = JSONObject(cleaned)
        val titular = resultObj.optString("titular", "").trim()
        val calzado = resultObj.optString("calzado", "").trim()
        val franjasObj = resultObj.optJSONObject("franjas")

        val salida = franjasObj?.optString("salida", "")?.trim() ?: ""
        val mediodia = franjasObj?.optString("mediodia", "")?.trim() ?: ""
        val tarde = franjasObj?.optString("tarde", "")?.trim() ?: ""
        val regreso = franjasObj?.optString("regreso", "")?.trim() ?: ""

        if (titular.isBlank()) {
            throw org.json.JSONException("El JSON de Groq no contiene un campo 'titular' válido: $cleaned")
        }

        GroqBioclimaticResult(
            titular = titular,
            calzado = calzado,
            franjas = GroqFranjas(
                salida = salida,
                mediodia = mediodia,
                tarde = tarde,
                regreso = regreso
            )
        )
    }
}
