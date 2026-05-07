package com.aegis.pdf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecentFileDao {

    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT 20")
    suspend fun getRecentFiles(): List<RecentFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: RecentFileEntity)

    @Query("DELETE FROM recent_files")
    suspend fun deleteAll()

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun deleteByPath(path: String)
}