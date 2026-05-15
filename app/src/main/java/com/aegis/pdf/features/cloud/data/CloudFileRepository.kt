package com.aegis.pdf.data.cloud  
  
import android.content.Context  
import com.aegis.pdf.data.local.RecentFileDao  
import com.aegis.pdf.data.local.RecentFileEntity  
import dagger.hilt.android.qualifiers.ApplicationContext  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import java.io.File  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class CloudFileRepository @Inject constructor(  
    @ApplicationContext private val context: Context,  
    private val googleDriveManager: GoogleDriveManager,  
    private val dropboxManager: DropboxManager,  
    private val oneDriveManager: OneDriveManager,  
    private val recentFileDao: RecentFileDao,  
    private val cloudAuthManager: CloudAuthManager  
) {  
  
    enum class Provider { GOOGLE_DRIVE, DROPBOX, ONEDRIVE }  
  
    /**  
     * Common interface ke through provider return karta hai.  
     * Note: Managers ko ab interface implement karna hoga.  
     */  
    fun getProvider(provider: Provider): CloudStorageProvider {  
        return when (provider) {  
            Provider.GOOGLE_DRIVE -> googleDriveManager  
            Provider.DROPBOX -> dropboxManager  
            Provider.ONEDRIVE -> oneDriveManager  
        }  
    }  
  
    /**  
     * Local DB mein entry karta hai taaki user "Recent" tab mein cloud file dekh sake.  
     */  
    suspend fun syncFileToRecent(file: File, provider: Provider) = withContext(Dispatchers.IO) {  
        val entity = RecentFileEntity(  
            name = file.name,  
            path = file.absolutePath,  
            size = file.length(),  
            timestamp = System.currentTimeMillis(),  
            sourceProvider = provider.name // Track karo ki file kahan se aayi  
        )  
        recentFileDao.insertFile(entity)  
    }  
  
    fun isAuthenticated(provider: Provider): Boolean {  
        // Case-sensitive safety: lowercase() use karein  
        return cloudAuthManager.isAuthenticated(provider.name.lowercase())  
    }  
  
    fun getDownloadDir(): File {  
        return File(context.cacheDir, "cloud_downloads").apply {  
            if (!exists()) mkdirs()  
        }  
    }  
  
    /**  
     * Cleanup: Purane cloud downloads delete karne ke liye  
     */  
    fun clearTempDownloads() {  
        getDownloadDir().deleteRecursively()  
    }  
}