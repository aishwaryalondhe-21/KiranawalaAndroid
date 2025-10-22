package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.StoreDao
import com.kiranawala.data.local.entities.StoreEntity
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.repositories.StoreRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Store repository implementation with Supabase
 */
class StoreRepositoryImpl @Inject constructor(
    private val storeDao: StoreDao,
    private val postgrest: Postgrest
) : StoreRepository {
    
    companion object {
        private const val TAG = "StoreRepository"
        private const val EARTH_RADIUS_KM = 6371.0
    }
    
    @Serializable
    data class StoreDto(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val contact: String,
        val logo_url: String? = null,
        val rating: Float = 4.5f,
        val minimum_order_value: Double = 100.0,
        val delivery_fee: Double = 30.0,
        val estimated_delivery_time: Int = 30,
        val is_open: Boolean = true,
        val subscription_status: String = "ACTIVE",
        val created_at: String,
        val updated_at: String
    )
    
    override suspend fun fetchNearbyStores(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Store>> {
        return try {
            KiranaLogger.d(TAG, "Fetching nearby stores for lat=$latitude, lon=$longitude, radius=$radiusKm km")
            
            // First, fetch ALL stores to debug
            val allStores = postgrest["stores"]
                .select(Columns.ALL)
                .decodeList<StoreDto>()
            
            KiranaLogger.d(TAG, "Found ${allStores.size} total stores in database:")
            allStores.forEach { store ->
                KiranaLogger.d(TAG, "Store: ${store.name} | Status: ${store.subscription_status} | Open: ${store.is_open}")
            }
            
            // Filter for active and open stores
            val activeStores = allStores.filter { store ->
                store.subscription_status == "ACTIVE" && store.is_open
            }
            
            KiranaLogger.d(TAG, "Found ${activeStores.size} active stores after filtering")
            
            // Filter by distance and convert to domain model
            val nearbyStores = activeStores
                .map { it.toDomainModel() }
                .filter { store ->
                    val distance = calculateDistance(latitude, longitude, store.latitude, store.longitude)
                    KiranaLogger.d(TAG, "Store: ${store.name} | Distance: ${distance}km | Within ${radiusKm}km: ${distance <= radiusKm}")
                    distance <= radiusKm
                }
                .sortedBy { store ->
                    calculateDistance(latitude, longitude, store.latitude, store.longitude)
                }
            
            // Cache in local database
            storeDao.insertStores(nearbyStores.map { it.toEntity() })
            
            KiranaLogger.d(TAG, "Found ${nearbyStores.size} nearby stores")
            Result.Success(nearbyStores)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch nearby stores", e)
            // Fallback to cached data
            val cachedStores = storeDao.getAllStores().map { it.toDomainModel() }
            if (cachedStores.isNotEmpty()) {
                Result.Success(cachedStores)
            } else {
                Result.Error(e)
            }
        }
    }
    
    override suspend fun getStoreById(storeId: String): Result<Store> {
        return try {
            KiranaLogger.d(TAG, "Fetching store by ID: $storeId")
            
            val storeDto = postgrest["stores"]
                .select(Columns.ALL) {
                    filter {
                        eq("id", storeId)
                    }
                }
                .decodeSingle<StoreDto>()
            
            val store = storeDto.toDomainModel()
            
            // Cache in local database
            storeDao.insertStore(store.toEntity())
            
            Result.Success(store)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch store by ID", e)
            // Fallback to cached data
            val cachedStore = storeDao.getStoreById(storeId)?.toDomainModel()
            if (cachedStore != null) {
                Result.Success(cachedStore)
            } else {
                Result.Error(e)
            }
        }
    }
    
    override suspend fun searchStores(
        query: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Store>> {
        return try {
            KiranaLogger.d(TAG, "Searching stores with query: $query")
            
            val stores = postgrest["stores"]
                .select(Columns.ALL) {
                    filter {
                        eq("subscription_status", "ACTIVE")
                        eq("is_open", true)
                        or {
                            ilike("name", "%$query%")
                            ilike("address", "%$query%")
                        }
                    }
                }
                .decodeList<StoreDto>()
            
            // Apply same distance filtering as fetchNearbyStores
            val radiusKm = 5.0 // Use same 5km radius for search
            val searchResults = stores
                .map { it.toDomainModel() }
                .filter { store ->
                    val distance = calculateDistance(latitude, longitude, store.latitude, store.longitude)
                    KiranaLogger.d(TAG, "Search result: ${store.name} | Distance: ${distance}km | Within ${radiusKm}km: ${distance <= radiusKm}")
                    distance <= radiusKm
                }
                .sortedBy { store ->
                    calculateDistance(latitude, longitude, store.latitude, store.longitude)
                }
            
            KiranaLogger.d(TAG, "Found ${searchResults.size} stores matching query within ${radiusKm}km")
            Result.Success(searchResults)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to search stores", e)
            Result.Error(e)
        }
    }
    
    override fun observeStores(): Flow<List<Store>> {
        return storeDao.observeStores().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun refreshStores(latitude: Double, longitude: Double): Result<Unit> {
        return try {
            fetchNearbyStores(latitude, longitude)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    // Helper function to calculate distance between two coordinates (Haversine formula)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_KM * c
    }
    
    // Extension functions for mapping
    private fun StoreDto.toDomainModel(): Store {
        return Store(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            contact = contact,
            logo = logo_url,
            rating = rating,
            minimumOrderValue = minimum_order_value,
            deliveryFee = delivery_fee,
            estimatedDeliveryTime = estimated_delivery_time,
            isOpen = is_open,
            subscriptionStatus = subscription_status,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }
    
    private fun Store.toEntity(): StoreEntity {
        return StoreEntity(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            contact = contact,
            logo = logo,
            rating = rating,
            minimumOrderValue = minimumOrderValue,
            deliveryFee = deliveryFee,
            estimatedDeliveryTime = estimatedDeliveryTime,
            isOpen = isOpen,
            subscriptionStatus = subscriptionStatus,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun StoreEntity.toDomainModel(): Store {
        return Store(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            contact = contact,
            logo = logo,
            rating = rating,
            minimumOrderValue = minimumOrderValue,
            deliveryFee = deliveryFee,
            estimatedDeliveryTime = estimatedDeliveryTime,
            isOpen = isOpen,
            subscriptionStatus = subscriptionStatus,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
