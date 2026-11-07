package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Audio lesson data model for Classes educational audio library.
 */
data class AudioLesson(
    val id: String,
    val title: String,
    val targetClass: String, // "S.1", "S.2", "S.3", "S.4", "S.5", "S.6"
    val subject: String,
    val topic: String,
    val teacherName: String,
    val description: String,
    val durationSeconds: Int,
    val audioUrl: String,
    val thumbnailUrl: String? = null,
    val isApproved: Boolean = true,
    val isFeatured: Boolean = false
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

/**
 * Secondary class levels S.1 to S.6.
 */
enum class ClassLevel(val code: String, val title: String, val description: String) {
    S1("S.1", "Senior One", "Foundational secondary curriculum & introduction to sciences"),
    S2("S.2", "Senior Two", "Core secondary curriculum & intermediate concepts"),
    S3("S.3", "Senior Three", "Subject specialization & practical laboratory theory"),
    S4("S.4", "Senior Four", "UCE candidate preparation, past papers & syllabus completion"),
    S5("S.5", "Senior Five", "Advanced level subsidiary & principal subject foundations"),
    S6("S.6", "Senior Six", "UACE candidate revision, in-depth analysis & mock prep")
}

/**
 * Playback progress and completion manager for Audio Lessons.
 */
object AudioLessonProgressManager {
    private const val PREFS_NAME = "cachap_audio_progress"
    private const val KEY_PROGRESS_PREFIX = "progress_"
    private const val KEY_COMPLETED_PREFIX = "completed_"
    private const val KEY_COMPLETED_COUNT = "total_audio_completed_count"

    fun saveProgress(context: Context, lessonId: String, positionSeconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("$KEY_PROGRESS_PREFIX$lessonId", positionSeconds).apply()
    }

    fun getProgress(context: Context, lessonId: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("$KEY_PROGRESS_PREFIX$lessonId", 0)
    }

    fun markCompleted(context: Context, lessonId: String, lessonTitle: String, subject: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alreadyCompleted = prefs.getBoolean("$KEY_COMPLETED_PREFIX$lessonId", false)
        if (!alreadyCompleted) {
            val currentCount = prefs.getInt(KEY_COMPLETED_COUNT, 0)
            prefs.edit()
                .putBoolean("$KEY_COMPLETED_PREFIX$lessonId", true)
                .putInt(KEY_COMPLETED_COUNT, currentCount + 1)
                .apply()

            // Record into MyLearning study history
            MyLearningRepository.recordHistoryItem(
                context = context,
                title = "Completed Audio Lesson: $lessonTitle",
                subtitle = "$subject • Audio Library",
                category = HistoryCategory.AUDIO_LESSON
            )
        }
    }

    fun isCompleted(context: Context, lessonId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("$KEY_COMPLETED_PREFIX$lessonId", false)
    }

    fun getTotalCompletedCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COMPLETED_COUNT, 0)
    }
}

/**
 * Standard curriculum audio lessons for Cachap Classes.
 * Uses online educational audio streams from Wikimedia Commons / educational archives.
 */
val DefaultAudioLessons: List<AudioLesson> = listOf(
    AudioLesson(
        id = "audio-1",
        title = "Wave Motion, Reflection & Refraction Explained",
        targetClass = "S.4",
        subject = "Physics",
        topic = "Sound & Wave Theory",
        teacherName = "Mr. Okello David",
        description = "Comprehensive masterclass on transverse and longitudinal waves, refractive index, and Snell's Law applications for UNEB candidates.",
        durationSeconds = 874, // 14:34
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4b/Sound_of_a_tuning_fork.ogg",
        isApproved = true,
        isFeatured = true
    ),
    AudioLesson(
        id = "audio-2",
        title = "Cell Division: Mitosis vs Meiosis Stages",
        targetClass = "S.3",
        subject = "Biology",
        topic = "Genetics & Cytology",
        teacherName = "Ms. Nalubega Sarah",
        description = "Detailed auditory walkthrough of prophase, metaphase, anaphase, and telophase. Includes mnemonic hacks for remembering chromosome behavior.",
        durationSeconds = 720, // 12:00
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c8/Example.ogg",
        isApproved = true,
        isFeatured = true
    ),
    AudioLesson(
        id = "audio-3",
        title = "Calculus: Differentiation from First Principles",
        targetClass = "S.4",
        subject = "Mathematics",
        topic = "Pure Mathematics",
        teacherName = "Tr. Kato Emmanuel",
        description = "Learn how to find dy/dx using delta limits, gradient curves, and tangents without getting lost in the algebra.",
        durationSeconds = 1040, // 17:20
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4b/Sound_of_a_tuning_fork.ogg",
        isApproved = true,
        isFeatured = true
    ),
    AudioLesson(
        id = "audio-4",
        title = "Acids, Bases & Titration Calculations",
        targetClass = "S.4",
        subject = "Chemistry",
        topic = "Physical Chemistry",
        teacherName = "Dr. Mugisha Patrick",
        description = "Step-by-step audio breakdown of molarity, stoichiometry, end points, and indicator selection for UNEB practical paper 3.",
        durationSeconds = 915, // 15:15
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c8/Example.ogg",
        isApproved = true,
        isFeatured = false
    ),
    AudioLesson(
        id = "audio-5",
        title = "East African Long Distance Trade Routes (19th Century)",
        targetClass = "S.2",
        subject = "History",
        topic = "East African History",
        teacherName = "Senior Master Akena",
        description = "How trade routes shaped kingdoms like Buganda, Bunyoro, and Mirambo's Nyamwezi empire. Impact of caravan trade and firearms.",
        durationSeconds = 810, // 13:30
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4b/Sound_of_a_tuning_fork.ogg",
        isApproved = true,
        isFeatured = false
    ),
    AudioLesson(
        id = "audio-6",
        title = "Rift Valley Formation & Volcanicity in East Africa",
        targetClass = "S.1",
        subject = "Geography",
        topic = "Physical Geography",
        teacherName = "Ms. Tumusiime Grace",
        description = "Tensional theory vs compressional theory of rifting, block faulting (horst mountains like Rwenzori), and volcanic landforms.",
        durationSeconds = 690, // 11:30
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c8/Example.ogg",
        isApproved = true,
        isFeatured = false
    ),
    AudioLesson(
        id = "audio-7",
        title = "Mastering English Summary Writing & Note Making",
        targetClass = "S.4",
        subject = "English",
        topic = "Grammar & Composition",
        teacherName = "Mrs. Babirye Joy",
        description = "Techniques for extracting main points within the 100-word limit without losing sentence coherence or punctuation marks.",
        durationSeconds = 750, // 12:30
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4b/Sound_of_a_tuning_fork.ogg",
        isApproved = true,
        isFeatured = true
    ),
    AudioLesson(
        id = "audio-8",
        title = "Price Determination in Competitive Markets",
        targetClass = "S.5",
        subject = "Economics",
        topic = "Microeconomics",
        teacherName = "Mr. Mukasa Ronald",
        description = "Understanding equilibrium price, shifts in supply and demand curves, and government price ceiling and floor policies.",
        durationSeconds = 960, // 16:00
        audioUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c8/Example.ogg",
        isApproved = true,
        isFeatured = false
    )
)
