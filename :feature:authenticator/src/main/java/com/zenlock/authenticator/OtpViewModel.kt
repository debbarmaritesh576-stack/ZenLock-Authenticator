package com.zenlock.authenticator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlock.auth.data.model.OtpAccount
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

    private val _otpState = MutableStateFlow<Map<String, String>>(emptyMap())
    val otpState = _otpState.asStateFlow()

    fun startOtpUpdates(accounts: List<OtpAccount>) {
        viewModelScope.launch {
            while (true) {
                val updated = accounts.associate { account ->
                    account.id to repository.getOtp(account)
                }

                _otpState.value = updated
                delay(1000)
            }
        }
    }
}