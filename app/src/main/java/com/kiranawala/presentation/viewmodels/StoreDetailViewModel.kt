package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.use_cases.product.FilterProductsByCategoryUseCase
import com.kiranawala.domain.use_cases.product.GetStoreProductsUseCase
import com.kiranawala.domain.use_cases.product.SearchProductsUseCase
import com.kiranawala.domain.use_cases.store.GetStoreByIdUseCase
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Store Detail Screen
 */
@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val getStoreByIdUseCase: GetStoreByIdUseCase,
    private val getStoreProductsUseCase: GetStoreProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    companion object {
        private const val TAG = "StoreDetailViewModel"
        const val STORE_ID_ARG = "storeId"
    }
    
    private val storeId: String = savedStateHandle.get<String>(STORE_ID_ARG) ?: ""
    
    private val _storeState = MutableStateFlow<StoreState>(StoreState.Loading)
    val storeState: StateFlow<StoreState> = _storeState.asStateFlow()
    
    private val _productsState = MutableStateFlow<ProductsState>(ProductsState.Loading)
    val productsState: StateFlow<ProductsState> = _productsState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    private var allProducts: List<Product> = emptyList()
    
    init {
        loadStoreDetails()
        loadProducts()
    }
    
    private fun loadStoreDetails() {
        viewModelScope.launch {
            _storeState.value = StoreState.Loading
            
            KiranaLogger.d(TAG, "Loading store details for ID: $storeId")
            
            when (val result = getStoreByIdUseCase(storeId)) {
                is Result.Success -> {
                    _storeState.value = StoreState.Success(result.data)
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to load store details", result.exception)
                    _storeState.value = StoreState.Error(
                        result.exception.message ?: "Failed to load store details"
                    )
                }
                is Result.Loading -> {
                    _storeState.value = StoreState.Loading
                }
            }
        }
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            _productsState.value = ProductsState.Loading
            
            KiranaLogger.d(TAG, "Loading products for store: $storeId")
            
            when (val result = getStoreProductsUseCase(storeId)) {
                is Result.Success -> {
                    allProducts = result.data
                    if (result.data.isEmpty()) {
                        _productsState.value = ProductsState.Empty
                    } else {
                        _productsState.value = ProductsState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to load products", result.exception)
                    _productsState.value = ProductsState.Error(
                        result.exception.message ?: "Failed to load products"
                    )
                }
                is Result.Loading -> {
                    _productsState.value = ProductsState.Loading
                }
            }
        }
    }
    
    fun searchProducts(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            // Reset to all products or current category filter
            val category = _selectedCategory.value
            if (category != null) {
                filterByCategory(category)
            } else {
                _productsState.value = if (allProducts.isEmpty()) {
                    ProductsState.Empty
                } else {
                    ProductsState.Success(allProducts)
                }
            }
            return
        }
        
        viewModelScope.launch {
            _productsState.value = ProductsState.Loading
            
            KiranaLogger.d(TAG, "Searching products with query: $query")
            
            when (val result = searchProductsUseCase(storeId, query)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _productsState.value = ProductsState.Empty
                    } else {
                        _productsState.value = ProductsState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to search products", result.exception)
                    _productsState.value = ProductsState.Error(
                        result.exception.message ?: "Failed to search products"
                    )
                }
                is Result.Loading -> {
                    _productsState.value = ProductsState.Loading
                }
            }
        }
    }
    
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
        
        viewModelScope.launch {
            _productsState.value = ProductsState.Loading
            
            KiranaLogger.d(TAG, "Filtering products by category: $category")
            
            when (val result = filterProductsByCategoryUseCase(storeId, category)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _productsState.value = ProductsState.Empty
                    } else {
                        _productsState.value = ProductsState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to filter products", result.exception)
                    _productsState.value = ProductsState.Error(
                        result.exception.message ?: "Failed to filter products"
                    )
                }
                is Result.Loading -> {
                    _productsState.value = ProductsState.Loading
                }
            }
        }
    }
    
    fun clearCategoryFilter() {
        _selectedCategory.value = null
        _searchQuery.value = ""
        _productsState.value = if (allProducts.isEmpty()) {
            ProductsState.Empty
        } else {
            ProductsState.Success(allProducts)
        }
    }
    
    fun refresh() {
        loadStoreDetails()
        loadProducts()
    }
    
    fun getCategories(): List<String> {
        return allProducts.map { it.category }.distinct().sorted()
    }
}

/**
 * UI State for Store Details
 */
sealed class StoreState {
    object Loading : StoreState()
    data class Success(val store: Store) : StoreState()
    data class Error(val message: String) : StoreState()
}

/**
 * UI State for Products
 */
sealed class ProductsState {
    object Loading : ProductsState()
    object Empty : ProductsState()
    data class Success(val products: List<Product>) : ProductsState()
    data class Error(val message: String) : ProductsState()
}
