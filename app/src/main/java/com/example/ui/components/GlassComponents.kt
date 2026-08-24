package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color tokens for glassmorphism
val GlassColor = Color(0x600F172A)
val GlassStrongColor = Color(0x720F172A)
val GlassPillColor = Color(0x520F172A)
val GlassBorderColor = Color(0x45FFFFFF)
val GlassStrongBorderColor = Color(0x55FFFFFF)
val RecommendationCardBg = Color(0x7A0F172A)
val RecommendationCardBorder = Color(0x55FFFFFF)
val RecommendationPillBg = Color(0x660F172A)
val RecommendationPillBorder = Color(0x38FFFFFF)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape, spotColor = Color(0x30000000), ambientColor = Color(0x18000000))
            .background(color = GlassColor, shape = shape)
            .border(width = 1.dp, color = GlassBorderColor, shape = shape)
            .clip(shape)
    ) {
        content()
    }
}

@Composable
fun GlassStrongCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = shape, spotColor = Color(0x38000000), ambientColor = Color(0x20000000))
            .background(color = GlassStrongColor, shape = shape)
            .border(width = 1.dp, color = GlassStrongBorderColor, shape = shape)
            .clip(shape)
    ) {
        content()
    }
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(999.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = GlassPillColor, shape = shape)
            .border(width = 1.dp, color = Color(0x4DFFFFFF), shape = shape)
            .clip(shape)
    ) {
        content()
    }
}

@Composable
fun LiveLocationPulse(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(12.dp * scale)
                .background(Color(0xFF34D399).copy(alpha = alpha), shape = CircleShape)
        )
        // Solid center dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(0xFF10B981), shape = CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}
