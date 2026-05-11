package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListBoxEditor(
    field: FormField,
    onValueChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItems by remember(field.id) { mutableStateOf(setOf<String>()) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = field.name + if (field.isRequired) " *" else "",
            style = MaterialTheme.typography.labelLarge
        )

        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
            shape = MaterialTheme.shapes.small,
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            LazyColumn {
                items(field.options) { option ->
                    val isSelected = option in selectedItems
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    selectedItems = if (isSelected) {
                                        selectedItems - option
                                    } else {
                                        selectedItems + option
                                    }
                                    onValueChange(selectedItems.toList())
                                }
                            )
                            .padding(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}