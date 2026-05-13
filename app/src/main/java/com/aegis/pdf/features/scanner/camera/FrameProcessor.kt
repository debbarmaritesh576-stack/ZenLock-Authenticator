package com.aegis.pdf.features.scanner.camera

import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import com.aegis.pdf.features.scanner.opencv.OpenCvDocumentDetector

@Singleton
class FrameProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openCvDetector: OpenCvDocumentDetector
) {
    private val TAG = "FrameProcessor"
    private var lastFrameTime = 0L
    private val frameInterval = 100L

    suspend fun processFrame(bitmap: Bitmap) = withContext(Dispatchers.Default) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFrameTime < frameInterval) {
            return@withContext
        }
        lastFrameTime = currentTime

        try {
            val bounds = openCvDetector.detectDocument(bitmap)
            Log.d(TAG, "Frame processed: bounds detected = ${bounds != null}")
        } catch (e: Exception) {
            Log.e(TAG, "Frame processing failed", e)
        }
    }
}