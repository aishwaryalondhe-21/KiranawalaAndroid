package com.kiranawala.domain.use_cases.cart

import com.kiranawala.domain.models.Cart
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(customerId: String): Flow<Result<Cart?>> {
        return cartRepository.getCart(customerId)
            .map<Cart?, Result<Cart?>> { cart ->
                Result.Success(cart)
            }
            .catch { e ->
                emit(Result.Error(Exception(e)))
            }
    }
}
