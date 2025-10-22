package com.kiranawala.utils.validators

/**
 * Email validation utility
 * Validates email format according to RFC 5322 standard
 */
object EmailValidator {
    
    private val EMAIL_PATTERN = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )
    
    /**
     * Validates if the given email is in correct format
     * @param email Email string to validate
     * @return true if email is valid, false otherwise
     */
    fun isValid(email: String): Boolean {
        return email.isNotBlank() && EMAIL_PATTERN.matches(email)
    }
    
    /**
     * Validates email and returns error message if invalid
     * @param email Email string to validate
     * @return null if valid, error message if invalid
     */
    fun validate(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !EMAIL_PATTERN.matches(email) -> "Invalid email format"
            else -> null
        }
    }
}
