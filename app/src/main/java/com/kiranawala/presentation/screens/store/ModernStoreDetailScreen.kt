package com.kiranawala.presentation.screens.store

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Store
import com.kiranawala.presentation.theme.KiranaColors
import kotlin.math.roundToInt

/**
 * Modern Store Detail Screen - Blinkit/Zepto/Swiggy Instamart Style
 * Fully scrollable with banner, store info, search, categories, and products
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernStoreDetailContent(
    store: Store,
    products: List<Product>,
    categories: List<String>,
    searchQuery: String,
    selectedCategory: String?,
    onSearchChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onClearFilter: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onProductClick: (String) -> Unit,
    onReviewsClick: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == KiranaColors.BackgroundDark
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Banner Image Section
        item {
            StoreBannerSection(store = store, isDark = isDark)
        }
        
        // Store Info Card (Scrollable)
        item {
            StoreInfoCard(
                store = store,
                isDark = isDark,
                onReviewsClick = onReviewsClick
            )
        }
        
        // Search Bar
        item {
            ModernSearchSection(
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        
        // Category Chips
        item {
            CategoryChipsSection(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                onClearFilter = onClearFilter,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        // Products Grid
        items(products) { product ->
            ModernProductCard(
                product = product,
                onProductClick = { onProductClick(product.id) },
                onAddToCart = { onAddToCart(product) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        
        // Empty state
        if (products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Store Banner Section - Top scrollable image
 */
@Composable
private fun StoreBannerSection(
    store: Store,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (!store.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = store.imageUrl,
                contentDescription = "${store.name} banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isDark) Color.Black.copy(alpha = 0.7f) 
                                else Color.Black.copy(alpha = 0.4f)
                            ),
                            startY = 0f,
                            endY = 600f
                        )
                    )
            )
        } else {
            // Fallback gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            }
        }
        
        // Closed overlay if needed
        if (!store.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = "CLOSED NOW",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Store Info Card - Scrollable with the content
 */
@Composable
private fun StoreInfoCard(
    store: Store,
    isDark: Boolean,
    onReviewsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Store name and rating row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Rating Badge (Clickable)
                Surface(
                    onClick = onReviewsClick,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = store.rating.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "(${store.totalReviews})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Status chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (store.isOpen) 
                    if (isDark) KiranaColors.PrimaryDark.copy(alpha = 0.15f)
                    else KiranaColors.PrimaryLight.copy(alpha = 0.15f)
                else 
                    MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (store.isOpen) 
                                    if (isDark) KiranaColors.PrimaryDark else KiranaColors.PrimaryLight
                                else 
                                    MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (store.isOpen) "Open Now" else "Closed",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (store.isOpen) 
                            if (isDark) KiranaColors.PrimaryDark else KiranaColors.PrimaryLight
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Address
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = store.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Divider
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            // Delivery Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DeliveryInfoItem(
                    icon = Icons.Outlined.ShoppingBag,
                    label = "Min Order",
                    value = "₹${store.minimumOrderValue.roundToInt()}"
                )
                
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                DeliveryInfoItem(
                    icon = Icons.Outlined.LocalShipping,
                    label = "Delivery Fee",
                    value = if (store.deliveryFee == 0.0) "FREE" else "₹${store.deliveryFee.roundToInt()}"
                )
                
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                DeliveryInfoItem(
                    icon = Icons.Outlined.Schedule,
                    label = "Delivery Time",
                    value = "${store.estimatedDeliveryTime} min"
                )
            }
        }
    }
}

@Composable
private fun DeliveryInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Modern Search Section
 */
@Composable
private fun ModernSearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        placeholder = {
            Text(
                text = "Search products...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        singleLine = true
    )
}

/**
 * Category Chips Section
 */
@Composable
private fun CategoryChipsSection(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == KiranaColors.BackgroundDark
    
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Clear filter chip
        if (selectedCategory != null) {
            item {
                FilterChip(
                    selected = false,
                    onClick = onClearFilter,
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Clear")
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
        
        items(categories) { category ->
            val isSelected = category == selectedCategory
            
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDark) 
                        KiranaColors.PrimaryDark.copy(alpha = 0.9f)
                    else 
                        KiranaColors.PrimaryLight.copy(alpha = 0.9f),
                    selectedLabelColor = Color.White
                ),
                elevation = FilterChipDefaults.filterChipElevation(
                    elevation = 2.dp,
                    pressedElevation = 4.dp
                )
            )
        }
    }
}

/**
 * Modern Product Card - Compact with image left, info center, action right
 */
@Composable
private fun ModernProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onProductClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            if (!product.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!product.description.isNullOrEmpty()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "₹${product.price}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Add Button
            FloatingActionButton(
                onClick = onAddToCart,
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to cart",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
