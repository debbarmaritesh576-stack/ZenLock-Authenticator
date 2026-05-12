package com.aegis.pdf.features.convert

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

enum class ConvertTarget {
    PDF_TO_WORD, PDF_TO_EXCEL, PDF_TO_PPT,
    PDF_TO_IMAGE, PDF_TO_HTML, PDF_TO_TEXT,
    IMAGE_TO_PDF, WORD_TO_PDF, EXCEL_TO_PDF, PPT_TO_PDF, HTML_TO_PDF
}

data class ConvertOption(
    val target: ConvertTarget,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val convertOptions = listOf(
    ConvertOption(ConvertTarget.PDF_TO_WORD, "PDF to Word", "Convert PDF to editable DOCX", Icons.Default.Description),
    ConvertOption(ConvertTarget.PDF_TO_EXCEL, "PDF to Excel", "Extract tables to XLSX", Icons.Default.TableChart),
    ConvertOption(ConvertTarget.PDF_TO_PPT, "PDF to PowerPoint", "Convert slides to PPTX", Icons.Default.Slideshow),
    ConvertOption(ConvertTarget.PDF_TO_IMAGE, "PDF to Image", "Export pages as JPG/PNG", Icons.Default.Image),
    ConvertOption(ConvertTarget.PDF_TO_HTML, "PDF to HTML", "Convert to web page", Icons.Default.Code),
    ConvertOption(ConvertTarget.PDF_TO_TEXT, "PDF to Text", "Extract plain text", Icons.Default.TextSnippet),
    ConvertOption(ConvertTarget.IMAGE_TO_PDF, "Image to PDF", "Create PDF from images", Icons.Default.AddPhotoAlternate),
    ConvertOption(ConvertTarget.WORD_TO_PDF, "Word to PDF", "Convert DOCX to PDF", Icons.Default.PictureAsPdf),
    ConvertOption(ConvertTarget.EXCEL_TO_PDF, "Excel to PDF", "Convert XLSX to PDF", Icons.Default.PictureAsPdf),
    ConvertOption(ConvertTarget.PPT_TO_PDF, "PPT to PDF", "Convert PPTX to PDF", Icons.Default.PictureAsPdf),
    ConvertOption(ConvertTarget.HTML_TO_PDF, "HTML to PDF", "Convert web page to PDF", Icons.Default.PictureAsPdf)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(
    onBack: () -> Unit,
    onConvertSelected: (ConvertTarget) -> Unit,
    viewModel: ConvertViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Convert") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showHistory() }) {
                        Icon(Icons.Default.History, "History")
                    }
                    IconButton(onClick = { viewModel.showSettings() }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (state.showHistory) {
            ConversionHistoryScreen(
                history = state.history,
                onClear = { viewModel.clearHistory() },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Choose Conversion", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                items(convertOptions) { option ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onConvertSelected(option.target) }
                    ) {
                        ListItem(
                            headlineContent = { Text(option.title) },
                            supportingContent = { Text(option.subtitle) },
                            leadingContent = {
                                Icon(option.icon, null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        )
                    }
                }
            }
        }
    }
}