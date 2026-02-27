package org.edward.app.data.remote.product

import org.edward.app.data.utils.AsyncResult

interface ProductRepository {
    suspend fun getProducts(skip: Int = 0, take: Int = 20): AsyncResult<List<Product>>
    suspend fun getProduct(id: String): AsyncResult<ProductDetail>
    suspend fun addProduct(product: Product): AsyncResult<Product>
    suspend fun createOptionType(
        productId: String,
        request: CreateProductOptionTypeRequest
    ): AsyncResult<OptionType>

    suspend fun getOptionTypes(productId: String): AsyncResult<List<OptionType>>
    suspend fun createOptionValue(
        optionTypeId: String,
        request: CreateProductOptionValueRequest
    ): AsyncResult<OptionValue>

    suspend fun getOptionValues(optionTypeId: String): AsyncResult<List<OptionValue>>

    suspend fun createVariant(
        productId: String,
        request: CreateProductVariantRequest
    ): AsyncResult<Variant>
}