package com.aegis.pdf.data.cloud  
  
import android.content.Context  
import android.util.Log  
import com.aegis.pdf.data.cloud.CloudAuthManager  
import dagger.hilt.android.qualifiers.ApplicationContext  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import okhttp3.OkHttpClient  
import okhttp3.Request  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class OneDriveManager @Inject constructor(  
    @ApplicationContext private val context: Context,  
    private val authManager: CloudAuthManager  
) {  
    companion object {  
        private const val TAG = "AegisOneDrive"  
        private const val PROVIDER = "onedrive"  
        private const val BASE_URL = "https://graph.microsoft.com/v1.0"  
    }  
  
    private val client = OkHttpClient.Builder()  
        .followRedirects(true) // Microsoft redirects ke liye zaroori hai  
        .build()  
  
    /**  
     * Search all PDF files across the user's OneDrive  
     */  
    suspend fun listPdfFiles(): List<CloudPdfFile> = withContext(Dispatchers.IO) {  
        val accessToken = authManager.getToken(PROVIDER) ?: return@withContext emptyList()  
        val files = mutableListOf<CloudPdfFile>()  
  
        val request = Request.Builder()  
            .url("$BASE_URL/me/drive/root/search(q='.pdf')")  
            .addHeader("Authorization", "Bearer $accessToken")  
            .get()  
            .build()  
  
        try {  
            client.newCall(request).execute().use { response ->  
                if (response.isSuccessful) {  
                    val jsonResponse = response.body?.string()  
                    // Yahan aap apna JSON parsing logic (Gson/Kotlin Serialization) lagao  
                    Log.d(TAG, "Files found: $jsonResponse")  
                }  
            }  
        } catch (e: Exception) {  
            Log.e(TAG, "Search failed: ${e.message}")  
        }  
        files  
    }  
  
    /**  
     * Download with Chunked Streaming to prevent OOM  
     */  
    suspend fun downloadFile(fileId: String): File? = withContext(Dispatchers.IO) {  
        val accessToken = authManager.getToken(PROVIDER) ?: return@withContext null  
        val outputFile = File(context.cacheDir, "onedrive_${System.currentTimeMillis()}.pdf")  
  
        val request = Request.Builder()  
            .url("$BASE_URL/me/drive/items/$fileId/content")  
            .addHeader("Authorization", "Bearer $accessToken")  
            .get()  
            .build()  
  
        try {  
            client.newCall(request).execute().use { response ->  
                if (!response.isSuccessful) return@withContext null  
  
                response.body?.byteStream()?.use { input ->  
                    outputFile.outputStream().use { output ->  
                        input.copyTo(output) // Efficient byte-by-byte copy  
                    }  
                }  
                outputFile  
            }  
        } catch (e: Exception) {  
            Log.e(TAG, "OneDrive download failed: ${e.message}")  
            null  
        }  
    }  
}