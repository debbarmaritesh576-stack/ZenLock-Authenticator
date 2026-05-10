package com.aegis.pdf.core

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeRenderer @Inject constructor(
    private val nativeBridge: NativeBridge
) {

    companion object {
        private const val TAG = "AegisRenderer"
    }

    private var isGLInitialized = false
    private var currentDocPtr: Long = -1L
    private var currentPage: Int = -1
    private var zoomLevel: Float = 1.0f
    private var panX: Float = 0f
    private var panY: Float = 0f

    // ========== GL Surface Rendering ==========

    private external fun nativeInitGL(surface: Surface): Boolean
    private external fun nativeRenderGL(docPtr: Long, pageNum: Int, zoom: Float, panX: Float, panY: Float): Boolean
    private external fun nativeResizeGL(width: Int, height: Int)
    private external fun nativeReleaseGL()

    fun initGL(surface: Surface): Boolean {
        return try {
            val result = nativeInitGL(surface)
            isGLInitialized = result
            Log.i(TAG, "GL initialized: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing GL", e)
            false
        }
    }

    fun initGL(surfaceTexture: SurfaceTexture): Boolean {
        return initGL(Surface(surfaceTexture))
    }

    fun renderGL(docPtr: Long, pageNum: Int): Boolean {
        if (!isGLInitialized) return false
        
        currentDocPtr = docPtr
        currentPage = pageNum
        
        return try {
            nativeRenderGL(docPtr, pageNum, zoomLevel, panX, panY)
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering GL", e)
            false
        }
    }

    fun resizeGL(width: Int, height: Int) {
        try {
            nativeResizeGL(width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Error resizing GL", e)
        }
    }

    fun releaseGL() {
        try {
            nativeReleaseGL()
            isGLInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing GL", e)
        }
    }

    // ========== Zoom & Pan ==========

    fun setZoom(zoom: Float) {
        zoomLevel = zoom.coerceIn(0.1f, 10.0f)
    }

    fun setPan(x: Float, y: Float) {
        panX = x
        panY = y
    }

    fun getZoom(): Float = zoomLevel
    fun getPanX(): Float = panX
    fun getPanY(): Float = panY

    // ========== Bitmap Rendering (Fallback) ==========

    suspend fun renderToBitmap(
        docPtr: Long,
        pageNum: Int,
        width: Int,
        height: Int
    ): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val success = nativeBridge.renderPage(docPtr, pageNum, bitmap, width, height)
            if (success) bitmap else null
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering to bitmap", e)
            null
        }
    }

    // ========== Progressive Rendering ==========

    suspend fun renderProgressive(
        docPtr: Long,
        pageNum: Int,
        targetWidth: Int,
        targetHeight: Int,
        onLowResReady: (Bitmap) -> Unit,
        onFullResReady: (Bitmap) -> Unit
    ) {
        // Step 1: Render low-res first (fast)
        val lowResBitmap = renderToBitmap(
            docPtr, pageNum,
            targetWidth / 4, targetHeight / 4
        )
        lowResBitmap?.let { onLowResReady(it) }

        // Step 2: Render full-res (slower)
        val fullResBitmap = renderToBitmap(
            docPtr, pageNum,
            targetWidth, targetHeight
        )
        fullResBitmap?.let { onFullResReady(it) }

        // Cleanup low-res
        lowResBitmap?.recycle()
    }

    // ========== Cleanup ==========

    fun cleanup() {
        releaseGL()
        currentDocPtr = -1L
        currentPage = -1
        zoomLevel = 1.0f
        panX = 0f
        panY = 0f
    }
}