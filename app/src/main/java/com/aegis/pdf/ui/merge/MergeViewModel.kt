package com.aegis.pdf.ui.merge

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.domain.usecase.MergePdfUseCase
import com.aegis.pdf.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MergeViewModel @Inject constructor(
    private val mergeUseCase: MergePdfUseCase,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    data class PickedFile(val uri: Uri, val name: String, val size: String)

    private val _files = MutableStateFlow<List<PickedFile>>(emptyList())
    val files: StateFlow<List<PickedFile>> = _files.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    fun addFile(uri: Uri) {
        val name = documentDataSource.getFileName(uri)
        val size = FileUtils.formatSize(documentDataSource.getFileSize(uri))
        _files.value = _files.value + PickedFile(uri, name, size)
    }

    fun removeFile(index: Int) {
        _files.value = _files.value.toMutableList().apply { removeAt(index) }
    }

    fun mergeFiles() {
        viewModelScope.launch {
            _isProcessing.value = true
            when (val result = mergeUseCase(_files.value.map { it.uri })) {
                is MergePdfUseCase.Result.Success -> {
                    _resultMessage.value = "PDF merged successfully!\nFile: ${result.outputFile.name}"
                    _files.value = emptyList()
                }
                is MergePdfUseCase.Result.Error -> {
                    _resultMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun clearResult() { _resultMessage.value = null }
}