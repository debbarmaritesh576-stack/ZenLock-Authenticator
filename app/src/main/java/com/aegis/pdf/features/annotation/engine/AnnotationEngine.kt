package com.aegis.pdf.features.annotation.engine

import android.graphics.PointF
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import com.aegis.pdf.features.annotation.model.Annotation
import com.aegis.pdf.features.annotation.model.AnnotationType
import com.aegis.pdf.features.annotation.model.AnnotationPoint
import kotlin.math.abs
import kotlin.math.sqrt

@Singleton
class AnnotationEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AnnotationEngine"

    suspend fun createAnnotation(
        type: AnnotationType,
        pageNumber: Int,
        points: List<AnnotationPoint>,
        color: Color = Color.Yellow,
        strokeWidth: Float = 2f,
        text: String = ""
    ): Annotation? = withContext(Dispatchers.Default) {
        try {
            if (points.isEmpty() && text.isEmpty()) return@withContext null

            val bounds = calculateBounds(points)

            val annotation = Annotation(
                type = type,
                pageNumber = pageNumber,
                points = points,
                color = color,
                strokeWidth = strokeWidth,
                text = text,
                bounds = bounds
            )

            Log.d(TAG, "Annotation created: ${annotation.id}, type=$type")
            annotation
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create annotation", e)
            null
        }
    }

    fun detectTextSelection(startPoint: PointF, endPoint: PointF): Annotation? {
        return try {
            val bounds = Rect(
                left = minOf(startPoint.x, endPoint.x),
                top = minOf(startPoint.y, endPoint.y),
                right = maxOf(startPoint.x, endPoint.x),
                bottom = maxOf(startPoint.y, endPoint.y)
            )

            Annotation(
                type = AnnotationType.HIGHLIGHT,
                pageNumber = 0,
                startPoint = startPoint,
                endPoint = endPoint,
                bounds = bounds
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect text selection", e)
            null
        }
    }

    fun detectDrawingGesture(points: List<AnnotationPoint>): AnnotationType {
        if (points.size < 3) return AnnotationType.FREEHAND

        try {
            val directions = mutableListOf<Float>()
            for (i in 1 until points.size) {
                val dx = points[i].x - points[i - 1].x
                val dy = points[i].y - points[i - 1].y
                val angle = kotlin.math.atan2(dy, dx)
                directions.add(angle)
            }

            val angleVariance = directions.variance()
            val straightness = 1 - angleVariance

            return when {
                straightness > 0.9f -> {
                    val dx = points.last().x - points.first().x
                    val dy = points.last().y - points.first().y
                    if (abs(dx) > abs(dy)) AnnotationType.LINE else AnnotationType.ARROW
                }
                isCircular(points) -> AnnotationType.CIRCLE
                isRectangular(points) -> AnnotationType.RECTANGLE
                else -> AnnotationType.FREEHAND
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect gesture", e)
            return AnnotationType.FREEHAND
        }
    }

    fun calculateDistance(point1: PointF, point2: PointF): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx * dx + dy * dy)
    }

    fun calculateBounds(points: List<AnnotationPoint>): Rect {
        if (points.isEmpty()) return Rect.Zero

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        return Rect(
            left = minX - 10f,
            top = minY - 10f,
            right = maxX + 10f,
            bottom = maxY + 10f
        )
    }

    fun simplifyPath(points: List<AnnotationPoint>, tolerance: Float = 2f): List<AnnotationPoint> {
        if (points.size < 3) return points

        val simplified = mutableListOf<AnnotationPoint>()
        simplified.add(points.first())

        for (i in 1 until points.size - 1) {
            val prevPoint = points[i - 1]
            val currentPoint = points[i]
            val nextPoint = points[i + 1]

            val distance = pointLineDistance(
                currentPoint.x, currentPoint.y,
                prevPoint.x, prevPoint.y,
                nextPoint.x, nextPoint.y
            )

            if (distance > tolerance) {
                simplified.add(currentPoint)
            }
        }

        simplified.add(points.last())
        return simplified
    }

    fun smoothPath(points: List<AnnotationPoint>): List<AnnotationPoint> {
        if (points.size < 3) return points

        val smoothed = mutableListOf<AnnotationPoint>()
        smoothed.add(points.first())

        for (i in 1 until points.size - 1) {
            val prev = points[i - 1]
            val current = points[i]
            val next = points[i + 1]

            val smoothX = (prev.x + current.x + next.x) / 3
            val smoothY = (prev.y + current.y + next.y) / 3

            smoothed.add(current.copy(x = smoothX, y = smoothY))
        }

        smoothed.add(points.last())
        return smoothed
    }

    fun hitTest(point: PointF, annotation: Annotation, tolerance: Float = 5f): Boolean {
        val bounds = annotation.bounds ?: return false

        return when (annotation.type) {
            AnnotationType.HIGHLIGHT, AnnotationType.UNDERLINE, AnnotationType.STRIKEOUT -> {
                bounds.contains(point)
            }
            AnnotationType.FREEHAND -> {
                annotation.points.any { 
                    calculateDistance(point, PointF(it.x, it.y)) < tolerance
                }
            }
            AnnotationType.RECTANGLE, AnnotationType.CIRCLE -> {
                isPointOnShape(point, annotation, tolerance)
            }
            else -> bounds.contains(point)
        }
    }

    fun exportAnnotations(annotations: List<Annotation>, format: String = "JSON"): String {
        return try {
            when (format) {
                "JSON" -> com.google.gson.Gson().toJson(annotations)
                "XFDF" -> exportToXFDF(annotations)
                else -> ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export annotations", e)
            ""
        }
    }

    fun importAnnotations(data: String, format: String = "JSON"): List<Annotation> {
        return try {
            when (format) {
                "JSON" -> {
                    val type = object : com.google.gson.reflect.TypeToken<List<Annotation>>() {}.type
                    com.google.gson.Gson().fromJson(data, type) ?: emptyList()
                }
                "XFDF" -> importFromXFDF(data)
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import annotations", e)
            emptyList()
        }
    }

    private fun isCircular(points: List<AnnotationPoint>): Boolean {
        if (points.size < 8) return false

        val centroidX = points.map { it.x }.average()
        val centroidY = points.map { it.y }.average()

        val distances = points.map { point ->
            sqrt((point.x - centroidX) * (point.x - centroidX) + 
                 (point.y - centroidY) * (point.y - centroidY))
        }

        val avgDistance = distances.average()
        val variance = distances.map { (it - avgDistance) * (it - avgDistance) }.average()

        return variance < avgDistance * avgDistance * 0.1
    }

    private fun isRectangular(points: List<AnnotationPoint>): Boolean {
        if (points.size < 4) return false
        val hull = convexHull(points)
        return hull.size == 4 || hull.size == 5
    }

    private fun convexHull(points: List<AnnotationPoint>): List<AnnotationPoint> {
        if (points.size < 3) return points

        val sorted = points.sortedWith(compareBy({ it.x }, { it.y }))
        val lower = mutableListOf<AnnotationPoint>()

        for (point in sorted) {
            while (lower.size >= 2 && 
                   crossProduct(lower[lower.size - 2], lower[lower.size - 1], point) <= 0) {
                lower.removeAt(lower.size - 1)
            }
            lower.add(point)
        }

        val upper = mutableListOf<AnnotationPoint>()
        for (point in sorted.asReversed()) {
            while (upper.size >= 2 && 
                   crossProduct(upper[upper.size - 2], upper[upper.size - 1], point) <= 0) {
                upper.removeAt(upper.size - 1)
            }
            upper.add(point)
        }

        lower.removeAt(lower.size - 1)
        upper.removeAt(upper.size - 1)
        return lower + upper
    }

    private fun crossProduct(o: AnnotationPoint, a: AnnotationPoint, b: AnnotationPoint): Float {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    }

    private fun pointLineDistance(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val t = maxOf(0f, minOf(1f, ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)))
        val closestX = x1 + t * dx
        val closestY = y1 + t * dy
        return sqrt((px - closestX) * (px - closestX) + (py - closestY) * (py - closestY))
    }

    private fun isPointOnShape(point: PointF, annotation: Annotation, tolerance: Float): Boolean {
        val bounds = annotation.bounds ?: return false
        return bounds.contains(point)
    }

    private fun exportToXFDF(annotations: List<Annotation>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<xfdf>\n")
        sb.append("<annots>\n")

        annotations.forEach { annotation ->
            sb.append("  <")
            sb.append(annotation.type.name.lowercase())
            sb.append(" color=\"#${annotation.color.hashCode().toString(16)}\" ")
            sb.append("author=\"${annotation.author}\" ")
            sb.append("date=\"${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(annotation.createdTime)}\"")
            sb.append("/>\n")
        }

        sb.append("</annots>\n")
        sb.append("</xfdf>")
        return sb.toString()
    }

    private fun importFromXFDF(data: String): List<Annotation> {
        return try {
            val annotations = mutableListOf<Annotation>()
            val regex = Regex("<(\\w+)[^>]*>")
            val matches = regex.findAll(data)

            matches.forEach { match ->
                val type = match.groupValues[1].uppercase()
                if (type in AnnotationType.values().map { it.name }) {
                    annotations.add(Annotation(type = AnnotationType.valueOf(type), pageNumber = 0))
                }
            }

            annotations
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse XFDF", e)
            emptyList()
        }
    }

    private fun List<Float>.variance(): Float {
        val mean = this.average().toFloat()
        return if (this.isEmpty()) 0f else this.map { (it - mean) * (it - mean) }.average().toFloat()
    }
}