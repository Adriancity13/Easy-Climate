package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.HourlyItem
import com.example.utils.WeatherUtils
import kotlinx.coroutines.launch

@Composable
fun HourlyForecastCarousel(
    hourlyItems: List<HourlyItem>,
    modifier: Modifier = Modifier
) {
    if (hourlyItems.isEmpty()) return

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Derive scroll progress for timeline bar
    val totalItems = hourlyItems.size
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val firstVisibleScrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    val progressFraction by remember {
        derivedStateOf {
            if (totalItems <= 1) 0f
            else {
                val approxItemWidth = 200f
                val totalScrollable = (totalItems - 3).coerceAtLeast(1) * approxItemWidth
                val currentScroll = firstVisibleIndex * approxItemWidth + firstVisibleScrollOffset
                (currentScroll / totalScrollable).coerceIn(0f, 1f)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Próximas horas",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(Toca una hora para centrar)",
                    color = Color(0xFFFDE68A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Left / Right Scroll Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GlassPillColor, CircleShape)
                        .border(1.dp, Color(0x66FFFFFF), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                val target = (listState.firstVisibleItemIndex - 3).coerceAtLeast(0)
                                listState.animateScrollToItem(target)
                            }
                        }
                        .testTag("hourly_left_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Desplazar a la izquierda",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GlassPillColor, CircleShape)
                        .border(1.dp, Color(0x66FFFFFF), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                val target = (listState.firstVisibleItemIndex + 3).coerceAtMost(hourlyItems.size - 1)
                                listState.animateScrollToItem(target)
                            }
                        }
                        .testTag("hourly_right_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Desplazar a la derecha",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Row
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_scroll_list")
        ) {
            itemsIndexed(hourlyItems) { index, item ->
                val isSelected = selectedIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 0.98f,
                    animationSpec = tween(200),
                    label = "hourlyCardScale"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0x800F172A) else Color(0x480F172A),
                    animationSpec = tween(200),
                    label = "hourlyCardBg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFDE047) else Color(0x38FFFFFF),
                    animationSpec = tween(200),
                    label = "hourlyCardBorder"
                )

                val iconType = WeatherUtils.getIconType(item.weatherCode, item.isDay)

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .width(74.dp)
                        .shadow(
                            elevation = if (isSelected) 8.dp else 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = if (isSelected) Color(0x60000000) else Color(0x20000000)
                        )
                        .background(color = bgColor, shape = RoundedCornerShape(18.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedIndex = index
                            coroutineScope.launch {
                                val target = (index - 1).coerceAtLeast(0)
                                listState.animateScrollToItem(target)
                            }
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.label,
                            color = if (isSelected) Color(0xFFFDE047) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )

                        WeatherConditionIcon(
                            iconType = iconType,
                            size = if (isSelected) 24.dp else 22.dp,
                            tint = Color.White
                        )

                        Text(
                            text = "${item.temp}°",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = if (item.rainProb > 0) "${item.rainProb}%" else "·",
                            color = if (item.rainProb > 0) Color(0xFF93C5FD) else Color(0x88FFFFFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Timeline Track
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(999.dp))
            ) {
                // Thumb
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction.coerceIn(0.05f, 1f))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(12.dp)
                            .offset(x = 6.dp, y = (-4).dp)
                            .shadow(4.dp, CircleShape, spotColor = Color.White)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0x88000000), CircleShape)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ahora", color = Color(0xB3FFFFFF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("+12 horas", color = Color(0xB3FFFFFF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("+24 horas", color = Color(0xB3FFFFFF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
