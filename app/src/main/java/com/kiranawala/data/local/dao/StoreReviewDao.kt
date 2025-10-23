package com.kiranawala.data.local.dao

import androidx.room.*
import com.kiranawala.data.local.entities.StoreReviewEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Store Reviews
 * Handles local database operations for reviews
 */
@Dao
interface StoreReviewDao {
    
    /**
     * Insert a review
     * Replaces if already exists (upsert behavior)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: StoreReviewEntity)
    
    /**
     * Insert multiple reviews
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<StoreReviewEntity>)
    
    /**
     * Get all reviews for a specific store
     * Returns a Flow for reactive updates
     */
    @Query("SELECT * FROM store_reviews WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getStoreReviews(storeId: String): Flow<List<StoreReviewEntity>>
    
    /**
     * Get all reviews for a store sorted by rating (highest first)
     */
    @Query("SELECT * FROM store_reviews WHERE storeId = :storeId ORDER BY rating DESC, createdAt DESC")
    fun getStoreReviewsByRating(storeId: String): Flow<List<StoreReviewEntity>>
    
    /**
     * Get a specific review by ID
     */
    @Query("SELECT * FROM store_reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: String): StoreReviewEntity?
    
    /**
     * Get a customer's review for a specific store
     */
    @Query("SELECT * FROM store_reviews WHERE storeId = :storeId AND customerId = :customerId")
    suspend fun getCustomerReview(storeId: String, customerId: String): StoreReviewEntity?
    
    /**
     * Delete a review by ID
     */
    @Query("DELETE FROM store_reviews WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: String)
    
    /**
     * Delete all reviews for a store
     */
    @Query("DELETE FROM store_reviews WHERE storeId = :storeId")
    suspend fun deleteStoreReviews(storeId: String)
    
    /**
     * Delete all reviews
     */
    @Query("DELETE FROM store_reviews")
    suspend fun deleteAllReviews()
    
    /**
     * Get count of reviews for a store
     */
    @Query("SELECT COUNT(*) FROM store_reviews WHERE storeId = :storeId")
    suspend fun getReviewCount(storeId: String): Int
    
    /**
     * Get average rating for a store
     */
    @Query("SELECT AVG(rating) FROM store_reviews WHERE storeId = :storeId")
    suspend fun getAverageRating(storeId: String): Float?
}
