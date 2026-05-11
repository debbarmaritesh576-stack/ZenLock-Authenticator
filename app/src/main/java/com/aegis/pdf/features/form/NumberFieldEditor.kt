package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun NumberFieldEditor(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(field.id) { mutableStateOf(field.value) }
    var hasError by remember { mutableStateOf(false) }

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
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    text = newValue
                    hasError = false
                    onValueChange(newValue)
                } else {
                    hasError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !field.isReadOnly,
            isError = hasError,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            supportingText = {
                if (hasError) Text("Please enter a valid number")
            }
        )
    }
}