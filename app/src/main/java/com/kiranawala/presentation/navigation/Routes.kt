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
    object SelectLocationScreen : Routes("location/select")
    object AddressFormScreen : Routes("address/form?addressId={addressId}&autoPick={autoPick}") {
        fun createRoute(addressId: String? = null, autoPick: Boolean = false): String {
            val params = mutableListOf<String>()
            addressId?.let { params.add("addressId=$it") }
            if (autoPick) params.add("autoPick=true")
            return if (params.isEmpty()) {
                "address/form"
            } else {
                "address/form?" + params.joinToString("&")
            }
        }
    }
    object LocationPickerScreen : Routes("address/location-picker?lat={lat}&lng={lng}&query={query}") {
        fun createRoute(latitude: Double?, longitude: Double?, query: String? = null): String {
            val params = mutableListOf<String>()
            latitude?.let { params.add("lat=$it") }
            longitude?.let { params.add("lng=$it") }
            query?.takeIf { it.isNotBlank() }?.let { params.add("query=${it.encode()}" ) }
            return if (params.isEmpty()) {
                "address/location-picker"
            } else {
                "address/location-picker?" + params.joinToString("&")
            }
        }

        private fun String.encode(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
    }
    object OrderSuccessScreen : Routes("order/success/{orderId}")
    object AboutUsScreen : Routes("about-us")
}
