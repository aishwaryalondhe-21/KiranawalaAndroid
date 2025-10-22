package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.ProductDao
import com.kiranawala.data.local.entities.ProductEntity
import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.ProductRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Product repository implementation with Supabase
 */
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val postgrest: Postgrest
) : ProductRepository {
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    @Serializable
    data class ProductDto(
        val id: String,
        val store_id: String,
        val name: String,
        val description: String,
        val price: Double,
        val stock_quantity: Int,
        val image_url: String? = null,
        val category: String = "General",
        val is_available: Boolean = true
    )
    
    override suspend fun fetchStoreProducts(storeId: String): Result<List<Product>> {
        return try {
            KiranaLogger.d(TAG, "Fetching products for store: $storeId")
            
            val products = postgrest["products"]
                .select(Columns.ALL) {
                    filter {
                        eq("store_id", storeId)
                        eq("is_available", true)
                    }
                }
                .decodeList<ProductDto>()
            
            val productList = products.map { it.toDomainModel() }
            
            // Cache in local database
            productDao.insertProducts(productList.map { it.toEntity() })
            
            KiranaLogger.d(TAG, "Found ${productList.size} products for store $storeId")
            Result.Success(productList)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch store products", e)
            // Fallback to cached data
            val cachedProducts = productDao.getProductsByStore(storeId).map { it.toDomainModel() }
            if (cachedProducts.isNotEmpty()) {
                Result.Success(cachedProducts)
            } else {
                Result.Error(e)
            }
        }
    }
    
    override suspend fun getProductById(productId: String): Result<Product> {
        return try {
            KiranaLogger.d(TAG, "Fetching product by ID: $productId")
            
            val productDto = postgrest["products"]
                .select(Columns.ALL) {
                    filter {
                        eq("id", productId)
                    }
                }
                .decodeSingle<ProductDto>()
            
            val product = productDto.toDomainModel()
            
            // Cache in local database
            productDao.insertProduct(product.toEntity())
            
            Result.Success(product)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch product by ID", e)
            // Fallback to cached data
            val cachedProduct = productDao.getProductById(productId)?.toDomainModel()
            if (cachedProduct != null) {
                Result.Success(cachedProduct)
            } else {
                Result.Error(e)
            }
        }
    }
    
    override suspend fun searchProducts(storeId: String, query: String): Result<List<Product>> {
        return try {
            KiranaLogger.d(TAG, "Searching products in store $storeId with query: $query")
            
            val products = postgrest["products"]
                .select(Columns.ALL) {
                    filter {
                        eq("store_id", storeId)
                        eq("is_available", true)
                        or {
                            ilike("name", "%$query%")
                            ilike("description", "%$query%")
                            ilike("category", "%$query%")
                        }
                    }
                }
                .decodeList<ProductDto>()
            
            val searchResults = products.map { it.toDomainModel() }
            
            KiranaLogger.d(TAG, "Found ${searchResults.size} products matching query")
            Result.Success(searchResults)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to search products", e)
            Result.Error(e)
        }
    }
    
    override fun observeStoreProducts(storeId: String): Flow<List<Product>> {
        return productDao.observeProductsByStore(storeId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun filterByCategory(storeId: String, category: String): Result<List<Product>> {
        return try {
            KiranaLogger.d(TAG, "Filtering products by category: $category in store $storeId")
            
            val products = postgrest["products"]
                .select(Columns.ALL) {
                    filter {
                        eq("store_id", storeId)
                        eq("category", category)
                        eq("is_available", true)
                    }
                }
                .decodeList<ProductDto>()
            
            val filteredProducts = products.map { it.toDomainModel() }
            
            KiranaLogger.d(TAG, "Found ${filteredProducts.size} products in category $category")
            Result.Success(filteredProducts)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to filter products by category", e)
            Result.Error(e)
        }
    }
    
    // Extension functions for mapping
    private fun ProductDto.toDomainModel(): Product {
        return Product(
            id = id,
            storeId = store_id,
            name = name,
            description = description,
            price = price,
            stockQuantity = stock_quantity,
            imageUrl = image_url,
            category = category
        )
    }
    
    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
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
    
    private fun ProductEntity.toDomainModel(): Product {
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
