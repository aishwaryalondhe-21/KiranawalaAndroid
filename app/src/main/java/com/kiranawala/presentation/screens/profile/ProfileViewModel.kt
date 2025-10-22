package com.kiranawala.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.AppSettings
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.SecuritySettings
import com.kiranawala.domain.models.UserAddress
import com.kiranawala.domain.models.UserProfile
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.domain.repositories.ProfileRepository
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Profile UI State
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val addresses: List<UserAddress> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val securitySettings: SecuritySettings = SecuritySettings(),
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Profile UI Events
 */
sealed class ProfileUiEvent {
    object LoadProfile : ProfileUiEvent()
    data class UpdateProfile(val profile: UserProfile) : ProfileUiEvent()
    data class UpdateProfileImage(val imageUri: String) : ProfileUiEvent()
    data class AddAddress(val address: UserAddress) : ProfileUiEvent()
    data class UpdateAddress(val address: UserAddress) : ProfileUiEvent()
    data class DeleteAddress(val addressId: String) : ProfileUiEvent()
    data class SetDefaultAddress(val addressId: String) : ProfileUiEvent()
    data class UpdateAppSettings(val settings: AppSettings) : ProfileUiEvent()
    data class UpdateSecuritySettings(val settings: SecuritySettings) : ProfileUiEvent()
    object Logout : ProfileUiEvent()
    object ClearError : ProfileUiEvent()
    object ClearSuccess : ProfileUiEvent()
}

/**
 * ProfileViewModel - Manages user profile and settings
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "ProfileViewModel"
    }
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun handleEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LoadProfile -> loadProfile()
            is ProfileUiEvent.UpdateProfile -> updateProfile(event.profile)
            is ProfileUiEvent.UpdateProfileImage -> updateProfileImage(event.imageUri)
            is ProfileUiEvent.AddAddress -> addAddress(event.address)
            is ProfileUiEvent.UpdateAddress -> updateAddress(event.address)
            is ProfileUiEvent.DeleteAddress -> deleteAddress(event.addressId)
            is ProfileUiEvent.SetDefaultAddress -> setDefaultAddress(event.addressId)
            is ProfileUiEvent.UpdateAppSettings -> updateAppSettings(event.settings)
            is ProfileUiEvent.UpdateSecuritySettings -> updateSecuritySettings(event.settings)
            is ProfileUiEvent.Logout -> logout()
            is ProfileUiEvent.ClearError -> clearError()
            is ProfileUiEvent.ClearSuccess -> clearSuccess()
        }
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load profile and addresses directly
                val profile = when (val result = authRepository.getCurrentUserProfile()) {
                    is Result.Success -> result.data?.let {
                        com.kiranawala.domain.models.UserProfile(
                            id = it.id,
                            phone = it.phone,
                            name = it.name,
                            address = it.address,
                            profileImageUrl = null,
                            addresses = emptyList(),
                            defaultAddressId = null,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt
                        )
                    }
                    else -> null
                }
                val addresses = emptyList<UserAddress>()
                
                val appSettingsResult = profileRepository.getAppSettings()
                val securitySettingsResult = profileRepository.getSecuritySettings()
                
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    userProfile = profile,
                    addresses = addresses,
                    appSettings = when (appSettingsResult) {
                        is Result.Success<*> -> appSettingsResult.data as? AppSettings ?: AppSettings()
                        else -> AppSettings()
                    },
                    securitySettings = when (securitySettingsResult) {
                        is Result.Success<*> -> securitySettingsResult.data as? SecuritySettings ?: SecuritySettings()
                        else -> SecuritySettings()
                    }
                )
                
                KiranaLogger.d(TAG, "Profile loaded: ${profile?.id}, phone: ${profile?.phone}")
            } catch (e: Exception) {
                KiranaLogger.e(TAG, "Failed to load profile", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }
    
    private fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.updateUserProfile(profile)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = profile,
                        successMessage = "Profile updated successfully"
                    )
                    KiranaLogger.d(TAG, "Profile updated successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update profile", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update profile: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.updateProfileImage(imageUri)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Profile image updated successfully"
                    )
                    KiranaLogger.d(TAG, "Profile image updated: ${result.data}")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update profile image", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update profile image: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun addAddress(address: UserAddress) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.addAddress(address)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Address added successfully"
                    )
                    KiranaLogger.d(TAG, "Address added successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to add address", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to add address: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun updateAddress(address: UserAddress) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.updateAddress(address)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Address updated successfully"
                    )
                    KiranaLogger.d(TAG, "Address updated successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update address", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update address: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.deleteAddress(addressId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Address deleted successfully"
                    )
                    KiranaLogger.d(TAG, "Address deleted successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to delete address", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete address: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.setDefaultAddress(addressId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Default address updated"
                    )
                    KiranaLogger.d(TAG, "Default address set successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to set default address", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to set default address: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun updateAppSettings(settings: AppSettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.updateAppSettings(settings)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appSettings = settings,
                        successMessage = "Settings updated successfully"
                    )
                    KiranaLogger.d(TAG, "App settings updated successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update app settings", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update settings: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun updateSecuritySettings(settings: SecuritySettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = profileRepository.updateSecuritySettings(settings)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        securitySettings = settings,
                        successMessage = "Security settings updated successfully"
                    )
                    KiranaLogger.d(TAG, "Security settings updated successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update security settings", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update security settings: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.logout()) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Logout successful")
                    // Navigation will be handled by the UI
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Logout failed", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Logout failed: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

// Helper function for creating flows from suspend functions
private fun <T> flowOf(suspendFunction: suspend () -> T) = kotlinx.coroutines.flow.flow {
    emit(suspendFunction())
}