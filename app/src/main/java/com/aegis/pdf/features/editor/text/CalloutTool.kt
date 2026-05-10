package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalloutTool(
    targetX: Float,
    targetY: Float,
    text: String,
    calloutColor: Color = Color(0xFFFFEB3B),
    modifier: Modifier = Modifier
) {
    val boxWidth = 200f
    val boxHeight = 80f
    val boxX = targetX
    val boxY = targetY - boxHeight - 20f

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Arrow line from box to target
            val arrowStart = Offset(boxX + boxWidth / 2, boxY + boxHeight)
            val arrowEnd = Offset(targetX, targetY)

            drawLine(
                color = Color.DarkGray,
                start = arrowStart,
                end = arrowEnd,
                strokeWidth = 2f
            )

            // Arrow triangle
            val arrowPath = Path().apply {
                moveTo(arrowEnd.x, arrowEnd.y)
                lineTo(arrowEnd.x - 8f, arrowEnd.y - 12f)
                lineTo(arrowEnd.x + 8f, arrowEnd.y - 12f)
                close()
            }
            drawPath(arrowPath, color = Color.DarkGray)

            // Callout box
            drawRoundRect(
                color = calloutColor,
                topLeft = Offset(boxX, boxY),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color.DarkGray,
                topLeft = Offset(boxX, boxY),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(2f)
            )
        }

        // Text inside callout
        Box(
            modifier = Modifier
                .offset(x = boxX.dp, y = boxY.dp)
                .size(width = boxWidth.dp, height = boxHeight.dp)
                .padding(8.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.Black,
                maxLines = 4
            )
        }
    }
}