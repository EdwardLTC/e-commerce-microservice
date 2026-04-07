package org.edward.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DataStoreRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : DataStoreRepository {
    companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val ACCESS_TOKEN_EXPIRE_TIME = longPreferencesKey("access_token_expire_time")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val REFRESH_TOKEN_EXPIRE_TIME = longPreferencesKey("refresh_token_expire_time")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    override suspend fun saveDarkTheme(isDark: Boolean) {
        dataStore.edit { it[DARK_THEME] = isDark }
    }

    override fun isDarkTheme(): Flow<Boolean> {
        return dataStore.data.map { it[DARK_THEME] ?: false }
    }

    override suspend fun saveAccessToken(token: String, ttl: Long) {
        dataStore.edit {
            it[ACCESS_TOKEN] = token
            it[ACCESS_TOKEN_EXPIRE_TIME] = Clock.System.now().epochSeconds + ttl
        }
    }

    override fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            val token = prefs[ACCESS_TOKEN]
            val expiry = prefs[ACCESS_TOKEN_EXPIRE_TIME] ?: 0L
            if (token != null && Clock.System.now().epochSeconds < expiry) token else null
        }
    }

    override suspend fun saveRefreshToken(token: String, ttl: Long) {
        dataStore.edit {
            it[REFRESH_TOKEN] = token
            it[REFRESH_TOKEN_EXPIRE_TIME] = Clock.System.now().epochSeconds + ttl
        }
    }

    override fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            val token = prefs[REFRESH_TOKEN]
            val expiry = prefs[REFRESH_TOKEN_EXPIRE_TIME] ?: 0L
            if (token != null && Clock.System.now().epochSeconds < expiry) token else null
        }
    }

    override fun getTokenData(): Flow<DataStoreRepository.Companion.TokenData?> {
        return dataStore.data.map { prefs ->
            val accessToken = prefs[ACCESS_TOKEN]
            val accessExpiry = prefs[ACCESS_TOKEN_EXPIRE_TIME] ?: 0L
            val refreshToken = prefs[REFRESH_TOKEN]
            val refreshExpiry = prefs[REFRESH_TOKEN_EXPIRE_TIME] ?: 0L

            if (accessToken != null && refreshToken != null) {
                DataStoreRepository.Companion.TokenData(
                    accessToken, accessExpiry, refreshToken, refreshExpiry
                )
            } else null
        }.distinctUntilChanged()
    }

    override suspend fun clearToken() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(ACCESS_TOKEN_EXPIRE_TIME)
            it.remove(REFRESH_TOKEN)
            it.remove(REFRESH_TOKEN_EXPIRE_TIME)
        }
    }

    override suspend fun saveUserName(name: String) {
        dataStore.edit { it[USER_NAME] = name }
    }

    override fun getUserName(): Flow<String> {
        return dataStore.data.map { it[USER_NAME] ?: "" }
    }

    override suspend fun saveUserEmail(email: String) {
        dataStore.edit { it[USER_EMAIL] = email }
    }

    override fun getUserEmail(): Flow<String> {
        return dataStore.data.map { it[USER_EMAIL] ?: "" }
    }

    override suspend fun clearUserInfo() {
        dataStore.edit {
            it.remove(USER_NAME)
            it.remove(USER_EMAIL)
        }
    }
}
