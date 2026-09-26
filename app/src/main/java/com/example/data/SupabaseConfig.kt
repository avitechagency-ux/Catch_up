package com.example.data

/**
 * Supabase configuration and client helper preparing for Supabase backend sync.
 */
object SupabaseConfig {
    var supabaseUrl: String = "https://your-project.supabase.co"
    var supabaseAnonKey: String = "your-anon-key"

    fun isConfigured(): Boolean {
        return supabaseUrl.isNotBlank() && !supabaseUrl.contains("your-project") &&
                supabaseAnonKey.isNotBlank() && !supabaseAnonKey.contains("your-anon-key")
    }
}
