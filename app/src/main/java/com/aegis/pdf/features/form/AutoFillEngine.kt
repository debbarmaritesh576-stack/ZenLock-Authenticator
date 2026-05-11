package com.aegis.pdf.features.form

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class AutoFillProfile(
    val id: String,
    val name: String,
    val data: Map<String, String>
)

@Singleton
class AutoFillEngine @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "AutoFillEngine"
        private const val PREF_FILE = "aegis_autofill_secure"
        private const val PREF_KEY = "profiles"
    }

    private val gson = Gson()

    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREF_FILE,
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted prefs, falling back to regular", e)
            context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        }
    }

    private val fieldMappings = mapOf(
        "name" to listOf("name", "full name", "first name", "last name", "your name"),
        "email" to listOf("email", "e-mail", "email address", "your email"),
        "phone" to listOf("phone", "telephone", "mobile", "contact", "phone number"),
        "address" to listOf("address", "street", "city", "state", "zip", "postal", "country"),
        "company" to listOf("company", "organization", "employer", "business"),
        "title" to listOf("title", "designation", "position", "job title"),
        "date" to listOf("date", "birth", "dob", "today"),
        "signature" to listOf("signature", "sign", "signed")
    )

    fun getProfiles(): List<AutoFillProfile> {
        return try {
            val json = securePrefs.getString(PREF_KEY, "[]") ?: "[]"
            val type = object : TypeToken<List<AutoFillProfile>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profiles", e)
            emptyList()
        }
    }

    fun saveProfile(profile: AutoFillProfile) {
        try {
            val profiles = getProfiles().toMutableList()
            val existing = profiles.indexOfFirst { it.id == profile.id }
            if (existing >= 0) {
                profiles[existing] = profile
            } else {
                profiles.add(profile)
            }
            val json = gson.toJson(profiles)
            securePrefs.edit().putString(PREF_KEY, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile", e)
        }
    }

    fun deleteProfile(profileId: String) {
        try {
            val profiles = getProfiles().filter { it.id != profileId }
            val json = gson.toJson(profiles)
            securePrefs.edit().putString(PREF_KEY, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete profile", e)
        }
    }

    fun autoFill(fields: List<FormField>, profile: AutoFillProfile): List<FormField> {
        return fields.map { field ->
            val matchedKey = findMatchingKey(field.name)
            if (matchedKey != null && profile.data.containsKey(matchedKey)) {
                field.copy(value = profile.data[matchedKey] ?: "")
            } else {
                field
            }
        }
    }

    fun suggestProfile(fields: List<FormField>): AutoFillProfile? {
        val profiles = getProfiles()
        if (profiles.isEmpty()) return null

        var bestProfile: AutoFillProfile? = null
        var bestScore = 0

        profiles.forEach { profile ->
            var score = 0
            fields.forEach { field ->
                val matchedKey = findMatchingKey(field.name)
                if (matchedKey != null && profile.data.containsKey(matchedKey)) score++
            }
            if (score > bestScore) {
                bestScore = score
                bestProfile = profile
            }
        }

        return if (bestScore > 0) bestProfile else null
    }

    fun createDefaultProfile(): AutoFillProfile {
        return AutoFillProfile(
            id = UUID.randomUUID().toString(),
            name = "My Profile",
            data = mapOf(
                "name" to "",
                "email" to "",
                "phone" to "",
                "address" to "",
                "company" to "",
                "title" to ""
            )
        )
    }

    private fun findMatchingKey(fieldName: String): String? {
        val normalized = fieldName.lowercase().trim()
        fieldMappings.forEach { (key, aliases) ->
            if (aliases.any { normalized.contains(it) }) return key
        }
        return null
    }
}