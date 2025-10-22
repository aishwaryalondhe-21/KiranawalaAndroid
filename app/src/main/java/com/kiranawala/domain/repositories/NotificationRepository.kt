package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Notification
import com.kiranawala.domain.models.NotificationPreferences
import com.kiranawala.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification management
 */
interface NotificationRepository {
    
    /**
     * Get all notifications for the current user
     */
    fun getNotifications(): Flow<List<Notification>>
    
    /**
     * Get unread notifications count
     */
    fun getUnreadNotificationsCount(): Flow<Int>
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Unit>
    
    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(): Result<Unit>
    
    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    
    /**
     * Clear all notifications
     */
    suspend fun clearAllNotifications(): Result<Unit>
    
    /**
     * Get notification preferences
     */
    suspend fun getNotificationPreferences(): Result<NotificationPreferences>
    
    /**
     * Update notification preferences
     */
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit>
    
    /**
     * Register FCM token with server
     */
    suspend fun registerFCMToken(token: String): Result<Unit>
    
    /**
     * Request notification permission
     */
    suspend fun requestNotificationPermission(): Result<Boolean>
}