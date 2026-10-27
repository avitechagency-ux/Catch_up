package com.example.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NavigateNext
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
import com.example.data.QuizQuestion
import com.example.data.QuizQuestionFormat
import com.example.data.UserQuestionAnswer
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * Screen 4: Taking a Quiz
 *
 * Requirements:
 * - Support exactly: Multiple-choice, True/false, Short-answer.
 * - Display one question at a time.
 * - Immediately after student submits an answer, reveal whether it is correct,
 *   the correct answer, and the explanation where available.
 * - Allow student to continue to the next question.
 */
@Composable
fun QuizTakingScreen(
    quiz: Quiz,
    onNavigateBack: () -> Unit,
    onQuizCompleted: (score: Int, total: Int, answers: List<UserQuestionAnswer>) -> Unit
) {
    BackHandler { onNavigateBack() }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val currentQuestion = quiz.questions.getOrNull(currentQuestionIndex) ?: return

    var selectedOption by remember(currentQuestionIndex) { mutableStateOf<String?>(null) }
    var shortAnswerText by remember(currentQuestionIndex) { mutableStateOf("") }
    var hasSubmitted by remember(currentQuestionIndex) { mutableStateOf(false) }

    val accumulatedAnswers = remember { mutableStateListOf<UserQuestionAnswer>() }

    val totalQuestions = quiz.questions.size
    val scrollState = rememberScrollState()

    // Determine correctness for current question
    val isAnswerCorrect = remember(hasSubmitted, selectedOption, shortAnswerText) {
        if (!hasSubmitted) false
        else {
            val userAns = when (currentQuestion.format) {
                QuizQuestionFormat.MULTIPLE_CHOICE, QuizQuestionFormat.TRUE_FALSE -> selectedOption?.trim().orEmpty()
                QuizQuestionFormat.SHORT_ANSWER -> shortAnswerText.trim()
            }
            if (userAns.isBlank()) false
            else {
                val matchesCorrect = userAns.equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)
                val matchesAcceptable = currentQuestion.acceptableAnswers.any { it.trim().equals(userAns, ignoreCase = true) }
                matchesCorrect || matchesAcceptable
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        modifier = Modifier.testTag("quiz_taking_screen"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("quiz_taking_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = quiz.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.testTag("quiz_taking_header_title")
                        )
                        Text(
                            text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFFFFB300),
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Format Tag & Question Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = quiz.color.copy(alpha = 0.20f),
                            spotColor = quiz.color.copy(alpha = 0.20f)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = quiz.color.copy(alpha = 0.12f)
                        ) {
                            val formatName = when (currentQuestion.format) {
                                QuizQuestionFormat.MULTIPLE_CHOICE -> "Multiple Choice"
                                QuizQuestionFormat.TRUE_FALSE -> "True / False"
                                QuizQuestionFormat.SHORT_ANSWER -> "Short Answer"
                            }
                            Text(
                                text = formatName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = quiz.color,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            lineHeight = 25.sp,
                            modifier = Modifier.testTag("question_text_${currentQuestion.id}")
                        )
                    }
                }

                // Answer Options / Inputs
                when (currentQuestion.format) {
                    QuizQuestionFormat.MULTIPLE_CHOICE -> {
                        val options = currentQuestion.options ?: emptyList()
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            options.forEachIndexed { idx, option ->
                                val isSelected = selectedOption == option
                                val optionLetter = ('A' + idx).toString()

                                OptionCard(
                                    letter = optionLetter,
                                    text = option,
                                    isSelected = isSelected,
                                    isEnabled = !hasSubmitted,
                                    onClick = { selectedOption = option }
                                )
                            }
                        }
                    }

                    QuizQuestionFormat.TRUE_FALSE -> {
                        val options = listOf("True", "False")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            options.forEach { option ->
                                val isSelected = selectedOption == option
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(16.dp))
                                        .clickable(enabled = !hasSubmitted) { selectedOption = option }
                                        .testTag("tf_option_$option"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) quiz.color else Color(0xFFF8FAFC),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) quiz.color else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = option,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    QuizQuestionFormat.SHORT_ANSWER -> {
                        OutlinedTextField(
                            value = shortAnswerText,
                            onValueChange = { if (!hasSubmitted) shortAnswerText = it },
                            placeholder = { Text("Type your answer here...") },
                            enabled = !hasSubmitted,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("short_answer_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = quiz.color,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                // Immediate Answer Feedback Panel (revealed after submitting)
                AnimatedVisibility(
                    visible = hasSubmitted,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Correctness Banner
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isAnswerCorrect) Color(0xFFE6F4EA) else Color(0xFFFCE8E6),
                            border = BorderStroke(
                                1.dp,
                                if (isAnswerCorrect) Color(0xFF34A853) else Color(0xFFEA4335)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feedback_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAnswerCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                                    contentDescription = null,
                                    tint = if (isAnswerCorrect) Color(0xFF1E8E3E) else Color(0xFFD93025),
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isAnswerCorrect) "Correct Answer!" else "Incorrect",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAnswerCorrect) Color(0xFF1E8E3E) else Color(0xFFD93025)
                                    )
                                    if (!isAnswerCorrect) {
                                        Text(
                                            text = "Correct answer: ${currentQuestion.correctAnswer}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDark
                                        )
                                    }
                                }
                            }
                        }

                        // Explanation Box (if available)
                        if (!currentQuestion.explanation.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("explanation_box")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Explanation",
                                        tint = quiz.color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Explanation",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = quiz.color
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentQuestion.explanation,
                                            fontSize = 13.sp,
                                            color = TextDark,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: "Submit Answer" -> then "Next Question" or "Finish Quiz"
            val isInputProvided = when (currentQuestion.format) {
                QuizQuestionFormat.MULTIPLE_CHOICE, QuizQuestionFormat.TRUE_FALSE -> !selectedOption.isNullOrBlank()
                QuizQuestionFormat.SHORT_ANSWER -> shortAnswerText.isNotBlank()
            }

            if (!hasSubmitted) {
                Button(
                    onClick = { hasSubmitted = true },
                    enabled = isInputProvided,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .testTag("submit_answer_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = quiz.color,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Submit Answer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        val userAns = when (currentQuestion.format) {
                            QuizQuestionFormat.MULTIPLE_CHOICE, QuizQuestionFormat.TRUE_FALSE -> selectedOption.orEmpty()
                            QuizQuestionFormat.SHORT_ANSWER -> shortAnswerText
                        }

                        val recordedAns = UserQuestionAnswer(
                            questionId = currentQuestion.id,
                            questionText = currentQuestion.questionText,
                            format = currentQuestion.format,
                            userAnswer = userAns,
                            isCorrect = isAnswerCorrect,
                            correctAnswer = currentQuestion.correctAnswer,
                            explanation = currentQuestion.explanation
                        )
                        accumulatedAnswers.add(recordedAns)

                        if (currentQuestionIndex < totalQuestions - 1) {
                            currentQuestionIndex++
                        } else {
                            // Quiz Completed!
                            val finalScore = accumulatedAnswers.count { it.isCorrect }
                            onQuizCompleted(finalScore, totalQuestions, accumulatedAnswers.toList())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("next_question_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = quiz.color,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentQuestionIndex < totalQuestions - 1) "Next Question" else "See Results",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack, // flipped or next icon
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Option Card for Multiple Choice Questions.
 */
@Composable
private fun OptionCard(
    letter: String,
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .testTag("mc_option_$letter"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) IndigoPurple else Color(0xFFCBD5E1)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) IndigoPurple else Color(0xFFE2E8F0),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = letter,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextDark
                    )
                }
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
