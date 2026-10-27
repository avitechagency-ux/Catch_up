package com.example.data

/**
 * Model representing a book in the Library.
 */
data class Book(
    val id: String,
    val title: String,
    val author: String?,
    val coverUrl: String?, // URL or resource ID
    val documentUrl: String? // URL or ID to open the book
)
