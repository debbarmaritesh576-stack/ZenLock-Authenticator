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
import com.aegis.pdf.features.scanner.export.PdfExportManager

@Singleton
class ScanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfExportManager: PdfExportManager
) {
    private val TAG = "ScanRepository"
    private val scansDir = File(context.filesDir, "scans")

    init {
        scansDir.mkdirs()
    }

    suspend fun saveFrame(bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "frame_${System.currentTimeMillis()}.jpg"
            val file = File(scansDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            Log.d(TAG, "Frame saved: $fileName")
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save frame", e)
            null
        }
    }

    suspend fun createPdfFromScans(
        bitmaps: List<Bitmap>,
        password: String? = null,
        compress: Boolean = true,
        addMetadata: Boolean = true
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val metadata = if (addMetadata) {
                mapOf(
                    "title" to "Scanned Document",
                    "author" to "Aegis PDF",
                    "subject" to "Multi-page Scan",
                    "keywords" to "scan, document, multi-page"
                )
            } else {
                null
            }

            val pdfUri = pdfExportManager.createPdfFromBitmaps(
                bitmaps = bitmaps,
                fileName = "document_${System.currentTimeMillis()}.pdf",
                password = password,
                compress = compress,
                addWatermark = false,
                metadata = metadata,
                enableOcr = false
            )

            Log.d(TAG, "PDF created from ${bitmaps.size} scans: $pdfUri")
            pdfUri
        } catch (e: Exception) {
            Log.e(TAG, "PDF creation failed", e)
            null
        }
    }

    suspend fun exportAsImages(
        bitmaps: List<Bitmap>,
        format: String = "JPEG"
    ): List<Uri> = withContext(Dispatchers.IO) {
        try {
            val uris = pdfExportManager.exportAsImages(bitmaps, format, 90)
            Log.d(TAG, "Images exported: ${uris.size} files")
            uris
        } catch (e: Exception) {
            Log.e(TAG, "Image export failed", e)
            emptyList()
        }
    }

    fun getSavedScans(): List<Uri> {
        return try {
            scansDir.listFiles()?.map { Uri.fromFile(it) } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get saved scans", e)
            emptyList()
        }
    }

    fun deleteScan(uri: Uri): Boolean {
        return try {
            val file = File(uri.path ?: return false)
            file.delete()
            Log.d(TAG, "Scan deleted: $uri")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete scan", e)
            false
        }
    }

    fun clearAllScans() {
        try {
            scansDir.listFiles()?.forEach { it.delete() }
            Log.d(TAG, "All scans cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear scans", e)
        }
    }

    suspend fun getScanSize(uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            val file = File(uri.path ?: return@withContext 0L)
            file.length()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get scan size", e)
            0L
        }
    }

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${String.format("%.2f", bytes.toDouble() / (1024 * 1024 * 1024))} GB"
    }
}