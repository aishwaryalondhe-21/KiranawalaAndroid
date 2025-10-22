package com.kiranawala.domain.use_cases.product

import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.ProductRepository
import javax.inject.Inject

/**
 * Use case to fetch all products for a specific store
 */
class GetStoreProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(storeId: String): Result<List<Product>> {
        return productRepository.fetchStoreProducts(storeId)
    }
}
