package com.aegis.pdf.ui.ocr

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun OcrScreen(
    viewModel: OcrViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    val fileName by viewModel.fileName.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.extractText(it, context) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("OCR - Extract Text", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Select Image")
            }
            if (extractedText.isNotEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.copyToClipboard(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copy Text")
                }
            }
        }

        if (fileName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("File: $fileName", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (extractedText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = extractedText,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    fontSize = 14.sp
                )
            }
        } else if (!isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        "Select an image to extract text",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (isProcessing) {
        ProgressDialog(title = "Extracting Text", message = "Processing image...")
    }
}