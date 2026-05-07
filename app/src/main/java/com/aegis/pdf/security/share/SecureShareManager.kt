package com.aegis.pdf.core.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val expiryTimes = mutableMapOf<String, Long>()

    fun shareWithExpiry(
        file: File,
        expiryMinutes: Int
    ): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiryMinutes.toLong())
        expiryTimes[file.name] = expiryTime

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Shared via Aegis PDF (Expires in $expiryMinutes min)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun isLinkExpired(fileName: String): Boolean {
        val expiryTime = expiryTimes[fileName] ?: return false
        return System.currentTimeMillis() > expiryTime
    }

    fun createPasswordProtectedShare(
        file: File,
        password: String
    ): File? {
        return try {
            val encryptedFile = File(context.cacheDir, "share_${file.name}")
            // Encrypt file with password
            encryptWithPassword(file, encryptedFile, password)
            encryptedFile
        } catch (e: Exception) {
            null
        }
    }

    private fun encryptWithPassword(
        inputFile: File,
        outputFile: File,
        password: String
    ) {
        val key = javax.crypto.spec.SecretKeySpec(password.toByteArray().take(16).toByteArray(), "AES")
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encrypted = cipher.doFinal(inputFile.readBytes())

        outputFile.outputStream().use { output ->
            output.write(iv)
            output.write(encrypted)
        }
    }
}