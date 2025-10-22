package com.kiranawala.domain.models

data class CartItem(
    val product: Product,
    val quantity: Int,
    val storeId: String
) {
    val subtotal: Double
        get() = product.price * quantity
}

data class Cart(
    val storeId: String,
    val storeName: String,
    val items: List<CartItem>,
    val minimumOrderValue: Double,
    val deliveryFee: Double
) {
    val subtotal: Double
        get() = items.sumOf { item -> item.subtotal }
    
    val total: Double
        get() = subtotal + deliveryFee
    
    val itemCount: Int
        get() = items.sumOf { item -> item.quantity }
    
    val meetsMinimumOrder: Boolean
        get() = subtotal >= minimumOrderValue
}
