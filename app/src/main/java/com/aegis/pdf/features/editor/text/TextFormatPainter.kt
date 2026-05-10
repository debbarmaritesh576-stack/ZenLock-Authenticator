package com.aegis.pdf.features.editor.text

import androidx.compose.ui.graphics.Color
import javax.inject.Inject
import javax.inject.Singleton

data class TextFormat(
    val fontName: String,
    val fontSize: Float,
    val color: Color,
    val isBold: Boolean,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val alignment: TextAlignment,
    val lineSpacing: Float
)

@Singleton
class TextFormatPainter @Inject constructor() {

    private var copiedFormat: TextFormat? = null

    fun copyFormat(element: TextElement): TextFormat {
        val format = TextFormat(
            fontName = element.fontName,
            fontSize = element.fontSize,
            color = element.color,
            isBold = element.isBold,
            isItalic = element.isItalic,
            isUnderline = element.isUnderline,
            alignment = element.alignment,
            lineSpacing = 1.0f
        )
        copiedFormat = format
        return format
    }

    fun pasteFormat(element: TextElement): TextElement {
        val format = copiedFormat ?: return element
        return element.copy(
            fontName = format.fontName,
            fontSize = format.fontSize,
            color = format.color,
            isBold = format.isBold,
            isItalic = format.isItalic,
            isUnderline = format.isUnderline,
            alignment = format.alignment
        )
    }

    fun hasCopiedFormat(): Boolean = copiedFormat != null

    fun applyFormat(elements: List<TextElement>, selectedIds: List<Long>, format: TextFormat): List<TextElement> {
        return elements.map { element ->
            if (element.id in selectedIds) {
                element.copy(
                    fontName = format.fontName,
                    fontSize = format.fontSize,
                    color = format.color,
                    isBold = format.isBold,
                    isItalic = format.isItalic,
                    isUnderline = format.isUnderline,
                    alignment = format.alignment
                )
            } else element
        }
    }

    fun clearFormat(element: TextElement): TextElement {
        return element.copy(
            isBold = false,
            isItalic = false,
            isUnderline = false,
            color = Color.Black,
            fontSize = 14f,
            fontName = "Helvetica"
        )
    }
}