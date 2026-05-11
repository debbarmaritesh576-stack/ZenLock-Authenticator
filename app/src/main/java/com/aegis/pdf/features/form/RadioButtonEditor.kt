package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonEditor(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember(field.id) { mutableStateOf(field.value) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = field.name + if (field.isRequired) " *" else "",
            style = MaterialTheme.typography.labelLarge
        )

        field.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == option,
                        onClick = {
                            selectedOption = option
                            onValueChange(option)
                        }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = null,
                    enabled = !field.isReadOnly
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}