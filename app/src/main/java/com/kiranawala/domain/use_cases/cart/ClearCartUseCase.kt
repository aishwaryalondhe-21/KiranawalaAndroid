package com.kiranawala.domain.use_cases.cart

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.CartRepository
import javax.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(customerId: String): Result<Unit> {
        return try {
            cartRepository.clearCart(customerId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
