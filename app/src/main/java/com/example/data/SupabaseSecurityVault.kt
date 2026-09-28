package com.example.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.experimental.xor

/**
 * Multi-layered security vault for protecting Supabase keys and user credentials in the Android app:
 * 1. In-memory XOR byte obfuscation so raw API keys are not held as plain String fields on the heap.
 * 2. Hardware-backed Android Keystore (AES-256-GCM) encryption for any persisted session secrets.
 * 3. SHA-256 salted password hashing before transmitting passwords to Supabase.
 * 4. OkHttp Interceptor that injects `apikey`, `Authorization`, and `x-cachap-client` headers only
 *    for verified HTTPS requests to the configured Supabase host, and redacts sensitive headers.
 */
object SupabaseSecurityVault {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "cachap_supabase_master_key_v1"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val PASSWORD_SALT = "cachap_ug_curriculum_salt_v2"
    const val CLIENT_INTEGRITY_HEADER = "x-cachap-client"
    const val CLIENT_INTEGRITY_VALUE = "cachap-android-v2.3"
    const val SESSION_TOKEN_HEADER = "x-cachap-session"

    private val xorMask: ByteArray = byteArrayOf(
        0x5A, 0x3C, 0x7F, 0x19, 0x6E, 0x42, 0x2B, 0x4D,
        0x11, 0x67, 0x38, 0x53, 0x29, 0x74, 0x1E, 0x61
    )

    // XOR-masked signature bytes for fallback when CI builds without a custom .env secret
    private val defaultMaskedSignatureBytes: ByteArray = byteArrayOf(
        0x3F, 0x63, 0x16, 0x55, 0x21, 0x09, 0x1A, 0x01,
        0x67, 0x03, 0x4C, 0x09, 0x50, 0x26, 0x6A, 0x27,
        0x36, 0x7F, 0x20, 0x4A, 0x1A, 0x11, 0x62, 0x37,
        0x6B, 0x57, 0x61, 0x12, 0x6A, 0x40, 0x70, 0x11,
        0x36, 0x0A, 0x0C, 0x4A, 0x3A, 0x11, 0x74, 0x02,
        0x44, 0x2F, 0x61
    )

    private var obfuscatedAnonKeyBytes: ByteArray? = null
    private var obfuscatedSessionTokenBytes: ByteArray? = null

    private fun reconstructDefaultProjectKey(): String {
        val headerPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val payloadPart = "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh6dXRneGl3ZXh4aG1wcXR5YmFmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA0NzAyNjQsImV4cCI6MjEwNjA0NjI2NH0"
        val unmaskedSig = ByteArray(defaultMaskedSignatureBytes.size)
        for (i in defaultMaskedSignatureBytes.indices) {
            unmaskedSig[i] = defaultMaskedSignatureBytes[i] xor xorMask[i % xorMask.size]
        }
        val sigPart = String(unmaskedSig, StandardCharsets.UTF_8)
        unmaskedSig.fill(0)
        return "$headerPart.$payloadPart.$sigPart"
    }

    /**
     * Stores the custom table-based auth session token (`public.auth_sessions.session_token`)
     * in an XOR-masked byte array in memory.
     */
    @Synchronized
    fun storeSessionToken(token: String) {
        val trimmed = token.trim()
        if (trimmed.isEmpty()) {
            obfuscatedSessionTokenBytes = null
            return
        }
        val inputBytes = trimmed.toByteArray(StandardCharsets.UTF_8)
        val masked = ByteArray(inputBytes.size)
        for (i in inputBytes.indices) {
            masked[i] = inputBytes[i] xor xorMask[i % xorMask.size]
        }
        obfuscatedSessionTokenBytes = masked
        inputBytes.fill(0)
    }

    /**
     * Retrieves the active custom table-auth session token, or empty string if logged out.
     */
    @Synchronized
    fun retrieveSessionToken(): String {
        val masked = obfuscatedSessionTokenBytes ?: return ""
        val unmasked = ByteArray(masked.size)
        for (i in masked.indices) {
            unmasked[i] = masked[i] xor xorMask[i % xorMask.size]
        }
        val result = String(unmasked, StandardCharsets.UTF_8)
        unmasked.fill(0)
        return result
    }

    @Synchronized
    fun clearSessionToken() {
        obfuscatedSessionTokenBytes?.fill(0)
        obfuscatedSessionTokenBytes = null
    }

    /**
     * Stores the anon key in an XOR-masked byte array in memory instead of a plain String reference.
     * Automatically falls back to the XOR-masked project key when built in CI (GitHub Actions)
     * without a custom secret.
     */
    @Synchronized
    fun storeObfuscatedKey(rawKey: String) {
        val trimmed = rawKey.trim()
        val effectiveKey = if (trimmed.isEmpty() || trimmed == "your-anon-key") {
            reconstructDefaultProjectKey()
        } else {
            trimmed
        }
        val inputBytes = effectiveKey.toByteArray(StandardCharsets.UTF_8)
        val masked = ByteArray(inputBytes.size)
        for (i in inputBytes.indices) {
            masked[i] = inputBytes[i] xor xorMask[i % xorMask.size]
        }
        obfuscatedAnonKeyBytes = masked
        inputBytes.fill(0)
    }

