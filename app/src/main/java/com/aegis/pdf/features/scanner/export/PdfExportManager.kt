package com.aegis.pdf.features.scanner.export

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image as ITextImage
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.io.image.ImageDataFactory

@Singleton
class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfCompressor: PdfCompressor,
    private val pdfMetadataWriter: PdfMetadataWriter
) {
    private val TAG = "PdfExportManager"
    private val exportDir = File(context.cacheDir, "pdf_exports")

    init {
        exportDir.mkdirs()
    }

    suspend fun createPdfFromBitmaps(
        bitmaps: List<Bitmap>,
        fileName: String = "document_${System.currentTimeMillis()}.pdf",
        password: String? = null,
        compress: Boolean = true,
        addWatermark: Boolean = false,
        metadata: Map<String, String>? = null,
        enableOcr: Boolean = false
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            if (bitmaps.isEmpty()) {
                Log.e(TAG, "No bitmaps provided")
                return@withContext null
            }

            val outputFile = File(exportDir, fileName)
            
            val writerProps = WriterProperties()
            if (password != null) {
                writerProps.setStandardEncryption(
                    password.toByteArray(),
                    "aegis_pdf".toByteArray(),
                    EncryptionConstants.ALLOW_PRINTING,
                    EncryptionConstants.ENCRYPTION_AES_256
                )
            }

            val pdfWriter = PdfWriter(outputFile, writerProps)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            bitmaps.forEachIndexed { index, bitmap ->
                try {
                    val scaledBitmap = if (compress) {
                        pdfCompressor.compressBitmap(bitmap)
                    } else {
                        bitmap
                    }

                    val bitmapFile = File(context.cacheDir, "temp_page_$index.jpg")
                    FileOutputStream(bitmapFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }

                    val imageData = ImageDataFactory.create(bitmapFile.absolutePath)
                    val image = ITextImage(imageData)
                    image.scaleToFit(PageSize.A4.width - 40, PageSize.A4.height - 40)

                    pdfDocument.addNewPage(PageSize.A4)
                    document.add(image)

                    if (addWatermark) {
                        addWatermarkToPage(pdfDocument, index)
                    }

                    bitmapFile.delete()
                    Log.d(TAG, "Page $index added to PDF")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add page $index", e)
                }
            }

            if (metadata != null) {
                pdfMetadataWriter.addMetadata(pdfDocument, metadata)
            }

            document.close()

            Log.d(TAG, "PDF created successfully: ${outputFile.absolutePath}")
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "PDF creation failed", e)
            null
        }
    }

    private fun addWatermarkToPage(pdfDocument: PdfDocument, pageIndex: Int) {
        try {
            val page = pdfDocument.getPage(pageIndex + 1)
            val pdfCanvas = PdfCanvas(page)
            val canvas = Canvas(pdfCanvas, page.pageSize)

            canvas.showTextAligned(
                "Aegis PDF",
                page.pageSize.width / 2,
                page.pageSize.height / 2,
                TextAlignment.CENTER
            )

            canvas.close()
            Log.d(TAG, "Watermark added to page ${pageIndex + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add watermark", e)
        }
    }

    suspend fun exportAsImages(
        bitmaps: List<Bitmap>,
        format: String = "JPEG",
        quality: Int = 90
    ): List<Uri> = withContext(Dispatchers.IO) {
        try {
            val uris = mutableListOf<Uri>()

            bitmaps.forEachIndexed { index, bitmap ->
                val extension = when (format) {
                    "PNG" -> "png"
                    "WEBP" -> "webp"
                    else -> "jpg"
                }
                val fileName = "page_${index + 1}.$extension"
                val file = File(exportDir, fileName)

                FileOutputStream(file).use { out ->
                    val compressFormat = when (format) {
                        "PNG" -> Bitmap.CompressFormat.PNG
                        "WEBP" -> Bitmap.CompressFormat.WEBP
                        else -> Bitmap.CompressFormat.JPEG
                    }
                    bitmap.compress(compressFormat, quality, out)
                }

                uris.add(Uri.fromFile(file))
                Log.d(TAG, "Image exported: $fileName")
            }

            uris
        } catch (e: Exception) {
            Log.e(TAG, "Image export failed", e)
            emptyList()
        }
    }

    fun getExportedFiles(): List<File> {
        return try {
            exportDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get exported files", e)
            emptyList()
        }
    }

    fun clearExports() {
        try {
            exportDir.listFiles()?.forEach { it.delete() }
            Log.d(TAG, "Exports cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear exports", e)
        }
    }
}

@Singleton
class PdfCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "PdfCompressor"
    private val maxWidth = 1200
    private val maxHeight = 1600

    fun compressBitmap(bitmap: Bitmap, quality: Int = 75): Bitmap {
        return if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val ratio = if (bitmap.width > bitmap.height) {
                maxWidth.toFloat() / bitmap.width
            } else {
                maxHeight.toFloat() / bitmap.height
            }

            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val compressed = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            Log.d(TAG, "Bitmap compressed: ${bitmap.width}x${bitmap.height} -> ${newWidth}x${newHeight}")
            compressed
        } else {
            bitmap
        }
    }

    suspend fun compressPdfFile(inputPath: String, outputPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "PDF compression started: $inputPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "PDF compression failed", e)
            false
        }
    }
}

@Singleton
class PdfMetadataWriter @Inject constructor() {
    private val TAG = "PdfMetadataWriter"

    fun addMetadata(pdfDocument: PdfDocument, metadata: Map<String, String>) {
        try {
            val documentInfo = pdfDocument.documentInfo

            metadata.forEach { (key, value) ->
                when (key) {
                    "title" -> documentInfo.setTitle(value)
                    "author" -> documentInfo.setAuthor(value)
                    "subject" -> documentInfo.setSubject(value)
                    "keywords" -> documentInfo.setKeywords(value)
                    "creator" -> documentInfo.setCreator("Aegis PDF Scanner")
                }
            }

            Log.d(TAG, "Metadata added to PDF")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add metadata", e)
        }
    }

    fun getMetadataTemplate(): Map<String, String> {
        return mapOf(
            "title" to "Scanned Document",
            "author" to "Aegis PDF",
            "subject" to "Document Scan",
            "keywords" to "scan, document"
        )
    }
}