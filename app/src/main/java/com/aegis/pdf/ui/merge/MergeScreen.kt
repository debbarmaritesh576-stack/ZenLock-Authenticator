package com.aegis.pdf.ui.merge

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.FilePicker
import com.aegis.pdf.ui.components.ProgressDialog
import com.aegis.pdf.ui.components.PickedFile
import com.aegis.pdf.data.repository.FileRepository

@Composable
fun MergeScreen(
    viewModel: MergeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fileRepository = remember { FileRepository(context) }
    val files by viewModel.files.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val name = fileRepository.getFileName(uri)
            val size = fileRepository.formatSize(fileRepository.getFileSize(uri))
            viewModel.addFile(PickedFile(uri.toString(), name, size))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Merge PDF",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilePicker(
            files = files,
            onRemoveFile = { viewModel.removeFile(it) },
            onAddFile = { launcher.launch(arrayOf("application/pdf")) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.mergeFiles() },
            modifier = Modifier.fillMaxWidth(),
            enabled = files.size >= 2 && !isProcessing
        ) {
            Text("Merge ${files.size} Files")
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Merging PDFs", message = "Combining your files...")
    }

    resultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            title = { Text("Done") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearResult()
                    onBack()
                }) {
                    Text("OK")
                }
            }
        )
    }
}