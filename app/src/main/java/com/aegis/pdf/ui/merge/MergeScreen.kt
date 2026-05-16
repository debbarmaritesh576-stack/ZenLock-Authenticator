package com.aegis.pdf.feature.merger  
  
import androidx.compose.foundation.layout.*  
import androidx.compose.foundation.lazy.LazyColumn  
import androidx.compose.foundation.lazy.itemsIndexed  
import androidx.compose.material3.*  
import androidx.compose.runtime.*  
import androidx.compose.ui.Alignment  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.platform.LocalContext  
import androidx.compose.ui.unit.dp  
import java.io.File  
  
@OptIn(ExperimentalMaterial3Api::class)  
@Composable  
fun MergeScreen(  
    viewModel: MergeViewModel,  
    onMergeCompleted: (File) -> Unit  
) {  
    val files by viewModel.selectedFiles.collectAsState()  
    val uiState by viewModel.uiState.collectAsState()  
    val context = LocalContext.current  
    var outputName by remember { mutableStateOf("Aegis_Merged") }  
  
    Scaffold(  
        topBar = { TopAppBar(title = { Text("Merge Documents") }) }  
    ) { paddingValues ->  
        Column(  
            modifier = Modifier  
                .fillMaxSize()  
                .padding(paddingValues)  
                .padding(16.dp)  
        ) {  
            // Output File Name Input Field  
            OutlinedTextField(  
                value = outputName,  
                onValueChange = { outputName = it },  
                label = { Text("Output File Name") },  
                modifier = Modifier.fillMaxWidth()  
            )  
  
            Spacer(modifier = Modifier.height(16.dp))  
  
            // Dynamic List Layout  
            LazyColumn(modifier = Modifier.weight(1f)) {  
                itemsIndexed(files) { index, file ->  
                    Card(  
                        modifier = Modifier  
                            .fillMaxWidth()  
                            .padding(vertical = 4.dp)  
                    ) {  
                        Row(  
                            modifier = Modifier  
                                .padding(16.dp)  
                                .fillMaxWidth(),  
                            horizontalArrangement = Arrangement.SpaceBetween,  
                            verticalAlignment = Alignment.CenterVertically  
                        ) {  
                            Text(text = "${index + 1}. ${file.name}", maxLines = 1)  
                            Button(  
                                onClick = { viewModel.removeFile(file) },  
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)  
                            ) {  
                                Text("Remove")  
                            }  
                        }  
                    }  
                }  
            }  
  
            Spacer(modifier = Modifier.height(16.dp))  
  
            // Contextual Button State Control based on Custom UIState  
            when (val state = uiState) {  
                is MergeUiState.Loading -> {  
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))  
                }  
                is MergeUiState.Success -> {  
                    LaunchedEffect(state.file) { onMergeCompleted(state.file) }  
                }  
                is MergeUiState.Error -> {  
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)  
                }  
                else -> {  
                    Button(  
                        onClick = { viewModel.startMerging(context.filesDir, outputName) },  
                        modifier = Modifier.fillMaxWidth(),  
                        enabled = files.size >= 2  
                    ) {  
                        Text("Merge ${files.size} Files")  
                    }  
                }  
            }  
        }  
    }  
}