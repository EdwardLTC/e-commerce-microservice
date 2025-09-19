package org.edward.app.data.remote.openai

import org.edward.app.data.utils.AsyncResult

interface OpenAIRepository {
    suspend fun ask(message: String): AsyncResult<String>
}