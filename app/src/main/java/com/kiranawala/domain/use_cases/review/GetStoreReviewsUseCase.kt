package com.kiranawala.domain.use_cases.review

import com.kiranawala.domain.models.StoreReview
import com.kiranawala.domain.repositories.StoreReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all reviews for a store
 * Returns a Flow for reactive updates
 */
class GetStoreReviewsUseCase @Inject constructor(
    private val repository: StoreReviewRepository
) {
    operator fun invoke(storeId: String): Flow<List<StoreReview>> {
        return repository.getStoreReviews(storeId)
    }
}
