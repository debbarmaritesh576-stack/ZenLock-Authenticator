package com.aegis.pdf.features.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFieldEditor(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember(field.id) { mutableStateOf(field.value) }
    var showPicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        if (field.name.isNotEmpty()) {
            Text(
                text = field.name + if (field.isRequired) " *" else "",
                style = MaterialTheme.typography.labelLarge
            )
        }

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
            enabled = !field.isReadOnly,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.CalendarMonth, "Select date")
            },
            label = { Text("Date") }
        )

        if (showPicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = dateFormatter.format(Date(millis))
                            onValueChange(selectedDate)
                        }
                        showPicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}