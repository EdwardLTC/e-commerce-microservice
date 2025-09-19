package org.edward.app.data.remote.auth

import org.edward.app.data.utils.AsyncResult

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): AsyncResult<LoginResponse>
}