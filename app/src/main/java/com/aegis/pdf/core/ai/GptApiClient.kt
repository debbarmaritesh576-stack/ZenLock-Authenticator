package com.aegis.pdf.core.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GptApiClient @Inject constructor() {

    private var apiKey: String = ""
    private val baseUrl = "https://api.openai.com/v1"

    fun setApiKey(key: String) {
        apiKey = key
    }

    suspend fun chat(
        messages: List<ChatMessage>,
        model: String = "gpt-3.5-turbo",
        maxTokens: Int = 500,
        temperature: Double = 0.3
    ): String {
        return withContext(Dispatchers.IO) {
            val url = URL("$baseUrl/chat/completions")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
            }

            val messagesArray = JSONArray()
            messages.forEach { msg ->
                messagesArray.put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }

            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messagesArray)
                put("max_tokens", maxTokens)
                put("temperature", temperature)
            }

            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }

            val responseCode = connection.responseCode
            val response = if (responseCode == 200) {
                connection.inputStream.bufferedReader().readText()
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("API Error $responseCode: $errorBody")
            }

            connection.disconnect()

            val json = JSONObject(response)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    data class ChatMessage(
        val role: String,
        val content: String
    ) {
        companion object {
            fun system(content: String) = ChatMessage("system", content)
            fun user(content: String) = ChatMessage("user", content)
            fun assistant(content: String) = ChatMessage("assistant", content)
        }
    }
}