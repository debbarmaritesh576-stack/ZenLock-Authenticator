package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

@Composable
fun TextBoxResizer(
    isSelected: Boolean,
    elementWidth: Float,
    elementHeight: Float,
    onResize: (newWidth: Float, newHeight: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isSelected) return

    var isDragging by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Corner handles
        Box(
            modifier = Modifier
                .size(14.dp)
                .offset(x = (elementWidth - 7).dp, y = (elementHeight - 7).dp)
                .background(Color.Blue, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWidth = (elementWidth + dragAmount.x).coerceAtLeast(50f)
                        val newHeight = (elementHeight + dragAmount.y).coerceAtLeast(20f)
                        onResize(newWidth, newHeight)
                    }
                }
        )

        // Edge handles
        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(x = (elementWidth - 5).dp, y = (elementHeight / 2 - 5).dp)
                .background(Color.Blue, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWidth = (elementWidth + dragAmount.x).coerceAtLeast(50f)
                        onResize(newWidth, elementHeight)
                    }
                }
        )

        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(x = (elementWidth / 2 - 5).dp, y = (elementHeight - 5).dp)
                .background(Color.Blue, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newHeight = (elementHeight + dragAmount.y).coerceAtLeast(20f)
                        onResize(elementWidth, newHeight)
                    }
                }
        )
    }
}