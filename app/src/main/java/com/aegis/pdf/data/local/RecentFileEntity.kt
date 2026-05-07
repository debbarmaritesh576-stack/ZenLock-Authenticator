package com.aegis.pdf.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val size: Long,
    val pageCount: Int = 0,
    val lastOpened: Long = System.currentTimeMillis()
)