package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfCompareEngine @Inject constructor() {

    fun compareDocuments(
        file1: File,
        file2: File
    ): CompareResult {
        return try {
            PDDocument.load(file1).use { doc1 ->
                PDDocument.load(file2).use { doc2 ->
                    val stripper = PDFTextStripper()
                    val text1 = stripper.getText(doc1)
                    val text2 = stripper.getText(doc2)

                    val differences = findDifferences(text1, text2)
                    val similarity = calculateSimilarity(text1, text2)

                    CompareResult(
                        similarity = similarity,
                        pageCount1 = doc1.numberOfPages,
                        pageCount2 = doc2.numberOfPages,
                        differences = differences
                    )
                }
            }
        } catch (e: Exception) {
            CompareResult(similarity = 0f)
        }
    }

    private fun findDifferences(text1: String, text2: String): List<Difference> {
        val diffs = mutableListOf<Difference>()
        val lines1 = text1.split("\n")
        val lines2 = text2.split("\n")
        val maxLines = maxOf(lines1.size, lines2.size)

        for (i in 0 until maxLines) {
            val line1 = lines1.getOrNull(i) ?: ""
            val line2 = lines2.getOrNull(i) ?: ""
            if (line1 != line2) {
                diffs.add(Difference(line = i + 1, text1 = line1, text2 = line2))
            }
        }
        return diffs.take(50)
    }

    private fun calculateSimilarity(text1: String, text2: String): Float {
        if (text1.isEmpty() && text2.isEmpty()) return 1f
        if (text1.isEmpty() || text2.isEmpty()) return 0f

        val words1 = text1.split("\\s+".toRegex()).toSet()
        val words2 = text2.split("\\s+".toRegex()).toSet()
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return if (union == 0) 0f else intersection.toFloat() / union
    }

    data class CompareResult(
        val similarity: Float = 0f,
        val pageCount1: Int = 0,
        val pageCount2: Int = 0,
        val differences: List<Difference> = emptyList()
    )

    data class Difference(
        val line: Int,
        val text1: String,
        val text2: String
    )
}