package com.aegis.pdf.ui.split

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.PdfSplitter
import com.aegis.pdf.data.repository.PdfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SplitViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfSplitter: PdfSplitter,
    private val repository: PdfRepository
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private var inputFile: File? = null

    fun setInputFile(uri: Uri, context: Context) {
        val name = uri.lastPathSegment ?: "unknown.pdf"
        _fileName.value = name
        inputFile = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            inputFile?.outputStream()?.use { output -> input.copyTo(output) }
        }
        _pageCount.value = repository.getPdfInfo(inputFile!!).pageCount
    }

    fun splitAllPages() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val input = inputFile ?: throw Exception("No file selected")
                    val outputDir = File(context.filesDir, "AegisPDF/split_${System.currentTimeMillis()}")
                    outputDir.mkdirs()
                    val files = pdfSplitter.splitAllPages(input, outputDir)
                    withContext(Dispatchers.Main) {
                        _resultMessage.value = "Split complete! ${files.size} pages saved."
                    }
                }
            } catch (e: Exception) {
                _resultMessage.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearResult() {
        _resultMessage.value = null
    }
}