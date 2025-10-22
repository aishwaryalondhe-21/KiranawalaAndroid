package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

/**
 * Address model for multiple addresses per user
 */
data class Address(
    val id: String,
    val userId: String,
    val addressLine: String,
    val buildingName: String?,
    val flatNumber: String?,
    val latitude: Double,
    val longitude: Double,
    val label: String, // Home, Work, Other
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)