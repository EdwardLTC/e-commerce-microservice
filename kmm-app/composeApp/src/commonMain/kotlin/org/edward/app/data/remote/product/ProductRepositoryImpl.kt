package org.edward.app.data.remote.product

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.edward.app.data.utils.AsyncResult

class ProductRepositoryImpl(
    private val httpClient: HttpClient
) : ProductRepository {

    companion object {
        const val PRODUCT = "products"
    }

    override suspend fun getProducts(skip: Int, take: Int): AsyncResult<List<Product>> {
        return try {
            val response: List<Product> = httpClient.get(PRODUCT) {
                parameter("skip", skip)
                parameter("take", take)
            }.body()
            AsyncResult.Success(response)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun getProduct(id: String): AsyncResult<ProductDetail> {
        return try {
            AsyncResult.Success(httpClient.get("$PRODUCT/$id").body<ProductDetail>())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun addProduct(product: Product): AsyncResult<Product> {
        return try {
            AsyncResult.Success(httpClient.post(PRODUCT) { setBody(product) }.body<Product>())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun createOptionType(
        productId: String,
        request: CreateProductOptionTypeRequest
    ): AsyncResult<OptionType> {
        return try {
            val path = "$PRODUCT/$productId/option-types"
            AsyncResult.Success(httpClient.post(path) { setBody(request) }.body())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun getOptionTypes(productId: String): AsyncResult<List<OptionType>> {
        return try {
            val path = "$PRODUCT/$productId/option-types"
            AsyncResult.Success(httpClient.get(path).body())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun createOptionValue(
        optionTypeId: String,
        request: CreateProductOptionValueRequest
    ): AsyncResult<OptionValue> {
        return try {
            val path = "$PRODUCT/option-types/$optionTypeId/option-values"
            AsyncResult.Success(httpClient.post(path) { setBody(request) }.body())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun getOptionValues(optionTypeId: String): AsyncResult<List<OptionValue>> {
        return try {
            val path = "$PRODUCT/option-types/$optionTypeId/option-values"
            AsyncResult.Success(httpClient.get(path).body())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun createVariant(
        productId: String,
        request: CreateProductVariantRequest
    ): AsyncResult<Variant> {
        return try {
            val path = "$PRODUCT/$productId/variants"
            AsyncResult.Success(httpClient.post(path) { setBody(request) }.body())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }
}