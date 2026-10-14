package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Helper to get the designated brand color for each Ugandan curriculum subject card.
 */
fun getSubjectThemeColor(subjectName: String): Color {
    return when (subjectName.trim().lowercase()) {
        "math", "mathematics" -> TealTile
        "english" -> Color(0xFFD81B60)
        "physics" -> PurpleTile
        "chemistry" -> AmberTile
        "biology" -> NavyTile
        "geography" -> CoralTile
        "history" -> BlueTile
        "agriculture" -> Color(0xFF2E7D32)
        "ict" -> Color(0xFF00838F)
        "entrepreneurship" -> Color(0xFFF39C12)
        "commerce" -> Color(0xFF3F51B5)
        "economics" -> Color(0xFF1B5E20)
        "technology & design", "technology and design" -> Color(0xFF5D4037)
        "nutrition & food tech", "nutrition and food tech" -> Color(0xFFE65100)
        "c.r.e", "cre" -> Color(0xFF8E24AA)
        "i.r.e", "ire" -> Color(0xFF00796B)
        "literature" -> Color(0xFFC2185B)
        "fine art" -> Color(0xFFFF5722)
        "performing arts" -> Color(0xFF9C27B0)
        "physical education", "pe" -> Color(0xFFC0392B)
        "kiswahili", "swahili" -> Color(0xFFD35400)
        "luganda" -> Color(0xFF16A085)
        "french" -> Color(0xFF455A64)
        "german" -> Color(0xFF37474F)
        "arabic" -> Color(0xFF00695C)
        else -> IndigoPurple
    }
}

/**
 * Normalizes a secondary class string (e.g. "S.4 Green", "s4", "S.1", "Senior 2")
 * to its canonical Ugandan secondary class level ("S.1" .. "S.6").
 */
fun normalizeSecondaryClass(rawClass: String): String {
    val cleaned = rawClass.trim()
    if (cleaned.isEmpty()) return ""
    val match = Regex("(?i)\\b(?:s\\.?|senior)\\s*([1-6])\\b").find(cleaned)
    if (match != null) {
        return "S.${match.groupValues[1]}"
    }
    return cleaned.uppercase()
}

/**
 * Checks whether a note's targetClass matches a student's academicClass.
 * Ensures students in one class (e.g. S.4) NEVER receive notes uploaded for other classes (e.g. S.1, S.2, S.3, S.5, S.6).
 */
fun doesNoteMatchStudentClass(noteTargetClass: String, studentClass: String): Boolean {
    val cleanStudent = studentClass.trim()
    if (cleanStudent.isEmpty()) return true
    val cleanNoteClass = noteTargetClass.trim()
    if (cleanNoteClass.isEmpty()) return false

    val normalizedStudent = normalizeSecondaryClass(cleanStudent)
    val noteClassMatches = Regex("(?i)\\b(?:s\\.?|senior)\\s*([1-6])\\b")
        .findAll(cleanNoteClass)
        .map { "S.${it.groupValues[1]}" }
        .toSet()

    return if (noteClassMatches.isNotEmpty()) {
        normalizedStudent in noteClassMatches
    } else {
        cleanNoteClass.equals(cleanStudent, ignoreCase = true) ||
            cleanNoteClass.equals(normalizedStudent, ignoreCase = true)
    }
}

/**
 * Checks whether a note's teacherIdCode matches the Teacher ID connected by the student for that subject.
 */
fun doesNoteMatchLinkedTeacher(noteTeacherIdCode: String, linkedTeacherIdCode: String): Boolean {
    val cleanLinked = linkedTeacherIdCode.trim().uppercase()
    if (cleanLinked.isEmpty()) return true
    val cleanNoteCode = noteTeacherIdCode.trim().uppercase()
    if (cleanNoteCode.isEmpty()) return false
    return cleanNoteCode == cleanLinked
}

/**
 * Filters a list of [TopicItem] notes or assignments strictly by the student's class, stream, and connected Teacher ID.
 */
