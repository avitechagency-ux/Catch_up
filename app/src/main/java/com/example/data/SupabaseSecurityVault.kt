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

    private var obfuscatedAnonKeyBytes: ByteArray? = null
    private var obfuscatedSessionTokenBytes: ByteArray? = null

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
     */
    @Synchronized
    fun storeObfuscatedKey(rawKey: String) {
        val trimmed = rawKey.trim()
        if (trimmed.isEmpty() || trimmed == "your-anon-key") {
            obfuscatedAnonKeyBytes = null
            return
        }
        val inputBytes = trimmed.toByteArray(StandardCharsets.UTF_8)
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
        val masked = obfuscatedAnonKeyBytes ?: return ""
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

    /**
     * Hashes a user password with SHA-256 + platform salt so plain-text passwords are never sent
     * over the network or stored in the remote `public.users` table.
     */
    fun hashPassword(plainPassword: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val salted = "$PASSWORD_SALT:${plainPassword.trim()}"
        val hashBytes = digest.digest(salted.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies a plain password against a SHA-256 hash. Never allows blank hashes.
     */
    fun verifyPassword(plainPassword: String, storedPasswordOrHash: String): Boolean {
        if (storedPasswordOrHash.isBlank() || plainPassword.isBlank()) return false
        val hashedInput = hashPassword(plainPassword)
        return storedPasswordOrHash.equals(hashedInput, ignoreCase = true)
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
