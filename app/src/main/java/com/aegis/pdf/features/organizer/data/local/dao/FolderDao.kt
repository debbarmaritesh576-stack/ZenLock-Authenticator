package com.aegis.pdf.features.organizer.data.local.dao

import androidx.room.*
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY name")
    suspend fun getRootFolders(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY name")
    suspend fun getSubFolders(parentId: String): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolder(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("SELECT * FROM folders ORDER BY name")
    suspend fun getAllFolders(): List<FolderEntity>
}