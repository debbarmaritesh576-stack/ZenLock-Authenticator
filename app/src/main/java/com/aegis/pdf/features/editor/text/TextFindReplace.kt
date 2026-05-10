package com.aegis.pdf.features.editor.text

import javax.inject.Inject
import javax.inject.Singleton

data class FindResult(
    val elementId: Long,
    val startIndex: Int,
    val endIndex: Int,
    val matchedText: String
)

@Singleton
class TextFindReplace @Inject constructor() {

    private var currentResults: List<FindResult> = emptyList()
    private var currentResultIndex: Int = -1
    private var searchQuery: String = ""
    private var caseSensitive: Boolean = false
    private var wholeWord: Boolean = false

    fun findInElements(
        elements: List<TextElement>,
        query: String,
        caseSensitive: Boolean = false,
        wholeWord: Boolean = false
    ): List<FindResult> {
        searchQuery = query
        this.caseSensitive = caseSensitive
        this.wholeWord = wholeWord

        if (query.isBlank()) {
            currentResults = emptyList()
            return emptyList()
        }

        val results = mutableListOf<FindResult>()

        elements.forEach { element ->
            val text = if (caseSensitive) element.text else element.text.lowercase()
            val searchFor = if (caseSensitive) query else query.lowercase()

            var startIndex = text.indexOf(searchFor)
            while (startIndex >= 0) {
                val endIndex = startIndex + searchFor.length

                if (!wholeWord || isWholeWord(text, startIndex, endIndex)) {
                    results.add(
                        FindResult(
                            elementId = element.id,
                            startIndex = startIndex,
                            endIndex = endIndex,
                            matchedText = element.text.substring(startIndex, endIndex)
                        )
                    )
                }

                startIndex = text.indexOf(searchFor, endIndex)
            }
        }

        currentResults = results
        currentResultIndex = if (results.isNotEmpty()) 0 else -1
        return results
    }

    fun replace(
        elements: List<TextElement>,
        result: FindResult,
        replacement: String
    ): List<TextElement> {
        return elements.map { element ->
            if (element.id == result.elementId) {
                val newText = element.text.substring(0, result.startIndex) +
                        replacement +
                        element.text.substring(result.endIndex)
                element.copy(text = newText)
            } else element
        }
    }

    fun replaceAll(
        elements: List<TextElement>,
        query: String,
        replacement: String,
        caseSensitive: Boolean = false
    ): List<TextElement> {
        val results = findInElements(elements, query, caseSensitive, false)
        var updatedElements = elements

        // Replace from end to start to preserve indices
        results.reversed().forEach { result ->
            updatedElements = replace(updatedElements, result, replacement)
        }

        return updatedElements
    }

    fun getNextResult(): FindResult? {
        if (currentResults.isEmpty()) return null
        currentResultIndex = (currentResultIndex + 1) % currentResults.size
        return currentResults.getOrNull(currentResultIndex)
    }

    fun getPreviousResult(): FindResult? {
        if (currentResults.isEmpty()) return null
        currentResultIndex = if (currentResultIndex <= 0) {
            currentResults.size - 1
        } else {
            currentResultIndex - 1
        }
        return currentResults.getOrNull(currentResultIndex)
    }

    fun getCurrentResult(): FindResult? {
        return currentResults.getOrNull(currentResultIndex)
    }

    fun getResultCount(): Int = currentResults.size

    fun getCurrentIndex(): Int = currentResultIndex + 1

    fun clearResults() {
        currentResults = emptyList()
        currentResultIndex = -1
        searchQuery = ""
    }

    fun hasQuery(): Boolean = searchQuery.isNotBlank()

    private fun isWholeWord(text: String, start: Int, end: Int): Boolean {
        val beforeIsBoundary = start == 0 || !text[start - 1].isLetterOrDigit()
        val afterIsBoundary = end >= text.length || !text[end].isLetterOrDigit()
        return beforeIsBoundary && afterIsBoundary
    }
}