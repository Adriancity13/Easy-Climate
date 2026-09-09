package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DailyItem
import com.example.utils.WeatherUtils

@Composable
fun DailyForecastAccordionList(
    dailyItems: List<DailyItem>,
    modifier: Modifier = Modifier
) {
    if (dailyItems.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header (Clean typography)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximos 7 días",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toca para ver sus 24h",
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Unified Glass Container for all 7 days with subtle linear dividers
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                dailyItems.forEachIndexed { index, item ->
                    val isExpanded = expandedIndex == index

                    DailyAccordionRow(
                        item = item,
                        index = index,
                        isExpanded = isExpanded,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            expandedIndex = if (isExpanded) null else index
                        }
                    )

                    if (index < dailyItems.size - 1) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyAccordionRow(
    item: DailyItem,
    index: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(200),
        label = "chevronRotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day Row Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("daily_accordion_header_$index"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day Name
            Text(
                text = item.dayName,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(64.dp)
            )

            // Clothing & Weather Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.clothingIcon,
                    fontSize = 15.sp
                )
                WeatherConditionIcon(
                    iconType = item.iconType,
                    size = 18.dp,
                    tint = Color.White
                )
            }

            // Description / Technical Summary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (item.technicalSummary.isNotBlank()) item.technicalSummary else item.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                if (item.bioclimaticAlert != null) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x35EF4444), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.bioclimaticAlert,
                            color = Color(0xFFFCA5A5),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Max / Min Temp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.maxTemp}°",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.minTemp}°",
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Rotating Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Expandir día",
                tint = Color.White.copy(alpha = 0.60f),
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 2.dp)
                    .rotate(rotationAngle)
            )
        }

        // Expanded Body (Clean negative space, no multi-layered nested boxes)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. 24-hour horizontal evolution strip
                if (item.hourlyList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "EVOLUCIÓN DE LAS 24 HORAS",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(item.hourlyList) { hourItem ->
                                val hIconType = WeatherUtils.getIconType(hourItem.weatherCode, hourItem.isDay)

                                Column(
                                    modifier = Modifier
                                        .width(56.dp)
                                        .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = hourItem.label,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    WeatherConditionIcon(
                                        iconType = hIconType,
                                        size = 18.dp,
                                        tint = Color.White
                                    )

                                    Text(
                                        text = "${hourItem.temp}°",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = if (hourItem.rainProb > 0) "${hourItem.rainProb}%" else "·",
                                        color = if (hourItem.rainProb > 0) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.40f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Sunrise & Sunset for this day
                if (item.sunrise.isNotBlank() && item.sunset.isNotBlank() && item.sunrise != "--:--") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Amanecer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WeatherConditionIcon(
                                iconType = "sunrise",
                                size = 16.dp,
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                            Column {
                                Text(
                                    text = "Amanecer",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = item.sunrise,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Daylight Duration
                        if (item.daylightDuration.isNotBlank()) {
                            Text(
                                text = "☀️ ${item.daylightDuration} sol",
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Atardecer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WeatherConditionIcon(
                                iconType = "sunset",
                                size = 16.dp,
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Atardecer",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = item.sunset,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. Recommendation for this day (Clean layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "RECOMENDACIÓN PARA ESTE DÍA",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = item.advice.baseAdvice,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 15.sp
                    )

                    // Modifiers
                    item.advice.modifiers.forEach { mod ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(text = mod.icon, fontSize = 12.sp)
                            Column {
                                Text(
                                    text = "${mod.title}:",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = mod.text,
                                    color = Color.White.copy(alpha = 0.70f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // 4. Day Metrics (3 flat pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Lluvia
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Lluvia", color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp, fontWeight = FontWeight.Normal)
                        Text("${item.rainProb}%", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Viento
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Viento", color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp, fontWeight = FontWeight.Normal)
                        Text("${item.windSpeed} km/h", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sensación máx
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Sensación", color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp, fontWeight = FontWeight.Normal)
                        Text("${item.feelsLikeMax}°", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
