package com.kiranawala.presentation.components.modern

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kiranawala.domain.models.Store
import com.kiranawala.presentation.theme.KiranaColors
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Swipeable Store Card with Gesture Interactions
 * 
 * Gestures:
 * - Swipe Right → Quick Order (Green)
 * - Swipe Left → Add to Favorites (Amber)
 * - Long Press → Show Preview Menu
 * 
 * Features:
 * - Haptic feedback on threshold
 * - Spring-based animations
 * - Visual feedback icons
 * - Smooth return animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableStoreCard(
    store: Store,
    onClick: () -> Unit,
    onRatingClick: () -> Unit,
    onQuickOrder: () -> Unit = {},
    onAddToFavorites: () -> Unit = {},
    onLongPress: () -> Unit = {},
    parallaxOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isSwipeTriggered by remember { mutableStateOf(false) }
    var swipeDirection by remember { mutableStateOf<SwipeDirection?>(null) }
    
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val isDark = MaterialTheme.colorScheme.background == KiranaColors.BackgroundDark
    
    // Swipe threshold (40% of card width)
    val swipeThreshold = 200f
    
    // Animate back to center when released
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isSwipeTriggered) 0f else offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = {
            if (isSwipeTriggered) {
                isSwipeTriggered = false
                offsetX = 0f
                swipeDirection = null
            }
        },
        label = "swipe-animation"
    )
    
    // Scale animation for press effect
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press-scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Background action indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left swipe indicator (Favorites - Amber)
            SwipeActionIndicator(
                icon = Icons.Default.Favorite,
                label = "Favorite",
                color = KiranaColors.Secondary,
                isVisible = offsetX < -50f,
                alpha = (offsetX.absoluteValue / swipeThreshold).coerceIn(0f, 1f)
            )
            
            // Right swipe indicator (Quick Order - Green)
            SwipeActionIndicator(
                icon = Icons.Default.ShoppingCart,
                label = "Quick Order",
                color = KiranaColors.Primary,
                isVisible = offsetX > 50f,
                alpha = (offsetX / swipeThreshold).coerceIn(0f, 1f)
            )
        }
        
        // Main Card with swipe gesture
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .scale(scale)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isPressed = true
                        },
                        onDragEnd = {
                            isPressed = false
                            
                            // Trigger action if threshold exceeded
                            when {
                                offsetX > swipeThreshold -> {
                                    // Quick Order (Right Swipe)
                                    isSwipeTriggered = true
                                    swipeDirection = SwipeDirection.RIGHT
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onQuickOrder()
                                }
                                offsetX < -swipeThreshold -> {
                                    // Add to Favorites (Left Swipe)
                                    isSwipeTriggered = true
                                    swipeDirection = SwipeDirection.LEFT
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onAddToFavorites()
                                }
                                else -> {
                                    // Return to center
                                    isSwipeTriggered = true
                                }
                            }
                        },
                        onDragCancel = {
                            isPressed = false
                            isSwipeTriggered = true
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-300f, 300f)
                            
                            // Haptic feedback at threshold
                            if ((offsetX > swipeThreshold || offsetX < -swipeThreshold) && 
                                !isSwipeTriggered) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            }
                        }
                    )
                },
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) {
                    KiranaColors.SurfaceDark
                } else {
                    KiranaColors.SurfaceLight
                }
            )
        ) {
            StoreCardContent(
                store = store,
                onRatingClick = onRatingClick,
                isDark = isDark,
                parallaxOffset = parallaxOffset
            )
        }
    }
}

/**
 * Store Card Content with Hero Image and Glassmorphism
 * Following 2025 Bento Grid Design System
 */
@Composable
private fun StoreCardContent(
    store: Store,
    onRatingClick: () -> Unit,
    isDark: Boolean,
    parallaxOffset: Float = 0f
) {
    val deliveryTimeColor = when {
        store.estimatedDeliveryTime < 30 -> KiranaColors.SpeedFast
        store.estimatedDeliveryTime <= 45 -> KiranaColors.SpeedMedium
        else -> KiranaColors.SpeedSlow
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Hero Image Section with Glassmorphism & Parallax (160dp height)
            if (!store.imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // Background Hero Image with blur and parallax effect
                    AsyncImage(
                        model = store.imageUrl,
                        contentDescription = "${store.name} hero image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // Parallax effect: move image slower than scroll
                                translationY = parallaxOffset * 0.5f
                            }
                            .blur(20.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient Overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        if (isDark)
                                            KiranaColors.SurfaceDark.copy(alpha = 0.95f)
                                        else
                                            KiranaColors.SurfaceLight.copy(alpha = 0.95f)
                                    ),
                                    startY = 0f,
                                    endY = 400f
                                )
                            )
                    )

                    // Floating Rating Badge (Absolute positioned)
                    Surface(
                        onClick = onRatingClick,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .defaultMinSize(minWidth = 60.dp, minHeight = 36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = store.rating.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Store Name
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp  // Larger, bolder as per requirements
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Address with Distance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = store.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    store.distance?.let { distance ->
                        Text(
                            text = "• %.1f km".format(distance),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Social Proof Badges
                if (store.currentOrders > 0 || store.isFeatured) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (store.currentOrders > 0) {
                            LiveOrdersBadge(currentOrders = store.currentOrders)
                        }
                        if (store.isFeatured) {
                            FeaturedBadge()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Row - Icon-First Design
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ModernInfoChipCompact(
                        icon = Icons.Default.ShoppingBag,
                        label = "Min",
                        value = "₹${store.minimumOrderValue.roundToInt()}",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    ModernInfoChipCompact(
                        icon = Icons.Default.DeliveryDining,
                        label = "Fee",
                        value = "₹${store.deliveryFee.roundToInt()}",
                        tint = KiranaColors.Secondary
                    )

                    ModernInfoChipCompact(
                        icon = Icons.Default.AccessTime,
                        label = "Time",
                        value = "${store.estimatedDeliveryTime}m",
                        tint = deliveryTimeColor
                    )
                }

                // Closed Status
                if (!store.isOpen) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Currently Closed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Swipe Action Indicator (appears behind card)
 */
@Composable
private fun SwipeActionIndicator(
    icon: ImageVector,
    label: String,
    color: Color,
    isVisible: Boolean,
    alpha: Float
) {
    if (isVisible) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Compact Info Chip for swipeable card
 */
@Composable
private fun ModernInfoChipCompact(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Swipe Direction Enum
 */
private enum class SwipeDirection {
    LEFT, RIGHT
}
