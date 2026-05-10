package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextEditorToolbar(
    currentTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    currentFont: String,
    currentSize: Float,
    currentColor: Color,
    onFontClick: () -> Unit,
    onSizeClick: () -> Unit,
    onColorClick: () -> Unit,
    bold: Boolean,
    italic: Boolean,
    underline: Boolean,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    alignment: TextAlignment,
    onAlignmentClick: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Tool selection
            EditorTool.values().forEach { tool ->
                FilterChip(
                    selected = currentTool == tool,
                    onClick = { onToolSelected(tool) },
                    label = { Text(tool.name, fontSize = 11.sp) }
                )
            }

            VerticalDivider(modifier = Modifier.height(32.dp))

            // Font name button
            TextButton(onClick = onFontClick) {
                Text(currentFont, fontSize = 13.sp, maxLines = 1)
            }

            // Font size button
            TextButton(onClick = onSizeClick) {
                Text("${currentSize.toInt()}", fontSize = 13.sp)
            }

            // Color indicator
            Surface(
                onClick = onColorClick,
                modifier = Modifier.size(28.dp),
                shape = MaterialTheme.shapes.small,
                color = currentColor,
                border = ButtonDefaults.outlinedButtonBorder
            ) {}

            VerticalDivider(modifier = Modifier.height(32.dp))

            // Bold, Italic, Underline
            BoldItalicUnderlineTool(
                bold = bold,
                italic = italic,
                underline = underline,
                onBoldClick = onBoldClick,
                onItalicClick = onItalicClick,
                onUnderlineClick = onUnderlineClick
            )

            // Alignment
            TextAlignmentTool(
                alignment = alignment,
                onAlignmentClick = onAlignmentClick
            )
        }
    }
}