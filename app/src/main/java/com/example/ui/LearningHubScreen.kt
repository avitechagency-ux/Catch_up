package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.GoldMustard
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftPastelBackground
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * The 6 Core Learning Hub Categories.
 * Styled following the visual pattern of SubjectCard in SubjectsScreen.
 *
 * NOTE: Strictly no subtitles, descriptions, or secondary text inside cards.
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
    GAMES("Games", Icons.Outlined.SportsEsports, CoralTile)
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
    val isFeatured: Boolean = false
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

/**
 * Page 1: Learning Hub Home Screen.
 *
 * Implements:
 * 1. Deep Maroon curved header card with back navigation and pill search bar.
 * 2. 6 Clean, uncluttered category cards in 2x3 grid (strictly NO subtext or secondary descriptions).
 * 3. Featured Resources section with horizontal preview cards.
 * 4. Recently Added Resources section with rich metadata.
 * 5. Real-time search with realistic empty states.
 * 6. Responsive layout matching Cachap theme.
 */
@Composable
fun LearningHubScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToStudyMaterial: () -> Unit = {},
    onNavigateToRevision: () -> Unit = {},
    onNavigateToQuizzes: () -> Unit = {},
    onNavigateToVisualLearning: () -> Unit = {},
    onOpenResource: (title: String, subject: String) -> Unit = { _, _ -> }
) {
    BackHandler { onNavigateBack() }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryForSection by remember { mutableStateOf<LearningHubCategory?>(null) }

    val allResources = remember { DefaultLearningHubResources }

    // Filter resources based on search query
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            allResources.filter {
                it.title.lowercase().contains(q) ||
                    it.subject.lowercase().contains(q) ||
                    it.category.title.lowercase().contains(q) ||
                    it.author.lowercase().contains(q)
            }
        }
    }

    val featuredResources = remember { allResources.filter { it.isFeatured } }
    val recentlyAddedResources = remember { allResources.take(6) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("learning_hub_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // 1. HEADER: Deep Maroon card with curved bottom
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
            ) {
                // Header row: Back button, Title, and Gold badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("learning_hub_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Learning Hub",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("learning_hub_title")
                        )
                    }

                    // Gold / Mustard badge removed
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar embedded inside Maroon header card
                LearningHubSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearQuery = {
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ==========================================
            // 2. MAIN SCROLLABLE CONTENT
            // ==========================================
            if (searchQuery.isNotBlank()) {
                // SEARCH RESULTS VIEW
                if (searchResults.isEmpty()) {
                    // Realistic Empty State
                    LearningHubEmptySearchState(
                        query = searchQuery,
                        onClearSearch = {
                            searchQuery = ""
                            focusManager.clearFocus()
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("learning_hub_search_results"),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Search Results (${searchResults.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        items(searchResults, key = { it.id }) { resource ->
                            LearningHubResourceCard(
                                resource = resource,
                                onClick = { onOpenResource(resource.title, resource.subject) }
                            )
                        }
                    }
                }
            } else {
                // DEFAULT HUB VIEW (Categories + Featured + Recently Added)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 32.dp)
                ) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // ------------------------------------------
                    // SECTION A: 6 CATEGORIES GRID (2x3)
                    // ------------------------------------------

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Row 1: Library (0), Study Material (1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LearningHubCategoryCard(
                                category = LearningHubCategory.LIBRARY,
                                onClick = onNavigateToLibrary,
                                modifier = Modifier.weight(1f)
                            )
                            LearningHubCategoryCard(
                                category = LearningHubCategory.STUDY_MATERIAL,
                                onClick = onNavigateToStudyMaterial,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Revision (2), Quizzes (3)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LearningHubCategoryCard(
                                category = LearningHubCategory.REVISION,
                                onClick = onNavigateToRevision,
                                modifier = Modifier.weight(1f)
                            )
                            LearningHubCategoryCard(
                                category = LearningHubCategory.QUIZZES,
                                onClick = onNavigateToQuizzes,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3: Visual Learning (4), Games (5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LearningHubCategoryCard(
                                category = LearningHubCategory.VISUAL_LEARNING,
                                onClick = onNavigateToVisualLearning,
                                modifier = Modifier.weight(1f)
                            )
                            LearningHubCategoryCard(
                                category = LearningHubCategory.GAMES,
                                onClick = { selectedCategoryForSection = LearningHubCategory.GAMES },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                }
            }
        }
    }

    // Modal Section View when tapping a category card
    selectedCategoryForSection?.let { cat ->
        val categoryResources = allResources.filter { it.category == cat }
        CategorySectionDialog(
            category = cat,
            resources = categoryResources,
            onDismiss = { selectedCategoryForSection = null },
            onResourceClick = { res ->
                selectedCategoryForSection = null
                onOpenResource(res.title, res.subject)
            }
        )
    }
}

/**
 * Single Learning Hub Category Card.
 * Following the visual pattern of SubjectCard in SubjectsScreen.
 *
 * CRITICAL RULE: Card contains ONLY the category icon and bold name.
 * Strictly no subtitles, descriptions, or secondary sentences.
 */
@Composable
fun LearningHubCategoryCard(
    category: LearningHubCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagKey = category.name.lowercase()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = category.color.copy(alpha = 0.35f),
                spotColor = category.color.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("learning_hub_card_$tagKey"),
        shape = RoundedCornerShape(18.dp),
        color = category.color,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = Color.White,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("learning_hub_icon_$tagKey")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Name Only (Bold, White)
            Text(
                text = category.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("learning_hub_name_$tagKey")
            )
        }
    }
}

/**
 * Search Bar embedded in the Maroon header, matching SubjectSearchBar in SubjectsScreen.
 */
@Composable
fun LearningHubSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFECEBF2),
                shape = RoundedCornerShape(26.dp)
            )
            .testTag("learning_hub_search_bar"),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = DeepMaroon,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search learning resources, topics...",
                        fontSize = 14.sp,
                        color = IconGrey
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    ),
                    cursorBrush = SolidColor(DeepMaroon),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {}),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("learning_hub_search_input")
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = IconGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Featured Resource Card in horizontal scroll row.
 */
