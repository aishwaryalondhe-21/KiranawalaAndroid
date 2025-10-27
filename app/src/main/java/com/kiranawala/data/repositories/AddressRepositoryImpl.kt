package com.kiranawala.data.repositories

import com.kiranawala.data.local.dao.AddressDao
import com.kiranawala.data.local.entities.AddressEntity
import com.kiranawala.data.local.preferences.PreferencesManager
import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.AddressType
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.AddressRepository
import com.kiranawala.utils.logger.KiranaLogger
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val addressDao: AddressDao,
    private val preferencesManager: PreferencesManager
) : AddressRepository {

    companion object {
        private const val TAG = "AddressRepository"
        private const val TABLE_NAME = "addresses"
    }

    @Serializable
    private data class AddressRow(
        val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("address_type") val addressType: String? = null,
        val latitude: Double,
        val longitude: Double,
        @SerialName("formatted_address") val formattedAddress: String? = null,
        @SerialName("address_line1") val addressLine1: String? = null,
        @SerialName("address_line2") val addressLine2: String? = null,
        val city: String? = null,
        val state: String? = null,
        val pincode: String? = null,
        @SerialName("receiver_name") val receiverName: String? = null,
        @SerialName("receiver_phone") val receiverPhone: String? = null,
        @SerialName("is_default") val isDefault: Boolean? = null,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String,
        // Legacy schema support
        @SerialName("address_line") val legacyAddressLine: String? = null,
        @SerialName("building_name") val legacyBuildingName: String? = null,
        @SerialName("flat_number") val legacyFlatNumber: String? = null
    )

    @Serializable
    private data class AddressInsert(
        val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("address_type") val addressType: String,
        val latitude: Double,
        val longitude: Double,
        @SerialName("formatted_address") val formattedAddress: String,
        @SerialName("address_line1") val addressLine1: String,
        @SerialName("address_line2") val addressLine2: String?,
        val city: String,
        val state: String,
        val pincode: String,
        @SerialName("receiver_name") val receiverName: String,
        @SerialName("receiver_phone") val receiverPhone: String,
        @SerialName("is_default") val isDefault: Boolean
    )

    @Serializable
    private data class AddressUpdate(
        @SerialName("address_type") val addressType: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        @SerialName("formatted_address") val formattedAddress: String? = null,
        @SerialName("address_line1") val addressLine1: String? = null,
        @SerialName("address_line2") val addressLine2: String? = null,
        val city: String? = null,
        val state: String? = null,
        val pincode: String? = null,
        @SerialName("receiver_name") val receiverName: String? = null,
        @SerialName("receiver_phone") val receiverPhone: String? = null,
        @SerialName("is_default") val isDefault: Boolean? = null
    )

    override fun getUserAddresses(userId: String): Flow<List<Address>> {
        return addressDao.observeAddresses(userId)
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                when (val result = refreshAddresses(userId)) {
                    is Result.Error -> KiranaLogger.e(TAG, "Remote refresh failed", result.exception)
                    else -> Unit
                }
            }
            .catch { exception ->
                KiranaLogger.e(TAG, "Failed to observe addresses", exception)
                throw exception
            }
    }

    override suspend fun getDefaultAddress(userId: String): Result<Address?> {
        return try {
            val local = addressDao.getDefaultAddress(userId)?.toDomain()
            if (local != null) {
                Result.Success(local)
            } else {
                refreshAddresses(userId)
                val refreshed = addressDao.getDefaultAddress(userId)?.toDomain()
                Result.Success(refreshed)
            }
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to get default address", e)
            Result.Error(e)
        }
    }

    override suspend fun getAddressById(addressId: String): Result<Address?> {
        return try {
            val remote = fetchAddressById(addressId)
            Result.Success(remote)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch address by id", e)
            Result.Error(e)
        }
    }

    override suspend fun addAddress(address: Address): Result<String> {
        return try {
            KiranaLogger.d(TAG, "Adding address ${address.id} for user ${address.customerId}")
            val insert = AddressInsert(
                id = address.id,
                userId = address.customerId,
                addressType = address.addressType.name,
                latitude = address.latitude,
                longitude = address.longitude,
                formattedAddress = address.formattedAddress,
                addressLine1 = address.addressLine1,
                addressLine2 = address.addressLine2,
                city = address.city,
                state = address.state,
                pincode = address.pincode,
                receiverName = address.receiverName,
                receiverPhone = address.receiverPhone,
                isDefault = address.isDefault
            )

            postgrest.from(TABLE_NAME).insert(insert)

            persistLocal(address)

            if (address.isDefault) {
                updateDefaultLocally(address.id, address.customerId)
            }

            Result.Success(address.id)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to add address", e)
            Result.Error(e)
        }
    }

    override suspend fun updateAddress(address: Address): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Updating address ${address.id}")
            val update = AddressUpdate(
                addressType = address.addressType.name,
                latitude = address.latitude,
                longitude = address.longitude,
                formattedAddress = address.formattedAddress,
                addressLine1 = address.addressLine1,
                addressLine2 = address.addressLine2,
                city = address.city,
                state = address.state,
                pincode = address.pincode,
                receiverName = address.receiverName,
                receiverPhone = address.receiverPhone,
                isDefault = address.isDefault
            )

            postgrest.from(TABLE_NAME)
                .update(update) {
                    filter { eq("id", address.id) }
                }

            persistLocal(address)

            if (address.isDefault) {
                updateDefaultLocally(address.id, address.customerId)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to update address", e)
            Result.Error(e)
        }
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Deleting address $addressId")
            postgrest.from(TABLE_NAME)
                .delete { filter { eq("id", addressId) } }

            addressDao.deleteAddress(addressId)

            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to delete address", e)
            Result.Error(e)
        }
    }

    override suspend fun setDefaultAddress(addressId: String, userId: String): Result<Unit> {
        return try {
            KiranaLogger.d(TAG, "Setting default address $addressId for $userId")

            postgrest.from(TABLE_NAME)
                .update(mapOf("is_default" to false)) {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                    }
                }

            postgrest.from(TABLE_NAME)
                .update(mapOf("is_default" to true)) {
                    filter { eq("id", addressId) }
                }

            updateDefaultLocally(addressId, userId)

            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to set default address", e)
            Result.Error(e)
        }
    }

    override fun observeRecentSearches(): Flow<List<String>> = preferencesManager.recentAddressSearches

    override suspend fun addRecentSearch(query: String) {
        preferencesManager.addRecentAddressSearch(query)
    }

    override suspend fun refreshAddresses(userId: String): Result<Unit> {
        return try {
            val rows = postgrest.from(TABLE_NAME)
                .select(Columns.ALL) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<AddressRow>()

            val entities = rows.map { it.toEntity() }

            withContext(Dispatchers.IO) {
                addressDao.replaceAddresses(userId, entities)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to refresh addresses", e)
            Result.Error(e)
        }
    }

    private suspend fun fetchAddressById(addressId: String): Address? {
        return try {
            val row = postgrest.from(TABLE_NAME)
                .select(Columns.ALL) {
                    filter { eq("id", addressId) }
                    limit(1)
                }
                .decodeList<AddressRow>()
                .firstOrNull()
            row?.toDomain()
        } catch (e: Exception) {
            KiranaLogger.e(TAG, "Failed to fetch address from remote", e)
            null
        }
    }

    private suspend fun persistLocal(address: Address) {
        withContext(Dispatchers.IO) {
            addressDao.upsertAddress(address.toEntity())
        }
    }

    private fun AddressRow.toDomain(): Address {
        return Address(
            id = id,
            customerId = userId,
            addressType = resolvedAddressType(),
            latitude = latitude,
            longitude = longitude,
            formattedAddress = resolvedFormattedAddress(),
            addressLine1 = resolvedAddressLine1(),
            addressLine2 = resolvedAddressLine2(),
            city = resolvedCity(),
            state = resolvedState(),
            pincode = resolvedPincode(),
            receiverName = resolvedReceiverName(),
            receiverPhone = resolvedReceiverPhone(),
            isDefault = resolvedIsDefault(),
            createdAt = Instant.parse(createdAt).toLocalDateTime(TimeZone.UTC),
            updatedAt = Instant.parse(updatedAt).toLocalDateTime(TimeZone.UTC)
        )
    }

    private fun AddressRow.toEntity(): AddressEntity {
        return AddressEntity(
            id = id,
            customerId = userId,
            addressType = resolvedAddressType().name,
            latitude = latitude,
            longitude = longitude,
            formattedAddress = resolvedFormattedAddress(),
            addressLine1 = resolvedAddressLine1(),
            addressLine2 = resolvedAddressLine2(),
            city = resolvedCity(),
            state = resolvedState(),
            pincode = resolvedPincode(),
            receiverName = resolvedReceiverName(),
            receiverPhone = resolvedReceiverPhone(),
            isDefault = resolvedIsDefault(),
            createdAt = Instant.parse(createdAt).toLocalDateTime(TimeZone.UTC),
            updatedAt = Instant.parse(updatedAt).toLocalDateTime(TimeZone.UTC)
        )
    }

    private fun Address.toEntity(): AddressEntity {
        return AddressEntity(
            id = id,
            customerId = customerId,
            addressType = addressType.name,
            latitude = latitude,
            longitude = longitude,
            formattedAddress = formattedAddress,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            state = state,
            pincode = pincode,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            isDefault = isDefault,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun AddressEntity.toDomain(): Address {
        return Address(
            id = id,
            customerId = customerId,
            addressType = AddressType.fromRaw(addressType),
            latitude = latitude,
            longitude = longitude,
            formattedAddress = formattedAddress,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            state = state,
            pincode = pincode,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            isDefault = isDefault,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun AddressRow.resolvedAddressType(): AddressType {
        val raw = addressType ?: AddressType.HOME.name
        return AddressType.fromRaw(raw)
    }

    private fun AddressRow.resolvedFormattedAddress(): String {
        val legacyComposite = listOfNotNull(legacyFlatNumber, legacyBuildingName, legacyAddressLine)
            .joinToString(", ")
            .ifBlank { null }
        return formattedAddress
            ?.takeIf { it.isNotBlank() }
            ?: legacyComposite
            ?: legacyAddressLine
            ?: ""
    }

    private fun AddressRow.resolvedAddressLine1(): String {
        return addressLine1
            ?.takeIf { it.isNotBlank() }
            ?: legacyAddressLine
            ?: legacyBuildingName
            ?: resolvedFormattedAddress()
    }

    private fun AddressRow.resolvedAddressLine2(): String? {
        return addressLine2
            ?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(legacyFlatNumber, legacyBuildingName)
                .joinToString(", ")
                .takeIf { it.isNotBlank() }
    }

    private fun AddressRow.resolvedCity(): String = city?.takeIf { it.isNotBlank() } ?: ""

    private fun AddressRow.resolvedState(): String = state?.takeIf { it.isNotBlank() } ?: ""

    private fun AddressRow.resolvedPincode(): String = pincode?.takeIf { it.isNotBlank() } ?: ""

    private fun AddressRow.resolvedReceiverName(): String = receiverName?.trim()?.takeIf { it.isNotEmpty() } ?: ""

    private fun AddressRow.resolvedReceiverPhone(): String = receiverPhone?.trim()?.takeIf { it.isNotEmpty() } ?: ""

    private fun AddressRow.resolvedIsDefault(): Boolean = isDefault ?: false

    private suspend fun updateDefaultLocally(addressId: String, customerId: String) {
        withContext(Dispatchers.IO) {
            addressDao.clearDefaultForCustomer(customerId)
            addressDao.markAsDefault(addressId)
        }
    }
}
