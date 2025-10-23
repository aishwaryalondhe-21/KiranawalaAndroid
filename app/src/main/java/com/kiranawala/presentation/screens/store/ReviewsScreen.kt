package com.kiranawala.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.kiranawala.utils.SessionManager
import com.kiranawala.di.SessionManagerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.kiranawala.domain.models.Store
import com.kiranawala.presentation.viewmodels.StoreDetailViewModel
import com.kiranawala.presentation.viewmodels.StoreState
import com.kiranawala.presentation.viewmodels.ReviewsViewModel
import com.kiranawala.presentation.viewmodels.AddReviewState
import com.kiranawala.presentation.viewmodels.ReviewsState
import com.kiranawala.presentation.components.review.ReviewsListContent
import com.kiranawala.presentation.components.review.AddReviewDialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    onBackClick: () -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel(),
    reviewsViewModel: ReviewsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(context, SessionManagerEntryPoint::class.java).sessionManager()
    }
    
    val storeState by viewModel.storeState.collectAsState()
    val reviewsState by reviewsViewModel.reviewsState.collectAsState()
    val sortOption by reviewsViewModel.sortOption.collectAsState()
    val addReviewState by reviewsViewModel.addReviewState.collectAsState()
    
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var currentUserName by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize with current user ID
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userId = sessionManager.getCurrentUserId()
            userId?.let { 
                currentUserId = it
                currentUserName = "Customer" // TODO: Get from user profile
            }
            
            // Load reviews for this store
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
    
    // Handle add review success
    LaunchedEffect(addReviewState) {
        when (addReviewState) {
            is AddReviewState.Success -> {
                showAddReviewDialog = false
                snackbarHostState.showSnackbar(
                    message = "Review added successfully!",
                    duration = SnackbarDuration.Short
                )
                reviewsViewModel.clearAddReviewState()
            }
            is AddReviewState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (addReviewState as AddReviewState.Error).message,
                    duration = SnackbarDuration.Long
                )
                reviewsViewModel.clearAddReviewState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (storeState) {
                            is StoreState.Success -> "Reviews - ${(storeState as StoreState.Success).store.name}"
                            else -> "Reviews"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentUserId != null) {
                FloatingActionButton(
                    onClick = { showAddReviewDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = "Add Review"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (reviewsState) {
                is ReviewsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ReviewsState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reviews yet. Be the first to review!")
                    }
                }
                is ReviewsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text((reviewsState as ReviewsState.Error).message)
                            Button(onClick = { reviewsViewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is ReviewsState.Success -> {
                    val store = (storeState as? StoreState.Success)?.store
                    val reviews = (reviewsState as ReviewsState.Success).reviews
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Rating Summary Header
                        item {
                            if (store != null) {
                                RatingSummaryCard(
                                    store = store,
                                    reviewCount = reviews.size,
                                    averageRating = calculateAverageRating(reviews)
                                )
                            }
                        }
                        
                        // Sort Bar
                        item {
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
                                    Text(
                                        text = "${reviews.size} ${if (reviews.size == 1) "Review" else "Reviews"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = sortOption == com.kiranawala.presentation.viewmodels.ReviewsSortOption.RECENT,
                                            onClick = { reviewsViewModel.setSortOption(com.kiranawala.presentation.viewmodels.ReviewsSortOption.RECENT) },
                                            label = { Text("Recent") }
                                        )
                                        FilterChip(
                                            selected = sortOption == com.kiranawala.presentation.viewmodels.ReviewsSortOption.TOP_RATED,
                                            onClick = { reviewsViewModel.setSortOption(com.kiranawala.presentation.viewmodels.ReviewsSortOption.TOP_RATED) },
                                            label = { Text("Top Rated") }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Reviews - directly in LazyColumn items
                        items(
                            items = reviews,
                            key = { review -> review.id }
                        ) { review ->
                            com.kiranawala.presentation.components.review.ReviewCard(
                                review = review,
                                currentUserId = currentUserId,
                                onDeleteClick = { reviewId ->
                                    currentUserId?.let { userId ->
                                        reviewsViewModel.deleteReview(reviewId, userId)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Add Review Dialog
        if (showAddReviewDialog) {
            val store = (storeState as? StoreState.Success)?.store
            if (store != null && currentUserId != null) {
                AddReviewDialog(
                    storeName = store.name,
                    onDismiss = { showAddReviewDialog = false },
                    onSubmit = { rating, comment ->
                        reviewsViewModel.addReview(
                            storeId = store.id,
                            customerId = currentUserId!!,
                            customerName = currentUserName,
                            rating = rating,
                            comment = comment
                        )
                    },
                    isLoading = addReviewState is AddReviewState.Loading
                )
            }
        }
    }
}

@Composable
fun RatingSummaryCard(
    store: Store,
    reviewCount: Int,
    averageRating: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Average Rating
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (averageRating > 0) String.format("%.1f", averageRating) else store.rating.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < (averageRating.roundToInt())) 
                                Icons.Default.Star 
                            else 
                                Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "$reviewCount ratings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Divider
            Divider(
                modifier = Modifier
                    .height(80.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
            
            // Store Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    color = if (store.isOpen) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (store.isOpen) "Open" else "Closed",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

