package com.aegis.pdf.core.ocr  
  
sealed class OcrResult {  
    object Idle : OcrResult()  
    object Processing : OcrResult()  
    data class Success(val text: String) : OcrResult()  
    data class Error(val message: String) : OcrResult()  
}