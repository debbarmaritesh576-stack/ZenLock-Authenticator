package com.aegis.pdf.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.data.repository.PdfRepository
import com.aegis.pdf.data.repository.RecentFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PdfRepository
) : ViewModel() {

    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()

    val tools = listOf(
        Tool("Merge PDF", "Combine multiple PDFs", "merge"),
        Tool("Split PDF", "Extract or remove pages", "split"),
        Tool("Compress", "Reduce file size", "compress"),
        Tool("PDF to Image", "Convert pages to images", "pdf_to_image"),
        Tool("Image to PDF", "Create PDF from images", "image_to_pdf"),
        Tool("View PDF", "Open and read PDF", "viewer"),
        Tool("Scanner", "Scan documents", "scanner"),
        Tool("Password", "Protect or unlock", "security"),
        Tool("Watermark", "Add text or image mark", "watermark")
    )

    data class Tool(
        val name: String,
        val description: String,
        val route: String
    )

    init {
        loadRecentFiles()
    }

    fun loadRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _recentFiles.value = repository.getRecentFiles()
        }
    }

    fun onToolClick(tool: Tool) {
        // Navigation handled by NavController in HomeScreen
    }
}