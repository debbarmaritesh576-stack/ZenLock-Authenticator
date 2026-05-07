package com.aegis.pdf.data.repository

import android.content.Context
import com.aegis.pdf.core.pdf.PdfInfo
import com.aegis.pdf.core.pdf.PdfManager
import com.aegis.pdf.data.local.RecentFileDao
import com.aegis.pdf.data.local.RecentFileEntity
import java.io.File

class PdfRepository(
    private val context: Context,
    private val pdfManager: PdfManager,
    private val recentFileDao: RecentFileDao
) {

    suspend fun getRecentFiles(): List<RecentFile> {
        return recentFileDao.getRecentFiles().map { entity ->
            RecentFile(
                name = entity.name,
                path = entity.path,
                size = formatSize(entity.size),
                date = formatDate(entity.lastOpened)
            )
        }
    }

    suspend fun addToRecent(file: File, pageCount: Int = 0) {
        recentFileDao.insertFile(
            RecentFileEntity(
                name = file.name,
                path = file.absolutePath,
                size = file.length(),
                pageCount = pageCount
            )
        )
    }

    fun getOutputDirectory(): File {
        val dir = File(context.filesDir, "AegisPDF")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createOutputFile(prefix: String): File {
        val dir = getOutputDirectory()
        return File(dir, "${prefix}_${System.currentTimeMillis()}.pdf")
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun getPdfInfo(file: File): PdfInfo = pdfManager.getPdfInfo(file)
    fun isValidPdf(file: File): Boolean = pdfManager.isValidPdf(file)
}

data class RecentFile(
    val name: String,
    val path: String,
    val size: String,
    val date: String
)