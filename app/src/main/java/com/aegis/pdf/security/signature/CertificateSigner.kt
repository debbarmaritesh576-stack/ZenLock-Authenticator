package com.aegis.browser.security

import android.content.Context
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

class CertificateSigner(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Sign data with RSA-2048 private key from Android Keystore
     * Uses SHA256withRSA — industry standard
     */
    fun signData(data: ByteArray, keyAlias: String = "aegis_signing_key"): SignatureResult {
        return try {
            // Get private key from hardware-backed keystore
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
                ?: return SignatureResult.Error("Key not found: $keyAlias")

            val privateKey = entry.privateKey
            val certificate = entry.certificate as? X509Certificate
                ?: return SignatureResult.Error("Certificate not found")

            // Create signature with SHA256withRSA
            val signature = Signature.getInstance("SHA256withRSA").apply {
                initSign(privateKey)
                update(data)
            }

            val signedData = signature.sign()

            SignatureResult.Success(
                signature = Base64.getEncoder().encodeToString(signedData),
                algorithm = "SHA256withRSA",
                certificate = Base64.getEncoder().encodeToString(certificate.encoded),
                publicKey = Base64.getEncoder().encodeToString(certificate.publicKey.encoded)
            )
        } catch (e: Exception) {
            SignatureResult.Error("Signing failed: ${e.message}")
        }
    }

    /**
     * Verify signature with X.509 certificate
     */
    fun verifySignature(data: ByteArray, signatureBase64: String, certificateBase64: String): Boolean {
        return try {
            val signatureBytes = Base64.getDecoder().decode(signatureBase64)
            val certBytes = Base64.getDecoder().decode(certificateBase64)

            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificate = certificateFactory.generateCertificate(
                ByteArrayInputStream(certBytes)
            ) as X509Certificate

            val signature = Signature.getInstance("SHA256withRSA").apply {
                initVerify(certificate.publicKey)
                update(data)
            }

            signature.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sign a file and save signature
     */
    fun signFile(inputPath: String, outputSignaturePath: String, keyAlias: String = "aegis_signing_key"): SignatureResult {
        return try {
            val file = File(inputPath)
            val fileData = file.readBytes()

            // Calculate file hash
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(fileData)
            val hashBase64 = Base64.getEncoder().encodeToString(hash)

            // Sign the hash
            val result = signData(hash, keyAlias)

            when (result) {
                is SignatureResult.Success -> {
                    // Save signature to file
                    val signatureFile = File(outputSignaturePath)
                    FileOutputStream(signatureFile).use { fos ->
                        fos.write("---BEGIN SIGNATURE---\n".toByteArray())
                        fos.write("Algorithm: ${result.algorithm}\n".toByteArray())
                        fos.write("FileHash: $hashBase64\n".toByteArray())
                        fos.write("Signature: ${result.signature}\n".toByteArray())
                        fos.write("Certificate: ${result.certificate}\n".toByteArray())
                        fos.write("---END SIGNATURE---\n".toByteArray())
                    }
                    result
                }
                is SignatureResult.Error -> result
            }
        } catch (e: Exception) {
            SignatureResult.Error("File signing failed: ${e.message}")
        }
    }

    /**
     * Verify signed file
     */
    fun verifySignedFile(filePath: String, signaturePath: String): Boolean {
        return try {
            val file = File(filePath)
            val fileData = file.readBytes()

            val signatureContent = File(signaturePath).readText()

            val signatureBase64 = extractField(signatureContent, "Signature")
            val certificateBase64 = extractField(signatureContent, "Certificate")
            val expectedHash = extractField(signatureContent, "FileHash")

            if (signatureBase64 == null || certificateBase64 == null || expectedHash == null) {
                return false
            }

            // Verify file hash matches
            val digest = MessageDigest.getInstance("SHA-256")
            val actualHash = Base64.getEncoder().encodeToString(digest.digest(fileData))
            if (actualHash != expectedHash) return false

            // Verify signature
            verifySignature(digest.digest(fileData), signatureBase64, certificateBase64)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate signing key in Android Keystore
     */
    fun generateSigningKey(keyAlias: String = "aegis_signing_key") {
        if (!keyStore.containsAlias(keyAlias)) {
            // Use KeyPairGenerator for new key
            val keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA", "AndroidKeyStore").apply {
                initialize(
                    java.security.spec.KeyGenParameterSpec.Builder(
                        keyAlias,
                        java.security.KeyProperties.PURPOSE_SIGN or java.security.KeyProperties.PURPOSE_VERIFY
                    )
                        .setDigests(java.security.KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(java.security.KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setKeySize(2048)
                        .setCertificateSubject(javax.security.auth.x500.X500Principal("CN=Aegis Browser"))
                        .setCertificateSerialNumber(java.math.BigInteger.valueOf(System.currentTimeMillis()))
                        .setCertificateNotBefore(java.util.Date())
                        .setCertificateNotAfter(java.util.Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                        .build()
                )
            }
            keyPairGenerator.generateKeyPair()
        }
    }

    private fun extractField(content: String, fieldName: String): String? {
        val pattern = Regex("$fieldName: (.+)")
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }

    sealed class SignatureResult {
        data class Success(
            val signature: String,
            val algorithm: String,
            val certificate: String,
            val publicKey: String
        ) : SignatureResult()

        data class Error(val message: String) : SignatureResult()
    }
}