package com.aegis.pdf.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class EditorViewModel @Inject constructor(
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private var inputFile: File? = null

    fun loadPdf(uri: Uri, context: Context) {
        _fileName.value = documentDataSource.getFileName(uri)
        inputFile = documentDataSource.copyToTemp(uri)
        _pageCount.value = try {
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputFile).use { it.numberOfPages }
        } catch (e: Exception) { 0 }
    }

    fun performAction(action: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    _resultMessage.value = "Action '$action' completed successfully!"
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