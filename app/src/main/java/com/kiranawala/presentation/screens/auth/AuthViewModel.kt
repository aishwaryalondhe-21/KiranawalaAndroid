package com.kiranawala.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.use_cases.auth.SendOTPUseCase
import com.kiranawala.domain.use_cases.auth.VerifyOTPUseCase
import com.kiranawala.domain.use_cases.auth.LogoutUseCase
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authentication state
 */
data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val otpSent: Boolean = false
)

/**
 * Authentication UI events
 */
sealed class AuthEvent {
    data class SendOTP(val phone: String) : AuthEvent()
    data class VerifyOTP(val phone: String, val otp: String, val name: String? = null) : AuthEvent()
    object Logout : AuthEvent()
    object ClearError : AuthEvent()
    object ClearSuccess : AuthEvent()
}

/**
 * Authentication ViewModel
 * Handles all authentication-related operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sendOTPUseCase: SendOTPUseCase,
    private val verifyOTPUseCase: VerifyOTPUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()
    
    /**
     * Handle authentication events
     */
    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SendOTP -> sendOTP(event.phone)
            is AuthEvent.VerifyOTP -> verifyOTP(event.phone, event.otp, event.name)
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> clearError()
            is AuthEvent.ClearSuccess -> clearSuccess()
        }
    }
    
    /**
     * Send OTP to phone
     */
    private fun sendOTP(phone: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null, otpSent = false) }
            
            KiranaLogger.d(TAG, "Sending OTP")
            
            when (val result = sendOTPUseCase(phone)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "OTP sent successfully")
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            otpSent = true,
                            successMessage = "OTP sent to $phone"
                        )
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to send OTP: ${result.exception.message}")
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to send OTP"
                        )
                    }
                }
                is Result.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    /**
     * Verify OTP and login/signup
     */
    private fun verifyOTP(phone: String, otp: String, name: String?) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            KiranaLogger.d(TAG, "Verifying OTP")
            
            when (val result = verifyOTPUseCase(phone, otp, name)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "OTP verified successfully")
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            successMessage = if (name != null) "Account created!" else "Login successful!"
                        )
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "OTP verification failed: ${result.exception.message}")
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Invalid OTP"
                        )
                    }
                }
                is Result.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    /**
     * Logout user
     */
    private fun logout() {
        viewModelScope.launch {
            KiranaLogger.d(TAG, "Attempting logout")
            
            when (val result = logoutUseCase()) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Logout successful")
                    _authState.update { AuthState() }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Logout failed: ${result.exception.message}")
                    _authState.update {
                        it.copy(error = "Logout failed")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    /**
     * Clear error message
     */
    private fun clearError() {
        _authState.update { it.copy(error = null) }
    }
    
    /**
     * Clear success message
     */
    private fun clearSuccess() {
        _authState.update { it.copy(successMessage = null) }
    }
}
