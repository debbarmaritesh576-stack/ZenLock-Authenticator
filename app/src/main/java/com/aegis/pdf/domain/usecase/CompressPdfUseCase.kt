package com.aegis.pdf.domain.usecase

import com.aegis.pdf.core.pdf.PdfCompressor
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.data.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompressPdfUseCase @Inject constructor(
    private val pdfCompressor: PdfCompressor,
    private val documentDataSource: DocumentDataSource,
    private val repository: PdfRepository
) {
    suspend operator fun invoke(
        uri: android.net.Uri,
        quality: PdfCompressor.Quality = PdfCompressor.Quality.MEDIUM
    ): Result {
        return withContext(Dispatchers.IO) {
            try {
                val inputFile = documentDataSource.copyToTemp(uri)
                    ?: return@withContext Result.Error("Failed to load file")
                val outputFile = repository.createOutputFile("compressed")
                val result = pdfCompressor.compress(inputFile, outputFile, quality)
                documentDataSource.deleteAll(inputFile)
                if (result.success) {
                    Result.Success(outputFile, result.savedPercentage)
                } else Result.Error("Compression failed")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class Result {
        data class Success(val file: File, val savedPercentage: Float) : Result()
        data class Error(val message: String) : Result()
    }
}