package com.zenlock.auth.state

data class OtpUiState(
    val loading: Boolean = false,
    val otpCodes: Map<String, String> = emptyMap(),
    val remainingSeconds: Int = 30,
    val error: String? = null
)