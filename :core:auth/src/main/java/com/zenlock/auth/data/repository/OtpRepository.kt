package com.zenlock.auth.data.repository

import com.zenlock.auth.core.security.EncryptedOtpStorage
import com.zenlock.auth.data.local.dao.AuthAccountDao
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import com.zenlock.auth.domain.crypto.Base32Decoder
import com.zenlock.auth.domain.crypto.TotpGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OtpRepository @Inject constructor(
    private val dao: AuthAccountDao,
    private val encryption: EncryptedOtpStorage,
    private val base32Decoder: Base32Decoder,
    private val totpGenerator: TotpGenerator
) {

    /**
     * Live OTP accounts stream (UI layer)
     */
    fun getAccounts(): Flow<List<AuthAccountEntity>> {
        return dao.getAllAccounts()
    }

    /**
     * Generate current OTP for a single account
     */
    fun generateOtp(account: AuthAccountEntity): String {
        return try {
            val decryptedSecret = encryption.decrypt(account.encryptedSecret)
            val secretBytes = base32Decoder.decode(decryptedSecret)

            totpGenerator.generateTOTP(secretBytes)
        } catch (e: Exception) {
            "------"
        }
    }

    /**
     * Insert new account
     */
    suspend fun addAccount(account: AuthAccountEntity) {
        dao.insertAccount(account)
    }

    /**
     * Delete account
     */
    suspend fun deleteAccount(account: AuthAccountEntity) {
        dao.deleteAccount(account)
    }

    /**
     * Clear all accounts
     */
    suspend fun clearAll() {
        dao.clearAll()
    }
}