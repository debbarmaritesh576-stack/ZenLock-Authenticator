package com.aegis.pdf.core.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrEngine @Inject constructor() {

    private val recognizer: TextRecognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder()
            .build()
    )

    suspend fun extractText(bitmap: Bitmap): String {
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text) {}
                }
                .addOnFailureListener { e ->
                    continuation.resume("Error: ${e.message}") {}
                }
        }
    }

    suspend fun extractTextFromFile(file: File): String {
        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            ?: return "Failed to load image"
        return extractText(bitmap)
    }

    fun close() {
        recognizer.close()
    }
}