package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Cart
import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.use_cases.cart.*
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for shopping cart
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CartViewModel"
    }

    private val _cartState = MutableStateFlow<CartState>(CartState.Loading)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private var customerId: String = ""

    fun initialize(customerId: String) {
        KiranaLogger.d(TAG, "Initializing cart for customer: $customerId")
        this.customerId = customerId
        observeCart()
    }

    private fun observeCart() {
        KiranaLogger.d(TAG, "Starting to observe cart for customer: $customerId")
        viewModelScope.launch {
            getCartUseCase(customerId).collect { result ->
                KiranaLogger.d(TAG, "Cart update received: ${result::class.simpleName}")
                when (result) {
                    is Result.Success -> {
                        val cart = result.data
                        if (cart == null) {
                            KiranaLogger.d(TAG, "Cart is empty")
                            _cartState.value = CartState.Empty
                        } else {
                            KiranaLogger.d(TAG, "Cart loaded: ${cart.items.size} items, total: ₹${cart.total}")
                            _cartState.value = CartState.Success(cart)
                        }
                    }
                    is Result.Error -> {
                        KiranaLogger.e(TAG, "Failed to load cart", result.exception)
                        _cartState.value = CartState.Error(
                            result.exception.message ?: "Failed to load cart"
                        )
                    }
                    is Result.Loading -> {
                        KiranaLogger.d(TAG, "Cart loading...")
                        _cartState.value = CartState.Loading
                    }
                }
            }
        }
    }

    private val _addToCartError = MutableStateFlow<String?>(null)
    val addToCartError: StateFlow<String?> = _addToCartError.asStateFlow()
    
    fun addToCart(storeId: String, product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            KiranaLogger.d(TAG, "Adding ${product.name} to cart")
            when (val result = addToCartUseCase(customerId, storeId, product, quantity)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Product added to cart successfully")
                    _addToCartError.value = null
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to add to cart", result.exception)
                    _addToCartError.value = result.exception.message
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun clearAddToCartError() {
        _addToCartError.value = null
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            KiranaLogger.d(TAG, "Removing product from cart")
            when (val result = removeFromCartUseCase(customerId, productId)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Product removed from cart")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to remove from cart", result.exception)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            KiranaLogger.d(TAG, "Updating quantity to $quantity")
            when (val result = updateCartItemQuantityUseCase(customerId, productId, quantity)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Quantity updated")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to update quantity", result.exception)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            KiranaLogger.d(TAG, "Clearing cart")
            when (val result = clearCartUseCase(customerId)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Cart cleared")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to clear cart", result.exception)
                }
                is Result.Loading -> {}
            }
        }
    }
}

sealed class CartState {
    object Loading : CartState()
    object Empty : CartState()
    data class Success(val cart: Cart) : CartState()
    data class Error(val message: String) : CartState()
}
