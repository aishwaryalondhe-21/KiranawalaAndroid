package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.AddressType
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.repositories.AddressRepository
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.domain.repositories.StoreRepository
import com.kiranawala.domain.use_cases.address.AddressValidationErrors
import com.kiranawala.domain.use_cases.address.AddressValidationRequest
import com.kiranawala.domain.use_cases.address.ValidateAddressUseCase
import com.kiranawala.utils.LocationAddress
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val authRepository: AuthRepository,
    private val validateAddressUseCase: ValidateAddressUseCase,
    private val storeRepository: StoreRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AddressViewModel"
    }

    private val _uiState = MutableStateFlow(AddressUiState(isLoading = true))
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddressFormState())
    val formState: StateFlow<AddressFormState> = _formState.asStateFlow()

    // Current location state for location header
    private val _currentLocation = MutableStateFlow<LocationAddress?>(null)
    val currentLocation: StateFlow<LocationAddress?> = _currentLocation.asStateFlow()

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    private var currentUserId: String? = null
    private var addressCollectionJob: Job? = null
    private val stores = MutableStateFlow(emptyList<Store>())

    init {
        observeRecentSearches()
        observeStores()
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val userResult = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    val userId = userResult.data
                    if (userId.isNullOrEmpty()) {
                        _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                        return@launch
                    }
                    currentUserId = userId
                    observeAddresses(userId)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = userResult.exception.message ?: "Unable to fetch user")
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun observeAddresses(userId: String) {
        addressCollectionJob?.cancel()
        addressCollectionJob = viewModelScope.launch {
            addressRepository.getUserAddresses(userId)
                .collect { addresses ->
                    val distanceMap = buildDistanceMap(addresses, stores.value)
                    _uiState.update {
                        it.copy(
                            addresses = addresses,
                            defaultAddress = addresses.firstOrNull(Address::isDefault),
                            isLoading = false,
                            error = null,
                            distanceByAddressId = distanceMap
                        )
                    }
                }
        }
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            addressRepository.observeRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    private fun observeStores() {
        viewModelScope.launch {
            storeRepository.observeStores().collect { storeList ->
                stores.value = storeList
                val distanceMap = buildDistanceMap(_uiState.value.addresses, storeList)
                _uiState.update { it.copy(distanceByAddressId = distanceMap) }
            }
        }
    }

    fun refreshAddresses() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            when (val result = addressRepository.refreshAddresses(userId)) {
                is Result.Error -> _uiState.update {
                    it.copy(isSyncing = false, error = result.exception.message ?: "Failed to refresh addresses")
                }
                else -> _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun initializeForm(addressId: String?) {
        if (addressId == null) {
            _formState.value = AddressFormState()
            return
        }
        val existing = _uiState.value.addresses.firstOrNull { it.id == addressId }
        if (existing != null) {
            _formState.value = AddressFormState(
                id = existing.id,
                formattedAddress = existing.formattedAddress,
                addressLine1 = existing.addressLine1,
                addressLine2 = existing.addressLine2.orEmpty(),
                city = existing.city,
                state = existing.state,
                pincode = existing.pincode,
                latitude = existing.latitude,
                longitude = existing.longitude,
                addressType = existing.addressType,
                receiverName = existing.receiverName,
                receiverPhone = existing.receiverPhone,
                isDefault = existing.isDefault
            )
        } else {
            _formState.value = AddressFormState()
        }
    }

    fun onLocationSelected(selection: LocationSelection) {
        viewModelScope.launch {
            addressRepository.addRecentSearch(selection.formattedAddress)
        }
        _formState.update {
            it.copy(
                formattedAddress = selection.formattedAddress,
                addressLine1 = selection.addressLine1,
                addressLine2 = selection.addressLine2.orEmpty(),
                city = selection.city,
                state = selection.state,
                pincode = selection.pincode,
                latitude = selection.latitude,
                longitude = selection.longitude,
                validationErrors = it.validationErrors.copy(
                    formattedAddressError = null,
                    addressLine1Error = null,
                    cityError = null,
                    stateError = null,
                    pincodeError = null
                )
            )
        }
    }

    fun updateField(field: AddressFormField, value: String) {
        _formState.update { current ->
            current.copy(
                addressLine1 = if (field == AddressFormField.ADDRESS_LINE1) value else current.addressLine1,
                addressLine2 = if (field == AddressFormField.ADDRESS_LINE2) value else current.addressLine2,
                city = if (field == AddressFormField.CITY) value else current.city,
                state = if (field == AddressFormField.STATE) value else current.state,
                pincode = if (field == AddressFormField.PINCODE) value else current.pincode,
                receiverName = if (field == AddressFormField.RECEIVER_NAME) value else current.receiverName,
                receiverPhone = if (field == AddressFormField.RECEIVER_PHONE) value else current.receiverPhone,
                validationErrors = current.validationErrors.clear(field)
            )
        }
    }

    fun updateAddressType(type: AddressType) {
        _formState.update { it.copy(addressType = type) }
    }

    fun toggleDefault() {
        _formState.update { it.copy(isDefault = !it.isDefault) }
    }

    fun saveAddress(onSuccess: () -> Unit) {
        val userId = currentUserId
        if (userId.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        val form = _formState.value
        val latitude = form.latitude
        val longitude = form.longitude
        if (latitude == null || longitude == null) {
            _formState.update {
                it.copy(validationErrors = it.validationErrors.copy(formattedAddressError = "Select a delivery location"))
            }
            return
        }

        val validation = validateAddressUseCase(
            AddressValidationRequest(
                formattedAddress = form.formattedAddress,
                addressLine1 = form.addressLine1,
                city = form.city,
                state = form.state,
                pincode = form.pincode,
                receiverName = form.receiverName,
                receiverPhone = form.receiverPhone,
                addressType = form.addressType
            )
        )

        if (!validation.isValid) {
            _formState.update { it.copy(validationErrors = validation) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, validationErrors = validation) }
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val existing = form.id?.let { id -> _uiState.value.addresses.firstOrNull { it.id == id } }
            val isDefault = form.isDefault || (existing == null && _uiState.value.addresses.isEmpty())
            val address = Address(
                id = existing?.id ?: UUID.randomUUID().toString(),
                customerId = userId,
                addressType = form.addressType,
                latitude = latitude,
                longitude = longitude,
                formattedAddress = form.formattedAddress.trim(),
                addressLine1 = form.addressLine1.trim(),
                addressLine2 = form.addressLine2.trim().takeIf { it.isNotEmpty() },
                city = form.city.trim(),
                state = form.state.trim(),
                pincode = form.pincode.trim(),
                receiverName = form.receiverName.trim(),
                receiverPhone = form.receiverPhone.trim(),
                isDefault = isDefault,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )

            val result = if (existing == null) {
                addressRepository.addAddress(address)
            } else {
                addressRepository.updateAddress(address)
            }

            when (result) {
                is Result.Success -> {
                    _formState.value = AddressFormState()
                    onSuccess()
                }
                is Result.Error -> {
                    _formState.update { it.copy(isSubmitting = false) }
                    _uiState.update {
                        it.copy(error = result.exception.message ?: "Unable to save address")
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = addressRepository.deleteAddress(addressId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message ?: "Failed to delete address")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun setDefaultAddress(addressId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = addressRepository.setDefaultAddress(addressId, userId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message ?: "Failed to set default address")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Current Location Management
    fun setCurrentLocation(location: LocationAddress) {
        _currentLocation.value = location
        KiranaLogger.d(TAG, "Current location set to: ${location.city}, ${location.state}")
    }

    fun setLocationDetecting(isDetecting: Boolean) {
        _isDetectingLocation.value = isDetecting
    }

    fun getCurrentLocationValue(): LocationAddress? {
        return _currentLocation.value
    }

    /**
     * Initialize current location from default address if available
     */
    fun initializeCurrentLocationFromDefaultAddress() {
        viewModelScope.launch {
            val defaultAddr = _uiState.value.defaultAddress
            if (defaultAddr != null && _currentLocation.value == null) {
                val locationAddress = LocationAddress(
                    formattedAddress = defaultAddr.formattedAddress,
                    city = defaultAddr.city,
                    state = defaultAddr.state,
                    country = "",
                    latitude = defaultAddr.latitude,
                    longitude = defaultAddr.longitude
                )
                setCurrentLocation(locationAddress)
                KiranaLogger.d(TAG, "Initialized location from default address")
            }
        }
    }
}

data class AddressUiState(
    val addresses: List<Address> = emptyList(),
    val defaultAddress: Address? = null,
    val recentSearches: List<String> = emptyList(),
    val distanceByAddressId: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null
)

data class AddressFormState(
    val id: String? = null,
    val formattedAddress: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressType: AddressType = AddressType.HOME,
    val receiverName: String = "",
    val receiverPhone: String = "",
    val isDefault: Boolean = false,
    val validationErrors: AddressValidationErrors = AddressValidationErrors(),
    val isSubmitting: Boolean = false
)

enum class AddressFormField {
    ADDRESS_LINE1,
    ADDRESS_LINE2,
    CITY,
    STATE,
    PINCODE,
    RECEIVER_NAME,
    RECEIVER_PHONE
}

data class LocationSelection(
    val formattedAddress: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val pincode: String,
    val latitude: Double,
    val longitude: Double
)

private fun AddressValidationErrors.clear(field: AddressFormField): AddressValidationErrors {
    return when (field) {
        AddressFormField.ADDRESS_LINE1 -> copy(addressLine1Error = null)
        AddressFormField.ADDRESS_LINE2 -> this
        AddressFormField.CITY -> copy(cityError = null)
        AddressFormField.STATE -> copy(stateError = null)
        AddressFormField.PINCODE -> copy(pincodeError = null)
        AddressFormField.RECEIVER_NAME -> copy(receiverNameError = null)
        AddressFormField.RECEIVER_PHONE -> copy(receiverPhoneError = null)
    }
}

private fun buildDistanceMap(addresses: List<Address>, stores: List<Store>): Map<String, Double> {
    if (addresses.isEmpty() || stores.isEmpty()) return emptyMap()
    return addresses.associate { address ->
        val nearest = stores.minOfOrNull { store ->
            haversineKm(address.latitude, address.longitude, store.latitude, store.longitude)
        }
        address.id to (nearest ?: Double.NaN)
    }.filterValues { !it.isNaN() }
}

private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = (lat2 - lat1).toRadians()
    val dLon = (lon2 - lon1).toRadians()
    val a = sin(dLat / 2).pow(2) + cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

private fun Double.toRadians(): Double = this * (PI / 180.0)
