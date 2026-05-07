package com.aegis.pdf.ui.ocr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.ocr.OcrEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrEngine: OcrEngine
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    fun extractText(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isProcessing.value = true
            _fileName.value = uri.lastPathSegment ?: "image"
            try {
                val bitmap = BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(uri)
                )
                if (bitmap != null) {
                    _extractedText.value = ocrEngine.extractText(bitmap)
                    bitmap.recycle()
                } else {
                    _extractedText.value = "Failed to load image"
                }
            } catch (e: Exception) {
                _extractedText.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun copyToClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OCR Text", _extractedText.value)
        clipboard.setPrimaryClip(clip)
    }
}