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
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Quiz
import com.example.data.SupabaseRepository
import com.example.ui.SubjectSearchBar
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Screen 2: Subject Quizzes — Quiz Selection (Loaded from Supabase)
 */
@Composable
fun SubjectQuizzesScreen(
    subjectName: String,
    onNavigateBack: () -> Unit,
    onQuizClick: (Quiz) -> Unit
) {
    BackHandler { onNavigateBack() }

    var searchQuery by remember { mutableStateOf("") }
    var availableQuizzes by remember { mutableStateOf<List<Quiz>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadQuizzes() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val fetched = SupabaseRepository.fetchQuizzesForSubject(subjectName)
                availableQuizzes = fetched
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load quizzes"
                isLoading = false
            }
        }
    }

    LaunchedEffect(subjectName) {
        loadQuizzes()
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_quizzes_title")
                    )
                    IconButton(
                        onClick = { loadQuizzes() },
                        modifier = Modifier.testTag("subject_quizzes_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh quizzes",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SubjectSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search quizzes...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_quizzes_search_bar")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IndigoPurple)
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Unable to load quizzes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "Please check your connection.", fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadQuizzes() }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)) {
                            Text("Retry")
                        }
                    }
                }
                filteredQuizzes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (availableQuizzes.isEmpty())
                                "No quizzes yet for this subject"
                            else
                                "No quizzes found matching \"$searchQuery\"",
                            color = TextMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.testTag("subject_quizzes_list")
                    ) {
                        items(filteredQuizzes, key = { it.id }) { quiz ->
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
}

/**
 * Quiz Selection Card — Clean card with wrapped/ellipsized title and no dangling bullets.
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val parts = mutableListOf<String>()
                if (quiz.topic.isNotBlank()) parts.add("Topic: ${quiz.topic}")
                parts.add("${quiz.questionCount} Questions")
                if (quiz.timeLimitMinutes != null) parts.add("${quiz.timeLimitMinutes} mins")
                val detailsText = parts.joinToString(" • ")

                Text(
                    text = detailsText,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
