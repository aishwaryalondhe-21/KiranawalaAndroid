package com.kiranawala.domain.use_cases.auth

import com.kiranawala.domain.models.Customer
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import javax.inject.Inject

/**
 * Get current user use case
 * Retrieves currently logged in user profile
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "GetCurrentUserUseCase"
    }
    
    /**
     * Execute get current user
     * @return Result containing customer profile or error
     */
    suspend operator fun invoke(): Result<Customer?> {
        return try {
            KiranaLogger.d(TAG, "Fetching current user profile")
            authRepository.getCurrentUserProfile()
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to get current user", e)
            Result.Error(e)
        }
    }
}
