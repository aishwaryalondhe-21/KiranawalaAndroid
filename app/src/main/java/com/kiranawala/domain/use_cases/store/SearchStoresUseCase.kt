package com.kiranawala.domain.use_cases.store

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.repositories.StoreRepository
import javax.inject.Inject

/**
 * Use case to search stores by name or keywords
 */
class SearchStoresUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(
        query: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Store>> {
        return storeRepository.searchStores(query, latitude, longitude)
    }
}
