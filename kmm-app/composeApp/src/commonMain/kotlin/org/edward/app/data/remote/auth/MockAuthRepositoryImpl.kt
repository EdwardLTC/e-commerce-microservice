package org.edward.app.data.remote.auth

import kotlinx.coroutines.delay
import org.edward.app.data.utils.AsyncResult

class MockAuthRepositoryImpl : AuthRepository {

    private val registeredUsers = mutableMapOf(
        "demo@ecommerce.com" to MockUser("u-001", "Demo User", "demo@ecommerce.com", "password123"),
        "john@example.com" to MockUser("u-002", "John Doe", "john@example.com", "pass1234"),
    )

    private var currentPassword: String? = null

    override suspend fun login(request: LoginRequest): AsyncResult<LoginResponse> {
        delay(800)
        val user = registeredUsers[request.email]
        if (user == null || user.password != request.password) {
            return AsyncResult.Error(
                displayMessage = "Invalid email or password"
            )
        }
        currentPassword = user.password
        return AsyncResult.Success(
            LoginResponse(accessToken = "mock_access_token_${user.id}_${currentTimeMillis()}")
        )
    }

    override suspend fun register(request: RegisterRequest): AsyncResult<RegisterResponse> {
        delay(1000)
        if (registeredUsers.containsKey(request.email)) {
            return AsyncResult.Error(displayMessage = "Email already registered")
        }
        val id = "u-${(registeredUsers.size + 1).toString().padStart(3, '0')}"
        registeredUsers[request.email] = MockUser(id, request.name, request.email, request.password)
        return AsyncResult.Success(
            RegisterResponse(id = id, name = request.name, email = request.email)
        )
    }

    override suspend fun logout(): AsyncResult<Unit> {
        delay(300)
        currentPassword = null
        return AsyncResult.Success(Unit)
    }

    override suspend fun changePassword(request: ChangePasswordRequest): AsyncResult<Unit> {
        delay(600)
        if (currentPassword != null && request.oldPassword != currentPassword) {
            return AsyncResult.Error(displayMessage = "Current password is incorrect")
        }
        currentPassword = request.newPassword
        return AsyncResult.Success(Unit)
    }

    private fun currentTimeMillis(): Long =
        kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds

    private data class MockUser(
        val id: String,
        val name: String,
        val email: String,
        val password: String,
    )
}
