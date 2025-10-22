package com.kiranawala.data.local.dao

import androidx.room.*
import com.kiranawala.data.local.entities.OrderEntity
import com.kiranawala.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)
    
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    suspend fun getCustomerOrders(customerId: String): List<OrderEntity>
    
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun observeCustomerOrders(customerId: String): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrder(orderId: String): OrderEntity?
    
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>
    
    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)
    
    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}