package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.use_cases.store.GetNearbyStoresUseCase
import com.kiranawala.domain.use_cases.store.SearchStoresUseCase
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Store List Screen
 */
@HiltViewModel
class StoreListViewModel @Inject constructor(
    private val getNearbyStoresUseCase: GetNearbyStoresUseCase,
    private val searchStoresUseCase: SearchStoresUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "StoreListViewModel"
        private const val DEFAULT_LATITUDE = 18.5204 // Pune, India
        private const val DEFAULT_LONGITUDE = 73.8567
    }
    
    private val _uiState = MutableStateFlow<StoreListUiState>(StoreListUiState.Loading)
    val uiState: StateFlow<StoreListUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private var currentLatitude = DEFAULT_LATITUDE
    private var currentLongitude = DEFAULT_LONGITUDE
    
    init {
        loadNearbyStores()
    }
    
    fun loadNearbyStores(
        latitude: Double = currentLatitude,
        longitude: Double = currentLongitude,
        radiusKm: Double = 5.0
    ) {
        viewModelScope.launch {
            _uiState.value = StoreListUiState.Loading
            currentLatitude = latitude
            currentLongitude = longitude
            
            KiranaLogger.d(TAG, "Loading nearby stores")
            
            when (val result = getNearbyStoresUseCase(latitude, longitude, radiusKm)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.value = StoreListUiState.Empty
                    } else {
                        _uiState.value = StoreListUiState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to load stores", result.exception)
                    _uiState.value = StoreListUiState.Error(
                        result.exception.message ?: "Failed to load stores"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = StoreListUiState.Loading
                }
            }
        }
    }
    
    fun searchStores(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadNearbyStores()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = StoreListUiState.Loading
            
            KiranaLogger.d(TAG, "Searching stores with query: $query")
            
            when (val result = searchStoresUseCase(query, currentLatitude, currentLongitude)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.value = StoreListUiState.Empty
                    } else {
                        _uiState.value = StoreListUiState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to search stores", result.exception)
                    _uiState.value = StoreListUiState.Error(
                        result.exception.message ?: "Failed to search stores"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = StoreListUiState.Loading
                }
            }
        }
    }
    
    fun refresh() {
        loadNearbyStores()
    }
    
    fun updateLocation(latitude: Double, longitude: Double) {
        currentLatitude = latitude
        currentLongitude = longitude
        loadNearbyStores(latitude, longitude)
    }
}

/**
 * UI State for Store List Screen
 */
sealed class StoreListUiState {
    object Loading : StoreListUiState()
    object Empty : StoreListUiState()
    data class Success(val stores: List<Store>) : StoreListUiState()
    data class Error(val message: String) : StoreListUiState()
}
