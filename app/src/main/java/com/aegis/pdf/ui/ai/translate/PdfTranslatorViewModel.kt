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
class PdfTranslatorViewModel @Inject constructor(
    private val aiService: AiService,
    private val pdfTextExtractor: PdfTextExtractor,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private var extractedText: String = ""
    private var inputFile: File? = null

    fun loadPdf(uri: Uri, context: Context) {
        _fileName.value = documentDataSource.getFileName(uri)
        inputFile = documentDataSource.copyToTemp(uri)
        _translatedText.value = ""
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                inputFile?.let { file ->
                    extractedText = pdfTextExtractor.extractText(file)
                }
            }
        }
    }

    fun translate(targetLanguage: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    if (extractedText.isBlank()) {
                        _translatedText.value = "No text found in PDF"
                        return@withContext
                    }
                    val languageNames = mapOf(
                        "hi" to "Hindi", "es" to "Spanish", "fr" to "French",
                        "de" to "German", "ja" to "Japanese", "zh" to "Chinese",
                        "ar" to "Arabic", "ru" to "Russian", "ko" to "Korean"
                    )
                    val result = aiService.translateText(
                        extractedText,
                        languageNames[targetLanguage] ?: targetLanguage
                    )
                    withContext(Dispatchers.Main) {
                        _translatedText.value = result
                    }
                }
            } catch (e: Exception) {
                _translatedText.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
}