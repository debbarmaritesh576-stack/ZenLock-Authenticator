package com.aegis.pdf.data.cloud  
  
import android.content.Context  
import androidx.security.crypto.EncryptedSharedPreferences  
import androidx.security.crypto.MasterKey  
import dagger.hilt.android.qualifiers.ApplicationContext  
import javax.inject.Inject  
import javax.inject.Singleton  
  
@Singleton  
class CloudAuthManager @Inject constructor(  
    @ApplicationContext context: Context  
) {  
    private val masterKey = MasterKey.Builder(context)  
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)  
        .build()  
  
    private val prefs = EncryptedSharedPreferences.create(  
        context,  
        "aegis_cloud_auth",  
        masterKey,  
        EncryptedSharedPreferences.PrefOption.AES256_SIV,  
        EncryptedSharedPreferences.PrefOption.AES256_GCM  
    )  
  
    fun saveToken(provider: String, token: String) {  
        prefs.edit().putString("${provider}_token", token).apply()  
    }  
  
    fun getToken(provider: String): String? {  
        return prefs.getString("${provider}_token", null)  
    }  
  
    fun clearToken(provider: String) {  
        prefs.edit().remove("${provider}_token").apply()  
    }  
}