package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Product repository interface
 * Defines contract for product-related operations
 */
interface ProductRepository {
    /**
     * Fetch products for a specific store
     * @param storeId Store ID
     * @return Result containing list of products or error
     */
    suspend fun fetchStoreProducts(storeId: String): Result<List<Product>>
    
    /**
     * Get product by ID
     * @param productId Product ID
     * @return Result containing product or error
     */
    suspend fun getProductById(productId: String): Result<Product>
    
    /**
     * Search products within a store
     * @param storeId Store ID
     * @param query Search query
     * @return Result containing list of products or error
     */
    suspend fun searchProducts(storeId: String, query: String): Result<List<Product>>
    
    /**
     * Observe products for a store from local cache
     * @param storeId Store ID
     * @return Flow emitting list of products
     */
    fun observeStoreProducts(storeId: String): Flow<List<Product>>
    
    /**
     * Filter products by category
     * @param storeId Store ID
     * @param category Product category
     * @return Result containing list of products or error
     */
    suspend fun filterByCategory(storeId: String, category: String): Result<List<Product>>
}
