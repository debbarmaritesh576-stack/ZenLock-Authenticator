package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

data class UnderlineAnnotation(
    val id: Long, val pageNumber: Int, val segments: List<LineSegment>,
    val color: Color = Color.Red, val thickness: Float = 3f
)
data class LineSegment(val start: Offset, val end: Offset)

class UnderlineTool {
    private var isDragging = false
    private var lastPoint: Offset? = null
    private val segments = mutableListOf<LineSegment>()

    fun start(start: Offset) { isDragging = true; lastPoint = start; segments.clear() }
    fun update(current: Offset) {
        if (!isDragging || lastPoint == null) return
        segments.add(LineSegment(lastPoint!!, current)); lastPoint = current
    }
    fun end(): UnderlineAnnotation? {
        isDragging = false; lastPoint = null
        return if (segments.isEmpty()) null else UnderlineAnnotation(System.currentTimeMillis(), 0, segments.toList()).also { segments.clear() }
    }
    fun DrawScope.draw(a: UnderlineAnnotation) {
        a.segments.forEach { drawLine(a.color, it.start, it.end, a.thickness) }
    }
}