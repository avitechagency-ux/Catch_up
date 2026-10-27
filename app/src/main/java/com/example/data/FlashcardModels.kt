package com.example.data

/**
 * Model representing a flashcard for revision.
 */
data class Flashcard(
    val id: String,
    val subject: String,
    val question: String,
    val answer: String
)
