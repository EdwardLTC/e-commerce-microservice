package org.edward.app.data.remote.product

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.edward.app.data.utils.AsyncResult

class ProductRepositoryImpl(
    private val httpClient: HttpClient
) : ProductRepository {

    companion object {
        const val PRODUCT = "products"
    }

    override suspend fun getProducts(): AsyncResult<List<Product>> {
        return try {
            AsyncResult.Success(httpClient.get(PRODUCT).body<List<Product>>())
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = e.message)
        }
    }

    override suspend fun getProduct(id: Int): AsyncResult<Product> {
        return try {
            AsyncResult.Success(httpClient.get("$PRODUCT/$id").body<Product>())
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
}