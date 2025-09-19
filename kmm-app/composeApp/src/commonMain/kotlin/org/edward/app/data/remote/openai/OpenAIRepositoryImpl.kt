package org.edward.app.data.remote.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import org.edward.app.config.EnvConfig
import org.edward.app.data.utils.AsyncResult

class OpenAIRepositoryImpl : OpenAIRepository {
    private val openAI = OpenAI(EnvConfig.OPEN_AI_KEY)

    override suspend fun ask(message: String): AsyncResult<String> {
        try {
            val messageBuilder =
                listOf(ChatMessage(ChatRole.User, message))

            val completion = openAI.chatCompletion(
                request = ChatCompletionRequest(
                    model = ModelId(EnvConfig.OPEN_AI_MODEL),
                    messages = messageBuilder,
                )
            )

            return AsyncResult.Success(
                completion.choices.firstOrNull()?.message?.content ?: "No response from OpenAI."
            )
        } catch (e: Exception) {
            return AsyncResult.Error(
                exception = e,
                displayMessage = "Failed to get response from OpenAI."
            )
        }
    }
}