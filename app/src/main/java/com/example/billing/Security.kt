package com.example.billing

import android.util.Base64
import android.util.Log
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Security-check functions for Myket In-App Billing verification.
 */
object Security {
    private const val TAG = "IABSecurity"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    fun verifyPurchase(base64PublicKey: String, signedData: String, signature: String): Boolean {
        if (signedData.isBlank() || base64PublicKey.isBlank() || signature.isBlank()) {
            Log.e(TAG, "Purchase verification failed: missing data.")
            return false
        }
        val publicKey = generatePublicKey(base64PublicKey) ?: return false
        return verify(publicKey, signedData, signature)
    }

    private fun generatePublicKey(encodedPublicKey: String): PublicKey? {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate public key", e)
            null
        }
    }

    private fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        return try {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray(Charsets.UTF_8))
            val sigBytes = Base64.decode(signature, Base64.DEFAULT)
            sig.verify(sigBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Base64 signature verification error", e)
            false
        }
    }
}
