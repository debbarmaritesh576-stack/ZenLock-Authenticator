package com.aegis.pdf.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.ProgressDialog

@Composable
fun PageNumberScreen(
    viewModel: PageNumberViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    var position by remember { mutableStateOf("bottom-right") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.loadPdf(it, context) } }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Add Page Numbers", style = MaterialTheme.typography.headlineMedium)
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

            Text("Position:", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right", "Center").forEach { pos ->
                    FilterChip(
                        selected = position == pos.lowercase().replace(" ", "-"),
                        onClick = { position = pos.lowercase().replace(" ", "-") },
                        label = { Text(pos) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.addPageNumbers(position) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text("Add Page Numbers")
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Processing", message = "Adding page numbers...")
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