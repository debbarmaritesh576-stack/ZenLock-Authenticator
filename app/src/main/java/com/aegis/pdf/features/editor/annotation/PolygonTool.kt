package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

data class PolygonAnnotation(
    val id: Long, val pageNumber: Int, val points: List<Offset>,
    val fillColor: Color = Color.Transparent, val strokeColor: Color = Color.Red, val strokeWidth: Float = 3f
)

class PolygonTool {
    private val points = mutableListOf<Offset>()
    private var active = false
    var currentFill = Color.Transparent
    var currentStroke = Color.Red
    var currentWidth = 3f

    fun start() { active = true; points.clear() }
    fun addPoint(point: Offset) { if (active) points.add(point) }
    fun finish(): PolygonAnnotation? {
        active = false
        return if (points.size < 3) null else PolygonAnnotation(System.currentTimeMillis(), 0, points.toList(), currentFill, currentStroke, currentWidth).also { points.clear() }
    }
    fun cancel() { active = false; points.clear() }
    fun getPoints(): List<Offset> = points.toList()
    fun isActive(): Boolean = active

    fun DrawScope.draw(a: PolygonAnnotation) {
        if (a.points.isEmpty()) return
        val path = Path().apply {
            moveTo(a.points.first().x, a.points.first().y)
            for (i in 1 until a.points.size) lineTo(a.points[i].x, a.points[i].y)
            close()
        }
        if (a.fillColor != Color.Transparent) drawPath(path, a.fillColor, style = Fill)
        drawPath(path, a.strokeColor, style = Stroke(a.strokeWidth))
    }
}