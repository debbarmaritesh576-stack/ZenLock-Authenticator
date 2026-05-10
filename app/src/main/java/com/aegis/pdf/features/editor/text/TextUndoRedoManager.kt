package com.aegis.pdf.features.editor.text

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.max
import kotlin.math.min

class TextSelectionManager {

    private var selectedElementId: Long? = null
    private var selectionStart: Int = 0
    private var selectionEnd: Int = 0
    private var isSelecting = false

    fun startSelection(elementId: Long, position: Int) {
        selectedElementId = elementId
        selectionStart = position
        selectionEnd = position
        isSelecting = true
    }

    fun updateSelection(position: Int) {
        if (!isSelecting) return
        selectionEnd = position
    }

    fun endSelection(): TextSelection? {
        isSelecting = false
        if (selectedElementId == null) return null
        
        val start = min(selectionStart, selectionEnd)
        val end = max(selectionStart, selectionEnd)
        
        if (start == end) return null
        
        return TextSelection(
            elementId = selectedElementId!!,
            startIndex = start,
            endIndex = end
        )
    }

    fun clearSelection() {
        selectedElementId = null
        selectionStart = 0
        selectionEnd = 0
        isSelecting = false
    }

    fun isTouchOnSelectionHandle(elementId: Long, touchPoint: Offset, bounds: Rect): Boolean {
        val handleRadius = 30f
        val leftHandle = Offset(bounds.left, bounds.bottom)
        val rightHandle = Offset(bounds.right, bounds.bottom)
        
        return (touchPoint - leftHandle).getDistance() < handleRadius ||
               (touchPoint - rightHandle).getDistance() < handleRadius
    }

    data class TextSelection(
        val elementId: Long,
        val startIndex: Int,
        val endIndex: Int
    )
}