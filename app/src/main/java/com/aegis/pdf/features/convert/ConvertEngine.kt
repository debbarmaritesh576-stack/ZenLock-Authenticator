package com.aegis.pdf.features.convert  
  
import android.content.Context  
import android.graphics.Bitmap  
import android.net.Uri  
import android.util.Log  
import com.aegis.pdf.core.NativeBridge  
import dagger.hilt.android.qualifiers.ApplicationContext  
import kotlinx.coroutines.*  
import kotlinx.coroutines.sync.Semaphore  
import kotlinx.coroutines.sync.withPermit  
import java.io.BufferedOutputStream  
import java.io.File  
import java.io.FileOutputStream  
import javax.inject.Inject  
import javax.inject.Singleton  
  
/**  
 * Aegis ConvertEngine: Handling 300+ file architecture with Native C++ Core  
 */  
@Singleton  
class ConvertEngine @Inject constructor(  
    @ApplicationContext private val context: Context,  
    private val nativeBridge: NativeBridge  
) {  
    companion object {  
        private const val TAG = "AegisConvertEngine"  
        private const val DEFAULT_QUALITY = 90  
        private const val MAX_IMAGE_WIDTH = 1080 // High-quality resolution  
    }  
  
    // Semaphore to prevent OOM on low-end devices by limiting concurrent tasks  
    private val conversionSemaphore = Semaphore(2)  
  
    suspend fun convert(inputUri: Uri, type: ConvertType): ConvertResult = withContext(Dispatchers.IO) {  
        // 1. Copy to cache with unique name to avoid conflicts  
        val inputPath = copyToCache(inputUri, "convert_in")  
            ?: return@withContext ConvertResult.Error(ConvertErrorType.FILE_NOT_FOUND, "Input file access failed")  
  
        val outputPath = getOutputPath(inputPath, type)  
          
        try {  
            conversionSemaphore.withPermit {  
                ensureActive() // Check for cancellation before starting native task  
  
                val success = when (type) {  
                    ConvertType.PDF_TO_IMAGE -> convertPdfToImages(inputPath, outputPath)  
                    ConvertType.IMAGE_TO_PDF -> { /* Handled in specialized function */ false }  
                    else -> nativeBridge.performConversion(inputPath, outputPath, type.ordinal)  
                }  
  
                if (success) {  
                    val outputFile = File(outputPath)  
                    if (outputFile.exists()) {  
                        ConvertResult.Success(listOf(Uri.fromFile(outputFile)))  
                    } else {  
                        throw Exception("Output file not generated")  
                    }  
                } else {  
                    ConvertResult.Error(ConvertErrorType.CONVERSION_FAILED, "Native engine failed to convert")  
                }  
            }  
        } catch (e: CancellationException) {  
            ConvertResult.Error(ConvertErrorType.CANCELLED, "Task cancelled by user")  
        } catch (e: Exception) {  
            Log.e(TAG, "Fatal Conversion Error: ${e.message}")  
            ConvertResult.Error(ConvertErrorType.CONVERSION_FAILED, e.message ?: "Unknown Error")  
        } finally {  
            cleanupTempFile(inputPath)  
        }  
    }  
  
    /**  
     * Specialized PDF to Image conversion with proper Aspect Ratio scaling  
     */  
    private suspend fun convertPdfToImages(inputPath: String, outputDirPath: String): Boolean {  
        val docPtr = nativeBridge.openDocument(inputPath)  
        if (docPtr == 0L) return false  
  
        try {  
            val pageCount = nativeBridge.getPageCount(docPtr)  
            val outputDir = File(outputDirPath)  
            if (!outputDir.exists()) outputDir.mkdirs()  
  
            for (page in 1..pageCount) {  
                yield() // Cooperative cancellation  
  
                // Get original dimensions to maintain Aspect Ratio  
                val originalWidth = nativeBridge.getPageWidth(docPtr, page)  
                val originalHeight = nativeBridge.getPageHeight(docPtr, page)  
                  
                val scale = MAX_IMAGE_WIDTH.toFloat() / originalWidth  
                val targetWidth = MAX_IMAGE_WIDTH  
                val targetHeight = (originalHeight * scale).toInt()  
  
                // ARGB_8888 for high quality, RGB_565 only if extreme memory constraint  
                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)  
                  
                try {  
                    val renderSuccess = nativeBridge.renderPage(docPtr, page, bitmap, targetWidth, targetHeight)  
                    if (renderSuccess) {  
                        val imageFile = File(outputDir, "page_${page}.png")  
                        FileOutputStream(imageFile).use { out ->  
                            BufferedOutputStream(out).use { bos ->  
                                bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_QUALITY, bos)  
                            }  
                        }  
                    }  
                } finally {  
                    bitmap.recycle() // Force memory release  
                }  
            }  
            return true  
        } finally {  
            nativeBridge.closeDocument(docPtr) // Crucial: Destroys C++ object  
        }  
    }  
  
    private fun copyToCache(uri: Uri, prefix: String): String? {  
        return try {  
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.tmp")  
            context.contentResolver.openInputStream(uri)?.use { input ->  
                file.outputStream().buffered().use { output ->  
                    input.copyTo(output)  
                }  
            }  
            file.absolutePath  
        } catch (e: Exception) {  
            null  
        }  
    }  
  
    private fun getOutputPath(inputPath: String, type: ConvertType): String {  
        val baseName = File(inputPath).nameWithoutExtension  
        return File(context.cacheDir, "${baseName}_out${type.outputExt}").absolutePath  
    }  
  
    private fun cleanupTempFile(path: String) {  
        try { File(path).delete() } catch (e: Exception) { }  
    }  
}