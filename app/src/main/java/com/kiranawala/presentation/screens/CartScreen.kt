package com.kiranawala.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranawala.domain.models.Cart
import com.kiranawala.domain.models.CartItem
import com.kiranawala.presentation.components.modern.*
import com.kiranawala.presentation.viewmodels.CartState
import com.kiranawala.presentation.viewmodels.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (Cart) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartState by viewModel.cartState.collectAsState()

    LaunchedEffect(customerId) {
        viewModel.initialize(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Shopping Cart",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (cartState is CartState.Success) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.Delete, "Clear Cart")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = cartState) {
                is CartState.Loading -> {
                    ModernEmptyState(
                        icon = Icons.Default.ShoppingCart,
                        title = "Loading Cart",
                        description = "Please wait while we load your cart",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is CartState.Empty -> {
                    ModernEmptyState(
                        icon = Icons.Default.ShoppingCart,
                        title = "Your Cart is Empty",
                        description = "Add items from stores to get started",
                        modifier = Modifier.fillMaxSize(),
                        actionText = "Browse Stores",
                        onActionClick = onNavigateBack
                    )
                }
                is CartState.Success -> {
                    CartContent(
                        cart = state.cart,
                        modifier = Modifier.fillMaxSize(),
                        onUpdateQuantity = { productId, quantity ->
                            viewModel.updateQuantity(productId, quantity)
                        },
                        onRemoveItem = { productId ->
                            viewModel.removeFromCart(productId)
                        },
                        onCheckout = {
                            onNavigateToCheckout(state.cart)
                        }
                    )
                }
                is CartState.Error -> {
                    ModernEmptyState(
                        icon = Icons.Default.Error,
                        title = "Failed to Load Cart",
                        description = state.message,
                        modifier = Modifier.fillMaxSize(),
                        actionText = "Retry",
                        onActionClick = { viewModel.initialize(customerId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartContent(
    cart: Cart,
    modifier: Modifier = Modifier,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Column(modifier = modifier) {
        // Store info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = cart.storeName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${cart.items.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cart items
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(cart.items) { item ->
                CartItemCard(
                    item = item,
                    onUpdateQuantity = onUpdateQuantity,
                    onRemove = onRemoveItem
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Order summary
        OrderSummaryCard(
            cart = cart,
            onCheckout = onCheckout
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${item.product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subtotal: ₹${item.subtotal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (item.quantity > 1) {
                                onUpdateQuantity(item.product.id, item.quantity - 1)
                            } else {
                                onRemove(item.product.id)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = {
                            onUpdateQuantity(item.product.id, item.quantity + 1)
                        }
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }

                // Remove button
                TextButton(onClick = { onRemove(item.product.id) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    cart: Cart,
    onCheckout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal")
                Text("₹${String.format("%.2f", cart.subtotal)}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery Fee")
                Text("₹${String.format("%.2f", cart.deliveryFee)}")
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%.2f", cart.total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!cart.meetsMinimumOrder) {
                Text(
                    text = "Minimum order: ₹${cart.minimumOrderValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth(),
                enabled = cart.meetsMinimumOrder
            ) {
                Text("Proceed to Checkout")
            }
        }
    }
}

@Composable
private fun EmptyCartView(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add items to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateBack) {
            Text("Browse Stores")
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
