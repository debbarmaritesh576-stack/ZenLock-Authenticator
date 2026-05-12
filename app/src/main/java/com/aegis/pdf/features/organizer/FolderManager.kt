package com.aegis.pdf.features.organizer

import android.content.Context
import androidx.room.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String? = null,
    val color: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY name")
    suspend fun getRootFolders(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY name")
    suspend fun getSubFolders(parentId: String): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolder(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("SELECT * FROM folders ORDER BY name")
    suspend fun getAllFolders(): List<FolderEntity>
}

sealed class FolderResult<out T> {
    data class Success<T>(val data: T) : FolderResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : FolderResult<Nothing>()
}

@Singleton
class FolderManager @Inject constructor(
    private val db: AppDatabase
) {
    private val dao get() = db.folderDao()

    suspend fun createFolder(name: String, parentId: String? = null): FolderResult<FolderEntity> {
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
                dao.insert(folder)
                FolderResult.Success(folder)
            } catch (e: Exception) {
                FolderResult.Error("Failed to create folder", e)
            }
        }
    }

    suspend fun renameFolder(folderId: String, newName: String): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (newName.isBlank()) {
                    return@withContext FolderResult.Error("Folder name cannot be empty")
                }

                val folder = dao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                dao.update(folder.copy(name = newName.trim(), updatedAt = System.currentTimeMillis()))
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to rename folder", e)
            }
        }
    }

    suspend fun deleteFolder(folderId: String): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = dao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                deleteRecursive(folder)
                FolderResult.Success(Unit)
            } catch (e: Exception) {
                FolderResult.Error("Failed to delete folder", e)
            }
        }
    }

    private suspend fun deleteRecursive(folder: FolderEntity) {
        val children = dao.getSubFolders(folder.id)
        children.forEach { deleteRecursive(it) }
        dao.delete(folder)
    }

    suspend fun moveFolder(folderId: String, newParentId: String?): FolderResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = dao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                if (newParentId != null && newParentId == folderId) {
                    return@withContext FolderResult.Error("Cannot move folder to itself")
                }

                if (newParentId != null) {
                    val parent = dao.getFolder(newParentId)
                        ?: return@withContext FolderResult.Error("Target folder not found")
                }

                dao.update(folder.copy(parentId = newParentId))
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
                var current = dao.getFolder(folderId)

                while (current != null) {
                    path.add(0, current)
                    current = current.parentId?.let { dao.getFolder(it) }
                }

                FolderResult.Success(path)
            } catch (e: Exception) {
                FolderResult.Error("Failed to get folder path", e)
            }
        }
    }

    suspend fun getFolderSize(folderId: String): FolderResult<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = dao.getFolder(folderId)
                    ?: return@withContext FolderResult.Error("Folder not found")

                val size = calculateFolderSize(folder)
                FolderResult.Success(size)
            } catch (e: Exception) {
                FolderResult.Error("Failed to calculate folder size", e)
            }
        }
    }

    private suspend fun calculateFolderSize(folder: FolderEntity): Long {
        var size = 0L
        val children = dao.getSubFolders(folder.id)
        children.forEach { size += calculateFolderSize(it) }
        return size
    }

    suspend fun getRootFolders(): FolderResult<List<FolderEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val folders = dao.getRootFolders()
                FolderResult.Success(folders)
            } catch (e: Exception) {
                FolderResult.Error("Failed to load root folders", e)
            }
        }
    }

    suspend fun getSubFolders(parentId: String): FolderResult<List<FolderEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val folders = dao.getSubFolders(parentId)
                FolderResult.Success(folders)
            } catch (e: Exception) {
                FolderResult.Error("Failed to load subfolders", e)
            }
        }
    }

    suspend fun getFolder(id: String): FolderResult<FolderEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = dao.getFolder(id)
                    ?: return@withContext FolderResult.Error("Folder not found")
                FolderResult.Success(folder)
            } catch (e: Exception) {
                FolderResult.Error("Failed to load folder", e)
            }
        }
    }
}