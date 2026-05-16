package com.aegis.pdf.feature.splitter  
  
import androidx.compose.foundation.background  
import androidx.compose.foundation.clickable  
import androidx.compose.foundation.layout.*  
import androidx.compose.foundation.lazy.grid.GridCells  
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid  
import androidx.compose.material3.*  
import androidx.compose.runtime.*  
import androidx.compose.ui.Alignment  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.graphics.Color  
import androidx.compose.ui.unit.dp  
import java.io.File  
  
@OptIn(ExperimentalMaterial3Api::class)  
@Composable  
fun SplitScreen(  
    viewModel: SplitViewModel,  
    sourceFile: File,  
    totalDocPages: Int,  
    onSplitDone: (File) -> Unit  
) {  
    val selectedPages by viewModel.selectedPages.collectAsState()  
    val splitState by viewModel.splitState.collectAsState()  
    var outputName by remember { mutableStateOf("Aegis_Sliced_Doc") }  
  
    Scaffold(  
        topBar = { TopAppBar(title = { Text("Split PDF Pages") }) }  
    ) { paddingValues ->  
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {  
            OutlinedTextField(  
                value = outputName,  
                onValueChange = { outputName = it },  
                label = { Text("Output Document Name") },  
                modifier = Modifier.fillMaxWidth()  
            )  
  
            Spacer(modifier = Modifier.height(16.dp))  
  
            // Dynamic Checkbox Selection Grid Container  
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f)) {  
                items(totalDocPages) { index ->  
                    val isSelected = selectedPages.contains(index)  
                    Box(  
                        modifier = Modifier  
                            .padding(4.dp)  
                            .aspectRatio(0.7f)  
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.LightGray)  
                            .clickable { viewModel.togglePage(index) },  
                        contentAlignment = Alignment.Center  
                    ) {  
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {  
                            Text("Page ${index + 1}")  
                            Checkbox(checked = isSelected, onCheckedChange = { viewModel.togglePage(index) })  
                        }  
                    }  
                }  
            }  
  
            Spacer(modifier = Modifier.height(16.dp))  
  
            // Transaction execution mapping states check  
            when (val state = splitState) {  
                is SplitUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))  
                is SplitUiState.Success -> LaunchedEffect(state.file) { onSplitDone(state.file) }  
                is SplitUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)  
                else -> {  
                    Button(  
                        onClick = { viewModel.executeSplit(sourceFile, sourceFile.parentFile!!, outputName) },  
                        modifier = Modifier.fillMaxWidth(),  
                        enabled = selectedPages.isNotEmpty()  
                    ) {  
                        Text("Extract ${selectedPages.size} Pages")  
                    }  
                }  
            }  
        }  
    }  
}