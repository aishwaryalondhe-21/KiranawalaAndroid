// TEMPORARY DEBUG VERSION - Replace fetchNearbyStores method in StoreRepositoryImpl.kt
// This will show ALL stores regardless of status for debugging

override suspend fun fetchNearbyStores(
    latitude: Double,
    longitude: Double,
    radiusKm: Double
): Result<List<Store>> {
    return try {
        KiranaLogger.d(TAG, "DEBUG: Fetching ALL stores (ignoring filters)")
        
        // TEMP: Fetch ALL stores without filters for debugging
        val stores = postgrest["stores"]
            .select(Columns.ALL)
            .decodeList<StoreDto>()
        
        KiranaLogger.d(TAG, "DEBUG: Found ${stores.size} total stores in database")
        stores.forEach { store ->
            KiranaLogger.d(TAG, "DEBUG: Store: ${store.name}, Status: ${store.subscription_status}, Open: ${store.is_open}")
        }
        
        // Convert to domain model and filter by distance
        val nearbyStores = stores
            .map { it.toDomainModel() }
            .filter { store ->
                calculateDistance(latitude, longitude, store.latitude, store.longitude) <= radiusKm
            }
            .sortedBy { store ->
                calculateDistance(latitude, longitude, store.latitude, store.longitude)
            }
        
        // Cache in local database
        storeDao.insertStores(nearbyStores.map { it.toEntity() })
        
        KiranaLogger.d(TAG, "DEBUG: Returning ${nearbyStores.size} nearby stores")
        Result.Success(nearbyStores)
    } catch (e: Exception) {
        KiranaLogger.e(TAG, "DEBUG: Failed to fetch stores", e)
        // Fallback to cached data
        val cachedStores = storeDao.getAllStores().map { it.toDomainModel() }
        if (cachedStores.isNotEmpty()) {
            Result.Success(cachedStores)
        } else {
            Result.Error(e)
        }
    }
}