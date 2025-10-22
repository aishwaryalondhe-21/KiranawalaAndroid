package com.kiranawala.domain.use_cases.cart

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.CartRepository
import javax.inject.Inject

class UpdateCartItemQuantityUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        customerId: String,
        productId: String,
        quantity: Int
    ): Result<Unit> {
        return try {
            if (quantity <= 0) {
                cartRepository.removeFromCart(customerId, productId)
            } else {
                cartRepository.updateQuantity(customerId, productId, quantity)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
