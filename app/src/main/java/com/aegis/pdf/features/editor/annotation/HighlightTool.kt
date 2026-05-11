package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

data class HighlightAnnotation(
    val id: Long, val pageNumber: Int, val rects: List<Rect>,
    val color: Color = Color.Yellow.copy(alpha = 0.5f), val opacity: Float = 0.5f
)

class HighlightTool {
    private var isDragging = false
    private var currentRect: Rect? = null
    private val rects = mutableListOf<Rect>()

    fun start(start: Offset) { isDragging = true; currentRect = Rect(start, start); rects.clear() }

    fun update(current: Offset) {
        if (!isDragging || currentRect == null) return
        val s = currentRect!!.topLeft
        currentRect = Rect(minOf(s.x, current.x), minOf(s.y, current.y), maxOf(s.x, current.x), maxOf(s.y, current.y))
        rects.add(currentRect!!)
    }

    fun end(): HighlightAnnotation? {
        isDragging = false
        return if (rects.isEmpty()) null else HighlightAnnotation(System.currentTimeMillis(), 0, rects.toList()).also { rects.clear() }
    }

    fun DrawScope.draw(a: HighlightAnnotation) {
        a.rects.forEach { drawRect(a.color, Offset(it.left, it.top), androidx.compose.ui.geometry.Size(it.width, it.height)) }
    }
}