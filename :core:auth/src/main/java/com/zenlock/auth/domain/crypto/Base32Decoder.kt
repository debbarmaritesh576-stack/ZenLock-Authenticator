package com.zenlock.auth.domain.crypto

class Base32Decoder {

    companion object {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    }

    fun decode(input: String): ByteArray {
        val cleaned = input.trim().replace("=", "").uppercase()

        var buffer = 0
        var bitsLeft = 0
        val result = mutableListOf<Byte>()

        for (char in cleaned) {
            val index = ALPHABET.indexOf(char)
            if (index == -1) continue

            buffer = (buffer shl 5) or index
            bitsLeft += 5

            if (bitsLeft >= 8) {
                bitsLeft -= 8
                val byte = (buffer shr bitsLeft) and 0xFF
                result.add(byte.toByte())
            }
        }

        return result.toByteArray()
    }
}