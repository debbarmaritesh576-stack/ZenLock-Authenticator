@Composable  
fun CloudDriveScreen(  
    viewModel: CloudDriveViewModel = hiltViewModel(),  
    onBack: () -> Unit  
) {  
    val isConnected by viewModel.isConnected.collectAsState()  
    val files by viewModel.cloudFiles.collectAsState()  
    val isRefreshing by viewModel.isRefreshing.collectAsState() // Naya state  
    val accountName by viewModel.accountName.collectAsState()  
    val snackbarHostState = remember { SnackbarHostState() }  
  
    Scaffold(  
        snackbarHost = { SnackbarHost(snackbarHostState) },  
        topBar = {  
            TopAppBar(  
                title = { Text("Aegis Cloud Storage") },  
                navigationIcon = {  
                    IconButton(onClick = onBack) {  
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")  
                    }  
                }  
            )  
        }  
    ) { padding ->  
        Column(  
            modifier = Modifier  
                .fillMaxSize()  
                .padding(padding)  
                .padding(16.dp)  
        ) {  
            if (!isConnected) {  
                ConnectionPanel(viewModel)  
            } else {  
                AccountStatusCard(accountName, viewModel::disconnect)  
                  
                Spacer(modifier = Modifier.height(24.dp))  
  
                // Files Header with Sync Indicator  
                Row(  
                    verticalAlignment = Alignment.CenterVertically,  
                    modifier = Modifier.fillMaxWidth()  
                ) {  
                    Text(  
                        text = "Your Cloud PDFs",  
                        style = MaterialTheme.typography.titleLarge,  
                        modifier = Modifier.weight(1f)  
                    )  
                    if (isRefreshing) {  
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))  
                    } else {  
                        IconButton(onClick = { viewModel.refreshFiles() }) {  
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")  
                        }  
                    }  
                }  
  
                Spacer(modifier = Modifier.height(12.dp))  
  
                if (files.isEmpty() && !isRefreshing) {  
                    EmptyCloudState()  
                } else {  
                    FileList(files, viewModel::downloadFile)  
                }  
            }  
        }  
    }  
}  
  
@Composable  
fun FileList(files: List<CloudPdfFile>, onDownload: (CloudPdfFile) -> Unit) {  
    LazyColumn(  
        verticalArrangement = Arrangement.spacedBy(10.dp),  
        contentPadding = PaddingValues(bottom = 16.dp)  
    ) {  
        items(files) { file ->  
            Card(  
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),  
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))  
            ) {  
                Row(  
                    modifier = Modifier  
                        .fillMaxWidth()  
                        .padding(16.dp),  
                    verticalAlignment = Alignment.CenterVertically  
                ) {  
                    // PDF Icon  
                    Icon(  
                        imageVector = Icons.Default.PictureAsPdf,  
                        contentDescription = null,  
                        tint = MaterialTheme.colorScheme.primary,  
                        modifier = Modifier.size(32.dp)  
                    )  
                      
                    Spacer(modifier = Modifier.width(16.dp))  
                      
                    Column(modifier = Modifier.weight(1f)) {  
                        Text(file.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)  
                        Text(file.size, style = MaterialTheme.typography.bodySmall)  
                    }  
  
                    FilledTonalButton(  
                        onClick = { onDownload(file) },  
                        shape = RoundedCornerShape(8.dp)  
                    ) {  
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))  
                        Spacer(modifier = Modifier.width(4.dp))  
                        Text("Save")  
                    }  
                }  
            }  
        }  
    }  
}