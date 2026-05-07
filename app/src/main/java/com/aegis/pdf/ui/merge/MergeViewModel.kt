package com.aegis.pdf.ui.split

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.domain.usecase.SplitPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitViewModel @Inject constructor(
    private val splitUseCase: SplitPdfUseCase,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private var inputUri: Uri? = null

    fun setInputFile(uri: Uri) {
        inputUri = uri
        _fileName.value = documentDataSource.getFileName(uri)
    }

    fun splitAllPages() {
        viewModelScope.launch {
            _isProcessing.value = true
            val uri = inputUri
            if (uri == null) {
                _resultMessage.value = "No file selected"
                _isProcessing.value = false
                return@launch
            }
            when (val result = splitUseCase(uri)) {
                is SplitPdfUseCase.Result.Success -> {
                    _resultMessage.value = "Split complete! ${result.files.size} pages saved."
                }
                is SplitPdfUseCase.Result.Error -> {
                    _resultMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun clearResult() { _resultMessage.value = null }
}