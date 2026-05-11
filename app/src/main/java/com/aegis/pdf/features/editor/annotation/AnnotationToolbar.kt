package com.aegis.pdf.features.editor.annotation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.StrikethroughS
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class AnnotationToolType {
    HIGHLIGHT,
    UNDERLINE,
    STRIKEOUT,
    SQUIGGLY,
    FREEHAND,
    ARROW,
    LINE,
    RECTANGLE,
    CIRCLE,
    POLYGON,
    STAMP,
    STICKY_NOTE,
    SELECT
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),

            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // =========================
            // TEXT MARKUP
            // =========================

            IconToolChip(
                icon = Icons.Default.Highlight,
                label = "Highlight",
                selected = selectedTool == AnnotationToolType.HIGHLIGHT
            ) {
                onToolSelected(AnnotationToolType.HIGHLIGHT)
            }

            IconToolChip(
                icon = Icons.Default.FormatUnderlined,
                label = "Underline",
                selected = selectedTool == AnnotationToolType.UNDERLINE
            ) {
                onToolSelected(AnnotationToolType.UNDERLINE)
            }

            IconToolChip(
                icon = Icons.Default.StrikethroughS,
                label = "Strike",
                selected = selectedTool == AnnotationToolType.STRIKEOUT
            ) {
                onToolSelected(AnnotationToolType.STRIKEOUT)
            }

            IconToolChip(
                icon = Icons.Default.Waves,
                label = "Squiggly",
                selected = selectedTool == AnnotationToolType.SQUIGGLY
            ) {
                onToolSelected(AnnotationToolType.SQUIGGLY)
            }

            VerticalDivider(
                modifier = Modifier.height(48.dp)
            )

            // =========================
            // DRAWING
            // =========================

            IconToolChip(
                icon = Icons.Default.Edit,
                label = "Pen",
                selected = selectedTool == AnnotationToolType.FREEHAND
            ) {
                onToolSelected(AnnotationToolType.FREEHAND)
            }

            VerticalDivider(
                modifier = Modifier.height(48.dp)
            )

            // =========================
            // SHAPES
            // =========================

            IconToolChip(
                icon = Icons.Default.ArrowForward,
                label = "Arrow",
                selected = selectedTool == AnnotationToolType.ARROW
            ) {
                onToolSelected(AnnotationToolType.ARROW)
            }

            IconToolChip(
                icon = Icons.Default.Remove,
                label = "Line",
                selected = selectedTool == AnnotationToolType.LINE
            ) {
                onToolSelected(AnnotationToolType.LINE)
            }

            IconToolChip(
                icon = Icons.Default.CheckBoxOutlineBlank,
                label = "Rectangle",
                selected = selectedTool == AnnotationToolType.RECTANGLE
            ) {
                onToolSelected(AnnotationToolType.RECTANGLE)
            }

            IconToolChip(
                icon = Icons.Default.Circle,
                label = "Circle",
                selected = selectedTool == AnnotationToolType.CIRCLE
            ) {
                onToolSelected(AnnotationToolType.CIRCLE)
            }

            IconToolChip(
                icon = Icons.Default.ChangeHistory,
                label = "Polygon",
                selected = selectedTool == AnnotationToolType.POLYGON
            ) {
                onToolSelected(AnnotationToolType.POLYGON)
            }

            VerticalDivider(
                modifier = Modifier.height(48.dp)
            )

            // =========================
            // STAMP & NOTE
            // =========================

            IconToolChip(
                icon = Icons.Default.CheckCircle,
                label = "Stamp",
                selected = selectedTool == AnnotationToolType.STAMP
            ) {
                onToolSelected(AnnotationToolType.STAMP)
            }

            IconToolChip(
                icon = Icons.Default.Note,
                label = "Note",
                selected = selectedTool == AnnotationToolType.STICKY_NOTE
            ) {
                onToolSelected(AnnotationToolType.STICKY_NOTE)
            }

            VerticalDivider(
                modifier = Modifier.height(48.dp)
            )

            // =========================
            // SELECT TOOL
            // =========================

            IconToolChip(
                icon = Icons.Default.SelectAll,
                label = "Select",
                selected = selectedTool == AnnotationToolType.SELECT
            ) {
                onToolSelected(AnnotationToolType.SELECT)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // =========================
            // COLOR PICKER
            // =========================

            Surface(
                onClick = onColorClick,
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                color = currentColor,
                border = ButtonDefaults.outlinedButtonBorder
            ) {}

            Spacer(modifier = Modifier.width(4.dp))

            // =========================
            // SIZE BUTTON
            // =========================

            FilledTonalButton(
                onClick = onSizeClick
            ) {
                Text("${currentSize.toInt()} px")
            }
        }
    }
}

@Composable
private fun IconToolChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    FilterChip(
        selected = selected,
        onClick = onClick,

        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
        },

        label = {
            Text(text = label)
        }
    )
}