package com.aegis.pdf.features.convert

import android.content.Context
import android.net.Uri
import com.aegis.pdf.core.NativeBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageToPdfConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeBridge: NativeBridge
) {
    suspend fun convert(imageUris: List<Uri>): Uri? {
        if (imageUris.isEmpty()) throw ConversionException("No images selected")

        val outputPath = File(context.cacheDir, "created_${System.currentTimeMillis()}.pdf").absolutePath

        try {
            val docPtr = nativeBridge.createDocument(outputPath)
            if (docPtr <= 0) throw ConversionException("Failed to create PDF")

            imageUris.forEach { uri ->
                val imagePath = copyToCache(uri) ?: return@forEach
                nativeBridge.addImage(docPtr, 1, imagePath, 0f, 0f, 612f, 792f)
            }

            nativeBridge.saveDocument(docPtr, outputPath)
            nativeBridge.closeDocument(docPtr)

            return Uri.fromFile(File(outputPath))
        } catch (e: Exception) {
            throw ConversionException("Image to PDF failed: ${e.message}")
        }
    }

    private fun copyToCache(uri: Uri): String? {
        val file = File(context.cacheDir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}