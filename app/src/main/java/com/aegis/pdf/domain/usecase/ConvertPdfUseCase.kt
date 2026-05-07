package com.aegis.pdf.domain.usecase

import com.aegis.pdf.core.pdf.PdfConverter
import com.aegis.pdf.data.local.DocumentDataSource
import com.aegis.pdf.data.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConvertPdfUseCase @Inject constructor(
    private val pdfConverter: PdfConverter,
    private val documentDataSource: DocumentDataSource,
    private val repository: PdfRepository
) {
    suspend fun pdfToImages(uri: android.net.Uri): Result {
        return withContext(Dispatchers.IO) {
            try {
                val inputFile = documentDataSource.copyToTemp(uri)
                    ?: return@withContext Result.Error("Failed to load file")
                val outputDir = repository.createOutputDir("images")
                val images = pdfConverter.pdfToImages(inputFile, outputDir)
                documentDataSource.deleteAll(inputFile)
                if (images.isNotEmpty()) Result.Success(images)
                else Result.Error("Conversion failed")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun imagesToPdf(uris: List<android.net.Uri>): Result {
        return withContext(Dispatchers.IO) {
            try {
                val imageFiles = uris.mapNotNull { documentDataSource.copyToTemp(it) }
                if (imageFiles.isEmpty()) return@withContext Result.Error("No valid images")
                val outputFile = repository.createOutputFile("converted")
                val success = pdfConverter.imagesToPdf(imageFiles, outputFile)
                documentDataSource.deleteAll(imageFiles)
                if (success) Result.Success(listOf(outputFile))
                else Result.Error("Conversion failed")
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