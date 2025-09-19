package org.edward.app.data.remote.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.edward.app.data.utils.AsyncResult

class AuthRepositoryImpl(private val httpClient: HttpClient) : AuthRepository {
    companion object Companion {
        const val AUTH = "auth"
    }

    override suspend fun login(loginRequest: LoginRequest): AsyncResult<LoginResponse> {
        return try {
            val response: LoginResponse = httpClient.post("$AUTH/login") {
                setBody(loginRequest)
                contentType(ContentType.Application.Json)
            }.body<LoginResponse>()
            AsyncResult.Success(response)
        } catch (e: ResponseException) {
            val message = e.response.bodyAsText()
            AsyncResult.Error(e, displayMessage = message)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = "Login failed: ${e.message ?: "Unknown error"}")
        }
    }
}