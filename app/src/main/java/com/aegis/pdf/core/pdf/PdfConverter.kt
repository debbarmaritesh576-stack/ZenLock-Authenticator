package com.aegis.pdf.core.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.io.FileOutputStream

class PdfConverter {

    fun pdfToImages(inputFile: File, outputDir: File): List<File> {
        val images = mutableListOf<File>()
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val outputFile = File(outputDir, "page_${i + 1}.png")
                FileOutputStream(outputFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                bitmap.recycle()
                images.add(outputFile)
            }
            renderer.close()
            fileDescriptor.close()
            images
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun imagesToPdf(imageFiles: List<File>, outputFile: File): Boolean {
        return try {
            PDDocument().use { document ->
                imageFiles.forEach { file ->
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        bitmap?.let {
                            val page = PDPage(PDRectangle(it.width.toFloat(), it.height.toFloat()))
                            document.addPage(page)
                            val tempFile = File.createTempFile("conv", ".jpg")
                            FileOutputStream(tempFile).use { out ->
                                it.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            val pdImage = PDImageXObject.createFromFile(tempFile.absolutePath, document)
                            PDPageContentStream(document, page).use { cs ->
                                cs.drawImage(pdImage, 0f, 0f)
                            }
                            tempFile.delete()
                        }
                    }
                }
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}