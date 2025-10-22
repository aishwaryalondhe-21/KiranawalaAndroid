package com.kiranawala.data.repositories

import com.kiranawala.domain.models.AppSettings
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.models.SecuritySettings
import com.kiranawala.domain.models.UserAddress
import com.kiranawala.domain.models.UserProfile
import com.kiranawala.domain.repositories.ProfileRepository
import com.kiranawala.data.local.dao.CustomerDao
import com.kiranawala.data.local.preferences.PreferencesManager
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProfileRepository
 * Manages user profile, addresses, and settings
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val customerDao: CustomerDao,
    private val authRepository: AuthRepository,
    private val supabase: SupabaseClient
) : ProfileRepository {
    
    companion object {
        private const val TAG = "ProfileRepository"
    }
    
    override fun getUserProfile(): Flow<UserProfile?> {
        return flow {
            when (val result = authRepository.getCurrentUserProfile()) {
                is Result.Success -> {
                    val customer = result.data
                    if (customer != null) {
                        val profile = UserProfile(
                            id = customer.id,
                            phone = customer.phone,
                            name = customer.name,
                            address = customer.address,
                            profileImageUrl = null, // No image support yet
                            addresses = emptyList(), // TODO: Load addresses separately
                            defaultAddressId = null,
                            createdAt = customer.createdAt,
                            updatedAt = customer.updatedAt
                        )
                        emit(profile)
                    } else {
                        emit(null)
                    }
                }
                else -> emit(null)
            }
        }
    }
    
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Updating profile for user: ${profile.id}")
            
            // Update in Supabase first
            supabase.from("customers").update(
                {
                    set("name", profile.name.ifEmpty { "Enter Name" })
                    set("address", profile.address.ifEmpty { "Enter Address" })
                    set("updated_at", Clock.System.now().toString())
                }
            ) {
                filter {
                    eq("id", profile.id)
                }
            }
            
            KiranaLogger.d(TAG, "Profile updated in Supabase successfully")
            
            // Then update local database
            val customer = customerDao.getCustomerById(profile.id)
            if (customer != null) {
                val updatedCustomer = customer.copy(
                    name = profile.name.ifEmpty { "Enter Name" },
                    address = profile.address.ifEmpty { "Enter Address" },
                    updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )
                customerDao.updateCustomer(updatedCustomer)
                KiranaLogger.d(TAG, "Profile updated in local DB successfully")
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to update profile", e)
            Result.Error(e)
        }
    }
    
    override suspend fun updateProfileImage(imageUri: String): Result<String> {
        return try {
            // TODO: Implement image upload logic
            Result.Success(imageUri)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getUserAddresses(): Flow<List<UserAddress>> {
        // Return empty list for now
        return flowOf(emptyList())
    }
    
    override suspend fun addAddress(address: UserAddress): Result<UserAddress> {
        return try {
            // TODO: Implement address addition logic
            Result.Success(address)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateAddress(address: UserAddress): Result<Unit> {
        return try {
            // TODO: Implement address update logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        return try {
            // TODO: Implement address deletion logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun setDefaultAddress(addressId: String): Result<Unit> {
        return try {
            // TODO: Implement default address logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getAppSettings(): Result<AppSettings> {
        return try {
            val settings = AppSettings()
            Result.Success(settings)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateAppSettings(settings: AppSettings): Result<Unit> {
        return try {
            // TODO: Implement settings update logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getSecuritySettings(): Result<SecuritySettings> {
        return try {
            val settings = SecuritySettings()
            Result.Success(settings)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateSecuritySettings(settings: SecuritySettings): Result<Unit> {
        return try {
            // TODO: Implement security settings update logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            // TODO: Implement account deletion logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}