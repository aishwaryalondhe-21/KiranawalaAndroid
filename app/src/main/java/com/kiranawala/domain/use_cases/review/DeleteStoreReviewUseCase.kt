package com.kiranawala.domain.use_cases.review

import com.kiranawala.domain.models.AppException
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.StoreReviewRepository
import javax.inject.Inject

/**
 * Use case for deleting a store review
 * Validates ownership and delegates to repository
 */
class DeleteStoreReviewUseCase @Inject constructor(
    private val repository: StoreReviewRepository
) {
    suspend operator fun invoke(reviewId: String, customerId: String): Result<Unit> {
        // Validate IDs
        if (reviewId.isBlank() || customerId.isBlank()) {
            return Result.Error(AppException.ValidationException("Review ID and Customer ID are required"))
        }
        
        return repository.deleteReview(reviewId, customerId)
    }
}
