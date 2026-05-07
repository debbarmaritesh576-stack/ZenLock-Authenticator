package com.aegis.pdf.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class BiometricLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        HARDWARE_UNAVAILABLE,
        ERROR
    }

    fun canUseBiometrics(): BiometricStatus {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.HARDWARE_UNAVAILABLE
            else -> BiometricStatus.ERROR
        }
    }

    suspend fun authenticate(activity: FragmentActivity): Boolean {
        return suspendCoroutine { continuation ->
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Aegis PDF Security")
                .setSubtitle("Verify your identity to access secured files")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(true)
                .build()

            val biometricPrompt = BiometricPrompt(activity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(true)
                    }

                    override fun onAuthenticationFailed() {
                        // Continue listening
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resume(false)
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }
    }
}