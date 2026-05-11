package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

data class RectangleAnnotation(
    val id: Long, val pageNumber: Int, val bounds: Rect,
    val fillColor: Color = Color.Transparent, val strokeColor: Color = Color.Red, val strokeWidth: Float = 3f, val cornerRadius: Float = 0f
)

class RectangleTool {
    private var startPoint: Offset? = null
    var currentFill = Color.Transparent
    var currentStroke = Color.Red
    var currentWidth = 3f
    var currentRadius = 0f

    fun start(start: Offset) { startPoint = start }
    fun end(end: Offset): RectangleAnnotation? {
        val s = startPoint ?: return null; startPoint = null
        return RectangleAnnotation(System.currentTimeMillis(), 0,
            Rect(minOf(s.x, end.x), minOf(s.y, end.y), maxOf(s.x, end.x), maxOf(s.y, end.y)),
            currentFill, currentStroke, currentWidth, currentRadius)
    }
    fun DrawScope.draw(a: RectangleAnnotation) {
        val path = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(a.bounds, a.cornerRadius, a.cornerRadius)) }
        if (a.fillColor != Color.Transparent) drawPath(path, a.fillColor, style = Fill)
        drawPath(path, a.strokeColor, style = Stroke(a.strokeWidth))
    }
}