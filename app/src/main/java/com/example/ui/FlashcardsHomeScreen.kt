package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AccountSecurityVault
import com.example.data.Flashcard
import com.example.data.FlashcardDeck
import com.example.data.SupabaseRepository
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun FlashcardsHomeScreen(
    userRole: AccountSecurityVault.UserRole,
    onNavigateBack: () -> Unit,
    onDeckClick: (FlashcardDeck) -> Unit
) {
    BackHandler { onNavigateBack() }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var decks by remember { mutableStateOf<List<FlashcardDeck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        decks = SupabaseRepository.fetchAllFlashcardDecks()
        isLoading = false
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
                    Text("Flashcards", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        floatingActionButton = {
            if (userRole == AccountSecurityVault.UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = IndigoPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Deck")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = IndigoPurple)
            } else if (decks.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Style, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No flashcard decks available.", color = TextMuted)
                    Text("Admin will add some soon 😎", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(decks) { deck ->
                        DeckItemCard(deck = deck, onClick = { onDeckClick(deck) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFlashcardDeckDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newDeck ->
                coroutineScope.launch {
                    val success = SupabaseRepository.saveFlashcardDeck(newDeck)
                    if (success) {
                        Toast.makeText(context, "Deck added successfully!", Toast.LENGTH_SHORT).show()
                        decks = SupabaseRepository.fetchAllFlashcardDecks()
                        showAddDialog = false
                    } else {
                        Toast.makeText(context, "Failed to add deck.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun AddFlashcardDeckDialog(
    onDismiss: () -> Unit,
    onConfirm: (FlashcardDeck) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    val cards = remember { mutableStateListOf<Flashcard>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Create Flashcard Deck", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IndigoPurple)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "Close") }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Deck Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Add Cards", fontWeight = FontWeight.Bold, color = TextDark)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Answer") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (question.isNotBlank() && answer.isNotBlank()) {
                            cards.add(Flashcard(java.util.UUID.randomUUID().toString(), question, answer))
                            question = ""
                            answer = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple.copy(alpha = 0.1f), contentColor = IndigoPurple)
                ) {
                    Text("Add Card to Deck")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                cards.forEachIndexed { index, card ->
                    Text("${index + 1}. Q: ${card.question}", fontSize = 12.sp, color = TextDark)
                    Text("   A: ${card.answer}", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (title.isNotBlank() && subject.isNotBlank() && cards.isNotEmpty()) {
                            onConfirm(FlashcardDeck(java.util.UUID.randomUUID().toString(), title, subject, cards.toList()))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                ) {
                    Text("Save Deck to Database", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DeckItemCard(deck: FlashcardDeck, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F7FC)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deck.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = deck.subject, fontSize = 13.sp, color = TextMuted)
                    Text(text = " • ${deck.cards.size} cards", fontSize = 13.sp, color = TextMuted)
                }
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Open Deck", tint = TextMuted)
        }
    }
}
