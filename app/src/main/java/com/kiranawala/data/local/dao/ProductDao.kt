package com.kiranawala.data.local.dao

import androidx.room.*
import com.kiranawala.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Query("SELECT * FROM products WHERE storeId = :storeId")
    suspend fun getProductsByStore(storeId: String): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE storeId = :storeId")
    fun observeProductsByStore(storeId: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE storeId = :storeId AND name LIKE '%' || :query || '%'")
    suspend fun searchProducts(storeId: String, query: String): List<ProductEntity>
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM products WHERE storeId = :storeId")
    suspend fun deleteStoreProducts(storeId: String)
}