package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

data class FreehandAnnotation(
    val id: Long, val pageNumber: Int, val path: Path,
    val color: Color = Color.Blue, val strokeWidth: Float = 4f
)

class FreehandPen {
    private val currentPath = Path()
    private var isDrawing = false

    fun start(start: Offset) { isDrawing = true; currentPath.reset(); currentPath.moveTo(start.x, start.y) }
    fun update(current: Offset) { if (isDrawing) currentPath.lineTo(current.x, current.y) }
    fun end(): FreehandAnnotation? {
        isDrawing = false
        return if (currentPath.isEmpty) null else FreehandAnnotation(System.currentTimeMillis(), 0, Path().apply { addPath(currentPath) }).also { currentPath.reset() }
    }
    fun DrawScope.draw(a: FreehandAnnotation) {
        drawPath(a.path, a.color, style = Stroke(a.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    val currentDrawPath: Path get() = currentPath
}