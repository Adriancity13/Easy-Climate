package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.CurrentWeatherUI
import com.example.engine.ClothingRecommendation
import com.example.engine.UrbanEnvironmentType
import com.example.ui.viewmodel.WeatherViewModel
import com.example.utils.DevToolsTelemetry
import com.example.widget.worker.WeatherWorkScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Diálogo de seguridad de acceso al Modo Desarrollador con PIN de 4 dígitos.
 */
@Composable
fun PinSecurityDialog(
    onDismissRequest: () -> Unit,
    onPinSuccess: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val validPins = listOf("1234", "0000", "4321")

    fun verifyPin() {
        if (pinText.trim() in validPins) {
            onPinSuccess()
        } else {
            isError = true
            errorMessage = "PIN incorrecto. Prueba con 1234."
            pinText = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF1E293B),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF94A3B8),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔒", fontSize = 22.sp)
            }
        },
        title = {
            Text(
                text = "Modo Desarrollador",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Introduce el PIN de 4 dígitos para acceder al panel de diagnóstico y telemetría en tiempo real.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFCBD5E1)
                )

                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinText = it
                            isError = false
                        }
                        if (it.length == 4) {
                            if (it in validPins) {
                                onPinSuccess()
                            } else {
                                isError = true
                                errorMessage = "PIN incorrecto. Prueba con 1234."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .testTag("pin_input_field"),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { verifyPin() }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF60A5FA),
                        unfocusedBorderColor = Color(0xFF475569),
                        cursorColor = Color(0xFF60A5FA)
                    )
                )

                if (isError) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "PIN por defecto: 1234",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { verifyPin() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.testTag("unlock_admin_button")
            ) {
                Text("Desbloquear", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}

/**
 * Pantalla / Modal Completo de Administración y Diagnóstico (DevTools Admin Panel).
 */
@Composable
fun DevToolsAdminModal(
    currentWeatherUI: CurrentWeatherUI?,
    viewModel: WeatherViewModel,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("🧬 Bioclimático", "🗺️ Microclima OSM", "🌐 Red & LLM", "💾 Caché", "🎛️ Sandbox", "📊 Telemetría")

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0F1D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🛠️", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "Easy-Climate DevTools",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Panel de Diagnóstico & Telemetría RAM",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar DevTools",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Tabs Navigation
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF60A5FA),
                    edgePadding = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                                )
                            }
                        )
                    }
                }

                // Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> BioclimaticInspectorTab(currentWeatherUI?.recommendation)
                        1 -> OsmMicroclimateInspectorTab(viewModel, context)
                        2 -> NetworkAndLlmMonitorTab(context)
                        3 -> CacheAndStorageTab(viewModel, context)
                        4 -> ClimateSandboxTab(viewModel) { onDismissRequest() }
                        5 -> TelemetryAndPerformanceTab(context)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 1: INSPECTOR DEL MOTOR BIOCLIMÁTICO
