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
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    onBookClick: (Book) -> Unit = {}
) {
    BackHandler { onNavigateBack() }
    
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }

    // Mock data for now, should be replaced by real data source
    val books = remember {
        listOf(
            Book("1", "Introduction to Algebra", "John Doe", null, null),
            Book("2", "Uganda Geography Atlas", "Geography Dept", null, null),
            Book("3", "Biology: Cell Structure", "Biology Faculty", null, null),
            Book("4", "History of East Africa", "History Master", null, null)
        )
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
        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No books found.", color = TextMuted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(filteredBooks) { book ->
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
            // Placeholder for cover
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ) {}
            Spacer(modifier = Modifier.height(8.dp))
            Text(book.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 2)
            book.author?.let {
                Text(it, fontSize = 12.sp, color = TextMuted, maxLines = 1)
            }
        }
    }
}
