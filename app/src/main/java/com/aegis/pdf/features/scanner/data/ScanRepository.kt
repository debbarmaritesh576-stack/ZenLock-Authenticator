package com.aegis.pdf.features.scanner.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream
import com.aegis.pdf.features.scanner.model.ScanResult
import com.aegis.pdf.features.scanner.model.ScanSettings

@Singleton
class ScanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentDetectionEngine: com.aegis.pdf.features.scanner.engine.DocumentDetectionEngine,
    private val documentEnhancementEngine: com.aegis.pdf.features.scanner.engine.DocumentEnhancementEngine
) {
    private val TAG = "ScanRepository"
    private val scanDir = File(context.cacheDir, "scans")

    init {
        scanDir.mkdirs()
    }

    suspend fun processFrame(
        bitmap: Bitmap,
        settings: ScanSettings
    ): Pair<Bitmap, com.aegis.pdf.features.scanner.model.DocumentBounds?> = withContext(Dispatchers.Default) {
        try {
            val bounds = documentDetectionEngine.detectDocumentBounds(bitmap)
            Log.d(TAG, "Document bounds detected: ${bounds != null}")
            Pair(bitmap, bounds)
        } catch (e: Exception) {
            Log.e(TAG, "Frame processing failed", e)
            throw e
        }
    }

    suspend fun enhanceAndSave(
        bitmap: Bitmap,
        bounds: com.aegis.pdf.features.scanner.model.DocumentBounds,
        settings: ScanSettings
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val result = documentEnhancementEngine.enhanceDocument(bitmap, bounds, settings)
            val fileName = "scan_${System.currentTimeMillis()}.jpg"
            val file = File(scanDir, fileName)
            
            FileOutputStream(file).use { out ->
                result.processedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            
            Log.d(TAG, "Scan saved: ${file.absolutePath}, quality: ${result.quality}")
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enhance and save scan", e)
            null
        }
    }

    suspend fun createPdfFromScans(scanUris: List<Uri>): Uri? = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(scanDir, "document_${System.currentTimeMillis()}.pdf")
            
            Log.d(TAG, "PDF created: ${outputFile.absolutePath} from ${scanUris.size} scans")
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create PDF from scans", e)
            null
        }
    }

    fun getScanHistory(): List<Uri> {
        return scanDir.listFiles()?.map { Uri.fromFile(it) } ?: emptyList()
    }

    fun clearScans() {
        scanDir.listFiles()?.forEach { it.delete() }
        Log.d(TAG, "Scans cleared")
    }
}