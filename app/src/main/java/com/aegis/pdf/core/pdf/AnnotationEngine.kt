package com.aegis.pdf.core.pdf

import android.graphics.Color
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationEngine @Inject constructor() {

    data class TextAnnotation(
        val page: Int,
        val text: String,
        val x: Float,
        val y: Float,
        val fontSize: Float = 12f,
        val color: Int = Color.RED
    )

    data class HighlightArea(
        val page: Int,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int = Color.YELLOW
    )

    fun addTextAnnotation(
        inputFile: File,
        outputFile: File,
        annotations: List<TextAnnotation>
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                annotations.groupBy { it.page }.forEach { (pageNum, pageAnnotations) ->
                    if (pageNum in 1..document.numberOfPages) {
                        val page = document.getPage(pageNum - 1)
                        PDPageContentStream(
                            document, page,
                            PDPageContentStream.AppendMode.APPEND,
                            true
                        ).use { cs ->
                            pageAnnotations.forEach { ann ->
                                cs.setNonStrokingColor(
                                    Color.red(ann.color),
                                    Color.green(ann.color),
                                    Color.blue(ann.color)
                                )
                                cs.setFont(PDType1Font.HELVETICA, ann.fontSize)
                                cs.beginText()
                                cs.newLineAtOffset(ann.x, ann.y)
                                cs.showText(ann.text)
                                cs.endText()
                            }
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

    fun addHighlight(
        inputFile: File,
        outputFile: File,
        highlights: List<HighlightArea>
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                highlights.groupBy { it.page }.forEach { (pageNum, pageHighlights) ->
                    if (pageNum in 1..document.numberOfPages) {
                        val page = document.getPage(pageNum - 1)
                        PDPageContentStream(
                            document, page,
                            PDPageContentStream.AppendMode.APPEND,
                            true
                        ).use { cs ->
                            cs.setNonStrokingColor(
                                Color.red(Color.YELLOW),
                                Color.green(Color.YELLOW),
                                Color.blue(Color.YELLOW)
                            )
                            pageHighlights.forEach { area ->
                                cs.addRect(area.x, area.y, area.width, area.height)
                                cs.fill()
                            }
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