package com.zenlock.auth.domain.model

data class OtpAccount(
    val type: String,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
)