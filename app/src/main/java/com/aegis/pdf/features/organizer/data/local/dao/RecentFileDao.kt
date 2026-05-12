package com.aegis.pdf.features.organizer.data.local.dao

import androidx.room.*
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files WHERE id = :fileId")
    suspend fun getFile(fileId: String): RecentFileEntity?

    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC")
    suspend fun getAllFiles(): List<RecentFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: RecentFileEntity)

    @Update
    suspend fun update(file: RecentFileEntity)

    @Delete
    suspend fun delete(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE id = :fileId")
    suspend fun deleteById(fileId: String)
}