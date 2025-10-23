package com.kiranawala.presentation.components.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialog for adding a new review
 * Allows user to select rating (1-5 stars) and optionally add a comment
 */
@Composable
fun AddReviewDialog(
    storeName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit,
    isLoading: Boolean = false
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val maxCommentLength = 500
    
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Rate $storeName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Share your experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Star Rating Selector
                Text(
                    text = "Your Rating",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "${index + 1} stars",
                                modifier = Modifier.size(36.dp),
                                tint = if (index < rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                if (rating > 0) {
                    Text(
                        text = when (rating) {
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Very Good"
                            5 -> "Excellent"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Comment TextField
                OutlinedTextField(
                    value = comment,
                    onValueChange = { 
                        if (it.length <= maxCommentLength) {
                            comment = it 
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Comment (Optional)") },
                    placeholder = { Text("Share details about your experience...") },
                    supportingText = {
                        Text(
                            text = "${comment.length}/$maxCommentLength",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (comment.length >= maxCommentLength) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.medium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    // Submit Button
                    Button(
                        onClick = {
                            val finalComment = comment.trim().takeIf { it.isNotBlank() }
                            onSubmit(rating, finalComment)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = rating > 0 && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}
