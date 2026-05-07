package com.aegis.pdf.ui.compress

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.PdfCompressor
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.domain.usecase.CompressPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompressViewModel @Inject constructor(
    private val compressUseCase: CompressPdfUseCase,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _selectedQuality = MutableStateFlow(1)
    val selectedQuality: StateFlow<Int> = _selectedQuality.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private var inputUri: Uri? = null

    fun setInputFile(uri: Uri) {
        inputUri = uri
        _fileName.value = documentDataSource.getFileName(uri)
    }

    fun selectQuality(index: Int) { _selectedQuality.value = index }

    fun compress() {
        viewModelScope.launch {
            _isProcessing.value = true
            val quality = when (_selectedQuality.value) {
                0 -> PdfCompressor.Quality.LOW
                1 -> PdfCompressor.Quality.MEDIUM
                else -> PdfCompressor.Quality.HIGH
            }
            val uri = inputUri
            if (uri == null) {
                _resultMessage.value = "No file selected"
                _isProcessing.value = false
                return@launch
            }
            when (val result = compressUseCase(uri, quality)) {
                is CompressPdfUseCase.Result.Success -> {
                    _resultMessage.value = "Compressed! Saved ${result.savedPercentage.toInt()}%"
                }
                is CompressPdfUseCase.Result.Error -> {
                    _resultMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun clearResult() { _resultMessage.value = null }
}