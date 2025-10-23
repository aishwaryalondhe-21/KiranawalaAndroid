package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.StoreReviewDao
import com.kiranawala.data.local.entities.StoreReviewEntity
import com.kiranawala.data.remote.dto.response.StoreReviewDto
import com.kiranawala.domain.models.AppException
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.StoreReview
import com.kiranawala.domain.repositories.StoreReviewRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of StoreReviewRepository
 * Handles review operations with Supabase backend and Room cache
 */
class StoreReviewRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val reviewDao: StoreReviewDao
) : StoreReviewRepository {
    
    companion object {
        private const val TAG = "StoreReviewRepository"
        private const val TABLE_NAME = "store_reviews"
    }
    
    override suspend fun addReview(
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ): Result<String> {
        return try {
            KiranaLogger.d(TAG, "Adding review for store: $storeId by customer: $customerId")
            
            val reviewDto = StoreReviewDto(
                store_id = storeId,
                customer_id = customerId,
                customer_name = customerName,
                rating = rating,
                comment = comment
            )
            
            // Insert to Supabase
            val insertedReview = supabase.postgrest[TABLE_NAME]
                .insert(reviewDto) {
                    select()
                }
                .decodeSingle<StoreReviewDto>()
            
            val reviewId = insertedReview.id ?: UUID.randomUUID().toString()
            KiranaLogger.d(TAG, "Review added successfully with ID: $reviewId")
            
            // Cache locally
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val entity = StoreReviewEntity(
                id = reviewId,
                storeId = storeId,
                customerId = customerId,
                customerName = customerName,
                rating = rating,
                comment = comment,
                createdAt = now,
                updatedAt = now
            )
            reviewDao.insertReview(entity)
            
            // Update store rating after adding review
            updateStoreRating(storeId)
            
            Result.Success(reviewId)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to add review", e)
            Result.Error(AppException.NetworkException("Failed to add review: ${e.message}"))
        }
    }
    
    override fun getStoreReviews(storeId: String): Flow<List<StoreReview>> = flow {
        KiranaLogger.d(TAG, "Fetching reviews for store: $storeId")
        
        try {
            // Fetch from Supabase
            val reviewDtos = supabase.postgrest[TABLE_NAME]
                .select(Columns.ALL) {
                    filter {
                        eq("store_id", storeId)
                    }
                }
                .decodeList<StoreReviewDto>()
            
            KiranaLogger.d(TAG, "Fetched ${reviewDtos.size} reviews from Supabase")
            
            // Convert to domain models
            val reviews = reviewDtos.mapNotNull { dto ->
                toDomainModel(dto)
            }
            
            // Cache locally
            val entities = reviews.map { review ->
                StoreReviewEntity(
                    id = review.id,
                    storeId = review.storeId,
                    customerId = review.customerId,
                    customerName = review.customerName,
                    rating = review.rating,
                    comment = review.comment,
                    createdAt = review.createdAt,
                    updatedAt = review.updatedAt
                )
            }
            reviewDao.insertReviews(entities)
            
            emit(reviews)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch reviews from Supabase, using cache", e)
            
            // Fallback to local cache
            reviewDao.getStoreReviews(storeId)
                .map { entities ->
                    entities.map { entity ->
                        StoreReview(
                            id = entity.id,
                            storeId = entity.storeId,
                            customerId = entity.customerId,
                            customerName = entity.customerName,
                            rating = entity.rating,
                            comment = entity.comment,
                            createdAt = entity.createdAt,
                            updatedAt = entity.updatedAt
                        )
                    }
                }
                .collect { emit(it) }
        }
    }
    
    override suspend fun deleteReview(reviewId: String, customerId: String): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Deleting review: $reviewId")
            
            // Get store ID before deleting
            val review = reviewDao.getReviewById(reviewId)
            val storeId = review?.storeId
            
            // Delete from Supabase (RLS will ensure customer owns the review)
            supabase.postgrest[TABLE_NAME]
                .delete {
                    filter {
                        eq("id", reviewId)
                        eq("customer_id", customerId)
                    }
                }
            
            // Delete from local cache
            reviewDao.deleteReview(reviewId)
            
            // Update store rating after deletion
            storeId?.let { updateStoreRating(it) }
            
            KiranaLogger.d(TAG, "Review deleted successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to delete review", e)
            Result.Error(AppException.NetworkException("Failed to delete review: ${e.message}"))
        }
    }
    
    override suspend fun getCustomerReview(storeId: String, customerId: String): StoreReview? {
        return try {
            // Try from Supabase first
            val reviewDto = supabase.postgrest[TABLE_NAME]
                .select(Columns.ALL) {
                    filter {
                        eq("store_id", storeId)
                        eq("customer_id", customerId)
                    }
                }
                .decodeSingleOrNull<StoreReviewDto>()
            
            reviewDto?.let { toDomainModel(it) }
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch customer review, using cache", e)
            
            // Fallback to cache
            val entity = reviewDao.getCustomerReview(storeId, customerId)
            entity?.let {
                StoreReview(
                    id = it.id,
                    storeId = it.storeId,
                    customerId = it.customerId,
                    customerName = it.customerName,
                    rating = it.rating,
                    comment = it.comment,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }
    }
    
    override suspend fun updateReview(
        reviewId: String,
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Updating review: $reviewId")
            
            val reviewDto = StoreReviewDto(
                id = reviewId,
                store_id = storeId,
                customer_id = customerId,
                customer_name = customerName,
                rating = rating,
                comment = comment
            )
            
            // Update in Supabase
            supabase.postgrest[TABLE_NAME]
                .update(reviewDto) {
                    filter {
                        eq("id", reviewId)
                        eq("customer_id", customerId)
                    }
                }
            
            // Update local cache
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val entity = StoreReviewEntity(
                id = reviewId,
                storeId = storeId,
                customerId = customerId,
                customerName = customerName,
                rating = rating,
                comment = comment,
                createdAt = now,
                updatedAt = now
            )
            reviewDao.insertReview(entity) // Upsert
            
            // Update store rating after review update
            updateStoreRating(storeId)
            
            KiranaLogger.d(TAG, "Review updated successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to update review", e)
            Result.Error(AppException.NetworkException("Failed to update review: ${e.message}"))
        }
    }
    
    override suspend fun updateStoreRating(storeId: String): Result<Float> {
        return try {
            // Fetch all reviews for the store
            val reviewDtos = supabase.postgrest[TABLE_NAME]
                .select(Columns.ALL) {
                    filter {
                        eq("store_id", storeId)
                    }
                }
                .decodeList<StoreReviewDto>()
            
            // Calculate average rating
            val averageRating = if (reviewDtos.isNotEmpty()) {
                reviewDtos.map { it.rating }.average().toFloat()
            } else {
                4.5f // Default rating when no reviews
            }
            
            // Update stores table with new rating
            supabase.postgrest["stores"]
                .update(mapOf("rating" to averageRating)) {
                    filter {
                        eq("id", storeId)
                    }
                }
            
            KiranaLogger.d(TAG, "Store $storeId rating updated to: $averageRating")
            Result.Success(averageRating)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to update store rating", e)
            
            // Fallback to local calculation
            val localRating = reviewDao.getAverageRating(storeId) ?: 4.5f
            Result.Success(localRating)
        }
    }
    
    /**
     * Convert DTO to domain model
     */
    private fun toDomainModel(dto: StoreReviewDto): StoreReview? {
        return try {
            val id = dto.id ?: return null
            val createdAt = dto.created_at?.let { 
                Instant.parse(it).toLocalDateTime(TimeZone.UTC) 
            } ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            
            val updatedAt = dto.updated_at?.let { 
                Instant.parse(it).toLocalDateTime(TimeZone.UTC) 
            } ?: createdAt
            
            StoreReview(
                id = id,
                storeId = dto.store_id,
                customerId = dto.customer_id,
                customerName = dto.customer_name,
                rating = dto.rating,
                comment = dto.comment,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to convert DTO to domain model", e)
            null
        }
    }
}
