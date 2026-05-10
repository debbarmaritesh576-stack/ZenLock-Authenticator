package com.aegis.pdf.core

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeBridge @Inject constructor() {

    companion object {
        private const val TAG = "AegisNative"
        private var isLoaded = false

        init {
            try {
                System.loadLibrary("aegis_pdf_engine")
                isLoaded = true
                Log.i(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library: ${e.message}")
                isLoaded = false
            }
        }
    }

    // ========== Document Management ==========

    private external fun nativeOpenDocument(path: String): Long
    private external fun nativeCloseDocument(docPtr: Long)
    private external fun nativeGetPageCount(docPtr: Long): Int
    private external fun nativeGetDocumentInfo(docPtr: Long): String

    suspend fun openDocument(path: String): Long {
        return withContext(Dispatchers.IO) {
            if (!isLoaded) return@withContext -1L
            try {
                nativeOpenDocument(path)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening document: $path", e)
                -1L
            }
        }
    }

    fun closeDocument(docPtr: Long) {
        if (!isLoaded || docPtr <= 0) return
        try {
            nativeCloseDocument(docPtr)
        } catch (e: Exception) {
            Log.e(TAG, "Error closing document", e)
        }
    }

    suspend fun getPageCount(docPtr: Long): Int {
        return withContext(Dispatchers.IO) {
            if (!isLoaded || docPtr <= 0) return@withContext 0
            try {
                nativeGetPageCount(docPtr)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting page count", e)
                0
            }
        }
    }

    fun getDocumentInfo(docPtr: Long): String {
        if (!isLoaded || docPtr <= 0) return "{}"
        return try {
            nativeGetDocumentInfo(docPtr)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document info", e)
            "{}"
        }
    }

    // ========== Page Rendering ==========

    private external fun nativeRenderPage(
        docPtr: Long,
        pageNum: Int,
        bitmap: Bitmap,
        width: Int,
        height: Int
    ): Boolean

    private external fun nativeRenderTile(
        docPtr: Long,
        pageNum: Int,
        tileX: Int,
        tileY: Int,
        tileW: Int,
        tileH: Int,
        scale: Float
    ): ByteArray?

    suspend fun renderPage(
        docPtr: Long,
        pageNum: Int,
        bitmap: Bitmap,
        width: Int = bitmap.width,
        height: Int = bitmap.height
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isLoaded || docPtr <= 0 || bitmap.isRecycled) return@withContext false
            try {
                nativeRenderPage(docPtr, pageNum, bitmap, width, height)
            } catch (e: Exception) {
                Log.e(TAG, "Error rendering page $pageNum", e)
                false
            }
        }
    }

    fun renderTile(
        docPtr: Long,
        pageNum: Int,
        tileX: Int,
        tileY: Int,
        tileW: Int,
        tileH: Int,
        scale: Float = 1.0f
    ): ByteArray? {
        if (!isLoaded || docPtr <= 0) return null
        return try {
            nativeRenderTile(docPtr, pageNum, tileX, tileY, tileW, tileH, scale)
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering tile", e)
            null
        }
    }

    // ========== Preloading ==========

    private external fun nativePreloadPage(docPtr: Long, pageNum: Int)
    private external fun nativeCancelPreload(docPtr: Long)

    fun preloadPage(docPtr: Long, pageNum: Int) {
        if (!isLoaded || docPtr <= 0) return
        try {
            nativePreloadPage(docPtr, pageNum)
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading page", e)
        }
    }

    fun cancelPreload(docPtr: Long) {
        if (!isLoaded || docPtr <= 0) return
        try {
            nativeCancelPreload(docPtr)
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling preload", e)
        }
    }

    // ========== Search ==========

    private external fun nativeSearchInDocument(
        docPtr: Long,
        query: String,
        caseSensitive: Boolean
    ): IntArray

    fun searchInDocument(
        docPtr: Long,
        query: String,
        caseSensitive: Boolean = false
    ): List<Int> {
        if (!isLoaded || docPtr <= 0 || query.isBlank()) return emptyList()
        return try {
            nativeSearchInDocument(docPtr, query, caseSensitive).toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching document", e)
            emptyList()
        }
    }

    // ========== Text Extraction ==========

    private external fun nativeExtractText(docPtr: Long, pageNum: Int): String

    fun extractText(docPtr: Long, pageNum: Int): String {
        if (!isLoaded || docPtr <= 0) return ""
        return try {
            nativeExtractText(docPtr, pageNum)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text", e)
            ""
        }
    }

    // ========== PDF Writing ==========

    private external fun nativeCreateDocument(path: String): Long
    private external fun nativeAddPage(docPtr: Long, width: Float, height: Float): Int
    private external fun nativeAddText(docPtr: Long, pageNum: Int, text: String, x: Float, y: Float, fontSize: Float)
    private external fun nativeAddImage(docPtr: Long, pageNum: Int, imagePath: String, x: Float, y: Float, w: Float, h: Float)
    private external fun nativeSaveDocument(docPtr: Long, path: String): Boolean

    fun createDocument(path: String): Long {
        if (!isLoaded) return -1L
        return try {
            nativeCreateDocument(path)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating document", e)
            -1L
        }
    }

    fun addPage(docPtr: Long, width: Float = 612f, height: Float = 792f): Int {
        if (!isLoaded || docPtr <= 0) return -1
        return try {
            nativeAddPage(docPtr, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding page", e)
            -1
        }
    }

    fun addText(docPtr: Long, pageNum: Int, text: String, x: Float, y: Float, fontSize: Float = 12f) {
        if (!isLoaded || docPtr <= 0) return
        try {
            nativeAddText(docPtr, pageNum, text, x, y, fontSize)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding text", e)
        }
    }

    fun addImage(docPtr: Long, pageNum: Int, imagePath: String, x: Float, y: Float, w: Float, h: Float) {
        if (!isLoaded || docPtr <= 0) return
        try {
            nativeAddImage(docPtr, pageNum, imagePath, x, y, w, h)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding image", e)
        }
    }

    fun saveDocument(docPtr: Long, path: String): Boolean {
        if (!isLoaded || docPtr <= 0) return false
        return try {
            nativeSaveDocument(docPtr, path)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving document", e)
            false
        }
    }

    // ========== Encryption ==========

    private external fun nativeEncryptPdf(inputPath: String, outputPath: String, password: String): Boolean
    private external fun nativeDecryptPdf(inputPath: String, outputPath: String, password: String): Boolean
    private external fun nativeCheckPassword(inputPath: String, password: String): Boolean

    fun encryptPdf(inputPath: String, outputPath: String, password: String): Boolean {
        if (!isLoaded) return false
        return try {
            nativeEncryptPdf(inputPath, outputPath, password)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting PDF", e)
            false
        }
    }

    fun decryptPdf(inputPath: String, outputPath: String, password: String): Boolean {
        if (!isLoaded) return false
        return try {
            nativeDecryptPdf(inputPath, outputPath, password)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting PDF", e)
            false
        }
    }

    fun checkPassword(inputPath: String, password: String): Boolean {
        if (!isLoaded) return false
        return try {
            nativeCheckPassword(inputPath, password)
        } catch (e: Exception) {
            false
        }
    }

    // ========== Repair ==========

    private external fun nativeRepairPdf(inputPath: String, outputPath: String): Boolean

    fun repairPdf(inputPath: String, outputPath: String): Boolean {
        if (!isLoaded) return false
        return try {
            nativeRepairPdf(inputPath, outputPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error repairing PDF", e)
            false
        }
    }

    // ========== Utilities ==========

    fun isNativeAvailable(): Boolean = isLoaded

    fun getVersion(): String {
        return if (isLoaded) "1.0.0" else "N/A"
    }
}