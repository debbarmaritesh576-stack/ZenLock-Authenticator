package com.zenlock.auth.core.otp

import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

class TotpGenerator {

    companion object {
        private const val TIME_STEP_SECONDS = 30L
        private const val CODE_DIGITS = 6
    }

    /**
     * Generate TOTP code (Google Authenticator / FreeOTP compatible)
     */
    fun generateTOTP(secret: String, timeMillis: Long = System.currentTimeMillis()): String {
        val key = base32Decode(secret)
        val timeStep = timeMillis / 1000 / TIME_STEP_SECONDS

        val data = ByteArray(8)
        var value = timeStep

        for (i in 7 downTo 0) {
            data[i] = (value and 0xFF).toByte()
            value = value shr 8
        }

        val hmac = hmacSha1(key, data)
        val offset = hmac[hmac.size - 1].toInt() and 0x0F

        val binary =
            ((hmac[offset].toInt() and 0x7F) shl 24) or
            ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
            ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
            (hmac[offset + 3].toInt() and 0xFF)

        val otp = binary % 10.0.pow(CODE_DIGITS.toDouble()).toInt()

        return otp.toString().padStart(CODE_DIGITS, '0')
    }

    /**
     * HMAC-SHA1 hashing
     */
    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        val spec = SecretKeySpec(key, "HmacSHA1")
        mac.init(spec)
        return mac.doFinal(data)
    }

    /**
     * Base32 decode (for OTP secrets)
     */
    private fun base32Decode(base32: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = base32.replace("=", "").uppercase()

        var bits = 0
        var value = 0
        val output = mutableListOf<Byte>()

        for (char in cleaned) {
            val index = alphabet.indexOf(char)
            if (index == -1) continue

            value = (value shl 5) or index
            bits += 5

            if (bits >= 8) {
                output.add(((value shr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }

        return output.toByteArray()
    }

    /**
     * Remaining seconds in current TOTP cycle
     */
    fun getRemainingSeconds(): Int {
        val current = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        return (TIME_STEP_SECONDS - (current % TIME_STEP_SECONDS)).toInt()
    }
}