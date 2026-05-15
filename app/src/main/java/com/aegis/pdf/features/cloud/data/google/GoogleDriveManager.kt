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
import dagger.hilt.android.qualifiers.ApplicationContext  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.withContext  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class GoogleDriveManager @Inject constructor(  
    @ApplicationContext private val context: Context  
) {  
    companion object {  
        private const val TAG = "AegisDrive"  
        private val SCOPES = listOf(  
            DriveScopes.DRIVE_FILE,  
            DriveScopes.DRIVE_METADATA_READONLY // Metadata ke liye zaroori hai  
        )  
    }  
  
    private val googleSignInClient: GoogleSignInClient by lazy {  
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)  
            .requestEmail()  
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_METADATA_READONLY))  
            .build()  
        GoogleSignIn.getClient(context, signInOptions)  
    }  
  
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent  
  
    /**  
     * Drive Service ko build karne ka sahi tareeka  
     */  
    fun getDriveService(): Drive? {  
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null  
          
        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES).apply {  
            selectedAccount = account.account  
        }  
  
        return Drive.Builder(  
            NetHttpTransport(),  
            GsonFactory.getDefaultInstance(),  
            credential  
        )  
            .setApplicationName("Aegis PDF")  
            .build()  
    }  
  
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {  
        try {  
            val service = getDriveService() ?: return@withContext false  
            // Sirf 1 file mangwa kar check karo connection  
            val files = service.files().list().setPageSize(1).execute()  
            files != null  
        } catch (e: Exception) {  
            Log.e(TAG, "Drive connection failed: ${e.message}")  
            false  
        }  
    }  
  
    suspend fun signOut() = withContext(Dispatchers.IO) {  
        googleSignInClient.signOut()  
    }  
}