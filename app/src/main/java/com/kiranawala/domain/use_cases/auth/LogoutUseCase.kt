package com.kiranawala.domain.use_cases.auth

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import javax.inject.Inject

/**
 * Logout use case
 * Handles user logout
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "LogoutUseCase"
    }
    
    /**
     * Execute logout
     * @return Result indicating success or error
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Attempting logout")
            authRepository.logout()
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Logout failed", e)
            Result.Error(e)
        }
    }
}