@Composable
fun FeaturedResourceCard(
    resource: LearningHubResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(240.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = DeepMaroon.copy(alpha = 0.15f),
                spotColor = Color(0x10000000)
            )
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("featured_resource_${resource.id}"),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = resource.category.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = resource.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = resource.category.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftPastelBackground
                ) {
                    Text(
                        text = resource.targetClass,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepMaroon,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = resource.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resource.author,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DeepMaroon
            ) {
                Text(
                    text = "Open Resource",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * List card for Learning Hub Resource (Used in search results & recently added).
 */
@Composable
fun LearningHubResourceCard(
    resource: LearningHubResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x10000000),
                spotColor = Color(0x10000000)
            )
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("learning_resource_item_${resource.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = resource.category.color.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = resource.category.icon,
                        contentDescription = null,
                        tint = resource.category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resource.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = resource.category.color
                    )
                    Text(
                        text = " • ${resource.targetClass}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = resource.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${resource.author} • ${resource.date}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SoftPastelBackground
            ) {
                Text(
                    text = "Open",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Realistic Empty Search State.
 */
@Composable
fun LearningHubEmptySearchState(
    query: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = SoftPastelBackground,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = DeepMaroon,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No resources found for \"$query\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try searching by subject name (e.g. Mathematics, Biology) or a different topic keyword.",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onClearSearch,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepMaroon,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Clear Search",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Dialog opening the selected Category section with corresponding resources.
 */
@Composable
fun CategorySectionDialog(
    category: LearningHubCategory,
    resources: List<LearningHubResource>,
    onDismiss: () -> Unit,
    onResourceClick: (LearningHubResource) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        modifier = Modifier.testTag("category_section_dialog_${category.name.lowercase()}"),
        icon = {
            Surface(
                shape = CircleShape,
                color = category.color,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = category.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (resources.isEmpty()) {
                    Text(
                        text = "No resources found in this category yet. Check back soon!",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    Text(
                        text = "Available ${category.title} Resources:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    resources.forEach { res ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onResourceClick(res) },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPastelBackground,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = res.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${res.subject} • ${res.targetClass}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DeepMaroon
                                ) {
                                    Text(
                                        text = "Open",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepMaroon,
                    contentColor = Color.White
                )
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Quick Access Banner for the Learning Hub.
 * Embedded into both Student and Teacher Home screens.
 */
@Composable
fun LearningHubQuickAccessBanner(
    onNavigateToLearningHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onNavigateToLearningHub)
            .testTag("learning_hub_quick_access_banner"),
        shape = RoundedCornerShape(16.dp),
        color = BlueTile,
        border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.6f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GoldMustard,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.LocalLibrary,
                            contentDescription = "Learning Hub",
                            tint = DeepMaroon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Learning Hub",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onNavigateToLearningHub,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldMustard,
                    contentColor = DeepMaroon
                ),
                modifier = Modifier.testTag("learning_hub_open_button")
            ) {
                Text(
                    text = "Open",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LearningHubScreenPreview() {
    LearningHubScreen()
}
