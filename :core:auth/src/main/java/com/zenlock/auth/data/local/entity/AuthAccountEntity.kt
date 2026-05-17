package com.zenlock.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stored OTP account (encrypted secret)
 */
@Entity(tableName = "otp_accounts")
data class AuthAccountEntity(

    @PrimaryKey
    val id: String,

    val issuer: String,

    val accountName: String,

    /**
     * Encrypted secret (NEVER plain text)
     */
    val encryptedSecret: String,

    val algorithm: String = "SHA1",

    val digits: Int = 6,

    val period: Int = 30,

    val createdAt: Long = System.currentTimeMillis()
)