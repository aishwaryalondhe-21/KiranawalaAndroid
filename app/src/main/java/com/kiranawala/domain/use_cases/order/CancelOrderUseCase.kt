package com.kiranawala.domain.use_cases.order

import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.OrderRepository
import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Unit> {
        return try {
            orderRepository.cancelOrder(orderId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
