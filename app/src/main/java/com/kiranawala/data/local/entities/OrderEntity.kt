package com.kiranawala.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val customerId: String,
    val storeId: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
