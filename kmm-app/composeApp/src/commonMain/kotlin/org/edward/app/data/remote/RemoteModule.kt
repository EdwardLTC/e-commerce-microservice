package org.edward.app.data.remote

import org.edward.app.data.remote.auth.AuthRepository
import org.edward.app.data.remote.auth.AuthRepositoryImpl
import org.edward.app.data.remote.openai.OpenAIRepository
import org.edward.app.data.remote.openai.OpenAIRepositoryImpl
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.remote.product.ProductRepositoryImpl
import org.edward.app.data.remote.user.UserRepository
import org.edward.app.data.remote.user.UserRepositoryImpl
import org.koin.dsl.module

val remoteModule = module {
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<OpenAIRepository> { OpenAIRepositoryImpl() }
}