package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextPropertiesSheet(
    element: TextElement?,
    onUpdate: (TextElement) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    if (element == null) return

    val sheetState = rememberModalBottomSheetState()
    var editedText by remember { mutableStateOf(element.text) }
    var fontSize by remember { mutableStateOf(element.fontSize) }
    var isBold by remember { mutableStateOf(element.isBold) }
    var isItalic by remember { mutableStateOf(element.isItalic) }
    var isUnderline by remember { mutableStateOf(element.isUnderline) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Text Properties", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Text content
            OutlinedTextField(
                value = editedText,
                onValueChange = { editedText = it },
                label = { Text("Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Font size
            Text("Font Size: ${fontSize.toInt()}")
            Slider(
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 8f..72f
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Style toggles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = isBold, onClick = { isBold = !isBold }, label = { Text("Bold") })
                FilterChip(selected = isItalic, onClick = { isItalic = !isItalic }, label = { Text("Italic") })
                FilterChip(selected = isUnderline, onClick = { isUnderline = !isUnderline }, label = { Text("Underline") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Position info
            Text("Position: X=${element.x.toInt()}, Y=${element.y.toInt()}", 
                 style = MaterialTheme.typography.bodySmall)
            Text("Size: ${element.width.toInt()}×${element.height.toInt()}", 
                 style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }

                Button(onClick = {
                    onUpdate(element.copy(
                        text = editedText,
                        fontSize = fontSize,
                        isBold = isBold,
                        isItalic = isItalic,
                        isUnderline = isUnderline
                    ))
                    onDismiss()
                }) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}