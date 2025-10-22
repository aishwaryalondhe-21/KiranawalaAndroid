package com.kiranawala.domain.use_cases.auth

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import javax.inject.Inject

/**
 * Verify OTP use case
 * Verifies OTP and logs in/signs up user
 */
class VerifyOTPUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "VerifyOTPUseCase"
    }
    
    /**
     * Execute OTP verification
     * @param phone Phone number
     * @param otp OTP code
     * @param name User name (for new users, optional for existing users)
     * @return Result containing user ID or error
     */
    suspend operator fun invoke(
        phone: String,
        otp: String,
        name: String? = null
    ): Result<String> {
        return try {
            // Validate inputs
            if (phone.isBlank()) {
                return Result.Error(Exception("Phone number is required"))
            }
            
            if (otp.isBlank()) {
                return Result.Error(Exception("OTP is required"))
            }
            
            if (otp.length != 6) {
                return Result.Error(Exception("OTP must be 6 digits"))
            }
            
            KiranaLogger.d(TAG, "Verifying OTP for phone: $phone")
            authRepository.verifyOTP(phone, otp, name)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "OTP verification failed", e)
            Result.Error(e)
        }
    }
}
