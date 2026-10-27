package com.example.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DefaultCurriculumSubjects
import com.example.ui.SubjectItem
import com.example.ui.SubjectSearchBar
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * Custom Hexagon Shape for Subject Cards in Quizzes Home.
 * Creates a clean, flat-topped hexagonal geometric boundary.
 */
val HexagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val w4 = w * 0.22f
    val w34 = w * 0.78f
    val h2 = h * 0.50f

    moveTo(w4, 0f)
    lineTo(w34, 0f)
    lineTo(w, h2)
    lineTo(w34, h)
    lineTo(w4, h)
    lineTo(0f, h2)
    close()
}

/**
 * Screen 1: Quizzes Home — Subject Selection
 *
 * Requirements:
 * - Use creative hexagon-shaped subject cards instead of standard rectangular cards.
 * - Make the hexagons clean, well-aligned, evenly spaced, and easy to tap on a phone.
 * - Use existing subjects from the app (`DefaultCurriculumSubjects`).
 * - Display each subject name clearly inside its hexagon.
 * - Use existing Cachap colors with tasteful variations.
 * - Strictly NO subtitles, descriptions, or extra sections.
 */
@Composable
fun QuizzesHomeScreen(
    onNavigateBack: () -> Unit,
    onSubjectClick: (SubjectItem) -> Unit
) {
    BackHandler { onNavigateBack() }

    val allSubjects = remember { DefaultCurriculumSubjects }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSubjects = remember(searchQuery) {
        if (searchQuery.isBlank()) allSubjects
        else allSubjects.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.White,
        modifier = Modifier.testTag("quizzes_home_screen"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("quizzes_home_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Quizzes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("quizzes_home_title")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                SubjectSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quizzes_home_search_bar")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredSubjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No quiz subjects found for \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("quizzes_subject_grid"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(filteredSubjects, key = { it.name }) { subject ->
                        HexagonSubjectCard(
                            subject = subject,
                            onClick = { onSubjectClick(subject) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Creative Hexagon-Shaped Subject Card for Quizzes Home.
 *
 * Clean, well-aligned, easily touchable (min size 120.dp height), with icon & title inside.
 * Strictly contains ONLY the icon and clear bold subject name. No subtitles or secondary text.
 */
@Composable
fun HexagonSubjectCard(
    subject: SubjectItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(
                elevation = 6.dp,
                shape = HexagonShape,
                ambientColor = subject.color.copy(alpha = 0.40f),
                spotColor = subject.color.copy(alpha = 0.40f)
            )
            .clip(HexagonShape)
            .background(subject.color)
            .clickable(onClick = onClick)
            .testTag("hexagon_subject_card_${subject.name.lowercase()}"),
        shape = HexagonShape,
        color = subject.color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = subject.icon,
                            contentDescription = subject.name,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subject.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
