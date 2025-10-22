package com.kiranawala.utils.validators

/**
 * Phone number validation utility
 * Validates Indian phone numbers (10 digits)
 */
object PhoneValidator {
    
    private val PHONE_PATTERN = Regex("^[6-9]\\d{9}$")
    
    /**
     * Validates if the given phone number is valid
     * @param phone Phone number string to validate
     * @return true if phone is valid, false otherwise
     */
    fun isValid(phone: String): Boolean {
        val cleanPhone = phone.replace("\\s".toRegex(), "")
        return cleanPhone.length == 10 && PHONE_PATTERN.matches(cleanPhone)
    }
    
    /**
     * Validates phone and returns error message if invalid
     * @param phone Phone number string to validate
     * @return null if valid, error message if invalid
     */
    fun validate(phone: String): String? {
        val cleanPhone = phone.replace("\\s".toRegex(), "")
        return when {
            cleanPhone.isBlank() -> "Phone number cannot be empty"
            cleanPhone.length != 10 -> "Phone number must be 10 digits"
            !PHONE_PATTERN.matches(cleanPhone) -> "Invalid phone number format"
            else -> null
        }
    }
    
    /**
     * Formats phone number with country code
     * @param phone 10-digit phone number
     * @return Formatted phone with +91 prefix
     */
    fun formatWithCountryCode(phone: String): String {
        val cleanPhone = phone.replace("\\s".toRegex(), "")
        return if (isValid(cleanPhone)) "+91$cleanPhone" else phone
    }
}
