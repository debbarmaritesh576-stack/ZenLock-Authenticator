package com.aegis.pdf.features.annotation.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.features.annotation.viewmodel.AnnotationViewModel
import com.aegis.pdf.features.annotation.model.AnnotationType
import com.aegis.pdf.features.annotation.model.AnnotationStyle

@Composable
fun AnnotationToolbar(
    viewModel: AnnotationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                icon = Icons.Default.MarkunreadMailbox,
                label = "Highlight",
                isSelected = state.currentType == AnnotationType.HIGHLIGHT,
                color = Color.Yellow,
                onClick = { viewModel.setCurrentAnnotationType(AnnotationType.HIGHLIGHT) }
            )

            ToolbarButton(
                icon = Icons.Default.TextFields,
                label = "Underline",
                isSelected = state.currentType == AnnotationType.UNDERLINE,
                onClick = { viewModel.setCurrentAnnotationType(AnnotationType.UNDERLINE) }
            )

            ToolbarButton(
                icon = Icons.Default.StrikethroughS,
                label = "Strikeout",
                isSelected = state.currentType == AnnotationType.STRIKEOUT,
                onClick = { viewModel.setCurrentAnnotationType(AnnotationType.STRIKEOUT) }
            )

            ToolbarButton(
                icon = Icons.Default.Edit,
                label = "Freehand",
                isSelected = state.currentType == AnnotationType.FREEHAND,
                onClick = { viewModel.setCurrentAnnotationType(AnnotationType.FREEHAND) }
            )

            ToolbarButton(
                icon = Icons.Default.ArrowForward,
                label = "Arrow",
                isSelected = state.currentType == AnnotationType.ARROW,
                onClick = { viewModel.setCurrentAnnotationType(AnnotationType.ARROW) }
            )

            ToolbarButton(
                icon = Icons.Default.TextSnippet,
                label = "Text",
                isSelected = state.currentType == AnnotationType.TEXT_BOX,
                onClick = { viewModel.setCurrentAnnotationType(AnnotationType.TEXT_BOX) }
            )

            ToolbarButton(
                icon = Icons.Default.Undo,
                label = "Undo",
                enabled = state.undoStack.isNotEmpty(),
                onClick = { viewModel.undo() }
            )

            ToolbarButton(
                icon = Icons.Default.Redo,
                label = "Redo",
                enabled = state.redoStack.isNotEmpty(),
                onClick = { viewModel.redo() }
            )

            Spacer(Modifier.weight(1f))

            ToolbarButton(
                icon = Icons.Default.DeleteSweep,
                label = "Clear",
                iconColor = Color.Red,
                onClick = { viewModel.clearAllAnnotations() }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color:", style = MaterialTheme.typography.labelSmall)

            listOf(
                Color.Yellow, Color.Green, Color.Red, Color.Blue,
                Color.Cyan, Color.Magenta, Color.Black, Color.Gray
            ).forEach { color ->
                ColorButton(
                    color = color,
                    isSelected = state.currentStyle.color == color,
                    onClick = {
                        val newStyle = state.currentStyle.copy(color = color)
                        viewModel.setCurrentStyle(newStyle)
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            Text("Size:", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = state.currentStyle.strokeWidth,
                onValueChange = {
                    val newStyle = state.currentStyle.copy(strokeWidth = it)
                    viewModel.setCurrentStyle(newStyle)
                },
                valueRange = 1f..10f,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    iconColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
    ) {
        Icon(icon, label, tint = if (iconColor != Color.Unspecified) iconColor else LocalContentColor.current)
    }
}

@Composable
fun ColorButton(
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                } else Modifier
            )
    )
}