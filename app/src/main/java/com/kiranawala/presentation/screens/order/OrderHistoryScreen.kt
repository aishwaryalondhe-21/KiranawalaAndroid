package com.kiranawala.presentation.screens.order

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranawala.domain.models.Order
import com.kiranawala.presentation.components.modern.*
import com.kiranawala.presentation.theme.KiranaColors
import com.kiranawala.presentation.viewmodels.OrderHistoryState
import com.kiranawala.presentation.viewmodels.OrderHistoryViewModel
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    customerId: String,
    onOrderClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val orderHistoryState by viewModel.orderHistoryState.collectAsState()
    
    LaunchedEffect(customerId) {
        viewModel.loadOrderHistory(customerId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = orderHistoryState) {
                is OrderHistoryState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        GroceryCartLoadingAnimation(
                            message = "Fetching your past orders...",
                            primaryColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is OrderHistoryState.Empty -> {
                    ModernEmptyState(
                        icon = Icons.Default.Receipt,
                        title = "No Orders Yet",
                        description = "Your order history will appear here once you place your first order",
                        modifier = Modifier.fillMaxSize(),
                        actionText = "Start Shopping",
                        onActionClick = onBackClick
                    )
                }
                is OrderHistoryState.Success -> {
                    ModernOrderHistoryContent(
                        orders = state.orders,
                        modifier = Modifier.fillMaxSize(),
                        onOrderClick = onOrderClick,
                        onRefresh = { viewModel.loadOrderHistory(customerId) }
                    )
                }
                is OrderHistoryState.Error -> {
                    ModernEmptyState(
                        icon = Icons.Default.Error,
                        title = "Failed to Load Orders",
                        description = state.message,
                        modifier = Modifier.fillMaxSize(),
                        actionText = "Retry",
                        onActionClick = { viewModel.loadOrderHistory(customerId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernOrderHistoryContent(
    orders: List<Order>,
    modifier: Modifier = Modifier,
    onOrderClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var expandedOrderId by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModernSectionHeader(
                title = "${orders.size} Orders",
                actionText = "Refresh",
                onActionClick = onRefresh
            )
        }

        items(orders, key = { it.id }) { order ->
            ModernExpandableOrderCard(
                order = order,
                isExpanded = expandedOrderId == order.id,
                onExpandChange = { expanded ->
                    expandedOrderId = if (expanded) order.id else null
                },
                onClick = { onOrderClick(order.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModernExpandableOrderCard(
    order: Order,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == KiranaColors.BackgroundDark
    
    ExpandableCard(
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        header = {
            Column {
                // Header Row with Store Name and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.storeName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Tag,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "#${order.id.take(8)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    ModernOrderStatusChip(status = order.status, isDark = isDark)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${order.items.size} items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = order.createdAt.toString().split("T")[0],
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "₹${String.format("%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        expandedContent = {
            Column {
                // Order Items List
                Text(
                    text = "Order Items",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "×${item.quantity}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Text(
                                text = item.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "₹${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // View Details Button
                ModernActionButton(
                    text = "View Full Details",
                    icon = Icons.Default.ArrowForward,
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun ModernOrderStatusChip(status: String, isDark: Boolean) {
    val (backgroundColor, textColor, icon) = when (status.uppercase()) {
        "PENDING" -> Triple(
            if (isDark) KiranaColors.SecondaryDark.copy(alpha = 0.3f) else KiranaColors.SecondaryLight.copy(alpha = 0.2f),
            if (isDark) KiranaColors.SecondaryDark else KiranaColors.SecondaryLight,
            Icons.Default.Schedule
        )
        "PROCESSING" -> Triple(
            if (isDark) KiranaColors.PrimaryDark.copy(alpha = 0.3f) else KiranaColors.PrimaryLight.copy(alpha = 0.2f),
            if (isDark) KiranaColors.PrimaryDark else KiranaColors.PrimaryLight,
            Icons.Default.Autorenew
        )
        "COMPLETED" -> Triple(
            KiranaColors.Success.copy(alpha = 0.2f),
            KiranaColors.Success,
            Icons.Default.CheckCircle
        )
        "CANCELLED" -> Triple(
            KiranaColors.Error.copy(alpha = 0.2f),
            KiranaColors.Error,
            Icons.Default.Cancel
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Help
        )
    }
    
    ModernStatusChip(
        text = status.lowercase().replaceFirstChar { it.uppercase() },
        icon = icon,
        backgroundColor = backgroundColor,
        contentColor = textColor
    )
}

