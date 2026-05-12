package com.aegis.pdf.features.organizer.data.manager

import com.aegis.pdf.features.organizer.data.local.dao.FolderDao
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity
import com.aegis.pdf.features.organizer.domain.result.FolderResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderManager @Inject constructor(
    private val folderDao: FolderDao
) {

    suspend fun createFolder(
        name: String,
        parentId: String? = null
    ): FolderResult<FolderEntity> {
        return withContext(Dispatchers.IO) {
            try {
                if (name.isBlank()) {
                    return@withContext FolderResult.Error("Folder name cannot be empty")
                }

                val folder = FolderEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name.trim(),
                    parentId = parentId
                )
                folderDao.insert(folder)
                FolderResult.Success(folder)
            } catch (e: Exception) {
                FolderResult.Error("Failed to create folder", e)
            }
        }
    }

    suspend fun renameFolder(
        folderId: String,
        newName: String
    ): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (newName.isBlank()) {
                    return@withContext FolderResult.Error("Folder name cannot be empty")
                }

                val folder = folderDao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                folderDao.update(
                    folder.copy(
                        name = newName.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to rename folder", e)
            }
        }
    }

    suspend fun deleteFolder(folderId: String): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = folderDao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                deleteRecursive(folder)
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to delete folder", e)
            }
        }
    }

    private suspend fun deleteRecursive(folder: FolderEntity) {
        val children = folderDao.getSubFolders(folder.id)
        children.forEach { deleteRecursive(it) }
        folderDao.delete(folder)
    }

    suspend fun moveFolder(
        folderId: String,
        newParentId: String?
    ): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = folderDao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                if (newParentId != null && newParentId == folderId) {
                    return@withContext FolderResult.Error("Cannot move folder to itself")
                }

                if (newParentId != null) {
                    val parent = folderDao.getFolder(newParentId)
                        ?: return@withContext FolderResult.Error("Target folder not found")

                    val isCyclicMove = isFolderDescendant(newParentId, folderId)
                    if (isCyclicMove) {
                        return@withContext FolderResult.Error(
                            "Cannot move folder to its own subfolder"
                        )
                    }
                }

                folderDao.update(folder.copy(parentId = newParentId))
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to move folder", e)
            }
        }
    }

    suspend fun getFolderPath(folderId: String): FolderResult<List<FolderEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val path = mutableListOf<FolderEntity>()
                var current = folderDao.getFolder(folderId)

                while (current != null) {
                    path.add(0, current)
                    current = current.parentId?.let { folderDao.getFolder(it) }
                }

                FolderResult.Success(path)
            } catch (e: Exception) {
                FolderResult.Error("Failed to get folder path", e)
            }
        }
    }

    private suspend fun isFolderDescendant(
        parentId: String,
        folderId: String
    ): Boolean {
        var current = folderDao.getFolder(parentId)
        while (current != null) {
            if (current.id == folderId) {
                return true
            }
            current = current.parentId?.let { folderDao.getFolder(it) }
        }
        return false
    }
}