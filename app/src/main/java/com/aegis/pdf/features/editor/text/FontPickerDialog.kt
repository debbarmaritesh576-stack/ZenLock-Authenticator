package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun FontPickerDialog(
    currentFont: String,
    onFontSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fonts = listOf(
        "Helvetica" to "Helvetica",
        "Times New Roman" to "Times New Roman",
        "Courier" to "Courier",
        "Noto Sans" to "Noto Sans",
        "Noto Serif" to "Noto Serif",
        "Roboto" to "Roboto",
        "Open Sans" to "Open Sans"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Font",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyColumn {
                    items(fonts.size) { index ->
                        val (name, family) = fonts[index]
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFontSelected(name) }
                                .padding(vertical = 4.dp),
                            color = if (name == currentFont)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = name,
                                fontFamily = FontFamily.Default,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}