File: app/src/main/java/com/example/myapplication/data/repository/CerebrasRepository.kt
```kotlin
package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.model.ChatRequest
import com.example.myapplication.data.remote.CerebrasApiService
import com.example.myapplication.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to handle data operations for Cerebras AI.
 * Abstracts the API calls from the UI layer and acts as a bridge for the MathRepository.
 */
class CerebrasRepository(
    private val apiService: CerebrasApiService = RetrofitClient.apiService
) {

    /**
     * Sends a prompt to the Cerebras API and returns the generated text.
     * 
     * @param prompt The user's input text (or system-engineered prompt).
     * @param modelId The ID of the model to use (e.g., "llama3.1-8b", "llama3.1-70b").
     * @return Result containing the generated string or an exception.
     */
    suspend fun sendPrompt(
        prompt: String, 
        modelId: String = "llama3.1-8b"
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Construct the messages list (Single turn context for now as per MathRepository logic)
                val messages = listOf(
                    ChatMessage(role = "user", content = prompt)
                )

                val request = ChatRequest(
                    model = modelId,
                    messages = messages,
                    maxTokens = 1024,
                    temperature = 0.7,
                    stream = false
                )

                // Execute the network request via Retrofit
                val response = apiService.generateChatCompletion(request)

                // Extract content from the first choice
                val content = response.choices.firstOrNull()?.message?.content

                if (!content.isNullOrEmpty()) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("Received empty response from API"))
                }
            } catch (e: Exception) {
                Log.e("CerebrasRepo", "Network request failed", e)
                Result.failure(e)
            }
        }
    }
}
```