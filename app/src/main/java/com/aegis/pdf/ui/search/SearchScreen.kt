package com.aegis.pdf.ui.search  
  
import androidx.compose.foundation.layout.*  
import androidx.compose.foundation.lazy.LazyColumn  
import androidx.compose.foundation.lazy.items  
import androidx.compose.material3.*  
import androidx.compose.runtime.*  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.unit.dp  
  
@Composable  
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {  
    val query by viewModel.searchQuery.collectAsState()  
    val results by viewModel.searchResults.collectAsState()  
  
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {  
        // 1. Search Bar  
        OutlinedTextField(  
            value = query,  
            onValueChange = { viewModel.onQueryChange(it) },  
            label = { Text("Search files or content...") },  
            modifier = Modifier.fillMaxWidth(),  
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }  
        )  
  
        Spacer(modifier = Modifier.height(16.dp))  
  
        // 2. Results List  
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {  
            items(results) { result ->  
                SearchResultItem(result)  
            }  
        }  
    }  
}  
  
@Composable  
fun SearchResultItem(result: SearchQueryResult) {  
    Card(modifier = Modifier.fillMaxWidth()) {  
        Row(modifier = Modifier.padding(16.dp)) {  
            val icon = when(result) {  
                is SearchQueryResult.Local -> Icons.Default.Description  
                is SearchQueryResult.Cloud -> Icons.Default.Cloud  
                is SearchQueryResult.Content -> Icons.Default.TextFields  
            }  
            Icon(icon, contentDescription = null)  
            Spacer(modifier = Modifier.width(12.dp))  
            Column {  
                Text(text = result.fileName, style = MaterialTheme.typography.titleMedium)  
                Text(  
                    text = when(result) {  
                        is SearchQueryResult.Local -> "Local File"  
                        is SearchQueryResult.Cloud -> "Cloud: ${result.provider}"  
                        is SearchQueryResult.Content -> "Found inside document"  
                    },  
                    style = MaterialTheme.typography.bodySmall  
                )  
            }  
        }  
    }  
}