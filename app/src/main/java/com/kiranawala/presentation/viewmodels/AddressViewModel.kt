package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AddressRepository
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "AddressViewModel"
    }
    
    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()
    
    init {
        loadAddresses()
    }
    
    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userIdResult = authRepository.getCurrentUser()
            if (userIdResult is Result.Success && userIdResult.data != null) {
                addressRepository.getUserAddresses(userIdResult.data)
                    .catch { exception ->
                        KiranaLogger.e(TAG, "Error loading addresses", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load addresses"
                            )
                        }
                    }
                    .collect { addresses ->
                        _uiState.update {
                            it.copy(
                                addresses = addresses,
                                defaultAddress = addresses.find { addr -> addr.isDefault },
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                }
            }
        }
    }
    
    fun addAddress(
        addressLine: String,
        buildingName: String?,
        flatNumber: String?,
        latitude: Double,
        longitude: Double,
        label: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userIdResult = authRepository.getCurrentUser()
            if (userIdResult is Result.Success && userIdResult.data != null) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val address = Address(
                    id = UUID.randomUUID().toString(),
                    userId = userIdResult.data,
                    addressLine = addressLine,
                    buildingName = buildingName,
                    flatNumber = flatNumber,
                    latitude = latitude,
                    longitude = longitude,
                    label = label,
                    isDefault = isDefault,
                    createdAt = now,
                    updatedAt = now
                )
                
                when (val result = addressRepository.addAddress(address)) {
                    is Result.Success -> {
                        KiranaLogger.d(TAG, "Address added successfully")
                        loadAddresses()
                    }
                    is Result.Error -> {
                        KiranaLogger.e(TAG, "Failed to add address", result.exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Failed to add address"
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
    
    fun updateAddress(address: Address) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = addressRepository.updateAddress(address)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Address updated successfully")
                    loadAddresses()
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update address", result.exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to update address"
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = addressRepository.deleteAddress(addressId)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Address deleted successfully")
                    loadAddresses()
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to delete address", result.exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to delete address"
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userIdResult = authRepository.getCurrentUser()
            if (userIdResult is Result.Success && userIdResult.data != null) {
                when (val result = addressRepository.setDefaultAddress(addressId, userIdResult.data)) {
                    is Result.Success -> {
                        KiranaLogger.d(TAG, "Default address set successfully")
                        loadAddresses()
                    }
                    is Result.Error -> {
                        KiranaLogger.e(TAG, "Failed to set default address", result.exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Failed to set default address"
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AddressUiState(
    val addresses: List<Address> = emptyList(),
    val defaultAddress: Address? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
