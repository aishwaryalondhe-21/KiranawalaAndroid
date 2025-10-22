package com.kiranawala.domain.models

/**
 * Custom exception hierarchy for the application
 * Provides type-safe error handling across all layers
 */
sealed class AppException(message: String) : Exception(message) {
    
    /**
     * Network-related exceptions
     */
    data class NetworkException(val error: String) : AppException(error) {
        companion object {
            fun noConnection() = NetworkException("No internet connection")
            fun timeout() = NetworkException("Request timeout")
            fun unknown() = NetworkException("Network error occurred")
        }
    }
    
    /**
     * Authentication-related exceptions
     */
    data class AuthenticationException(val error: String) : AppException(error) {
        companion object {
            fun invalidCredentials() = AuthenticationException("Invalid email or password")
            fun userNotFound() = AuthenticationException("User not found")
            fun sessionExpired() = AuthenticationException("Session expired, please login again")
            fun unauthorized() = AuthenticationException("Unauthorized access")
            fun emailAlreadyExists() = AuthenticationException("Email already registered")
        }
    }
    
    /**
     * Validation-related exceptions
     */
    data class ValidationException(val error: String) : AppException(error) {
        companion object {
            fun invalidEmail() = ValidationException("Invalid email format")
            fun invalidPhone() = ValidationException("Invalid phone number")
            fun weakPassword() = ValidationException("Password is too weak")
            fun emptyField(fieldName: String) = ValidationException("$fieldName cannot be empty")
            fun minOrderValue(minValue: Double) = 
                ValidationException("Minimum order value is ₹$minValue")
        }
    }
    
    /**
     * Server-related exceptions
     */
    data class ServerException(val code: Int, val error: String) : AppException(error) {
        companion object {
            fun badRequest() = ServerException(400, "Bad request")
            fun notFound() = ServerException(404, "Resource not found")
            fun internalError() = ServerException(500, "Internal server error")
            fun serviceUnavailable() = ServerException(503, "Service unavailable")
        }
    }
    
    /**
     * Local database exceptions
     */
    data class LocalDatabaseException(val error: String) : AppException(error) {
        companion object {
            fun saveFailed() = LocalDatabaseException("Failed to save data locally")
            fun fetchFailed() = LocalDatabaseException("Failed to fetch data from local database")
            fun deleteFailed() = LocalDatabaseException("Failed to delete data")
        }
    }
    
    /**
     * Business logic exceptions
     */
    data class BusinessException(val error: String) : AppException(error) {
        companion object {
            fun storeNotAvailable() = BusinessException("Store is currently not available")
            fun productOutOfStock() = BusinessException("Product is out of stock")
            fun orderCancellationFailed() = BusinessException("Cannot cancel order at this stage")
            fun paymentFailed() = BusinessException("Payment failed, please try again")
            fun invalidOTP() = BusinessException("Invalid OTP entered")
        }
    }
    
    /**
     * Unknown/Generic exceptions
     */
    data class UnknownException(val error: String) : AppException(error) {
        companion object {
            fun generic(message: String? = null) = 
                UnknownException(message ?: "An unknown error occurred")
        }
    }
}

/**
 * Extension function to convert generic exceptions to AppException
 */
fun Exception.toAppException(): AppException {
    return when (this) {
        is AppException -> this
        else -> AppException.UnknownException.generic(this.message)
    }
}
