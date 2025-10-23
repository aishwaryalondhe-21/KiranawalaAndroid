package com.kiranawala.domain.use_cases.review

import com.kiranawala.domain.models.AppException
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.StoreReviewRepository
import javax.inject.Inject

/**
 * Use case for adding a store review
 * Validates input and delegates to repository
 */
class AddStoreReviewUseCase @Inject constructor(
    private val repository: StoreReviewRepository
) {
    suspend operator fun invoke(
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ): Result<String> {
        // Validate rating
        if (rating < 1 || rating > 5) {
            return Result.Error(AppException.ValidationException("Rating must be between 1 and 5"))
        }
        
        // Validate IDs
        if (storeId.isBlank() || customerId.isBlank()) {
            return Result.Error(AppException.ValidationException("Store ID and Customer ID are required"))
        }
        
        // Validate customer name
        if (customerName.isBlank()) {
            return Result.Error(AppException.ValidationException("Customer name is required"))
        }
        
        // Validate comment length if provided
        if (comment != null && comment.length > 500) {
            return Result.Error(AppException.ValidationException("Comment must be 500 characters or less"))
        }
        
        return repository.addReview(
            storeId = storeId,
            customerId = customerId,
            customerName = customerName,
            rating = rating,
            comment = comment?.trim()
        )
    }
}
