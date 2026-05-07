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
class AiService @Inject constructor() {

    private val apiKey: String = "" // User will set in settings
    private val baseUrl = "https://api.openai.com/v1/chat/completions"

    suspend fun summarizeText(text: String, maxLength: Int = 150): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Summarize this text in $maxLength words: $text"
                callGptApi(prompt)
            } catch (e: Exception) {
                "Summarization failed: ${e.message}"
            }
        }
    }

    suspend fun askQuestion(context: String, question: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                Context: $context
                
                Question: $question
                
                Answer based on the context above.
                """.trimIndent()
                callGptApi(prompt)
            } catch (e: Exception) {
                "Failed to answer: ${e.message}"
            }
        }
    }

    suspend fun translateText(text: String, targetLanguage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Translate the following text to $targetLanguage: $text"
                callGptApi(prompt)
            } catch (e: Exception) {
                "Translation failed: ${e.message}"
            }
        }
    }

    suspend fun generatePdfTitle(content: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Generate a short, descriptive title for this document: $content"
                callGptApi(prompt, maxTokens = 10)
            } catch (e: Exception) {
                "Untitled Document"
            }
        }
    }

    suspend fun smartSearch(query: String, documents: List<String>): List<SearchMatch> {
        return withContext(Dispatchers.IO) {
            try {
                val docText = documents.joinToString("\n---\n")
                val prompt = """
                Find documents matching: "$query"
                
                Documents:
                $docText
                
                Return JSON array with [{docIndex: number, relevance: number, snippet: string}]
                """.trimIndent()
                
                val response = callGptApi(prompt)
                parseSearchResults(response)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun callGptApi(prompt: String, maxTokens: Int = 500): String {
        val url = URL(baseUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            doOutput = true
        }

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", maxTokens)
            put("temperature", 0.3)
        }

        connection.outputStream.use { os ->
            os.write(requestBody.toString().toByteArray())
        }

        val response = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        val jsonResponse = JSONObject(response)
        val choices = jsonResponse.getJSONArray("choices")
        return if (choices.length() > 0) {
            choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } else {
            "No response"
        }
    }

    private fun parseSearchResults(json: String): List<SearchMatch> {
        return try {
            val array = JSONArray(json)
            val results = mutableListOf<SearchMatch>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                results.add(
                    SearchMatch(
                        docIndex = obj.getInt("docIndex"),
                        relevance = obj.getDouble("relevance").toFloat(),
                        snippet = obj.getString("snippet")
                    )
                )
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setApiKey(key: String) {
        // Securely store API key
    }

    fun hasApiKey(): Boolean = apiKey.isNotEmpty()
}

data class SearchMatch(
    val docIndex: Int,
    val relevance: Float,
    val snippet: String
)
