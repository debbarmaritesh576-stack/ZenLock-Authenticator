package com.aegis.pdf.features.convert

import android.net.Uri
import android.content.Context

object ConvertUtils {
    fun estimateSize(inputUri: Uri, type: ConvertType, context: Context): String {
        val inputSize = try { context.contentResolver.openFileDescriptor(inputUri, "r")?.statSize ?: 0 } catch (e: Exception) { 0L }
        val ratio = when (type) {
            ConvertType.PDF_TO_WORD -> 0.8; ConvertType.PDF_TO_EXCEL -> 0.6; ConvertType.PDF_TO_IMAGE -> 2.5
            ConvertType.PDF_TO_HTML -> 0.7; ConvertType.PDF_TO_TEXT -> 0.3; ConvertType.IMAGE_TO_PDF -> 1.2; else -> 1.0
        }
        return formatBytes((inputSize * ratio).toLong())
    }

    fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"; bytes < 1048576 -> "${bytes / 1024} KB"; else -> "${bytes / 1048576} MB"
    }

    fun getMimeType(type: ConvertType): String = when (type) {
        ConvertType.PDF_TO_WORD -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ConvertType.PDF_TO_EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ConvertType.PDF_TO_IMAGE -> "image/png"; ConvertType.PDF_TO_HTML -> "text/html"
        ConvertType.PDF_TO_TEXT -> "text/plain"; else -> "application/pdf"
    }
}