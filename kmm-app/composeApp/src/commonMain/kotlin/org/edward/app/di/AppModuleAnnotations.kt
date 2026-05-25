package org.edward.app.di

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.edward.app.data.local.DataStoreRepository
import org.edward.app.data.remote.auth.AuthRepository
import org.edward.app.data.remote.auth.AuthRepositoryImpl
import org.edward.app.data.remote.auth.MockAuthRepositoryImpl
import org.edward.app.data.remote.product.MockProductRepositoryImpl
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.remote.product.ProductRepositoryImpl
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

const val USE_MOCK_DATA = true

@Module
@ComponentScan("org.edward.app")
class AppModuleAnnotations {

    @Single
    fun httpClient(dataStoreRepository: DataStoreRepository): HttpClient {
        return HttpClient {
            expectSuccess = false
            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom("http://localhost:3000"))
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d("--------------------------------------------------")
                        Napier.d("[HttpClient] $message")
                    }
                }
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val tokenData = dataStoreRepository.getTokenData().first()
                        tokenData?.accessToken?.let { access ->
                            BearerTokens(
                                accessToken = access,
                                refreshToken = tokenData.refreshToken ?: ""
                            )
                        }
                    }
                }
            }
            HttpResponseValidator {
                validateResponse { response ->
                    when (response.status.value) {
                        in 400..499 -> throw ClientRequestException(response, "Client error")
                        in 500..599 -> throw ServerResponseException(response, "Server error")
                    }
                }

                handleResponseExceptionWithRequest { cause, request ->
                    Napier.e("HTTP Exception: ${cause.message}", cause)
                    Napier.e("Request URL: ${request.url}")
                    Napier.e("Request Method: ${request.method}")

                    when (cause) {
                        is ClientRequestException -> Napier.e("Client error body: ${cause.response.bodyAsText()}")
                        is ServerResponseException -> Napier.e("Server error body: ${cause.response.bodyAsText()}")
                    }
                }
            }
        }
    }

    @Single
    fun productRepository(httpClient: HttpClient): ProductRepository {
        return if (USE_MOCK_DATA) MockProductRepositoryImpl() else ProductRepositoryImpl(httpClient)
    }

    @Single
    fun authRepository(httpClient: HttpClient): AuthRepository {
        return if (USE_MOCK_DATA) MockAuthRepositoryImpl() else AuthRepositoryImpl(httpClient)
    }
}

