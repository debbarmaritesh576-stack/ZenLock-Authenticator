package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParagraphEditor(
    element: TextElement?,
    onIndentChange: (Float) -> Unit,
    onMarginChange: (Float, Float, Float, Float) -> Unit,
    onBulletToggle: (Boolean) -> Unit,
    onNumberingToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (element == null) return

    var leftIndent by remember { mutableStateOf(0f) }
    var rightIndent by remember { mutableStateOf(0f) }
    var topMargin by remember { mutableStateOf(0f) }
    var bottomMargin by remember { mutableStateOf(0f) }
    var hasBullet by remember { mutableStateOf(false) }
    var hasNumbering by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text("Paragraph Settings", style = MaterialTheme.typography.titleSmall)

        Spacer(modifier = Modifier.height(12.dp))

        // Indentation
        Text("Left Indent: ${leftIndent.toInt()}pt", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = leftIndent,
            onValueChange = { 
                leftIndent = it
                onIndentChange(it)
            },
            valueRange = 0f..100f
        )

        Text("Right Indent: ${rightIndent.toInt()}pt",