// -------------------------------------------------------------------------------------------------
@Composable
private fun BioclimaticInspectorTab(recommendation: ClothingRecommendation?) {
    if (recommendation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No hay datos bioclimáticos cargados en este momento.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        }
        return
    }

    val adv = recommendation.advancedMetrics
    val metrics = recommendation.metrics

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Source Badge Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fuente de Decisión Activa",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Text(
                    text = recommendation.source.label,
                    color = if (recommendation.source == com.example.engine.RecommendationSource.AI_BIOCLIMATIC) Color(0xFF34D399) else Color(0xFF60A5FA),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = recommendation.thermalLevel.levelName,
                color = recommendation.thermalLevel.color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(recommendation.thermalLevel.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // 1. Magnus-Tetens & Humidex
        AdminSectionCard(title = "💧 Evaporación & Capilaridad (Magnus-Tetens)") {
            AdminMetricRow(
                label = "Punto de Rocío (Td)",
                value = "${String.format(Locale.US, "%.1f", adv.dewPoint)} °C",
                subtext = if (adv.dewPoint >= 16.0) "Td ≥ 16°C (Bochorno / Riesgo Sudor)" else "Confort Evaporativo"
            )
            AdminMetricRow(
                label = "Índice Humidex",
                value = String.format(Locale.US, "%.1f", adv.humidex),
                subtext = if (adv.humidex >= 30.0) "Incomodidad por humedad" else "Sin estrés higrotérmico"
            )
            AdminMetricRow(
                label = "Humedad Relativa / Presión Vapor",
                value = "${metrics.humidity}% HR",
                subtext = "Magnus Td: alpha(T, RH)"
            )
        }

        // 2. Convección Urbana y Venturi
        AdminSectionCard(title = "🌬️ Convección Urbana & Efecto Venturi") {
            AdminMetricRow(
                label = "Viento Base de Estación",
                value = "${String.format(Locale.US, "%.1f", metrics.windSpeed)} km/h",
                subtext = "Medido a 10m"
            )
            AdminMetricRow(
                label = "Viento Urbano Corregido (Venturi x1.2)",
                value = "${String.format(Locale.US, "%.1f", adv.urbanWindSpeed)} km/h",
                subtext = "Cañón urbano calle / edificios"
            )
            AdminMetricRow(
                label = "Delta Wind Chill Pectoral (ΔT)",
                value = "${String.format(Locale.US, "%.1f", adv.deltaWindChill)} °C",
                subtext = if (adv.isMandatoryChestProtection) "PROTECCIÓN PECTORAL MANDATORIA" else "Disipación convectiva moderada",
                highlightColor = if (adv.isMandatoryChestProtection) Color(0xFFF87171) else Color(0xFF34D399)
            )
        }

        // 3. Mucosa & Radiación
        AdminSectionCard(title = "☀️ Radiación Solar vs Sombra & Mucosa") {
            AdminMetricRow(
                label = "Irradiancia Solar Estimada",
                value = "${adv.solarRadiationWm2.toInt()} W/m²",
                subtext = "Aporte: +${String.format(Locale.US, "%.1f", adv.solarBoostSun)} °C al sol"
            )
            AdminMetricRow(
                label = "Contraste Sol vs Sombra",
                value = "Sol ${adv.solarPerceivedTemp.toInt()}°C / Sombra ${adv.shadePerceivedTemp.toInt()}°C",
                subtext = if (adv.isSunShadeContrastHigh) "Contraste Alto -> Prenda con cremallera" else "Bajo contraste térmico"
            )
            AdminMetricRow(
                label = "Riesgo Mucosa Respiratoria",
                value = if (adv.isRespiratoryMucosaRisk) "ACTIVO" else "Normal",
                subtext = if (adv.isRespiratoryMucosaRisk) "Aire frío/seco (Braga técnica sugerida)" else "Condición bronquial estable",
                highlightColor = if (adv.isRespiratoryMucosaRisk) Color(0xFFFBBF24) else Color(0xFF34D399)
            )
        }

        // 4. Oscilación & Sunset Drop
        AdminSectionCard(title = "🌅 'Sunset Drop' & Oscilación Térmica") {
            AdminMetricRow(
                label = "Puesta de Sol Oficial",
                value = adv.sunsetTime ?: "20:30",
                subtext = "Hora de corte térmico"
            )
            AdminMetricRow(
                label = "Caída Térmica Vespertina",
                value = "${adv.thermalOscillation.toInt()} °C oscilación",
                subtext = if (adv.isSunsetColdSweatRisk) "ALERTA: Riesgo sudor frío post-sunset" else "Caída térmica progresiva",
                highlightColor = if (adv.isSunsetColdSweatRisk) Color(0xFFF87171) else Color(0xFF34D399)
            )
        }

        // 5. Matriz CLO y Capas
        AdminSectionCard(title = "🧥 Nivel de Aislamiento Térmico (CLO)") {
            AdminMetricRow(
                label = "Índice CLO",
                value = "${String.format(Locale.US, "%.2f", adv.requiredClo)} CLO",
                subtext = adv.cloDescription
            )
            AdminMetricRow(
                label = "Capa Base (Contacto)",
                value = recommendation.layerStrategy.baseLayer,
                subtext = "Evacuación de sudor"
            )
            recommendation.layerStrategy.midLayer?.let {
                AdminMetricRow(
                    label = "Capa Media (Aislamiento)",
                    value = it,
                    subtext = "Retención de calor"
                )
            }
            recommendation.layerStrategy.outerLayer?.let {
                AdminMetricRow(
                    label = "Capa Exterior (Protección)",
                    value = it,
                    subtext = "Cortavientos / membrana"
                )
            }
        }

        // 6. Flags Bioclimáticas
        AdminSectionCard(title = "🏷️ Flags Bioclimáticas Activas") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminFlagChip(label = "Sudor Crítico", isActive = adv.isHighSweatRisk)
                AdminFlagChip(label = "Pecho Venturi", isActive = adv.isMandatoryChestProtection)
                AdminFlagChip(label = "Mucosa", isActive = adv.isRespiratoryMucosaRisk)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminFlagChip(label = "Contraste Sol", isActive = adv.isSunShadeContrastHigh)
                AdminFlagChip(label = "Sunset Drop", isActive = adv.isSunsetColdSweatRisk)
                AdminFlagChip(label = "Frío Húmedo", isActive = metrics.isDampCold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 2: INSPECTOR MICROCLIMÁTICO OSM (NOMINATIM)
// -------------------------------------------------------------------------------------------------
@Composable
private fun OsmMicroclimateInspectorTab(viewModel: WeatherViewModel, context: Context) {
    val nominatimMetrics by DevToolsTelemetry.nominatimMetrics.collectAsState()

    val statusColor = when {
        nominatimMetrics.httpStatus.contains("EXITOSA") -> Color(0xFF10B981)
        nominatimMetrics.httpStatus.contains("TIMEOUT") -> Color(0xFFEF4444)
        nominatimMetrics.httpStatus.contains("FALLBACK_FORZADO") -> Color(0xFFF59E0B)
        nominatimMetrics.httpStatus.contains("FALLBACK") -> Color(0xFFF97316)
        else -> Color(0xFF64748B)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Estado del Servicio Nominatim
        AdminSectionCard(title = "🛰️ Estado del Servicio OpenStreetMap (Nominatim)") {
            // Service Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado HTTP / Fallback:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = nominatimMetrics.httpStatus,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            AdminMetricRow(
                label = "Latencia Nominatim",
                value = "${nominatimMetrics.lastNominatimLatencyMs} ms",
                subtext = if (nominatimMetrics.lastNominatimLatencyMs > 0) {
                    if (nominatimMetrics.lastNominatimLatencyMs < 500) "⚡ Ultra-rápido (< 500ms)"
                    else if (nominatimMetrics.lastNominatimLatencyMs < 1500) "⏱️ Aceptable (< 1.5s)"
                    else "⚠️ Latencia alta / Límite alcanzado"
                } else "Sin consulta activa / Cache en memoria",
                highlightColor = if (nominatimMetrics.lastNominatimLatencyMs in 1..800) Color(0xFF34D399) else if (nominatimMetrics.lastNominatimLatencyMs >= 1500) Color(0xFFEF4444) else Color.White
            )

            AdminMetricRow(
                label = "Modo de Ubicación Activo",
                value = nominatimMetrics.locationMode,
                subtext = when (nominatimMetrics.locationMode) {
                    "GPS_EXACTO" -> "Reverse geocoding punto a punto (Precisión alta)"
                    "BUSQUEDA_ZONA" -> "Forward geocoding por municipio / barrio"
                    "OVERRIDE_MANUAL" -> "Microclima forzado manualmente en Sandbox"
                    else -> "Modo estándar"
                },
                highlightColor = Color(0xFF38BDF8)
            )

            AdminMetricRow(
                label = "User-Agent Configurado",
                value = "EasyClimateApp/1.0",
                subtext = "contacto@easyclimate.local (Conforme a OSM Usage Policy)"
            )

            AdminMetricRow(
                label = "Timeout Estricto de Conexión",
                value = "1.500 ms (1.5s)",
                subtext = "Retorno silencioso a perfil neutro si se excede"
            )
        }

        // 2. Datos Crudos de OpenStreetMap
        AdminSectionCard(title = "🗺️ Datos Crudos de OpenStreetMap (Nominatim)") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Lugar Detectado (display_name):",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = nominatimMetrics.detectedPlaceName.ifEmpty { "Sin ubicación geocodificada actualmente" },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Etiquetas OSM Clave Extraídas (extratags):",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (nominatimMetrics.activeOsmTags.isEmpty()) {
                Text(
                    text = "No se detectaron etiquetas urbanas específicas (highway, leisure, waterway, natural, etc.). Se utiliza el perfil estándar.",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    nominatimMetrics.activeOsmTags.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
                                color = Color(0xFF60A5FA),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = value,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 3. Impacto en la Física Bioclimática (Ajustes aplicados)
        AdminSectionCard(title = "⚙️ Impacto en la Física Bioclimática (Ajustes en RAM)") {
            // Environment Type Banner
            val (envEmoji, envTitle, envColor) = when (nominatimMetrics.assignedEnvironmentType) {
                UrbanEnvironmentType.NARROW_STREET -> Triple("🏙️", "CALLE ESTRECHA / RESIDENCIAL", Color(0xFFF59E0B))
                UrbanEnvironmentType.WIDE_AVENUE -> Triple("☀️", "AVENIDA ANCHA / PLAZA ABIERTA", Color(0xFFF97316))
                UrbanEnvironmentType.PARK_FOREST -> Triple("🌿", "PARQUE / ZONA ARBOLADA", Color(0xFF10B981))
                UrbanEnvironmentType.WATER_BODY -> Triple("🌊", "RIBERA / LAGO / MASA DE AGUA", Color(0xFF06B6D4))
                UrbanEnvironmentType.STANDARD -> Triple("🏛️", "ENTORNO URBANO ESTÁNDAR", Color(0xFF94A3B8))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(envColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, envColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = envEmoji, fontSize = 20.sp)
                Column {
                    Text(
                        text = "Microclima: $envTitle",
                        color = envColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = nominatimMetrics.assignedEnvironmentType.label,
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Delta Temp
            AdminMetricRow(
                label = "Temperatura Evapotranspirativa (ΔT)",
                value = "${String.format(Locale.US, "%.1f", nominatimMetrics.rawTempBase)}°C -> ${String.format(Locale.US, "%.1f", nominatimMetrics.adjustedTemp)}°C",
                subtext = "Modificador: ${if (nominatimMetrics.tempDelta >= 0) "+" else ""}${String.format(Locale.US, "%.1f", nominatimMetrics.tempDelta)}°C (${if (nominatimMetrics.tempDelta < 0) "Descuento vegetal" else if (nominatimMetrics.tempDelta > 0) "Ganancia radiante" else "Sin corrección"})",
                highlightColor = if (nominatimMetrics.tempDelta < 0) Color(0xFF34D399) else if (nominatimMetrics.tempDelta > 0) Color(0xFFF97316) else Color.White
            )

            // Delta Humidity
            AdminMetricRow(
                label = "Modificador Humedad Relativa (ΔHR)",
                value = "${nominatimMetrics.rawHumidityBase}% -> ${nominatimMetrics.adjustedHumidity}%",
                subtext = "Modificador: ${if (nominatimMetrics.humidityDelta >= 0) "+" else ""}${nominatimMetrics.humidityDelta}% (${if (nominatimMetrics.humidityDelta > 0) "Aporte evaporativo acuático" else if (nominatimMetrics.humidityDelta < 0) "Dispersión solar" else "Sin corrección"})",
                highlightColor = if (nominatimMetrics.humidityDelta > 0) Color(0xFF38BDF8) else Color.White
            )

            // Wind Factor
            AdminMetricRow(
                label = "Factor Viento Urbano (Efecto Venturi)",
                value = "${String.format(Locale.US, "%.1f", nominatimMetrics.rawWindBase)} -> ${String.format(Locale.US, "%.1f", nominatimMetrics.adjustedWind)} km/h",
                subtext = "Multiplicador de fricción: x${String.format(Locale.US, "%.2f", nominatimMetrics.windFactor)} (${if (nominatimMetrics.windFactor > 1.0) "Aceleración en cañón urbano" else if (nominatimMetrics.windFactor < 1.0) "Frenado por arbolado" else "Base"})",
                highlightColor = if (nominatimMetrics.windFactor > 1.0) Color(0xFFF87171) else Color.White
            )

            // Solar Factor
            AdminMetricRow(
                label = "Factor Irradiancia Solar Directa",
                value = "x${String.format(Locale.US, "%.2f", nominatimMetrics.solarFactor)}",
                subtext = if (nominatimMetrics.solarFactor > 1.0) "Plaza/Avenida despejada (+20% irradiancia)" else if (nominatimMetrics.solarFactor < 1.0) "Sombra de edificios/árboles (-15% a -20%)" else "Neutro"
            )
        }

        // 4. Controles Interactivos de Depuración (Sandbox / Override)
        AdminSectionCard(title = "🎛️ Controles Interactivos de Depuración (Sandbox Microclimático)") {
            // Forzar Fallback Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simular Fallback OSM",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fuerza el perfil neutro sin consultar la API de Nominatim",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = nominatimMetrics.isOsmFallbackForced,
                    onCheckedChange = { forced ->
                        DevToolsTelemetry.setOsmFallbackForced(forced)
                        viewModel.reloadCurrentLocation()
                        Toast.makeText(
                            context,
                            if (forced) "Fallback OSM Forzado: Perfil Neutro" else "Fallback OSM Desactivado: Nominatim Activo",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFF59E0B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Selector de Prueba Rápida de Microclima (Override en Vivo):",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val overrideOptions = listOf(
                null to "🔄 Detección Real OSM",
                UrbanEnvironmentType.PARK_FOREST to "🌿 Parque / Bosque (-1.5°C)",
                UrbanEnvironmentType.WATER_BODY to "🌊 Río / Lago (+10% HR)",
                UrbanEnvironmentType.NARROW_STREET to "🏙️ Calle Estrecha (Venturi x1.25)",
                UrbanEnvironmentType.WIDE_AVENUE to "☀️ Plaza / Avenida (+0.8°C)"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                overrideOptions.forEach { (envType, label) ->
                    val isSelected = nominatimMetrics.manualOverrideEnvironment == envType
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) Color(0xFF2563EB).copy(alpha = 0.35f) else Color(0xFF1E293B),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                DevToolsTelemetry.setManualOverrideEnvironment(envType)
                                viewModel.reloadCurrentLocation()
                                Toast.makeText(context, "Microclima forzado: $label", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Text(
                                text = "ACTIVO",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.reloadCurrentLocation()
                    Toast.makeText(context, "Reconsultando OpenStreetMap y Open-Meteo...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Re-ejecutar Consulta OSM y Recalcular Clima", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 3: MONITOR DE RED & LLM (GROQ CLOUD)
// -------------------------------------------------------------------------------------------------
@Composable
private fun NetworkAndLlmMonitorTab(context: Context) {
    val netMetrics by DevToolsTelemetry.networkMetrics.collectAsState()
    var selectedRawView by remember { mutableStateOf("prompt") } // "prompt" or "response"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Latency Overview
        AdminSectionCard(title = "⏱️ Métricas de Latencia en Tiempo Real") {
            AdminMetricRow(
                label = "API Open-Meteo",
                value = "${netMetrics.lastOpenMeteoLatencyMs} ms",
                subtext = "Pronóstico horario y diario global"
            )
            AdminMetricRow(
                label = "Groq Cloud API (Llama 3.1 8B)",
                value = "${netMetrics.lastGroqLatencyMs} ms",
                subtext = "Timeout configurado en 2500 ms"
            )
            AdminMetricRow(
                label = "Generación de Tokens / seg",
                value = "${String.format(Locale.US, "%.1f", netMetrics.groqTokensPerSecond)} tok/s",
                subtext = "Rendimiento LPU Inference Engine"
            )
            AdminMetricRow(
                label = "Tokens Consumidos en Sesión",
                value = "${netMetrics.totalTokensConsumed} tokens",
                subtext = "Llamadas exitosas: ${netMetrics.groqCallsCount}"
            )
            AdminMetricRow(
                label = "Fallbacks a Motor Local",
                value = "${netMetrics.fallbackCount} veces",
                subtext = "Activados por timeout o modo offline",
                highlightColor = if (netMetrics.fallbackCount > 0) Color(0xFFFBBF24) else Color(0xFF34D399)
            )
        }

        // Raw Payload Viewer
        AdminSectionCard(title = "📄 Visor de Payloads Crudos (JSON / Prompts)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedRawView = "prompt" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRawView == "prompt") Color(0xFF3B82F6) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Prompt Enviado", fontSize = 11.sp)
                }
                Button(
                    onClick = { selectedRawView = "response" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRawView == "response") Color(0xFF3B82F6) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Respuesta Cruda", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val contentToShow = if (selectedRawView == "prompt") {
                netMetrics.lastGroqPromptJson.ifBlank { "No se ha registrado ningún prompt a Groq todavía." }
            } else {
                netMetrics.lastGroqRawResponse.ifBlank { "No se ha registrado respuesta de Groq todavía." }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = contentToShow,
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("DevTools Payload", contentToShow)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Payload copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copiar al Portapapeles", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 3: INSPECTOR DE CACHÉ & ALMACENAMIENTO
// -------------------------------------------------------------------------------------------------
@Composable
private fun CacheAndStorageTab(viewModel: WeatherViewModel, context: Context) {
    val cacheMetrics by DevToolsTelemetry.cacheMetrics.collectAsState()
    val logs by DevToolsTelemetry.logs.collectAsState()

    val elapsedMinutes = (System.currentTimeMillis() - cacheMetrics.lastRefreshTimeMs) / 60000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cache Double-Barrier Status
        AdminSectionCard(title = "🛡️ Estado de Caché de Doble Barrera") {
            AdminMetricRow(
                label = "Distancia GPS vs Caché (Haversine)",
                value = "${String.format(Locale.US, "%.3f", cacheMetrics.distanceKm)} km",
                subtext = "Umbral de invalidación por movimiento: 15 km"
            )
            AdminMetricRow(
                label = "Tiempo transcurrido (TTL)",
                value = "$elapsedMinutes min",
                subtext = "Umbral de invalidación por tiempo: 60 min"
            )
            AdminMetricRow(
                label = "Delta Térmico (ΔT)",
                value = "${String.format(Locale.US, "%.1f", cacheMetrics.deltaTempC)} °C",
                subtext = "Umbral de invalidación térmica: ≥ 3 °C"
            )
            AdminMetricRow(
                label = "Ubicación en Caché",
                value = cacheMetrics.cachedCityName ?: "Sin caché",
                subtext = "Lat: ${cacheMetrics.cachedLat ?: "--"}, Lon: ${cacheMetrics.cachedLon ?: "--"}"
            )
        }

        // Direct Actions
        AdminSectionCard(title = "⚡ Acciones de Control de Caché y Red") {
            // Simulated Offline Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simular Modo Offline",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fuerza que las llamadas fallen para comprobar el fallback",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = cacheMetrics.isSimulatedOffline,
                    onCheckedChange = { viewModel.toggleSimulatedOffline(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFEF4444)
                    )
                )
            }

            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

            // Purge Cache Button
            Button(
                onClick = {
                    viewModel.forceClearCache()
                    Toast.makeText(context, "Caché de SharedPreferences purgada", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🗑️ Forzar Purga Total de Caché", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Export Logs Button
            OutlinedButton(
                onClick = {
                    val exportText = DevToolsTelemetry.exportAllLogs()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("EasyClimate DevLogs", exportText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Registro de logs copiado al portapapeles", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 Exportar Registro de Logs", color = Color.White, fontSize = 12.sp)
            }
        }

        // Recent System Logs Console
        AdminSectionCard(title = "📜 Consola de Registros en Tiempo Real") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No hay registros recientes.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        logs.takeLast(30).forEach { log ->
                            Text(
                                text = "[${log.timestamp}] [${log.tag}] ${log.message}",
                                color = if (log.isError) Color(0xFFF87171) else Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 4: SIMULADOR DE ESCENARIOS CLIMÁTICOS (SANDBOX)
// -------------------------------------------------------------------------------------------------
@Composable
private fun ClimateSandboxTab(
    viewModel: WeatherViewModel,
    onApplied: () -> Unit
) {
    val simConfig by DevToolsTelemetry.simulationConfig.collectAsState()

    var tempC by remember { mutableDoubleStateOf(simConfig.tempC) }
    var humidity by remember { mutableIntStateOf(simConfig.humidity) }
    var windKmH by remember { mutableDoubleStateOf(simConfig.windSpeedKmH) }
    var cloudCover by remember { mutableIntStateOf(simConfig.cloudCover) }
    var weatherCode by remember { mutableIntStateOf(simConfig.weatherCode) }
    var isDay by remember { mutableStateOf(simConfig.isDay) }

    fun applyPreset(t: Double, h: Int, w: Double, clouds: Int, code: Int, day: Boolean) {
        tempC = t
        humidity = h
        windKmH = w
        cloudCover = clouds
        weatherCode = code
        isDay = day
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Presets
        AdminSectionCard(title = "⚡ Escenarios Extremos Predefinidos") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip(label = "❄️ Nieve (-3°C)", modifier = Modifier.weight(1f)) {
                    applyPreset(-3.0, 75, 30.0, 95, 71, true)
                }
                PresetChip(label = "⛈️ Tormenta (22°C)", modifier = Modifier.weight(1f)) {
                    applyPreset(22.0, 85, 45.0, 100, 95, false)
                }
                PresetChip(label = "🌅 Ocaso (14°C)", modifier = Modifier.weight(1f)) {
                    applyPreset(14.0, 65, 18.0, 40, 2, false)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip(label = "🏖️ Bochorno (36°C)", modifier = Modifier.weight(1f)) {
                    applyPreset(36.0, 80, 8.0, 20, 0, true)
                }
                PresetChip(label = "🍃 Fresco (16°C)", modifier = Modifier.weight(1f)) {
                    applyPreset(16.0, 50, 12.0, 30, 1, true)
                }
                PresetChip(label = "🌫️ Niebla (8°C)", modifier = Modifier.weight(1f)) {
                    applyPreset(8.0, 95, 5.0, 100, 45, true)
                }
            }
        }

        // Sliders
        AdminSectionCard(title = "🎛️ Parámetros Meteorológicos Manuales") {
            // Temperatura
            Text(
                text = "Temperatura: ${tempC.roundToInt()} °C",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = tempC.toFloat(),
                onValueChange = { tempC = it.toDouble() },
                valueRange = -15f..45f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF60A5FA),
                    activeTrackColor = Color(0xFF3B82F6)
                )
            )

            // Humedad
            Text(
                text = "Humedad Relativa: $humidity %",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = humidity.toFloat(),
                onValueChange = { humidity = it.roundToInt() },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF34D399),
                    activeTrackColor = Color(0xFF10B981)
                )
            )

            // Viento
            Text(
                text = "Velocidad de Viento: ${windKmH.roundToInt()} km/h",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = windKmH.toFloat(),
                onValueChange = { windKmH = it.toDouble() },
                valueRange = 0f..80f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFBBF24),
                    activeTrackColor = Color(0xFFD97706)
                )
            )

            // Cobertura Nubosa
            Text(
                text = "Cobertura Nubosa: $cloudCover %",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = cloudCover.toFloat(),
                onValueChange = { cloudCover = it.roundToInt() },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFA78BFA),
                    activeTrackColor = Color(0xFF7C3AED)
                )
            )

            // Código WMO Selector
            Text(
                text = "Condición WMO: $weatherCode (${getWmoLabel(weatherCode)})",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0 to "Despejado", 3 to "Nublado", 45 to "Niebla", 63 to "Lluvia", 71 to "Nieve", 95 to "Tormenta").forEach { (code, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (weatherCode == code) Color(0xFF2563EB) else Color(0xFF1E293B))
                            .clickable { weatherCode = code }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Día / Noche Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDay) "☀️ Horario Diurno (Sol Activo)" else "🌙 Horario Nocturno (Estrellas)",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Switch(
                    checked = isDay,
                    onCheckedChange = { isDay = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFF59E0B)
                    )
                )
            }
        }

        // Apply / Reset Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.applyClimateOverride(
                        temp = tempC,
                        humidity = humidity,
                        windSpeed = windKmH,
                        cloudCover = cloudCover,
                        weatherCode = weatherCode,
                        isDay = isDay
                    )
                    onApplied()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚡ Aplicar Simulación en la UI", fontWeight = FontWeight.Bold)
            }

            if (simConfig.isActive) {
                OutlinedButton(
                    onClick = {
                        viewModel.resetClimateOverride()
                        onApplied()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔄 Restablecer Datos Reales", color = Color(0xFF60A5FA))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 5: TELEMETRÍA Y RENDIMIENTO DEL DISPOSITIVO
// -------------------------------------------------------------------------------------------------
@Composable
private fun TelemetryAndPerformanceTab(context: Context) {
    val runtime = Runtime.getRuntime()
    val totalRamMb = runtime.totalMemory() / (1024 * 1024)
    val freeRamMb = runtime.freeMemory() / (1024 * 1024)
    val usedRamMb = totalRamMb - freeRamMb
    val maxRamMb = runtime.maxMemory() / (1024 * 1024)
    val ramProgress = if (maxRamMb > 0) usedRamMb.toFloat() / maxRamMb.toFloat() else 0f

    // Medición de FPS en tiempo real
    var fps by remember { mutableIntStateOf(60) }
    LaunchedEffect(Unit) {
        var frameCount = 0
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                frameCount++
                if (now - lastTime >= 1_000_000_000L) {
                    fps = frameCount
                    frameCount = 0
                    lastTime = now
                }
            }
        }
    }

    val prefs = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
    val lastUpdateTime = prefs.getLong("last_weather_update_time", 0L)
    val lastUpdateStr = if (lastUpdateTime > 0) {
        SimpleDateFormat("HH:mm:ss (dd/MM)", Locale.getDefault()).format(Date(lastUpdateTime))
    } else {
        "Nunca / Reciente"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Memory RAM Monitor
        AdminSectionCard(title = "🧠 Uso de Memoria RAM (JVM Process Heap)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RAM Asignada: $usedRamMb MB / $maxRamMb MB",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(ramProgress * 100).toInt()}%",
                    color = Color(0xFF60A5FA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ramProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (ramProgress > 0.8f) Color(0xFFEF4444) else Color(0xFF3B82F6),
                trackColor = Color(0xFF1E293B),
            )
            Spacer(modifier = Modifier.height(6.dp))
            AdminMetricRow(
                label = "Memoria Libre en Heap",
                value = "$freeRamMb MB",
                subtext = "Total Asignado: $totalRamMb MB"
            )

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = {
                    System.gc()
                    Toast.makeText(context, "System.gc() solicitado", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Liberar Memoria (System.gc())", fontSize = 11.sp, color = Color.White)
            }
        }

        // FPS & Battery Performance
        AdminSectionCard(title = "⚡ Renderizado Canvas & Impacto de Batería") {
            AdminMetricRow(
                label = "Tasa de Refresco de Pantalla (FPS)",
                value = "$fps FPS",
                subtext = "Renderizado procedural con Hardware Acceleration",
                highlightColor = if (fps >= 55) Color(0xFF34D399) else Color(0xFFFBBF24)
            )
            AdminMetricRow(
                label = "Estimación de Consumo Energético",
                value = "Muy Bajo (< 1.5% CPU)",
                subtext = "Partículas de lluvia/nieve optimizadas con drawPoints batch"
            )
        }

        // Widget & WorkManager Telemetry
        AdminSectionCard(title = "📱 Estado del Widget de Escritorio & WorkManager") {
            AdminMetricRow(
                label = "Tarea Periódica de Fondo",
                value = "Activa (Cada 1 hora)",
                subtext = "weather_widget_periodic_update"
            )
            AdminMetricRow(
                label = "Restricciones de Red",
                value = "NetworkType.CONNECTED",
                subtext = "Solo descarga datos si hay conexión"
            )
            AdminMetricRow(
                label = "Última Sincronización del Widget",
                value = lastUpdateStr,
                subtext = "SharedPreferences timestamp"
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    WeatherWorkScheduler.enqueueOneTimeSync(context)
                    Toast.makeText(context, "Sincronización de fondo encolada", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📲 Forzar Actualización del Widget Ahora", fontSize = 11.sp, color = Color(0xFF60A5FA))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// REUSABLE HELPER COMPOSABLES
// -------------------------------------------------------------------------------------------------

@Composable
private fun AdminSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            content()
        }
    }
}

@Composable
private fun AdminMetricRow(
    label: String,
    value: String,
    subtext: String? = null,
    highlightColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtext != null) {
                Text(
                    text = subtext,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
        Text(
            text = value,
            color = highlightColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AdminFlagChip(label: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (isActive) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF1E293B),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isActive) Color(0xFFEF4444) else Color(0xFF334155),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isActive) "●" else "○",
                color = if (isActive) Color(0xFFEF4444) else Color(0xFF64748B),
                fontSize = 10.sp
            )
            Text(
                text = label,
                color = if (isActive) Color.White else Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PresetChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF93C5FD),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private fun getWmoLabel(code: Int): String {
    return when (code) {
        0 -> "Despejado"
        1, 2, 3 -> "Nuboso"
        45, 48 -> "Niebla"
        51, 53, 55 -> "Llovizna"
        61, 63, 65 -> "Lluvia"
        71, 73, 75 -> "Nieve"
        80, 81, 82 -> "Chubascos"
        95, 96, 99 -> "Tormenta"
        else -> "Variable"
    }
}
