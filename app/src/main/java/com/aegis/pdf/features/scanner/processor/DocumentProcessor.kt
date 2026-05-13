package com.aegis.pdf.features.scanner.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import com.aegis.pdf.features.scanner.model.DocumentBounds
import com.aegis.pdf.features.scanner.model.ScanSettings
import com.aegis.pdf.features.scanner.model.ColorMode
import kotlin.math.abs
import kotlin.math.sqrt

@Singleton
class DocumentProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "DocumentProcessor"

    suspend fun processDocument(
        bitmap: Bitmap,
        bounds: DocumentBounds,
        settings: ScanSettings
    ): Bitmap = withContext(Dispatchers.Default) {
        try {
            var processed = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            if (settings.perspectiveCorrection) {
                processed = perspectiveCorrection(processed, bounds)
            }

            if (settings.removeGlare) {
                processed = removeGlare(processed)
            }

            if (settings.removeNoise && bitmap.width <= 1500) {
                processed = bilateralFilter(processed, 9, 75f, 75f)
            } else if (settings.removeNoise) {
                Log.d(TAG, "Skipping bilateral filter for large bitmap (performance)")
            }

            if (settings.increaseContrast) {
                processed = adjustContrast(processed, 1.3f)
            }

            when (settings.colorMode) {
                ColorMode.GRAYSCALE -> processed = toGrayscale(processed)
                ColorMode.BLACK_WHITE -> processed = adaptiveThreshold(processed)
                ColorMode.AUTO -> processed = autoColorMode(processed)
                ColorMode.COLOR -> {
                    if (settings.enableMagicColor) {
                        processed = magicColor(processed)
                    }
                }
            }

            if (settings.autoEnhance) {
                processed = autoEnhance(processed)
            }

            Log.d(TAG, "Document processed successfully")
            processed
        } catch (e: Exception) {
            Log.e(TAG, "Document processing failed", e)
            throw e
        }
    }

    private fun perspectiveCorrection(bitmap: Bitmap, bounds: DocumentBounds): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val srcPoints = floatArrayOf(
            bounds.topLeft.first, bounds.topLeft.second,
            bounds.topRight.first, bounds.topRight.second,
            bounds.bottomRight.first, bounds.bottomRight.second,
            bounds.bottomLeft.first, bounds.bottomLeft.second
        )

        val dstPoints = floatArrayOf(
            0f, 0f,
            width.toFloat(), 0f,
            width.toFloat(), height.toFloat(),
            0f, height.toFloat()
        )

        val matrix = getTransformMatrix(srcPoints, dstPoints)
        val corrected = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(corrected)
        canvas.drawBitmap(bitmap, matrix, Paint(Paint.FILTER_BITMAP_FLAG))

        Log.d(TAG, "Perspective correction applied")
        return corrected
    }

    private fun removeGlare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val brightPixelThreshold = 240

        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF

            val brightness = (r + g + b) / 3
            if (brightness > brightPixelThreshold) {
                val factor = (200f / brightness)
                val newR = (r * factor).toInt().coerceIn(0, 255)
                val newG = (g * factor).toInt().coerceIn(0, 255)
                val newB = (b * factor).toInt().coerceIn(0, 255)
                pixels[i] = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)

        Log.d(TAG, "Glare removal applied")
        return result
    }

    private fun bilateralFilter(
        bitmap: Bitmap,
        kernelSize: Int,
        sigmaColor: Float,
        sigmaSpatial: Float
    ): Bitmap {
        if (bitmap.width > 1500) {
            Log.d(TAG, "Skipping bilateral filter for large bitmap")
            return bitmap
        }

        val width = bitmap.width
        val height = bitmap.height
        val radius = kernelSize / 2

        val srcPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val dstPixels = IntArray(width * height)

        for (y in radius until height - radius) {
            for (x in radius until width - radius) {
                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                var totalWeight = 0f

                val centerPixel = srcPixels[y * width + x]
                val centerR = (centerPixel shr 16) and 0xFF
                val centerG = (centerPixel shr 8) and 0xFF
                val centerB = centerPixel and 0xFF

                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val neighborPixel = srcPixels[(y + dy) * width + (x + dx)]
                        val neighborR = (neighborPixel shr 16) and 0xFF
                        val neighborG = (neighborPixel shr 8) and 0xFF
                        val neighborB = neighborPixel and 0xFF

                        val colorDiff = sqrt(
                            ((centerR - neighborR) * (centerR - neighborR) +
                            (centerG - neighborG) * (centerG - neighborG) +
                            (centerB - neighborB) * (centerB - neighborB)).toFloat()
                        )
                        val spatialDiff = sqrt((dx * dx + dy * dy).toFloat())

                        val colorWeight = kotlin.math.exp(-colorDiff / (2 * sigmaColor * sigmaColor))
                        val spatialWeight = kotlin.math.exp(-spatialDiff / (2 * sigmaSpatial * sigmaSpatial))
                        val weight = colorWeight * spatialWeight

                        sumR += neighborR * weight
                        sumG += neighborG * weight
                        sumB += neighborB * weight
                        totalWeight += weight
                    }
                }

                val finalR = (sumR / totalWeight).toInt().coerceIn(0, 255)
                val finalG = (sumG / totalWeight).toInt().coerceIn(0, 255)
                val finalB = (sumB / totalWeight).toInt().coerceIn(0, 255)

                dstPixels[y * width + x] = (0xFF shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(dstPixels, 0, width, 0, 0, width, height)

        Log.d(TAG, "Bilateral filter applied")
        return result
    }

    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, (1 - contrast) * 128,
                0f, contrast, 0f, 0f, (1 - contrast) * 128,
                0f, 0f, contrast, 0f, (1 - contrast) * 128,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        Log.d(TAG, "Contrast adjusted: $contrast")
        return result
    }

    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF

            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)

        Log.d(TAG, "Grayscale conversion applied")
        return result
    }

    private fun adaptiveThreshold(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val gray = toGrayscale(bitmap)
        val pixels = IntArray(width * height)
        gray.getPixels(pixels, 0, width, 0, 0, width, height)

        val windowSize = 31
        val constant = 10
        val result = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val minY = (y - windowSize / 2).coerceIn(0, height - 1)
                val maxY = (y + windowSize / 2).coerceIn(0, height - 1)
                val minX = (x - windowSize / 2).coerceIn(0, width - 1)
                val maxX = (x + windowSize / 2).coerceIn(0, width - 1)

                var sum = 0L
                var count = 0

                for (wy in minY..maxY) {
                    for (wx in minX..maxX) {
                        val pixel = pixels[wy * width + wx]
                        sum += (pixel shr 16) and 0xFF
                        count++
                    }
                }

                val mean = sum / count
                val currentPixel = (pixels[y * width + x] shr 16) and 0xFF

                val value = if (currentPixel > mean - constant) 255 else 0
                result[y * width + x] = (0xFF shl 24) or (value shl 16) or (value shl 8) or value
            }
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(result, 0, width, 0, 0, width, height)
        safeRecycle(gray)

        Log.d(TAG, "Adaptive threshold applied")
        return output
    }

    private fun autoColorMode(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var colorfulness = 0.0
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            colorfulness += abs(r - g) + abs(g - b)
        }
        colorfulness /= pixels.size

        return if (colorfulness > 30) bitmap else toGrayscale(bitmap)
    }

    private fun magicColor(bitmap: Bitmap): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1.1f, 0f, 0f, 0f, 0f,
                0f, 1.05f, 0f, 0f, 0f,
                0f, 0f, 0.95f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        Log.d(TAG, "Magic color applied")
        return result
    }

    private fun autoEnhance(bitmap: Bitmap): Bitmap {
        var enhanced = adjustContrast(bitmap, 1.2f)
        val final = adjustBrightness(enhanced, 1.1f)
        safeRecycle(enhanced)

        Log.d(TAG, "Auto enhance applied")
        return final
    }

    private fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, (brightness - 1) * 255,
                0f, 1f, 0f, 0f, (brightness - 1) * 255,
                0f, 0f, 1f, 0f, (brightness - 1) * 255,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        Log.d(TAG, "Bitmap rotated: $degrees degrees")
        return rotated
    }

    private fun getTransformMatrix(src: FloatArray, dst: FloatArray): Matrix {
        val matrix = Matrix()
        matrix.setPolyToPoly(src, 0, dst, 0, 4)
        return matrix
    }

    private fun safeRecycle(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}