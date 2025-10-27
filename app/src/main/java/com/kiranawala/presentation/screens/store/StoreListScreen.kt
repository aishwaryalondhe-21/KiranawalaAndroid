package com.kiranawala.presentation.screens.store

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kiranawala.domain.models.Store
import com.kiranawala.presentation.components.LocationHeader
import com.kiranawala.presentation.components.modern.ModernSearchBar
import com.kiranawala.presentation.components.modern.ImageStoreCard
import com.kiranawala.presentation.viewmodels.AddressViewModel
import com.kiranawala.presentation.viewmodels.StoreListUiState
import com.kiranawala.presentation.viewmodels.StoreListViewModel
import com.kiranawala.utils.LocationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun StoreListScreen(
    onStoreClick: (String) -> Unit,
    onReviewsClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onOrderHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    viewModel: StoreListViewModel = hiltViewModel(),
    addressViewModel: AddressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Location state
    val currentLocation by addressViewModel.currentLocation.collectAsState()
    val isDetectingLocation by addressViewModel.isDetectingLocation.collectAsState()
    val context = LocalContext.current

    // Snackbar state for gesture feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Auto-detect location on first launch
    LaunchedEffect(Unit) {
        // First try to initialize from default address
        addressViewModel.initializeCurrentLocationFromDefaultAddress()
        
        // If no location set and permissions granted, detect GPS location
        if (currentLocation == null && LocationUtils.hasLocationPermission(context)) {
            addressViewModel.setLocationDetecting(true)
            val gpsLocation = LocationUtils.getCurrentLocation(context)
            if (gpsLocation != null) {
                val locationAddress = LocationUtils.reverseGeocode(
                    context,
                    gpsLocation.latitude,
                    gpsLocation.longitude
                )
                if (locationAddress != null) {
                    addressViewModel.setCurrentLocation(locationAddress)
                }
            }
            addressViewModel.setLocationDetecting(false)
        }
    }

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refresh()
            // Reset after a delay (ViewModel will update state)
            scope.launch {
                kotlinx.coroutines.delay(1000)
                isRefreshing = false
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nearby Stores",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onOrderHistoryClick) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Order History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Location Header
                LocationHeader(
                    currentLocation = currentLocation,
                    isLoading = isDetectingLocation,
                    onLocationClick = onLocationClick
                )

                // Modern Search Bar
                ModernSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchStores(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                // Content based on state
                when (uiState) {
                    is StoreListUiState.Loading -> {
                        LoadingContent()
                    }
                    is StoreListUiState.Empty -> {
                        EmptyContent(onRefresh = { viewModel.refresh() })
                    }
                    is StoreListUiState.Success -> {
                        val stores = (uiState as StoreListUiState.Success).stores
                        StoreList(
                            stores = stores,
                            onStoreClick = onStoreClick,
                            onReviewsClick = onReviewsClick,
                            onRefresh = { viewModel.refresh() },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    is StoreListUiState.Error -> {
                        val message = (uiState as StoreListUiState.Error).message
                        ErrorContent(
                            message = message,
                            onRetry = { viewModel.refresh() }
                        )
                    }
                }
            }

            // Pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Old SearchBar removed - using ModernSearchBar instead

@Composable
fun StoreList(
    stores: List<Store>,
    onStoreClick: (String) -> Unit,
    onReviewsClick: (String) -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Progressive disclosure state
    var isExpanded by remember { mutableStateOf(false) }
    val displayedStores = if (isExpanded || stores.size <= 3) stores else stores.take(3)
    val hasMore = stores.size > 3

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(
            items = displayedStores,
            key = { _, store -> store.id }
        ) { index, store ->
            // Calculate parallax offset based on item position
            val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            val parallaxOffset = itemInfo?.let {
                // Calculate offset relative to viewport center
                val itemCenter = it.offset + (it.size / 2f)
                val viewportCenter = listState.layoutInfo.viewportEndOffset / 2f
                (itemCenter - viewportCenter) * 0.3f // Parallax factor
            } ?: 0f

            // Use image-based card - Zomato/Swiggy style
            ImageStoreCard(
                store = store,
                onClick = { onStoreClick(store.id) }
            )
        }

        // Show More / Show Less Button (Progressive Disclosure)
        if (hasMore) {
            item {
                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpanded)
                            "Show Less"
                        else
                            "Show ${stores.size - 3} More Stores",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Duplicate StoreCard removed - using SwipeableStoreCard instead

@Composable
fun LoadingContent() {
    // Use animated loading state with bouncing dots
    com.kiranawala.presentation.components.modern.AnimatedLoadingState(
        message = "Finding fresh stores near you..."
    )
}

@Composable
fun EmptyContent(onRefresh: () -> Unit) {
    // Use animated empty state
    com.kiranawala.presentation.components.modern.AnimatedEmptyState(
        icon = Icons.Default.Store,
        title = "No fresh finds yet",
        subtitle = "Lost in the aisles! Try refreshing or check your location.",
        actionText = "Refresh",
        onAction = onRefresh
    )
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    // Use animated error state
    com.kiranawala.presentation.components.modern.AnimatedErrorState(
        errorMessage = "Lost in the aisles!",
        subtitle = message,
        onRetry = onRetry
    )
}
