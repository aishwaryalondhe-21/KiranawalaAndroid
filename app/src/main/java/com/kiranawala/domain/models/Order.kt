package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

data class Order(
    val id: String,
    val customerId: String,
    val storeId: String,
    val storeName: String,
    val totalAmount: Double,
    val deliveryFee: Double,
    val status: String, // Changed from OrderStatus enum to String for flexibility
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val customerPhone: String,
    val customerName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItem(
    val id: String,
    val orderId: String,
    val productId: String,
    val productName: String, // Added product name for display
    val quantity: Int,
    val price: Double
)

enum class OrderStatus {
    PENDING, PROCESSING, COMPLETED, CANCELLED, FAILED
}