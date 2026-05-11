package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxFieldEditor(
    field: FormField,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var checked by remember(field.id) { mutableStateOf(field.value == "Yes" || field.value == "true") }

    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                onValueChange(newValue)
            },
            enabled = !field.isReadOnly
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = field.name + if (field.isRequired) " *" else "",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}