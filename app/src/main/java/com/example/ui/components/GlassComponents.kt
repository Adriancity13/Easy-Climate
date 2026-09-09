package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Modern Clean Glassmorphism Design Tokens (Single-Layer architecture)
val GlassColor = Color(0x380F172A)
val GlassStrongColor = Color(0x480F172A)
val GlassPillColor = Color(0x300F172A)
val GlassBorderColor = Color(0x22FFFFFF)
val GlassStrongBorderColor = Color(0x30FFFFFF)
val SubduedDividerColor = Color(0x14FFFFFF) // Color.White.copy(alpha = 0.08f)

val RecommendationCardBg = Color(0x3D0F172A)
val RecommendationCardBorder = Color(0x28FFFFFF)
val RecommendationPillBg = Color(0x1AFFFFFF)
val RecommendationPillBorder = Color(0x18FFFFFF)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape, spotColor = Color(0x25000000), ambientColor = Color(0x10000000))
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
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = shape, spotColor = Color(0x30000000), ambientColor = Color(0x15000000))
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
            .border(width = 1.dp, color = GlassBorderColor, shape = shape)
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
                .size(9.dp)
                .background(Color(0xFF10B981), shape = CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}
