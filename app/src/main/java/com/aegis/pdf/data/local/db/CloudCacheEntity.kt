package com.aegis.pdf.data.local.db  
  
import androidx.room.Entity  
import androidx.room.Index  
import androidx.room.PrimaryKey  
  
/**  
 * Room Entity to cache cloud file metadata for offline viewing.  
 */  
@Entity(  
    tableName = "cloud_cache",  
    indices = [Index(value = ["provider"])] // Fast searching by provider  
)  
data class CloudCacheEntity(  
    @PrimaryKey val id: String,        // Unique ID from cloud  
    val name: String,  
    val size: String,  
    val provider: String,              // Helps filter files in DB  
    val lastUpdated: Long = System.currentTimeMillis()  
)