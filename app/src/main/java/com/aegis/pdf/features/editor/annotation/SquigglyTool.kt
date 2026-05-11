package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

data class SquigglyAnnotation(
    val id: Long, val pageNumber: Int, val path: Path,
    val color: Color = Color(0xFF008000), val thickness: Float = 2f
)

class SquigglyTool {
    private var isDragging = false
    private val points = mutableListOf<Offset>()

    fun start(start: Offset) { isDragging = true; points.clear(); points.add(start) }
    fun update(current: Offset) { if (isDragging) points.add(current) }
    fun end(): SquigglyAnnotation? {
        isDragging = false
        if (points.size < 2) return null
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            var i = 1
            while (i < points.size - 1) {
                val mx = (points[i].x + points[i+1].x) / 2
                val my = (points[i].y + points[i+1].y) / 2
                quadTo(points[i].x, points[i].y, mx, my); i += 2
            }
            if (i < points.size) lineTo(points.last().x, points.last().y)
        }
        return SquigglyAnnotation(System.currentTimeMillis(), 0, path).also { points.clear() }
    }
    fun DrawScope.draw(a: SquigglyAnnotation) {
        drawPath(a.path, a.color, style = Stroke(a.thickness, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)))
    }
}