package com.kiranawala.presentation.components.modern

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kiranawala.presentation.theme.KiranaColors

/**
 * Shimmer effect for loading states
 * Modern skeleton screen instead of spinners
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-translation"
    )

    val shimmerColors = listOf(
        KiranaColors.ShimmerBase.copy(alpha = 0.9f),
        KiranaColors.ShimmerHighlight.copy(alpha = 0.3f),
        KiranaColors.ShimmerBase.copy(alpha = 0.9f)
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim - 200f, translateAnim - 200f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
}

/**
 * Shimmer placeholder for Store Card while loading
 */
@Composable
fun ShimmerStoreCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = KiranaColors.SurfaceLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Store name placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .width(200.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                ShimmerEffect(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            // Address placeholder
            ShimmerEffect(
                modifier = Modifier
                    .width(150.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info chips placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) {
                    ShimmerEffect(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

/**
 * Shimmer loading list - shows 3 placeholder cards
 */
@Composable
fun ShimmerLoadingList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            ShimmerStoreCard()
        }
    }
}
