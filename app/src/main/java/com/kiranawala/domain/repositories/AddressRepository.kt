package com.kiranawala.domain.repositories

import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for address management
 */
interface AddressRepository {
    /**
     * Get all addresses for the current user
     * @return Flow of list of addresses
     */
    fun getUserAddresses(userId: String): Flow<List<Address>>
    
    /**
     * Get default address for the current user
     * @return Result containing default address or null
     */
    suspend fun getDefaultAddress(userId: String): Result<Address?>
    
    /**
     * Get address by ID
     * @param addressId Address ID
     * @return Result containing address or error
     */
    suspend fun getAddressById(addressId: String): Result<Address?>
    
    /**
     * Add new address
     * @param address Address to add
     * @return Result containing created address ID or error
     */
    suspend fun addAddress(address: Address): Result<String>
    
    /**
     * Update existing address
     * @param address Updated address
     * @return Result indicating success or error
     */
    suspend fun updateAddress(address: Address): Result<Unit>
    
    /**
     * Delete address
     * @param addressId Address ID to delete
     * @return Result indicating success or error
     */
    suspend fun deleteAddress(addressId: String): Result<Unit>
    
    /**
     * Set address as default
     * @param addressId Address ID to set as default
     * @param userId User ID
     * @return Result indicating success or error
     */
    suspend fun setDefaultAddress(addressId: String, userId: String): Result<Unit>
}
