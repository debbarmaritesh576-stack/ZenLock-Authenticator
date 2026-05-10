package com.aegis.pdf.features.editor.text

import javax.inject.Inject
import javax.inject.Singleton

data class SpellCheckResult(
    val word: String,
    val isCorrect: Boolean,
    val suggestions: List<String>,
    val startIndex: Int,
    val endIndex: Int
)

@Singleton
class SpellCheckEngine @Inject constructor() {

    // Basic English dictionary (would be loaded from assets in production)
    private val dictionary = setOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "her", "she", "or",
        "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know",
        "take", "people", "into", "year", "your", "good", "some", "could",
        "them", "see", "other", "than", "then", "now", "look", "only", "come",
        "its", "over", "think", "also", "back", "after", "use", "two", "how",
        "our", "work", "first", "well", "way", "even", "new", "want", "because",
        "any", "these", "give", "day", "most", "us", "hello", "world", "pdf",
        "document", "text", "edit", "page", "file", "open", "save", "print"
    )

    private val commonCorrections = mapOf(
        "teh" to "the",
        "adn" to "and",
        "thier" to "their",
        "recieve" to "receive",
        "beleive" to "believe",
        "accomodate" to "accommodate",
        "occured" to "occurred",
        "untill" to "until",
        "begining" to "beginning",
        "definately" to "definitely",
        "goverment" to "government",
        "independant" to "independent",
        "knowlege" to "knowledge",
        "neccessary" to "necessary",
        "occassion" to "occasion",
        "paralell" to "parallel",
        "percieve" to "perceive",
        "recomend" to "recommend",
        "suceed" to "succeed",
        "tommorow" to "tomorrow",
        "wierd" to "weird"
    )

    fun checkText(text: String): List<SpellCheckResult> {
        val results = mutableListOf<SpellCheckResult>()
        val words = text.split(Regex("\\s+"))

        var currentIndex = 0
        for (word in words) {
            val cleanWord = word.replace(Regex("[^a-zA-Z]"), "").lowercase()
            if (cleanWord.isNotEmpty()) {
                val isCorrect = dictionary.contains(cleanWord)
                val suggestions = if (!isCorrect) {
                    generateSuggestions(cleanWord)
                } else emptyList()

                results.add(
                    SpellCheckResult(
                        word = word,
                        isCorrect = isCorrect,
                        suggestions = suggestions,
                        startIndex = currentIndex,
                        endIndex = currentIndex + word.length
                    )
                )
            }
            currentIndex += word.length + 1
        }

        return results
    }

    fun isWordCorrect(word: String): Boolean {
        return dictionary.contains(word.lowercase())
    }

    fun getCorrection(word: String): String? {
        return commonCorrections[word.lowercase()]
    }

    fun getSuggestions(word: String): List<String> {
        return generateSuggestions(word.lowercase())
    }

    private fun generateSuggestions(word: String): List<String> {
        // Check common corrections first
        commonCorrections[word]?.let { return listOf(it) }

        // Simple Levenshtein-based suggestions
        return dictionary
            .filter { levenshteinDistance(word, it) <= 2 }
            .sortedBy { levenshteinDistance(word, it) }
            .take(5)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }

        return dp[m][n]
    }
}