package com.aegis.pdf.features.editor.text

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class TextElement(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val fontSize: Float,
    val fontName: String,
    val color: Color,
    val isBold: Boolean,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val alignment: TextAlignment,
    val rotation: Float = 0f
)

enum class TextAlignment { LEFT, CENTER, RIGHT }
enum class EditorTool { SELECT, TYPEWRITER, CALLOUT, TEXT_BOX }

data class TextEditorState(
    val elements: List<TextElement> = emptyList(),
    val selectedElementId: Long? = null,
    val selectedTool: EditorTool = EditorTool.SELECT,
    val currentFont: String = "Helvetica",
    val currentFontSize: Float = 14f,
    val currentColor: Color = Color.Black,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val alignment: TextAlignment = TextAlignment.LEFT,
    val showFontPicker: Boolean = false,
    val showSizeSlider: Boolean = false,
    val showColorPicker: Boolean = false
)

@HiltViewModel
class TextEditorViewModel @Inject constructor(
    private val undoRedoManager: TextUndoRedoManager
) : ViewModel() {

    private val _state = MutableStateFlow(TextEditorState())
    val state: StateFlow<TextEditorState> = _state

    private var nextId = 0L

    fun selectTool(tool: EditorTool) {
        _state.value = _state.value.copy(selectedTool = tool)
    }

    fun addTextBox(x: Float, y: Float) {
        val element = TextElement(
            id = nextId++,
            text = "Enter text",
            x = x,
            y = y,
            width = 200f,
            height = 50f,
            fontSize = _state.value.currentFontSize,
            fontName = _state.value.currentFont,
            color = _state.value.currentColor,
            isBold = _state.value.isBold,
            isItalic = _state.value.isItalic,
            isUnderline = _state.value.isUnderline,
            alignment = _state.value.alignment
        )
        undoRedoManager.pushAction(AddTextAction(element))
        _state.value = _state.value.copy(
            elements = _state.value.elements + element,
            selectedElementId = element.id
        )
    }

    fun selectText(id: Long) {
        _state.value = _state.value.copy(selectedElementId = id)
    }

    fun moveText(id: Long, x: Float, y: Float) {
        val elements = _state.value.elements.map {
            if (it.id == id) it.copy(x = x, y = y) else it
        }
        _state.value = _state.value.copy(elements = elements)
    }

    fun resizeText(id: Long, w: Float, h: Float) {
        val elements = _state.value.elements.map {
            if (it.id == id) it.copy(width = w, height = h) else it
        }
        _state.value = _state.value.copy(elements = elements)
    }

    fun updateText(id: Long, newText: String) {
        val elements = _state.value.elements.map {
            if (it.id == id) it.copy(text = newText) else it
        }
        _state.value = _state.value.copy(elements = elements)
    }

    fun deleteSelected() {
        val selected = _state.value.selectedElementId ?: return
        val elements = _state.value.elements.filter { it.id != selected }
        _state.value = _state.value.copy(
            elements = elements,
            selectedElementId = null
        )
    }

    fun toggleBold() {
        _state.value = _state.value.copy(isBold = !_state.value.isBold)
    }

    fun toggleItalic() {
        _state.value = _state.value.copy(isItalic = !_state.value.isItalic)
    }

    fun toggleUnderline() {
        _state.value = _state.value.copy(isUnderline = !_state.value.isUnderline)
    }

    fun cycleAlignment() {
        val next = when (_state.value.alignment) {
            TextAlignment.LEFT -> TextAlignment.CENTER
            TextAlignment.CENTER -> TextAlignment.RIGHT
            TextAlignment.RIGHT -> TextAlignment.LEFT
        }
        _state.value = _state.value.copy(alignment = next)
    }

    fun setFontSize(size: Float) {
        _state.value = _state.value.copy(currentFontSize = size)
    }

    fun setColor(color: Color) {
        _state.value = _state.value.copy(currentColor = color)
    }

    fun selectFont(font: String) {
        _state.value = _state.value.copy(currentFont = font)
    }

    fun showFontPicker() { _state.value = _state.value.copy(showFontPicker = true) }
    fun hideFontPicker() { _state.value = _state.value.copy(showFontPicker = false) }
    fun showSizeSlider() { _state.value = _state.value.copy(showSizeSlider = true) }
    fun hideSizeSlider() { _state.value = _state.value.copy(showSizeSlider = false) }
    fun showColorPicker() { _state.value = _state.value.copy(showColorPicker = true) }
    fun hideColorPicker() { _state.value = _state.value.copy(showColorPicker = false) }

    fun undo() { undoRedoManager.undo() }
    fun redo() { undoRedoManager.redo() }
}