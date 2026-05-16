package com.aegis.pdf.core.pdf  
  
import android.content.Context  
import com.tom_roush.pdfbox.io.MemoryUsageSetting  
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class PdfMergerEngine @Inject constructor(  
    private val context: Context  
) {  
    /**  
     * Multiple PDF files ko efficiently merge karta hai without choking the RAM.  
     */  
    suspend fun mergeDocuments(inputFiles: List<File>, outputFile: File): Result<File> =   
        withContext(Dispatchers.IO) {  
            val validFiles = inputFiles.filter { it.exists() && it.extension.equals("pdf", true) }  
            if (validFiles.size < 2) {  
                return@withContext Result.failure(IllegalArgumentException("At least 2 valid PDF files are required."))  
            }  
  
            return@withContext try {  
                val merger = PDFMergerUtility().apply {  
                    destinationFileName = outputFile.absolutePath  
                }  
  
                validFiles.forEach { file ->  
                    merger.addSource(file)  
                }  
  
                // Production Optimization: Use disk scratch space instead of holding everything in RAM  
                val scratchDir = File(context.cacheDir, "pdf_scratch").apply { mkdirs() }  
                merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly(scratchDir.absolutePath, -1))  
  
                Result.success(outputFile)  
            } catch (e: Exception) {  
                Result.failure(e)  
            }  
        }  
}