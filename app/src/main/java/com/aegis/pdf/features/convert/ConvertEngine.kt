package com.aegis.pdf.features.convert

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.aegis.pdf.core.NativeBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class ConvertResult {
    data class Success(val uris: List<Uri>) : ConvertResult()
    data class Error(val type: ConvertErrorType, val message: String, val throwable: Throwable? = null) : ConvertResult()
}

enum class ConvertErrorType {
    FILE_NOT_FOUND, INVALID_FORMAT, CONVERSION_FAILED, OUT_OF_MEMORY, CANCELLED, UNKNOWN
}

enum class ConvertType(val inputMime: String, val outputExt: String) {
    PDF_TO_WORD("application/pdf", ".docx"),
    PDF_TO_EXCEL("application/pdf", ".xlsx"),
    PDF_TO_PPT("application/pdf", ".pptx"),
    PDF_TO_IMAGE("application/pdf", ".png"),
    PDF_TO_HTML("application/pdf", ".html"),
    PDF_TO_TEXT("application/pdf", ".txt"),
    IMAGE_TO_PDF("image/*", ".pdf"),
    WORD_TO_PDF("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".pdf"),
    EXCEL_TO_PDF("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".pdf"),
    PPT_TO_PDF("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pdf"),
    HTML_TO_PDF("text/html", ".pdf")
}

@Singleton
class ConvertEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeBridge: NativeBridge
) {
    companion object {
        private const val TAG = "ConvertEngine"
        private const val MAX_BITMAP_WIDTH = 720
        private val EXT_REGEX = Regex("\\.[^.]+$")
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var conversionJob: Job? = null

    suspend fun convert(inputUri: Uri, type: ConvertType): ConvertResult = withContext(Dispatchers.IO) {
        val inputPath = copyToCache(inputUri, type.name.lowercase())
            ?: return@withContext ConvertResult.Error(ConvertErrorType.FILE_NOT_FOUND, "Cannot read input file")

        try {
            val outputPath = getOutputPath(inputPath, type)
            conversionJob?.ensureActive()

            when (type) {
                ConvertType.PDF_TO_WORD -> nativeBridge.convertPdfToWord(inputPath, outputPath)
                ConvertType.PDF_TO_EXCEL -> nativeBridge.convertPdfToExcel(inputPath, outputPath)
                ConvertType.PDF_TO_PPT -> nativeBridge.convertPdfToPpt(inputPath, outputPath)
                ConvertType.PDF_TO_IMAGE -> convertPdfToImages(inputPath, outputPath)
                ConvertType.PDF_TO_HTML -> nativeBridge.convertPdfToHtml(inputPath, outputPath)
                ConvertType.PDF_TO_TEXT -> nativeBridge.convertPdfToText(inputPath, outputPath)
                ConvertType.IMAGE_TO_PDF -> return@withContext ConvertResult.Error(ConvertErrorType.INVALID_FORMAT, "Use convertImagesToPdf()")
                ConvertType.WORD_TO_PDF -> nativeBridge.convertWordToPdf(inputPath, outputPath)
                ConvertType.EXCEL_TO_PDF -> nativeBridge.convertExcelToPdf(inputPath, outputPath)
                ConvertType.PPT_TO_PDF -> nativeBridge.convertPptToPdf(inputPath, outputPath)
                ConvertType.HTML_TO_PDF -> nativeBridge.convertHtmlFileToPdf(inputPath, outputPath)
            }

            cleanupTempFile(inputPath)
            ConvertResult.Success(listOf(getSafeUri(File(outputPath))))
        } catch (e: CancellationException) {
            cleanupTempFile(inputPath)
            ConvertResult.Error(ConvertErrorType.CANCELLED, "Conversion cancelled")
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OOM during conversion", e)
            cleanupTempFile(inputPath)
            ConvertResult.Error(ConvertErrorType.OUT_OF_MEMORY, "File too large", e)
        } catch (e: Exception) {
            Log.e(TAG, "Conversion failed", e)
            cleanupTempFile(inputPath)
            ConvertResult.Error(ConvertErrorType.CONVERSION_FAILED, e.message ?: "Unknown error", e)
        }
    }

    suspend fun convertBatch(
        uris: List<Uri>,
        type: ConvertType,
        onProgress: (Float) -> Unit
    ): List<Uri> = withContext(Dispatchers.IO) {
        val outputs = mutableListOf<Uri>()
        val semaphore = kotlinx.coroutines.sync.Semaphore(2) // Max 2 parallel

        val jobs = uris.mapIndexed { index, uri ->
            async {
                semaphore.withPermit {
                    ensureActive()
                    val result = convert(uri, type)
                    if (result is ConvertResult.Success) {
                        outputs.addAll(result.uris)
                    }
                    onProgress((index + 1f) / uris.size)
                }
            }
        }
        jobs.awaitAll()
        outputs
    }

    suspend fun convertImagesToPdf(imageUris: List<Uri>): ConvertResult = withContext(Dispatchers.IO) {
        val output = File(context.cacheDir, "images_${System.currentTimeMillis()}.pdf")

        try {
            val docPtr = nativeBridge.createDocument(output.absolutePath)
            if (docPtr <= 0) throw Exception("Failed to create PDF")

            try {
                imageUris.forEachIndexed { index, uri ->
                    ensureActive()
                    val imagePath = copyToCache(uri, "img_$index")
                    if (imagePath != null) {
                        nativeBridge.addImage(docPtr, 1, imagePath, 0f, 0f, 612f, 792f)
                        cleanupTempFile(imagePath)
                    }
                }
                nativeBridge.saveDocument(docPtr, output.absolutePath)
            } finally {
                nativeBridge.closeDocument(docPtr)
            }

            ConvertResult.Success(listOf(getSafeUri(output)))
        } catch (e: CancellationException) {
            ConvertResult.Error(ConvertErrorType.CANCELLED, "Cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Image to PDF failed", e)
            ConvertResult.Error(ConvertErrorType.CONVERSION_FAILED, e.message ?: "Failed")
        }
    }

    private suspend fun convertPdfToImages(inputPath: String, outputDir: String): Boolean {
        val docPtr = nativeBridge.openDocument(inputPath)
        try {
            val pageCount = nativeBridge.getPageCount(docPtr)
            File(outputDir).mkdirs()

            for (page in 1..pageCount) {
                ensureActive()

                val width = MAX_BITMAP_WIDTH
                val height = (width * 1.414f).toInt()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                try {
                    nativeBridge.renderPage(docPtr, page, bitmap, width, height)
                    FileOutputStream(File(outputDir, "page_$page.png")).use { out ->
                        BufferedOutputStream(out).use { bos ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos)
                        }
                    }
                } finally {
                    bitmap.recycle()
                }
            }
            return true
        } finally {
            nativeBridge.closeDocument(docPtr)
        }
    }

    private fun copyToCache(uri: Uri, prefix: String): String? {
        return try {
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.tmp")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().buffered().use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Copy to cache failed", e)
            null
        }
    }

    private fun getOutputPath(inputPath: String, type: ConvertType): String {
        return inputPath.replace(EXT_REGEX, "") + "_converted" + type.outputExt
    }

    private fun getSafeUri(file: File): Uri {
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }
    }

    private fun cleanupTempFile(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete temp file: $path")
        }
    }

    fun cancelConversion() {
        conversionJob?.cancel()
    }

    fun cleanupCache() {
        scope.launch {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("pdf_to_") || file.name.startsWith("img_") ||
                    file.name.startsWith("word_") || file.name.startsWith("excel_") ||
                    file.name.startsWith("ppt_") || file.name.startsWith("html_") ||
                    file.name.endsWith(".tmp")) {
                    file.delete()
                }
            }
        }
    }
}