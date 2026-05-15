package com.aegis.pdf.data.cloud  
  
import android.content.Context  
import android.util.Log  
import com.aegis.pdf.data.cloud.CloudAuthManager  
import dagger.hilt.android.qualifiers.ApplicationContext  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import okhttp3.MediaType.Companion.toMediaType  
import okhttp3.OkHttpClient  
import okhttp3.Request  
import okhttp3.RequestBody.Companion.asRequestBody  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class DropboxManager @Inject constructor(  
    @ApplicationContext private val context: Context,  
    private val authManager: CloudAuthManager // Aapka Secure Auth Manager  
) {  
    companion object {  
        private const val TAG = "AegisDropbox"  
        private const val PROVIDER = "dropbox"  
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()  
    }  
  
    private val client = OkHttpClient()  
  
    /**  
     * Download PDF from Dropbox and save to local cache  
     */  
    suspend fun downloadFile(dropboxPath: String): File? = withContext(Dispatchers.IO) {  
        val accessToken = authManager.getToken(PROVIDER) ?: return@withContext null  
        val outputFile = File(context.cacheDir, "dropbox_${System.currentTimeMillis()}.pdf")  
  
        // Dropbox uses a specific header for arguments in download  
        val apiArg = """{"path": "$dropboxPath"}"""  
  
        val request = Request.Builder()  
            .url("https://content.dropboxapi.com/2/files/download")  
            .addHeader("Authorization", "Bearer $accessToken")  
            .addHeader("Dropbox-API-Arg", apiArg)  
            .post(okhttp3.internal.EMPTY_REQUEST) // POST with empty body  
            .build()  
  
        try {  
            client.newCall(request).execute().use { response ->  
                if (!response.isSuccessful) return@withContext null  
                  
                response.body?.byteStream()?.use { input ->  
                    outputFile.outputStream().use { output ->  
                        input.copyTo(output)  
                    }  
                }  
                outputFile  
            }  
        } catch (e: Exception) {  
            Log.e(TAG, "Download failed: ${e.message}")  
            null  
        }  
    }  
  
    /**  
     * Upload PDF to Dropbox using Streaming (No Memory Crash)  
     */  
    suspend fun uploadFile(file: File): Boolean = withContext(Dispatchers.IO) {  
        val accessToken = authManager.getToken(PROVIDER) ?: return@withContext false  
          
        // Dropbox-API-Arg needs to be JSON formatted  
        val apiArg = """{"path": "/${file.name}", "mode": "add", "autorename": true, "mute": false}"""  
  
        val request = Request.Builder()  
            .url("https://content.dropboxapi.com/2/files/upload")  
            .addHeader("Authorization", "Bearer $accessToken")  
            .addHeader("Dropbox-API-Arg", apiArg)  
            .addHeader("Content-Type", "application/octet-stream")  
            .post(file.asRequestBody("application/pdf".toMediaType())) // STREAMS the file from disk  
            .build()  
  
        try {  
            client.newCall(request).execute().use { response ->  
                if (response.isSuccessful) {  
                    Log.d(TAG, "Upload success: ${file.name}")  
                    true  
                } else {  
                    Log.e(TAG, "Upload failed: ${response.code} ${response.message}")  
                    false  
                }  
            }  
        } catch (e: Exception) {  
            Log.e(TAG, "Upload error: ${e.message}")  
            false  
        }  
    }  
}