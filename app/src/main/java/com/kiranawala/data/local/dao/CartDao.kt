package com.kiranawala.data.local.dao

import androidx.room.*
import com.kiranawala.data.local.entities.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartEntity)
    
    @Query("SELECT * FROM cart WHERE customerId = :customerId AND storeId = :storeId")
    suspend fun getCart(customerId: String, storeId: String): List<CartEntity>
    
    @Query("SELECT * FROM cart WHERE customerId = :customerId")
    suspend fun getCartByCustomer(customerId: String): List<CartEntity>
    
    @Query("SELECT * FROM cart WHERE customerId = :customerId")
    fun observeCartByCustomer(customerId: String): Flow<List<CartEntity>>
    
    @Query("SELECT * FROM cart WHERE customerId = :customerId AND storeId = :storeId")
    fun observeCart(customerId: String, storeId: String): Flow<List<CartEntity>>
    
    @Query("DELETE FROM cart WHERE customerId = :customerId AND productId = :productId")
    suspend fun removeFromCart(customerId: String, productId: String)
    
    @Query("DELETE FROM cart WHERE customerId = :customerId AND storeId = :storeId")
    suspend fun clearCart(customerId: String, storeId: String)
    
    @Query("DELETE FROM cart WHERE customerId = :customerId")
    suspend fun clearCartByCustomer(customerId: String)
    
    @Update
    suspend fun updateCartItem(item: CartEntity)
}