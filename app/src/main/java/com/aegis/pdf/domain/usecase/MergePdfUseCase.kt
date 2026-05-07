package com.aegis.pdf.domain.usecase  
  
import com.aegis.pdf.core.pdf.PdfMerger  
import com.aegis.pdf.data.local.DocumentDataSource  
import com.aegis.pdf.data.repository.PdfRepository  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class MergePdfUseCase @Inject constructor(  
    private val pdfMerger: PdfMerger,  
    private val documentDataSource: DocumentDataSource,  
    private val repository: PdfRepository  
) {  
    suspend operator fun invoke(inputUris: List<android.net.Uri>): Result {  
        return withContext(Dispatchers.IO) {  
            try {  
                val inputFiles = inputUris.mapNotNull { uri ->  
                    documentDataSource.copyToTemp(uri)  
                }  
                if (inputFiles.size < 2) {  
                    return@withContext Result.Error("Select at least 2 PDF files")  
                }  
                val outputFile = repository.createOutputFile("merged")  
                val success = pdfMerger.mergePdfFiles(inputFiles, outputFile)  
                documentDataSource.deleteAll(inputFiles)  
                if (success) Result.Success(outputFile)  
                else Result.Error("Merge failed")  
            } catch (e: Exception) {  
                Result.Error(e.message ?: "Unknown error")  
            }  
        }  
    }  
  
    sealed class Result {  
        data class Success(val outputFile: File) : Result()  
        data class Error(val message: String) : Result()  
    }  
}