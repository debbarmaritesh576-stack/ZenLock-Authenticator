package com.aegis.pdf.features.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    docPtr: Long,
    pageNum: Int,
    onBack: () -> Unit,
    viewModel: FormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(docPtr, pageNum) {
        viewModel.loadForm(docPtr, pageNum)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form - Page $pageNum") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.autoFill() }) {
                        Icon(Icons.Default.AutoAwesome, "Auto-fill")
                    }
                    IconButton(onClick = {
                        val errors = viewModel.validate()
                        if (errors.isEmpty()) {
                            viewModel.save()
                        }
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                    IconButton(onClick = { viewModel.showExportOptions() }) {
                        Icon(Icons.Default.Share, "Export")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.fields.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No form fields detected on this page",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.fields) { field ->
                    when (field.type) {
                        FormFieldType.TEXT -> TextFieldEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                        FormFieldType.MULTILINE_TEXT -> MultilineTextField(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                        FormFieldType.NUMBER -> NumberFieldEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                        FormFieldType.DATE -> DateFieldEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                        FormFieldType.CHECKBOX -> CheckboxFieldEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, if (it) "Yes" else "No") }
                        )
                        FormFieldType.RADIO -> RadioButtonEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                        FormFieldType.DROPDOWN -> DropdownFieldEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                        else -> TextFieldEditor(
                            field = field,
                            onValueChange = { viewModel.updateField(field.id, it) }
                        )
                    }
                }
            }
        }
    }

    if (state.showExportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExportOptions() },
            title = { Text("Export Form Data") },
            text = {
                Column {
                    TextButton(onClick = {
                        viewModel.exportToJson()
                    }) {
                        Icon(Icons.Default.Code, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export as JSON")
                    }
                    TextButton(onClick = {
                        viewModel.exportToCsv()
                    }) {
                        Icon(Icons.Default.TableChart, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export as CSV")
                    }
                    TextButton(onClick = {
                        viewModel.exportToFdf()
                    }) {
                        Icon(Icons.Default.Description, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export as FDF")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideExportOptions() }) {
                    Text("Cancel")
                }
            }
        )
    }
}