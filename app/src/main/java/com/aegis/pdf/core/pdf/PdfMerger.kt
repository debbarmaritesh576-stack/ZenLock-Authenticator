package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File

class PdfMerger {

    fun mergePdfFiles(inputFiles: List<File>, outputFile: File): Boolean {
        return try {
            val merger = PDFMergerUtility()
            merger.destinationFileName = outputFile.absolutePath

            inputFiles.forEach { file ->
                if (file.exists() && file.extension.equals("pdf", true)) {
                    merger.addSource(file)
                }
            }

            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun mergeImages(imageFiles: List<File>, outputFile: File): Boolean {
        return try {
            PDDocument().use { document ->
                imageFiles.forEach { imageFile ->
                    if (imageFile.exists()) {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                        bitmap?.let {
                            val page = PDPage(PDRectangle(it.width.toFloat(), it.height.toFloat()))
                            document.addPage(page)
                            val tempFile = File.createTempFile("img", ".jpg")
                            it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, java.io.FileOutputStream(tempFile))
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