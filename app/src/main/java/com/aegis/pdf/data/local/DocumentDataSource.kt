package com.aegis.pdf.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tempFiles = mutableSetOf<File>()

    fun getFileName(uri: Uri): String {
        var name = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    fun getFileSize(uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    fun copyToTemp(uri: Uri): File? {
        return try {
            val name = getFileName(uri)
            val tempFile = File(context.cacheDir, "aegis_${System.currentTimeMillis()}_$name")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            tempFiles.add(tempFile)
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun deleteAll(vararg files: File) {
        files.forEach { file ->
            try {
                if (file.exists()) file.delete()
                tempFiles.remove(file)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteAll(files: List<File>) {
        files.forEach { file ->
            try {
                if (file.exists()) file.delete()
                tempFiles.remove(file)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun cleanupTempFiles() {
        tempFiles.forEach { file ->
            try {
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                // Ignore
            }
        }
        tempFiles.clear()
    }
}