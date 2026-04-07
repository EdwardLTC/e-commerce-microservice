package org.edward.app.data.remote

import org.edward.app.data.remote.auth.AuthRepository
import org.edward.app.data.remote.auth.AuthRepositoryImpl
import org.edward.app.data.remote.auth.MockAuthRepositoryImpl
import org.edward.app.data.remote.product.MockProductRepositoryImpl
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.remote.product.ProductRepositoryImpl
import org.koin.dsl.module

/**
 * Set to `true` to use mock data without a running backend,
 * or `false` to connect to the real API gateway.
 */
const val USE_MOCK_DATA = true

val remoteModule = module {
    if (USE_MOCK_DATA) {
        single<ProductRepository> { MockProductRepositoryImpl() }
        single<AuthRepository> { MockAuthRepositoryImpl() }
    } else {
        single<ProductRepository> { ProductRepositoryImpl(get()) }
        single<AuthRepository> { AuthRepositoryImpl(get()) }
    }
}
