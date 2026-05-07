package com.aegis.pdf.ui.split

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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.ProgressDialog

@Composable
fun SplitScreen(
    viewModel: SplitViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setInputFile(it, context) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Split PDF", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (fileName.isEmpty()) {
            Button(onClick = { launcher.launch("application/pdf") }) {
                Text("Select PDF File")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected: $fileName", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pages: $pageCount", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.splitAllPages() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text("Split All Pages")
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Splitting PDF", message = "Processing pages...")
    }

    resultMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            title = { Text("Done") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearResult()
                    if (msg.startsWith("Split")) onBack()
                }) { Text("OK") }
            }
        )
    }
}