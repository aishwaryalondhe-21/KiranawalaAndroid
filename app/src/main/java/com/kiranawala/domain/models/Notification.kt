package com.kiranawala.domain.models

import kotlinx.datetime.LocalDateTime

/**
 * Represents a notification in the app
 */
data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val orderId: String? = null,
    val storeId: String? = null,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime? = null
)

/**
 * Types of notifications supported by the app
 */
enum class NotificationType(val displayName: String) {
    ORDER_PLACED("Order Placed"),
    ORDER_CONFIRMED("Order Confirmed"),
    ORDER_PREPARING("Order Being Prepared"),
    ORDER_OUT_FOR_DELIVERY("Out for Delivery"),
    ORDER_DELIVERED("Order Delivered"),
    ORDER_CANCELLED("Order Cancelled"),
    PROMOTION("Special Offer"),
    STORE_ANNOUNCEMENT("Store Update"),
    GENERAL("General")
}

/**
 * Notification preferences for users
 */
data class NotificationPreferences(
    val orderUpdatesEnabled: Boolean = true,
    val promotionsEnabled: Boolean = true,
    val generalEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)