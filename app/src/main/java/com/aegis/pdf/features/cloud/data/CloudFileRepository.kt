package com.aegis.pdf.data.cloud

import android.content.Context
import com.aegis.pdf.data.local.RecentFileDao
import com.aegis.pdf.data.local.RecentFileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun getProvider(provider: Provider): CloudStorageProvider {
        return when (provider) {
            Provider.GOOGLE_DRIVE -> googleDriveManager
            Provider.DROPBOX -> dropboxManager
            Provider.ONEDRIVE -> oneDriveManager
        }
    }

    suspend fun syncFileToRecent(file: File, provider: Provider) {
        recentFileDao.insertFile(
            RecentFileEntity(
                name = "[${provider.name}] ${file.name}",
                path = file.absolutePath,
                size = file.length()
            )
        )
    }

    fun getDownloadDir(): File {
        val dir = File(context.filesDir, "cloud_downloads")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun isAuthenticated(provider: Provider): Boolean {
        return cloudAuthManager.isAuthenticated(provider.name)
    }
}

interface CloudStorageProvider {
    fun connect()
    fun listPdfFiles(): List<CloudPdfFile>
    fun downloadFile(fileId: String): File?
    fun uploadFile(file: File): Boolean
}