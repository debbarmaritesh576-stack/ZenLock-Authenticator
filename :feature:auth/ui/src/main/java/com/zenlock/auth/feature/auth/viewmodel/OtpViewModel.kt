package com.zenlock.auth.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import com.zenlock.auth.data.repository.OtpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val repository: OtpRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AuthAccountEntity>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _otpCodes = MutableStateFlow<Map<Long, String>>(emptyMap())
    val otpCodes = _otpCodes.asStateFlow()

    init {
        observeAccounts()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            repository.getAccounts().collect { list ->
                _accounts.value = list
                refreshOtps()
            }
        }
    }

    fun refreshOtps() {
        viewModelScope.launch {
            val map = _accounts.value.associate { account ->
                account.id to repository.generateOtp(account)
            }
            _otpCodes.value = map
        }
    }

    /**
     * 🔥 MAIN FEATURE: Add new authenticator account
     */
    fun addAccount(
        issuer: String,
        accountName: String,
        secret: String
    ) {
        viewModelScope.launch {
            val entity = AuthAccountEntity(
                id = 0,
                issuer = issuer,
                accountName = accountName,
                secret = secret,
                createdAt = System.currentTimeMillis()
            )

            repository.insertAccount(entity)

            // refresh immediately after insert
            refreshOtps()
        }
    }

    fun deleteAccount(account: AuthAccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            refreshOtps()
        }
    }

    fun manualRefresh() {
        refreshOtps()
    }
}