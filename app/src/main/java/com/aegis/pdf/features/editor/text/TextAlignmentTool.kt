package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextAlignmentTool(
    alignment: TextAlignment,
    onAlignmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (alignment) {
        TextAlignment.LEFT -> "⇤"
        TextAlignment.CENTER -> "⇔"
        TextAlignment.RIGHT -> "⇥"
    }

    val label = when (alignment) {
        TextAlignment.LEFT -> "Align Left"
        TextAlignment.CENTER -> "Align Center"
        TextAlignment.RIGHT -> "Align Right"
    }

    FilledTonalButton(
        onClick = onAlignmentClick,
        modifier = modifier.width(44.dp)
    ) {
        Text(icon, fontSize = 18.sp)
    }
}