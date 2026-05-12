package com.aegis.pdf.features.organizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "file_tags",
    primaryKeys = ["fileId", "tagId"]
)
data class FileTagCrossRef(
    val fileId: String,
    val tagId: String
)