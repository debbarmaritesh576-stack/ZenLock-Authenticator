package com.aegis.pdf.features.scanner.opencv

import android.graphics.Bitmap
import android.graphics.Mat
import android.graphics.Point
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import com.aegis.pdf.features.scanner.model.DocumentBounds

@Singleton
class OpenCvDocumentDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "OpenCvDocumentDetector"

    init {
        org.opencv.core.Core.setNumThreads(4)
        Log.d(TAG, "OpenCV initialized")
    }

    suspend fun detectDocument(bitmap: Bitmap): DocumentBounds? = withContext(Dispatchers.Default) {
        try {
            val mat = Mat()
            org.opencv.android.Utils.bitmapToMat(bitmap, mat)

            val gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)

            val blurred = Mat()
            Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)

            val edges = Mat()
            Imgproc.Canny(blurred, edges, 50.0, 150.0)

            val dilated = Mat()
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
            Imgproc.dilate(edges, dilated, kernel, Point(-1.0, -1.0), 2)

            val contours = mutableListOf<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(dilated, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

            val largestContour = contours.maxByOrNull { Imgproc.contourArea(it) } 
                ?: return@withContext null

            val contourArea = Imgproc.contourArea(largestContour)
            val imageArea = bitmap.width * bitmap.height
            if (contourArea < imageArea * 0.1) {
                return@withContext null
            }

            val epsilon = 0.02 * Imgproc.arcLength(MatOfPoint2f(*largestContour.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*largestContour.toArray()), approx, epsilon, true)

            if (approx.total() != 4L) {
                return@withContext null
            }

            val corners = approx.toArray().sortedBy { it.x + it.y }
            val sortedCorners = sortCorners(corners.toTypedArray())

            val confidence = calculateConfidence(largestContour, bitmap)

            mat.release()
            gray.release()
            blurred.release()
            edges.release()
            dilated.release()
            kernel.release()
            hierarchy.release()

            Log.d(TAG, "Document detected: confidence=$confidence")

            DocumentBounds(
                topLeft = Pair(sortedCorners[0].x.toFloat(), sortedCorners[0].y.toFloat()),
                topRight = Pair(sortedCorners[1].x.toFloat(), sortedCorners[1].y.toFloat()),
                bottomRight = Pair(sortedCorners[2].x.toFloat(), sortedCorners[2].y.toFloat()),
                bottomLeft = Pair(sortedCorners[3].x.toFloat(), sortedCorners[3].y.toFloat()),
                confidence = confidence
            )
        } catch (e: Exception) {
            Log.e(TAG, "Document detection failed", e)
            null
        }
    }

    private fun sortCorners(corners: Array<Point>): Array<Point> {
        val sorted = mutableListOf<Point>()

        val topY = corners.minOf { it.y }
        val bottomY = corners.maxOf { it.y }
        val leftX = corners.minOf { it.x }
        val rightX = corners.maxOf { it.x }

        for (corner in corners) {
            when {
                corner.y <= (topY + bottomY) / 2 && corner.x <= (leftX + rightX) / 2 -> sorted.add(0, corner)
                corner.y <= (topY + bottomY) / 2 -> sorted.add(1, corner)
                corner.x <= (leftX + rightX) / 2 -> sorted.add(3, corner)
                else -> sorted.add(2, corner)
            }
        }

        return sorted.toTypedArray()
    }

    private fun calculateConfidence(contour: MatOfPoint, bitmap: Bitmap): Float {
        val rect = Imgproc.boundingRect(contour)
        val imageArea = bitmap.width * bitmap.height
        val contourArea = Imgproc.contourArea(contour)
        
        val areaRatio = contourArea / imageArea
        val areaConfidence = areaRatio.coerceIn(0f, 1f)

        val perimeter = Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
        val circularity = (4 * Math.PI * contourArea) / (perimeter * perimeter)
        val circularityConfidence = (1 - Math.abs(circularity - 0.8)).toFloat().coerceIn(0f, 1f)

        return (areaConfidence + circularityConfidence) / 2
    }
}