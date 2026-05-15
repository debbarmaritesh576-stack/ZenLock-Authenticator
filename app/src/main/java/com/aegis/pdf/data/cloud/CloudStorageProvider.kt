package com.aegis.pdf.data.cloud  
  
import com.aegis.pdf.data.cloud.model.CloudPdfFile  
import java.io.File  
  
/**  
 * Common interface for all cloud storage services.  
 */  
interface CloudStorageProvider {  
      
    // Auth and Connection  
    suspend fun connect()  
      
    // File Operations  
    suspend fun listPdfFiles(): List<CloudPdfFile>  
      
    suspend fun downloadFile(fileId: String): File?  
      
    suspend fun uploadFile(file: File): Boolean  
      
    // Check if account is still linked  
    fun isConnected(): Boolean  
}