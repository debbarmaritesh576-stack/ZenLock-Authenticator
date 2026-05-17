package com.zenlock.auth.data.repository

import com.zenlock.auth.core.otp.TotpGenerator
import com.zenlock.auth.data.model.OtpAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpRepository @Inject constructor(
    private val generator: TotpGenerator
) {

    fun getOtp(account: OtpAccount): String {
        return generator.generateTOTP(account.secret)
    }

    fun getRemainingTime(): Int {
        return generator.getRemainingSeconds()
    }
}