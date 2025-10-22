package com.kiranawala.data.repositories

import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AddressRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Implementation of AddressRepository using Supabase
 */
class AddressRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : AddressRepository {
    
    companion object {
        private const val TAG = "AddressRepository"
        private const val TABLE_NAME = "addresses"
    }
    
    @Serializable
    private data class AddressRow(
        val id: String,
        val user_id: String,
        val address_line: String,
        val building_name: String? = null,
        val flat_number: String? = null,
        val latitude: Double,
        val longitude: Double,
        val label: String,
        val is_default: Boolean,
        val created_at: String,
        val updated_at: String
    )
    
    @Serializable
    private data class AddressInsert(
        val user_id: String,
        val address_line: String,
        val building_name: String? = null,
        val flat_number: String? = null,
        val latitude: Double,
        val longitude: Double,
        val label: String,
        val is_default: Boolean
    )
    
    @Serializable
    private data class AddressUpdate(
        val address_line: String? = null,
        val building_name: String? = null,
        val flat_number: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val label: String? = null,
        val is_default: Boolean? = null
    )
    
    override fun getUserAddresses(userId: String): Flow<List<Address>> = flow {
        try {
            KiranaLogger.d(TAG, "Fetching addresses for user: $userId")
            
            val rows = postgrest.from(TABLE_NAME)
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<AddressRow>()
                .sortedByDescending { it.created_at }
            
            val addresses = rows.map { it.toDomainModel() }
            KiranaLogger.d(TAG, "Fetched ${addresses.size} addresses")
            emit(addresses)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch addresses", e)
            emit(emptyList())
        }
    }
    
    override suspend fun getDefaultAddress(userId: String): Result<Address?> {
        return try {
            KiranaLogger.d(TAG, "Fetching default address for user: $userId")
            
            val rows = postgrest.from(TABLE_NAME)
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                    }
                    limit(1)
                }
                .decodeList<AddressRow>()
            
            val address = rows.firstOrNull()?.toDomainModel()
            KiranaLogger.d(TAG, "Default address: ${address?.id}")
            Result.Success(address)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch default address", e)
            Result.Error(e)
        }
    }
    
    override suspend fun getAddressById(addressId: String): Result<Address?> {
        return try {
            KiranaLogger.d(TAG, "Fetching address by ID: $addressId")
            
            val rows = postgrest.from(TABLE_NAME)
                .select(Columns.ALL) {
                    filter {
                        eq("id", addressId)
                    }
                    limit(1)
                }
                .decodeList<AddressRow>()
            
            val address = rows.firstOrNull()?.toDomainModel()
            KiranaLogger.d(TAG, "Address found: ${address != null}")
            Result.Success(address)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch address by ID", e)
            Result.Error(e)
        }
    }
    
    override suspend fun addAddress(address: Address): Result<String> {
        return try {
            KiranaLogger.d(TAG, "Adding new address for user: ${address.userId}")
            
            val insertData = AddressInsert(
                user_id = address.userId,
                address_line = address.addressLine,
                building_name = address.buildingName,
                flat_number = address.flatNumber,
                latitude = address.latitude,
                longitude = address.longitude,
                label = address.label,
                is_default = address.isDefault
            )
            
            val result = postgrest.from(TABLE_NAME)
                .insert(insertData) {
                    select(Columns.list("id"))
                }
                .decodeSingle<AddressRow>()
            
            KiranaLogger.d(TAG, "Address added successfully: ${result.id}")
            Result.Success(result.id)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to add address", e)
            Result.Error(e)
        }
    }
    
    override suspend fun updateAddress(address: Address): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Updating address: ${address.id}")
            
            val updateData = AddressUpdate(
                address_line = address.addressLine,
                building_name = address.buildingName,
                flat_number = address.flatNumber,
                latitude = address.latitude,
                longitude = address.longitude,
                label = address.label,
                is_default = address.isDefault
            )
            
            postgrest.from(TABLE_NAME)
                .update(updateData) {
                    filter {
                        eq("id", address.id)
                    }
                }
            
            KiranaLogger.d(TAG, "Address updated successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to update address", e)
            Result.Error(e)
        }
    }
    
    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Deleting address: $addressId")
            
            postgrest.from(TABLE_NAME)
                .delete {
                    filter {
                        eq("id", addressId)
                    }
                }
            
            KiranaLogger.d(TAG, "Address deleted successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to delete address", e)
            Result.Error(e)
        }
    }
    
    override suspend fun setDefaultAddress(addressId: String, userId: String): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Setting default address: $addressId for user: $userId")
            
            // First, unset all default addresses for this user
            postgrest.from(TABLE_NAME)
                .update(mapOf("is_default" to false)) {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                    }
                }
            
            // Then set the new default
            postgrest.from(TABLE_NAME)
                .update(mapOf("is_default" to true)) {
                    filter {
                        eq("id", addressId)
                    }
                }
            
            KiranaLogger.d(TAG, "Default address set successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to set default address", e)
            Result.Error(e)
        }
    }
    
    private fun AddressRow.toDomainModel(): Address {
        return Address(
            id = id,
            userId = user_id,
            addressLine = address_line,
            buildingName = building_name,
            flatNumber = flat_number,
            latitude = latitude,
            longitude = longitude,
            label = label,
            isDefault = is_default,
            createdAt = Instant.parse(created_at).toLocalDateTime(TimeZone.UTC),
            updatedAt = Instant.parse(updated_at).toLocalDateTime(TimeZone.UTC)
        )
    }
}
