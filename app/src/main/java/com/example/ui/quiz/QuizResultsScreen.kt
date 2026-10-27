package com.example.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Quiz
import com.example.data.QuizAttemptResult
import com.example.data.QuizRepository
import com.example.data.UserQuestionAnswer
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * Screen 5: Quiz Results
 *
 * Requirements:
 * - Shows final score, correct and incorrect answers count, percentage,
 *   and full question review with explanations.
 * - Includes "Retry Quiz" and "Back to Quizzes" action buttons.
 * - Persists attempt result using QuizRepository.
 */
@Composable
fun QuizResultsScreen(
    quiz: Quiz,
    score: Int,
    totalQuestions: Int,
    userAnswers: List<UserQuestionAnswer>,
    onRetryQuiz: () -> Unit,
    onBackToQuizzes: () -> Unit
) {
    BackHandler { onBackToQuizzes() }

    val context = LocalContext.current
    val percentage = if (totalQuestions > 0) ((score.toFloat() / totalQuestions) * 100).toInt() else 0

    // Save attempt on load
    LaunchedEffect(quiz.id, score) {
        val result = QuizAttemptResult(
            quizId = quiz.id,
            quizTitle = quiz.title,
            subject = quiz.subject,
            score = score,
            totalQuestions = totalQuestions,
            percentage = percentage,
            userAnswers = userAnswers
        )
        QuizRepository.saveQuizAttempt(context, result)
    }

    Scaffold(
        containerColor = Color.White,
        modifier = Modifier.testTag("quiz_results_screen"),
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
                        onClick = onBackToQuizzes,
                        modifier = Modifier.testTag("quiz_results_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Quiz Summary",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("quiz_results_title")
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Score Summary Header Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = quiz.color.copy(alpha = 0.25f),
                            spotColor = quiz.color.copy(alpha = 0.25f)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (percentage >= 50) Color(0xFFE6F4EA) else Color(0xFFFCE8E6),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$percentage%",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (percentage >= 50) Color(0xFF1E8E3E) else Color(0xFFD93025)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (percentage >= 80) "Excellent Work!" else if (percentage >= 50) "Good Effort!" else "Keep Practicing!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = quiz.title,
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats Breakdown Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBox(
                                label = "Total",
                                value = "$totalQuestions",
                                color = TextDark
                            )
                            StatBox(
                                label = "Correct",
                                value = "$score",
                                color = Color(0xFF1E8E3E)
                            )
                            StatBox(
                                label = "Incorrect",
                                value = "${totalQuestions - score}",
                                color = Color(0xFFD93025)
                            )
                        }
                    }
                }
            }

            // Action Buttons: Retry Quiz & Back to Quizzes
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onRetryQuiz,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("retry_quiz_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, quiz.color)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Replay,
                                contentDescription = "Retry",
                                tint = quiz.color
                            )
                            Text(
                                text = "Retry Quiz",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = quiz.color
                            )
                        }
                    }

                    Button(
                        onClick = onBackToQuizzes,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .testTag("back_to_quizzes_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = quiz.color,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Back to Quizzes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Question Review Title
            item {
                Text(
                    text = "Question Review (${userAnswers.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Detailed Question Review List with Explanations
            itemsIndexed(userAnswers, key = { idx, ans -> "${ans.questionId}_$idx" }) { index, ans ->
                ReviewQuestionItem(index = index + 1, answer = ans, quizColor = quiz.color)
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

/**
 * Question Review Item with student's answer, correct answer, and explanation.
 */
@Composable
private fun ReviewQuestionItem(
    index: Int,
    answer: UserQuestionAnswer,
    quizColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (answer.isCorrect) Color(0xFFCEEAD6) else Color(0xFFFAD2CF),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("review_item_$index"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Question $index",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = quizColor
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (answer.isCorrect) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (answer.isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = if (answer.isCorrect) Color(0xFF1E8E3E) else Color(0xFFD93025),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (answer.isCorrect) "Correct" else "Incorrect",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (answer.isCorrect) Color(0xFF1E8E3E) else Color(0xFFD93025)
                        )
                    }
                }
            }

            Text(
                text = answer.questionText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // User Answer Row
            Row {
                Text(
                    text = "Your Answer: ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Text(
                    text = answer.userAnswer.ifBlank { "(No Answer Provided)" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (answer.isCorrect) Color(0xFF1E8E3E) else Color(0xFFD93025)
                )
            }

            // Correct Answer Row if incorrect
            if (!answer.isCorrect) {
                Row {
                    Text(
                        text = "Correct Answer: ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Text(
                        text = answer.correctAnswer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E8E3E)
                    )
                }
            }

            // Explanation box if available
            if (!answer.explanation.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = quizColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = answer.explanation,
                            fontSize = 12.sp,
                            color = TextDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
