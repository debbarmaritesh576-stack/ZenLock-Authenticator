package com.aegis.pdf.data.local  
  
import androidx.room.*  
import kotlinx.coroutines.flow.Flow  
  
/**  
 * DAO for managing recently accessed PDF files and Search logic.  
 */  
@Dao  
interface RecentFileDao {  
  
    // 1. Fetch all recent files (Newest first)  
    @Query("SELECT * FROM recent_files ORDER BY lastAccessed DESC")  
    fun getAllRecentFiles(): Flow<List<RecentFileEntity>>  
  
    // 2. Insert or Update a file (Jab user koi PDF open kare)  
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    suspend fun insertRecentFile(file: RecentFileEntity)  
  
    // 3. Delete a specific record from history  
    @Delete  
    suspend fun deleteRecentFile(file: RecentFileEntity)  
  
    // 4. Clear all history (Settings mein kaam aayega)  
    @Query("DELETE FROM recent_files")  
    suspend fun clearAllHistory()  
  
    // 5. Basic Offline Search (Filenames aur Paths par)  
    @Query("""  
        SELECT * FROM recent_files   
        WHERE fileName LIKE '%' || :query || '%'   
        OR filePath LIKE '%' || :query || '%'  
        ORDER BY lastAccessed DESC  
    """)  
    fun searchFiles(query: String): Flow<List<RecentFileEntity>>  
  
    // 6. Get file by Path (Check karne ke liye ki file pehle se DB mein hai ya nahi)  
    @Query("SELECT * FROM recent_files WHERE filePath = :path LIMIT 1")  
    suspend fun getFileByPath(path: String): RecentFileEntity?  
  
    // 7. Update Last Accessed Time (Jab user wapis wahi file khole)  
    @Query("UPDATE recent_files SET lastAccessed = :timestamp WHERE filePath = :path")  
    suspend fun updateAccessTime(path: String, timestamp: Long)  
  
    // 8. Filter by Favorites (Agar future mein Bookmark feature chahiye)  
    @Query("SELECT * FROM recent_files WHERE isFavorite = 1 ORDER BY lastAccessed DESC")  
    fun getFavoriteFiles(): Flow<List<RecentFileEntity>>  
  
    // 9. Transactional Update: Check if exists, then update or insert  
    @Transaction  
    suspend fun addOrUpdateRecent(file: RecentFileEntity) {  
        val existing = getFileByPath(file.filePath)  
        if (existing == null) {  
            insertRecentFile(file)  
        } else {  
            updateAccessTime(file.filePath, System.currentTimeMillis())  
        }  
    }  
}