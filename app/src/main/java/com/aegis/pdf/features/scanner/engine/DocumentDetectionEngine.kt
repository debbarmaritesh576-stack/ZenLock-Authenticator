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
import kotlin.math.sqrt

@Singleton
class DocumentDetectionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "DocumentDetectionEngine"

    suspend fun detectDocumentBounds(bitmap: Bitmap): DocumentBounds? = withContext(Dispatchers.Default) {
        try {
            val gray = convertToGrayscale(bitmap)
            val edges = detectEdges(gray)
            val contours = findContours(edges)
            val largestContour = contours.maxByOrNull { it.size } ?: return@withContext null
            
            if (largestContour.size < 4) return@withContext null

            val bounds = approximatePolygon(largestContour)
            if (bounds.size != 4) return@withContext null

            val confidence = calculateConfidence(bounds, bitmap)
            
            Log.d(TAG, "Document detected with confidence: $confidence")
            
            DocumentBounds(
                topLeft = bounds[0],
                topRight = bounds[1],
                bottomRight = bounds[2],
                bottomLeft = bounds[3],
                confidence = confidence
            )
        } catch (e: Exception) {
            Log.e(TAG, "Document detection failed", e)
            null
        }
    }

    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val gray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            val gray_value = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = (0xFF shl 24) or (gray_value shl 16) or (gray_value shl 8) or gray_value
        }
        
        gray.setPixels(pixels, 0, width, 0, 0, width, height)
        return gray
    }

    private fun detectEdges(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val edges = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val edgePixels = IntArray(width * height)
        
        val sobelX = intArrayOf(-1, 0, 1, -2, 0, 2, -1, 0, 1)
        val sobelY = intArrayOf(-1, -2, -1, 0, 0, 0, 1, 2, 1)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var gx = 0
                var gy = 0
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val idx = (y + ky) * width + (x + kx)
                        val gray = (pixels[idx] shr 16) and 0xFF
                        val sobelIdx = (ky + 1) * 3 + (kx + 1)
                        gx += gray * sobelX[sobelIdx]
                        gy += gray * sobelY[sobelIdx]
                    }
                }
                
                val magnitude = sqrt((gx * gx + gy * gy).toFloat()).toInt()
                val threshold = 50
                val edgeValue = if (magnitude > threshold) 255 else 0
                edgePixels[y * width + x] = (0xFF shl 24) or (edgeValue shl 16) or (edgeValue shl 8) or edgeValue
            }
        }
        
        edges.setPixels(edgePixels, 0, width, 0, 0, width, height)
        return edges
    }

    private fun findContours(bitmap: Bitmap): List<List<Pair<Float, Float>>> {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val visited = Array(height) { BooleanArray(width) }
        val contours = mutableListOf<List<Pair<Float, Float>>>()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!visited[y][x] && isWhitePixel(pixels[y * width + x])) {
                    val contour = traceContour(pixels, visited, x, y, width, height)
                    if (contour.size > 10) {
                        contours.add(contour)
                    }
                }
            }
        }
        
        return contours
    }

    private fun traceContour(
        pixels: IntArray,
        visited: Array<BooleanArray>,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int
    ): List<Pair<Float, Float>> {
        val contour = mutableListOf<Pair<Float, Float>>()
        var x = startX
        var y = startY
        
        do {
            visited[y][x] = true
            contour.add(Pair(x.toFloat(), y.toFloat()))
            
            var found = false
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val nx = x + dx
                    val ny = y + dy
                    
                    if (nx in 0 until width && ny in 0 until height &&
                        !visited[ny][nx] && isWhitePixel(pixels[ny * width + nx])) {
                        x = nx
                        y = ny
                        found = true
                        break
                    }
                }
                if (found) break
            }
            
            if (!found) break
        } while (contour.size < 1000)
        
        return contour
    }

    private fun approximatePolygon(contour: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        if (contour.size < 4) return emptyList()
        
        val corners = mutableListOf<Pair<Float, Float>>()
        val step = contour.size / 4
        
        for (i in 0..3) {
            corners.add(contour[i * step % contour.size])
        }
        
        return sortCorners(corners)
    }

    private fun sortCorners(corners: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        val sorted = corners.sortedWith(compareBy({ it.second }, { it.first }))
        val topLeft = if (sorted[0].first < sorted[1].first) sorted[0] else sorted[1]
        val topRight = if (sorted[0].first < sorted[1].first) sorted[1] else sorted[0]
        val bottomLeft = if (sorted[2].first < sorted[3].first) sorted[2] else sorted[3]
        val bottomRight = if (sorted[2].first < sorted[3].first) sorted[3] else sorted[2]
        
        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    private fun calculateConfidence(bounds: List<Pair<Float, Float>>, bitmap: Bitmap): Float {
        if (bounds.size != 4) return 0f
        
        val (tl, tr, br, bl) = bounds
        val topWidth = sqrt((tr.first - tl.first) * (tr.first - tl.first) + 
                           (tr.second - tl.second) * (tr.second - tl.second))
        val bottomWidth = sqrt((br.first - bl.first) * (br.first - bl.first) + 
                              (br.second - bl.second) * (br.second - bl.second))
        val leftHeight = sqrt((bl.first - tl.first) * (bl.first - tl.first) + 
                             (bl.second - tl.second) * (bl.second - tl.second))
        val rightHeight = sqrt((br.first - tr.first) * (br.first - tr.first) + 
                              (br.second - tr.second) * (br.second - tr.second))
        
        val aspectRatio = (topWidth + bottomWidth) / (leftHeight + rightHeight) / 2
        val aspectConfidence = if (aspectRatio in 0.5f..2f) 1f else 0.5f
        
        val area = (topWidth + bottomWidth) / 2 * (leftHeight + rightHeight) / 2
        val minArea = bitmap.width * bitmap.height * 0.1f
        val areaConfidence = (area / minArea).coerceIn(0f, 1f)
        
        return (aspectConfidence + areaConfidence) / 2
    }

    private fun isWhitePixel(pixel: Int): Boolean {
        val gray = (pixel shr 16) and 0xFF
        return gray > 128
    }
}