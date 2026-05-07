package com.aegis.pdf.core.security

import android.graphics.Color
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedactionEngine @Inject constructor() {

    data class RedactionArea(
        val page: Int,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )

    fun applyRedactions(
        inputFile: File,
        outputFile: File,
        areas: List<RedactionArea>
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                areas.groupBy { it.page }.forEach { (pageNum, pageAreas) ->
                    if (pageNum in 1..document.numberOfPages) {
                        val page = document.getPage(pageNum - 1)
                        PDPageContentStream(
                            document, page,
                            PDPageContentStream.AppendMode.APPEND,
                            true
                        ).use { cs ->
                            cs.setNonStrokingColor(Color.BLACK)
                            pageAreas.forEach { area ->
                                cs.addRect(area.x, area.y, area.width, area.height)
                                cs.fill()
                            }
                            cs.close()
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

    fun autoRedactPatterns(
        inputFile: File,
        outputFile: File,
        patterns: List<String>
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}