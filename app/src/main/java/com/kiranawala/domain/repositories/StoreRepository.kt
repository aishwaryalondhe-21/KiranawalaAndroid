package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import kotlinx.coroutines.flow.Flow

/**
 * Store repository interface
 * Defines contract for store-related operations
 */
interface StoreRepository {
    /**
     * Fetch nearby stores based on location
     * @param latitude User latitude
     * @param longitude User longitude
     * @param radiusKm Search radius in kilometers
     * @return Result containing list of stores or error
     */
    suspend fun fetchNearbyStores(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Result<List<Store>>
    
    /**
     * Get store by ID
     * @param storeId Store ID
     * @return Result containing store or error
     */
    suspend fun getStoreById(storeId: String): Result<Store>
    
    /**
     * Search stores by name or keywords
     * @param query Search query
     * @param latitude User latitude for sorting by distance
     * @param longitude User longitude for sorting by distance
     * @return Result containing list of stores or error
     */
    suspend fun searchStores(
        query: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Store>>
    
    /**
     * Observe stores from local cache
     * @return Flow emitting list of cached stores
     */
    fun observeStores(): Flow<List<Store>>
    
    /**
     * Refresh stores from remote
     * @param latitude User latitude
     * @param longitude User longitude
     * @return Result indicating success or error
     */
    suspend fun refreshStores(latitude: Double, longitude: Double): Result<Unit>
}
