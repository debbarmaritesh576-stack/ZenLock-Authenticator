package com.aegis.pdf.domain.usecase  
  
import com.aegis.pdf.core.pdf.PdfSplitter  
import com.aegis.pdf.data.local.DocumentDataSource  
import com.aegis.pdf.data.repository.PdfRepository  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class SplitPdfUseCase @Inject constructor(  
    private val pdfSplitter: PdfSplitter,  
    private val documentDataSource: DocumentDataSource,  
    private val repository: PdfRepository  
) {  
    suspend operator fun invoke(uri: android.net.Uri): Result {  
        return withContext(Dispatchers.IO) {  
            try {  
                val inputFile = documentDataSource.copyToTemp(uri)  
                    ?: return@withContext Result.Error("Failed to load file")  
                val outputDir = repository.createOutputDir("split")  
                val files = pdfSplitter.splitAllPages(inputFile, outputDir)  
                documentDataSource.deleteAll(inputFile)  
                if (files.isNotEmpty()) Result.Success(files)  
                else Result.Error("Split failed")  
            } catch (e: Exception) {  
                Result.Error(e.message ?: "Unknown error")  
            }  
        }  
    }  
  
    sealed class Result {  
        data class Success(val files: List<File>) : Result()  
        data class Error(val message: String) : Result()  
    }  
}