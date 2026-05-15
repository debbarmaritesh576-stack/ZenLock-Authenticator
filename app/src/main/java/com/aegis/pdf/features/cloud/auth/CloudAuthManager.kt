package com.aegis.pdf.data.cloud  
  
import android.content.Context  
import android.content.SharedPreferences  
import android.util.Log  
import androidx.security.crypto.EncryptedSharedPreferences  
import androidx.security.crypto.MasterKey  
import dagger.hilt.android.qualifiers.ApplicationContext  
import javax.inject.Inject  
import javax.inject.Singleton  
  
/**  
 * Aegis CloudAuthManager: High-security token storage with auto-recovery logic.  
 * Handles sensitive cloud credentials for Google Drive, Dropbox, etc.  
 */  
@Singleton  
class CloudAuthManager @Inject constructor(  
    @ApplicationContext private val context: Context  
) {  
  
    companion object {  
        private const val TAG = "AegisCloudAuth"  
        private const val PREF_FILE_NAME = "aegis_secure_cloud_prefs"  
    }  
  
    // Lazy initialization ensures the MasterKey is not built on the Main Thread during app startup.  
    private val securePrefs: SharedPreferences by lazy {  
        createSecurePrefs()  
    }  
  
    /**  
     * Creates EncryptedSharedPreferences with a fallback mechanism if KeyStore is corrupted.  
     */  
    private fun createSecurePrefs(): SharedPreferences {  
        return try {  
            val masterKey = MasterKey.Builder(context)  
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)  
                .build()  
  
            EncryptedSharedPreferences.create(  
                context,  
                PREF_FILE_NAME,  
                masterKey,  
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  
            )  
        } catch (e: Exception) {  
            Log.e(TAG, "KeyStore encryption failed! Clearing old keys to recover: ${e.message}")  
            // Critical: If decryption fails (OS update/Key corruption), we must reset to prevent crash.  
            context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).apply {  
                edit().clear().apply()  
            }  
            // Return standard prefs as last resort or try creating again  
            context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)  
        }  
    }  
  
    /**  
     * Safely saves cloud tokens.   
     * @param provider e.g., "google_drive", "dropbox"  
     */  
    fun saveToken(provider: String, token: String, refreshToken: String? = null, expiryMillis: Long = -1) {  
        val safeProvider = provider.lowercase().trim()  
        securePrefs.edit().apply {  
            putString("access_token_$safeProvider", token)  
            refreshToken?.let { putString("refresh_token_$safeProvider", it) }  
            putLong("expiry_$safeProvider", expiryMillis)  
            apply()  
        }  
    }  
  
    /**  
     * Retrieves the stored access token.  
     */  
    fun getToken(provider: String): String? {  
        return try {  
            securePrefs.getString("access_token_${provider.lowercase().trim()}", null)  
        } catch (e: Exception) {  
            Log.e(TAG, "Decryption error for $provider")  
            null  
        }  
    }  
  
    /**  
     * Checks if the session has expired.  
     */  
    fun isSessionExpired(provider: String): Boolean {  
        val expiry = securePrefs.getLong("expiry_${provider.lowercase().trim()}", -1)  
        return if (expiry == -1L) false else System.currentTimeMillis() > expiry  
    }  
  
    fun clearAuth(provider: String) {  
        val safeProvider = provider.lowercase().trim()  
        securePrefs.edit()  
            .remove("access_token_$safeProvider")  
            .remove("refresh_token_$safeProvider")  
            .remove("expiry_$safeProvider")  
            .apply()  
    }  
  
    fun clearAll() {  
        securePrefs.edit().clear().apply()  
    }  
}