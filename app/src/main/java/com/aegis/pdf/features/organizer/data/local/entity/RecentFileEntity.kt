package com.aegis.pdf.features.organizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val lastOpened: Long,
    val mimeType: String = "application/pdf",
    val pages: Int = 0
)