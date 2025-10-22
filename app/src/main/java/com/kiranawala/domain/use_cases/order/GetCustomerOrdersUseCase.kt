package com.kiranawala.domain.use_cases.order

import com.kiranawala.domain.models.Order
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCustomerOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(customerId: String): Flow<Result<List<Order>>> {
        return orderRepository.getCustomerOrders(customerId)
            .map<List<Order>, Result<List<Order>>> { orders ->
                Result.Success(orders)
            }
            .catch { e ->
                emit(Result.Error(Exception(e)))
            }
    }
}
