package com.zenlock.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_accounts")
data class AuthAccountEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val issuer: String,

    val accountName: String,

    /**
     * ⚠️ In production this should be encrypted (AES/GCM)
     * currently stored as raw Base32 secret for simplicity layer
     */
    val secret: String,

    val createdAt: Long,

    val lastUsedAt: Long? = null
)