package com.aegis.pdf.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
fun PageReorderScreen(
    viewModel: PageReorderViewModel = hiltViewModel(),
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
        Text("Reorder Pages", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (fileName.isEmpty()) {
            Button(onClick = { launcher.launch("application/pdf") }) {
                Text("Select PDF File")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("File: $fileName", fontWeight = FontWeight.SemiBold)
                    Text("Total Pages: $pageCount")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Drag to reorder (coming soon)", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveReordered() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text("Save Current Order")
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Saving", message = "Reordering pages...")
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