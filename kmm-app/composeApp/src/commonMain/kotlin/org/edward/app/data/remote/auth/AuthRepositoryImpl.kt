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
    companion object {
        const val AUTH = "auth"
    }

    override suspend fun login(request: LoginRequest): AsyncResult<LoginResponse> {
        return try {
            val response: LoginResponse = httpClient.post("$AUTH/login") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
            AsyncResult.Success(response)
        } catch (e: ResponseException) {
            val message = try { e.response.bodyAsText() } catch (_: Exception) { e.message ?: "Login failed" }
            AsyncResult.Error(e, displayMessage = message)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = "Login failed: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun register(request: RegisterRequest): AsyncResult<RegisterResponse> {
        return try {
            val response: RegisterResponse = httpClient.post("$AUTH/register") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
            AsyncResult.Success(response)
        } catch (e: ResponseException) {
            val message = try { e.response.bodyAsText() } catch (_: Exception) { e.message ?: "Registration failed" }
            AsyncResult.Error(e, displayMessage = message)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = "Registration failed: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun logout(): AsyncResult<Unit> {
        return try {
            httpClient.post("$AUTH/logout")
            AsyncResult.Success(Unit)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = "Logout failed: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequest): AsyncResult<Unit> {
        return try {
            httpClient.post("$AUTH/change-password") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }
            AsyncResult.Success(Unit)
        } catch (e: ResponseException) {
            val message = try { e.response.bodyAsText() } catch (_: Exception) { e.message ?: "Failed" }
            AsyncResult.Error(e, displayMessage = message)
        } catch (e: Exception) {
            AsyncResult.Error(e, displayMessage = "Password change failed: ${e.message ?: "Unknown error"}")
        }
    }
}
