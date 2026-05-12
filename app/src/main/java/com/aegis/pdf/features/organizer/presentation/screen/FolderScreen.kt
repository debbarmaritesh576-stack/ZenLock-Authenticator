package com.aegis.pdf.features.organizer.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.features.organizer.presentation.components.BreadcrumbNavigator
import com.aegis.pdf.features.organizer.presentation.components.FileCard
import com.aegis.pdf.features.organizer.presentation.components.FileGrid
import com.aegis.pdf.features.organizer.presentation.components.FileList
import com.aegis.pdf.features.organizer.presentation.model.BreadcrumbUiModel
import com.aegis.pdf.features.organizer.presentation.utils.FileSortFilter
import com.aegis.pdf.features.organizer.presentation.viewmodel.FolderViewModel
import com.aegis.pdf.features.organizer.presentation.viewmodel.MultiSelectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    folderId: String? = null,
    onFolderClick: (String) -> Unit,
    onFileClick: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val selectionViewModel: MultiSelectViewModel = hiltViewModel()
    val selectionState by selectionViewModel.state.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showMoreOptions by remember { mutableStateOf(false) }

    LaunchedEffect(folderId) {
        viewModel.loadFolder(folderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectionState.isSelectionMode) {
                            "${selectionState.selectedFiles.size + selectionState.selectedFolders.size} selected"
                        } else {
                            state.folderName ?: "My Documents"
                        }
                    )
                },
                navigationIcon = {
                    if (folderId != null) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    } else if (selectionState.isSelectionMode) {
                        IconButton(onClick = { selectionViewModel.clearAll() }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    }
                },
                actions = {
                    if (!selectionState.isSelectionMode) {
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                if (state.isGridView) Icons.Default.List else Icons.Default.GridView3,
                                "Toggle view"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleSortOptions() }) {
                            Icon(Icons.Default.Sort, "Sort")
                        }
                        IconButton(onClick = { showMoreOptions = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!selectionState.isSelectionMode) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BreadcrumbNavigator(
                breadcrumbs = state.breadcrumbs,
                onBreadcrumbClick = { selectedFolderId ->
                    viewModel.loadFolder(selectedFolderId)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.error != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                state.error ?: "An error occurred",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.clearError() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                    state.folders.isEmpty() && state.files.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "This folder is empty",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Create a new folder or add a document",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    state.showSortOptions -> {
                        FileSortFilter(
                            currentSort = state.sortBy,
                            isAscending = state.sortAscending,
                            onSortChange = { viewModel.setSortOption(it) },
                            onDirectionToggle = { viewModel.toggleSortDirection() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        )
                    }
                    else -> {
                        if (state.isGridView) {
                            FileGrid(
                                folders = state.folders,
                                files = state.files,
                                onFolderClick = onFolderClick,
                                onFileClick = onFileClick,
                                onFolderLongClick = { folderIdSelected ->
                                    selectionViewModel.enterSelectionMode()
                                    selectionViewModel.selectFolder(folderIdSelected)
                                },
                                onFileLongClick = { fileIdSelected ->
                                    selectionViewModel.enterSelectionMode()
                                    selectionViewModel.selectFile(fileIdSelected)
                                },
                                selectedFolders = selectionState.selectedFolders,
                                selectedFiles = selectionState.selectedFiles,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            FileList(
                                folders = state.folders,
                                files = state.files,
                                onFolderClick = onFolderClick,
                                onFileClick = onFileClick,
                                onFolderLongClick = { folderIdSelected ->
                                    selectionViewModel.enterSelectionMode()
                                    selectionViewModel.selectFolder(folderIdSelected)
                                },
                                onFileLongClick = { fileIdSelected ->
                                    selectionViewModel.enterSelectionMode()
                                    selectionViewModel.selectFile(fileIdSelected)
                                },
                                selectedFolders = selectionState.selectedFolders,
                                selectedFiles = selectionState.selectedFiles,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName)
                            newFolderName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMoreOptions) {
        DropdownMenu(
            expanded = showMoreOptions,
            onDismissRequest = { showMoreOptions = false }
        ) {
            DropdownMenuItem(
                text = { Text("Select All") },
                onClick = {
                    selectionViewModel.selectAll(
                        state.files.map { it.id },
                        state.folders.map { it.id }
                    )
                    showMoreOptions = false
                }
            )
            DropdownMenuItem(
                text = { Text("Refresh") },
                onClick = {
                    viewModel.loadFolder(folderId)
                    showMoreOptions = false
                }
            )
        }
    }
}