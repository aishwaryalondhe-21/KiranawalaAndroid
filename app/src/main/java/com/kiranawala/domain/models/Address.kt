package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

enum class AddressType {
    HOME,
    WORK,
    OTHER;

    companion object {
        fun fromRaw(value: String): AddressType = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: OTHER
    }
}

data class Address(
    val id: String,
    val customerId: String,
    val addressType: AddressType,
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val pincode: String,
    val receiverName: String,
    val receiverPhone: String,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)