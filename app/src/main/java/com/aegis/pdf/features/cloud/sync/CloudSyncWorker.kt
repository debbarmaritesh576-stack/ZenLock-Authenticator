package com.aegis.pdf.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CloudSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CloudSyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val syncType = inputData.getString("sync_type") ?: "full"
            val filePath = inputData.getString("file_path")
            val fileId = inputData.getString("file_id")

            when (syncType) {
                "upload" -> uploadFile(filePath!!)
                "download" -> downloadFile(fileId!!, filePath!!)
                "delete" -> deleteFile(fileId!!)
                "list" -> listFiles()
                "full" -> performFullSync()
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.retry()
        }
    }

    private suspend fun uploadFile(localPath: String): Result {
        return try {
            val driveService = GoogleDriveManager.getInstance(applicationContext)
            val localFile = java.io.File(localPath)
            
            if (!localFile.exists()) {
                return Result.failure()
            }

            val fileMetadata = File().apply {
                name = localFile.name
                mimeType = "application/pdf"
                parents = listOf("root")
            }

            val mediaContent = FileContent("application/pdf", localFile)
            val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, size, modifiedTime")
                .execute()

            // Save sync record to local database
            saveSyncRecord(
                SyncRecord(
                    localPath = localPath,
                    driveId = uploadedFile.id,
                    fileName = uploadedFile.name,
                    fileSize = uploadedFile.size ?: 0,
                    modifiedTime = uploadedFile.modifiedTime?.value ?: 0,
                    syncStatus = SyncStatus.SYNCED
                )
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun downloadFile(driveId: String, localPath: String): Result {
        return try {
            val driveService = GoogleDriveManager.getInstance(applicationContext)
            val localFile = java.io.File(localPath)
            
            // Create parent directories
            localFile.parentFile?.mkdirs()

            FileOutputStream(localFile).use { outputStream ->
                driveService.files().get(driveId).executeMediaAndDownloadTo(outputStream)
            }

            // Verify download
            if (localFile.exists() && localFile.length() > 0) {
                saveSyncRecord(
                    SyncRecord(
                        localPath = localPath,
                        driveId = driveId,
                        syncStatus = SyncStatus.SYNCED
                    )
                )
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun deleteFile(driveId: String): Result {
        return try {
            val driveService = GoogleDriveManager.getInstance(applicationContext)
            driveService.files().delete(driveId).execute()
            deleteSyncRecord(driveId)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun listFiles(): Result {
        return try {
            val driveService = GoogleDriveManager.getInstance(applicationContext)
            
            val result = driveService.files().list()
                .setQ("mimeType='application/pdf' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, size, modifiedTime)")
                .setOrderBy("modifiedTime desc")
                .setPageSize(50)
                .execute()

            val files = result.files ?: emptyList()
            
            val syncData = files.map { file ->
                SyncRecord(
                    driveId = file.id,
                    fileName = file.name ?: "Unknown",
                    fileSize = file.size ?: 0,
                    modifiedTime = file.modifiedTime?.value ?: 0
                )
            }

            saveSyncList(syncData)
            
            val outputData = androidx.work.Data.Builder()
                .putInt("file_count", files.size)
                .build()

            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "List files failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun performFullSync(): Result {
        return try {
            // Step 1: List all remote files
            listFiles()

            // Step 2: Get all pending local uploads
            val pendingUploads = getPendingUploads()

            // Step 3: Upload pending files
            pendingUploads.forEach { record ->
                uploadFile(record.localPath)
            }

            // Step 4: Download updated files
            val outdatedFiles = getOutdatedFiles()
            outdatedFiles.forEach { record ->
                downloadFile(record.driveId, record.localPath)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed: ${e.message}")
            Result.retry()
        }
    }

    private fun saveSyncRecord(record: SyncRecord) {
        applicationContext.openFileOutput("sync_records.json", Context.MODE_APPEND).use { fos ->
            fos.write("${com.google.gson.Gson().toJson(record)}\n".toByteArray())
        }
    }

    private fun deleteSyncRecord(driveId: String) {
        // Implementation for removing sync record
    }

    private fun saveSyncList(records: List<SyncRecord>) {
        applicationContext.openFileOutput("sync_list.json", Context.MODE_PRIVATE).use { fos ->
            fos.write(com.google.gson.Gson().toJson(records).toByteArray())
        }
    }

    private fun getPendingUploads(): List<SyncRecord> {
        return emptyList()
    }

    private fun getOutdatedFiles(): List<SyncRecord> {
        return emptyList()
    }
}

enum class SyncStatus {
    PENDING, SYNCING, SYNCED, FAILED, CONFLICT
}

data class SyncRecord(
    val localPath: String = "",
    val driveId: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val modifiedTime: Long = 0,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)