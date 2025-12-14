File: app/src/main/java/com/example/myapplication/data/api/RetrofitClient.kt
```kotlin
package com.example.myapplication.data.api

import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.remote.CerebrasApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to provide the Retrofit instance configured for Cerebras API.
 * Configured to use a direct API key to resolve Auth errors.
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.cerebras.ai/"
    private const val TAG = "RetrofitClient"

    /**
     * LIVE API KEY.
     * 
     * ACTION REQUIRED: Replace the placeholder below with your valid Cerebras API Key.
     * The key must start with 'csk-' to be valid.
     * 
     * Security Note: In a production app, fetch this from BuildConfig (e.g., BuildConfig.CEREBRAS_API_KEY)
     * or a secure storage mechanism rather than hardcoding it here.
     */
    private const val API_KEY = "csk-YOUR_API_KEY_HERE"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Use BODY level for debug builds to see full JSON, NONE for release to avoid leaking secrets
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()

            // Log warning if key appears to be default placeholder
            if (API_KEY == "csk-YOUR_API_KEY_HERE" || API_KEY.contains("placeholder") || API_KEY.isBlank()) {
                Log.w(TAG, "Warning: Cerebras API Key is currently a placeholder. API calls will likely fail with 401 Unauthorized.")
            }

            // Add Authorization header to every request
            val request = original.newBuilder()
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Exposes the Retrofit service implementation.
     * This property is used by the Repository to execute API calls.
     */
    val apiService: CerebrasApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CerebrasApiService::class.java)
    }
}
```