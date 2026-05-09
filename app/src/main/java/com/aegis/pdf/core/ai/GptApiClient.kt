package com.aegis.pdf.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class GptApiClient(private val context: Context) {

    private val gson = Gson()
    private var apiKey: String? = null
    private var baseUrl: String = "https://api.openai.com/v1"
    private var modelName: String = "gpt-3.5-turbo"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Set API key - NOW ACTUALLY WORKS
     */
    fun setApiKey(key: String) {
        if (key.isNotBlank()) {
            apiKey = key.trim()
        }
    }

    /**
     * Set custom model
     */
    fun setModel(model: String) {
        modelName = model
    }

    /**
     * Set custom base URL (for OpenAI-compatible APIs)
     */
    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    /**
     * Check if API key is set
     */
    fun isConfigured(): Boolean {
        return !apiKey.isNullOrBlank()
    }

    /**
     * Send chat completion request
     */
    suspend fun chatCompletion(
        systemPrompt: String,
        userMessage: String,
        temperature: Double = 0.7,
        maxTokens: Int = 2000
    ): GptResponse {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isNullOrBlank()) {
                    return@withContext GptResponse.Error("API key not set. Please configure it in Settings.")
                }

                val requestBody = mapOf(
                    "model" to modelName,
                    "messages" to listOf(
                        mapOf("role" to "system", "content" to systemPrompt),
                        mapOf("role" to "user", "content" to userMessage)
                    ),
                    "temperature" to temperature,
                    "max_tokens" to maxTokens
                )

                val jsonBody = gson.toJson(requestBody)

                val request = Request.Builder()
                    .url("$baseUrl/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val completion = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                    val content = completion.choices?.firstOrNull()?.message?.content
                    val tokens = completion.usage?.totalTokens ?: 0

                    GptResponse.Success(
                        content = content ?: "",
                        tokensUsed = tokens,
                        model = completion.model ?: modelName
                    )
                } else {
                    GptResponse.Error(
                        "API Error (${response.code}): ${responseBody ?: "Unknown error"}"
                    )
                }
            } catch (e: java.net.ConnectException) {
                GptResponse.Error("Network error: Could not connect to API.")
            } catch (e: java.net.SocketTimeoutException) {
                GptResponse.Error("Request timeout: The server took too long to respond.")
            } catch (e: Exception) {
                GptResponse.Error("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Simple text completion
     */
    suspend fun complete(
        prompt: String,
        temperature: Double = 0.7,
        maxTokens: Int = 2000
    ): GptResponse {
        return chatCompletion(
            systemPrompt = "You are a helpful assistant.",
            userMessage = prompt,
            temperature = temperature,
            maxTokens = maxTokens
        )
    }

    /**
     * Test API connection
     */
    suspend fun testConnection(): Boolean {
        return when (val response = complete("Hello", maxTokens = 10)) {
            is GptResponse.Success -> true
            is GptResponse.Error -> false
        }
    }
}

sealed class GptResponse {
    data class Success(
        val content: String,
        val tokensUsed: Int,
        val model: String
    ) : GptResponse()

    data class Error(
        val message: String
    ) : GptResponse()
}