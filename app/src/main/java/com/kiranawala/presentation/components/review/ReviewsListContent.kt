package com.kiranawala.presentation.components.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kiranawala.domain.models.StoreReview
import com.kiranawala.presentation.viewmodels.ReviewsState
import com.kiranawala.presentation.viewmodels.ReviewsSortOption

/**
 * Content component for displaying reviews list
 * Handles loading, success, empty, and error states
 */
@Composable
fun ReviewsListContent(
    reviewsState: ReviewsState,
    sortOption: ReviewsSortOption,
    currentUserId: String?,
    onSortChange: (ReviewsSortOption) -> Unit,
    onAddReview: () -> Unit,
    onDeleteReview: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (reviewsState) {
            is ReviewsState.Loading -> {
                LoadingContent()
            }
            is ReviewsState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Sort Options
                    ReviewSortBar(
                        sortOption = sortOption,
                        onSortChange = onSortChange,
                        reviewCount = reviewsState.reviews.size
                    )
                    
                    // Reviews List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = reviewsState.reviews,
                            key = { review -> review.id }
                        ) { review ->
                            ReviewCard(
                                review = review,
                                currentUserId = currentUserId,
                                onDeleteClick = onDeleteReview
                            )
                        }
                    }
                }
            }
            is ReviewsState.Empty -> {
                EmptyReviewsContent(message = reviewsState.message)
            }
            is ReviewsState.Error -> {
                ErrorReviewsContent(
                    message = reviewsState.message,
                    onRetry = onRetry
                )
            }
        }
        
        // Floating Action Button for adding review
        FloatingActionButton(
            onClick = onAddReview,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Review"
            )
        }
    }
}

/**
 * Sort bar with review count and sort options
 */
@Composable
private fun ReviewSortBar(
    sortOption: ReviewsSortOption,
    onSortChange: (ReviewsSortOption) -> Unit,
    reviewCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Review count
            Text(
                text = "$reviewCount ${if (reviewCount == 1) "Review" else "Reviews"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Sort chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = sortOption == ReviewsSortOption.RECENT,
                    onClick = { onSortChange(ReviewsSortOption.RECENT) },
                    label = { Text("Recent") }
                )
                FilterChip(
                    selected = sortOption == ReviewsSortOption.TOP_RATED,
                    onClick = { onSortChange(ReviewsSortOption.TOP_RATED) },
                    label = { Text("Top Rated") }
                )
            }
        }
    }
}

/**
 * Loading state
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading reviews...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state
 */
@Composable
private fun EmptyReviewsContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap the + button to add a review",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state
 */
@Composable
private fun ErrorReviewsContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Failed to load reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
