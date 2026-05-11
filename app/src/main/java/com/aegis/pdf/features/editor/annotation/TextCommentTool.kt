package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class TextCommentAnnotation(
    val id: Long, val pageNumber: Int, val position: Offset,
    val text: String, val author: String = "User", val timestamp: Long = System.currentTimeMillis()
)

class TextCommentTool {
    fun place(position: Offset, text: String, author: String = "User") = TextCommentAnnotation(System.currentTimeMillis(), 0, position, text, author)
    fun edit(a: TextCommentAnnotation, newText: String) = a.copy(text = newText, timestamp = System.currentTimeMillis())
    fun reply(a: TextCommentAnnotation, reply: String) = a.copy(text = "${a.text}\n\n↳ $reply", timestamp = System.currentTimeMillis())
}