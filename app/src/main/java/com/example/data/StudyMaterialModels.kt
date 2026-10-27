package com.example.data

/**
 * Model representing a study material resource.
 */
data class StudyMaterialResource(
    val id: String,
    val title: String,
    val subject: String,
    val metadata: String, // e.g. "S.4 • Mathematics"
    val documentUrl: String? // URL or ID to open the resource
)
