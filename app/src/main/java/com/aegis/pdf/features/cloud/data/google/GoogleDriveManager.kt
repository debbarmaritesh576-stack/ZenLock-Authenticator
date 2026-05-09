package com.aegis.pdf.sync

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "GoogleDriveManager"
        private var instance: GoogleDriveManager? = null

        fun getInstance(context: Context): Drive {
            if (instance == null) {
                instance = GoogleDriveManager(context.applicationContext)
            }
            return instance!!.getDriveService()
        }

        fun initialize(context: Context) {
            instance = GoogleDriveManager(context.applicationContext)
        }
    }

    private val driveScopes = listOf(
        DriveScopes.DRIVE_FILE,
        DriveScopes.DRIVE_APPDATA,
        DriveScopes.DRIVE_METADATA
    )

    private val googleSignInClient: GoogleSignInClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        GoogleSignIn.getClient(context, signInOptions)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            googleSignInClient.signOut()
        }
    }

    fun getDriveService(): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            driveScopes
        ).apply {
            selectedAccountName = getAccountName()
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Aegis PDF")
            .build()
    }

    private fun getAccountName(): String? {
        val prefs = context.getSharedPreferences("aegis_drive_prefs", Context.MODE_PRIVATE)
        return prefs.getString("account_name", null)
    }

    fun setAccountName(accountName: String) {
        context.getSharedPreferences("aegis_drive_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("account_name", accountName)
            .apply()
    }

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val drive = getDriveService()
                drive.files().list().setPageSize(1).execute()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Connection test failed", e)
                false
            }
        }
    }
}