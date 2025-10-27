package com.kiranawala.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("kiranawala_prefs")

private val jsonFormatter = Json { ignoreUnknownKeys = true; encodeDefaults = true }
private val recentSearchSerializer = ListSerializer(String.serializer())

class PreferencesManager(private val context: Context) {
    
    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val LAST_LOCATION_LAT = doublePreferencesKey("last_location_lat")
        val LAST_LOCATION_LNG = doublePreferencesKey("last_location_lng")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val ADDRESS_RECENT_SEARCHES = stringPreferencesKey("address_recent_searches")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    fun getUserId(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: ""
    }
    
    fun getUserPhone(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_PHONE] ?: ""
    }
    
    val fcmToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FCM_TOKEN]
    }
    
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    val recentAddressSearches: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[ADDRESS_RECENT_SEARCHES]?.let { stored ->
            runCatching { jsonFormatter.decodeFromString(recentSearchSerializer, stored) }
                .getOrNull()
        } ?: emptyList()
    }
    
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }
    
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }
    
    suspend fun saveIsLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }
    
    suspend fun saveUserData(userId: String, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_PHONE] = phone
            preferences[IS_LOGGED_IN] = true
        }
    }
    
    suspend fun saveLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[LAST_LOCATION_LAT] = latitude
            preferences[LAST_LOCATION_LNG] = longitude
        }
    }
    
    suspend fun saveFCMToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN] = token
        }
    }
    
    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun addRecentAddressSearch(query: String) {
        val sanitized = query.trim()
        if (sanitized.isEmpty()) return
        context.dataStore.edit { preferences ->
            val existing = preferences[ADDRESS_RECENT_SEARCHES]?.let { stored ->
                runCatching { jsonFormatter.decodeFromString(recentSearchSerializer, stored) }
                    .getOrDefault(emptyList())
            } ?: emptyList()
            val updated = listOf(sanitized) + existing.filterNot { it.equals(sanitized, ignoreCase = true) }
            preferences[ADDRESS_RECENT_SEARCHES] = jsonFormatter.encodeToString(
                recentSearchSerializer,
                updated.take(5)
            )
        }
    }
    
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}