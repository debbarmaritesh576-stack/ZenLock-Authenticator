
package com.aegis.pdf.core.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.io.FileOutputStream

class PdfCompressor {

    enum class Quality(val scale: Int) {
        LOW(600),
        MEDIUM(1200),
        HIGH(1920)
    }

    fun compress(inputFile: File, outputFile: File, quality: Quality = Quality.MEDIUM): Result {
        return try {
            val originalSize = inputFile.length()
            PDDocument.load(inputFile).use { document ->
                document.pages.forEach { page ->
                    page.resources?.xObjectNames?.forEach { name ->
                        val xObject = page.resources.getXObject(name)
                        if (xObject is PDImageXObject) {
                            compressImage(xObject, quality)
                        }
                    }
                }
                document.save(outputFile)
            }
            val compressedSize = outputFile.length()
            val saved = ((originalSize - compressedSize).toFloat() / originalSize * 100)
            Result(true, originalSize, compressedSize, saved.coerceAtLeast(0f))
        } catch (e: Exception) {
            Result(false)
        }
    }

    private fun compressImage(image: PDImageXObject, quality: Quality) {
        try {
            val bitmap = image.image
            if (bitmap.width > quality.scale || bitmap.height > quality.scale) {
                val ratio = minOf(
                    quality.scale.toFloat() / bitmap.width,
                    quality.scale.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            }
        } catch (e: Exception) {
            // Skip compression on failure
        }
    }

    data class Result(
        val success: Boolean,
        val originalSize: Long = 0,
        val compressedSize: Long = 0,
        val savedPercentage: Float = 0f
    )
}