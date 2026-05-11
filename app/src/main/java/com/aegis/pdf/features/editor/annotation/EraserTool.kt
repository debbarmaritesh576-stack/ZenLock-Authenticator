package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

class EraserTool {
    private var isErasing = false
    private var size: Float = 20f
    private val erasedAreas = mutableListOf<Rect>()

    fun setSize(s: Float) { size = s.coerceIn(5f, 50f) }
    fun getSize() = size

    fun start(point: Offset) {
        isErasing = true; erasedAreas.clear()
        erasedAreas.add(Rect(point.x - size/2, point.y - size/2, point.x + size/2, point.y + size/2))
    }
    fun update(point: Offset) {
        if (!isErasing) return
        erasedAreas.add(Rect(point.x - size/2, point.y - size/2, point.x + size/2, point.y + size/2))
    }
    fun end() { isErasing = false }
    fun isErased(point: Offset) = erasedAreas.any { point.x in it.left..it.right && point.y in it.top..it.bottom }
    fun isAnnotationErased(bounds: Rect) = erasedAreas.any { it.overlaps(bounds) }
    fun DrawScope.drawIndicator(position: Offset) {
        if (!isErasing) return
        drawCircle(Color.Gray.copy(alpha = 0.3f), size/2, position, style = Stroke(2f))
    }
    fun clear() { erasedAreas.clear(); isErasing = false }
}