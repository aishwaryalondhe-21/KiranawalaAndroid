package com.kiranawala.presentation.navigation

sealed class Routes(val route: String) {
    // Auth Routes
    object SplashScreen : Routes("splash")
    object LoginScreen : Routes("login")
    object SignUpScreen : Routes("signup")
    object OTPScreen : Routes("otp/{phone}")
    
    // Main Routes
    object HomeScreen : Routes("home")
    object StoreDetailScreen : Routes("store/{storeId}")
    object ReviewsScreen : Routes("store/{storeId}/reviews") {
        fun createRoute(storeId: String) = "store/$storeId/reviews"
    }
    object CartScreen : Routes("cart")
    object CheckoutScreen : Routes("checkout")
    object PaymentScreen : Routes("payment/{orderId}")
    object OrdersScreen : Routes("orders")
    object OrderDetailScreen : Routes("order/{orderId}?showReview={showReview}") {
        fun createRoute(orderId: String, showReview: Boolean = false) =
            "order/$orderId?showReview=$showReview"
    }
    object ProfileScreen : Routes("profile")
    object ProfileEditScreen : Routes("profile/edit")
    object AddressListScreen : Routes("addresses")
    object AddressFormScreen : Routes("address/form?addressId={addressId}") {
        fun createRoute(addressId: String? = null) =
            if (addressId != null) "address/form?addressId=$addressId" else "address/form"
    }
    object OrderSuccessScreen : Routes("order/success/{orderId}")
    object AboutUsScreen : Routes("about-us")
}
