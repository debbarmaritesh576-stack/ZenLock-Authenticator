package com.aegis.pdf.ui.compress

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.PdfCompressor
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
class CompressViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfCompressor: PdfCompressor,
    private val repository: PdfRepository
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _selectedQuality = MutableStateFlow(1)
    val selectedQuality: StateFlow<Int> = _selectedQuality.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private var inputFile: File? = null

    fun setInputFile(uri: Uri, context: Context) {
        val name = uri.lastPathSegment ?: "unknown.pdf"
        _fileName.value = name
        inputFile = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            inputFile?.outputStream()?.use { output -> input.copyTo(output) }
        }
    }

    fun selectQuality(index: Int) {
        _selectedQuality.value = index
    }

    fun compress() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val input = inputFile ?: throw Exception("No file selected")
                    val outputFile = repository.createOutputFile("compressed")
                    val quality = when (_selectedQuality.value) {
                        0 -> PdfCompressor.Quality.LOW
                        1 -> PdfCompressor.Quality.MEDIUM
                        else -> PdfCompressor.Quality.HIGH
                    }
                    val result = pdfCompressor.compress(input, outputFile, quality)
                    withContext(Dispatchers.Main) {
                        _resultMessage.value = if (result.success) {
                            "Success! Saved: ${outputFile.name}\nSaved: ${result.savedPercentage.toInt()}%"
                        } else "Compression failed"
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

    private fun PdfRepository.createOutputFile(prefix: String): File {
        val dir = File(context.filesDir, "AegisPDF")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${prefix}_${System.currentTimeMillis()}.pdf")
    }
}