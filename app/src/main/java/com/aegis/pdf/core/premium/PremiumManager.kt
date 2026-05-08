package com.aegis.pdf.core.premium

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)

    enum class Plan { FREE, MONTHLY, YEARLY, LIFETIME }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_PLAN = "premium_plan"
        private const val KEY_EXPIRY = "premium_expiry"
        private const val KEY_PURCHASE_TOKEN = "purchase_token"
    }

    val isPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)

    val currentPlan: Plan
        get() {
            val planName = prefs.getString(KEY_PLAN, Plan.FREE.name) ?: Plan.FREE.name
            return Plan.valueOf(planName)
        }

    val features: List<String>
        get() = if (isPremium) {
            listOf(
                "Unlimited merges",
                "No ads",
                "Advanced OCR",
                "Cloud storage sync",
                "Priority support",
                "All templates",
                "Batch processing",
                "Password removal",
                "AI features unlimited"
            )
        } else {
            emptyList()
        }

    fun upgradeToPremium(plan: Plan, purchaseToken: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_PREMIUM, true)
            putString(KEY_PLAN, plan.name)
            putString(KEY_PURCHASE_TOKEN, purchaseToken)
            when (plan) {
                Plan.MONTHLY -> putLong(KEY_EXPIRY, System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
                Plan.YEARLY -> putLong(KEY_EXPIRY, System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
                Plan.LIFETIME -> putLong(KEY_EXPIRY, Long.MAX_VALUE)
                else -> {}
            }
            apply()
        }
    }

    fun revokePremium() {
        prefs.edit().clear().apply()
    }

    fun isExpired(): Boolean {
        val expiry = prefs.getLong(KEY_EXPIRY, 0)
        return System.currentTimeMillis() > expiry
    }
}