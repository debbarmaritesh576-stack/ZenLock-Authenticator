package com.aegis.pdf.features.scanner.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frameProcessor: FrameProcessor
) {
    private val TAG = "ImageAnalyzer"

    fun analyzFrame(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            bitmap?.let {
                frameProcessor.processFrame(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Frame analysis failed", e)
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val image = imageProxy.image ?: return null

            val planes = image.planes
            val buffer = planes[0].buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert ImageProxy to Bitmap", e)
            null
        }
    }
}