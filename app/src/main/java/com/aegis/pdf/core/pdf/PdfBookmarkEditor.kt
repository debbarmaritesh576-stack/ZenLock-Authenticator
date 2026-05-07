package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfBookmarkEditor @Inject constructor() {

    data class Bookmark(
        val title: String,
        val pageNumber: Int,
        val children: List<Bookmark> = emptyList()
    )

    fun getBookmarks(file: File): List<Bookmark> {
        return try {
            PDDocument.load(file).use { document ->
                val outline = document.documentCatalog.documentOutline
                if (outline == null) return emptyList()
                extractBookmarks(outline.firstChild)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractBookmarks(item: PDOutlineItem?): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        var current = item
        while (current != null) {
            try {
                bookmarks.add(
                    Bookmark(
                        title = current.title ?: "Untitled",
                        pageNumber = current.findDestinationPage()?.let { it + 1 } ?: 1,
                        children = extractBookmarks(current.firstChild)
                    )
                )
            } catch (e: Exception) {
                // Skip corrupted bookmarks
            }
            current = current.nextSibling
        }
        return bookmarks
    }

    fun addBookmark(
        inputFile: File,
        outputFile: File,
        title: String,
        pageNumber: Int
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val outline = document.documentCatalog.documentOutline
                    ?: PDDocumentOutline().also {
                        document.documentCatalog.documentOutline = it
                    }

                val page = document.getPage(pageNumber - 1)
                val dest = org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination()
                dest.page = page

                val item = PDOutlineItem()
                item.title = title
                item.destination = dest
                outline.addLast(item)

                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeAllBookmarks(inputFile: File, outputFile: File): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                document.documentCatalog.documentOutline = null
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}