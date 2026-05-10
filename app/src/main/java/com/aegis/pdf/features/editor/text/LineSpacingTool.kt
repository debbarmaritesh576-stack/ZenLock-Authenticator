package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LineSpacingTool(
    currentSpacing: Float,
    onSpacingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacings = listOf(1.0f, 1.15f, 1.5f, 2.0f, 2.5f, 3.0f)

    Column(modifier = modifier.padding(8.dp)) {
        Text("Line Spacing", style = MaterialTheme.typography.labelSmall)

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            spacings.forEach { spacing ->
                FilterChip(
                    selected = currentSpacing == spacing,
                    onClick = { onSpacingChanged(spacing) },
                    label = { Text("${spacing}x") }
                )
            }
        }
    }
}