package com.aegis.pdf.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var driveService: Drive? = null
    private var googleSignInClient: GoogleSignInClient? = null

    private val requiredScopes = listOf(
        DriveScopes.DRIVE_FILE,
        DriveScopes.DRIVE_READONLY
    )

    fun getSignInClient(): GoogleSignInClient {
        if (googleSignInClient == null) {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
        }
        return googleSignInClient!!
    }

    fun connect() {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, requiredScopes
        )
        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Aegis PDF").build()
    }

    fun listPdfFiles(): List<CloudPdfFile> {
        val files = mutableListOf<CloudPdfFile>()
        try {
            val result = driveService?.files()?.list()
                ?.setQ("mimeType='application/pdf'")
                ?.setPageSize(50)
                ?.setFields("files(id, name, size, createdTime)")
                ?.execute()

            result?.files?.forEach { file ->
                files.add(
                    CloudPdfFile(
                        id = file.id,
                        name = file.name ?: "Unknown",
                        size = formatSize(file.size ?: 0),
                        type = "application/pdf"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return files
    }

    fun downloadFile(fileId: String): File? {
        return try {
            val outputFile = File(context.cacheDir, "download_${System.currentTimeMillis()}.pdf")
            driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(
                java.io.FileOutputStream(outputFile)
            )
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    fun uploadFile(file: File): Boolean {
        return try {
            val metadata = com.google.api.services.drive.model.File().apply {
                name = file.name
                mimeType = "application/pdf"
            }
            val mediaContent = com.google.api.client.http.FileContent("application/pdf", file)
            driveService?.files()?.create(metadata, mediaContent)?.execute()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}

data class CloudPdfFile(
    val id: String,
    val name: String,
    val size: String,
    val type: String
)