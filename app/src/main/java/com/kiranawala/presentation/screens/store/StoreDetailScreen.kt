package com.kiranawala.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.kiranawala.utils.SessionManager
import com.kiranawala.di.SessionManagerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Store
import com.kiranawala.presentation.viewmodels.CartViewModel
import com.kiranawala.presentation.viewmodels.ProductsState
import com.kiranawala.presentation.viewmodels.StoreDetailViewModel
import com.kiranawala.presentation.viewmodels.StoreState
import com.kiranawala.presentation.viewmodels.ReviewsViewModel
import com.kiranawala.presentation.viewmodels.AddReviewState
import com.kiranawala.presentation.components.review.ReviewsListContent
import com.kiranawala.presentation.components.review.AddReviewDialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onReviewsClick: (String) -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    reviewsViewModel: ReviewsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(context, SessionManagerEntryPoint::class.java).sessionManager()
    }
    val storeState by viewModel.storeState.collectAsState()
    val productsState by viewModel.productsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val addToCartError by cartViewModel.addToCartError.collectAsState()
    
    // Reviews state
    val reviewsState by reviewsViewModel.reviewsState.collectAsState()
    
    // Quantity dialog state
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize cart with current user ID
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userId = sessionManager.getCurrentUserId()
            userId?.let { 
                cartViewModel.initialize(it)
            }
            
            // Load reviews for rating calculation
            (storeState as? StoreState.Success)?.let { state ->
                reviewsViewModel.loadReviews(state.store.id)
            }
        }
    }
    
    // Load reviews when store loads
    LaunchedEffect(storeState) {
        (storeState as? StoreState.Success)?.let { state ->
            reviewsViewModel.loadReviews(state.store.id)
        }
    }
    
    // Show error if adding to cart fails
    LaunchedEffect(addToCartError) {
        addToCartError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            cartViewModel.clearAddToCartError()
        }
    }
    
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (storeState) {
                        is StoreState.Success -> {
                            Text((storeState as StoreState.Success).store.name)
                        }
                        else -> Text("Store Details")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Badge {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Store Header
            when (storeState) {
                is StoreState.Success -> {
                    val store = (storeState as StoreState.Success).store
                    val reviews = (reviewsState as? com.kiranawala.presentation.viewmodels.ReviewsState.Success)?.reviews
                    StoreHeader(
                        store = store,
                        reviewCount = reviews?.size ?: 0,
                        averageRating = calculateAverageRating(reviews ?: emptyList()),
                        onRatingClick = { onReviewsClick(store.id) }
                    )
                }
                is StoreState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is StoreState.Error -> {
                    // Show error in header
                }
            }
            
            // Products Content
            ProductsTabContent(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.searchProducts(it) },
                selectedCategory = selectedCategory,
                categories = viewModel.getCategories(),
                onCategorySelected = { viewModel.filterByCategory(it) },
                onClearFilter = { viewModel.clearCategoryFilter() },
                productsState = productsState,
                onProductClick = onProductClick,
                onAddToCart = { product ->
                    selectedProduct = product
                    showQuantityDialog = true
                },
                onRetry = { viewModel.refresh() }
            )
        }
        
        
        // Quantity Dialog
        if (showQuantityDialog && selectedProduct != null) {
            val store = (storeState as? StoreState.Success)?.store
            QuantityDialog(
                product = selectedProduct!!,
                onDismiss = { showQuantityDialog = false },
                onConfirm = { quantity ->
                    if (store != null) {
                        cartViewModel.addToCart(store.id, selectedProduct!!, quantity)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${selectedProduct!!.name} added to cart",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    showQuantityDialog = false
                }
            )
        }
    }
}

@Composable
fun QuantityDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Cart") },
        text = {
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- }
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    IconButton(
                        onClick = { if (quantity < product.stockQuantity) quantity++ }
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
                
                Text(
                    text = "Total: ₹${product.price * quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity) }) {
                Text("Add to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StoreHeader(
    store: Store,
    reviewCount: Int,
    averageRating: Float,
    onRatingClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Store Name and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        onClick = onRatingClick,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (averageRating > 0) String.format("%.1f", averageRating) else store.rating.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "• $reviewCount ratings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Status Badge
                Surface(
                    color = if (store.isOpen) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (store.isOpen) "Open" else "Closed",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (store.isOpen) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Address
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = store.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Store Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StoreInfoItem(
                    icon = Icons.Default.ShoppingBag,
                    label = "Min Order",
                    value = "₹${store.minimumOrderValue.roundToInt()}"
                )
                StoreInfoItem(
                    icon = Icons.Default.DeliveryDining,
                    label = "Delivery",
                    value = "₹${store.deliveryFee.roundToInt()}"
                )
                StoreInfoItem(
                    icon = Icons.Default.AccessTime,
                    label = "Time",
                    value = "${store.estimatedDeliveryTime} min"
                )
            }
        }
    }
}

@Composable
fun StoreInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (selectedCategory != null) {
                TextButton(onClick = onClearFilter) {
                    Text("Clear")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

// Removed - ProductGrid logic moved to LazyColumn items

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Product Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${product.price}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.stockQuantity < 10) {
                            Text(
                                text = "Only ${product.stockQuantity} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Add to Cart Button - Bright and prominent
                    Button(
                        onClick = onAddToCart,
                        enabled = product.stockQuantity > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to cart",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProductsContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No products found",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ErrorProductsContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Failed to load products",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Calculate average rating from reviews list
fun calculateAverageRating(reviews: List<com.kiranawala.domain.models.StoreReview>): Float {
    if (reviews.isEmpty()) return 0f
    val sum = reviews.sumOf { it.rating }
    return sum.toFloat() / reviews.size
}

@Composable
fun ProductsTabContent(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onClearFilter: () -> Unit,
    productsState: ProductsState,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
        }
        
        // Category Filter
        item {
            if (categories.isNotEmpty()) {
                CategoryFilter(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    onClearFilter = onClearFilter
                )
            }
        }
        
        // Products
        when (productsState) {
            is ProductsState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is ProductsState.Empty -> {
                item {
                    EmptyProductsContent()
                }
            }
            is ProductsState.Success -> {
                // Product items directly in LazyColumn - NO nested Column
                items(productsState.products) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { onAddToCart(product) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            is ProductsState.Error -> {
                item {
                    ErrorProductsContent(
                        message = productsState.message,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}
