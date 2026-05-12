package com.aegis.pdf.features.convert

import android.content.Context
import android.net.Uri
import com.aegis.pdf.core.NativeBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToExcelConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeBridge: NativeBridge
) {
    suspend fun convert(inputUri: Uri): Uri? {
        val inputPath = copyToCache(inputUri) ?: throw ConversionException("Cannot read input file")
        val outputPath = inputPath.replace(".pdf", ".xlsx")

        try {
            nativeBridge.convertPdfToExcel(inputPath, outputPath)
            return Uri.fromFile(java.io.File(outputPath))
        } catch (e: Exception) {
            throw ConversionException("PDF to Excel failed: ${e.message}")
        }
    }

    private fun copyToCache(uri: Uri): String? {
        val file = java.io.File(context.cacheDir, "excel_input_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}