package com.aegis.pdf.core.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File

class PdfManager {

    fun getPdfInfo(file: File): PdfInfo {
        return try {
            PDDocument.load(file).use { document ->
                PdfInfo(
                    fileName = file.name,
                    fileSize = file.length(),
                    pageCount = document.numberOfPages,
                    isEncrypted = document.isEncrypted,
                    title = document.documentInformation?.title ?: file.name,
                    author = document.documentInformation?.author ?: "Unknown"
                )
            }
        } catch (e: Exception) {
            PdfInfo(fileName = file.name, fileSize = file.length())
        }
    }

    fun getPdfInfo(uri: Uri, context: Context): PdfInfo {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                PDDocument.load(stream).use { document ->
                    PdfInfo(
                        fileName = uri.lastPathSegment ?: "Unknown",
                        pageCount = document.numberOfPages,
                        isEncrypted = document.isEncrypted
                    )
                }
            } ?: PdfInfo()
        } catch (e: Exception) {
            PdfInfo()
        }
    }

    fun isValidPdf(file: File): Boolean {
        return try {
            PDDocument.load(file).use { true }
        } catch (e: Exception) {
            false
        }
    }

    fun getPageCount(file: File): Int {
        return try {
            PDDocument.load(file).use { it.numberOfPages }
        } catch (e: Exception) {
            0
        }
    }
}

data class PdfInfo(
    val fileName: String = "",
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val isEncrypted: Boolean = false,
    val title: String = "",
    val author: String = ""
)