package com.aegis.pdf.features.organizer.data.repository

import com.aegis.pdf.features.organizer.data.local.dao.FolderDao
import com.aegis.pdf.features.organizer.data.local.dao.RecentFileDao
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity
import com.aegis.pdf.features.organizer.domain.result.FolderResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val fileDao: RecentFileDao
) {

    suspend fun getRootFolders(): FolderResult<List<FolderEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val folders = folderDao.getRootFolders()
                FolderResult.Success(folders)
            } catch (e: Exception) {
                FolderResult.Error("Failed to load folders", e)
            }
        }
    }

    suspend fun getSubFolders(parentId: String): FolderResult<List<FolderEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val folders = folderDao.getSubFolders(parentId)
                FolderResult.Success(folders)
            } catch (e: Exception) {
                FolderResult.Error("Failed to load subfolders", e)
            }
        }
    }

    suspend fun getFolder(id: String): FolderResult<FolderEntity?> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = folderDao.getFolder(id)
                FolderResult.Success(folder)
            } catch (e: Exception) {
                FolderResult.Error("Failed to load folder", e)
            }
        }
    }

    suspend fun insertFolder(folder: FolderEntity): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                folderDao.insert(folder)
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to insert folder", e)
            }
        }
    }

    suspend fun updateFolder(folder: FolderEntity): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                folderDao.update(folder)
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to update folder", e)
            }
        }
    }

    suspend fun deleteFolder(folder: FolderEntity): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                folderDao.delete(folder)
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to delete folder", e)
            }
        }
    }

    suspend fun getFilesInFolder(folderId: String?): List<RecentFileEntity> {
        return withContext(Dispatchers.IO) {
            try {
                fileDao.getAllFiles()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getFolderFilesSize(folderId: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                val files = fileDao.getAllFiles()
                files.sumOf { it.fileSize }
            } catch (e: Exception) {
                0L
            }
        }
    }
}