package org.edward.app.data.network

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
import org.koin.dsl.module

val networkModule = module {
    single {
        val dataStoreRepository: DataStoreRepository = get()

        HttpClient {
            expectSuccess = false
            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom("https://fakestoreapi.com/"))
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
}