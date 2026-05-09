package com.aegis.browser.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

class PdfCompressor(private val context: Context) {

    data class CompressionResult(
        val originalSize: Long,
        val compressedSize: Long,
        val compressionRatio: Float,
        val outputPath: String
    )

    /**
     * Compress PDF with quality settings
     * @param inputPath Source PDF path
     * @param outputPath Output compressed PDF path
     * @param quality Compression quality (0-100), lower = smaller file
     * @param scaleFactor Image scale factor (0.1 - 1.0), lower = smaller
     */
    fun compress(
        inputPath: String,
        outputPath: String,
        quality: Int = 50,
        scaleFactor: Float = 0.5f
    ): CompressionResult {
        val inputFile = File(inputPath)
        val originalSize = inputFile.length()

        try {
            // Method 1: iText-based recompression (works for text + image PDFs)
            compressWithIText(inputPath, outputPath, quality, scaleFactor)
        } catch (e: Exception) {
            try {
                // Method 2: Fallback - page-by-page bitmap recompression
                compressWithAndroidPdfRenderer(inputPath, outputPath, quality, scaleFactor)
            } catch (e2: Exception) {
                // Method 3: Last resort - just copy file
                inputFile.copyTo(File(outputPath), overwrite = true)
            }
        }

        val compressedFile = File(outputPath)
        val compressedSize = compressedFile.length()
        val ratio = if (originalSize > 0) {
            ((originalSize - compressedSize).toFloat() / originalSize * 100)
        } else 0f

        return CompressionResult(
            originalSize = originalSize,
            compressedSize = compressedSize,
            compressionRatio = ratio,
            outputPath = outputPath
        )
    }

    /**
     * Method 1: iText-based compression with image optimization
     */
    private fun compressWithIText(
        inputPath: String,
        outputPath: String,
        quality: Int,
        scaleFactor: Float
    ) {
        val reader = PdfReader(inputPath)
        val writer = PdfWriter(outputPath)
        
        // Writer properties for compression
        writer.setCompressionLevel(9)
        writer.setFullCompressionMode(true)
        
        val pdfDoc = PdfDocument(reader, writer)
        val pageCount = pdfDoc.numberOfPages

        for (pageNum in 1..pageCount) {
            val page = pdfDoc.getPage(pageNum)
            val pageSize = page.pageSize
            
            // Get page content
            val contentStream = page.contentBytes
            
            // Compress images embedded in page
            val compressedContent = compressImagesInStream(
                contentStream, 
                quality, 
                scaleFactor
            )
            
            // Update page with compressed content
            page.setContentBytes(compressedContent)
            
            // Release resources
            page.flush()
        }

        pdfDoc.close()
        reader.close()
        writer.close()
    }

    /**
     * Method 2: Android PdfRenderer - renders pages as bitmaps with compression
     */
    private fun compressWithAndroidPdfRenderer(
        inputPath: String,
        outputPath: String,
        quality: Int,
        scaleFactor: Float
    ) {
        val inputFile = File(inputPath)
        val fileDescriptor = ParcelFileDescriptor.open(
            inputFile, 
            ParcelFileDescriptor.MODE_READ_ONLY
        )
        val renderer = PdfRenderer(fileDescriptor)
        val pageCount = renderer.pageCount

        // Create output PDF using iText (if available) or write compressed images
        val writer = PdfWriter(outputPath)
        writer.setCompressionLevel(9)
        
        val outputPdf = PdfDocument(writer)
        val document = Document(outputPdf)

        for (pageIndex in 0 until pageCount) {
            val page = renderer.openPage(pageIndex)
            val width = page.width
            val height = page.height

            // Create bitmap with scaled dimensions
            val scaledWidth = (width * scaleFactor).toInt()
            val scaledHeight = (height * scaleFactor).toInt()

            val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)
            bitmap.setHasAlpha(false)

            // Render page to bitmap at reduced resolution
            page.render(
                bitmap, 
                null, 
                null, 
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )

            // Calculate actual JPEG quality for target size
            val actualQuality = calculateOptimalQuality(bitmap, quality)

            // Convert bitmap to compressed JPEG bytes
            val compressedBytes = compressBitmapToJpeg(bitmap, actualQuality)

            // Create compressed bitmap from bytes
            val compressedBitmap = BitmapFactory.decodeByteArray(
                compressedBytes, 
                0, 
                compressedBytes.size
            )

            // Add compressed page to output PDF
            val pageSize = com.itextpdf.kernel.geom.PageSize(
                scaledWidth.toFloat(), 
                scaledHeight.toFloat()
            )
            val newPage = outputPdf.addNewPage(pageSize)
            
            val imageData = bitmapToByteArray(compressedBitmap)
            val imageObj = PdfImageXObject.createImageXObjectFromBytes(
                imageData, 
                outputPdf
            )
            
            val canvas = PdfCanvas(newPage)
            canvas.addImageAt(imageObj, 0f, 0f, true)

            // Cleanup
            bitmap.recycle()
            compressedBitmap.recycle()
            page.close()
        }

