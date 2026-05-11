package com.aegis.pdf.features.editor.annotation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AnnotationListItem(val id: Long, val type: String, val page: Int, val preview: String, val isVisible: Boolean)

@Composable
fun AnnotationListPanel(
    annotations: List<AnnotationListItem>,
    onVisibilityToggle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onAnnotationClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Annotations (${annotations.size})", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
        if (annotations.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                Text("No annotations yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        LazyColumn {
            items(annotations) { a ->
                Surface(Modifier.fillMaxWidth().clickable { onAnnotationClick(a.id) }, tonalElevation = 1.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(a.type, style = MaterialTheme.typography.bodyMedium)
                            Text("Page ${a.page} - ${a.preview}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Row {
                            IconButton(onClick = { onVisibilityToggle(a.id) }) {
                                Icon(Icons.Default.Visibility, "Toggle", Modifier.size(18.dp), tint = if (a.isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDelete(a.id) }) {
                                Icon(Icons.Default.Delete, "Delete", Modifier.size(18.dp))
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}