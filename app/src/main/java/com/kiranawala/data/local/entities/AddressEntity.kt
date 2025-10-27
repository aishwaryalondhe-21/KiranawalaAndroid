package com.kiranawala.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "addresses",
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["customer_id", "is_default"])
    ]
)
data class AddressEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "customer_id") val customerId: String,
    @ColumnInfo(name = "address_type") val addressType: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "formatted_address") val formattedAddress: String,
    @ColumnInfo(name = "address_line1") val addressLine1: String,
    @ColumnInfo(name = "address_line2") val addressLine2: String?,
    val city: String,
    val state: String,
    val pincode: String,
    @ColumnInfo(name = "receiver_name") val receiverName: String,
    @ColumnInfo(name = "receiver_phone") val receiverPhone: String,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)
