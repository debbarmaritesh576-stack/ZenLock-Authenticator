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

    // Show validation errors via snackbar
    LaunchedEffect(state.validationErrors) {
        if (state.validationErrors.isNotEmpty()) {
            val firstError = state.validationErrors.first()
            snackbarHostState.showSnackbar(
                message = firstError.message,
                actionLabel = "Show All",
                duration = SnackbarDuration.Long
            )
        }
    }

    // Show save success
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Form saved successfully!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form - Page $pageNum (${state.fields.size} fields)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Auto-fill button
                    if (state.suggestedProfile != null) {
                        IconButton(onClick = { viewModel.autoFill() }) {
                            Icon(Icons.Default.AutoAwesome, "Auto-fill")
                        }
                    }
                    // Validate & Save
                    IconButton(onClick = { viewModel.validateAndSave() }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                    // Export
                    IconButton(onClick = { viewModel.showExportOptions() }) {
                        Icon(Icons.Default.Share, "Export")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.fields.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Description, null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("No form fields detected",
                        style = MaterialTheme.typography.bodyLarge)
                    Text("Try scanning the page or check if PDF has fillable fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Show validation errors at top
                if (state.validationErrors.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚠ ${state.validationErrors.size} field(s) need attention",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer)
                                state.validationErrors.take(3).forEach { error ->
                                    Text("• ${error.fieldName}: ${error.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }

                items(state.fields) { field ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = if (state.focusedFieldId == field.id) 3.dp else 0.dp
                    ) {
                        when (field.type) {
                            FormFieldType.TEXT, FormFieldType.EMAIL, FormFieldType.PHONE -> {
                                TextFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.MULTILINE_TEXT -> {
                                MultilineTextField(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.NUMBER -> {
                                NumberFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.DATE -> {
                                DateFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.TIME -> {
                                TimeFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.CHECKBOX -> {
                                CheckboxFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, if (it) "Yes" else "Off") }
                                )
                            }
                            FormFieldType.RADIO -> {
                                RadioButtonEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.DROPDOWN -> {
                                DropdownFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.LISTBOX -> {
                                ListBoxEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it.joinToString(",")) }
                                )
                            }
                            FormFieldType.SIGNATURE -> {
                                SignatureFieldEditor(
                                    field = field,
                                    onSignatureSaved = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.IMAGE -> {
                                ImageFieldEditor(
                                    field = field,
                                    onImageSelected = { viewModel.updateField(field.id, it.toString()) }
                                )
                            }
                            FormFieldType.BARCODE -> {
                                BarcodeFieldEditor(
                                    field = field,
                                    onBarcodeScanned = { viewModel.updateField(field.id, it) }
                                )
                            }
                            FormFieldType.PASSWORD -> {
                                PasswordFieldEditor(
                                    field = field,
                                    onValueChange = { viewModel.updateField(field.id, it) }
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    // Export dialog
    if (state.showExportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExportOptions() },
            title = { Text("Export Form Data") },
            text = {
                Column {
                    ExportOption(Icons.Default.Code, "JSON", "Machine-readable format") {
                        viewModel.exportToJson()
                    }
                    ExportOption(Icons.Default.TableChart, "CSV", "Open in Excel") {
                        viewModel.exportToCsv()
                    }
                    ExportOption(Icons.Default.Description, "FDF", "Import to other PDF apps") {
                        viewModel.exportToFdf()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideExportOptions() }) { Text("Cancel") }
            }
        )
    }

    // Profile picker dialog
    if (state.showProfilePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.hideProfilePicker() },
            title = { Text("Choose Profile") },
            text = {
                LazyColumn {
                    items(state.profiles) { profile ->
                        ListItem(
                            headlineContent = { Text(profile.name) },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.autoFillWithProfile(profile.id)
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideProfilePicker() }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ExportOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    )
}

@Composable
private fun PasswordFieldEditor(field: FormField, onValueChange: (String) -> Unit) {
    var password by remember(field.id) { mutableStateOf(field.value) }
    var visible by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text(field.name + if (field.isRequired) " *" else "")
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            }
        )
    }
}