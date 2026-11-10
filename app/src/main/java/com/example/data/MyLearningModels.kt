package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Status of a personal study session.
 */
enum class SessionStatus {
    SCHEDULED,
    COMPLETED,
    SKIPPED
}

/**
 * Personal timetable study session model.
 */
data class StudySession(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val topic: String,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val startTime: String, // "16:00"
    val endTime: String,   // "17:30"
    val repeatsWeekly: Boolean = true,
    val reminderLeadMinutes: Int = 15, // 5, 10, 15, 30
    val focusLockEnabled: Boolean = false,
    val status: SessionStatus = SessionStatus.SCHEDULED,
    val notes: String = ""
)

/**
 * Student personal learning goal.
 */
data class StudyGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val targetDate: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 100,
    val isCompleted: Boolean = false
)

/**
 * Private student quick note.
 */
data class QuickNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val content: String,
    val dateModified: String = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
)

/**
 * Category for recorded student history activities.
 */
enum class HistoryCategory {
    SESSION,
    QUIZ,
    AUDIO_LESSON,
    GOAL
}

/**
 * Chronological study activity item.
 */
data class StudyHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
    val title: String,
    val subtitle: String,
    val category: HistoryCategory,
    val scorePercent: Int? = null
)

/**
 * Real student learning progress aggregation.
 */
data class StudentLearningProgress(
    val completedSessionsCount: Int,
    val scheduledSessionsCount: Int,
    val completedQuizzesCount: Int,
    val averageQuizScorePercent: Int,
    val completedAudioLessonsCount: Int,
    val activeGoalsCount: Int,
    val completedGoalsCount: Int,
    val quickNotesCount: Int
)

/**
 * Study Focus Lock configuration & state.
 */
data class FocusLockState(
    val isArmed: Boolean = false,
    val activeSessionTopic: String? = null,
    val timeRemainingSeconds: Int = 0,
    val isPaused: Boolean = false,
    val restrictedApps: List<String> = listOf("TikTok", "Instagram", "Facebook", "X (Twitter)", "WhatsApp", "YouTube", "Mobile Games")
)

/**
 * Central repository managing student private learning data.
 * All data is isolated and persistent in SharedPreferences per student account.
 */
object MyLearningRepository {
    private const val PREFS_NAME = "cachap_my_learning_prefs"
    private const val KEY_SESSIONS = "study_sessions_json"
    private const val KEY_GOALS = "study_goals_json"
    private const val KEY_NOTES = "quick_notes_json"
    private const val KEY_HISTORY = "study_history_json"
    private const val KEY_FOCUS_LOCK = "focus_lock_state"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val sessionsAdapter = moshi.adapter<List<StudySession>>(
        Types.newParameterizedType(List::class.java, StudySession::class.java)
    )
    private val goalsAdapter = moshi.adapter<List<StudyGoal>>(
        Types.newParameterizedType(List::class.java, StudyGoal::class.java)
    )
    private val notesAdapter = moshi.adapter<List<QuickNote>>(
        Types.newParameterizedType(List::class.java, QuickNote::class.java)
    )
    private val historyAdapter = moshi.adapter<List<StudyHistoryItem>>(
        Types.newParameterizedType(List::class.java, StudyHistoryItem::class.java)
    )