        document.close()
        outputPdf.close()
        writer.close()
        fileDescriptor.close()
    }

    /**
     * Compress bitmap to JPEG bytes
     */
    private fun compressBitmapToJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    /**
     * Calculate optimal JPEG quality to achieve target file size
     */
    private fun calculateOptimalQuality(bitmap: Bitmap, targetQuality: Int): Int {
        val stream = java.io.ByteArrayOutputStream()
        
        // Binary search for best quality
        var low = 5
        var high = targetQuality.coerceIn(10, 95)
        var bestQuality = low

        while (low <= high) {
            val mid = (low + high) / 2
            stream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, mid, stream)
            
            if (stream.size() <= (bitmap.byteCount / 4)) {
                bestQuality = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return bestQuality
    }

    /**
     * Convert bitmap to byte array for PDF embedding
     */
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return stream.toByteArray()
    }

    /**
     * Compress images within PDF content stream
     */
    private fun compressImagesInStream(
        content: ByteArray,
        quality: Int,
        scaleFactor: Float
    ): ByteArray {
        // Detect and recompress embedded images
        // This processes DCTDecode (JPEG) and FlateDecode streams
        return try {
            val contentStr = String(content, Charsets.ISO_8859_1)
            
            // Find DCTDecode images (JPEG)
            val dctPattern = Regex(
                "/Filter\\s+/DCTDecode[^}]*stream\\s*([\\s\\S]*?)endstream",
                RegexOption.IGNORE_CASE
            )
            
            val processed = dctPattern.replace(contentStr) { match ->
                val imageData = match.groupValues[1]
                val originalBytes = imageData.toByteArray(Charsets.ISO_8859_1)
                
                // Decode and recompress JPEG
                val bitmap = BitmapFactory.decodeByteArray(
                    originalBytes, 0, originalBytes.size
                )
                
                if (bitmap != null) {
                    val scaledWidth = (bitmap.width * scaleFactor).toInt()
                    val scaledHeight = (bitmap.height * scaleFactor).toInt()
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap, scaledWidth, scaledHeight, true
                    )
                    
                    val compressed = compressBitmapToJpeg(scaledBitmap, quality)
                    bitmap.recycle()
                    scaledBitmap.recycle()
                    
                    match.value.replace(
                        imageData, 
                        String(compressed, Charsets.ISO_8859_1)
                    )
                } else {
                    match.value
                }
            }

            processed.toByteArray(Charsets.ISO_8859_1)
        } catch (e: Exception) {
            content // Return original if processing fails
        }
    }

    /**
     * Lossless compression - removes metadata, optimizes structure
     */
    fun compressLossless(inputPath: String, outputPath: String): CompressionResult {
        val inputFile = File(inputPath)
        val originalSize = inputFile.length()

        val reader = PdfReader(inputPath)
        val writer = PdfWriter(outputPath)
        
        // Maximum lossless compression
        writer.setCompressionLevel(9)
        writer.setFullCompressionMode(true)
        writer.setSmartMode(true)
        
        val pdfDoc = PdfDocument(reader, writer)
        
        // Remove unused objects
        pdfDoc.flush()
        pdfDoc.close()
        
        reader.close()
        writer.close()

        val compressedFile = File(outputPath)
        val compressedSize = compressedFile.length()
        
        return CompressionResult(
            originalSize = originalSize,
            compressedSize = compressedSize,
            compressionRatio = if (originalSize > 0) {
                ((originalSize - compressedSize).toFloat() / originalSize * 100)
            } else 0f,
            outputPath = outputPath
        )
    }

    /**
     * Apply blur to reduce file size aggressively (for preview/thumbnail use)
     */
    fun compressForPreview(
        inputPath: String,
        outputPath: String,
        maxSizeKB: Int = 500
    ): CompressionResult {
        val inputFile = File(inputPath)
        val originalSize = inputFile.length()

        var currentScale = 0.5f
        var currentQuality = 30
        
        // Iterate until file is under max size
        repeat(5) {
            compress(inputPath, outputPath, currentQuality, currentScale)
            if (File(outputPath).length() <= maxSizeKB * 1024L) {
                return@repeat
            }
            currentScale *= 0.8f
            currentQuality = (currentQuality * 0.8).toInt()
        }

        val compressedFile = File(outputPath)
        return CompressionResult(
            originalSize = originalSize,
            compressedSize = compressedFile.length(),
            compressionRatio = if (originalSize > 0) {
                ((originalSize - compressedFile.length()).toFloat() / originalSize * 100)
            } else 0f,
            outputPath = outputPath
        )
    }

    /**
     * Get file size in human-readable format
     */
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${"%.2f".format(bytes.toDouble() / (1024 * 1024 * 1024))} GB"
        }
    }
}