package com.kiranawala.domain.use_cases.product

import com.kiranawala.domain.models.Product
import com.kiranawala.domain.models.Result
import com.kiranawala.domain.repositories.ProductRepository
import javax.inject.Inject

/**
 * Use case to search products within a store
 */
class SearchProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(storeId: String, query: String): Result<List<Product>> {
        return productRepository.searchProducts(storeId, query)
    }
}
