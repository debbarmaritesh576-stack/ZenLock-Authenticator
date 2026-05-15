package com.aegis.pdf.data.cloud.model  
  
/**  
 * Common model representing a PDF file from any cloud provider.  
 */  
data class CloudPdfFile(  
    val id: String,           // Cloud provider's unique ID  
    val name: String,         // Filename (e.g., "Resume.pdf")  
    val size: String,         // Human-readable size (e.g., "2.5 MB")  
    val provider: String,     // "GOOGLE_DRIVE", "DROPBOX", or "ONEDRIVE"  
    val lastModified: Long = System.currentTimeMillis(),  
    val downloadUrl: String? = null // Optional temporary link  
)