package com.aegis.pdf.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PickedFile(
    val uri: String,
    val name: String,
    val size: String
)

@Composable
fun FilePicker(
    files: List<PickedFile>,
    onRemoveFile: (Int) -> Unit,
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Selected Files (${files.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onAddFile) {
                Text("+ Add PDF")
            }
        }

        if (files.isEmpty()) {
            EmptyState(title = "No files selected", subtitle = "Tap + Add PDF to begin")
        } else {
            LazyColumn {
                items(files.size) { index ->
                    val file = files[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.name, fontSize = 14.sp, maxLines = 1)
                            Text(file.size, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        IconButton(onClick = { onRemoveFile(index) }) {
                            Text("✕", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}