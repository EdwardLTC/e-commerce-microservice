package org.edward.app.data.local

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    companion object {
        data class TokenData(
            val accessToken: String?,
            val accessTokenExpiry: Long,
            val refreshToken: String?,
            val refreshTokenExpiry: Long,
        )
    }

    suspend fun saveDarkTheme(isDark: Boolean)
    fun isDarkTheme(): Flow<Boolean>
    suspend fun saveAccessToken(token: String, ttl: Long)
    fun getAccessToken(): Flow<String?>
    suspend fun saveRefreshToken(token: String, ttl: Long)
    fun getRefreshToken(): Flow<String?>
    fun getTokenData(): Flow<TokenData?>
    suspend fun clearToken()

    suspend fun saveUserName(name: String)
    fun getUserName(): Flow<String>
    suspend fun saveUserEmail(email: String)
    fun getUserEmail(): Flow<String>
    suspend fun clearUserInfo()
}
