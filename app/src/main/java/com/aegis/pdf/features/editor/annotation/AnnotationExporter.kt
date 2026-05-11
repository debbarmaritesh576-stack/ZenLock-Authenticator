package com.aegis.pdf.features.editor.annotation

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

class AnnotationExporter(private val context: Context) {

    fun exportToJson(annotations: List<Any>): String {
        val json = JSONObject()
        val items = JSONArray()
        annotations.forEach { a ->
            when (a) {
                is HighlightAnnotation -> items.put(JSONObject().apply { put("type","highlight"); put("id",a.id); put("page",a.pageNumber) })
                is FreehandAnnotation -> items.put(JSONObject().apply { put("type","freehand"); put("id",a.id); put("page",a.pageNumber) })
                is StickyNoteAnnotation -> items.put(JSONObject().apply { put("type","sticky_note"); put("id",a.id); put("text",a.text); put("page",a.pageNumber) })
                is TextCommentAnnotation -> items.put(JSONObject().apply { put("type","comment"); put("id",a.id); put("text",a.text); put("author",a.author); put("page",a.pageNumber) })
            }
        }
        json.put("version", "1.0")
        json.put("exportDate", System.currentTimeMillis())
        json.put("annotations", items)
        return json.toString(2)
    }

    fun saveToFile(annotations: List<Any>, fileName: String): Uri? {
        val json = exportToJson(annotations)
        return try {
            val resolver = context.contentResolver
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let { resolver.openOutputStream(it)?.use { s -> s.write(json.toByteArray()) } }
            uri
        } catch (e: Exception) { null }
    }
}