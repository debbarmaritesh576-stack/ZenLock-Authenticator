package com.aegis.pdf.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (key, value) -> bundle.putString(key, value) }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    fun logToolUsed(toolName: String) {
        logEvent("tool_used", mapOf("tool_name" to toolName))
    }

    fun logPdfProcessed(processType: String, fileSize: Long, duration: Long) {
        logEvent("pdf_processed", mapOf(
            "process_type" to processType,
            "file_size" to fileSize.toString(),
            "duration_ms" to duration.toString()
        ))
    }

    fun logPurchase(plan: String, price: String) {
        logEvent("purchase_completed", mapOf(
            "plan" to plan,
            "price" to price
        ))
    }

    fun logFeatureUsed(feature: String) {
        logEvent("feature_used", mapOf("feature" to feature))
    }

    fun logError(errorType: String, message: String) {
        logEvent("app_error", mapOf(
            "error_type" to errorType,
            "message" to message
        ))
    }
}