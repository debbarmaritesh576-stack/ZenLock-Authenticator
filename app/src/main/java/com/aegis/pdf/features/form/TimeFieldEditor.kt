package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFieldEditor(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTime by remember(field.id) { mutableStateOf(field.value) }
    var showPicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.US) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = field.name + if (field.isRequired) " *" else "",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedTextField(
            value = selectedTime,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = !field.isReadOnly,
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.Schedule, "Select time") }
        )

        if (showPicker) {
            val timePickerState = rememberTimePickerState()
            AlertDialog(
                onDismissRequest = { showPicker = false },
                title = { Text("Select Time") },
                text = { TimePicker(state = timePickerState) },
                confirmButton = {
                    TextButton(onClick = {
                        selectedTime = "${timePickerState.hour}:${timePickerState.minute}"
                        onValueChange(selectedTime)
                        showPicker = false
                    }) { Text("OK") }
                }
            )
        }
    }
}