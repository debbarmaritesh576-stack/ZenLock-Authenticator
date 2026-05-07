package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderFooterEditor @Inject constructor() {

    fun addHeaderFooter(
        inputFile: File,
        outputFile: File,
        headerText: String = "",
        footerText: String = "",
        fontSize: Float = 10f,
        showPageNumbers: Boolean = true
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val totalPages = document.numberOfPages

                document.pages.forEachIndexed { index, page ->
                    val pageWidth = page.mediaBox.width
                    val pageHeight = page.mediaBox.height

                    PDPageContentStream(
                        document, page,
                        PDPageContentStream.AppendMode.APPEND,
                        true
                    ).use { cs ->
                        cs.setFont(PDType1Font.HELVETICA, fontSize)
                        cs.setNonStrokingColor(128, 128, 128)

                        // Header
                        if (headerText.isNotBlank()) {
                            cs.beginText()
                            cs.newLineAtOffset(pageWidth / 2, pageHeight - 30f)
                            cs.showText(headerText)
                            cs.endText()
                        }

                        // Footer with page numbers
                        if (footerText.isNotBlank() || showPageNumbers) {
                            val footer = buildString {
                                if (footerText.isNotBlank()) append(footerText)
                                if (showPageNumbers) {
                                    if (isNotEmpty()) append(" - ")
                                    append("Page ${index + 1} of $totalPages")
                                }
                            }
                            cs.beginText()
                            cs.newLineAtOffset(pageWidth / 2, 20f)
                            cs.showText(footer)
                            cs.endText()
                        }
                    }
                }
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}