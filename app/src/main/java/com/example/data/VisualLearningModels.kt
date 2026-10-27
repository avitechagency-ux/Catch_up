package com.example.data

import androidx.annotation.DrawableRes

/**
 * Aspect ratios supported in Cachap Pinterest-inspired visual discovery.
 */
enum class VisualAspectRatio(val ratio: Float, val label: String) {
    RATIO_1_1(1.0f, "1:1 Square"),
    RATIO_4_5(0.8f, "4:5 Portrait"),
    RATIO_3_4(0.75f, "3:4 Tall"),
    RATIO_16_9(16f / 9f, "16:9 Landscape")
}

/**
 * Source of the educational visual.
 */
enum class VisualSourceType {
    LEARNING_HUB,
    TEACHER_UPLOAD
}

/**
 * Section within the connected educational article.
 */
data class ArticleSection(
    val heading: String,
    val body: String,
    val keyPoints: List<String> = emptyList()
)

/**
 * Full educational article connected to an educational visual.
 */
data class VisualLearningArticle(
    val title: String,
    val introduction: String,
    val sections: List<ArticleSection>,
    val keyFacts: List<String> = emptyList(),
    val curriculumTip: String? = null
)

/**
 * Single educational visual item displayed in the masonry discovery feed.
 */
data class VisualLearningItem(
    val id: String,
    val title: String,
    val subject: String,
    val targetClass: String = "S.4",
    val author: String = "Cachap Curriculum Specialist",
    val date: String = "26 Oct 2026",
    @DrawableRes val drawableResId: Int? = null,
    val imageUrl: String? = null,
    val aspectRatio: VisualAspectRatio = VisualAspectRatio.RATIO_3_4,
    val sourceType: VisualSourceType = VisualSourceType.LEARNING_HUB,
    val status: String = "Approved", // "Approved", "Pending", "Rejected"
    val isApproved: Boolean = true,
    val article: VisualLearningArticle
)
