package com.aegis.pdf.core.pdf  
  
import android.graphics.Bitmap  
import android.graphics.BitmapFactory  
import com.google.mlkit.vision.common.InputImage  
import com.google.mlkit.vision.text.TextRecognition  
import com.google.mlkit.vision.text.latin.TextRecognizerOptions  
import kotlinx.coroutines.tasks.await  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class OcrSearchableEngine @Inject constructor() {  
  
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)  
  
    /**  
     * ADVANCED RAG / CONVERSION UTILITY: Image file ko scan karke string vectors coordinates detect karna  
     * aur un text blocks ko structure mappings format mein clean state delivery dena.  
     */  
    suspend fun extractTextFromImage(imageFile: File): Result<String> {  
        if (!imageFile.exists()) return Result.failure(Exception("Target asset missing"))  
          
        return try {  
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)  
            val image = InputImage.fromBitmap(bitmap, 0)  
              
            // Call Google ML kit background processor task synchronously using coroutine await hooks  
            val resultText = recognizer.process(image).await()  
              
            bitmap.recycle() // Protect RAM from frame heap fragmentation  
            Result.success(resultText.text)  
        } catch (e: Exception) {  
            Result.failure(e)  
        }  
    }  
}