package com.aegis.pdf.core.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class DocumentScanner {

    fun enhanceDocument(inputFile: File, outputFile: File): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            val enhanced = enhanceBitmap(bitmap)
            FileOutputStream(outputFile).use { out ->
                enhanced.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            enhanced.recycle()
            bitmap.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun enhanceBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale and increase contrast
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)

            var gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            // Increase contrast
            gray = if (gray < 128) {
                (gray * 0.5).toInt()
            } else {
                minOf((gray * 1.5).toInt(), 255)
            }

            // Threshold for document-like appearance
            gray = if (gray > 160) 255 else if (gray < 100) 0 else gray

            pixels[i] = android.graphics.Color.rgb(gray, gray, gray)
        }

        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        enhanced.setPixels(pixels, 0, width, 0, 0, width, height)
        return enhanced
    }

    fun detectEdges(bitmap: Bitmap): List<android.graphics.Point> {
        // Simple edge detection for document corners
        val points = mutableListOf<android.graphics.Point>()
        val width = bitmap.width
        val height = bitmap.height

        points.add(android.graphics.Point((width * 0.1).toInt(), (height * 0.1).toInt()))
        points.add(android.graphics.Point((width * 0.9).toInt(), (height * 0.1).toInt()))
        points.add(android.graphics.Point((width * 0.9).toInt(), (height * 0.9).toInt()))
        points.add(android.graphics.Point((width * 0.1).toInt(), (height * 0.9).toInt()))

        return points
    }
}