fun filterTopicsForStudent(
    topics: List<TopicItem>,
    studentClass: String,
    studentStream: String = "",
    linkedTeacherIdCode: String = ""
): List<TopicItem> {
    return topics.filter { item ->
        val classAndStreamMatches = com.example.data.doesContentTargetStudent(
            targetClass = item.targetClass,
            targetStreams = item.targetStreams,
            studentClass = studentClass,
            studentStream = studentStream
        )
        val teacherMatches = linkedTeacherIdCode.isBlank() ||
            item.teacherIdCode.isBlank() ||
            doesNoteMatchLinkedTeacher(item.teacherIdCode, linkedTeacherIdCode)
        classAndStreamMatches && teacherMatches
    }
}

/**
 * Data model for a topic item under a subject.
 */
data class TopicItem(
    val id: String,
    val title: String,
    val date: String,
    val fileType: String = "DOCX",
    val targetClass: String = "",
    val targetStreams: String = "ALL",
    val teacherIdCode: String = ""
)

/**
 * Toggle states between Notes and Assignments.
 */
enum class NotesContentType {
    NOTES,
    ASSIGNMENTS
}

/**
 * Subject Notes screen displaying notes & assignments for a selected subject (e.g., "Math").
 *
 * Structure:
 * 1. HEADER: Subject color-matched card header (e.g. Teal for Math, Purple for Physics, etc.)
 *    with back navigation button, subject title, and embedded search bar
 * 2. TOGGLE: "Notes" / "Assignments" segmented pill container
 * 3. TOPIC LIST: Rectangular cards with topic title on left and date on right
 * 4. BOTTOM NAVIGATION BAR: Reused with "Subjects" as active tab
 */
