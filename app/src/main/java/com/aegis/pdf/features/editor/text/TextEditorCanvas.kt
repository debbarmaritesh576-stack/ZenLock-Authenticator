package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun TextEditorCanvas(
    state: TextEditorState,
    onTextTap: (Long) -> Unit,
    onTextMove: (Long, Float, Float) -> Unit,
    onTextResize: (Long, Float, Float) -> Unit,
    onCanvasTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedElementId by remember { mutableStateOf<Long?>(null) }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.selectedTool) {
                    detectTapGestures { offset ->
                        val tappedElement = findElementAt(state.elements, offset)
                        if (tappedElement != null) {
                            onTextTap(tappedElement.id)
                        } else if (state.selectedTool != EditorTool.SELECT) {
                            onCanvasTap(offset.x, offset.y)
                        }
                    }
                }
                .pointerInput(state.selectedTool) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            draggedElementId = findElementAt(state.elements, offset)?.id
                        },
                        onDrag = { change, dragAmount ->
                            draggedElementId?.let { id ->
                                val element = state.elements.find { it.id == id }
                                if (element != null) {
                                    onTextMove(id, element.x + dragAmount.x, element.y + dragAmount.y)
                                }
                            }
                        },
                        onDragEnd = { draggedElementId = null }
                    )
                }
        ) {
            state.elements.forEach { element ->
                val textAlign = when (element.alignment) {
                    TextAlignment.LEFT -> TextAlign.Left
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.Right
                }

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = element.fontSize * 2
                        isAntiAlias = true
                        val style = when {
                            element.isBold && element.isItalic -> android.graphics.Typeface.BOLD_ITALIC
                            element.isBold -> android.graphics.Typeface.BOLD
                            element.isItalic -> android.graphics.Typeface.ITALIC
                            else -> android.graphics.Typeface.NORMAL
                        }
                        typeface = android.graphics.Typeface.create(element.fontName, style)
                        color = android.graphics.Color.argb(
                            (element.color.alpha * 255).toInt(),
                            (element.color.red * 255).toInt(),
                            (element.color.green * 255).toInt(),
                            (element.color.blue * 255).toInt()
                        )
                        if (element.isUnderline) flags = android.graphics.Paint.UNDERLINE_TEXT_FLAG
                    }

                    drawText(element.text, element.x, element.y + element.fontSize, paint)
                }

                // Selection border
                if (element.id == state.selectedElementId) {
                    drawRect(
                        color = Color.Blue.copy(alpha = 0.3f),
                        topLeft = Offset(element.x, element.y),
                        size = androidx.compose.ui.geometry.Size(element.width, element.height)
                    )
                }
            }
        }

        // Text editing overlay for selected element
        state.elements.find { it.id == state.selectedElementId }?.let { element ->
            BasicTextField(
                value = element.text,
                onValueChange = { /* handled in ViewModel */ },
                textStyle = TextStyle(
                    fontSize = element.fontSize.sp,
                    color = element.color,
                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (element.isUnderline) TextDecoration.Underline else TextDecoration.None,
                    textAlign = textAlign
                ),
                modifier = Modifier
                    .offset(x = element.x.dp, y = element.y.dp)
                    .size(width = element.width.dp, height = element.height.dp)
            )
        }
    }
}

private fun findElementAt(elements: List<TextElement>, point: Offset): TextElement? {
    return elements.findLast { element ->
        point.x in element.x..(element.x + element.width) &&
        point.y in element.y..(element.y + element.height)
    }
}