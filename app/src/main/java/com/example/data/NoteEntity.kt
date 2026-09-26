package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val targetClass: String,
    val type: String = "Notes", // "Notes" or "Assignment"
    val dateUploaded: String,
    val authorName: String = "Teacher",
    val fileSize: String = "2.4 MB",
    val isDownloaded: Boolean = false,
    val supabaseId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
