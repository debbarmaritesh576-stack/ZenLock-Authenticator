package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MultilineTextField(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(field.id) { mutableStateOf(field.value) }
    var charCount by remember { mutableStateOf(field.value.length) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        if (field.name.isNotEmpty()) {
            Text(
                text = field.name + if (field.isRequired) " *" else "",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                if (field.maxLength == 0 || newValue.length <= field.maxLength) {
                    text = newValue
                    charCount = newValue.length
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            enabled = !field.isReadOnly,
            minLines = 3,
            maxLines = 8,
            supportingText = {
                if (field.maxLength > 0) {
                    Text("$charCount/${field.maxLength}")
                }
            }
        )
    }
}