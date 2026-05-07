package com.aegis.pdf.ui.scanner

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.scanner.DocumentScanner
import com.aegis.pdf.data.repository.PdfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val documentScanner: DocumentScanner,
    private val repository: PdfRepository
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> = _capturedImage.asStateFlow()

    private var tempFile: File? = null

    fun onImageCaptured(bitmap: Bitmap, context: Context) {
        _capturedImage.value = bitmap
        tempFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    }

    fun enhanceAndSave(context: Context) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val input = tempFile ?: throw Exception("No image captured")
                    val outputFile = repository.createOutputFile("scanned")
                    val enhancedFile = File(context.cacheDir, "enhanced_${System.currentTimeMillis()}.jpg")

                    val success = documentScanner.enhanceDocument(input, enhancedFile)
                    if (success) {
                        val enhancedBitmap = BitmapFactory.decodeFile(enhancedFile.absolutePath)
                        val pdfFile = repository.createOutputFile("document")
                        convertToPdf(enhancedBitmap, pdfFile)
                        enhancedBitmap.recycle()
                        enhancedFile.delete()
                    }
                    withContext(Dispatchers.Main) {
                        _resultMessage.value = if (success) "Saved: ${outputFile.name}" else "Failed to enhance"
                    }
                }
            } catch (e: Exception) {
                _resultMessage.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun convertToPdf(bitmap: Bitmap, outputFile: File) {
        val document = com.tom_roush.pdfbox.pdmodel.PDDocument()
        val page = com.tom_roush.pdfbox.pdmodel.PDPage(
            com.tom_roush.pdfbox.pdmodel.common.PDRectangle(
                bitmap.width.toFloat(),
                bitmap.height.toFloat()
            )
        )
        document.addPage(page)
        val tempJpg = File.createTempFile("pdfimg", ".jpg")
        FileOutputStream(tempJpg).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        val pdImage = com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
            .createFromFile(tempJpg.absolutePath, document)
        com.tom_roush.pdfbox.pdmodel.PDPageContentStream(document, page).use { cs ->
            cs.drawImage(pdImage, 0f, 0f)
        }
        document.save(outputFile)
        document.close()
        tempJpg.delete()
    }

    fun clearImage() {
        _capturedImage.value = null
        tempFile?.delete()
        tempFile = null
    }

    fun clearResult() {
        _resultMessage.value = null
    }
}