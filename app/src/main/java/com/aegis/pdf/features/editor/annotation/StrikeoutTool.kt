package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope

data class StrikeoutAnnotation(
    val id: Long, val pageNumber: Int, val lines: List<LineSegment>,
    val color: Color = Color.Red, val thickness: Float = 2f
)

class StrikeoutTool {
    private var isDragging = false
    private var startPoint: Offset? = null
    private val lines = mutableListOf<LineSegment>()

    fun start(start: Offset) { isDragging = true; startPoint = start; lines.clear() }
    fun update(current: Offset) {
        if (!isDragging || startPoint == null) return
        lines.clear(); lines.add(LineSegment(startPoint!!, current))
    }
    fun end(): StrikeoutAnnotation? {
        isDragging = false; startPoint = null
        return if (lines.isEmpty()) null else StrikeoutAnnotation(System.currentTimeMillis(), 0, lines.toList()).also { lines.clear() }
    }
    fun DrawScope.draw(a: StrikeoutAnnotation) {
        a.lines.forEach { drawLine(a.color, it.start, it.end, a.thickness, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)) }
    }
}