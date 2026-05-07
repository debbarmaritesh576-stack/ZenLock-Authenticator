package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File

class PdfSplitter {

    fun splitAllPages(inputFile: File, outputDir: File): List<File> {
        val outputFiles = mutableListOf<File>()
        return try {
            PDDocument.load(inputFile).use { document ->
                val splitter = Splitter()
                val pages = splitter.split(document)

                pages.forEachIndexed { index, pageDoc ->
                    val outputFile = File(outputDir, "page_${index + 1}.pdf")
                    pageDoc.use { it.save(outputFile) }
                    outputFiles.add(outputFile)
                }
            }
            outputFiles
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun extractPage(inputFile: File, pageNumber: Int, outputFile: File): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                if (pageNumber in 1..document.numberOfPages) {
                    PDDocument().use { newDocument ->
                        newDocument.importPage(document.getPage(pageNumber - 1))
                        newDocument.save(outputFile)
                    }
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun deletePages(inputFile: File, pageNumbers: List<Int>, outputFile: File): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                PDDocument().use { newDocument ->
                    for (i in 0 until document.numberOfPages) {
                        if (!pageNumbers.contains(i + 1)) {
                            newDocument.importPage(document.getPage(i))
                        }
                    }
                    newDocument.save(outputFile)
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}