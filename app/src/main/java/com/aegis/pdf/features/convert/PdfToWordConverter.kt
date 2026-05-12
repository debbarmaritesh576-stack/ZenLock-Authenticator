package com.aegis.pdf.features.convert

import android.content.Context
import android.net.Uri
import com.aegis.pdf.core.NativeBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToWordConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeBridge: NativeBridge
) {
    suspend fun convert(inputUri: Uri): Uri? {
        val inputPath = copyToCache(inputUri) ?: return null
        val outputPath = inputPath.replace(".pdf", ".docx")

        try {
            nativeBridge.convertPdfToWord(inputPath, outputPath)
            return Uri.parse("file://$outputPath")
        } catch (e: Exception) {
            throw ConversionException("PDF to Word failed: ${e.message}")
        }
    }

    private fun copyToCache(uri: Uri): String? {
        val file = java.io.File(context.cacheDir, "input_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}

class ConversionException(message: String) : Exception(message)