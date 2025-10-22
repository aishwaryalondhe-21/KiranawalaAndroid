package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.CustomerDao
import com.kiranawala.data.local.entities.CustomerEntity
import com.kiranawala.data.local.preferences.PreferencesManager
import com.kiranawala.domain.models.Customer
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Authentication repository implementation with Supabase
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val customerDao: CustomerDao,
    private val preferencesManager: PreferencesManager,
    private val postgrest: Postgrest
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    override suspend fun sendOTP(phone: String): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Sending OTP to phone: $phone")
            
            // Send OTP via Supabase (signInWith works for both login and signup)
            auth.signInWith(OTP) {
                this.phone = phone
                createUser = true  // Allow creating new users
            }
            
            KiranaLogger.d(TAG, "OTP sent successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to send OTP", e)
            Result.Error(e)
        }
    }
    
    override suspend fun verifyOTP(phone: String, otp: String, name: String?): Result<String> {
        return try {
            KiranaLogger.d(TAG, "Verifying OTP for phone: $phone")
            
            // Verify OTP with Supabase  
            auth.verifyPhoneOtp(
                type = io.github.jan.supabase.gotrue.OtpType.Phone.SMS,
                phone = phone,
                token = otp
            )
            
            // Get current session
            val session = auth.currentSessionOrNull()
            val user = session?.user
            val userId = user?.id ?: return Result.Error(Exception("No user ID found"))
            
            // Persist basic session details
            preferencesManager.saveUserData(userId, phone)
            
            // Ensure local profile mirrors Supabase state
            val providedName = name?.takeIf { it.isNotBlank() }
            syncUserProfile(
                user = user,
                fallbackPhone = phone,
                providedName = providedName
            )
            
            KiranaLogger.d(TAG, "OTP verification successful for user: $userId")
            Result.Success(userId)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "OTP verification failed", e)
            Result.Error(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Attempting logout")
            
            // Sign out from Supabase
            auth.signOut()
            
            // Clear local session
            preferencesManager.saveIsLoggedIn(false)
            preferencesManager.saveUserId("")
            
            // Clear local database
            customerDao.deleteAllCustomers()
            
            KiranaLogger.d(TAG, "Logout successful")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Logout failed", e)
            Result.Error(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<String?> {
        return try {
            val session = auth.currentSessionOrNull()
            val userId = session?.user?.id
            KiranaLogger.d(TAG, "Current user: $userId")
            Result.Success(userId)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to get current user", e)
            Result.Error(e)
        }
    }
    
    
    override suspend fun isLoggedIn(): Boolean {
        try {
            // Check both Supabase session and local preferences
            val session = auth.currentSessionOrNull()
            val localLoginStatus = preferencesManager.isLoggedIn.first()
            
            val isLoggedIn = session != null && localLoginStatus
            KiranaLogger.d(TAG, "Is logged in: $isLoggedIn (session: ${session != null}, local: $localLoginStatus)")
            
            if (session == null && localLoginStatus) {
                // Session expired but user was logged in locally, clear local state
                preferencesManager.saveIsLoggedIn(false)
                preferencesManager.saveUserId("")
                return false
            }
            
            return isLoggedIn
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Error checking login status", e)
            return false
        }
    }
    
    override suspend fun getCurrentUserProfile(): Result<Customer?> {
        return try {
            val userId = preferencesManager.getUserId().first()
            if (userId.isEmpty()) {
                return Result.Success(null)
            }
            
            val customerEntity = customerDao.getCustomerById(userId)
                ?: preferencesManager.getUserPhone().first().takeIf { it.isNotBlank() }?.let { phone ->
                    customerDao.getCustomerByPhone(phone)
                        ?: fetchCustomerFromSupabase(phone)?.also { entity ->
                            customerDao.insertCustomer(entity)
                        }
                }
            val customer = customerEntity?.let {
                Customer(
                    id = it.id,
                    phone = it.phone,
                    name = it.name,
                    address = it.address,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
            
            Result.Success(customer)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to get user profile", e)
            Result.Error(e)
        }
    }
    
    private suspend fun syncUserProfile(
        user: UserInfo,
        fallbackPhone: String,
        providedName: String?
    ) {
        try {
            val existingCustomer = customerDao.getCustomerById(user.id)
                ?: customerDao.getCustomerByPhone(fallbackPhone)
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val resolvedPhone = (user.phone ?: fallbackPhone).ifBlank { fallbackPhone }
            val metadataName = user.userMetadata?.get("name") as? String
            val metadataAddress = user.userMetadata?.get("address") as? String
            val remoteProfile = fetchCustomerFromSupabase(resolvedPhone)
            val resolvedName = when {
                !providedName.isNullOrBlank() -> providedName
                !metadataName.isNullOrBlank() -> metadataName
                remoteProfile?.name?.isNotBlank() == true -> remoteProfile.name
                existingCustomer != null && existingCustomer.name.isNotBlank() -> existingCustomer.name
                else -> ""
            }
            val baseEntity = remoteProfile ?: existingCustomer
            val finalEntity = CustomerEntity(
                id = user.id,
                phone = resolvedPhone,
                name = when {
                    resolvedName.isNotBlank() -> resolvedName
                    baseEntity?.name?.isNotBlank() == true -> baseEntity.name
                    else -> metadataName.orEmpty()
                },
                address = when {
                    !metadataAddress.isNullOrBlank() -> metadataAddress
                    baseEntity?.address?.isNotBlank() == true -> baseEntity.address
                    else -> ""
                },
                latitude = baseEntity?.latitude ?: 0.0,
                longitude = baseEntity?.longitude ?: 0.0,
                createdAt = baseEntity?.createdAt ?: now,
                updatedAt = now
            )
            if (existingCustomer == null) {
                customerDao.insertCustomer(finalEntity)
            } else {
                customerDao.updateCustomer(finalEntity)
            }
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to sync user profile", e)
        }
    }

    @Serializable
    private data class CustomerRow(
        val id: String,
        val phone: String,
        val name: String? = null,
        val address: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val created_at: String,
        val updated_at: String
    )

    @OptIn(InternalSerializationApi::class)
    private suspend fun fetchCustomerFromSupabase(phone: String): CustomerEntity? {
        return try {
            val rows = postgrest.from("customers")
                .select(Columns.ALL) {
                    filter {
                        eq("phone", phone)
                    }
                }
                .decodeList<CustomerRow>()
                .sortedByDescending { it.updated_at }
            val row = rows.firstOrNull() ?: return null
            CustomerEntity(
                id = row.id,
                phone = row.phone,
                name = row.name.orEmpty(),
                address = row.address.orEmpty(),
                latitude = row.latitude ?: 0.0,
                longitude = row.longitude ?: 0.0,
                createdAt = Instant.parse(row.created_at).toLocalDateTime(TimeZone.UTC),
                updatedAt = Instant.parse(row.updated_at).toLocalDateTime(TimeZone.UTC)
            )
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch customer from Supabase", e)
            null
        }
    }
}
