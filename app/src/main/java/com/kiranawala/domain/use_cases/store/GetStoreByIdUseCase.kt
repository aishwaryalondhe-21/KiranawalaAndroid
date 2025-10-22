package com.kiranawala.domain.use_cases.store

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.Store
import com.kiranawala.domain.repositories.StoreRepository
import javax.inject.Inject

/**
 * Use case to get store details by ID
 */
class GetStoreByIdUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(storeId: String): Result<Store> {
        return storeRepository.getStoreById(storeId)
    }
}
