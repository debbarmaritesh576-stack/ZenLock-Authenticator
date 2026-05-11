package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sqrt

data class CircleAnnotation(
    val id: Long, val pageNumber: Int, val center: Offset, val radius: Float,
    val fillColor: Color = Color.Transparent, val strokeColor: Color = Color.Red, val strokeWidth: Float = 3f
)

class CircleTool {
    private var centerPoint: Offset? = null
    var currentFill = Color.Transparent
    var currentStroke = Color.Red
    var currentWidth = 3f

    fun start(center: Offset) { centerPoint = center }
    fun end(edge: Offset): CircleAnnotation? {
        val c = centerPoint ?: return null; centerPoint = null
        val r = sqrt(((edge.x-c.x)*(edge.x-c.x) + (edge.y-c.y)*(edge.y-c.y)).toDouble()).toFloat()
        return CircleAnnotation(System.currentTimeMillis(), 0, c, r, currentFill, currentStroke, currentWidth)
    }
    fun DrawScope.draw(a: CircleAnnotation) {
        val bounds = Rect(a.center.x-a.radius, a.center.y-a.radius, a.center.x+a.radius, a.center.y+a.radius)
        val path = Path().apply { addOval(bounds) }
        if (a.fillColor != Color.Transparent) drawPath(path, a.fillColor, style = Fill)
        drawPath(path, a.strokeColor, style = Stroke(a.strokeWidth))
    }
}