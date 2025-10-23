package com.kiranawala.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.StoreReview
import com.kiranawala.domain.use_cases.review.AddStoreReviewUseCase
import com.kiranawala.domain.use_cases.review.DeleteStoreReviewUseCase
import com.kiranawala.domain.use_cases.review.GetStoreReviewsUseCase
import com.kiranawala.domain.use_cases.review.UpdateStoreReviewUseCase
import com.kiranawala.domain.repositories.StoreReviewRepository
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing store reviews
 * Handles review listing, adding, deleting, and sorting
 */
@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val getStoreReviewsUseCase: GetStoreReviewsUseCase,
    private val addStoreReviewUseCase: AddStoreReviewUseCase,
    private val updateStoreReviewUseCase: UpdateStoreReviewUseCase,
    private val deleteStoreReviewUseCase: DeleteStoreReviewUseCase,
    private val reviewRepository: StoreReviewRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "ReviewsViewModel"
    }
    
    private val _reviewsState = MutableStateFlow<ReviewsState>(ReviewsState.Loading)
    val reviewsState: StateFlow<ReviewsState> = _reviewsState.asStateFlow()
    
    private val _addReviewState = MutableStateFlow<AddReviewState>(AddReviewState.Idle)
    val addReviewState: StateFlow<AddReviewState> = _addReviewState.asStateFlow()
    
    private val _deleteReviewState = MutableStateFlow<DeleteReviewState>(DeleteReviewState.Idle)
    val deleteReviewState: StateFlow<DeleteReviewState> = _deleteReviewState.asStateFlow()
    
    private val _sortOption = MutableStateFlow(ReviewsSortOption.RECENT)
    val sortOption: StateFlow<ReviewsSortOption> = _sortOption.asStateFlow()
    
    private var currentStoreId: String? = null
    private var allReviews: List<StoreReview> = emptyList()
    
    /**
     * Load reviews for a specific store
     */
    fun loadReviews(storeId: String) {
        if (currentStoreId == storeId && _reviewsState.value is ReviewsState.Success) {
            // Already loaded for this store
            return
        }
        
        currentStoreId = storeId
        
        viewModelScope.launch {
            _reviewsState.value = ReviewsState.Loading
            
            try {
                getStoreReviewsUseCase(storeId).collect { reviews ->
                    allReviews = reviews
                    
                    if (reviews.isEmpty()) {
                        _reviewsState.value = ReviewsState.Empty("No reviews yet. Be the first to review!")
                    } else {
                        applySorting()
                    }
                }
            } catch (e: Exception) {
                KiranaLogger.e(TAG, "Failed to load reviews", e)
                _reviewsState.value = ReviewsState.Error("Failed to load reviews: ${e.message}")
            }
        }
    }
    
    /**
     * Check if customer has already reviewed the store
     */
    suspend fun getCustomerReview(storeId: String, customerId: String): StoreReview? {
        return try {
            reviewRepository.getCustomerReview(storeId, customerId)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to get customer review", e)
            null
        }
    }
    
    /**
     * Add or update review (upsert)
     * Checks if customer already has a review and updates it, otherwise creates new one
     */
    fun addOrUpdateReview(
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ) {
        viewModelScope.launch {
            _addReviewState.value = AddReviewState.Loading
            
            // Check if customer already has a review
            val existingReview = getCustomerReview(storeId, customerId)
            
            val result = if (existingReview != null) {
                // Update existing review
                KiranaLogger.d(TAG, "Updating existing review: ${existingReview.id}")
                updateStoreReviewUseCase(
                    reviewId = existingReview.id,
                    storeId = storeId,
                    customerId = customerId,
                    customerName = customerName,
                    rating = rating,
                    comment = comment
                )
            } else {
                // Add new review
                KiranaLogger.d(TAG, "Adding new review")
                addStoreReviewUseCase(
                    storeId = storeId,
                    customerId = customerId,
                    customerName = customerName,
                    rating = rating,
                    comment = comment
                )
            }
            
            when (result) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Review saved successfully")
                    _addReviewState.value = AddReviewState.Success
                    // Reload reviews to show the updated one
                    loadReviews(storeId)
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to save review", result.exception)
                    _addReviewState.value = AddReviewState.Error(
                        result.exception.message ?: "Failed to save review"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Add a new review (legacy - use addOrUpdateReview instead)
     */
    fun addReview(
        storeId: String,
        customerId: String,
        customerName: String,
        rating: Int,
        comment: String?
    ) {
        viewModelScope.launch {
            _addReviewState.value = AddReviewState.Loading
            
            when (val result = addStoreReviewUseCase(
                storeId = storeId,
                customerId = customerId,
                customerName = customerName,
                rating = rating,
                comment = comment
            )) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Review added successfully: ${result.data}")
                    _addReviewState.value = AddReviewState.Success
                    // Reload reviews to show the new one
                    loadReviews(storeId)
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to add review", result.exception)
                    _addReviewState.value = AddReviewState.Error(
                        result.exception.message ?: "Failed to add review"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Delete a review
     */
    fun deleteReview(reviewId: String, customerId: String) {
        viewModelScope.launch {
            _deleteReviewState.value = DeleteReviewState.Loading
            
            when (val result = deleteStoreReviewUseCase(reviewId, customerId)) {
                is Result.Success -> {
                    KiranaLogger.d(TAG, "Review deleted successfully")
                    _deleteReviewState.value = DeleteReviewState.Success
                    // Reload reviews
                    currentStoreId?.let { loadReviews(it) }
                }
                is Result.Error -> {
                    KiranaLogger.e(TAG, "Failed to delete review", result.exception)
                    _deleteReviewState.value = DeleteReviewState.Error(
                        result.exception.message ?: "Failed to delete review"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Change sort option
     */
    fun setSortOption(option: ReviewsSortOption) {
        _sortOption.value = option
        applySorting()
    }
    
    /**
     * Apply current sorting to reviews
     */
    private fun applySorting() {
        val sortedReviews = when (_sortOption.value) {
            ReviewsSortOption.RECENT -> {
                allReviews.sortedByDescending { it.createdAt }
            }
            ReviewsSortOption.TOP_RATED -> {
                allReviews.sortedWith(
                    compareByDescending<StoreReview> { it.rating }
                        .thenByDescending { it.createdAt }
                )
            }
        }
        
        _reviewsState.value = ReviewsState.Success(sortedReviews)
    }
    
    /**
     * Clear add review state
     */
    fun clearAddReviewState() {
        _addReviewState.value = AddReviewState.Idle
    }
    
    /**
     * Clear delete review state
     */
    fun clearDeleteReviewState() {
        _deleteReviewState.value = DeleteReviewState.Idle
    }
    
    /**
     * Refresh reviews
     */
    fun refresh() {
        currentStoreId?.let { loadReviews(it) }
    }
}

/**
 * UI State for reviews list
 */
sealed class ReviewsState {
    object Loading : ReviewsState()
    data class Success(val reviews: List<StoreReview>) : ReviewsState()
    data class Empty(val message: String) : ReviewsState()
    data class Error(val message: String) : ReviewsState()
}

/**
 * UI State for adding a review
 */
sealed class AddReviewState {
    object Idle : AddReviewState()
    object Loading : AddReviewState()
    object Success : AddReviewState()
    data class Error(val message: String) : AddReviewState()
}

/**
 * UI State for deleting a review
 */
sealed class DeleteReviewState {
    object Idle : DeleteReviewState()
    object Loading : DeleteReviewState()
    object Success : DeleteReviewState()
    data class Error(val message: String) : DeleteReviewState()
}

/**
 * Sort options for reviews
 */
enum class ReviewsSortOption {
    RECENT,      // Sort by created_at DESC
    TOP_RATED    // Sort by rating DESC, then created_at DESC
}
