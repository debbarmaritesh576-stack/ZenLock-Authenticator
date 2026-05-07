package com.aegis.pdf.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.ProgressDialog

@Composable
fun PageRotateScreen(
    viewModel: PageRotateViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.loadPdf(it, context) } }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Rotate Pages", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (fileName.isEmpty()) {
            Button(onClick = { launcher.launch("application/pdf") }) {
                Text("Select PDF File")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("File: $fileName", fontWeight = FontWeight.SemiBold)
                    Text("Pages: $pageCount")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = { viewModel.rotateAll(90) }, modifier = Modifier.weight(1f)) {
                    Text("90°")
                }
                Button(onClick = { viewModel.rotateAll(180) }, modifier = Modifier.weight(1f)) {
                    Text("180°")
                }
                Button(onClick = { viewModel.rotateAll(270) }, modifier = Modifier.weight(1f)) {
                    Text("270°")
                }
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Rotating", message = "Processing pages...")
    }

    resultMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            title = { Text("Done") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearResult()
                    if (msg.startsWith("Saved")) onBack()
                }) { Text("OK") }
            }
        )
    }
}