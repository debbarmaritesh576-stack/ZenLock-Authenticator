package com.aegis.pdf.core.ocr  
  
import android.graphics.Bitmap  
import android.graphics.BitmapFactory  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import java.io.File  
import javax.inject.Inject  
  
class OcrProcessor @Inject constructor(  
    private val ocrEngine: OcrEngine  
) {  
    suspend fun processImageFile(file: File): OcrResult = withContext(Dispatchers.Default) {  
        try {  
            // 1. Optimize Bitmap (Bina crash ke load karna)  
            val options = BitmapFactory.Options().apply {  
                inJustDecodeBounds = true  
            }  
            BitmapFactory.decodeFile(file.absolutePath, options)  
              
            // Image agar bahut badi hai toh use scale down karo  
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)  
            options.inJustDecodeBounds = false  
              
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)  
                ?: return@withContext OcrResult.Error("Failed to decode image")  
  
            // 2. OCR Extraction  
            val text = ocrEngine.extractText(bitmap)  
              
            // 3. Cleanup to free memory  
            bitmap.recycle()  
              
            if (text.startsWith("Error:")) OcrResult.Error(text)  
            else OcrResult.Success(text)  
              
        } catch (e: Exception) {  
            OcrResult.Error(e.message ?: "Unknown OCR Error")  
        }  
    }  
  
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {  
        val (height: Int, width: Int) = options.outHeight to options.outWidth  
        var inSampleSize = 1  
        if (height > reqHeight || width > reqWidth) {  
            val halfHeight = height / 2  
            val halfWidth = width / 2  
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {  
                inSampleSize *= 2  
            }  
        }  
        return inSampleSize  
    }  
}