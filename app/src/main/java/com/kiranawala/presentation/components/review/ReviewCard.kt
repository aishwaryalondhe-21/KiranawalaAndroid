package com.kiranawala.presentation.components.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kiranawala.domain.models.StoreReview
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Card component for displaying a single review
 */
@Composable
fun ReviewCard(
    review: StoreReview,
    currentUserId: String?,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOwnReview = currentUserId == review.customerId
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar, Name, Rating, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customer Avatar (first letter)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.customerName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // Customer Name
                        Text(
                            text = review.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Time ago
                        Text(
                            text = getRelativeTimeString(review.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Delete button (only for own reviews)
                if (isOwnReview) {
                    IconButton(
                        onClick = { onDeleteClick(review.id) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete review",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Star Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (index < review.rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            // Comment (if exists)
            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Get relative time string (e.g., "2 days ago", "5 hours ago")
 */
private fun getRelativeTimeString(dateTime: kotlinx.datetime.LocalDateTime): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    // Simple approximation - convert to epoch seconds for comparison
    // Note: This is a simplified version, consider using a proper date library for production
    val reviewDate = dateTime.toString()
    val currentDate = now.toString()
    
    // Parse basic comparison (simplified)
    return when {
        reviewDate == currentDate -> "Just now"
        else -> "Recently"
    }
    
    // TODO: Implement proper relative time calculation using kotlinx-datetime
    // For now, showing "Recently" for all reviews
    // Production should calculate actual time difference
}
