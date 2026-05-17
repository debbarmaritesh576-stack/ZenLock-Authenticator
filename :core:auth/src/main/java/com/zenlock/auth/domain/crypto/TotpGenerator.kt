package com.zenlock.auth.domain.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class TotpGenerator {

    companion object {
        private const val TIME_STEP_SECONDS = 30L
        private const val OTP_DIGITS = 6
        private const val HMAC_ALGORITHM = "HmacSHA1"
    }

    fun generateTOTP(secret: ByteArray, timeMillis: Long = System.currentTimeMillis()): String {
        val timeStep = timeMillis / 1000 / TIME_STEP_SECONDS

        val data = ByteBuffer.allocate(8).putLong(timeStep).array()

        val hmac = hmacSha1(secret, data)

        val offset = (hmac[hmac.size - 1].toInt() and 0x0F)

        val binary =
            ((hmac[offset].toInt() and 0x7F) shl 24) or
            ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
            ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
            (hmac[offset + 3].toInt() and 0xFF)

        val otp = binary % 10.0.pow(OTP_DIGITS.toDouble()).toInt()

        return otp.toString().padStart(OTP_DIGITS, '0')
    }

    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val secretKey = SecretKeySpec(key, HMAC_ALGORITHM)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    private fun Double.pow(exp: Double): Double {
        return kotlin.math.pow(exp)
    }
}