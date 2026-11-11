package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun SubjectRevisionScreen(
    subjectName: String,
    onNavigateBack: () -> Unit,
    onDeckClick: (FlashcardDeck) -> Unit
) {
    BackHandler { onNavigateBack() }
    
    var searchQuery by remember { mutableStateOf("") }
    var decks by remember { mutableStateOf<List<FlashcardDeck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(subjectName) {
        isLoading = true
        val allDecks = SupabaseRepository.fetchAllFlashcardDecks()
        decks = allDecks.filter { it.subject.equals(subjectName, ignoreCase = true) }
        isLoading = false
    }

    val filteredDecks = remember(searchQuery, decks) {
        if (searchQuery.isBlank()) decks
        else decks.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

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
                    Text("$subjectName Revision", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                SubjectSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = IndigoPurple)
            } else if (filteredDecks.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Style, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No revision decks found for $subjectName.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDecks) { deck ->
                        RevisionDeckCard(deck = deck, onClick = { onDeckClick(deck) })
                    }
                }
            }
        }
    }
}

@Composable
fun RevisionDeckCard(deck: FlashcardDeck, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F7FC)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deck.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${deck.cards.size} Revision Cards", fontSize = 13.sp, color = TextMuted)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IndigoPurple
            ) {
                Text(
                    text = "Start",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
