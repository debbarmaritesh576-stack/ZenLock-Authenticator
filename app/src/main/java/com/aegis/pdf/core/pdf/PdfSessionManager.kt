package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfSessionManager @Inject constructor() {

    private val activeDocuments = mutableMapOf<String, PDDocument>()

    fun loadDocument(file: File): PDDocument? {
        return try {
            val doc = PDDocument.load(file)
            activeDocuments[file.absolutePath] = doc
            doc
        } catch (e: Exception) {
            null
        }
    }

    fun getDocument(file: File): PDDocument? {
        return activeDocuments[file.absolutePath]
    }

    fun closeDocument(file: File) {
        activeDocuments[file.absolutePath]?.use { it.close() }
        activeDocuments.remove(file.absolutePath)
    }

    fun closeAll() {
        activeDocuments.values.forEach { it.close() }
        activeDocuments.clear()
    }
}