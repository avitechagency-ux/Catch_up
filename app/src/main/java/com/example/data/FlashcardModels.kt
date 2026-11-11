package com.example.data

/**
 * Model representing a flashcard for revision.
 */
/**
 * Model representing a flashcard for revision.
 */
data class Flashcard(
    val id: String,
    val question: String,
    val answer: String
)

/**
 * Model representing a deck of flashcards.
 */
data class FlashcardDeck(
    val id: String,
    val title: String,
    val subject: String,
    val cards: List<Flashcard>
)
