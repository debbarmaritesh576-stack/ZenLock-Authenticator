package com.aegis.pdf.features.scanner.ocr

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "OcrProcessor"
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractText(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage).await()

            val extractedText = StringBuilder()
            result.textBlocks.forEach { block ->
                extractedText.append(block.text).append("\n")
            }

            Log.d(TAG, "Text extracted: ${extractedText.length} characters")
            extractedText.toString()
        } catch (e: Exception) {
            Log.e(TAG, "OCR extraction failed", e)
            ""
        }
    }

    suspend fun extractTextWithBlocks(bitmap: Bitmap): Map<String, Any> = withContext(Dispatchers.Default) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage).await()

            val blocks = mutableListOf<Map<String, Any>>()
            result.textBlocks.forEach { block ->
                blocks.add(mapOf(
                    "text" to block.text,
                    "confidence" to (block.confidence ?: 0f),
                    "bounds" to block.boundingBox
                ))
            }

            mapOf(
                "success" to true,
                "text" to result.text,
                "blockCount" to blocks.size,
                "blocks" to blocks
            )
        } catch (e: Exception) {
            Log.e(TAG, "OCR block extraction failed", e)
            mapOf("success" to false, "error" to e.message)
        }
    }

    fun close() {
        try {
            textRecognizer.close()
            Log.d(TAG, "OCR processor closed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close OCR processor", e)
        }
    }
}