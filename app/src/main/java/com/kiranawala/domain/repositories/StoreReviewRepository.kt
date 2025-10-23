package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.StoreReview
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Store Reviews
 * Defines operations for managing store reviews
 */
interface StoreReviewRepository {
    
    /**
     * Add a new review for a store
     * @param storeId The store to review
     * @param customerId The customer adding the review
     * @param customerName The customer's name
     * @param rating Rating from 1-5 stars
     * @param comment Optional comment text
     * @return Result with review ID on success
     */
    suspend fun addReview(
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ): Result<String>
    
    /**
     * Get all reviews for a specific store
     * @param storeId The store ID
     * @return Flow of reviews list
     */
    fun getStoreReviews(storeId: String): Flow<List<StoreReview>>
    
    /**
     * Delete a review
     * @param reviewId The review to delete
     * @param customerId The customer requesting deletion (must match review owner)
     * @return Result indicating success or failure
     */
    suspend fun deleteReview(reviewId: String, customerId: String): Result<Unit>
    
    /**
     * Get a customer's review for a specific store
     * @param storeId The store ID
     * @param customerId The customer ID
     * @return The review if it exists, null otherwise
     */
    suspend fun getCustomerReview(storeId: String, customerId: String): StoreReview?
    
    /**
     * Update an existing review
     * @param reviewId The review ID to update
     * @param storeId The store ID
     * @param customerId The customer ID (must match review owner)
     * @param customerName The customer's name
     * @param rating Updated rating from 1-5 stars
     * @param comment Updated comment text
     * @return Result indicating success or failure
     */
    suspend fun updateReview(
        reviewId: String,
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ): Result<Unit>
    
    /**
     * Calculate and update the average rating for a store
     * Updates the stores table rating column
     * @param storeId The store ID
     * @return Result with the new average rating
     */
    suspend fun updateStoreRating(storeId: String): Result<Float>
}
