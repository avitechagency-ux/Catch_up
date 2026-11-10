package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import com.example.data.SupabaseRepository
import com.example.data.LearningHubCategory
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    onBookClick: (Book) -> Unit = {}
) {
    BackHandler { onNavigateBack() }
    
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loadLibraryData: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val resources = SupabaseRepository.fetchAllHubResources()
                val libraryResources = resources.filter { 
                    it.category.name.equals("LIBRARY", ignoreCase = true) || 
                    it.category == LearningHubCategory.LIBRARY 
                }
                books = libraryResources.map { res ->
                    Book(
                        id = res.id ?: java.util.UUID.randomUUID().toString(),
                        title = res.title,
                        author = res.author.ifBlank { "UNEB Senior Examiner" },
                        coverUrl = null,
                        documentUrl = res.documentUrl
                    )
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load library resources: ${e.localizedMessage}"
                books = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLibraryData()
    }

    val filteredBooks = remember(searchQuery, books) {
        if (searchQuery.isBlank()) books
        else books.filter { it.title.contains(searchQuery, ignoreCase = true) || it.author?.contains(searchQuery, ignoreCase = true) == true }
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
                    Text("Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
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
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IndigoPurple)
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage ?: "Error loading library", color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { loadLibraryData() },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        } else if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = "No books",
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No library books yet.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Upload books from your Admin panel to see them here.", fontSize = 13.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(filteredBooks, key = { it.id }) { book ->
                    BookCard(book = book, onClick = { onBookClick(book) })
                }
            }
        }
    }
}

@Composable
fun BookCard(book: Book, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Book Cover / PDF thumbnail placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                color = IndigoPurple.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = book.title,
                        tint = IndigoPurple,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(book.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 2)
            book.author?.let {
                Text(it, fontSize = 12.sp, color = TextMuted, maxLines = 1)
            }
        }
    }
}
