package com.zenlock.auth.data.local.dao

import androidx.room.*
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthAccountDao {

    /**
     * Insert new OTP account
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AuthAccountEntity)

    /**
     * Delete specific account
     */
    @Delete
    suspend fun deleteAccount(account: AuthAccountEntity)

    /**
     * Get all accounts (LIVE STREAM)
     */
    @Query("SELECT * FROM otp_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<AuthAccountEntity>>

    /**
     * Get single account by ID
     */
    @Query("SELECT * FROM otp_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AuthAccountEntity?

    /**
     * Delete all accounts (reset feature)
     */
    @Query("DELETE FROM otp_accounts")
    suspend fun clearAll()
}