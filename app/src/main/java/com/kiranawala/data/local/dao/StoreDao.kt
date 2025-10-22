package com.kiranawala.data.local.dao

import androidx.room.*
import com.kiranawala.data.local.entities.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)
    
    @Query("SELECT * FROM stores")
    suspend fun getAllStores(): List<StoreEntity>
    
    @Query("SELECT * FROM stores")
    fun observeStores(): Flow<List<StoreEntity>>
    
    @Query("SELECT * FROM stores WHERE id = :storeId")
    suspend fun getStoreById(storeId: String): StoreEntity?
    
    @Query("SELECT * FROM stores WHERE name LIKE '%' || :query || '%'")
    suspend fun searchStores(query: String): List<StoreEntity>
    
    @Update
    suspend fun updateStore(store: StoreEntity)
    
    @Delete
    suspend fun deleteStore(store: StoreEntity)
    
    @Query("DELETE FROM stores")
    suspend fun clearAll()
}