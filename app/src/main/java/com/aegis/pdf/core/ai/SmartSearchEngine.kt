package com.aegis.pdf.core.ai

import com.aegis.pdf.core.pdf.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartSearchEngine @Inject constructor(
    private val pdfTextExtractor: PdfTextExtractor,
    private val aiService: AiService
) {

    data class SearchResult(
        val fileName: String,
        val pageNumber: Int,
        val snippet: String,
        val relevance: Float
    )

    suspend fun searchAllPdfs(
        query: String,
        pdfFiles: List<File>
    ): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<SearchResult>()

            pdfFiles.forEach { file ->
                try {
                    val text = pdfTextExtractor.extractText(file)
                    val pages = text.split("\f")
                    pages.forEachIndexed { index, pageText ->
                        if (pageText.contains(query, ignoreCase = true)) {
                            val startIndex = maxOf(0, pageText.indexOf(query, ignoreCase = true) - 40)
                            val endIndex = minOf(pageText.length, startIndex + 80)
                            val snippet = pageText.substring(startIndex, endIndex).trim()

                            val relevance = calculateRelevance(query, pageText)

                            results.add(
                                SearchResult(
                                    fileName = file.name,
                                    pageNumber = index + 1,
                                    snippet = "...$snippet...",
                                    relevance = relevance
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Skip corrupted files
                }
            }

            results.sortedByDescending { it.relevance }.take(50)
        }
    }

    private fun calculateRelevance(query: String, text: String): Float {
        val queryWords = query.lowercase().split(" ")
        val textLower = text.lowercase()
        var score = 0f

        queryWords.forEach { word ->
            var index = textLower.indexOf(word)
            while (index >= 0) {
                score += 1f
                index = textLower.indexOf(word, index + 1)
            }
        }

        // Bonus for exact phrase match
        if (textLower.contains(query.lowercase())) {
            score += 5f
        }

        // Normalize by text length
        return score / maxOf(text.length / 100, 1f)
    }
}