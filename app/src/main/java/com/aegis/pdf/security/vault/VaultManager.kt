package com.aegis.pdf.core.security

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val vaultDir: File
        get() {
            val dir = File(context.filesDir, "vault")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun addToVault(sourceFile: File): Boolean {
        return try {
            val encryptedFile = File(vaultDir, "${sourceFile.name}.aegis")
            EncryptedFile.Builder(
                context,
                encryptedFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build().apply {
                openFileOutput().use { output ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeFromVault(fileName: String): File? {
        return try {
            val encryptedFile = File(vaultDir, fileName)
            if (!encryptedFile.exists()) return null

            val outputFile = File(context.cacheDir, fileName.removeSuffix(".aegis"))
            EncryptedFile.Builder(
                context,
                encryptedFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build().apply {
                openFileInput().use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            encryptedFile.delete()
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    fun getVaultFiles(): List<VaultFile> {
        return vaultDir.listFiles()
            ?.filter { it.extension == "aegis" }
            ?.map { file ->
                VaultFile(
                    name = file.name.removeSuffix(".aegis"),
                    size = file.length(),
                    encryptedPath = file.absolutePath
                )
            } ?: emptyList()
    }

    fun deleteFromVault(fileName: String): Boolean {
        return File(vaultDir, fileName).delete()
    }

    fun isFileInVault(fileName: String): Boolean {
        return File(vaultDir, "$fileName.aegis").exists()
    }
}

data class VaultFile(
    val name: String,
    val size: Long,
    val encryptedPath: String
)