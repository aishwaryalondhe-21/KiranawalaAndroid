package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.use_cases.order.GetCustomerOrdersUseCase
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Order History Screen
 */
@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getCustomerOrdersUseCase: GetCustomerOrdersUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "OrderHistoryViewModel"
    }
    
    private val _orderHistoryState = MutableStateFlow<OrderHistoryState>(OrderHistoryState.Loading)
    val orderHistoryState: StateFlow<OrderHistoryState> = _orderHistoryState.asStateFlow()
    
    fun loadOrderHistory(customerId: String) {
        viewModelScope.launch {
            _orderHistoryState.value = OrderHistoryState.Loading
            
            KiranaLogger.d(TAG, "Loading order history for customer: $customerId")
            
            getCustomerOrdersUseCase(customerId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val orders = result.data
                        if (orders.isEmpty()) {
                            _orderHistoryState.value = OrderHistoryState.Empty
                        } else {
                            _orderHistoryState.value = OrderHistoryState.Success(orders)
                        }
                        KiranaLogger.d(TAG, "Loaded ${orders.size} orders")
                    }
                    is Result.Error -> {
                        KiranaLogger.e(TAG, "Failed to load order history", result.exception)
                        _orderHistoryState.value = OrderHistoryState.Error(
                            result.exception.message ?: "Failed to load orders"
                        )
                    }
                    is Result.Loading -> {
                        _orderHistoryState.value = OrderHistoryState.Loading
                    }
                }
            }
        }
    }
    
    fun refresh(customerId: String) {
        loadOrderHistory(customerId)
    }
}

/**
 * UI State for Order History Screen
 */
sealed class OrderHistoryState {
    object Loading : OrderHistoryState()
    object Empty : OrderHistoryState()
    data class Success(val orders: List<Order>) : OrderHistoryState()
    data class Error(val message: String) : OrderHistoryState()
}