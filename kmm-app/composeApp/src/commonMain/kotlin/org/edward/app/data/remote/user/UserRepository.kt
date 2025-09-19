package org.edward.app.data.remote.user

import org.edward.app.data.utils.AsyncResult

interface UserRepository {
    suspend fun getUsers(): AsyncResult<List<User>>
    suspend fun getUser(id: Int): AsyncResult<User>
    suspend fun addUser(user: User): AsyncResult<User>
}