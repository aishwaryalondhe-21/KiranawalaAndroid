package com.kiranawala.domain.use_cases.order

import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.OrderRepository
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> {
        return try {
            val order = orderRepository.getOrderById(orderId)
            if (order != null) {
                Result.Success(order)
            } else {
                Result.Error(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
