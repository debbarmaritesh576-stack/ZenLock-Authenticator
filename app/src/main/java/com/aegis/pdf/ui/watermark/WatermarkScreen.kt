package com.aegis.pdf.ui.watermark

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.ProgressDialog

@Composable
fun WatermarkScreen(
    viewModel: WatermarkViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isProcessing by viewModel.isProcessing.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val watermarkType by viewModel.watermarkType.collectAsState()
    var watermarkText by remember { mutableStateOf("") }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.setInputFile(it) } }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Add Watermark", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (fileName.isEmpty()) {
            Button(onClick = { pdfLauncher.launch("application/pdf") }) {
                Text("Select PDF File")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("File: $fileName")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = watermarkType == "text",
                            onClick = { viewModel.setType("text") },
                            label = { Text("Text") }
                        )
                        FilterChip(
                            selected = watermarkType == "image",
                            onClick = { viewModel.setType("image") },
                            label = { Text("Image") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (watermarkType == "text") {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = { watermarkText = it },
                            label = { Text("Watermark Text") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.applyWatermark(watermarkText) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Text("Apply Watermark")
                    }
                }
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Adding Watermark", message = "Processing...")
    }

    resultMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            title = { Text("Result") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearResult()
                    if (msg.startsWith("Success")) onBack()
                }) { Text("OK") }
            }
        )
    }
}