package com.kiranawala.domain.models

data class Product(
    val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val price: Double,
    val stockQuantity: Int,
    val imageUrl: String? = null,
    val category: String = "General"
)