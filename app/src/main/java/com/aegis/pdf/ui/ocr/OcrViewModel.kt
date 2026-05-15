package com.aegis.pdf.ui.ocr  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.core.ocr.OcrProcessor  
import com.aegis.pdf.core.ocr.OcrResult  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.launch  
import java.io.File  
import javax.inject.Inject  
  
@HiltViewModel  
class OcrViewModel @Inject constructor(  
    private val ocrProcessor: OcrProcessor  
) : ViewModel() {  
  
    private val _ocrState = MutableStateFlow<OcrResult>(OcrResult.Idle)  
    val ocrState = _ocrState.asStateFlow()  
  
    fun startOcr(file: File) {  
        viewModelScope.launch {  
            _ocrState.value = OcrResult.Processing  
            val result = ocrProcessor.processImageFile(file)  
            _ocrState.value = result  
        }  
    }  
}