package com.aegis.pdf.features.editor.text

import android.content.Context
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

data class ExportConfig(
    val format: ExportFormat = ExportFormat.PDF,
    val includeAnnotations: Boolean = true,
    val includeImages: Boolean = true,
    val quality: Int = 100,
    val pageRange: IntRange? = null
)

enum class ExportFormat {
    PDF, TXT, HTML, RTF, DOCX
}

@Singleton
class TextExportManager @Inject constructor(
    private val context: Context
) {

    fun exportToText(elements: List<TextElement>): String {
        val sb = StringBuilder()
        elements.forEach { element ->
            sb.appendLine(element.text)
        }
        return sb.toString()
    }

    fun exportToHtml(elements: List<TextElement>): String {
        val sb = StringBuilder()
        sb.appendLine("<!DOCTYPE html>")
        sb.appendLine("<html><head><meta charset=\"UTF-8\"><title>Exported Text</title></head>")
        sb.appendLine("<body>")

        elements.forEach { element ->
            val style = buildString {
                append("font-family:${element.fontName};")
                append("font-size:${element.fontSize}pt;")
                if (element.isBold) append("font-weight:bold;")
                if (element.isItalic) append("font-style:italic;")
                if (element.isUnderline) append("text-decoration:underline;")
                append("text-align:${element.alignment.name.lowercase()};")
            }
            sb.appendLine("<p style=\"$style\">${element.text}</p>")
        }

        sb.appendLine("</body></html>")
        return sb.toString()
    }

    fun exportToRtf(elements: List<TextElement>): String {
        val sb = StringBuilder()
        sb.appendLine("{\\rtf1\\ansi\\deff0")

        elements.forEach { element ->
            sb.append("{")
            if (element.isBold) sb.append("\\b")
            if (element.isItalic) sb.append("\\i")
            if (element.isUnderline) sb.append("\\ul")
            sb.append("\\fs${(element.fontSize * 2).toInt()}")
            sb.append(" ${element.text.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}")}")
            sb.appendLine("\\par}")
        }

        sb.appendLine("}")
        return sb.toString()
    }

    fun exportToMarkdown(elements: List<TextElement>): String {
        val sb = StringBuilder()
        elements.forEach { element ->
            var text = element.text
            if (element.isBold) text = "**$text**"
            if (element.isItalic) text = "*$text*"
            if (element.isUnderline) text = "<u>$text</u>"
            sb.appendLine(text)
            sb.appendLine()
        }
        return sb.toString()
    }

    fun saveToFile(content: String, fileName: String, mimeType: String): Uri? {
        return try {
            val resolver = context.contentResolver
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, mimeType)
            }

            val uri = resolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )

            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                }
            }
            uri
        } catch (e: Exception) {
            null
        }
    }
}