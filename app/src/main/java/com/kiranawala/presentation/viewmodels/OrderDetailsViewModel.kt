package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.use_cases.order.GetOrderByIdUseCase
import com.kiranawala.domain.use_cases.order.CancelOrderUseCase
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Order Details Screen
 */
@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "OrderDetailsViewModel"
    }
    
    private val _orderDetailsState = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val orderDetailsState: StateFlow<OrderDetailsState> = _orderDetailsState.asStateFlow()
    
    private val _cancelOrderState = MutableStateFlow<CancelOrderState>(CancelOrderState.Idle)
    val cancelOrderState: StateFlow<CancelOrderState> = _cancelOrderState.asStateFlow()
    
    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _orderDetailsState.value = OrderDetailsState.Loading
            
            KiranaLogger.d(TAG, "Loading order details for order: $orderId")
            
            when (val result = getOrderByIdUseCase(orderId)) {
                is Result.Success -> {
                    _orderDetailsState.value = OrderDetailsState.Success(result.data)
                    KiranaLogger.d(TAG, "Loaded order details successfully")
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to load order details", result.exception)
                    _orderDetailsState.value = OrderDetailsState.Error(
                        result.exception.message ?: "Failed to load order details"
                    )
                }
                is Result.Loading -> {
                    _orderDetailsState.value = OrderDetailsState.Loading
                }
            }
        }
    }
    
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _cancelOrderState.value = CancelOrderState.Loading
            
            KiranaLogger.d(TAG, "Cancelling order: $orderId")
            
            when (val result = cancelOrderUseCase(orderId)) {
                is Result.Success -> {
                    _cancelOrderState.value = CancelOrderState.Success
                    KiranaLogger.d(TAG, "Order cancelled successfully")
                    // Reload order details to show updated status
                    loadOrderDetails(orderId)
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to cancel order", result.exception)
                    _cancelOrderState.value = CancelOrderState.Error(
                        result.exception.message ?: "Failed to cancel order"
                    )
                }
                is Result.Loading -> {
                    _cancelOrderState.value = CancelOrderState.Loading
                }
            }
        }
    }
    
    fun clearCancelOrderState() {
        _cancelOrderState.value = CancelOrderState.Idle
    }
}

/**
 * UI State for Order Details Screen
 */
sealed class OrderDetailsState {
    object Loading : OrderDetailsState()
    data class Success(val order: Order) : OrderDetailsState()
    data class Error(val message: String) : OrderDetailsState()
}

/**
 * UI State for Cancel Order Operation
 */
sealed class CancelOrderState {
    object Idle : CancelOrderState()
    object Loading : CancelOrderState()
    object Success : CancelOrderState()
    data class Error(val message: String) : CancelOrderState()
}