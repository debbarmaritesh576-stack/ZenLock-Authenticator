package com.zenlock.auth.domain.parser

import android.net.Uri
import com.zenlock.auth.domain.model.OtpAccount
import java.net.URLDecoder
import javax.inject.Inject

class OtpUriParser @Inject constructor() {

    fun parse(uriString: String): OtpAccount? {
        return try {
            val uri = Uri.parse(uriString)

            if (uri.scheme != "otpauth") return null

            val type = uri.host ?: return null
            val path = uri.path ?: return null

            val label = path.removePrefix("/")

            val decodedLabel = URLDecoder.decode(label, "UTF-8")

            val parts = decodedLabel.split(":")

            val issuerFromLabel = if (parts.size > 1) parts[0] else ""
            val accountName = if (parts.size > 1) parts[1] else parts[0]

            val secret = uri.getQueryParameter("secret") ?: return null
            val issuer = uri.getQueryParameter("issuer") ?: issuerFromLabel

            OtpAccount(
                type = type,
                issuer = issuer,
                accountName = accountName,
                secret = secret,
                algorithm = uri.getQueryParameter("algorithm") ?: "SHA1",
                digits = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6,
                period = uri.getQueryParameter("period")?.toIntOrNull() ?: 30
            )

        } catch (e: Exception) {
            null
        }
    }
}