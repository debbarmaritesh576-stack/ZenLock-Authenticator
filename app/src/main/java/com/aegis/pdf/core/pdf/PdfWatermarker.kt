package com.aegis.pdf.core.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.io.FileOutputStream

class PdfWatermarker {

    fun addTextWatermark(
        inputFile: File,
        outputFile: File,
        text: String,
        fontSize: Float = 48f,
        opacity: Float = 0.3f
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                document.pages.forEach { page ->
                    val cs = PDPageContentStream(
                        document, page,
                        PDPageContentStream.AppendMode.APPEND,
                        true
                    )
                    cs.setNonStrokingColor(Color.argb((opacity * 255).toInt(), 128, 128, 128))
                    cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize)
                    cs.beginText()
                    cs.setTextMatrix(
                        java.awt.geom.AffineTransform.getRotateInstance(
                            Math.toRadians(45.0),
                            200.0, 400.0
                        )
                    )
                    cs.showText(text)
                    cs.endText()
                    cs.close()
                }
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addImageWatermark(
        inputFile: File,
        outputFile: File,
        imageFile: File,
        opacity: Float = 0.3f
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val pdImage = PDImageXObject.createFromFile(imageFile.absolutePath, document)
                document.pages.forEach { page ->
                    val cs = PDPageContentStream(
                        document, page,
                        PDPageContentStream.AppendMode.APPEND,
                        true
                    )
                    val pageWidth = page.mediaBox.width
                    val pageHeight = page.mediaBox.height
                    val imageWidth = pageWidth * 0.5f
                    val imageHeight = imageWidth * pdImage.height / pdImage.width
                    val x = (pageWidth - imageWidth) / 2
                    val y = (pageHeight - imageHeight) / 2

                    cs.setNonStrokingColor(Color.argb((opacity * 255).toInt(), 128, 128, 128))
                    cs.drawImage(pdImage, x, y, imageWidth, imageHeight)
                    cs.close()
                }
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}