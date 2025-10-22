package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.AppSettings
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.SecuritySettings
import com.kiranawala.domain.models.UserAddress
import com.kiranawala.domain.models.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile management
 */
interface ProfileRepository {
    
    /**
     * Get user profile
     */
    fun getUserProfile(): Flow<UserProfile?>
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    
    /**
     * Update profile image
     */
    suspend fun updateProfileImage(imageUri: String): Result<String>
    
    /**
     * Get user addresses
     */
    fun getUserAddresses(): Flow<List<UserAddress>>
    
    /**
     * Add new address
     */
    suspend fun addAddress(address: UserAddress): Result<UserAddress>
    
    /**
     * Update address
     */
    suspend fun updateAddress(address: UserAddress): Result<Unit>
    
    /**
     * Delete address
     */
    suspend fun deleteAddress(addressId: String): Result<Unit>
    
    /**
     * Set default address
     */
    suspend fun setDefaultAddress(addressId: String): Result<Unit>
    
    /**
     * Get app settings
     */
    suspend fun getAppSettings(): Result<AppSettings>
    
    /**
     * Update app settings
     */
    suspend fun updateAppSettings(settings: AppSettings): Result<Unit>
    
    /**
     * Get security settings
     */
    suspend fun getSecuritySettings(): Result<SecuritySettings>
    
    /**
     * Update security settings
     */
    suspend fun updateSecuritySettings(settings: SecuritySettings): Result<Unit>
    
    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit>
}