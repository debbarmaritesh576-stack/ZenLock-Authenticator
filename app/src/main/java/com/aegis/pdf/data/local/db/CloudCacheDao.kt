package com.aegis.pdf.data.local.db  
  
import androidx.room.*  
import kotlinx.coroutines.flow.Flow  
  
/**  
 * Data Access Object for cloud caching logic.  
 */  
@Dao  
interface CloudCacheDao {  
  
    // Get files for a specific provider (e.g., show only Dropbox files)  
    @Query("SELECT * FROM cloud_cache WHERE provider = :providerName ORDER BY name ASC")  
    fun getFilesByProvider(providerName: String): Flow<List<CloudCacheEntity>>  
  
    // Insert new files from cloud (Refresh logic)  
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    suspend fun insertFiles(files: List<CloudCacheEntity>)  
  
    // Delete old cache for a provider before updating  
    @Query("DELETE FROM cloud_cache WHERE provider = :providerName")  
    suspend fun clearCacheByProvider(providerName: String)  
  
    // Search files locally across all clouds  
    @Query("SELECT * FROM cloud_cache WHERE name LIKE '%' || :query || '%'")  
    suspend fun searchFiles(query: String): List<CloudCacheEntity>  
  
    @Transaction  
    suspend fun refreshCache(providerName: String, files: List<CloudCacheEntity>) {  
        clearCacheByProvider(providerName)  
        insertFiles(files)  
    }  
}