package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

data class StickyNoteAnnotation(
    val id: Long, val pageNumber: Int, val position: Offset, val text: String,
    val color: Color = Color(0xFFFFF176), val size: Size = Size(200f, 150f), val isExpanded: Boolean = false
)

class StickyNoteTool {
    var currentColor = Color(0xFFFFF176)
    var currentSize = Size(200f, 150f)

    fun place(position: Offset, text: String = "") = StickyNoteAnnotation(System.currentTimeMillis(), 0, position, text, currentColor, currentSize)
    fun toggle(a: StickyNoteAnnotation) = a.copy(isExpanded = !a.isExpanded)

    fun DrawScope.draw(a: StickyNoteAnnotation) {
        val displaySize = if (a.isExpanded) a.size else Size(40f, 40f)
        val path = Path().apply { addRect(androidx.compose.ui.geometry.Rect(a.position.x, a.position.y, a.position.x + displaySize.width, a.position.y + displaySize.height)) }
        drawPath(path, a.color, style = Fill)
        drawPath(path, Color.Gray.copy(alpha = 0.3f), style = Stroke(1f))
        if (a.isExpanded && a.text.isNotEmpty()) {
            drawText(
                rememberTextMeasurer().measure(a.text, TextStyle(color = Color.DarkGray, fontSize = 12.sp)),
                topLeft = Offset(a.position.x + 10f, a.position.y + 10f)
            )
        }
    }
}