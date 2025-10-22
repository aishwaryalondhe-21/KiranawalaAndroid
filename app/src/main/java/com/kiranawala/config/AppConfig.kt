package com.kiranawala.config

import com.kiranawala.BuildConfig

/**
 * Application configuration
 * Centralized configuration management for the app
 */
object AppConfig {
    
    // App Information
    const val APP_NAME = "Kiranawala"
    const val APP_VERSION = BuildConfig.VERSION_NAME
    
    // Supabase Configuration
    val SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
    
    // API Configuration
    const val API_TIMEOUT_SECONDS = 30L
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 30L
    
    // Business Logic Constants
    const val MIN_ORDER_VALUE = 100.0
    const val DEFAULT_DELIVERY_FEE = 30.0
    const val FREE_DELIVERY_THRESHOLD = 500.0
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 5
    
    // Location
    const val DEFAULT_LATITUDE = 19.0760  // Mumbai coordinates
    const val DEFAULT_LONGITUDE = 72.8777
    const val LOCATION_RADIUS_KM = 5.0
    const val LOCATION_UPDATE_INTERVAL_MS = 10000L  // 10 seconds
    
    // Cache Configuration
    const val CACHE_EXPIRY_HOURS = 24
    const val IMAGE_CACHE_SIZE_MB = 50
    
    // Search Configuration
    const val MIN_SEARCH_QUERY_LENGTH = 2
    const val SEARCH_DEBOUNCE_MS = 300L
    
    // Order Configuration
    const val ORDER_CANCELLATION_WINDOW_MINUTES = 5
    const val MAX_CART_ITEMS = 50
    
    // Validation Rules
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 128
    const val PHONE_NUMBER_LENGTH = 10
    
    // Feature Flags
    const val ENABLE_ANALYTICS = true
    const val ENABLE_CRASH_REPORTING = true
    const val ENABLE_OFFLINE_MODE = true
    const val ENABLE_BIOMETRIC_AUTH = true
    
    // Debug Configuration
    val IS_DEBUG: Boolean = BuildConfig.DEBUG
    const val ENABLE_LOGGING = true
    const val ENABLE_NETWORK_LOGGING = true
    
    // Storage Keys
    object StorageKeys {
        const val AUTH_TOKEN = "auth_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_PHONE = "user_phone"
        const val IS_LOGGED_IN = "is_logged_in"
        const val LAST_LOCATION_LAT = "last_location_lat"
        const val LAST_LOCATION_LNG = "last_location_lng"
        const val SELECTED_LANGUAGE = "selected_language"
        const val THEME_MODE = "theme_mode"
    }
    
    // Database Configuration
    object Database {
        const val NAME = "kiranawala.db"
        const val VERSION = 1
    }
    
    // Network Configuration
    object Network {
        const val MAX_RETRY_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1000L
        const val EXPONENTIAL_BACKOFF_MULTIPLIER = 2.0
    }
}
