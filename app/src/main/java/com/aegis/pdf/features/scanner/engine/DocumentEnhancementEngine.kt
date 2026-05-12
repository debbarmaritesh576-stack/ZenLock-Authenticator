package com.aegis.pdf.features.scanner.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.aegis.pdf.features.scanner.model.DocumentBounds
import com.aegis.pdf.features.scanner.model.ScanResult
import com.aegis.pdf.features.scanner.model.ScanSettings

@Singleton
class DocumentEnhancementEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "DocumentEnhancementEngine"

    suspend fun enhanceDocument(
        bitmap: Bitmap,
        bounds: DocumentBounds,
        settings: ScanSettings
    ): ScanResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        
        try {
            var processed = bitmap.copy(bitmap.config, true)
            
            if (settings.perspectiveCorrection) {
                processed = perspectiveCorrection(processed, bounds)
            }
            
            if (settings.removeGlare) {
                processed = removeGlare(processed)
            }
            
            if (settings.removeNoise) {
                processed = removeNoise(processed)
            }
            
            if (settings.increaseContrast) {
                processed = adjustContrast(processed, 1.5f)
            }
            
            when (settings.colorMode) {
                com.aegis.pdf.features.scanner.model.ColorMode.GRAYSCALE -> {
                    processed = toGrayscale(processed)
                }
                com.aegis.pdf.features.scanner.model.ColorMode.BLACK_WHITE -> {
                    processed = toBlackWhite(processed)
                }
                com.aegis.pdf.features.scanner.model.ColorMode.AUTO -> {
                    processed = autoColorMode(processed)
                }
                else -> {}
            }
            
            if (settings.enableMagicColor) {
                processed = magicColor(processed)
            }
            
            val quality = calculateQuality(processed)
            val processingTime = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "Enhancement completed: quality=$quality, time=${processingTime}ms")
            
            ScanResult(
                originalBitmap = bitmap,
                processedBitmap = processed,
                documentBounds = bounds,
                quality = quality,
                processingTime = processingTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Enhancement failed", e)
            throw e
        }
    }

    private fun perspectiveCorrection(bitmap: Bitmap, bounds: DocumentBounds): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val srcPixels = FloatArray(8)
        srcPixels[0] = bounds.topLeft.first
        srcPixels[1] = bounds.topLeft.second
        srcPixels[2] = bounds.topRight.first
        srcPixels[3] = bounds.topRight.second
        srcPixels[4] = bounds.bottomRight.first
        srcPixels[5] = bounds.bottomRight.second
        srcPixels[6] = bounds.bottomLeft.first
        srcPixels[7] = bounds.bottomLeft.second
        
        val dstPixels = floatArrayOf(
            0f, 0f,
            width.toFloat(), 0f,
            width.toFloat(), height.toFloat(),
            0f, height.toFloat()
        )
        
        val perspectiveMatrix = getPerspectiveTransform(srcPixels, dstPixels)
        
        val corrected = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val srcPixelArray = IntArray(width * height)
        bitmap.getPixels(srcPixelArray, 0, width, 0, 0, width, height)
        
        val dstPixelArray = IntArray(width * height)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val srcCoords = perspectiveMatrix.map(floatArrayOf(x.toFloat(), y.toFloat()))
                val srcX = srcCoords[0].toInt().coerceIn(0, width - 1)
                val srcY = srcCoords[1].toInt().coerceIn(0, height - 1)
                
                dstPixelArray[y * width + x] = srcPixelArray[srcY * width + srcX]
            }
        }
        
        corrected.setPixels(dstPixelArray, 0, width, 0, 0, width, height)
        return corrected
    }

    private fun removeGlare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val brightPixels = mutableListOf<Int>()
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val brightness = (r + g + b) / 3
            if (brightness > 220) {
                brightPixels.add(pixel)
            }
        }
        
        if (brightPixels.isEmpty()) return bitmap
        
        val avgBrightness = brightPixels.map { 
            val r = (it shr 16) and 0xFF
            val g = (it shr 8) and 0xFF
            val b = it and 0xFF
            (r + g + b) / 3
        }.average().toInt()
        
        val corrected = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            val brightness = (r + g + b) / 3
            
            if (brightness > 200) {
                val factor = (200 / brightness.toFloat()).coerceIn(0f, 1f)
                val newR = (r * factor).toInt()
                val newG = (g * factor).toInt()
                val newB = (b * factor).toInt()
                pixels[i] = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }
        
        corrected.setPixels(pixels, 0, width, 0, 0, width, height)
        return corrected
    }

    private fun removeNoise(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val srcPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
        
        val dstPixels = IntArray(width * height)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val neighborPixels = mutableListOf<Int>()
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        neighborPixels.add((srcPixels[(y + dy) * width + (x + dx)] shr 16) and 0xFF)
                    }
                }
                
                neighborPixels.sort()
                val median = neighborPixels[neighborPixels.size / 2]
                dstPixels[y * width + x] = (0xFF shl 24) or (median shl 16) or (median shl 8) or median
            }
        }
        
        val denoised = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        denoised.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return denoised
    }

    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            
            val newR = ((r - 128) * contrast + 128).toInt().coerceIn(0, 255)
            val newG = ((g - 128) * contrast + 128).toInt().coerceIn(0, 255)
            val newB = ((b - 128) * contrast + 128).toInt().coerceIn(0, 255)
            
            pixels[i] = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
        }
        
        val adjusted = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        adjusted.setPixels(pixels, 0, width, 0, 0, width, height)
        return adjusted
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
        
        val grayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        grayscale.setPixels(pixels, 0, width, 0, 0, width, height)
        return grayscale
    }

    private fun toBlackWhite(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var totalBrightness = 0L
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b).toLong()
        }
        val threshold = (totalBrightness / pixels.size).toInt()
        
        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            val bw = if (gray > threshold) 255 else 0
            pixels[i] = (0xFF shl 24) or (bw shl 16) or (bw shl 8) or bw
        }
        
        val bw = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bw.setPixels(pixels, 0, width, 0, 0, width, height)
        return bw
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
            colorfulness += kotlin.math.abs(r - g) + kotlin.math.abs(g - b)
        }
        colorfulness /= pixels.size
        
        return if (colorfulness > 30) bitmap else toGrayscale(bitmap)
    }

    private fun magicColor(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            
            val newR = (r * 1.1f).toInt().coerceIn(0, 255)
            val newG = (g * 1.05f).toInt().coerceIn(0, 255)
            val newB = (b * 0.95f).toInt().coerceIn(0, 255)
            
            pixels[i] = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
        }
        
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        enhanced.setPixels(pixels, 0, width, 0, 0, width, height)
        return enhanced
    }

    private fun calculateQuality(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var sharpness = 0.0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = (pixels[y * width + x] shr 16) and 0xFF
                val neighbors = listOf(
                    (pixels[(y - 1) * width + x] shr 16) and 0xFF,
                    (pixels[(y + 1) * width + x] shr 16) and 0xFF,
                    (pixels[y * width + (x - 1)] shr 16) and 0xFF,
                    (pixels[y * width + (x + 1)] shr 16) and 0xFF
                )
                sharpness += neighbors.map { kotlin.math.abs(center - it) }.average()
            }
        }
        sharpness /= ((width - 2) * (height - 2))
        
        return (sharpness / 255).toFloat().coerceIn(0f, 1f)
    }

    private fun getPerspectiveTransform(src: FloatArray, dst: FloatArray): PerspectiveMatrix {
        return PerspectiveMatrix(src, dst)
    }

    data class PerspectiveMatrix(val src: FloatArray, val dst: FloatArray) {
        fun map(point: FloatArray): FloatArray {
            return point
        }
    }
}