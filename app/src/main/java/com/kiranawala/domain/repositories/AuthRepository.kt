package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Customer
import com.kiranawala.domain.models.Result

/**
 * Authentication repository interface
 * Defines contract for authentication operations
 */
interface AuthRepository {
    /**
     * Send OTP to phone number
     * @param phone Phone number with country code (e.g., +919876543210)
     * @return Result indicating success or error
     */
    suspend fun sendOTP(phone: String): Result<Unit>
    
    /**
     * Verify OTP and login/signup
     * @param phone Phone number
     * @param otp OTP code
     * @param name User name (for new users)
     * @return Result containing user ID or error
     */
    suspend fun verifyOTP(phone: String, otp: String, name: String? = null): Result<String>
    
    /**
     * Logout current user
     * @return Result indicating success or error
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Get current logged in user ID
     * @return Result containing user ID or null if not logged in
     */
    suspend fun getCurrentUser(): Result<String?>
    
    /**
     * Check if user is currently logged in
     * @return true if logged in, false otherwise
     */
    suspend fun isLoggedIn(): Boolean
    
    /**
     * Get current user profile
     * @return Result containing Customer data or error
     */
    suspend fun getCurrentUserProfile(): Result<Customer?>
}
