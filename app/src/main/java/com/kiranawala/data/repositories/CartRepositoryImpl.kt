package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.CartDao
import com.kiranawala.data.local.dao.ProductDao
import com.kiranawala.data.local.dao.StoreDao
import com.kiranawala.data.local.entities.CartEntity
import com.kiranawala.domain.models.Cart
import com.kiranawala.domain.models.CartItem
import com.kiranawala.domain.models.Product
import com.kiranawala.domain.repositories.CartRepository
import com.kiranawala.utils.logger.KiranaLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Cart repository implementation
 */
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val storeDao: StoreDao
) : CartRepository {
    
    companion object {
        private const val TAG = "CartRepository"
    }
    
    override suspend fun addToCart(
        customerId: String,
        storeId: String,
        product: Product,
        quantity: Int
    ) {
        KiranaLogger.d(TAG, "Adding ${product.name} (x$quantity) to cart")
        
        // Check if cart has items from a different store
        val allCartItems = cartDao.getCartByCustomer(customerId)
        if (allCartItems.isNotEmpty()) {
            val existingStoreId = allCartItems.first().storeId
            if (existingStoreId != storeId) {
                throw IllegalStateException("Cannot add items from different stores. Please clear your cart first.")
            }
        }
        
        // Check if item already exists
        val existingCart = cartDao.getCart(customerId, storeId)
        val existingItem = existingCart.find { it.productId == product.id }
        
        if (existingItem != null) {
            // Update quantity
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
            cartDao.updateCartItem(updatedItem)
        } else {
            // Add new item
            val cartEntity = CartEntity(
                customerId = customerId,
                storeId = storeId,
                productId = product.id,
                quantity = quantity,
                price = product.price
            )
            cartDao.addToCart(cartEntity)
        }
    }
    
    override suspend fun removeFromCart(customerId: String, productId: String) {
        KiranaLogger.d(TAG, "Removing product $productId from cart")
        cartDao.removeFromCart(customerId, productId)
    }
    
    override suspend fun updateQuantity(
        customerId: String,
        productId: String,
        quantity: Int
    ) {
        KiranaLogger.d(TAG, "Updating product $productId quantity to $quantity")
        
        // Get all cart items for customer
        val allItems = cartDao.getCartByCustomer(customerId)
        val item = allItems.find { it.productId == productId }
        
        if (item != null) {
            val updatedItem = item.copy(quantity = quantity)
            cartDao.updateCartItem(updatedItem)
        }
    }
    
    override fun getCart(customerId: String): Flow<Cart?> {
        // Observe all cart items for customer (we only support single store cart)
        return cartDao.observeCartByCustomer(customerId).map { cartEntities ->
            KiranaLogger.d(TAG, "Cart updated: ${cartEntities.size} items")
            if (cartEntities.isEmpty()) {
                null
            } else {
                try {
                    // Group by store (should only be one)
                    val storeId = cartEntities.first().storeId
                    val store = storeDao.getStoreById(storeId)
                    
                    val cartItems = cartEntities.mapNotNull { entity ->
                        val productEntity = productDao.getProductById(entity.productId)
                        productEntity?.let {
                            CartItem(
                                product = it.toDomainModel(),
                                quantity = entity.quantity,
                                storeId = storeId
                            )
                        }
                    }
                    
                    if (store != null && cartItems.isNotEmpty()) {
                        Cart(
                            storeId = storeId,
                            storeName = store.name,
                            items = cartItems,
                            minimumOrderValue = store.minimumOrderValue,
                            deliveryFee = store.deliveryFee
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    KiranaLogger.e(TAG, "Error building cart", e)
                    null
                }
            }
        }
    }
    
    override suspend fun clearCart(customerId: String) {
        KiranaLogger.d(TAG, "Clearing cart for customer $customerId")
        // Clear all items for customer
        cartDao.clearCartByCustomer(customerId)
    }
    
    private fun com.kiranawala.data.local.entities.ProductEntity.toDomainModel(): Product {
        return Product(
            id = id,
            storeId = storeId,
            name = name,
            description = description,
            price = price,
            stockQuantity = stockQuantity,
            imageUrl = imageUrl,
            category = category
        )
    }
}
