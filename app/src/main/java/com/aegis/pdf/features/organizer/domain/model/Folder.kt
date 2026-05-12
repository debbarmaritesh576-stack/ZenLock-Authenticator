package com.aegis.pdf.features.organizer.domain.model

data class Folder(
    val id: String,
    val name: String,
    val parentId: String?,
    val color: Int,
    val createdAt: Long,
    val updatedAt: Long
)

data class Tag(
    val id: String,
    val name: String,
    val color: Int,
    val createdAt: Long
)

data class File(
    val id: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val lastOpened: Long,
    val mimeType: String,
    val pages: Int
)