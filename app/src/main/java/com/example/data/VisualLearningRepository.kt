package com.example.data

import android.content.Context
import android.util.Log

/**
 * Repository for Cachap Visual Learning — Pinterest-Inspired Educational Discovery.
 * Manages approved educational visuals, connected articles, teacher visual uploads,
 * and the administrative approval workflow.
 * 
 * Updated to fetch dynamically from Supabase/Cloudflare R2.
 */
object VisualLearningRepository {

    /**
     * Retrieves all approved visuals for students from Supabase.
     * CRITICAL REQUIREMENT: Strictly filters out any pending, rejected, or unapproved uploads.
     */
    suspend fun getApprovedVisuals(subjectFilter: String? = null): List<VisualLearningItem> {
        val allItems = SupabaseRepository.fetchAllVisualLearningItems()
        val allApproved = allItems.filter { item ->
            item.isApproved && item.status.equals("Approved", ignoreCase = true)
        }

        if (subjectFilter.isNullOrBlank() || subjectFilter.equals("All", ignoreCase = true)) {
            return allApproved
        }

        return allApproved.filter { it.subject.equals(subjectFilter, ignoreCase = true) }
    }

    /**
     * Retrieves all distinct subjects represented in the approved visuals.
     */
    suspend fun getAvailableSubjects(): List<String> {
        val allItems = SupabaseRepository.fetchAllVisualLearningItems()
        val subjects = allItems
            .filter { it.isApproved && it.status.equals("Approved", ignoreCase = true) }
            .map { it.subject }
            .distinct()
            .sorted()
        return listOf("All") + subjects
    }

    /**
     * Searches approved visuals by query string (title, subject, or article content).
     */
    suspend fun searchApprovedVisuals(query: String): List<VisualLearningItem> {
        val approved = getApprovedVisuals()
        if (query.isBlank()) return approved
        
        val lowerQuery = query.lowercase()
        return approved.filter { item ->
            item.title.lowercase().contains(lowerQuery) ||
            item.subject.lowercase().contains(lowerQuery) ||
            item.article.title.lowercase().contains(lowerQuery) ||
            item.article.introduction.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Retrieves a single visual learning item by ID.
     */
    suspend fun getVisualById(id: String): VisualLearningItem? {
        val allItems = SupabaseRepository.fetchAllVisualLearningItems()
        return allItems.firstOrNull { it.id == id }
    }

    // Compatibility methods for initialization (no-op now as we use Supabase)
    fun initialize(context: Context) {
        // No-op: Supabase fetching is stateless/on-demand
    }
}
