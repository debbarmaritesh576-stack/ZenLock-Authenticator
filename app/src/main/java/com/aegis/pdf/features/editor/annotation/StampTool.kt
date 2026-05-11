package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

enum class StampType { APPROVED, REJECTED, DRAFT, CONFIDENTIAL, FINAL, REVIEWED }

data class StampAnnotation(
    val id: Long, val pageNumber: Int, val position: Offset,
    val stampType: StampType, val color: Color = Color.Red, val size: Float = 120f
)

class StampTool {
    var currentType = StampType.APPROVED
    var currentColor = Color.Red
    var currentSize = 120f

    fun place(position: Offset) = StampAnnotation(System.currentTimeMillis(), 0, position, currentType, currentColor, currentSize)

    private fun getText(type: StampType) = when(type) {
        StampType.APPROVED -> "APPROVED"; StampType.REJECTED -> "REJECTED"; StampType.DRAFT -> "DRAFT"
        StampType.CONFIDENTIAL -> "CONFIDENTIAL"; StampType.FINAL -> "FINAL"; StampType.REVIEWED -> "REVIEWED"
    }

    fun DrawScope.draw(a: StampAnnotation) {
        val text = getText(a.stampType)
        drawRoundRect(a.color, Offset(a.position.x - a.size/2, a.position.y - 25f), Size(a.size, 50f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f), style = Stroke(4f))
        drawText(
            rememberTextMeasurer().measure(text, TextStyle(color = a.color, fontSize = 18.sp, fontWeight = FontWeight.Bold)),
            topLeft = Offset(a.position.x - a.size/2 + 20f, a.position.y - 14f)
        )
    }
}