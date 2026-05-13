package com.aegis.pdf.features.annotation.model

import android.graphics.PointF
import androidx.compose.ui.graphics.Color

enum class AnnotationType {
    HIGHLIGHT, UNDERLINE, STRIKEOUT, SQUIGGLY,
    FREEHAND, ARROW, LINE, RECTANGLE, CIRCLE, POLYGON,
    STAMP, STICKY_NOTE, ERASER, SIGNATURE, TEXT_BOX
}

data class AnnotationPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val timestamp: Long = System.currentTimeMillis()
)

data class Annotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: AnnotationType,
    val pageNumber: Int,
    val points: List<AnnotationPoint> = emptyList(),
    val startPoint: PointF? = null,
    val endPoint: PointF? = null,
    val color: Color = Color.Yellow,
    val strokeWidth: Float = 2f,
    val opacity: Float = 1f,
    val text: String = "",
    val author: String = "User",
    val createdTime: Long = System.currentTimeMillis(),
    val modifiedTime: Long = System.currentTimeMillis(),
    val isSelected: Boolean = false,
    val bounds: androidx.compose.ui.geometry.Rect? = null
)

data class AnnotationStyle(
    val color: Color = Color.Yellow,
    val strokeWidth: Float = 2f,
    val opacity: Float = 1f,
    val fontSize: Float = 14f,
    val fontFamily: String = "Roboto"
)

data class AnnotationState(
    val annotations: List<Annotation> = emptyList(),
    val currentType: AnnotationType = AnnotationType.HIGHLIGHT,
    val currentStyle: AnnotationStyle = AnnotationStyle(),
    val selectedAnnotationId: String? = null,
    val isDrawing: Boolean = false,
    val currentPoints: List<AnnotationPoint> = emptyList(),
    val undoStack: List<Annotation> = emptyList(),
    val redoStack: List<Annotation> = emptyList(),
    val pageNumber: Int = 0,
    val editingAnnotationId: String? = null
)