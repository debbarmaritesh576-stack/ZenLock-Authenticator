package com.aegis.pdf.core.ai

import com.aegis.pdf.core.pdf.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoTagManager @Inject constructor(
    private val pdfTextExtractor: PdfTextExtractor,
    private val aiService: AiService
) {

    private val predefinedTags = listOf(
        "invoice", "resume", "contract", "letter", "report",
        "manual", "presentation", "research", "legal", "medical",
        "financial", "educational", "personal", "work", "urgent",
        "review", "draft", "final", "archive", "important"
    )

    suspend fun generateTags(file: File): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val text = pdfTextExtractor.extractText(file)
                val firstPage = text.take(1000)

                // Local keyword matching
                val localTags = predefinedTags.filter { tag ->
                    firstPage.contains(tag, ignoreCase = true)
                }.toMutableList()

                // AI-based tagging
                if (aiService.hasApiKey() && localTags.size < 3) {
                    try {
                        val prompt = """
                        Based on this document content, suggest 3-5 relevant tags from: ${predefinedTags.joinToString()}
                        
                        Content: $firstPage
                        
                        Return only tags separated by commas.
                        """.trimIndent()

                        val aiResponse = aiService.summarizeText(prompt, maxLength = 20)
                        val aiTags = aiResponse.split(",").map { it.trim().lowercase() }
                            .filter { it in predefinedTags }
                        localTags.addAll(aiTags)
                    } catch (e: Exception) {
                        // Fallback to local tags only
                    }
                }

                localTags.distinct().take(5)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun suggestCategory(tags: List<String>): String {
        return when {
            tags.any { it in listOf("invoice", "financial", "receipt") } -> "Finance"
            tags.any { it in listOf("resume", "cv", "cover letter") } -> "Career"
            tags.any { it in listOf("contract", "legal", "agreement") } -> "Legal"
            tags.any { it in listOf("medical", "health", "prescription") } -> "Medical"
            tags.any { it in listOf("report", "research", "study") } -> "Research"
            tags.any { it in listOf("certificate", "degree", "transcript") } -> "Education"
            tags.any { it in listOf("manual", "guide", "tutorial") } -> "Tutorial"
            else -> "General"
        }
    }
}