package com.aegis.pdf.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.data.repository.PdfRepository
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
class PageNumberViewModel @Inject constructor(
    private val documentDataSource: DocumentDataSource,
    private val repository: PdfRepository
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private var inputFile: File? = null

    fun loadPdf(uri: Uri, context: Context) {
        _fileName.value = documentDataSource.getFileName(uri)
        inputFile = documentDataSource.copyToTemp(uri)
        _pageCount.value = try {
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputFile).use { it.numberOfPages }
        } catch (e: Exception) { 0 }
    }

    fun addPageNumbers(position: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val outputFile = repository.createOutputFile("numbered")
                    com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputFile).use { document ->
                        document.pages.forEachIndexed { index, page ->
                            val cs = com.tom_roush.pdfbox.pdmodel.PDPageContentStream(
                                document, page,
                                com.tom_roush.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND,
                                true
                            )
                            cs.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 10f)
                            cs.beginText()
                            val pageNum = (index + 1).toString()
                            val width = page.mediaBox.width
                            val height = page.mediaBox.height

                            val (x, y) = when (position) {
                                "top-left" -> 50f to height - 30f
                                "top-right" -> width - 50f to height - 30f
                                "bottom-left" -> 50f to 30f
                                "bottom-right" -> width - 50f to 30f
                                else -> width / 2 to 30f
                            }
                            cs.newLineAtOffset(x, y)
                            cs.showText(pageNum)
                            cs.endText()
                            cs.close()
                        }
                        document.save(outputFile)
                    }
                    withContext(Dispatchers.Main) {
                        _resultMessage.value = "Saved: ${outputFile.name}"
                    }
                }
            } catch (e: Exception) {
                _resultMessage.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearResult() { _resultMessage.value = null }
}