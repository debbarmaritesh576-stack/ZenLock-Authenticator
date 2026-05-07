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
class AiChatViewModel @Inject constructor(
    private val aiService: AiService,
    private val pdfTextExtractor: PdfTextExtractor,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private var pdfContext: String = ""
    private var inputFile: File? = null

    data class ChatMessage(
        val text: String,
        val isUser: Boolean
    )

    fun loadPdf(uri: Uri, context: Context) {
        _fileName.value = documentDataSource.getFileName(uri)
        inputFile = documentDataSource.copyToTemp(uri)
        _messages.value = emptyList()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                inputFile?.let { file ->
                    pdfContext = pdfTextExtractor.extractText(file)
                }
            }
        }
    }

    fun askQuestion(question: String) {
        _messages.value = _messages.value + ChatMessage(question, true)

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    if (pdfContext.isBlank()) {
                        _messages.value = _messages.value + ChatMessage("No document loaded", false)
                        return@withContext
                    }
                    val answer = aiService.askQuestion(pdfContext, question)
                    _messages.value = _messages.value + ChatMessage(answer, false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Error: ${e.message}", false)
            } finally {
                _isProcessing.value = false
            }
        }
    }
}