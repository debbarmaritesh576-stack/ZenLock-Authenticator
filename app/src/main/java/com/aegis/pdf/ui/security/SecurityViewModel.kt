package com.aegis.pdf.ui.security

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.PdfEncryptor
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.data.repository.PdfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val pdfEncryptor: PdfEncryptor,
    private val documentDataSource: DocumentDataSource,
    private val repository: PdfRepository
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _mode = MutableStateFlow("protect")
    val mode: StateFlow<String> = _mode.asStateFlow()

    private var inputUri: Uri? = null

    fun setInputFile(uri: Uri) {
        inputUri = uri
        _fileName.value = documentDataSource.getFileName(uri)
    }

    fun setMode(mode: String) {
        _mode.value = mode
    }

    fun process(password: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val inputFile = documentDataSource.copyToTemp(inputUri!!)!!
                    val outputFile = repository.createOutputFile(
                        if (_mode.value == "protect") "protected" else "unlocked"
                    )
                    val success = if (_mode.value == "protect") {
                        pdfEncryptor.addPassword(inputFile, outputFile, password)
                    } else {
                        pdfEncryptor.removePassword(inputFile, outputFile, password)
                    }
                    documentDataSource.deleteAll(inputFile)
                    withContext(Dispatchers.Main) {
                        _resultMessage.value = if (success) {
                            "Success! File saved: ${outputFile.name}"
                        } else "Operation failed. Check password."
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