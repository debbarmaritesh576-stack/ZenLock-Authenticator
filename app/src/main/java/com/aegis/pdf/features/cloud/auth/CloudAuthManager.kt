package com.aegis.pdf.data.cloud

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "aegis_cloud_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(provider: String, token: String) {
        securePrefs.edit().putString("token_$provider", token).apply()
    }

    fun getToken(provider: String): String? {
        return securePrefs.getString("token_$provider", null)
    }

    fun clearToken(provider: String) {
        securePrefs.edit().remove("token_$provider").apply()
    }

    fun isAuthenticated(provider: String): Boolean {
        return getToken(provider) != null
    }

    fun clearAll() {
        securePrefs.edit().clear().apply()
    }
}