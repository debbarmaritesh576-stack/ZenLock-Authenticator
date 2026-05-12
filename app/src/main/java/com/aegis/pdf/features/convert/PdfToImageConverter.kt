package com.aegis.pdf.features.convert

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.aegis.pdf.core.NativeBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToImageConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeBridge: NativeBridge
) {
    suspend fun convert(inputUri: Uri, format: String = "PNG"): Uri? {
        val inputPath = copyToCache(inputUri) ?: throw ConversionException("Cannot read input")
        val outputDir = File(context.cacheDir, "pdf_images_${System.currentTimeMillis()}")
        outputDir.mkdirs()

        try {
            val docPtr = nativeBridge.openDocument(inputPath)
            val pageCount = nativeBridge.getPageCount(docPtr)

            for (page in 1..pageCount) {
                val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
                nativeBridge.renderPage(docPtr, page, bitmap, 1080, 1920)

                val imageFile = File(outputDir, "page_$page.${format.lowercase()}")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(
                        if (format == "PNG") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                        90, out
                    )
                }
                bitmap.recycle()
            }
            nativeBridge.closeDocument(docPtr)

            return Uri.fromFile(outputDir)
        } catch (e: Exception) {
            throw ConversionException("PDF to Image failed: ${e.message}")
        }
    }

    private fun copyToCache(uri: Uri): String? {
        val file = File(context.cacheDir, "img_input_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}