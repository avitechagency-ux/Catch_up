package com.example.data

import com.example.BuildConfig

/**
 * Supabase configuration and security-hardened client helper for Cachap backend sync.
 * - Public project endpoint defaults to the Cachap Supabase URL (`https://xzutgxiwexxhmpqtybaf.supabase.co`).
 * - The `SUPABASE_ANON_KEY` is read from `BuildConfig` (injected via the AI Studio Secrets panel)
 *   and immediately stored in [SupabaseSecurityVault]'s XOR-masked byte buffer rather than held
 *   in plain-text memory.
 */
object SupabaseConfig {
    const val DEFAULT_PUBLIC_PROJECT_URL = "https://xzutgxiwexxhmpqtybaf.supabase.co"

    var supabaseUrl: String = run {
        val fromBuildConfig = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")
        if (fromBuildConfig.isNotBlank() &&
            !fromBuildConfig.contains("your-project") &&
            fromBuildConfig.startsWith("https://")
        ) {
            fromBuildConfig
        } else {
            DEFAULT_PUBLIC_PROJECT_URL
        }
    }

    init {
        SupabaseSecurityVault.storeObfuscatedKey(BuildConfig.SUPABASE_ANON_KEY)
    }

    /**
     * Updates the runtime anon key inside the XOR-obfuscated vault.
     */
    fun updateAnonKey(rawKey: String) {
        SupabaseSecurityVault.storeObfuscatedKey(rawKey)
    }

    /**
     * Retrieves the unmasked anon key on-demand for [SecureSupabaseInterceptor].
     */
    fun resolveAnonKey(): String {
        return SupabaseSecurityVault.retrieveAnonKey()
    }

    val restBaseUrl: String
        get() = "${supabaseUrl.removeSuffix("/")}/rest/v1/"

    val storageBaseUrl: String
        get() = "${supabaseUrl.removeSuffix("/")}/storage/v1/"

    fun isConfigured(): Boolean {
        val key = resolveAnonKey()
        return supabaseUrl.isNotBlank() &&
            !supabaseUrl.contains("your-project") &&
            supabaseUrl.startsWith("https://") &&
            key.isNotBlank() &&
            !key.contains("your-anon-key")
    }
}
