package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import kotlinx.coroutines.launch

/**
 * Data item representing a Subject in the grid.
 */
data class SubjectItem(
    val name: String,
    val noteCount: Int,
    val icon: ImageVector,
    val color: Color
)

/**
 * Subjects Screen for the Catch Up secondary school notes-sharing app.
 * Features the Ugandan Lower and Upper Secondary School Curriculum subjects (NCDC / UNEB):
 * 1. Purple header card with "Subjects" title and embedded search bar
 * 2. 2-column Grid with all curriculum subjects styled with distinct colors and outline icons
 * 3. Bottom navigation bar with "Subjects" active
 */
@Composable
fun SubjectsScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onSubjectClick: (SubjectItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Full Ugandan Secondary Curriculum Subjects (NCDC / UNEB)
    val allSubjects = remember {
        listOf(
            // Core Sciences & Mathematics
            SubjectItem(name = "Math", noteCount = 12, icon = Icons.Outlined.Architecture, color = TealTile),
            SubjectItem(name = "English", noteCount = 14, icon = Icons.Outlined.MenuBook, color = Color(0xFFD81B60)),
            SubjectItem(name = "Physics", noteCount = 8, icon = Icons.Outlined.Hub, color = PurpleTile),
            SubjectItem(name = "Chemistry", noteCount = 9, icon = Icons.Outlined.Science, color = AmberTile),
            SubjectItem(name = "Biology", noteCount = 15, icon = Icons.Outlined.Eco, color = NavyTile),
            SubjectItem(name = "Geography", noteCount = 11, icon = Icons.Outlined.Public, color = CoralTile),
            SubjectItem(name = "History", noteCount = 10, icon = Icons.Outlined.AccountBalance, color = BlueTile),

            // Vocational, Applied & Technical Subjects
            SubjectItem(name = "Agriculture", noteCount = 13, icon = Icons.Outlined.Agriculture, color = Color(0xFF2E7D32)),
            SubjectItem(name = "ICT", noteCount = 16, icon = Icons.Outlined.Computer, color = Color(0xFF00838F)),
            SubjectItem(name = "Entrepreneurship", noteCount = 9, icon = Icons.Outlined.TrendingUp, color = Color(0xFFF39C12)),
            SubjectItem(name = "Commerce", noteCount = 7, icon = Icons.Outlined.PointOfSale, color = Color(0xFF3F51B5)),
            SubjectItem(name = "Economics", noteCount = 8, icon = Icons.Outlined.ShowChart, color = Color(0xFF1B5E20)),
            SubjectItem(name = "Technology & Design", noteCount = 6, icon = Icons.Outlined.DesignServices, color = Color(0xFF5D4037)),
            SubjectItem(name = "Nutrition & Food Tech", noteCount = 7, icon = Icons.Outlined.Restaurant, color = Color(0xFFE65100)),

            // Religious & Moral Education
            SubjectItem(name = "C.R.E", noteCount = 8, icon = Icons.Outlined.Church, color = Color(0xFF8E24AA)),
            SubjectItem(name = "I.R.E", noteCount = 6, icon = Icons.Outlined.Mosque, color = Color(0xFF00796B)),

            // Arts, Languages & Humanities
            SubjectItem(name = "Literature", noteCount = 11, icon = Icons.Outlined.AutoStories, color = Color(0xFFC2185B)),
            SubjectItem(name = "Fine Art", noteCount = 5, icon = Icons.Outlined.Palette, color = Color(0xFFFF5722)),
            SubjectItem(name = "Performing Arts", noteCount = 6, icon = Icons.Outlined.MusicNote, color = Color(0xFF9C27B0)),
            SubjectItem(name = "Physical Education", noteCount = 7, icon = Icons.Outlined.SportsSoccer, color = Color(0xFFC0392B)),
            SubjectItem(name = "Kiswahili", noteCount = 10, icon = Icons.Outlined.RecordVoiceOver, color = Color(0xFFD35400)),
            SubjectItem(name = "Luganda", noteCount = 8, icon = Icons.Outlined.Translate, color = Color(0xFF16A085)),
            SubjectItem(name = "French", noteCount = 5, icon = Icons.Outlined.Language, color = Color(0xFF455A64)),
            SubjectItem(name = "German", noteCount = 4, icon = Icons.Outlined.Forum, color = Color(0xFF37474F)),
            SubjectItem(name = "Arabic", noteCount = 4, icon = Icons.Outlined.Spellcheck, color = Color(0xFF00695C))
        )
    }

    val filteredSubjects = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allSubjects
        } else {
            allSubjects.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("subjects_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 1, // "Subjects" is active
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> { /* Already on Subjects */ }
                        2 -> onNavigateToDownloads()
                        3 -> onNavigateToProfile()
                        else -> {
                            val tabName = NavItem.values().getOrNull(index)?.label ?: "Tab"
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Opening $tabName...")
                            }
                            onNavigateToTab(index)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // 1. HEADER: Purple background card matching Home header with "Subjects" title and embedded search bar (no welcome text)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Subjects",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("subjects_title")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar embedded inside purple header card
                SubjectSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. SUBJECT GRID: 2 columns, 6 cards total
            if (filteredSubjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subjects found for \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = IconGrey,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("subjects_grid"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredSubjects, key = { it.name }) { subject ->
                        SubjectCard(
                            subject = subject,
                            onClick = {
                                onSubjectClick(subject)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Opening ${subject.name} notes...")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Subject Search Bar in normal flow with "Search subjects..." placeholder
 */
@Composable
fun SubjectSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFECEBF2),
                shape = RoundedCornerShape(26.dp)
            )
            .testTag("subjects_search_bar"),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = IconGrey,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search subjects...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = IconGrey
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subjects_search_input")
                )
            }
        }
    }
}

/**
 * Individual Subject Card:
 * - Distinct solid background color matching the Quick Access theme palette
 * - Rounded corners (18dp) with 1dp subtle frosted white border and color-matched ambient elevation
 * - Crisp white outline icon (44dp)
 * - Subject name in bold white (no note count text)
 */
@Composable
fun SubjectCard(
    subject: SubjectItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = subject.color.copy(alpha = 0.35f),
                spotColor = subject.color.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("subject_card_${subject.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        color = subject.color,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Subject Outline Icon in crisp White
            Icon(
                imageVector = subject.icon,
                contentDescription = subject.name,
                tint = Color.White,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("subject_icon_${subject.name.lowercase()}")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subject Name only (Bold, White)
            Text(
                text = subject.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("subject_name_${subject.name.lowercase()}")
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SubjectsScreenPreview() {
    SubjectsScreen()
}
