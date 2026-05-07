package com.aegis.pdf.ui.viewer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.PdfManager
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
class PdfViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfManager: PdfManager,
    private val repository: PdfRepository
) : ViewModel() {

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    var inputFile: File? = null
        private set

    fun loadPdf(uri: Uri?, context: Context) {
        if (uri == null) {
            _fileName.value = ""
            _pageCount.value = 0
            inputFile = null
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val name = uri.lastPathSegment ?: "unknown.pdf"
                val file = File(context.cacheDir, name)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                inputFile = file
                _fileName.value = name
                _pageCount.value = pdfManager.getPageCount(file)
                repository.addToRecent(file, _pageCount.value)
            }
        }
    }
}