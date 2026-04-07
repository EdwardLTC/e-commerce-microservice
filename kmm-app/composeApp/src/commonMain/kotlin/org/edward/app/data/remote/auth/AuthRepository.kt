package org.edward.app.data.remote.auth

import org.edward.app.data.utils.AsyncResult

interface AuthRepository {
    suspend fun login(request: LoginRequest): AsyncResult<LoginResponse>
    suspend fun register(request: RegisterRequest): AsyncResult<RegisterResponse>
    suspend fun logout(): AsyncResult<Unit>
    suspend fun changePassword(request: ChangePasswordRequest): AsyncResult<Unit>
}
