package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class ArrowAnnotation(
    val id: Long, val pageNumber: Int, val start: Offset, val end: Offset,
    val color: Color = Color.Red, val strokeWidth: Float = 3f, val arrowSize: Float = 15f
)

class ArrowTool {
    private var startPoint: Offset? = null
    fun start(start: Offset) { startPoint = start }
    fun end(end: Offset): ArrowAnnotation? {
        val s = startPoint ?: return null; startPoint = null
        return ArrowAnnotation(System.currentTimeMillis(), 0, s, end)
    }
    fun DrawScope.draw(a: ArrowAnnotation) {
        drawLine(a.color, a.start, a.end, a.strokeWidth)
        val angle = atan2((a.end.y - a.start.y).toDouble(), (a.end.x - a.start.x).toDouble())
        val path = Path().apply {
            moveTo(a.end.x, a.end.y)
            lineTo((a.end.x - a.arrowSize * cos(angle - Math.PI/6)).toFloat(), (a.end.y - a.arrowSize * sin(angle - Math.PI/6)).toFloat())
            lineTo((a.end.x - a.arrowSize * cos(angle + Math.PI/6)).toFloat(), (a.end.y - a.arrowSize * sin(angle + Math.PI/6)).toFloat())
            close()
        }
        drawPath(path, a.color)
    }
}