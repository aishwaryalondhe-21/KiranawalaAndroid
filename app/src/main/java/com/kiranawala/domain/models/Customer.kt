package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

data class Customer(
    val id: String,
    val phone: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)