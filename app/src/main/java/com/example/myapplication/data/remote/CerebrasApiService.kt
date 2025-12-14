File: app/src/main/java/com/example/myapplication/data/remote/CerebrasApiService.kt
```kotlin
package com.example.myapplication.data.remote

import com.example.myapplication.data.model.ChatRequest
import com.example.myapplication.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface defining the Cerebras API endpoints.
 * This interface is used by Retrofit to generate the implementation for making network requests.
 */
interface CerebrasApiService {

    /**
     * Sends a chat completion request to the Cerebras API.
     *
     * @param request The request body containing the model, messages, and parameters.
     * @return A [ChatResponse] containing the generated text and usage data.
     */
    @POST("v1/chat/completions")
    suspend fun generateChatCompletion(@Body request: ChatRequest): ChatResponse
}
```