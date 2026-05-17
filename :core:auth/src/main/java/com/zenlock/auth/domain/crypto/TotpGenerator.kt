package com.zenlock.auth.domain.otp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

class TotpGenerator {

    private val timeStepSeconds = 30L
    private val digits = 6

    fun generateOtp(secret: ByteArray, time: Long = System.currentTimeMillis()): String {
        val counter = time / 1000 / timeStepSeconds
        return generateHotp(secret, counter)
    }

    private fun generateHotp(secret: ByteArray, counter: Long): String {
        val data = ByteArray(8)

        var value = counter
        for (i in 7 downTo 0) {
            data[i] = (value and 0xFF).toByte()
            value = value shr 8
        }

        val mac = Mac.getInstance("HmacSHA1")
        val keySpec = SecretKeySpec(secret, "HmacSHA1")
        mac.init(keySpec)

        val hash = mac.doFinal(data)

        val offset = hash.last().toInt() and 0x0F
        val binary =
            ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        val otp = binary % (10.0.pow(digits.toDouble())).toInt()

        return otp.toString().padStart(digits, '0')
    }
}