@Composable  
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {  
    val query by viewModel.searchQuery.collectAsState()  
    val results by viewModel.searchResults.collectAsState()  
  
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {  
        // Search Input Field  
        SearchBar(query) { viewModel.onQueryChange(it) }  
  
        Spacer(modifier = Modifier.height(16.dp))  
  
        // Logic: Agar query khali nahi hai aur results empty hain  
        if (query.isNotEmpty() && results.isEmpty()) {  
            NoResultsFoundView(query)  
        } else {  
            // Results dikhao  
            LazyColumn {  
                items(results) { result -> SearchResultItem(result) }  
            }  
        }  
    }  
}  
  
@Composable  
fun NoResultsFoundView(query: String) {  
    Column(  
        modifier = Modifier.fillMaxSize(),  
        verticalArrangement = Arrangement.Center,  
        horizontalAlignment = Alignment.CenterHorizontally  
    ) {  
        Icon(  
            imageVector = Icons.Default.SearchOff, // Pre-defined Material Icon  
            contentDescription = null,  
            modifier = Modifier.size(80.dp),  
            tint = MaterialTheme.colorScheme.outline  
        )  
        Text(  
            text = "No results for \"$query\"",  
            style = MaterialTheme.typography.headlineSmall,  
            textAlign = TextAlign.Center  
        )  
        Text(  
            text = "Try checking the spelling or use different keywords.",  
            style = MaterialTheme.typography.bodyMedium,  
            textAlign = TextAlign.Center,  
            color = MaterialTheme.colorScheme.onSurfaceVariant  
        )  
          
        // Bonus Action: Search in Cloud if not synced  
        Button(onClick = { /* Refresh Cloud Sync */ }, modifier = Modifier.padding(top = 16.dp)) {  
            Text("Refresh Cloud Sync")  
        }  
    }  
}