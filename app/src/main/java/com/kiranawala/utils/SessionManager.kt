package com.kiranawala.utils

import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.domain.models.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session manager utility for getting current user information
 */
@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend fun getCurrentUserId(): String? {
        return when (val result = authRepository.getCurrentUser()) {
            is Result.Success -> result.data
            is Result.Error -> null
            is Result.Loading -> null
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}