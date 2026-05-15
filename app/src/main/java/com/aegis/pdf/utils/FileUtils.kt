package com.aegis.pdf.utils  
  
import android.content.Context  
import android.net.Uri  
import android.provider.OpenableColumns  
import android.util.Log  
import java.io.File  
import java.io.FileOutputStream  
import java.io.InputStream  
import java.util.Locale  
  
object FileUtils {  
    private const val TAG = "AegisFileUtils"  
  
    /**  
     * File name nikalne ke liye safe method.   
     * Fallback strategy ke saath taaki -1 index par crash na ho.  
     */  
    fun getFileName(context: Context, uri: Uri): String {  
        var name = "document_${System.currentTimeMillis()}.pdf"  
        try {  
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->  
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)  
                if (nameIndex != -1 && cursor.moveToFirst()) {  
                    name = cursor.getString(nameIndex)  
                }  
            }  
        } catch (e: Exception) {  
            Log.e(TAG, "Error getting file name: ${e.message}")  
            // Fallback: Uri se name nikalne ki koshish  
            uri.path?.let { path ->  
                val lastSlash = path.lastIndexOf('/')  
                if (lastSlash != -1) name = path.substring(lastSlash + 1)  
            }  
        }  
        return name  
    }  
  
    /**  
     * File size accurate (Double precision) format mein dikhane ke liye.  
     */  
    fun formatSize(bytes: Long): String {  
        if (bytes <= 0) return "0 B"  
        val units = arrayOf("B", "KB", "MB", "GB", "TB")  
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()  
        return String.format(  
            Locale.getDefault(),   
            "%.2f %s",   
            bytes / Math.pow(1024.0, digitGroups.toDouble()),   
            units[digitGroups]  
        )  
    }  
  
    /**  
     * Uri ko internal cache mein copy karne ke liye.   
     * C++ engine ko path dene se pehle ye zaroori hai.  
     */  
    fun copyUriToTempFile(context: Context, uri: Uri): File? {  
        val fileName = getFileName(context, uri)  
        val tempFile = File(context.cacheDir, fileName)  
          
        return try {  
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)  
            inputStream?.use { input ->  
                FileOutputStream(tempFile).use { output ->  
                    // 8KB buffer size optimization for faster copy  
                    val buffer = ByteArray(8192)  
                    var read: Int  
                    while (input.read(buffer).also { read = it } != -1) {  
                        output.write(buffer, 0, read)  
                    }  
                    output.flush()  
                }  
            }  
            tempFile  
        } catch (e: Exception) {  
            Log.e(TAG, "Copy failed: ${e.message}")  
            null  
        }  
    }  
  
    /**  
     * Cache clear karne ke liye method.  
     */  
    fun clearCache(context: Context) {  
        try {  
            context.cacheDir.deleteRecursively()  
        } catch (e: Exception) {  
            Log.e(TAG, "Failed to clear cache")  
        }  
    }  
}