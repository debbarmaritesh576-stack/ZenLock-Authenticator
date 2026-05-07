package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import java.io.File

class PdfEncryptor {

    fun addPassword(
        inputFile: File,
        outputFile: File,
        password: String
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val accessPermission = AccessPermission()
                accessPermission.isReadOnly = false

                val policy = StandardProtectionPolicy(
                    password,
                    password,
                    accessPermission
                )
                policy.encryptionKeyLength = 128
                document.protectionPolicy = policy
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removePassword(
        inputFile: File,
        outputFile: File,
        password: String
    ): Boolean {
        return try {
            PDDocument.load(inputFile, password).use { document ->
                document.isAllSecurityToBeRemoved = true
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isPasswordProtected(file: File): Boolean {
        return try {
            PDDocument.load(file).use { it.isEncrypted }
        } catch (e: Exception) {
            true
        }
    }
}