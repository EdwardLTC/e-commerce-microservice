package org.edward.app.data.remote.product

import org.edward.app.data.utils.AsyncResult

interface ProductRepository {
    suspend fun getProducts(): AsyncResult<List<Product>>
    suspend fun getProduct(id: Int): AsyncResult<Product>
    suspend fun addProduct(product: Product): AsyncResult<Product>
}