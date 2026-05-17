package com.zenlock.auth.domain.crypto

class Base32Decoder {

    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decode(base32: String): ByteArray {
        val cleaned = base32
            .trim()
            .replace("=", "")
            .uppercase()

        val bytes = mutableListOf<Byte>()

        var buffer = 0
        var bitsLeft = 0

        for (char in cleaned) {
            val value = alphabet.indexOf(char)
            if (value == -1) continue

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                bitsLeft -= 8
                val byte = (buffer shr bitsLeft) and 0xFF
                bytes.add(byte.toByte())
            }
        }

        return bytes.toByteArray()
    }
}