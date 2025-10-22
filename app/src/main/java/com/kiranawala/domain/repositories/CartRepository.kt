package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Cart
import com.kiranawala.domain.models.Product
import kotlinx.coroutines.flow.Flow

/**
 * Cart repository interface
 * Defines contract for shopping cart operations
 */
interface CartRepository {
    /**
     * Add item to cart
     */
    suspend fun addToCart(
        customerId: String,
        storeId: String,
        product: Product,
        quantity: Int
    )
    
    /**
     * Remove item from cart
     */
    suspend fun removeFromCart(customerId: String, productId: String)
    
    /**
     * Update cart item quantity
     */
    suspend fun updateQuantity(
        customerId: String,
        productId: String,
        quantity: Int
    )
    
    /**
     * Get cart for customer (returns current cart regardless of store)
     */
    fun getCart(customerId: String): Flow<Cart?>
    
    /**
     * Clear entire cart
     */
    suspend fun clearCart(customerId: String)
}
