package com.aegis.pdf.ui.security

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.ProgressDialog

@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isProcessing by viewModel.isProcessing.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val mode by viewModel.mode.collectAsState()
    var password by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setInputFile(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("PDF Security", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (fileName.isEmpty()) {
            Button(onClick = { launcher.launch("application/pdf") }) {
                Text("Select PDF File")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("File: $fileName")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = mode == "protect",
                            onClick = { viewModel.setMode("protect") },
                            label = { Text("Add Password") }
                        )
                        FilterChip(
                            selected = mode == "unlock",
                            onClick = { viewModel.setMode("unlock") },
                            label = { Text("Remove Password") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.process(password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = password.isNotEmpty() && !isProcessing
                    ) {
                        Text(if (mode == "protect") "Add Password" else "Remove Password")
                    }
                }
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Processing", message = "Please wait...")
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