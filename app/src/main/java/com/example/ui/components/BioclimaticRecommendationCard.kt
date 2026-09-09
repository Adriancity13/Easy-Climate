package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ClothingRecommendation
import com.example.engine.ComplementAlert
import com.example.engine.DynamicComplementPill
import com.example.engine.DynamicFootwearPill
import com.example.engine.ModularLayerStrategy
import com.example.engine.RecommendationSource
import com.example.engine.ThermalLevel
import com.example.engine.TimelineMilestone
import java.util.Locale

private val CardBg = Color(0x380F172A)
private val CardBorder = Color(0x55FFFFFF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BioclimaticRecommendationCard(
    recommendation: ClothingRecommendation,
    modifier: Modifier = Modifier
) {
    var expandedDetails by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(22.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .padding(14.dp)
            .testTag("bioclimatic_recommendation_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {

            // 1. Header Row: Badge, Thermal Level & Solar Inertia indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val isAi = recommendation.source == RecommendationSource.AI_BIOCLIMATIC ||
                        recommendation.source == RecommendationSource.GEMINI_AI
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (isAi) Color(0x33A855F7) else Color(0x2238BDF8),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isAi) Color(0x88C084FC) else Color(0x5538BDF8),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.5.dp)
                ) {
                    Icon(
                        imageVector = if (isAi) Icons.Filled.AutoAwesome else Icons.Filled.Settings,
                        contentDescription = null,
                        tint = if (isAi) Color(0xFFE879F9) else Color(0xFF7DD3FC),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAi) "IA Bioclimática" else "Motor Local",
                        color = if (isAi) Color(0xFFF5D0FE) else Color(0xFFBAE6FD),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val levelColor = when (recommendation.thermalLevel) {
                    ThermalLevel.GELIDO -> Color(0xFF93C5FD)
                    ThermalLevel.FRIO_INTENSO -> Color(0xFF67E8F9)
                    ThermalLevel.FRIO_MODERADO -> Color(0xFFA5F3FC)
                    ThermalLevel.FRESCO_ENTRETIEMPO -> Color(0xFF86EFAC)
                    ThermalLevel.TEMPLADO_SUAVE -> Color(0xFFFDE047)
                    ThermalLevel.CALIDO -> Color(0xFFFDBA74)
                    ThermalLevel.CALOR_INTENSO -> Color(0xFFF87171)
                }
                Box(
                    modifier = Modifier
                        .background(levelColor.copy(alpha = 0.22f), CircleShape)
                        .border(1.dp, levelColor.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = recommendation.thermalLevel.levelName,
                        color = levelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Titular Hiper-Preciso en 1 frase (Lectura en < 2 segundos)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x351E293B), Color(0x22334155))
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, Color(0x55FDE047), RoundedCornerShape(14.dp))
                    .padding(11.dp)
                    .testTag("bioclimatic_hyper_headline")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ESTRATEGIA RECOMENDADA",
                            color = Color(0xFFFDE047),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val layerBadge = if (!recommendation.layerStrategy.isActive) {
                            if (recommendation.advancedMetrics.minTempPeriod >= 18.0) "Manga corta" else "Capa única"
                        } else {
                            "${recommendation.layerStrategy.totalLayers} Capas" +
                                    if (recommendation.layerStrategy.isThermalOscillationHigh) " (Oscilación)" else ""
                        }
                        Text(
                            text = layerBadge,
                            color = Color(0xFFBAE6FD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = recommendation.headline,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
            }

            // 3. Inercia Térmica Solar Directa vs. Sombra & Frío Calado / Bochorno
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x2A000000), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sol vs Sombra
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val sunT = String.format(Locale.US, "%.1f", recommendation.advancedMetrics.solarPerceivedTemp)
                        val shadeT = String.format(Locale.US, "%.1f", recommendation.advancedMetrics.shadePerceivedTemp)
                        Text(
                            text = "Sol: ${sunT}°C | Sombra: ${shadeT}°C",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delta solar o condición especial
                    val solarBoost = recommendation.advancedMetrics.solarBoostSun
                    if (solarBoost >= 2.5) {
                        Text(
                            text = "+${String.format(Locale.US, "%.1f", solarBoost)}°C al sol",
                            color = Color(0xFFFDE047),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (recommendation.metrics.isDampCold) {
                        Text(
                            text = "Frío calado (-2.2°C)",
                            color = Color(0xFF93C5FD),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (recommendation.metrics.isMuggyHeat) {
                        Text(
                            text = "Bochorno / Sudor lento",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 4. Píldoras Rápidas de Calzado y Complementos Clave (Iconos Dinámicos)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Calzado
                FootwearPillRow(footwear = recommendation.footwearPill)

                // Complementos dinámicos en FlowRow
                if (recommendation.complementPills.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recommendation.complementPills.forEach { comp ->
                            ComplementMiniPill(comp = comp)
                        }
                    }
                }
            }

            // 5. Línea de Tiempo Visual Rápida (4 Hitos Diarios: Salida, Mediodía, Tarde, Regreso)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x200F172A), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(14.dp))
                    .padding(10.dp)
                    .testTag("bioclimatic_4_milestones")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LÍNEA DE TIEMPO RÁPIDA (PRENDA CLAVE)",
                                color = Color(0xFFFDE68A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        recommendation.advancedMetrics.sunsetTime?.let { sunset ->
                            Text(
                                text = "Ocaso: $sunset h",
                                color = Color(0xFFFED7AA),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // 4 hitos en fila o grid de 2x2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recommendation.milestones.forEach { milestone ->
                            MilestoneItemCard(
                                milestone = milestone,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 6. Sistema de Capas Modulares (Método Cebolla 3 Capas)
            // SOLO se activa si la temperatura mínima cae por debajo de 14 °C
            if (recommendation.layerStrategy.isActive) {
                ModularLayersBox(strategy = recommendation.layerStrategy)
            }

            // 7. Toggle para detalles extendidos de franjas horarias y alertas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1F0F172A), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { expandedDetails = !expandedDetails }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("bioclimatic_details_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (expandedDetails) "Ocultar desglose horario" else "Ver desglose horario y alertas",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expandedDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Complement Alerts
                    if (recommendation.complementAlerts.isNotEmpty()) {
                        recommendation.complementAlerts.forEach { alert ->
                            ComplementAlertDetailedCard(alert = alert)
                        }
                    }

                    // 4 Franjas Horarias
                    if (recommendation.timeSlots.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            recommendation.timeSlots.forEach { slot ->
                                TimeSlotDetailCard(
                                    title = slot.fullTitle,
                                    temp = slot.temp,
                                    perceived = slot.perceivedTemp,
                                    advice = slot.adviceText,
                                    icon = slot.icon
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FootwearPillRow(footwear: DynamicFootwearPill) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x25FFFFFF), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = footwear.icon, fontSize = 14.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = footwear.title,
                    color = Color(0xFFFDE047),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
                Text(
                    text = footwear.reason,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            }
        }
    }
}

@Composable
private fun ComplementMiniPill(comp: DynamicComplementPill) {
    val bgColor = if (comp.isCrucial) Color(0x35D97706) else Color(0x25FFFFFF)
    val borderColor = if (comp.isCrucial) Color(0x75FBBF24) else Color(0x35FFFFFF)

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = comp.icon, fontSize = 12.sp)
            Text(
                text = comp.label,
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        }
    }
}

@Composable
private fun MilestoneItemCard(
    milestone: TimelineMilestone,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0x20FFFFFF), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = milestone.icon, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = when (milestone.id) {
                        "SALIDA" -> "Salida"
                        "MEDIODIA" -> "Mediodía"
                        "TARDE" -> "Tarde"
                        else -> "Regreso"
                    },
                    color = Color(0xFFFDE047),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${milestone.temp}°C",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = milestone.keyGarment,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 11.5.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModularLayersBox(strategy: ModularLayerStrategy) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x200F172A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "MÉTODO CEBOLLA (3 CAPAS MODULARES)",
                    color = Color(0xFFBAE6FD),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Capa 1: Base (Contacto)
            LayerItemRow(
                layerNumber = "1",
                layerName = "Base (Contacto)",
                layerContent = strategy.baseLayer,
                icon = "🎽"
            )

            // Capa 2: Intermedia (Aislamiento)
            strategy.midLayer?.let { mid ->
                LayerItemRow(
                    layerNumber = "2",
                    layerName = "Intermedia (Aislamiento)",
                    layerContent = mid,
                    icon = "🧶"
                )
            }

            // Capa 3: Exterior (Protección)
            strategy.outerLayer?.let { outer ->
                LayerItemRow(
                    layerNumber = "3",
                    layerName = "Exterior (Protección)",
                    layerContent = outer,
                    icon = "🧥"
                )
            }

            // Transición horaria si oscilación > 7°C
            strategy.layerTransitionAdvice?.let { advice ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x2A0369A1), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x5038BDF8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "💡 $advice",
                        color = Color(0xFFE0F2FE),
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerItemRow(
    layerNumber: String,
    layerName: String,
    layerContent: String,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = icon, fontSize = 12.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Capa $layerNumber · $layerName:",
                color = Color(0xFFFDE047),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = layerContent,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        }
    }
}

@Composable
private fun ComplementAlertDetailedCard(alert: ComplementAlert) {
    val (bgColor, borderColor, textColor) = if (alert.isWarning) {
        Triple(Color(0x30D97706), Color(0x70FBBF24), Color(0xFFFEF08A))
    } else {
        Triple(Color(0x2A0284C7), Color(0x6038BDF8), Color(0xFFBAE6FD))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = alert.icon, fontSize = 13.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = alert.description,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 10.5.sp,
                    lineHeight = 14.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            }
        }
    }
}

@Composable
private fun TimeSlotDetailCard(
    title: String,
    temp: Int,
    perceived: Int,
    advice: String,
    icon: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x18FFFFFF), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = icon, fontSize = 15.sp)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${temp}°C (Sens. ${perceived}°)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                Text(
                    text = advice,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            }
        }
    }
}
