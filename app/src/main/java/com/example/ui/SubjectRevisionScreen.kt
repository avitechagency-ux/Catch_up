package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flashcard
import com.example.ui.theme.*

@Composable
fun SubjectRevisionScreen(
    subjectName: String,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    
    // Placeholder flashcard data
    val allCards = remember {
        listOf(
            Flashcard("1", subjectName, "Question 1 for $subjectName", "Answer 1"),
            Flashcard("2", subjectName, "Question 2 for $subjectName", "Answer 2")
        )
    }
    
    val filteredCards = remember(searchQuery, allCards) {
        if (searchQuery.isBlank()) allCards
        else allCards.filter { it.question.contains(searchQuery, ignoreCase = true) }
    }
    
    var currentCardIndex by remember { mutableIntStateOf(0) }
    
    Scaffold(
        containerColor = Color.White,
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(subjectName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Reuse LearningHubSearchBar
                SubjectSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (filteredCards.isEmpty()) {
                Text("No flashcards found.", color = TextMuted)
            } else {
                val card = filteredCards[currentCardIndex % filteredCards.size]
                FlashcardView(
                    card = card,
                    onNext = { currentCardIndex++ }
                )
            }
        }
    }
}

@Composable
fun FlashcardView(card: Flashcard, onNext: () -> Unit) {
    var isAnswerVisible by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable { isAnswerVisible = !isAnswerVisible }
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoPurple)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = if (isAnswerVisible) card.answer else card.question,
                color = Color.White,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    if (isAnswerVisible) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isAnswerVisible = false; onNext() }) { Text("Review again") }
            Button(onClick = { isAnswerVisible = false; onNext() }) { Text("Know it") }
        }
    }
}
