package com.kiranawala.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kiranawala.MainActivity
import com.kiranawala.R
import com.kiranawala.domain.models.NotificationType
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local notification service for displaying order updates and promotions
 * This replaces FCM for now and can be extended with FCM later
 */
@Singleton
class LocalNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "LocalNotificationService"
        private const val CHANNEL_ID = "kiranawala_notifications"
        private const val CHANNEL_NAME = "Order Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for order updates and promotions"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Show a local notification
     */
    fun showNotification(
        title: String,
        body: String,
        type: NotificationType = NotificationType.GENERAL,
        orderId: String? = null,
        storeId: String? = null
    ) {
        try {
            val notificationId = System.currentTimeMillis().toInt()
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                when (type) {
                    NotificationType.ORDER_PLACED,
                    NotificationType.ORDER_CONFIRMED,
                    NotificationType.ORDER_PREPARING,
                    NotificationType.ORDER_OUT_FOR_DELIVERY,
                    NotificationType.ORDER_DELIVERED,
                    NotificationType.ORDER_CANCELLED -> {
                        putExtra("navigate_to", "order_details")
                        putExtra("order_id", orderId)
                    }
                    NotificationType.PROMOTION,
                    NotificationType.STORE_ANNOUNCEMENT -> {
                        putExtra("navigate_to", "store_detail")
                        putExtra("store_id", storeId)
                    }
                    else -> {
                        // General notification - go to home
                    }
                }
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_notification_overlay) // Default icon for now
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
            
            // Add sound and vibration for order updates
            if (type.name.startsWith("ORDER_")) {
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            
            NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
            
            KiranaLogger.d(TAG, "Local notification displayed: $title")
            
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to show notification", e)
        }
    }
    
    /**
     * Show order status update notification
     */
    fun showOrderStatusNotification(orderId: String, status: String) {
        val title = "Order Update"
        val body = when (status.uppercase()) {
            "PENDING" -> "Your order #$orderId has been placed successfully!"
            "PROCESSING" -> "Your order #$orderId is being prepared."
            "COMPLETED" -> "Your order #$orderId has been delivered!"
            "CANCELLED" -> "Your order #$orderId has been cancelled."
            else -> "Your order #$orderId status has been updated to $status."
        }
        
        val type = when (status.uppercase()) {
            "PENDING" -> NotificationType.ORDER_PLACED
            "PROCESSING" -> NotificationType.ORDER_PREPARING
            "COMPLETED" -> NotificationType.ORDER_DELIVERED
            "CANCELLED" -> NotificationType.ORDER_CANCELLED
            else -> NotificationType.GENERAL
        }
        
        showNotification(title, body, type, orderId)
    }
    
    /**
     * Show promotion notification
     */
    fun showPromotionNotification(storeId: String, storeName: String, offer: String) {
        val title = "Special Offer - $storeName"
        val body = offer
        
        showNotification(title, body, NotificationType.PROMOTION, storeId = storeId)
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            KiranaLogger.d(TAG, "Notification channel created")
        }
    }
    
    /**
     * Cancel a specific notification
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}