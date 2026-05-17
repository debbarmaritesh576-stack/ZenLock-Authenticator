package com.zenlock.auth.data.local.dao

import androidx.room.*
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthAccountDao {

    /**
     * 📥 Insert new authenticator account
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AuthAccountEntity): Long

    /**
     * 🗑 Delete existing account
     */
    @Delete
    suspend fun deleteAccount(account: AuthAccountEntity)

    /**
     * 📡 Live stream of all accounts (auto-updates UI)
     */
    @Query("SELECT * FROM auth_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<AuthAccountEntity>>

    /**
     * 🔍 Single account fetch
     */
    @Query("SELECT * FROM auth_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AuthAccountEntity?

    /**
     * ✏️ Update last used timestamp (OTP refresh tracking)
     */
    @Query("UPDATE auth_accounts SET lastUsedAt = :time WHERE id = :id")
    suspend fun updateLastUsed(id: Long, time: Long)
}