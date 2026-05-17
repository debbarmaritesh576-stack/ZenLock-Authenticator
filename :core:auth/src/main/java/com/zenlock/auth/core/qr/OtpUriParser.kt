package com.zenlock.auth.core.qr

import android.net.Uri
import com.zenlock.auth.data.model.OtpAccount
import java.util.UUID

object OtpUriParser {

    /**
     * Parses Google Authenticator / FreeOTP style URI:
     * otpauth://totp/Issuer:Account?secret=XXXX&issuer=XXX
     */
    fun parse(uri: String): OtpAccount? {
        return try {
            val parsed = Uri.parse(uri)

            if (parsed.scheme != "otpauth") return null

            val type = parsed.host ?: return null
            val path = parsed.path ?: return null

            val label = path.removePrefix("/")

            val issuerFromQuery = parsed.getQueryParameter("issuer")
            val secret = parsed.getQueryParameter("secret") ?: return null

            val parts = label.split(":")

            val issuer = issuerFromQuery ?: if (parts.size > 1) parts[0] else "Unknown"
            val accountName = if (parts.size > 1) parts[1] else label

            OtpAccount(
                id = UUID.randomUUID().toString(),
                issuer = issuer,
                accountName = accountName,
                secret = secret
            )

        } catch (e: Exception) {
            null
        }
    }
}