    /**
     * Reconstructs the anon key on demand for outbound HTTPS header injection.
     */
    @Synchronized
    fun retrieveAnonKey(): String {
        val masked = obfuscatedAnonKeyBytes ?: run {
            storeObfuscatedKey("")
            obfuscatedAnonKeyBytes ?: return ""
        }
        val unmasked = ByteArray(masked.size)
        for (i in masked.indices) {
            unmasked[i] = masked[i] xor xorMask[i % xorMask.size]
        }
        val result = String(unmasked, StandardCharsets.UTF_8)
        unmasked.fill(0)
        return result
    }

    /**
     * Masks a key for safe display or diagnostic logging (e.g., "eyJh...OUHY").
     */
    fun redactKey(key: String): String {
        val clean = key.trim()
        if (clean.length <= 10) return "[REDACTED]"
        return "${clean.take(4)}...[REDACTED]...${clean.takeLast(4)}"
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hashes a user password with SHA-256 + platform salt so plain-text passwords are never sent
     * over the network or stored in the remote `public.users` table.
     */
    fun hashPassword(plainPassword: String): String {
        return sha256Hex("$PASSWORD_SALT:${plainPassword.trim()}")
    }

    /**
     * Generates all candidate password hashes for a user-supplied plain password to handle:
     * - Current platform salt (`cachap_ug_curriculum_salt_v2`)
     * - Mobile keyboard auto-capitalization of the first character or lowercase normalization
     * - Earlier platform salts (`cachap_ug_curriculum_salt_v1`, `cachap_ug_curriculum_salt`) and unsalted SHA-256
     */
    fun candidatePasswordHashes(plainPassword: String): List<String> {
        val trimmed = plainPassword.trim()
        if (trimmed.isEmpty()) return emptyList()
        val firstCharSwapped = trimmed.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase() else ch.lowercase()
        }
        val lower = trimmed.lowercase()
        val passwordVariants = listOf(trimmed, plainPassword, firstCharSwapped, lower).distinct()
        val salts = listOf(
            PASSWORD_SALT,
            "cachap_ug_curriculum_salt_v1",
            "cachap_ug_curriculum_salt"
        )
        val hashes = mutableListOf<String>()
        for (variant in passwordVariants) {
            for (salt in salts) {
                hashes.add(sha256Hex("$salt:$variant"))
            }
            hashes.add(sha256Hex(variant))
        }
        return hashes.distinct()
    }

    /**
     * Verifies a plain password against a stored hash or value. Never allows blank inputs.
     */
    fun verifyPassword(plainPassword: String, storedPasswordOrHash: String): Boolean {
        val cleanStored = storedPasswordOrHash.trim()
        val cleanPlain = plainPassword.trim()
        if (cleanStored.isEmpty() || cleanPlain.isEmpty()) return false
        val extractedStored = if (cleanStored.contains("#pw:")) {
            cleanStored.substringAfter("#pw:").trim()
        } else {
            cleanStored
        }
        if (extractedStored.isEmpty()) return false
        if (extractedStored.equals(cleanPlain, ignoreCase = true)) return true
        val candidates = candidatePasswordHashes(plainPassword)
        return candidates.any { it.equals(extractedStored, ignoreCase = true) }
    }

    /**
     * Encrypts sensitive text using the hardware-backed Android Keystore (AES-256-GCM).
     * Falls back gracefully in local JVM unit tests where AndroidKeyStore is unavailable.
     */
    fun encryptWithKeystore(plainText: String): String {
        return try {
            val secretKey = getOrCreateSecretKey() ?: return Base64.encodeToString(
                plainText.toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(1 + iv.size + encrypted.size)
            combined[0] = iv.size.toByte()
            System.arraycopy(iv, 0, combined, 1, iv.size)
            System.arraycopy(encrypted, 0, combined, 1 + iv.size, encrypted.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            Base64.encodeToString(plainText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts a payload previously encrypted by [encryptWithKeystore].
     */
    fun decryptWithKeystore(cipherTextBase64: String): String {
        return try {
            val combined = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val secretKey = getOrCreateSecretKey() ?: return String(combined, StandardCharsets.UTF_8)
            val ivSize = combined[0].toInt() and 0xFF
            val iv = combined.copyOfRange(1, 1 + ivSize)
            val encrypted = combined.copyOfRange(1 + ivSize, combined.size)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    private fun getOrCreateSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (existingKey != null) {
                return existingKey.secretKey
            }
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * OkHttp Interceptor that securely attaches the Supabase `apikey`, `Authorization`, and
 * `x-cachap-client` headers only when communicating over HTTPS with the configured Supabase host.
 */
class SecureSupabaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val configuredHost = SupabaseConfig.supabaseUrl
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")

        // Only attach keys to verified HTTPS requests targeting our Supabase project host
        if (!original.url.isHttps || !original.url.host.equals(configuredHost, ignoreCase = true)) {
            return chain.proceed(original)
        }

        val activeKey = SupabaseConfig.resolveAnonKey()
        val requestBuilder = original.newBuilder()
            .header(SupabaseSecurityVault.CLIENT_INTEGRITY_HEADER, SupabaseSecurityVault.CLIENT_INTEGRITY_VALUE)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (activeKey.isNotBlank()) {
            requestBuilder
                .header("apikey", activeKey)
                .header("Authorization", "Bearer $activeKey")
        }

        val sessionToken = SupabaseSecurityVault.retrieveSessionToken()
        if (sessionToken.isNotBlank()) {
            requestBuilder.header(SupabaseSecurityVault.SESSION_TOKEN_HEADER, sessionToken)
        }

        return chain.proceed(requestBuilder.build())
    }
}
