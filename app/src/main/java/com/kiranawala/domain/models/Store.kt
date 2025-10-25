package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

data class Store(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contact: String,
    val logo: String? = null,
    val imageUrl: String? = null,  // Hero image for card
    val rating: Float = 4.5f,
    val totalReviews: Int = 0,  // Social proof
    val currentOrders: Int = 0,  // "X people ordering now"
    val minimumOrderValue: Double = 100.0,
    val deliveryFee: Double = 30.0,
    val estimatedDeliveryTime: Int = 30,
    val isOpen: Boolean = true,
    val isFeatured: Boolean = false,  // "Most ordered this week"
    val distance: Double? = null,  // Distance in km
    val subscriptionStatus: String = "ACTIVE",
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
