package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for Store Review
 * Represents a customer's rating and review for a store
 */
data class StoreReview(
    val id: String,
    val storeId: String,
    val customerId: String,
    val customerName: String,
    val rating: Int, // 1-5 stars
    val comment: String?, // Optional comment
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
