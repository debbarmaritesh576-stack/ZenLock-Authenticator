package com.aegis.pdf.core.pdf  
  
import com.tom_roush.pdfbox.pdmodel.PDDocument  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class PdfSplitter @Inject constructor() {  
  
    /**  
     * Specific page indexes ko uthakar ek nayi extracted PDF file prepare karta hai.  
     */  
    suspend fun splitSpecificPages(sourceFile: File, pagesToExtract: List<Int>, outputFile: File): Result<File> =   
        withContext(Dispatchers.IO) {  
            if (!sourceFile.exists()) return@withContext Result.failure(FileNotFoundException("Source file missing"))  
              
            return@withContext try {  
                PDDocument.load(sourceFile).use { sourceDoc ->  
                    PDDocument().use { newDoc ->  
                        pagesToExtract.forEach { pageIndex ->  
                            if (pageIndex in 0 until sourceDoc.numberOfPages) {  
                                newDoc.addPage(sourceDoc.getPage(pageIndex))  
                            }  
                        }  
                        outputFile.parentFile?.mkdirs()  
                        newDoc.save(outputFile)  
                    }  
                }  
                Result.success(outputFile)  
            } catch (e: Exception) {  
                Result.failure(e)  
            }  
        }  
}