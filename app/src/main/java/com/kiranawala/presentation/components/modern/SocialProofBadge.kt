package com.kiranawala.presentation.components.modern

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiranawala.presentation.theme.KiranaColors

/**
 * Social Proof Badge - "X people ordering now"
 * With animated pulse dot
 */
@Composable
fun LiveOrdersBadge(
    currentOrders: Int,
    modifier: Modifier = Modifier
) {
    if (currentOrders > 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse-scale"
        )
        
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            color = KiranaColors.Success.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(scale)
                        .background(KiranaColors.Success, CircleShape)
                )
                Text(
                    text = "$currentOrders ordering now",
                    style = MaterialTheme.typography.labelSmall,
                    color = KiranaColors.Success,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Featured Badge - "Most ordered this week"
 */
@Composable
fun FeaturedBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = KiranaColors.Secondary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = KiranaColors.Secondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Trending",
                style = MaterialTheme.typography.labelSmall,
                color = KiranaColors.Secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Distance Badge
 */
@Composable
fun DistanceBadge(
    distance: Double,
    modifier: Modifier = Modifier
) {
    Text(
        text = "%.1f km away".format(distance),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
