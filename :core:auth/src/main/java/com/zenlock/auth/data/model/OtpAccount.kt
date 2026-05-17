package com.zenlock.auth.data.model

data class OtpAccount(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
)