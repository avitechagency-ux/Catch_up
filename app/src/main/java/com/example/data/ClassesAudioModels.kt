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

fun extractYouTubeVideoId(url: String): String {
    if (url.isBlank()) return "5Q1A76bO80M"
    val clean = url.trim()
    val regex = Regex("(?:youtube(?:-nocookie)?\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?)|shorts/|.*[?&]v=)|youtu\\.be/)([^\"&?/\\s]{11})")
    val match = regex.find(clean)
    if (match != null) return match.groupValues[1]
    if (clean.length == 11 && !clean.contains("/")) return clean
    return "5Q1A76bO80M"
}

fun getYouTubeEmbedUrl(url: String, quality: String = "low_240p"): String {
    val videoId = extractYouTubeVideoId(url)
    val vq = when (quality) {
        "low_240p" -> "&vq=small"
        "balanced_360p" -> "&vq=medium"
        else -> "&vq=large"
    }
    return "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&rel=0&enablejsapi=1$vq&origin=https://www.youtube-nocookie.com&widget_referrer=https://www.youtube-nocookie.com"
}

fun getYouTubeThumbnailUrl(url: String): String {
    val videoId = extractYouTubeVideoId(url)
    return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

object DataSaverManager {
    private const val PREFS_NAME = "cachap_data_saver_prefs"
    private const val KEY_DATA_SAVER = "is_data_saver_enabled"
    private const val KEY_DEFAULT_QUALITY = "video_quality_mode" // "low_240p", "balanced_360p", "standard_480p"

    fun isDataSaverEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DATA_SAVER, true) // Enabled by default for Ugandan students with small data bundles
    }

    fun setDataSaverEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DATA_SAVER, enabled).apply()
    }

    fun getVideoQuality(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEFAULT_QUALITY, "low_240p") ?: "low_240p"
    }

    fun setVideoQuality(context: Context, quality: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEFAULT_QUALITY, quality).apply()
    }
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=5Q1A76bO80M",
        thumbnailUrl = "https://img.youtube.com/vi/5Q1A76bO80M/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=8m6hHRL77wU",
        thumbnailUrl = "https://img.youtube.com/vi/8m6hHRL77wU/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=WS3Lkc6GzLk",
        thumbnailUrl = "https://img.youtube.com/vi/WS3Lkc6GzLk/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=ANv9qTtT9X4",
        thumbnailUrl = "https://img.youtube.com/vi/ANv9qTtT9X4/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=LwRRR11S7E4",
        thumbnailUrl = "https://img.youtube.com/vi/LwRRR11S7E4/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=1gZ0tF_8jM4",
        thumbnailUrl = "https://img.youtube.com/vi/1gZ0tF_8jM4/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=y19vS0Z5kZ8",
        thumbnailUrl = "https://img.youtube.com/vi/y19vS0Z5kZ8/hqdefault.jpg",
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
        youtubeVideoUrl = "https://www.youtube.com/watch?v=g9aDIZJej_k",
        thumbnailUrl = "https://img.youtube.com/vi/g9aDIZJej_k/hqdefault.jpg",
        isFeatured = false
    )
)
