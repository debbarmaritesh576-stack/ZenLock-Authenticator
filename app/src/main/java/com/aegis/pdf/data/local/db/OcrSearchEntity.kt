package com.aegis.pdf.data.local.db  
  
import androidx.room.Entity  
import androidx.room.Fts4 // Ya Fts5 agar aapka SQLite version support kare  
import androidx.room.PrimaryKey  
  
@Fts4 // Full Text Search support  
@Entity(tableName = "pdf_content_search")  
data class OcrSearchEntity(  
    val fileId: String,       // Original File ID (Local ya Cloud)  
    val fileName: String,     // Display name  
    val extractedText: String // Poora text jo OCR ne nikala  
)