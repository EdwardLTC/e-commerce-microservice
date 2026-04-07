package org.edward.app.data.remote.product

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.edward.app.data.utils.AsyncResult

class ProductRepositoryImpl(private val httpClient: HttpClient) : ProductRepository {

    companion object {
        const val PRODUCT = "products"
    }

    override suspend fun getProducts(skip: Int, take: Int): AsyncResult<List<Product>> {
        return try {
            val response: GetProductsResponse = httpClient.get(PRODUCT) {
                parameter("skip", skip)
                parameter("take", take)
            }.body()
            AsyncResult.Success(response.products)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun getProduct(id: String): AsyncResult<ProductDetail> {
        return try {
            AsyncResult.Success(httpClient.get("$PRODUCT/$id").body<ProductDetail>())
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun createProduct(request: CreateProductRequest): AsyncResult<CreateProductResponse> {
        return try {
            AsyncResult.Success(httpClient.post(PRODUCT) { setBody(request) }.body())
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun createOptionType(
        productId: String,
        request: CreateProductOptionTypeRequest
    ): AsyncResult<String> {
        return try {
            val resp: CreateProductResponse =
                httpClient.post("$PRODUCT/$productId/option-types") { setBody(request) }.body()
            AsyncResult.Success(resp.id)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun getOptionTypes(productId: String): AsyncResult<List<OptionType>> {
        return try {
            AsyncResult.Success(httpClient.get("$PRODUCT/$productId/option-types").body())
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun createOptionValue(
        optionTypeId: String,
        request: CreateProductOptionValueRequest
    ): AsyncResult<String> {
        return try {
            val resp: CreateProductResponse =
                httpClient.post("$PRODUCT/option-types/$optionTypeId/option-values") { setBody(request) }.body()
            AsyncResult.Success(resp.id)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun getOptionValues(optionTypeId: String): AsyncResult<List<OptionValue>> {
        return try {
            AsyncResult.Success(httpClient.get("$PRODUCT/option-types/$optionTypeId/option-values").body())
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun createVariant(
        productId: String,
        request: CreateProductVariantRequest
    ): AsyncResult<String> {
        return try {
            val resp: CreateProductResponse =
                httpClient.post("$PRODUCT/$productId/variants") { setBody(request) }.body()
            AsyncResult.Success(resp.id)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }
}
