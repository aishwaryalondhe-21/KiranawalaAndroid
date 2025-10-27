package com.kiranawala.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiranawala.presentation.components.modern.storeDetailEnterTransition
import com.kiranawala.presentation.components.modern.storeDetailPopExitTransition
import com.kiranawala.presentation.components.modern.storeListExitTransition
import com.kiranawala.presentation.components.modern.storeListPopEnterTransition
import com.kiranawala.presentation.screens.CartScreen
import com.kiranawala.presentation.screens.CheckoutScreen
import com.kiranawala.presentation.screens.auth.PhoneAuthScreen
import com.kiranawala.presentation.screens.home.HomeScreen
import com.kiranawala.presentation.screens.splash.SplashScreen
import com.kiranawala.presentation.screens.store.StoreDetailScreen
import com.kiranawala.presentation.screens.store.StoreListScreen
import com.kiranawala.presentation.screens.store.ReviewsScreen
import com.kiranawala.presentation.screens.order.OrderHistoryScreen
import com.kiranawala.presentation.screens.order.OrderDetailsScreen
import com.kiranawala.presentation.screens.profile.ProfileScreen
import com.kiranawala.presentation.screens.profile.ProfileEditScreen
import com.kiranawala.presentation.screens.address.AddressListScreen
import com.kiranawala.presentation.screens.address.AddressFormScreen
import com.kiranawala.presentation.screens.address.LocationPickerScreen
import com.kiranawala.presentation.screens.order.OrderSuccessScreen
import com.kiranawala.presentation.screens.settings.AboutUsScreen
import com.kiranawala.utils.SessionManager
import com.kiranawala.di.SessionManagerEntryPoint
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

/**
 * Main navigation graph for the app
 * Defines all navigation routes and screens
 */
