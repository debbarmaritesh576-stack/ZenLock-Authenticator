package com.aegis.pdf.ui.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    var question by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.loadPdf(it, context) } }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Chat with PDF", style = MaterialTheme.typography.headlineMedium)
        Text("Ask questions about your document", style = MaterialTheme.typography.bodySmall)

        if (fileName.isEmpty()) {
            Button(
                onClick = { launcher.launch("application/pdf") },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text("Select PDF File")
            }
        } else {
            Text("File: $fileName", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isUser)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "${if (msg.isUser) "You" else "AI"}: ${msg.text}",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about the PDF...") },
                    enabled = !isProcessing
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (question.isNotBlank()) {
                            viewModel.askQuestion(question)
                            question = ""
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    },
                    enabled = !isProcessing && question.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    }
}