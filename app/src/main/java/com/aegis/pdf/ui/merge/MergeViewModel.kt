package com.aegis.pdf.ui.merge

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.PdfMerger
import com.aegis.pdf.data.repository.PdfRepository
import com.aegis.pdf.ui.components.PickedFile
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
class MergeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfMerger: PdfMerger,
    private val repository: PdfRepository
) : ViewModel() {

    private val _files = MutableStateFlow<List<PickedFile>>(emptyList())
    val files: StateFlow<List<PickedFile>> = _files.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    fun addFile(file: PickedFile) {
        _files.value = _files.value + file
    }

    fun removeFile(index: Int) {
        _files.value = _files.value.toMutableList().apply { removeAt(index) }
    }

    fun mergeFiles() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val inputFiles = _files.value.mapNotNull { file ->
                        val uri = android.net.Uri.parse(file.uri)
                        val tempFile = File(context.cacheDir, file.name)
                        if (repository.copyUriToFile(uri, tempFile)) tempFile else null
                    }
                    val outputFile = repository.createOutputFile("merged")
                    val success = pdfMerger.mergePdfFiles(inputFiles, outputFile)
                    inputFiles.forEach { it.delete() }
                    withContext(Dispatchers.Main) {
                        _resultMessage.value = if (success) "PDF merged successfully!\nSaved to: ${outputFile.name}"
                        else "Merge failed. Please try again."
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

    private fun PdfRepository.copyUriToFile(uri: android.net.Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) { false }
    }

    private fun PdfRepository.createOutputFile(prefix: String): File {
        val dir = File(context.filesDir, "AegisPDF")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${prefix}_${System.currentTimeMillis()}.pdf")
    }
}