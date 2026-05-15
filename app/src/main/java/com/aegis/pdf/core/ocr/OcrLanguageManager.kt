package com.aegis.pdf.core.ocr  
  
import com.google.mlkit.vision.text.TextRecognition  
import com.google.mlkit.vision.text.TextRecognizer  
import com.google.mlkit.vision.text.latin.TextRecognizerOptions  
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions // Hindi support  
import javax.inject.Inject  
  
class OcrLanguageManager @Inject constructor() {  
  
    fun getRecognizer(language: String): TextRecognizer {  
        return when (language) {  
            "HINDI" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())  
            // Add more: Chinese, Japanese, etc.  
            else -> TextRecognition.getClient(TextRecognizerOptions.Builder().build())  
        }  
    }  
}