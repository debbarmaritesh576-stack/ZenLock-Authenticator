package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeFieldEditor(
    field: FormField,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var barcodeValue by remember(field.id) { mutableStateOf(field.value) }
    var showScanner by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = field.name + if (field.isRequired) " *" else "",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedTextField(
            value = barcodeValue,
            onValueChange = { newValue ->
                barcodeValue = newValue
                onBarcodeScanned(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !field.isReadOnly,
            label = { Text("Barcode / QR Code") }
        )

        OutlinedButton(
            onClick = { showScanner = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan Barcode")
        }
    }
}