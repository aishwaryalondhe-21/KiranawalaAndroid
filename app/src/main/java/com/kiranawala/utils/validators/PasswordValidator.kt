package com.kiranawala.utils.validators

/**
 * Password validation utility
 * Enforces strong password requirements
 */
object PasswordValidator {
    
    private const val MIN_LENGTH = 8
    private const val MAX_LENGTH = 128
    
    /**
     * Validates if the password meets all requirements
     * @param password Password string to validate
     * @return true if password is valid, false otherwise
     */
    fun isValid(password: String): Boolean {
        return password.length >= MIN_LENGTH &&
               password.length <= MAX_LENGTH &&
               password.any { it.isDigit() } &&
               password.any { it.isLetter() }
    }
    
    /**
     * Validates password and returns detailed error message if invalid
     * @param password Password string to validate
     * @return null if valid, error message if invalid
     */
    fun validate(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < MIN_LENGTH -> "Password must be at least $MIN_LENGTH characters"
            password.length > MAX_LENGTH -> "Password must not exceed $MAX_LENGTH characters"
            !password.any { it.isDigit() } -> "Password must contain at least one digit"
            !password.any { it.isLetter() } -> "Password must contain at least one letter"
            else -> null
        }
    }
    
    /**
     * Checks password strength
     * @param password Password to check
     * @return Strength level: WEAK, MEDIUM, STRONG
     */
    fun getStrength(password: String): PasswordStrength {
        if (password.length < MIN_LENGTH) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score >= 4 -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
