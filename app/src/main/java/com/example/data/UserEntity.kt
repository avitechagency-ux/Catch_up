package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String = "",
    val phoneNumber: String = "",
    val fullName: String,
    val role: String, // "student", "teacher", "admin"
    val password: String = "",
    val schoolName: String = "",
    val academicClass: String = "",
    val stream: String = "",
    val subjects: String = "",
    val isPlanActive: Boolean = false,
    val planType: String = "Free",
    val supabaseId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
