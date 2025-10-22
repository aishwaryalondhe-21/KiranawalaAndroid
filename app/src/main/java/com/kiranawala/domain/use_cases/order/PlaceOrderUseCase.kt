package com.kiranawala.domain.use_cases.order

import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.OrderRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(order: Order): Result<String> {
        return try {
            val orderId = orderRepository.placeOrder(order)
            Result.Success(orderId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
