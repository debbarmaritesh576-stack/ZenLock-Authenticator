package com.aegis.pdf.premium

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class PremiumManager(private val context: Context) {

    companion object {
        private const val LICENSE_SERVER = "https://api.aegis-pdf.com/v1"
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    private val securePrefs by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "aegis_premium_secure",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val deviceId: String by lazy { generateDeviceId() }

    enum class PremiumStatus {
        NOT_CHECKED,
        FREE,
        PREMIUM,
        EXPIRED,
        GRACE_PERIOD
    }

    data class LicenseInfo(
        val status: PremiumStatus,
        val planType: String?,
        val expiryDate: Long?,
        val features: List<String>?
    )

    /**
     * Check premium status with server validation + offline cache
     */
    suspend fun checkPremiumStatus(): LicenseInfo {
        return withContext(Dispatchers.IO) {
            try {
                // Try server validation first
                val serverResult = validateWithServer()
                if (serverResult != null) {
                    saveToSecureCache(serverResult)
                    return@withContext serverResult
                }
            } catch (e: Exception) {
                // Server unreachable, use cached data
            }

            // Fallback to encrypted cache
            getCachedLicense()
        }
    }

    private fun validateWithServer(): LicenseInfo? {
        return try {
            val purchaseToken = getPurchaseToken() ?: return null
            val packageName = context.packageName

            val requestBody = JSONObject().apply {
                put("package_name", packageName)
                put("purchase_token", purchaseToken)
                put("device_id", deviceId)
            }

            val request = Request.Builder()
                .url("$LICENSE_SERVER/verify")
                .post(okhttp3.RequestBody.create(
                    "application/json".toOkHttpMediaType(),
                    requestBody.toString()
                ))
                .addHeader("Authorization", "Bearer ${getServerKey()}")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && body != null) {
                parseServerResponse(body)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseServerResponse(json: String): LicenseInfo {
        val obj = JSONObject(json)
        val valid = obj.getBoolean("valid")
        val planType = obj.optString("plan_type", null)
        val expiryTimestamp = obj.optLong("expiry_timestamp", 0)
        val signature = obj.getString("signature")

        // Verify server response signature
        if (!verifyServerSignature(json, signature)) {
            return LicenseInfo(PremiumStatus.FREE, null, null, null)
        }

        return if (valid && System.currentTimeMillis() < expiryTimestamp) {
            val featuresArray = obj.optJSONArray("features")
            val features = (0 until (featuresArray?.length() ?: 0)).map {
                featuresArray!!.getString(it)
            }

            LicenseInfo(
                status = PremiumStatus.PREMIUM,
                planType = planType,
                expiryDate = expiryTimestamp,
                features = features
            )
        } else if (valid && expiryTimestamp < System.currentTimeMillis()) {
            LicenseInfo(
                status = PremiumStatus.EXPIRED,
                planType = planType,
                expiryDate = expiryTimestamp,
                features = null
            )
        } else {
            LicenseInfo(PremiumStatus.FREE, null, null, null)
        }
    }

    private fun getCachedLicense(): LicenseInfo {
        return try {
            val statusStr = securePrefs.getString("premium_status", PremiumStatus.FREE.name)
            val status = PremiumStatus.valueOf(statusStr ?: PremiumStatus.FREE.name)
            val planType = securePrefs.getString("plan_type", null)
            val expiryDate = securePrefs.getLong("expiry_date", 0)
            val lastCheck = securePrefs.getLong("last_check", 0)
            val featuresStr = securePrefs.getString("features", null)
            val features = featuresStr?.split(",")
            val cachedSignature = securePrefs.getString("cache_signature", null)

            // Verify cache integrity
            val expectedSignature = signCacheData(status.name, planType ?: "", expiryDate.toString())
            if (cachedSignature != expectedSignature) {
                // Cache tampered - return FREE
                return LicenseInfo(PremiumStatus.FREE, null, null, null)
            }

            // Check if cache is expired
            if (System.currentTimeMillis() - lastCheck > CACHE_DURATION_MS) {
                return LicenseInfo(PremiumStatus.FREE, null, null, null)
            }

            LicenseInfo(status, planType, if (expiryDate > 0) expiryDate else null, features)
        } catch (e: Exception) {
            LicenseInfo(PremiumStatus.FREE, null, null, null)
        }
    }

    private fun saveToSecureCache(license: LicenseInfo) {
        val editor = securePrefs.edit()
        editor.putString("premium_status", license.status.name)
        editor.putString("plan_type", license.planType)
        editor.putLong("expiry_date", license.expiryDate ?: 0)
        editor.putLong("last_check", System.currentTimeMillis())
        editor.putString("features", license.features?.joinToString(","))

        // Tamper detection signature
        val signature = signCacheData(
            license.status.name,
            license.planType ?: "",
            (license.expiryDate ?: 0).toString()
        )
        editor.putString("cache_signature", signature)
        editor.apply()
    }

    fun updatePurchaseToken(token: String) {
        securePrefs.edit().putString("purchase_token", token).apply()
    }

    private fun getPurchaseToken(): String? {
        return securePrefs.getString("purchase_token", null)
    }

    private fun getServerKey(): String {
        // Encrypted API key for server communication
        return "aes_encrypted_key_stored_in_so_file_or_remote_config"
    }

    private fun generateDeviceId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        val prefs = context.getSharedPreferences("aegis_device", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)

        if (deviceId == null) {
            deviceId = Base64.encodeToString(
                MessageDigest.getInstance("SHA-256").digest(bytes),
                Base64.NO_WRAP
            )
            prefs.edit().putString("device_id", deviceId).apply()
        }

        return deviceId
    }

    private fun verifyServerSignature(data: String, signature: String): Boolean {
        return try {
            val publicKeyPEM = context.getString(R.string.public_key)
            val publicKey = loadPublicKey(publicKeyPEM)
            val sig = java.security.Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(data.toByteArray())
            sig.verify(Base64.decode(signature, Base64.DEFAULT))
        } catch (e: Exception) {
            false
        }
    }

    private fun signCacheData(vararg parts: String): String {
        val data = parts.joinToString("|") + "|aegis_salt"
        val digest = MessageDigest.getInstance("SHA-256")
        return Base64.encodeToString(digest.digest(data.toByteArray()), Base64.NO_WRAP)
    }

    private fun loadPublicKey(pem: String): java.security.PublicKey {
        val keyBytes = Base64.decode(
            pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), ""),
            Base64.DEFAULT
        )
        val keySpec = java.security.spec.X509EncodedKeySpec(keyBytes)
        return java.security.KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    private fun String.toOkHttpMediaType(): okhttp3.MediaType {
        return okhttp3.MediaType.parse(this)!!
    }
}