    // ==========================================
    // 1. PERSONAL TIMETABLE SESSIONS
    // ==========================================
    fun getSessions(context: Context): List<StudySession> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SESSIONS, null)
        if (json.isNullOrBlank()) {
            val defaults = getDefaultStudySessions()
            saveSessions(context, defaults)
            return defaults
        }
        return try {
            sessionsAdapter.fromJson(json) ?: getDefaultStudySessions()
        } catch (e: Exception) {
            getDefaultStudySessions()
        }
    }

    fun saveSessions(context: Context, sessions: List<StudySession>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val json = sessionsAdapter.toJson(sessions)
            prefs.edit().putString(KEY_SESSIONS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addSession(context: Context, session: StudySession) {
        val current = getSessions(context).toMutableList()
        current.add(0, session)
        saveSessions(context, current)
    }

    fun updateSession(context: Context, updated: StudySession) {
        val current = getSessions(context).map {
            if (it.id == updated.id) updated else it
        }
        saveSessions(context, current)
    }

    fun deleteSession(context: Context, sessionId: String) {
        val current = getSessions(context).filter { it.id != sessionId }
        saveSessions(context, current)
    }

    fun markSessionStatus(context: Context, sessionId: String, newStatus: SessionStatus) {
        val sessions = getSessions(context).toMutableList()
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index != -1) {
            val session = sessions[index]
            sessions[index] = session.copy(status = newStatus)
            saveSessions(context, sessions)

            if (newStatus == SessionStatus.COMPLETED) {
                recordHistoryItem(
                    context = context,
                    title = "Completed Session: ${session.subject}",
                    subtitle = "${session.topic} (${session.startTime} - ${session.endTime})",
                    category = HistoryCategory.SESSION
                )
            }
        }
    }

    // ==========================================
    // 2. STUDY GOALS
    // ==========================================
    fun getGoals(context: Context): List<StudyGoal> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_GOALS, null)
        if (json.isNullOrBlank()) {
            val defaults = getDefaultStudyGoals()
            saveGoals(context, defaults)
            return defaults
        }
        return try {
            goalsAdapter.fromJson(json) ?: getDefaultStudyGoals()
        } catch (e: Exception) {
            getDefaultStudyGoals()
        }
    }

    fun saveGoals(context: Context, goals: List<StudyGoal>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val json = goalsAdapter.toJson(goals)
            prefs.edit().putString(KEY_GOALS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addGoal(context: Context, goal: StudyGoal) {
        val current = getGoals(context).toMutableList()
        current.add(0, goal)
        saveGoals(context, current)
    }

    fun updateGoal(context: Context, updated: StudyGoal) {
        val current = getGoals(context).map {
            if (it.id == updated.id) updated else it
        }
        saveGoals(context, current)
    }

    fun toggleGoalCompletion(context: Context, goalId: String) {
        val goals = getGoals(context).toMutableList()
        val index = goals.indexOfFirst { it.id == goalId }
        if (index != -1) {
            val g = goals[index]
            val newCompleted = !g.isCompleted
            val newProgress = if (newCompleted) g.targetProgress else g.currentProgress
            goals[index] = g.copy(isCompleted = newCompleted, currentProgress = newProgress)
            saveGoals(context, goals)

            if (newCompleted) {
                recordHistoryItem(
                    context = context,
                    title = "Goal Achieved: ${g.title}",
                    subtitle = "${g.subject} • Target: ${g.targetDate}",
                    category = HistoryCategory.GOAL
                )
            }
        }
    }

    fun deleteGoal(context: Context, goalId: String) {
        val current = getGoals(context).filter { it.id != goalId }
        saveGoals(context, current)
    }

    // ==========================================
    // 3. QUICK NOTES
    // ==========================================
    fun getNotes(context: Context): List<QuickNote> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTES, null)
        if (json.isNullOrBlank()) {
            val defaults = getDefaultQuickNotes()
            saveNotes(context, defaults)
            return defaults
        }
        return try {
            notesAdapter.fromJson(json) ?: getDefaultQuickNotes()
        } catch (e: Exception) {
            getDefaultQuickNotes()
        }
    }

    fun saveNotes(context: Context, notes: List<QuickNote>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val json = notesAdapter.toJson(notes)
            prefs.edit().putString(KEY_NOTES, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addNote(context: Context, note: QuickNote) {
        val current = getNotes(context).toMutableList()
        current.add(0, note)
        saveNotes(context, current)
    }

    fun updateNote(context: Context, updated: QuickNote) {
        val current = getNotes(context).map {
            if (it.id == updated.id) updated else it
        }
        saveNotes(context, current)
    }

    fun deleteNote(context: Context, noteId: String) {
        val current = getNotes(context).filter { it.id != noteId }
        saveNotes(context, current)
    }

    // ==========================================
    // 4. STUDY HISTORY & LEARNING PROGRESS
    // ==========================================
    fun getHistory(context: Context): List<StudyHistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null)
        if (json.isNullOrBlank()) {
            val defaults = getDefaultHistory()
            saveHistory(context, defaults)
            return defaults
        }
        return try {
            historyAdapter.fromJson(json) ?: getDefaultHistory()
        } catch (e: Exception) {
            getDefaultHistory()
        }
    }

    private fun saveHistory(context: Context, items: List<StudyHistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val json = historyAdapter.toJson(items)
            prefs.edit().putString(KEY_HISTORY, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun recordHistoryItem(
        context: Context,
        title: String,
        subtitle: String,
        category: HistoryCategory,
        scorePercent: Int? = null
    ) {
        val item = StudyHistoryItem(
            title = title,
            subtitle = subtitle,
            category = category,
            scorePercent = scorePercent
        )
        val current = getHistory(context).toMutableList()
        current.add(0, item) // Newest on top
        saveHistory(context, current.take(100)) // Keep latest 100
    }

    /**
     * Aggregates real progress metrics from student sessions, quiz results, and audio completion.
     */
    fun computeStudentProgress(context: Context): StudentLearningProgress {
        val sessions = getSessions(context)
        val completedSessions = sessions.count { it.status == SessionStatus.COMPLETED }
        val scheduledSessions = sessions.size

        // Real quiz attempts from QuizRepository
        val quizAttempts = QuizRepository.getQuizAttempts(context)
        val completedQuizzes = quizAttempts.size
        val avgScore = if (completedQuizzes > 0) {
            quizAttempts.map { it.percentage }.average().toInt()
        } else 0

        val completedAudioCount = VideoLessonProgressManager.getTotalCompletedCount(context)
        val goals = getGoals(context)
        val completedGoals = goals.count { it.isCompleted }
        val activeGoals = goals.count { !it.isCompleted }
        val notesCount = getNotes(context).size

        return StudentLearningProgress(
            completedSessionsCount = completedSessions,
            scheduledSessionsCount = scheduledSessions,
            completedQuizzesCount = completedQuizzes,
            averageQuizScorePercent = avgScore,
            completedAudioLessonsCount = completedAudioCount,
            activeGoalsCount = activeGoals,
            completedGoalsCount = completedGoals,
            quickNotesCount = notesCount
        )
    }

    // Default starter data
    private fun getDefaultStudySessions(): List<StudySession> = listOf(
        StudySession(
            subject = "Physics",
            topic = "Light Waves & Refraction Revision",
            dayOfWeek = "Monday",
            startTime = "17:00",
            endTime = "18:15",
            repeatsWeekly = true,
            reminderLeadMinutes = 15,
            focusLockEnabled = true,
            status = SessionStatus.COMPLETED
        ),
        StudySession(
            subject = "Mathematics",
            topic = "Calculus: Differentiation Practice",
            dayOfWeek = "Tuesday",
            startTime = "16:30",
            endTime = "18:00",
            repeatsWeekly = true,
            reminderLeadMinutes = 10,
            focusLockEnabled = true,
            status = SessionStatus.SCHEDULED
        ),
        StudySession(
            subject = "Biology",
            topic = "Genetics & Monohybrid Cross Problems",
            dayOfWeek = "Wednesday",
            startTime = "19:00",
            endTime = "20:00",
            repeatsWeekly = true,
            reminderLeadMinutes = 15,
            focusLockEnabled = false,
            status = SessionStatus.SCHEDULED
        ),
        StudySession(
            subject = "Chemistry",
            topic = "Volumetric Analysis Past Papers",
            dayOfWeek = "Friday",
            startTime = "16:00",
            endTime = "17:30",
            repeatsWeekly = true,
            reminderLeadMinutes = 30,
            focusLockEnabled = true,
            status = SessionStatus.SCHEDULED
        )
    )

    private fun getDefaultStudyGoals(): List<StudyGoal> = listOf(
        StudyGoal(
            title = "Complete UCE Physics Past Paper 2024",
            subject = "Physics",
            targetDate = "30 Nov 2026",
            currentProgress = 70,
            targetProgress = 100,
            isCompleted = false
        ),
        StudyGoal(
            title = "Master Organic Chemistry Reactions (Alkanes & Alkenes)",
            subject = "Chemistry",
            targetDate = "15 Dec 2026",
            currentProgress = 45,
            targetProgress = 100,
            isCompleted = false
        ),
        StudyGoal(
            title = "Attempt 5 Mathematics Quizzes with >80% Score",
            subject = "Mathematics",
            targetDate = "05 Dec 2026",
            currentProgress = 3,
            targetProgress = 5,
            isCompleted = false
        )
    )

    private fun getDefaultQuickNotes(): List<QuickNote> = listOf(
        QuickNote(
            title = "Snell's Law Formula Checklist",
            subject = "Physics",
            content = "n1 * sin(θ1) = n2 * sin(θ2). Remember: refractive index of air is approximately 1.00. Light bends toward normal when entering denser medium."
        ),
        QuickNote(
            title = "UNEB Essay Command Words",
            subject = "English",
            content = "Describe = state facts and details without judging. Explain = give reasons (why and how). Evaluate = weigh arguments for and against with a conclusion."
        )
    )

    private fun getDefaultHistory(): List<StudyHistoryItem> = listOf(
        StudyHistoryItem(
            title = "Completed Session: Physics",
            subtitle = "Light Waves & Refraction Revision (17:00 - 18:15)",
            category = HistoryCategory.SESSION
        ),
        StudyHistoryItem(
            title = "Quiz Attempt: Cell Biology Fundamentals",
            subtitle = "Biology • 9/10 correct",
            category = HistoryCategory.QUIZ,
            scorePercent = 90
        ),
        StudyHistoryItem(
            title = "Audio Lesson: Wave Motion, Reflection & Refraction",
            subtitle = "Physics • Mr. Okello David",
            category = HistoryCategory.AUDIO_LESSON
        )
    )
}
