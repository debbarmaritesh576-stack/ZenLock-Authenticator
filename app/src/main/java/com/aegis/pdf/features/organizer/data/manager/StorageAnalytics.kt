package com.aegis.pdf.features.organizer.data.manager

import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long,
    val appUsedSpace: Long,
    val pdfCount: Int,
    val totalPdfSize: Long
)

data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)

data class DuplicateGroup(
    val hash: String,
    val files: List<FileInfo>
)

@Singleton
class StorageAnalytics @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getStorageInfo(): StorageInfo {
        return withContext(Dispatchers.IO) {
            try {
                val stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)
                val totalSpace = stat.totalBytes
                val freeSpace = stat.availableBytes
                val usedSpace = totalSpace - freeSpace

                val appDir = context.filesDir
                val appSpace = getFolderSize(appDir)
                val pdfInfo = getPdfStatistics()

                StorageInfo(
                    totalSpace = totalSpace,
                    freeSpace = freeSpace,
                    usedSpace = usedSpace,
                    appUsedSpace = appSpace,
                    pdfCount = pdfInfo.first,
                    totalPdfSize = pdfInfo.second
                )
            } catch (e: Exception) {
                StorageInfo(0, 0, 0, 0, 0, 0)
            }
        }
    }

    suspend fun getFileTypeBreakdown(): Map<String, Long> {
        return withContext(Dispatchers.IO) {
            try {
                val breakdown = mutableMapOf<String, Long>()
                val appDir = context.filesDir

                walkFiles(appDir) { file ->
                    val ext = file.extension.lowercase()
                    val type = when (ext) {
                        "pdf" -> "PDF"
                        "jpg", "jpeg", "png", "gif", "webp" -> "Images"
                        "doc", "docx", "txt" -> "Documents"
                        "mp4", "avi", "mkv" -> "Videos"
                        else -> "Other"
                    }
                    breakdown[type] = breakdown.getOrDefault(type, 0L) + file.length()
                }

                if (breakdown.isEmpty()) {
                    mapOf(
                        "PDF" to 0L,
                        "Images" to 0L,
                        "Documents" to 0L,
                        "Videos" to 0L,
                        "Other" to 0L
                    )
                } else {
                    breakdown
                }
            } catch (e: Exception) {
                mapOf(
                    "PDF" to 0L,
                    "Images" to 0L,
                    "Documents" to 0L,
                    "Videos" to 0L,
                    "Other" to 0L
                )
            }
        }
    }

    suspend fun getLargestFiles(count: Int = 10): List<FileInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val files = mutableListOf<FileInfo>()
                val appDir = context.filesDir

                walkFiles(appDir) { file ->
                    files.add(
                        FileInfo(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            lastModified = file.lastModified()
                        )
                    )
                }

                files.sortByDescending { it.size }.take(count)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getDuplicateFiles(): List<DuplicateGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val hashMap = mutableMapOf<String, MutableList<FileInfo>>()
                val appDir = context.filesDir

                walkFiles(appDir) { file ->
                    if (file.isFile && file.length() > 1024) {
                        val hash = calculateFileHash(file)
                        val fileInfo = FileInfo(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            lastModified = file.lastModified()
                        )
                        hashMap.getOrPut(hash) { mutableListOf() }.add(fileInfo)
                    }
                }

                hashMap.filter { it.value.size > 1 }
                    .map { (hash, files) -> DuplicateGroup(hash, files) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun clearAppCache(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                val initialSize = getFolderSize(cacheDir)
                deleteRecursive(cacheDir)
                cacheDir.mkdirs()
                initialSize
            } catch (e: Exception) {
                0L
            }
        }
    }

    suspend fun getAppStorage(): Long {
        return withContext(Dispatchers.IO) {
            try {
                getFolderSize(context.filesDir) + getFolderSize(context.cacheDir)
            } catch (e: Exception) {
                0L
            }
        }
    }

    private fun getFolderSize(file: File): Long {
        var size = 0L
        if (file.isDirectory) {
            file.listFiles()?.forEach { size += getFolderSize(it) }
        } else {
            size = file.length()
        }
        return size
    }

    private fun walkFiles(file: File, action: (File) -> Unit) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { walkFiles(it, action) }
        } else {
            action(file)
        }
    }

    private fun deleteRecursive(file: File): Boolean {
        return if (file.isDirectory) {
            file.listFiles()?.all { deleteRecursive(it) } ?: true && file.delete()
        } else {
            file.delete()
        }
    }

    private fun calculateFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            file.inputStream().use { input ->
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getPdfStatistics(): Pair<Int, Long> {
        return try {
            var count = 0
            var totalSize = 0L
            val appDir = context.filesDir

            walkFiles(appDir) { file ->
                if (file.extension.lowercase() == "pdf") {
                    count++
                    totalSize += file.length()
                }
            }

            Pair(count, totalSize)
        } catch (e: Exception) {
            Pair(0, 0L)
        }
    }
}