package org.edward.app.data.remote.user

import org.edward.app.data.utils.AsyncResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class UserRepositoryImpl(private val httpClient: HttpClient) : UserRepository {

    companion object {
        const val USER = "users"
    }

    override suspend fun getUsers(): AsyncResult<List<User>> {
        return try {
            AsyncResult.Success(httpClient.get(USER).body<List<User>>())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun getUser(id: Int): AsyncResult<User> {
        return try {
            AsyncResult.Success(httpClient.get("$USER/$id").body<User>())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }

    override suspend fun addUser(user: User): AsyncResult<User> {
        return try {
            AsyncResult.Success(httpClient.post(USER) {
                setBody(user)
            }.body<User>())
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }
}