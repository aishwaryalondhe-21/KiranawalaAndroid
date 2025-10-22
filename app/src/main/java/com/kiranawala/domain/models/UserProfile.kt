package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

/**
 * User profile information
 */
data class UserProfile(
    val id: String,
    val phone: String,
    val name: String,
    val address: String = "", // Simple address string from customer table
    val profileImageUrl: String? = null,
    val addresses: List<UserAddress> = emptyList(), // Detailed addresses (future use)
    val defaultAddressId: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * User address information
 */
data class UserAddress(
    val id: String,
    val customerId: String,
    val title: String, // e.g., "Home", "Office"
    val fullAddress: String,
    val landmark: String? = null,
    val city: String,
    val state: String,
    val pincode: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * App settings for user preferences
 */
data class AppSettings(
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val language: String = "en",
    val darkModeEnabled: Boolean = false,
    val locationTrackingEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true
)

/**
 * Account security settings
 */
data class SecuritySettings(
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val autoLogoutMinutes: Int = 30,
    val lastPasswordChange: LocalDateTime? = null
)