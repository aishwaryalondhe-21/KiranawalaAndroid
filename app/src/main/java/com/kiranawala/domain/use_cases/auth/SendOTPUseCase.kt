package com.kiranawala.domain.use_cases.auth

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import javax.inject.Inject

/**
 * Send OTP use case
 * Sends OTP to phone number
 */
class SendOTPUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "SendOTPUseCase"
    }
    
    /**
     * Execute send OTP
     * @param phone Phone number with country code (e.g., +919876543210)
     * @return Result indicating success or error
     */
    suspend operator fun invoke(phone: String): Result<Unit> {
        return try {
            // Validate phone number
            val cleanPhone = phone.trim()
            if (cleanPhone.isBlank()) {
                return Result.Error(Exception("Phone number is required"))
            }
            
            // Check if phone starts with +91 (India)
            if (!cleanPhone.startsWith("+")) {
                return Result.Error(Exception("Phone number must include country code (e.g., +91)"))
            }
            
            // Validate length (Indian numbers: +91 + 10 digits = 13 chars)
            if (cleanPhone.length < 12) {
                return Result.Error(Exception("Invalid phone number"))
            }
            
            KiranaLogger.d(TAG, "Sending OTP to: $cleanPhone")
            authRepository.sendOTP(cleanPhone)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to send OTP", e)
            Result.Error(e)
        }
    }
}
