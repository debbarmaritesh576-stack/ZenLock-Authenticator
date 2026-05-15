package com.aegis.pdf.data.local.db  
  
import androidx.room.Dao  
import androidx.room.Insert  
import androidx.room.OnConflictStrategy  
import androidx.room.Query  
  
@Dao  
interface OcrSearchDao {  
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    suspend fun insertContent(content: OcrSearchEntity)  
  
    // Match query poori PDF ke content mein search karegi  
    @Query("""  
        SELECT * FROM pdf_content_search   
        WHERE extractedText MATCH :query  
    """)  
    suspend fun searchInsidePdfs(query: String): List<OcrSearchEntity>  
}