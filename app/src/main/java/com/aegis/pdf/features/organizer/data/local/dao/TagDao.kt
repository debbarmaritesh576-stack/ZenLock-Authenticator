package com.aegis.pdf.features.organizer.data.local.dao

import androidx.room.*
import com.aegis.pdf.features.organizer.data.local.entity.TagEntity
import com.aegis.pdf.features.organizer.data.local.entity.FileTagCrossRef
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT t.* FROM tags t INNER JOIN file_tags ft ON t.id = ft.tagId WHERE ft.fileId = :fileId")
    suspend fun getTagsForFile(fileId: String): List<TagEntity>

    @Query("SELECT f.* FROM recent_files f INNER JOIN file_tags ft ON f.id = ft.fileId WHERE ft.tagId = :tagId")
    suspend fun getFilesForTag(tagId: String): List<RecentFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFileTag(crossRef: FileTagCrossRef)

    @Delete
    suspend fun removeFileTag(crossRef: FileTagCrossRef)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}