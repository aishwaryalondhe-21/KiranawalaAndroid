package com.kiranawala.data.local.encrypted_storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted storage manager for sensitive data
 * Uses AndroidX Security library for encryption
 */
@Singleton
class EncryptedStorageManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val ENCRYPTED_PREFS_NAME = "kiranawala_encrypted_prefs"
        private const val TAG = "EncryptedStorage"
    }
    
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedSharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to create encrypted preferences")
            throw e
        }
    }
    
    /**
     * Saves encrypted string value
     * @param key Storage key
     * @param value String value to encrypt and store
     */
    fun saveEncrypted(key: String, value: String) {
        try {
            encryptedSharedPreferences.edit().putString(key, value).apply()
            Timber.tag(TAG).d("Saved encrypted value for key: $key")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to save encrypted value for key: $key")
        }
    }
    
    /**
     * Retrieves encrypted string value
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Decrypted string value or default
     */
    fun getEncrypted(key: String, defaultValue: String? = null): String? {
        return try {
            encryptedSharedPreferences.getString(key, defaultValue)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get encrypted value for key: $key")
            defaultValue
        }
    }
    
    /**
     * Saves encrypted boolean value
     * @param key Storage key
     * @param value Boolean value to encrypt and store
     */
    fun saveEncryptedBoolean(key: String, value: Boolean) {
        try {
            encryptedSharedPreferences.edit().putBoolean(key, value).apply()
            Timber.tag(TAG).d("Saved encrypted boolean for key: $key")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to save encrypted boolean for key: $key")
        }
    }
    
    /**
     * Retrieves encrypted boolean value
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Decrypted boolean value or default
     */
    fun getEncryptedBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            encryptedSharedPreferences.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get encrypted boolean for key: $key")
            defaultValue
        }
    }
    
    /**
     * Removes encrypted value
     * @param key Storage key to remove
     */
    fun removeEncrypted(key: String) {
        try {
            encryptedSharedPreferences.edit().remove(key).apply()
            Timber.tag(TAG).d("Removed encrypted value for key: $key")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to remove encrypted value for key: $key")
        }
    }
    
    /**
     * Clears all encrypted data
     */
    fun clearAll() {
        try {
            encryptedSharedPreferences.edit().clear().apply()
            Timber.tag(TAG).d("Cleared all encrypted data")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to clear encrypted data")
        }
    }
    
    /**
     * Checks if key exists in encrypted storage
     * @param key Storage key to check
     * @return true if key exists, false otherwise
     */
    fun contains(key: String): Boolean {
        return try {
            encryptedSharedPreferences.contains(key)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to check if key exists: $key")
            false
        }
    }
}
