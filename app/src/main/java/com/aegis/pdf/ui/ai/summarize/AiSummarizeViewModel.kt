package com.aegis.pdf.ui.ai

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.ai.AiService
import com.aegis.pdf.core.pdf.PdfTextExtractor
import com.aegis.pdf.data.local.DocumentDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AiSummarizeViewModel @Inject constructor(
    private val aiService: AiService,
    private val pdfTextExtractor: PdfTextExtractor,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private var inputFile: File? = null

    fun loadPdf(uri: Uri, context: Context) {
        _fileName.value = documentDataSource.getFileName(uri)
        inputFile = documentDataSource.copyToTemp(uri)
        _summary.value = ""
    }

    fun summarize(maxLength: Int) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val file = inputFile ?: throw Exception("No file loaded")
                    val text = pdfTextExtractor.extractText(file)
                    if (text.isBlank()) {
                        _summary.value = "No text found in PDF"
                        return@withContext
                    }
                    val result = aiService.summarizeText(text, maxLength)
                    withContext(Dispatchers.Main) {
                        _summary.value = result
                    }
                }
            } catch (e: Exception) {
                _summary.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
}