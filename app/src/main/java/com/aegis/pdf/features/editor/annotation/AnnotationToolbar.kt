package com.aegis.pdf.features.editor.annotation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AnnotationToolType {
    HIGHLIGHT, UNDERLINE, STRIKEOUT, SQUIGGLY,
    FREEHAND, ARROW, LINE, RECTANGLE, CIRCLE, POLYGON,
    STAMP, STICKY_NOTE, SELECT
}

@Composable
fun AnnotationToolbar(
    selectedTool: AnnotationToolType,
    currentColor: Color,
    currentSize: Float,
    onToolSelected: (AnnotationToolType) -> Unit,
    onColorClick: () -> Unit,
    onSizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(tonalElevation = 3.dp, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Text Markup
            ToolChip("HL", selectedTool == AnnotationToolType.HIGHLIGHT) {
                onToolSelected(AnnotationToolType.HIGHLIGHT)
            }
            ToolChip("UL", selectedTool == AnnotationToolType.UNDERLINE) {
                onToolSelected(AnnotationToolType.UNDERLINE)
            }
            ToolChip("SO", selectedTool == AnnotationToolType.STRIKEOUT) {
                onToolSelected(AnnotationToolType.STRIKEOUT)
            }
            ToolChip("SQ", selectedTool == AnnotationToolType.SQUIGGLY) {
                onToolSelected(AnnotationToolType.SQUIGGLY)
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Drawing
            ToolChip("✏️", selectedTool == AnnotationToolType.FREEHAND) {
                onToolSelected(AnnotationToolType.FREEHAND)
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Lines & Shapes
            ToolChip("→", selectedTool == AnnotationToolType.ARROW) {
                onToolSelected(AnnotationToolType.ARROW)
            }
            ToolChip("—", selectedTool == AnnotationToolType.LINE) {
                onToolSelected(AnnotationToolType.LINE)
            }
            ToolChip("□", selectedTool == AnnotationToolType.RECTANGLE) {
                onToolSelected(AnnotationToolType.RECTANGLE)
            }
            ToolChip("○", selectedTool == AnnotationToolType.CIRCLE) {
                onToolSelected(AnnotationToolType.CIRCLE)
            }
            ToolChip("⬠", selectedTool == AnnotationToolType.POLYGON) {
                onToolSelected(AnnotationToolType.POLYGON)
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Stamp & Notes
            ToolChip("✓", selectedTool == AnnotationToolType.STAMP) {
                onToolSelected(AnnotationToolType.STAMP)
            }
            ToolChip("📝", selectedTool == AnnotationToolType.STICKY_NOTE) {
                onToolSelected(AnnotationToolType.STICKY_NOTE)
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Select
            ToolChip("⬚", selectedTool == AnnotationToolType.SELECT) {
                onToolSelected(AnnotationToolType.SELECT)
            }

            Spacer(Modifier.width(8.dp))

            // Color picker
            Surface(
                onClick = onColorClick,
                modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.small,
                color = currentColor,
                border = ButtonDefaults.outlinedButtonBorder
            ) {}

            Spacer(Modifier.width(4.dp))

            // Size
            TextButton(onClick = onSizeClick) {
                Text("${currentSize.toInt()}px")
            }
        }
    }
}

@Composable
private fun ToolChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}