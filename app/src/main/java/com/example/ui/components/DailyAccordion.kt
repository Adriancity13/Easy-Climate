package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
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

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
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
                text = "Toca un día para ver sus 24h",
                color = Color(0xBFFFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Glass Accordion Container
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                dailyItems.forEachIndexed { index, item ->
                    val isExpanded = expandedIndex == index

                    DailyAccordionRow(
                        item = item,
                        index = index,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedIndex = if (isExpanded) null else index
                        }
                    )

                    if (index < dailyItems.size - 1) {
                        HorizontalDivider(
                            color = Color(0x33FFFFFF),
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
        animationSpec = tween(250),
        label = "chevronRotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
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
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(65.dp)
            )

            // Icon
            WeatherConditionIcon(
                iconType = item.iconType,
                size = 22.dp,
                tint = Color.White
            )

            // Description
            Text(
                text = item.description,
                color = Color(0xEEFFFFFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

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
                    color = Color(0xCCFFFFFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Rotating Chevron
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Expandir día",
                tint = Color(0xCCFFFFFF),
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
                    .rotate(rotationAngle)
            )
        }

        // Expanded Body
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(250)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 24-hour horizontal evolution strip
                if (item.hourlyList.isNotEmpty()) {
                    Column {
                        Text(
                            text = "EVOLUCIÓN DE LAS 24 HORAS",
                            color = Color(0xEEFFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(item.hourlyList) { hourItem ->
                                val hIconType = WeatherUtils.getIconType(hourItem.weatherCode, hourItem.isDay)

                                Box(
                                    modifier = Modifier
                                        .width(58.dp)
                                        .background(Color(0x500F172A), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = hourItem.label,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
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
                                            color = if (hourItem.rainProb > 0) Color(0xFF93C5FD) else Color(0x88FFFFFF),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Sunrise & Sunset for this day
                if (item.sunrise.isNotBlank() && item.sunset.isNotBlank() && item.sunrise != "--:--") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x500F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    size = 18.dp,
                                    tint = Color(0xFFFDE047)
                                )
                                Column {
                                    Text(
                                        text = "Amanecer",
                                        color = Color(0xCCFFFFFF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
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
                                    color = Color(0xFFFDE68A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Atardecer
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                WeatherConditionIcon(
                                    iconType = "sunset",
                                    size = 18.dp,
                                    tint = Color(0xFFFB923C)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Atardecer",
                                        color = Color(0xCCFFFFFF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
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
                }

                // 3. Recommendation for this day (left untouched as requested)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RecommendationCardBg, RoundedCornerShape(14.dp))
                        .border(1.dp, RecommendationCardBorder, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECOMENDACIÓN PARA ESTE DÍA",
                                color = Color(0xFFFDE68A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = item.advice.baseAdvice,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 15.sp
                        )

                        // Modifiers
                        item.advice.modifiers.forEach { mod ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RecommendationPillBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, RecommendationPillBorder, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = mod.icon, fontSize = 13.sp)
                                    Column {
                                        Text(
                                            text = "${mod.title}:",
                                            color = Color(0xFFFDE047),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = mod.text,
                                            color = Color(0xEEFFFFFF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Day Metrics (3 pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lluvia
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x500F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lluvia", color = Color(0xCCFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text("${item.rainProb}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Viento
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x500F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Viento", color = Color(0xCCFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text("${item.windSpeed} km/h", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Sensación máx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x500F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sensación máx", color = Color(0xCCFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text("${item.feelsLikeMax}°", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
