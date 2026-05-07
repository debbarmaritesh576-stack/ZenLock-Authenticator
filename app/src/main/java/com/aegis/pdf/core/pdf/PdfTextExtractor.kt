package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

class PdfTextExtractor {

    fun extractText(file: File): String {
        return try {
            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                stripper.getText(document)
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun extractTextFromPage(file: File, pageNumber: Int): String {
        return try {
            PDDocument.load(file).use { document ->
                if (pageNumber in 1..document.numberOfPages) {
                    val stripper = PDFTextStripper()
                    stripper.startPage = pageNumber
                    stripper.endPage = pageNumber
                    stripper.getText(document)
                } else ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun searchInPdf(file: File, query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        return try {
            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                for (page in 1..document.numberOfPages) {
                    stripper.startPage = page
                    stripper.endPage = page
                    val text = stripper.getText(document)
                    var index = text.indexOf(query, ignoreCase = true)
                    while (index >= 0) {
                        val snippet = text.substring(
                            maxOf(0, index - 30),
                            minOf(text.length, index + query.length + 30)
                        )
                        results.add(SearchResult(page, snippet.trim()))
                        index = text.indexOf(query, index + 1, ignoreCase = true)
                    }
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class SearchResult(
        val page: Int,
        val snippet: String
    )
}