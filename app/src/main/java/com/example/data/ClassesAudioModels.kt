package com.example.data

import android.content.Context

data class VideoLesson(
    val id: String,
    val title: String,
    val targetClass: String, // "S.1", "S.2", "S.3", "S.4", "S.5", "S.6"
    val subject: String,
    val topic: String,
    val teacherName: String,
    val description: String,
    val durationFormatted: String, // e.g. "14:34"
    val youtubeVideoUrl: String, // YouTube URL or embed URL or ID
    val thumbnailUrl: String,
    val isApproved: Boolean = true,
    val isFeatured: Boolean = false
)

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

fun getYouTubeEmbedUrl(url: String): String {
    if (url.contains("embed/")) return url
    val videoId = when {
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
        url.contains("watch?v=") -> url.substringAfter("watch?v=").substringBefore("&")
        url.length == 11 -> url
        else -> "dQw4w9WgXcQ"
    }
    return "https://www.youtube.com/embed/$videoId?autoplay=1&rel=0"
}

fun getYouTubeThumbnailUrl(url: String): String {
    val videoId = when {
        url.contains("embed/") -> url.substringAfter("embed/").substringBefore("?")
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
        url.contains("watch?v=") -> url.substringAfter("watch?v=").substringBefore("&")
        url.length == 11 -> url
        else -> "dQw4w9WgXcQ"
    }
    return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

object VideoLessonProgressManager {
    private const val PREFS_NAME = "cachap_video_progress"
    private const val KEY_COMPLETED_PREFIX = "completed_video_"
    private const val KEY_COMPLETED_COUNT = "total_video_completed_count"

    fun markCompleted(context: Context, lessonId: String, lessonTitle: String, subject: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alreadyCompleted = prefs.getBoolean("$KEY_COMPLETED_PREFIX$lessonId", false)
        if (!alreadyCompleted) {
            val currentCount = prefs.getInt(KEY_COMPLETED_COUNT, 0)
            prefs.edit()
                .putBoolean("$KEY_COMPLETED_PREFIX$lessonId", true)
                .putInt(KEY_COMPLETED_COUNT, currentCount + 1)
                .apply()

            MyLearningRepository.recordHistoryItem(
                context = context,
                title = "Watched Video Lesson: $lessonTitle",
                subtitle = "$subject • Video Classes",
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

val DefaultVideoLessons: List<VideoLesson> = listOf(
    VideoLesson(
        id = "vid-1",
        title = "Wave Motion, Reflection & Refraction Masterclass",
        targetClass = "S.4",
        subject = "Physics",
        topic = "Sound & Wave Theory",
        teacherName = "Mr. Okello David",
        description = "Comprehensive video masterclass on transverse and longitudinal waves, refractive index, and Snell's Law applications for UNEB candidates.",
        durationFormatted = "14:34",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
        thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
        isFeatured = true
    ),
    VideoLesson(
        id = "vid-2",
        title = "Cell Division: Mitosis vs Meiosis Stages Visualized",
        targetClass = "S.3",
        subject = "Biology",
        topic = "Genetics & Cytology",
        teacherName = "Ms. Nalubega Sarah",
        description = "Detailed visual walkthrough of prophase, metaphase, anaphase, and telophase with animated chromosome behavior.",
        durationFormatted = "12:00",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=rgLqgN3y7tE",
        thumbnailUrl = "https://img.youtube.com/vi/rgLqgN3y7tE/hqdefault.jpg",
        isFeatured = true
    ),
    VideoLesson(
        id = "vid-3",
        title = "Calculus: Differentiation from First Principles",
        targetClass = "S.4",
        subject = "Mathematics",
        topic = "Pure Mathematics",
        teacherName = "Tr. Kato Emmanuel",
        description = "Learn how to find dy/dx using delta limits, gradient curves, and tangents with step-by-step whiteboard breakdowns.",
        durationFormatted = "17:20",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=ANv9qTtT9X4",
        thumbnailUrl = "https://img.youtube.com/vi/ANv9qTtT9X4/hqdefault.jpg",
        isFeatured = true
    ),
    VideoLesson(
        id = "vid-4",
        title = "Acids, Bases & Titration Practical Calculations",
        targetClass = "S.4",
        subject = "Chemistry",
        topic = "Physical Chemistry",
        teacherName = "Dr. Mugisha Patrick",
        description = "Step-by-step video breakdown of molarity, stoichiometry, end points, and indicator selection for UNEB practical paper 3.",
        durationFormatted = "15:15",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=34iA_Wz0NLE",
        thumbnailUrl = "https://img.youtube.com/vi/34iA_Wz0NLE/hqdefault.jpg",
        isFeatured = false
    ),
    VideoLesson(
        id = "vid-5",
        title = "East African Long Distance Trade Routes (19th Century)",
        targetClass = "S.2",
        subject = "History",
        topic = "East African History",
        teacherName = "Senior Master Akena",
        description = "How trade routes shaped kingdoms like Buganda, Bunyoro, and Mirambo's Nyamwezi empire with historical map overlays.",
        durationFormatted = "13:30",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
        isFeatured = false
    ),
    VideoLesson(
        id = "vid-6",
        title = "Rift Valley Formation & Volcanicity in East Africa",
        targetClass = "S.1",
        subject = "Geography",
        topic = "Physical Geography",
        teacherName = "Ms. Tumusiime Grace",
        description = "Tensional theory vs compressional theory of rifting, block faulting (horst mountains like Rwenzori), and 3D volcanic landforms.",
        durationFormatted = "11:30",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
        thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
        isFeatured = false
    ),
    VideoLesson(
        id = "vid-7",
        title = "Mastering English Summary Writing & Note Making",
        targetClass = "S.4",
        subject = "English",
        topic = "Grammar & Composition",
        teacherName = "Mrs. Babirye Joy",
        description = "Techniques for extracting main points within the 100-word limit without losing sentence coherence or punctuation marks.",
        durationFormatted = "12:30",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=rgLqgN3y7tE",
        thumbnailUrl = "https://img.youtube.com/vi/rgLqgN3y7tE/hqdefault.jpg",
        isFeatured = true
    ),
    VideoLesson(
        id = "vid-8",
        title = "Price Determination in Competitive Markets",
        targetClass = "S.5",
        subject = "Economics",
        topic = "Microeconomics",
        teacherName = "Mr. Mukasa Ronald",
        description = "Understanding equilibrium price, shifts in supply and demand curves, and government price ceiling and floor policies.",
        durationFormatted = "16:00",
        youtubeVideoUrl = "https://www.youtube.com/watch?v=ANv9qTtT9X4",
        thumbnailUrl = "https://img.youtube.com/vi/ANv9qTtT9X4/hqdefault.jpg",
        isFeatured = false
    )
)
