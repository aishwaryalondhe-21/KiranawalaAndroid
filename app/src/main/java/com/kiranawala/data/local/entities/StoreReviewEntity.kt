package com.kiranawala.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

/**
 * Room entity for storing store reviews locally
 * Used for offline caching and quick access
 */
@Entity(tableName = "store_reviews")
data class StoreReviewEntity(
    @PrimaryKey
    val id: String,
    val storeId: String,
    val customerId: String,
    val customerName: String,
    val rating: Int, // 1-5 stars
    val comment: String?, // Optional comment
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