@Composable
fun SubjectNotesScreen(
    subjectName: String = "Math",
    studentClass: String = "",
    linkedTeacherIdCode: String = "",
    modifier: Modifier = Modifier,
    initialNotes: List<TopicItem> = emptyList(),
    initialAssignments: List<TopicItem> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = onNavigateBack,
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onTopicClick: (TopicItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedContentType by remember { mutableStateOf(NotesContentType.NOTES) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var liveNotes by remember(subjectName, studentClass, linkedTeacherIdCode, initialNotes) {
        mutableStateOf(filterTopicsForStudent(initialNotes, studentClass, linkedTeacherIdCode))
    }
    var liveAssignments by remember(subjectName, studentClass, linkedTeacherIdCode, initialAssignments) {
        mutableStateOf(filterTopicsForStudent(initialAssignments, studentClass, linkedTeacherIdCode))
    }

    androidx.compose.runtime.LaunchedEffect(subjectName, studentClass, linkedTeacherIdCode, initialNotes, initialAssignments) {
        val fetchedNotes = com.example.data.SupabaseRepository.fetchNotesForSubject(
            subjectName = subjectName,
            contentType = "Notes",
            studentClass = studentClass,
            teacherIdCode = linkedTeacherIdCode
        )
        val fetchedAssignments = com.example.data.SupabaseRepository.fetchNotesForSubject(
            subjectName = subjectName,
            contentType = "Assignment",
            studentClass = studentClass,
            teacherIdCode = linkedTeacherIdCode
        )
        val scopedInitialNotes = filterTopicsForStudent(initialNotes, studentClass, linkedTeacherIdCode)
        val scopedInitialAssignments = filterTopicsForStudent(initialAssignments, studentClass, linkedTeacherIdCode)
        liveNotes = (scopedInitialNotes + fetchedNotes).distinctBy { it.title.trim().lowercase() }
        liveAssignments = (scopedInitialAssignments + fetchedAssignments).distinctBy { it.title.trim().lowercase() }
    }

    val headerColor = remember(subjectName) { getSubjectThemeColor(subjectName) }

    // Handle Android system back gesture to return to previous screen
    BackHandler {
        onNavigateBack()
    }

    val rawTopics = when (selectedContentType) {
        NotesContentType.NOTES -> liveNotes
        NotesContentType.ASSIGNMENTS -> liveAssignments
    }

    val filteredTopics = remember(searchQuery, rawTopics) {
        if (searchQuery.isBlank()) {
            rawTopics
        } else {
            rawTopics.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("subject_notes_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
        bottomBar = {
            // Reused Bottom Navigation Bar — "Subjects" tab (index 1) remains active
            BottomNavigationBar(
                selectedIndex = 1,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToSubjects()
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
            // 1. HEADER: Styled like the Subjects Screen header, colored with the specific subject card color
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = headerColor,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
            ) {
                // Top Row: Back Button & Subject Name Heading
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("subject_notes_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Subjects",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = subjectName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("subject_notes_header_title")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar embedded directly inside the subject colored header card
                SubjectNotesSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. TOGGLE — "Notes" / "Assignments" segmented pill container
            NotesSegmentedToggle(
                selectedType = selectedContentType,
                activeColor = headerColor,
                onSelectType = { selectedContentType = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TOPIC LIST (rectangular cards)
            if (filteredTopics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No topics found matching \"$searchQuery\"" else "No items available",
                        fontSize = 14.sp,
                        color = IconGrey,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("topic_list"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTopics, key = { it.id }) { topic ->
                        TopicCard(
                            topic = topic,
                            backgroundColor = headerColor,
                            onClick = {
                                onTopicClick(topic)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Opening \"${topic.title}\" PDF notes...")
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
 * Search Bar component embedded inside the subject notes header card
 */
@Composable
fun SubjectNotesSearchBar(
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
            .testTag("subject_notes_search_bar"),
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
                contentDescription = "Search notes",
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
                        text = "Search notes...",
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
                        .testTag("subject_notes_search_input")
                )
            }
        }
    }
}

/**
 * 2. TOGGLE — "Notes" / "Assignments"
 * - A single pill-shaped container split into two equal segments side by side
 * - Container background: light lavender tint (#F1EFF8)
 * - Active segment: filled with subject theme color (e.g. Teal for Math, Purple for Physics, etc.), white bold text
 * - Inactive segment: transparent background, grey text, no fill
 */
@Composable
fun NotesSegmentedToggle(
    selectedType: NotesContentType,
    activeColor: Color,
    onSelectType: (NotesContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .testTag("segmented_toggle_container"),
        shape = RoundedCornerShape(23.dp),
        color = Color(0xFFF1EFF8),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notes Segment
            val isNotesActive = selectedType == NotesContentType.NOTES
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isNotesActive) activeColor else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectType(NotesContentType.NOTES) }
                    .testTag("toggle_notes_segment"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Notes",
                    fontSize = 14.sp,
                    fontWeight = if (isNotesActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isNotesActive) Color.White else TextMuted
                )
            }

            // Assignments Segment
            val isAssignmentsActive = selectedType == NotesContentType.ASSIGNMENTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isAssignmentsActive) activeColor else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectType(NotesContentType.ASSIGNMENTS) }
                    .testTag("toggle_assignments_segment"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Assignments",
                    fontSize = 14.sp,
                    fontWeight = if (isAssignmentsActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAssignmentsActive) Color.White else TextMuted
                )
            }
        }
    }
}

/**
 * 3. TOPIC LIST CARD (rectangular cards)
 * - Full width (minus screen margins)
 * - Sleek 1cm (~44-48dp) height profile with rounded corners (12dp)
 * - Colored with the subject card's distinct theme color
 * - Topic name on left (bold white text) and date on right (crisp light white text)
 */
@Composable
fun TopicCard(
    topic: TopicItem,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x18000000)
            )
            .bounceClick(onClick = onClick)
            .testTag("topic_card_${topic.id}"),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Topic name in bold white text
            Text(
                text = topic.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .testTag("topic_title_${topic.id}")
            )

            // Right: Date in lighter semi-transparent white text
            Text(
                text = topic.date,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.testTag("topic_date_${topic.id}")
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SubjectNotesScreenPreview() {
    SubjectNotesScreen(subjectName = "Math")
}
