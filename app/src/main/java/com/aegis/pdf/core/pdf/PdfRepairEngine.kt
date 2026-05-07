package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfRepairEngine @Inject constructor() {

    fun repairCorrupted(inputFile: File, outputFile: File): RepairResult {
        return try {
            val originalSize = inputFile.length()
            PDDocument.load(inputFile).use { document ->
                document.save(outputFile)
            }
            val repairedSize = outputFile.length()
            RepairResult(
                success = true,
                originalSize = originalSize,
                repairedSize = repairedSize,
                message = "PDF repaired successfully"
            )
        } catch (e: Exception) {
            RepairResult(
                success = false,
                message = "Repair failed: ${e.message}"
            )
        }
    }

    fun validatePdf(file: File): ValidationResult {
        return try {
            PDDocument.load(file).use { document ->
                val issues = mutableListOf<String>()
                if (document.numberOfPages == 0) issues.add("No pages found")
                if (document.isEncrypted) issues.add("Document is encrypted")
                
                ValidationResult(
                    isValid = issues.isEmpty(),
                    pageCount = document.numberOfPages,
                    issues = issues
                )
            }
        } catch (e: Exception) {
            ValidationResult(isValid = false, issues = listOf(e.message ?: "Unknown error"))
        }
    }

    data class RepairResult(
        val success: Boolean,
        val originalSize: Long = 0,
        val repairedSize: Long = 0,
        val message: String = ""
    )

    data class ValidationResult(
        val isValid: Boolean,
        val pageCount: Int = 0,
        val issues: List<String> = emptyList()
    )
}