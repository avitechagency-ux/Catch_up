package com.example.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Quiz
import com.example.data.QuizRepository
import com.example.ui.SubjectSearchBar
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextMuted

/**
 * Screen 2: Subject Quizzes — Quiz Selection
 *
 * Requirements:
 * - Filtered strictly to approved quizzes for the selected subject.
 * - Reuses the EXACT card component style from the Revision page for the quiz cards
 *   (110.dp height, RoundedCornerShape(18.dp), shadow, white border, circle icon, title & details).
 * - Distinct colors for quiz cards.
 * - Displays quiz title and necessary details (topic, question count, time limit if configured).
 * - Includes search bar to filter quizzes by title or topic.
 */
@Composable
fun SubjectQuizzesScreen(
    subjectName: String,
    onNavigateBack: () -> Unit,
    onQuizClick: (Quiz) -> Unit
) {
    BackHandler { onNavigateBack() }

    var searchQuery by remember { mutableStateOf("") }

    // Fetch approved quizzes for this subject only
    val availableQuizzes = remember(subjectName) {
        QuizRepository.getApprovedQuizzesForSubject(subjectName)
    }

    val filteredQuizzes = remember(searchQuery, availableQuizzes) {
        if (searchQuery.isBlank()) {
            availableQuizzes
        } else {
            availableQuizzes.filter { quiz ->
                quiz.title.contains(searchQuery.trim(), ignoreCase = true) ||
                    quiz.topic.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        modifier = Modifier.testTag("subject_quizzes_screen"),
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
                        modifier = Modifier.testTag("subject_quizzes_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "$subjectName Quizzes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("subject_quizzes_title")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                SubjectSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_quizzes_search_bar")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredQuizzes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (availableQuizzes.isEmpty())
                            "No approved quizzes available for $subjectName yet."
                        else
                            "No quizzes found matching \"$searchQuery\"",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.testTag("subject_quizzes_list")
                ) {
                    items(filteredQuizzes, key = { it.id }) { quiz ->
                        // Reused exact card component layout from Revision page
                        QuizSelectionCard(
                            quiz = quiz,
                            onClick = { onQuizClick(quiz) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quiz Selection Card — Exact component structure, size, dimensions, styling,
 * and layout as RevisionSubjectCard from the Revision page.
 */
@Composable
fun QuizSelectionCard(
    quiz: Quiz,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = quiz.color.copy(alpha = 0.35f),
                spotColor = quiz.color.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("quiz_card_${quiz.id}"),
        shape = RoundedCornerShape(18.dp),
        color = quiz.color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Quiz,
                        contentDescription = quiz.title,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = quiz.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                val detailsText = buildString {
                    append("Topic: ").append(quiz.topic)
                    append(" • ").append(quiz.questionCount).append(" Questions")
                    if (quiz.timeLimitMinutes != null) {
                        append(" • ").append(quiz.timeLimitMinutes).append(" mins")
                    }
                }
                Text(
                    text = detailsText,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}
