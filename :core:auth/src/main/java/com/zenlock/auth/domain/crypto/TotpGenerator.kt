package com.zenlock.auth.domain.otp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

class TotpGenerator {

    fun generateOtp(
        secret: ByteArray,
        timeStep: Long = 30,
        digits: Int = 6
    ): String {

        val currentTime = System.currentTimeMillis() / 1000
        val counter = currentTime / timeStep

        val data = ByteBuffer.allocate(8).putLong(counter).array()

        val hash = hmacSha1(secret, data)

        val offset = hash[hash.size - 1].toInt() and 0x0F

        val binary =
            ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        val otp = binary % (10.0.pow(digits)).toInt()

        return otp.toString().padStart(digits, '0')
    }

    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(key, "HmacSHA1")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
}