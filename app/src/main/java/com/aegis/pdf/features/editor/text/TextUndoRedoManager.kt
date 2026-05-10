package com.aegis.pdf.features.editor.text

import javax.inject.Inject
import javax.inject.Singleton

sealed class TextEditAction {
    class AddText(val element: TextElement) : TextEditAction()
    class DeleteText(val element: TextElement) : TextEditAction()
    class MoveText(val elementId: Long, val oldX: Float, val oldY: Float, val newX: Float, val newY: Float) : TextEditAction()
    class ResizeText(val elementId: Long, val oldW: Float, val oldH: Float, val newW: Float, val newH: Float) : TextEditAction()
    class ChangeText(val elementId: Long, val oldText: String, val newText: String) : TextEditAction()
    class ChangeStyle(val elementId: Long, val oldStyle: TextStyleSnapshot, val newStyle: TextStyleSnapshot) : TextEditAction()
}

data class TextStyleSnapshot(
    val fontName: String,
    val fontSize: Float,
    val color: androidx.compose.ui.graphics.Color,
    val isBold: Boolean,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val alignment: TextAlignment
)

data class EditorSnapshot(
    val elements: List<TextElement>,
    val selectedElementId: Long?
)

@Singleton
class TextUndoRedoManager @Inject constructor() {

    private val undoStack = ArrayDeque<TextEditAction>(100)
    private val redoStack = ArrayDeque<TextEditAction>(100)
    private val maxStackSize = 100

    private var onUndoRedo: ((List<TextElement>) -> Unit)? = null

    fun setOnUndoRedoCallback(callback: (List<TextElement>) -> Unit) {
        onUndoRedo = callback
    }

    fun pushAction(action: TextEditAction) {
        undoStack.addLast(action)
        if (undoStack.size > maxStackSize) {
            undoStack.removeFirst()
        }
        redoStack.clear()
    }

    fun undo(currentElements: List<TextElement>): List<TextElement> {
        if (undoStack.isEmpty()) return currentElements

        val action = undoStack.removeLast()
        redoStack.addLast(action)

        val result = applyReverseAction(action, currentElements)
        onUndoRedo?.invoke(result)
        return result
    }

    fun redo(currentElements: List<TextElement>): List<TextElement> {
        if (redoStack.isEmpty()) return currentElements

        val action = redoStack.removeLast()
        undoStack.addLast(action)

        val result = applyAction(action, currentElements)
        onUndoRedo?.invoke(result)
        return result
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun applyAction(action: TextEditAction, elements: List<TextElement>): List<TextElement> {
        return when (action) {
            is TextEditAction.AddText -> elements + action.element
            is TextEditAction.DeleteText -> elements.filter { it.id != action.element.id }
            is TextEditAction.MoveText -> elements.map {
                if (it.id == action.elementId) it.copy(x = action.newX, y = action.newY) else it
            }
            is TextEditAction.ResizeText -> elements.map {
                if (it.id == action.elementId) it.copy(width = action.newW, height = action.newH) else it
            }
            is TextEditAction.ChangeText -> elements.map {
                if (it.id == action.elementId) it.copy(text = action.newText) else it
            }
            is TextEditAction.ChangeStyle -> elements.map {
                if (it.id == action.elementId) {
                    it.copy(
                        fontName = action.newStyle.fontName,
                        fontSize = action.newStyle.fontSize,
                        color = action.newStyle.color,
                        isBold = action.newStyle.isBold,
                        isItalic = action.newStyle.isItalic,
                        isUnderline = action.newStyle.isUnderline,
                        alignment = action.newStyle.alignment
                    )
                } else it
            }
        }
    }

    private fun applyReverseAction(action: TextEditAction, elements: List<TextElement>): List<TextElement> {
        return when (action) {
            is TextEditAction.AddText -> elements.filter { it.id != action.element.id }
            is TextEditAction.DeleteText -> elements + action.element
            is TextEditAction.MoveText -> elements.map {
                if (it.id == action.elementId) it.copy(x = action.oldX, y = action.oldY) else it
            }
            is TextEditAction.ResizeText -> elements.map {
                if (it.id == action.elementId) it.copy(width = action.oldW, height = action.oldH) else it
            }
            is TextEditAction.ChangeText -> elements.map {
                if (it.id == action.elementId) it.copy(text = action.oldText) else it
            }
            is TextEditAction.ChangeStyle -> elements.map {
                if (it.id == action.elementId) {
                    it.copy(
                        fontName = action.oldStyle.fontName,
                        fontSize = action.oldStyle.fontSize,
                        color = action.oldStyle.color,
                        isBold = action.oldStyle.isBold,
                        isItalic = action.oldStyle.isItalic,
                        isUnderline = action.oldStyle.isUnderline,
                        alignment = action.oldStyle.alignment
                    )
                } else it
            }
        }
    }
}