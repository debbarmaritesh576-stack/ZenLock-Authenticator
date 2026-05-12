package com.aegis.pdf.features.convert

import android.content.Context
import android.net.Uri
import com.aegis.pdf.core.NativeBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToPowerpointConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeBridge: NativeBridge
) {
    suspend fun convert(inputUri: Uri): Uri? {
        val inputPath = copyToCache(inputUri) ?: throw ConversionException("Cannot read input")
        val outputPath = inputPath.replace(".pdf", ".pptx")

        try {
            nativeBridge.convertPdfToPpt(inputPath, outputPath)
            return Uri.fromFile(java.io.File(outputPath))
        } catch (e: Exception) {
            throw ConversionException("PDF to PPT failed: ${e.message}")
        }
    }

    private fun copyToCache(uri: Uri): String? {
        val file = java.io.File(context.cacheDir, "ppt_input_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}