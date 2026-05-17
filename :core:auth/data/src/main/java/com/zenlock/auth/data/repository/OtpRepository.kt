package com.zenlock.auth.data.repository

import com.zenlock.auth.data.local.dao.AuthAccountDao
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import com.zenlock.auth.security.EncryptedOtpStorage
import com.zenlock.auth.domain.crypto.Base32Decoder
import com.zenlock.auth.domain.otp.TotpGenerator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OtpRepository @Inject constructor(
    private val dao: AuthAccountDao,
    private val encryption: EncryptedOtpStorage,
    private val base32Decoder: Base32Decoder,
    private val totpGenerator: TotpGenerator
) {

    /**
     * 📡 Live accounts stream
     */
    fun getAccounts(): Flow<List<AuthAccountEntity>> {
        return dao.getAllAccounts()
    }

    /**
     * 💾 Insert account with encryption
     */
    suspend fun insertAccount(account: AuthAccountEntity) {
        val encrypted = account.copy(
            secret = encryption.encrypt(account.secret)
        )
        dao.insertAccount(encrypted)
    }

    /**
     * 🗑 Delete account
     */
    suspend fun deleteAccount(account: AuthAccountEntity) {
        dao.deleteAccount(account)
    }

    /**
     * 🔐 Generate OTP for a specific account
     */
    fun generateOtp(account: AuthAccountEntity): String {
        return try {
            val decryptedSecret = encryption.decrypt(account.secret)
            val decodedBytes = base32Decoder.decode(decryptedSecret)
            totpGenerator.generateOtp(decodedBytes)
        } catch (e: Exception) {
            "------"
        }
    }
}