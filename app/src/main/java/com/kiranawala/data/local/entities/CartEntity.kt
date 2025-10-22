package com.kiranawala.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: String,
    val storeId: String,
    val productId: String,
    val quantity: Int,
    val price: Double
)