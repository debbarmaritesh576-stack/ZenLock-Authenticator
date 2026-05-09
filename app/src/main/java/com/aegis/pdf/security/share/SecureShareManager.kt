package com.aegis.browser.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SecureShareManager(private val context: Context) {

    companion object {
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH = 256
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val SALT_LENGTH = 16
    }

    /**
     * Encrypt file with password using AES-256-GCM + PBKDF2
     */
    fun encryptFile(
        inputPath: String,
        outputPath: String,
        password: String
    ): EncryptionResult {
        return try {
            // Generate cryptographically secure random salt
            val salt = ByteArray(SALT_LENGTH)
            SecureRandom().nextBytes(salt)

            // Generate random IV for AES-GCM
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // Derive AES-256 key from password using PBKDF2
            val key = deriveKey(password, salt)

            // Encrypt with AES-256-GCM
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            val inputFile = File(inputPath)
            val fileData = inputFile.readBytes()
            val encryptedData = cipher.doFinal(fileData)

            // Write: salt + iv + ciphertext
            FileOutputStream(File(outputPath)).use { fos ->
                fos.write(salt)           // 16 bytes
                fos.write(iv)             // 12 bytes
                fos.write(encryptedData)  // encrypted content + GCM tag
            }

            EncryptionResult.Success(outputPath)
        } catch (e: Exception) {
            EncryptionResult.Error("Encryption failed: ${e.message}")
        }
    }

    /**
     * Decrypt file with password
     */
    fun decryptFile(
        inputPath: String,
        outputPath: String,
        password: String
    ): EncryptionResult {
        return try {
            val encryptedFile = File(inputPath)
            val encryptedBytes = encryptedFile.readBytes()

            // Extract salt (first 16 bytes)
            val salt = encryptedBytes.copyOfRange(0, SALT_LENGTH)

            // Extract IV (next 12 bytes)
            val iv = encryptedBytes.copyOfRange(SALT_LENGTH, SALT_LENGTH + GCM_IV_LENGTH)

            // Extract ciphertext (remaining bytes)
            val ciphertext = encryptedBytes.copyOfRange(SALT_LENGTH + GCM_IV_LENGTH, encryptedBytes.size)

            // Derive key from password using same PBKDF2
            val key = deriveKey(password, salt)

            // Decrypt with AES-256-GCM
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            val decryptedData = cipher.doFinal(ciphertext)

            // Write decrypted file
            FileOutputStream(File(outputPath)).use { fos ->
                fos.write(decryptedData)
            }

            EncryptionResult.Success(outputPath)
        } catch (e: javax.crypto.AEADBadTagException) {
            EncryptionResult.Error("Wrong password or corrupted file")
        } catch (e: Exception) {
            EncryptionResult.Error("Decryption failed: ${e.message}")
        }
    }

    /**
     * Derive AES-256 key from password using PBKDF2-HMAC-SHA256
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Share encrypted file via intent
     */
    fun shareEncryptedFile(
        filePath: String,
        fileName: String,
        password: String
    ): Intent? {
        return try {
            val encryptedDir = File(context.cacheDir, "encrypted_share")
            encryptedDir.mkdirs()

            val encryptedFile = File(encryptedDir, "${fileName}.aegis")
            val result = encryptFile(filePath, encryptedFile.absolutePath, password)

            if (result is EncryptionResult.Success) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    encryptedFile
                )

                Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Encrypted: $fileName")
                    putExtra(Intent.EXTRA_TEXT, "File encrypted with Aegis Browser")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate file hash for integrity verification
     */
    fun calculateFileHash(filePath: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        FileInputStream(File(filePath)).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    sealed class EncryptionResult {
        data class Success(val path: String) : EncryptionResult()
        data class Error(val message: String) : EncryptionResult()
    }
}