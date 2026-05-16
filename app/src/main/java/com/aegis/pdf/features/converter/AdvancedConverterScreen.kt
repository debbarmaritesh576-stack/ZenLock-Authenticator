package com.aegis.pdf.feature.converter  
  
import androidx.compose.foundation.layout.*  
import androidx.compose.material3.*  
import androidx.compose.runtime.*  
import androidx.compose.ui.Alignment  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.platform.LocalContext  
import androidx.compose.ui.unit.dp  
import java.io.File  
  
@OptIn(ExperimentalMaterial3Api::class)  
@Composable  
fun AdvancedConverterScreen(  
    viewModel: AdvancedConversionViewModel,  
    onConversionComplete: (File) -> Unit  
) {  
    val uiState by viewModel.conversionState.collectAsState()  
    var selectedTab by remember { mutableStateOf(0) } // 0: URL to PDF, 1: PDF to Text  
    var inputUrl by remember { mutableStateOf("https://") }  
    val context = LocalContext.current  
  
    Scaffold(  
        topBar = { TopAppBar(title = { Text("Aegis Elite Converter") }) }  
    ) { paddingValues ->  
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {  
            // Tab structure selection controllers switches  
            TabRow(selectedTabIndex = selectedTab) {  
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Web to PDF") })  
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("PDF to Text") })  
            }  
  
            Spacer(modifier = Modifier.height(24.dp))  
  
            if (selectedTab == 0) {  
                OutlinedTextField(  
                    value = inputUrl,  
                    onValueChange = { inputUrl = it },  
                    label = { Text("Enter Webpage Target URL") },  
                    modifier = Modifier.fillMaxWidth()  
                )  
                Spacer(modifier = Modifier.weight(1f))  
                  
                Button(  
                    onClick = { viewModel.transformUrl(inputUrl, context.filesDir, "Web_Capture") },  
                    modifier = Modifier.fillMaxWidth()  
                ) { Text("Generate PDF Document from Web") }  
            } else {  
                Text("Select any Searchable PDF to scrap text matrix strings dynamically.")  
                Spacer(modifier = Modifier.weight(1f))  
                  
                // System picker fallback trigger placeholder button  
                Button(  
                    onClick = { /* Launch SAF Picker -> Pass selected File to viewModel.extractTextFromPdf */ },  
                    modifier = Modifier.fillMaxWidth()  
                ) { Text("Select Document and Extract Text") }  
            }  
  
            // Centralized loading spinner system trackers matrix checks  
            when (val state = uiState) {  
                is ConversionUiState.Processing -> {  
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {  
                        CircularProgressIndicator()  
                        Text(state.message, modifier = Modifier.padding(top = 8.dp))  
                    }  
                }  
                is ConversionUiState.Success -> LaunchedEffect(state.file) { onConversionComplete(state.file) }  
                is ConversionUiState.Error -> Text(state.reason, color = MaterialTheme.colorScheme.error)  
                else -> {}  
            }  
        }  
    }  
}