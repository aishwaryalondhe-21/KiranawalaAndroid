package com.kiranawala.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kiranawala.data.local.entities.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    @Query(
        "SELECT * FROM addresses WHERE customer_id = :customerId ORDER BY is_default DESC, updated_at DESC"
    )
    fun observeAddresses(customerId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE customer_id = :customerId AND is_default = 1 LIMIT 1")
    suspend fun getDefaultAddress(customerId: String): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAddress(address: AddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAddresses(addresses: List<AddressEntity>)

    @Query("DELETE FROM addresses WHERE id = :addressId")
    suspend fun deleteAddress(addressId: String)

    @Query("DELETE FROM addresses WHERE customer_id = :customerId")
    suspend fun deleteAddressesForCustomer(customerId: String)

    @Transaction
    suspend fun replaceAddresses(customerId: String, addresses: List<AddressEntity>) {
        deleteAddressesForCustomer(customerId)
        if (addresses.isNotEmpty()) {
            upsertAddresses(addresses)
        }
    }

    @Query("UPDATE addresses SET is_default = 0 WHERE customer_id = :customerId")
    suspend fun clearDefaultForCustomer(customerId: String)

    @Query("UPDATE addresses SET is_default = 1 WHERE id = :addressId")
    suspend fun markAsDefault(addressId: String)
}
