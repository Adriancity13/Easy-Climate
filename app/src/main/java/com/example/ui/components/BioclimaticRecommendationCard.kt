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
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BioclimaticRecommendationCard(
    recommendation: ClothingRecommendation,
    modifier: Modifier = Modifier
) {
    var expandedDetails by remember { mutableStateOf(false) }

    // Single Glassmorphic card container (20.dp radius, no nested matryoshka boxes)
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bioclimatic_recommendation_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 1. Header Row: Discreet Integrated Badge & Thermal Level Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val isAi = recommendation.source == RecommendationSource.AI_BIOCLIMATIC ||
                        recommendation.source == RecommendationSource.GEMINI_AI

                // Discreet source badge without shiny borders
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isAi) Icons.Filled.AutoAwesome else Icons.Filled.Settings,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isAi) "IA Bioclimática" else "Motor Local",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Thermal Level tag
                val levelColor = when (recommendation.thermalLevel) {
                    ThermalLevel.GELIDO -> Color(0xFF93C5FD)
                    ThermalLevel.FRIO_INTENSO -> Color(0xFF67E8F9)
                    ThermalLevel.FRIO_MODERADO -> Color(0xFFA5F3FC)
                    ThermalLevel.FRESCO_ENTRETIEMPO -> Color(0xFF86EFAC)
                    ThermalLevel.TEMPLADO_SUAVE -> Color(0xFFFDE047)
                    ThermalLevel.CALIDO -> Color(0xFFFDBA74)
                    ThermalLevel.CALOR_INTENSO -> Color(0xFFF87171)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .background(levelColor.copy(alpha = 0.12f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(levelColor, CircleShape)
                    )
                    Text(
                        text = recommendation.thermalLevel.levelName,
                        color = levelColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Main Recommendation Headline (Clean typography, no nested bordered box)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bioclimatic_hyper_headline"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ESTRATEGIA RECOMENDADA",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val layerBadge = if (!recommendation.layerStrategy.isActive) {
                        if (recommendation.advancedMetrics.minTempPeriod >= 18.0) "Manga corta" else "Capa única"
                    } else {
                        "${recommendation.layerStrategy.totalLayers} Capas" +
                                if (recommendation.layerStrategy.isThermalOscillationHigh) " (Oscilación)" else ""
                    }

                    Text(
                        text = layerBadge,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = recommendation.headline,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Subtle Linear Divider (1dp, 0.08f alpha)
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            // 3. Inercia Térmica Solar Directa vs. Sombra (Flat clean row)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val sunT = String.format(Locale.US, "%.1f", recommendation.advancedMetrics.solarPerceivedTemp)
                    val shadeT = String.format(Locale.US, "%.1f", recommendation.advancedMetrics.shadePerceivedTemp)
                    Text(
                        text = "Sol: ${sunT}°C · Sombra: ${shadeT}°C",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Solar Delta or special alert indicator
                val solarBoost = recommendation.advancedMetrics.solarBoostSun
                if (solarBoost >= 2.5) {
                    Text(
                        text = "+${String.format(Locale.US, "%.1f", solarBoost)}°C al sol",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (recommendation.metrics.isDampCold) {
                    Text(
                        text = "Frío calado (-2.2°C)",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (recommendation.metrics.isMuggyHeat) {
                    Text(
                        text = "Bochorno / Sudor lento",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Subtle Linear Divider
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            // 4. Calzado y Complementos Clave (Integrated flat rows)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Calzado
                FootwearRowFlat(footwear = recommendation.footwearPill)

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

            // Subtle Linear Divider
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            // 5. Línea de Tiempo Rápida (4 Hitos Diarios)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bioclimatic_4_milestones"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LÍNEA DE TIEMPO (PRENDA CLAVE)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    recommendation.advancedMetrics.sunsetTime?.let { sunset ->
                        Text(
                            text = "Ocaso: $sunset h",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 4 hitos en fila limpia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recommendation.milestones.forEach { milestone ->
                        MilestoneItemFlat(
                            milestone = milestone,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 6. Sistema de Capas Modulares (Método Cebolla 3 Capas)
            if (recommendation.layerStrategy.isActive) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    thickness = 1.dp
                )

                ModularLayersFlat(strategy = recommendation.layerStrategy)
            }

            // 7. Toggle para detalles extendidos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedDetails = !expandedDetails }
                    .padding(vertical = 4.dp)
                    .testTag("bioclimatic_details_toggle")
            ) {
                Text(
                    text = if (expandedDetails) "Ocultar desglose horario" else "Ver desglose horario y alertas",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expandedDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Complement Alerts
                    if (recommendation.complementAlerts.isNotEmpty()) {
                        recommendation.complementAlerts.forEach { alert ->
                            ComplementAlertDetailedFlat(alert = alert)
                        }
                    }

                    // 4 Franjas Horarias
                    if (recommendation.timeSlots.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            recommendation.timeSlots.forEach { slot ->
                                TimeSlotDetailFlat(
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
private fun FootwearRowFlat(footwear: DynamicFootwearPill) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = footwear.icon, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = footwear.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = footwear.reason,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ComplementMiniPill(comp: DynamicComplementPill) {
    val bgColor = if (comp.isCrucial) Color(0x35F59E0B) else Color.White.copy(alpha = 0.08f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = comp.icon, fontSize = 12.sp)
        Text(
            text = comp.label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MilestoneItemFlat(
    milestone: TimelineMilestone,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = milestone.icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = when (milestone.id) {
                    "SALIDA" -> "Salida"
                    "MEDIODIA" -> "Mediodía"
                    "TARDE" -> "Tarde"
                    else -> "Regreso"
                },
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${milestone.temp}°C",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = milestone.keyGarment,
            color = Color.White.copy(alpha = 0.70f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModularLayersFlat(strategy: ModularLayerStrategy) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Layers,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "MÉTODO CEBOLLA (3 CAPAS MODULARES)",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Capa 1: Base (Contacto)
        LayerItemFlat(
            layerNumber = "1",
            layerName = "Base (Contacto)",
            layerContent = strategy.baseLayer,
            icon = "🎽"
        )

        // Capa 2: Intermedia (Aislamiento)
        strategy.midLayer?.let { mid ->
            LayerItemFlat(
                layerNumber = "2",
                layerName = "Intermedia (Aislamiento)",
                layerContent = mid,
                icon = "🧶"
            )
        }

        // Capa 3: Exterior (Protección)
        strategy.outerLayer?.let { outer ->
            LayerItemFlat(
                layerNumber = "3",
                layerName = "Exterior (Protección)",
                layerContent = outer,
                icon = "🧥"
            )
        }

        // Transición horaria si oscilación > 7°C
        strategy.layerTransitionAdvice?.let { advice ->
            Text(
                text = "💡 $advice",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LayerItemFlat(
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
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = layerContent,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ComplementAlertDetailedFlat(alert: ComplementAlert) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = alert.icon, fontSize = 13.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                color = if (alert.isWarning) Color(0xFFFDE047) else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = alert.description,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TimeSlotDetailFlat(
    title: String,
    temp: Int,
    perceived: Int,
    advice: String,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = icon, fontSize = 14.sp)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${temp}°C (Sens. ${perceived}°)",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Text(
                text = advice,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
