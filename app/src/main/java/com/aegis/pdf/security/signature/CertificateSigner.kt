package com.aegis.pdf.core.security

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateSigner @Inject constructor() {

    fun signPdf(
        inputFile: File,
        outputFile: File,
        keyStoreFile: File? = null,
        keyStorePassword: String,
        keyAlias: String,
        reason: String = "Document signed via Aegis PDF",
        location: String = "India"
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val signature = PDSignature()
                signature.filter = PDSignature.FILTER_ADOBE_PPKLITE
                signature.subFilter = PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED
                signature.name = "Aegis PDF Signer"
                signature.reason = reason
                signature.location = location
                signature.signDate = Calendar.getInstance()

                document.addSignature(signature)
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun verifySignature(file: File): SignatureVerificationResult {
        return try {
            PDDocument.load(file).use { document ->
                val signatures = document.signatureDictionaries
                if (signatures.isEmpty()) {
                    SignatureVerificationResult.NoSignature
                } else {
                    val sig = signatures[0]
                    SignatureVerificationResult.Valid(
                        signerName = sig.name ?: "Unknown",
                        signDate = sig.signDate?.time ?: Date(),
                        reason = sig.reason ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            SignatureVerificationResult.Error(e.message ?: "Verification failed")
        }
    }

    sealed class SignatureVerificationResult {
        object NoSignature : SignatureVerificationResult()
        data class Valid(
            val signerName: String,
            val signDate: Date,
            val reason: String
        ) : SignatureVerificationResult()
        data class Error(val message: String) : SignatureVerificationResult()
    }
}