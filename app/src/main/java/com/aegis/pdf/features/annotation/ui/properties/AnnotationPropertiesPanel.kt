package com.aegis.pdf.features.annotation.ui.properties

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

@Composable
fun AnnotationPropertiesPanel(
    viewModel: AnnotationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val selectedAnnotation = state.annotations.find { it.id == state.selectedAnnotationId }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedAnnotation != null) {
            Text("Properties", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Opacity:", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = selectedAnnotation.opacity,
                    onValueChange = {
                        viewModel.changeAnnotationOpacity(selectedAnnotation.id, it)
                    },
                    valueRange = 0.1f..1f,
                    modifier = Modifier.weight(1f)
                )
                Text("${(selectedAnnotation.opacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Stroke:", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = selectedAnnotation.strokeWidth,
                    onValueChange = { /* Modify stroke */ },
                    valueRange = 1f..10f,
                    modifier = Modifier.weight(1f)
                )
                Text("${selectedAnnotation.strokeWidth.toInt()}px", style = MaterialTheme.typography.labelSmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.deleteAnnotation(selectedAnnotation.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }

                Button(
                    onClick = { /* Duplicate annotation */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Copy")
                }
            }
        } else {
            Text("No annotation selected", style = MaterialTheme.typography.labelSmall)
        }
    }
}