package com.kiranawala.presentation.components.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kiranawala.presentation.theme.KiranaColors

/**
 * Glassmorphism Card Component
 * Creates a frosted glass effect with blur and translucent background
 * Following 2025 design trends for depth and hierarchy
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 4.dp,
    glassOpacity: Float = 0.7f,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == KiranaColors.BackgroundDark
    
    val glassColor = if (isDark) {
        KiranaColors.GlassDark
    } else {
        KiranaColors.GlassLight
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = glassColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * Gradient Overlay for hero images
 * Creates smooth transition from image to content
 */
@Composable
fun GradientOverlay(
    modifier: Modifier = Modifier,
    startColor: Color = Color.Transparent,
    endColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                )
            )
    )
}

/**
 * Multi-layer shadow effect for elevated cards
 * Mimics Apple's design system shadow layers
 */
fun Modifier.multiLayerShadow(
    isDark: Boolean = false
): Modifier = this.then(
    if (isDark) {
        Modifier
            // Colored glow effect for dark mode
            .background(
                color = KiranaColors.Primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
    } else {
        Modifier
    }
)
