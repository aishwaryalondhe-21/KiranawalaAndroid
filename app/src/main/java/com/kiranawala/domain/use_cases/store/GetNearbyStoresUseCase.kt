package com.kiranawala.domain.use_cases.store

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.repositories.StoreRepository
import javax.inject.Inject

/**
 * Use case to fetch nearby stores based on user location
 */
class GetNearbyStoresUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Result<List<Store>> {
        return storeRepository.fetchNearbyStores(latitude, longitude, radiusKm)
    }
}
