package com.zenlock.auth.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import com.zenlock.auth.data.repository.OtpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
            val currentAccounts = _accounts.value

            val otpMap = currentAccounts.associate { account ->
                val otp = repository.generateOtp(account)
                account.id to otp
            }

            _otpCodes.value = otpMap
        }
    }

    /**
     * Optional: manual refresh trigger (pull-to-refresh future UI)
     */
    fun manualRefresh() {
        refreshOtps()
    }
}