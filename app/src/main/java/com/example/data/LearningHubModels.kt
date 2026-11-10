package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

/**
 * The 6 Core Learning Hub Categories.
 */
enum class LearningHubCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    LIBRARY("Library", Icons.Outlined.LocalLibrary, DeepMaroon),
    STUDY_MATERIAL("Study Material", Icons.Outlined.AutoStories, TealTile),
    REVISION("Revision", Icons.Outlined.HistoryEdu, AmberTile),
    QUIZZES("Quizzes", Icons.Outlined.Quiz, PurpleTile),
    VISUAL_LEARNING("Visual Learning", Icons.Outlined.OndemandVideo, BlueTile),
    GAMES("Games", Icons.Outlined.SportsEsports, CoralTile),
    LESSONS("Lessons", Icons.Outlined.OndemandVideo, IndigoPurple),
    GLOSSARY("Glossary", Icons.Outlined.MenuBook, PurpleTile),
    MY_LEARNING("My Learning", Icons.Outlined.CalendarMonth, AmberTile),
    TOOLS("Tools", Icons.Outlined.Calculate, CoralTile),
    TIPS("Tips", Icons.Outlined.Lightbulb, GoldMustard)
}

/**
 * Educational resource model for Learning Hub.
 */
data class LearningHubResource(
    val id: String,
    val title: String,
    val subject: String,
    val category: LearningHubCategory,
    val targetClass: String = "S.4",
    val author: String = "UNEB Senior Examiner",
    val date: String = "26 Oct 2026",
    val type: String = "Notes",
    val isFeatured: Boolean = false,
    val documentUrl: String? = null
)

/**
 * Default curriculum resources for Cachap Learning Hub.
 */
val DefaultLearningHubResources: List<LearningHubResource> = listOf(
    LearningHubResource(
        id = "res-1",
        title = "Calculus & Pure Mathematics Revision Pack",
        subject = "Mathematics",
        category = LearningHubCategory.REVISION,
        targetClass = "S.4",
        author = "NCDC Certified Teacher",
        date = "26 Oct 2026",
        type = "Revision Guide",
        isFeatured = true
    ),
    LearningHubResource(
        id = "res-2",
        title = "UNEB Physics Past Papers & Model Solutions",
        subject = "Physics",
        category = LearningHubCategory.REVISION,
        targetClass = "S.4",
        author = "Physics Department",
        date = "25 Oct 2026",
        type = "UNEB Solutions",
        isFeatured = true
    ),
    LearningHubResource(
        id = "res-3",
        title = "Cell Structure & Genetics Illustrated Study Notes",
        subject = "Biology",
        category = LearningHubCategory.STUDY_MATERIAL,
        targetClass = "S.3",
        author = "Biology Faculty",
        date = "25 Oct 2026",
        type = "Comprehensive Notes",
        isFeatured = true
    ),
    LearningHubResource(
        id = "res-4",
        title = "East African History & Decolonization Summary",
        subject = "History",
        category = LearningHubCategory.LIBRARY,
        targetClass = "S.4",
        author = "Senior History Master",
        date = "24 Oct 2026",
        type = "Library Book",
        isFeatured = true
    ),
    LearningHubResource(
        id = "res-5",
        title = "Organic Chemistry Reactions & Mechanisms Guide",
        subject = "Chemistry",
        category = LearningHubCategory.STUDY_MATERIAL,
        targetClass = "S.4",
        author = "Chemistry Department",
        date = "24 Oct 2026",
        type = "Formulae Guide",
        isFeatured = false
    ),
    LearningHubResource(
        id = "res-6",
        title = "Ugandan Physical Geography & Map Work Notes",
        subject = "Geography",
        category = LearningHubCategory.LIBRARY,
        targetClass = "S.3",
        author = "Geography Faculty",
        date = "23 Oct 2026",
        type = "Atlas & Map Work",
        isFeatured = false
    ),
    LearningHubResource(
        id = "res-7",
        title = "English Grammar, Poetry & Comprehension Quiz 1",
        subject = "English",
        category = LearningHubCategory.QUIZZES,
        targetClass = "S.2",
        author = "Language Department",
        date = "23 Oct 2026",
        type = "Interactive Quiz",
        isFeatured = false
    ),
    LearningHubResource(
        id = "res-8",
        title = "Light Waves, Reflection & Refraction Video Guide",
        subject = "Physics",
        category = LearningHubCategory.VISUAL_LEARNING,
        targetClass = "S.4",
        author = "Science Labs",
        date = "22 Oct 2026",
        type = "Video Walkthrough",
        isFeatured = false
    ),
    LearningHubResource(
        id = "res-9",
        title = "Uganda Geography Trivia & UNEB Map Quest",
        subject = "Geography",
        category = LearningHubCategory.GAMES,
        targetClass = "S.1",
        author = "EdTech Studio",
        date = "22 Oct 2026",
        type = "Educational Game",
        isFeatured = false
    ),
    LearningHubResource(
        id = "res-10",
        title = "Commerce & Entrepreneurship Past Paper Practice",
        subject = "Commerce",
        category = LearningHubCategory.REVISION,
        targetClass = "S.4",
        author = "Commerce Master",
        date = "21 Oct 2026",
        type = "Revision Guide",
        isFeatured = false
    )
)
