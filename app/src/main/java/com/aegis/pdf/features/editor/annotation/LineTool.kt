package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope

enum class LineStyle { SOLID, DASHED, DOTTED }

data class LineAnnotation(
    val id: Long, val pageNumber: Int, val start: Offset, val end: Offset,
    val color: Color = Color.Black, val width: Float = 3f, val style: LineStyle = LineStyle.SOLID
)

class LineTool {
    private var startPoint: Offset? = null
    var currentColor = Color.Black
    var currentWidth = 3f
    var currentStyle = LineStyle.SOLID

    fun start(start: Offset) { startPoint = start }
    fun end(end: Offset): LineAnnotation? {
        val s = startPoint ?: return null; startPoint = null
        return LineAnnotation(System.currentTimeMillis(), 0, s, end, currentColor, currentWidth, currentStyle)
    }
    fun DrawScope.draw(a: LineAnnotation) {
        val pe = when (a.style) {
            LineStyle.DASHED -> PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            LineStyle.DOTTED -> PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            else -> null
        }
        drawLine(a.color, a.start, a.end, a.width, pathEffect = pe)
    }
}