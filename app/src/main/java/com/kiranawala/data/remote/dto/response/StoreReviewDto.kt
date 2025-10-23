package com.kiranawala.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Store Review from Supabase API
 * Maps to store_reviews table in database
 */
@Serializable
data class StoreReviewDto(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("store_id")
    val store_id: String,
    
    @SerialName("customer_id")
    val customer_id: String,
    
    @SerialName("customer_name")
    val customer_name: String,
    
    @SerialName("rating")
    val rating: Int,
    
    @SerialName("comment")
    val comment: String? = null,
    
    @SerialName("created_at")
    val created_at: String? = null,
    
    @SerialName("updated_at")
    val updated_at: String? = null
)
