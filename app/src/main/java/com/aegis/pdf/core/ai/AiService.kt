package com.aegis.pdf.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiService(private val context: Context) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "aegis_ai_secure_prefs",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    data class AiResponse(
        val success: Boolean,
        val result: String?,
        val error: String?
    )

    suspend fun analyzePdf(pdfText: String, query: String): AiResponse {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = getApiKey()
                if (apiKey.isNullOrBlank()) {
                    return@withContext AiResponse(
                        success = false,
                        result = null,
                        error = "API key not configured. Please set your API key in Settings."
                    )
                }

                val requestBody = mapOf(
                    "model" to "gpt-3.5-turbo",
                    "messages" to listOf(
                        mapOf(
                            "role" to "system",
                            "content" to "You are a helpful assistant that analyzes PDF documents. Provide accurate and concise answers."
                        ),
                        mapOf(
                            "role" to "user",
                            "content" to "PDF Content: $pdfText\n\nQuestion: $query"
                        )
                    ),
                    "temperature" to 0.3,
                    "max_tokens" to 2000
                )

                val jsonBody = gson.toJson(requestBody)
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val aiResult = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                    val content = aiResult.choices?.firstOrNull()?.message?.content

                    AiResponse(
                        success = true,
                        result = content,
                        error = null
                    )
                } else {
                    val errorBody = responseBody ?: "Unknown error"
                    if (response.code == 401) {
                        AiResponse(
                            success = false,
                            result = null,
                            error = "Invalid API key. Please check your API key in Settings."
                        )
                    } else if (response.code == 429) {
                        AiResponse(
                            success = false,
                            result = null,
                            error = "Rate limit exceeded. Please try again later."
                        )
                    } else {
                        AiResponse(
                            success = false,
                            result = null,
                            error = "API error: ${response.code} - $errorBody"
                        )
                    }
                }
            } catch (e: java.net.ConnectException) {
                AiResponse(
                    success = false,
                    result = null,
                    error = "Network error: Unable to connect to AI service. Check your internet connection."
                )
            } catch (e: java.net.SocketTimeoutException) {
                AiResponse(
                    success = false,
                    result = null,
                    error = "Request timed out. The PDF may be too large. Try a shorter query."
                )
            } catch (e: Exception) {
                AiResponse(
                    success = false,
                    result = null,
                    error = "AI service error: ${e.message}"
                )
            }
        }
    }

    suspend fun summarizePdf(pdfText: String): AiResponse {
        return analyzePdf(pdfText, "Please summarize this document concisely.")
    }

    suspend fun extractKeywords(pdfText: String): AiResponse {
        return analyzePdf(pdfText, "Extract the 10 most important keywords from this document.")
    }

    fun setApiKey(key: String) {
        if (key.isNotBlank()) {
            securePrefs.edit().putString("api_key", key.trim()).apply()
        }
    }

    fun getApiKey(): String? {
        return securePrefs.getString("api_key", null)
    }

    fun isApiKeyConfigured(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    fun clearApiKey() {
        securePrefs.edit().remove("api_key").apply()
    }
}

// OpenAI API response models
data class ChatCompletionResponse(
    val id: String?,
    val `object`: String?,
    val created: Long?,
    val model: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int?,
    val message: Message?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class Message(
    val role: String?,
    val content: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?
)