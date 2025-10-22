package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Order
import kotlinx.coroutines.flow.Flow

/**
 * Order repository interface
 * Defines contract for order-related operations
 */
interface OrderRepository {
    /**
     * Place a new order
     */
    suspend fun placeOrder(order: Order): String
    
    /**
     * Get order by ID
     */
    suspend fun getOrderById(orderId: String): Order?
    
    /**
     * Get all orders for customer
     */
    fun getCustomerOrders(customerId: String): Flow<List<Order>>
    
    /**
     * Cancel an order
     */
    suspend fun cancelOrder(orderId: String)
}
