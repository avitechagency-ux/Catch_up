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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

// Light green header color matching the design requirement
val LightGreenHeader = Color(0xFF43A047)

/**
 * Data model for an offline cached study note within the app.
 * Cached notes are stored strictly in internal app cache/private storage,
 * never written to public device storage or OS-visible directories.
 */
data class CachedNote(
    val id: String,
    val title: String,
    val subject: String,
    val dateAdded: String,
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),
    val cacheKey: String = "internal_cache_note_"
)

// Alias for backwards compatibility
typealias DownloadedNote = CachedNote

const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000L

/**
 * Auto-expiry cache logic:
 * Checks each cached note's "last accessed" timestamp and removes it from the offline
 * cache list if more than 30 days have passed since last opened.
 */
fun pruneExpiredCachedNotes(
    currentCached: List<CachedNote>,
    currentTimeMs: Long = System.currentTimeMillis()
): List<CachedNote> {
    return currentCached.filter { note ->
        val ageMs = currentTimeMs - note.lastAccessedTimestamp
        ageMs <= THIRTY_DAYS_MS
    }
}

// Alias for backwards compatibility
fun pruneExpiredDownloads(
    currentDownloads: List<CachedNote>,
    currentTimeMs: Long = System.currentTimeMillis()
): List<CachedNote> = pruneExpiredCachedNotes(currentDownloads, currentTimeMs)

/**
 * Downloads Screen (Index 2 of Bottom Navigation).
 * Displays notes cached for offline in-app reading (stored securely in private app cache).
 *
 * HEADER:
 * - Same style as Subjects screen header card (28dp rounded bottom corners)
 * - Text: "Downloads" in large bold heading style
 * - Color: Light green (#43A047)
 *
 * BODY:
 * - List of cached note cards (rectangular with thin border, rounded corners)
 * - Or empty state if no saved notes
 *
 * BOTTOM NAVIGATION:
 * - Reused BottomNavigationBar with "Downloads" active
 */
@Composable
fun DownloadsScreen(
    downloadsList: List<CachedNote> = emptyList(),
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onNoteClick: (CachedNote) -> Unit = {},
    onPruneExpired: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Run auto-expiry check once when screen loads
    LaunchedEffect(Unit) {
        onPruneExpired()
    }

    val filteredDownloads = remember(downloadsList, searchQuery) {
        if (searchQuery.isBlank()) {
            downloadsList
        } else {
            downloadsList.filter {
                it.title.contains(searchQuery.trim(), ignoreCase = true) ||
                it.subject.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("downloads_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
        bottomBar = {
            // Reused Bottom Navigation Bar — "Downloads" (index 2) active
            BottomNavigationBar(
                selectedIndex = 2,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToSubjects()
                        2 -> { /* Already on Downloads */ }
                        3 -> onNavigateToProfile()
                        else -> onNavigateToTab(index)
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
            // 1. HEADER: Light Green background card matching Subjects screen header style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = LightGreenHeader,
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
                    text = "Downloads",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("downloads_title")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar embedded inside light green header card
                DownloadsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. BODY: List of saved notes or Empty state
            if (filteredDownloads.isEmpty()) {
                // Empty state centered vertically
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp)
                        .testTag("downloads_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 48dp download/bookmark outline icon (#C9C4DE)
                        Icon(
                            imageVector = Icons.Outlined.DownloadForOffline,
                            contentDescription = "No downloads",
                            tint = Color(0xFFC9C4DE),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("empty_downloads_icon")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Heading: "No downloads yet" (16sp SemiBold #1A1A1A)
                        Text(
                            text = if (searchQuery.isNotBlank()) "No downloads matching \"$searchQuery\"" else "No downloads yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("empty_downloads_heading")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtext: "Notes you save will appear here for offline access." (13sp #8A8A8A, max width 240dp)
                        Text(
                            text = if (searchQuery.isNotBlank()) "Check the spelling or try searching for another note title or subject." else "Notes you save will appear here for offline access.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF8A8A8A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(max = 240.dp)
                                .testTag("empty_downloads_subtext")
                        )
                    }
                }
            } else {
                // Vertical list of saved note cards
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("downloads_list"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDownloads, key = { it.id }) { note ->
                        DownloadedNoteCard(
                            note = note,
                            onClick = {
                                onNoteClick(note)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Opening \"${note.title}\" from offline cache...")
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
 * Rectangular bordered card for a downloaded note.
 * - Same card style as topic cards: white background, thin light border, rounded corners 12dp
 * - Sleek 1cm (~44-48dp) height profile
 * - Content:
 *   - Note title bold on left (e.g. "Algebra", 14sp Bold #1A1A1A)
 *   - Subject name below it (12sp #8A8A8A)
 *   - Small download-done icon (16dp, #4B3F8F) on the far right
 */
@Composable
fun DownloadedNoteCard(
    note: DownloadedNote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .shadow(
                elevation = 1.5.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x10000000),
                spotColor = Color(0x10000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFEBE9F2),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("download_card_${note.id}"),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                // Note title: Bold, 14sp, #1A1A1A
                Text(
                    text = note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("download_title_${note.id}")
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subject name: 12sp, #8A8A8A
                Text(
                    text = note.subject,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF8A8A8A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("download_subject_${note.id}")
                )
            }

            // Small download-done icon on far right (16dp, #4B3F8F)
            Icon(
                imageVector = Icons.Filled.FileDownloadDone,
                contentDescription = "Downloaded offline",
                tint = IndigoPurple,
                modifier = Modifier
                    .size(16.dp)
                    .testTag("download_saved_icon_${note.id}")
            )
        }
    }
}

/**
 * Embedded Search Bar inside Downloads header card
 */
@Composable
fun DownloadsSearchBar(
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
            .testTag("downloads_search_bar"),
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
                        text = "Search downloaded notes...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMuted
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(IndigoPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("downloads_search_input")
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = IconGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun DownloadsScreenPreview() {
    DownloadsScreen(
        downloadsList = listOf(
            DownloadedNote("1", "Algebra & Quadratic Equations", "Math", "18/09/2026"),
            DownloadedNote("2", "Physical Geography of East Africa", "Geography", "17/09/2026")
        )
    )
}
