package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Repository for managing Cachap Quizzes and persisting user attempt scores.
 */
object QuizRepository {
    private const val PREFS_NAME = "cachap_quiz_prefs"
    private const val KEY_ATTEMPTS = "quiz_attempts"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter<List<QuizAttemptResult>>(
        Types.newParameterizedType(List::class.java, QuizAttemptResult::class.java)
    )

    /**
     * Retrieves all approved quizzes for a selected subject.
     * Requirement: Students must see only approved quizzes filtered to the selected subject.
     */
    fun getApprovedQuizzesForSubject(subjectName: String): List<Quiz> {
        return DefaultApprovedQuizzes.filter { quiz ->
            quiz.isApproved && quiz.subject.equals(subjectName, ignoreCase = true)
        }
    }

    /**
     * Retrieves a single quiz by ID.
     */
    fun getQuizById(quizId: String): Quiz? {
        return DefaultApprovedQuizzes.firstOrNull { it.id == quizId && it.isApproved }
    }

    /**
     * Saves a completed quiz attempt result to SharedPreferences.
     */
    fun saveQuizAttempt(context: Context, result: QuizAttemptResult) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentAttempts = getQuizAttempts(context).toMutableList()
        currentAttempts.add(0, result) // Newest first

        try {
            val json = adapter.toJson(currentAttempts)
            prefs.edit().putString(KEY_ATTEMPTS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Fetches all saved quiz attempt results.
     */
    fun getQuizAttempts(context: Context): List<QuizAttemptResult> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ATTEMPTS, null) ?: return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches the highest or latest attempt result for a specific quiz.
     */
    fun getLatestAttemptForQuiz(context: Context, quizId: String): QuizAttemptResult? {
        return getQuizAttempts(context).firstOrNull { it.quizId == quizId }
    }
}
