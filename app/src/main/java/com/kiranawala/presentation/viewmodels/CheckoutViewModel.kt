package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Cart
import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.OrderItem
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.use_cases.cart.ClearCartUseCase
import com.kiranawala.domain.use_cases.order.PlaceOrderUseCase
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import com.kiranawala.utils.preferences.CustomerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * ViewModel for checkout process
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val customerPreferences: CustomerPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CheckoutViewModel"
    }

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _deliveryAddress = MutableStateFlow(customerPreferences.deliveryAddress)
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

    private val _customerPhone = MutableStateFlow(customerPreferences.customerPhone)
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _customerName = MutableStateFlow(customerPreferences.customerName)
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    init {
        prefillCustomerDetails()
    }

    fun setDeliveryAddress(address: String) {
        _deliveryAddress.value = address
        customerPreferences.deliveryAddress = address
    }

    fun setCustomerPhone(phone: String) {
        _customerPhone.value = phone
        customerPreferences.customerPhone = phone
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
        customerPreferences.customerName = name
    }

    private fun prefillCustomerDetails() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUserProfile()) {
                is Result.Success -> {
                    val profile = result.data
                    if (profile != null) {
                        if (profile.name.isNotBlank()) {
                            _customerName.value = profile.name
                            customerPreferences.customerName = profile.name
                        }
                        if (profile.phone.isNotBlank()) {
                            _customerPhone.value = profile.phone
                            customerPreferences.customerPhone = profile.phone
                        }
                        if (profile.address.isNotBlank()) {
                            _deliveryAddress.value = profile.address
                            customerPreferences.deliveryAddress = profile.address
                        }
                        KiranaLogger.d(TAG, "Prefilled checkout details from profile: ${profile.id}")
                    }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to prefill customer details", result.exception)
                }
                else -> Unit
            }
        }
    }

    fun placeOrder(customerId: String, cart: Cart) {
        viewModelScope.launch {
            if (!validateCheckoutData()) {
                _checkoutState.value = CheckoutState.Error("Please fill all required fields")
                return@launch
            }

            _checkoutState.value = CheckoutState.Loading
            KiranaLogger.d(TAG, "Placing order for store ${cart.storeId}")

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            
            val order = Order(
                id = "",
                customerId = customerId,
                storeId = cart.storeId,
                storeName = cart.storeName,
                totalAmount = cart.total,
                deliveryFee = cart.deliveryFee,
                status = "PENDING",
                items = cart.items.map { cartItem ->
                    OrderItem(
                        id = "",
                        orderId = "",
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        price = cartItem.product.price
                    )
                },
                deliveryAddress = _deliveryAddress.value,
                customerPhone = _customerPhone.value,
                customerName = _customerName.value,
                createdAt = now,
                updatedAt = now
            )

            when (val result = placeOrderUseCase(order)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Order placed successfully: ${result.data}")
                    
                    // Clear cart after successful order
                    clearCartUseCase(customerId)
                    
                    _checkoutState.value = CheckoutState.Success(result.data)
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to place order", result.exception)
                    _checkoutState.value = CheckoutState.Error(
                        result.exception.message ?: "Failed to place order"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun validateCheckoutData(): Boolean {
        return _deliveryAddress.value.isNotBlank() &&
                _customerPhone.value.isNotBlank() &&
                _customerName.value.isNotBlank()
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val orderId: String) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}
