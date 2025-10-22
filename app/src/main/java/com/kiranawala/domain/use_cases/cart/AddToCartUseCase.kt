package com.kiranawala.domain.use_cases.cart

import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        customerId: String,
        storeId: String,
        product: Product,
        quantity: Int = 1
    ): Result<Unit> {
        return try {
            cartRepository.addToCart(customerId, storeId, product, quantity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
