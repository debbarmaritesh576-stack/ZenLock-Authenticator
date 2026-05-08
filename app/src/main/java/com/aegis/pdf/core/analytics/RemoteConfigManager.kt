package com.aegis.pdf.core.analytics

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        private const val KEY_MIN_APP_VERSION = "min_app_version"
        private const val KEY_PREMIUM_DISCOUNT = "premium_discount"
        private const val KEY_MAX_FREE_MERGES = "max_free_merges"
        private const val KEY_ENABLE_AI_FEATURES = "enable_ai_features"
        private const val KEY_MAINTENANCE_MODE = "maintenance_mode"
    }

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            KEY_MIN_APP_VERSION to 1,
            KEY_PREMIUM_DISCOUNT to 0,
            KEY_MAX_FREE_MERGES to 5,
            KEY_ENABLE_AI_FEATURES to false,
            KEY_MAINTENANCE_MODE to false
        ))
    }

    fun fetch(onComplete: () -> Unit = {}) {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            onComplete()
        }
    }

    val minAppVersion: Int
        get() = remoteConfig.getLong(KEY_MIN_APP_VERSION).toInt()

    val premiumDiscount: Int
        get() = remoteConfig.getLong(KEY_PREMIUM_DISCOUNT).toInt()

    val maxFreeMerges: Int
        get() = remoteConfig.getLong(KEY_MAX_FREE_MERGES).toInt()

    val isAiEnabled: Boolean
        get() = remoteConfig.getBoolean(KEY_ENABLE_AI_FEATURES)

    val isMaintenanceMode: Boolean
        get() = remoteConfig.getBoolean(KEY_MAINTENANCE_MODE)
}