@Composable
fun KiranaNavigation() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(context, SessionManagerEntryPoint::class.java).sessionManager()
    }
    
    NavHost(
        navController = navController,
        startDestination = Routes.SplashScreen.route
    ) {
        // Splash Screen
        composable(Routes.SplashScreen.route) {
            SplashScreen(navController)
        }
        
        // Authentication Screen (Phone OTP)
        composable(Routes.LoginScreen.route) {
            PhoneAuthScreen(navController)
        }
        
        composable(Routes.SignUpScreen.route) {
            PhoneAuthScreen(navController)
        }
        
        // Main App Screens
        composable(Routes.HomeScreen.route) {
            // Store List Screen is now the home screen
            StoreListScreen(
                onStoreClick = { storeId ->
                    navController.navigate("store/$storeId")
                },
                onReviewsClick = { storeId ->
                    navController.navigate(Routes.ReviewsScreen.createRoute(storeId))
                },
                onCartClick = {
                    navController.navigate(Routes.CartScreen.route)
                },
                onOrderHistoryClick = {
                    navController.navigate(Routes.OrdersScreen.route)
                },
                onProfileClick = {
                    navController.navigate(Routes.ProfileScreen.route)
                }
            )
        }
        
        // Store Detail Screen with Shared Element Transitions
        composable(
            route = Routes.StoreDetailScreen.route,
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType }
            ),
            enterTransition = { storeDetailEnterTransition() },
            exitTransition = { storeListExitTransition() },
            popEnterTransition = { storeListPopEnterTransition() },
            popExitTransition = { storeDetailPopExitTransition() }
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            StoreDetailScreen(
                onBackClick = { navController.navigateUp() },
                onCartClick = {
                    navController.navigate(Routes.CartScreen.route)
                },
                onReviewsClick = { storeId ->
                    navController.navigate(Routes.ReviewsScreen.createRoute(storeId))
                },
                onProductClick = { productId ->
                    // TODO: Navigate to product detail or add to cart
                }
            )
        }
        
        // Reviews Screen
        composable(
            route = Routes.ReviewsScreen.route,
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType }
            )
        ) {
            ReviewsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Cart Screen
        composable(Routes.CartScreen.route) {
            var currentUserId by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    currentUserId = sessionManager.getCurrentUserId()
                }
            }
            
            currentUserId?.let { userId ->
                CartScreen(
                    customerId = userId,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToCheckout = { cart ->
                        navController.navigate(Routes.CheckoutScreen.route)
                    }
                )
            } ?: run {
                // Show loading or redirect to auth
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Checkout Screen
        composable(Routes.CheckoutScreen.route) {
            val cartViewModel: com.kiranawala.presentation.viewmodels.CartViewModel = hiltViewModel()
            var currentUserId by remember { mutableStateOf<String?>(null) }
            var cartSnapshot by remember { mutableStateOf<com.kiranawala.domain.models.Cart?>(null) }
            var navigateBack by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                currentUserId = sessionManager.getCurrentUserId()
            }
            
            // Take cart snapshot once, then stop observing
            if (cartSnapshot == null && !navigateBack) {
                LaunchedEffect(currentUserId) {
                    currentUserId?.let { userId ->
                        cartViewModel.initialize(userId)
                    }
                }
                
                val cartState by cartViewModel.cartState.collectAsState()
                when (cartState) {
                    is com.kiranawala.presentation.viewmodels.CartState.Success -> {
                        cartSnapshot = (cartState as com.kiranawala.presentation.viewmodels.CartState.Success).cart
                    }
                    is com.kiranawala.presentation.viewmodels.CartState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        navigateBack = true
                    }
                }
            }
            
            if (navigateBack) {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            } else {
                cartSnapshot?.let { cart ->
                    currentUserId?.let { userId ->
                        CheckoutScreen(
                            customerId = userId,
                            cart = cart,
                            onNavigateBack = { navController.navigateUp() },
                            onOrderPlaced = { orderId ->
                                KiranaLogger.d("NavigationGraph", "Navigating to order details with orderId: $orderId")
                                navController.navigate("order/$orderId?showReview=true") {
                                    popUpTo(Routes.HomeScreen.route) { inclusive = false }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Order History Screen
        composable(Routes.OrdersScreen.route) {
            var currentUserId by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    currentUserId = sessionManager.getCurrentUserId()
                }
            }
            
            currentUserId?.let { userId ->
                OrderHistoryScreen(
                    customerId = userId,
                    onOrderClick = { orderId ->
                        navController.navigate("order/$orderId")
                    },
                    onBackClick = { navController.navigateUp() }
                )
            } ?: run {
                // Show loading or redirect to auth
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Order Details Screen
        composable(
            route = Routes.OrderDetailScreen.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("showReview") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            val orderId = it.arguments?.getString("orderId") ?: ""
            val showReview = it.arguments?.getBoolean("showReview") ?: false
            OrderDetailsScreen(
                orderId = orderId,
                showReviewDialog = showReview,
                onBackClick = { navController.navigateUp() },
                onCancelOrder = {
                    // TODO: Implement cancel order functionality
                }
            )
        }
        
        // Profile Screen
        composable(Routes.ProfileScreen.route) {
            ProfileScreen(
                onBackClick = { navController.navigateUp() },
                onEditProfileClick = {
                    navController.navigate(Routes.ProfileEditScreen.route)
                },
                onAddressManagementClick = {
                    navController.navigate(Routes.AddressListScreen.route)
                },
                onNotificationSettingsClick = {
                    // TODO: Navigate to notification settings
                },
                onAppSettingsClick = {
                    // TODO: Navigate to app settings
                },
                onSecuritySettingsClick = {
                    // TODO: Navigate to security settings
                },
                onOrderHistoryClick = {
                    navController.navigate(Routes.OrdersScreen.route)
                },
                onAboutClick = {
                    navController.navigate(Routes.AboutUsScreen.route)
                },
                onLogoutClick = {
                    // Navigate to auth screen after logout
                    navController.navigate(Routes.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Profile Edit Screen
        composable(Routes.ProfileEditScreen.route) {
            ProfileEditScreen(
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() }
            )
        }
        
        // Address List Screen
        composable(Routes.AddressListScreen.route) {
            AddressListScreen(
                onNavigateBack = { navController.navigateUp() },
                onAddAddress = { query ->
                    query?.let {
                        navController.currentBackStackEntry?.savedStateHandle?.set("address_form_initial_query", it)
                    }
                    navController.navigate(Routes.AddressFormScreen.createRoute(autoPick = true))
                },
                onEditAddress = { address ->
                    navController.navigate(Routes.AddressFormScreen.createRoute(address.id))
                }
            )
        }
        
        // Address Form Screen
        composable(
            route = Routes.AddressFormScreen.route,
            arguments = listOf(
                navArgument("addressId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("autoPick") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getString("addressId")
            val autoPick = backStackEntry.arguments?.getBoolean("autoPick") ?: false
            val viewModel: com.kiranawala.presentation.viewmodels.AddressViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val address = addressId?.let { id ->
                uiState.addresses.find { it.id == id }
            }

            AddressFormScreen(
                existingAddress = address,
                savedStateHandle = backStackEntry.savedStateHandle,
                autoLaunchLocation = autoPick && address == null,
                onNavigateBack = { navController.navigateUp() },
                onSelectLocation = { lat, lng, query ->
                    navController.navigate(Routes.LocationPickerScreen.createRoute(lat, lng, query))
                }
            )
        }

        composable(
            route = Routes.LocationPickerScreen.route,
            arguments = listOf(
                navArgument("lat") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("lng") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialLat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val initialLng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
            val initialQuery = backStackEntry.arguments?.getString("query")

            LocationPickerScreen(
                initialLatitude = initialLat,
                initialLongitude = initialLng,
                initialQuery = initialQuery,
                onNavigateBack = { navController.navigateUp() },
                onLocationConfirmed = { result ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_formatted_address", result.formattedAddress)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_address_line1", result.addressLine1)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_address_line2", result.addressLine2)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_city", result.city)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_state", result.state)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_pincode", result.pincode)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_latitude", result.latitude)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_longitude", result.longitude)
                    navController.navigateUp()
                }
            )
        }
        
        // Order Success Screen
        composable(
            route = Routes.OrderSuccessScreen.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            KiranaLogger.d("NavigationGraph", "OrderSuccessScreen composable entered with orderId: $orderId")
            val viewModel: com.kiranawala.presentation.viewmodels.OrderDetailsViewModel = hiltViewModel()
            val orderDetailsState by viewModel.orderDetailsState.collectAsState()
            
            LaunchedEffect(orderId) {
                KiranaLogger.d("NavigationGraph", "Loading order details for success screen: $orderId")
                viewModel.loadOrderDetails(orderId)
            }
            
            when (val state = orderDetailsState) {
                is com.kiranawala.presentation.viewmodels.OrderDetailsState.Success -> {
                    OrderSuccessScreen(
                        orderId = state.order.id,
                        storeName = state.order.storeName,
                        totalAmount = state.order.totalAmount,
                        estimatedDeliveryMinutes = 30,
                        onViewOrderDetails = {
                            navController.navigate("order/$orderId")
                        },
                        onBackToHome = {
                            navController.navigate(Routes.HomeScreen.route) {
                                popUpTo(Routes.HomeScreen.route) { inclusive = false }
                            }
                        },
                        onTrackOrder = {
                            // Navigate to order details for now (tracking in Phase 5)
                            navController.navigate("order/$orderId")
                        }
                    )
                }
                else -> {
                    // Show loading while fetching order details
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        
        // About Us Screen
        composable(Routes.AboutUsScreen.route) {
            AboutUsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
