package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TypewriterTool(
    x: Float,
    y: Float,
    text: String,
    fontSize: Float,
    color: Color,
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean,
    alignment: TextAlignment,
    onTextChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingText by remember { mutableStateOf(text) }
    val textAlign = when (alignment) {
        TextAlignment.LEFT -> androidx.compose.ui.text.style.TextAlign.Left
        TextAlignment.CENTER -> androidx.compose.ui.text.style.TextAlign.Center
        TextAlignment.RIGHT -> androidx.compose.ui.text.style.TextAlign.Right
    }

    Box(
        modifier = modifier
            .offset(x = x.dp, y = y.dp)
            .background(Color.Transparent)
    ) {
        Column {
            // Cursor indicator
            Canvas(modifier = Modifier.size(24.dp, 4.dp)) {
                drawCircle(
                    color = Color.Red,
                    radius = 3f,
                    center = Offset(12f, 2f)
                )
            }

            // Text input
            BasicTextField(
                value = editingText,
                onValueChange = { 
                    editingText = it
                    onTextChange(it)
                },
                textStyle = TextStyle(
                    fontSize = fontSize.sp,
                    color = color,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None,
                    textAlign = textAlign
                ),
                modifier = Modifier
                    .widthIn(min = 100.dp, max = 400.dp)
                    .padding(4.dp),
                singleLine = false,
                decorationBox = { innerTextField ->
                    if (editingText.isEmpty()) {
                        Text(
                            "Type here...",
                            style = TextStyle(
                                fontSize = fontSize.sp,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}