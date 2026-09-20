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
 * Data model for a topic item under a subject.
 */
data class TopicItem(
    val id: String,
    val title: String,
    val date: String,
    val fileType: String = "PDF"
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
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onTopicClick: (TopicItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedContentType by remember { mutableStateOf(NotesContentType.NOTES) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val headerColor = remember(subjectName) { getSubjectThemeColor(subjectName) }

    // Handle Android system back gesture to return to subjects list
    BackHandler {
        onNavigateBack()
    }

    // Curated topics according to Ugandan Secondary Curriculum (NCDC / UNEB)
    val notesMap = remember {
        mapOf(
            "Math" to listOf(
                TopicItem("1", "Algebra", "18/10/26"),
                TopicItem("2", "Sets", "15/10/26"),
                TopicItem("3", "Construction", "12/10/26"),
                TopicItem("4", "Matrices & Transformations", "08/10/26"),
                TopicItem("5", "Trigonometry", "03/10/26"),
                TopicItem("6", "Vectors & Bearings", "28/09/26"),
                TopicItem("7", "Quadratic Equations", "22/09/26"),
                TopicItem("8", "Commercial Arithmetic", "17/09/26"),
                TopicItem("9", "Statistics & Probability", "10/09/26"),
                TopicItem("10", "Coordinate Geometry", "04/09/26")
            ),
            "Physics" to listOf(
                TopicItem("p1", "Mechanics & Motion", "17/10/26"),
                TopicItem("p2", "Heat & Thermodynamics", "14/10/26"),
                TopicItem("p3", "Light & Optics", "10/10/26"),
                TopicItem("p4", "Current Electricity", "05/10/26"),
                TopicItem("p5", "Magnetism & Electromagnetism", "29/09/26"),
                TopicItem("p6", "Waves & Sound", "21/09/26"),
                TopicItem("p7", "Modern Physics & Radioactivity", "14/09/26")
            ),
            "Biology" to listOf(
                TopicItem("b1", "Cell Biology & Microscopy", "18/10/26"),
                TopicItem("b2", "Nutrition in Plants & Animals", "16/10/26"),
                TopicItem("b3", "Transport in Plants & Animals", "11/10/26"),
                TopicItem("b4", "Respiration & Gas Exchange", "07/10/26"),
                TopicItem("b5", "Excretion & Homeostasis", "30/09/26"),
                TopicItem("b6", "Reproduction in Organisms", "24/09/26"),
                TopicItem("b7", "Genetics & Evolution", "18/09/26"),
                TopicItem("b8", "Ecology & Ecosystems", "09/09/26")
            ),
            "Chemistry" to listOf(
                TopicItem("c1", "Atomic Structure & Periodic Table", "17/10/26"),
                TopicItem("c2", "Chemical Bonding & Structure", "13/10/26"),
                TopicItem("c3", "Acids, Bases & Salts", "09/10/26"),
                TopicItem("c4", "Mole Concept & Stoichiometry", "04/10/26"),
                TopicItem("c5", "Carbon and its Compounds", "27/09/26"),
                TopicItem("c6", "Electrochemistry", "20/09/26"),
                TopicItem("c7", "Extraction of Metals", "12/09/26")
            ),
            "Geography" to listOf(
                TopicItem("g1", "Map Reading & Photographic Interpretation", "18/10/26"),
                TopicItem("g2", "Physical Geography of East Africa", "14/10/26"),
                TopicItem("g3", "Climate, Vegetation & Soils", "10/10/26"),
                TopicItem("g4", "Population & Settlement Patterns", "06/10/26"),
                TopicItem("g5", "Agriculture & Mining in East Africa", "28/09/26"),
                TopicItem("g6", "World Geography & Development Studies", "20/09/26")
            ),
            "History" to listOf(
                TopicItem("h1", "Pre-colonial African Societies", "16/10/26"),
                TopicItem("h2", "European Scramble & Partition of Africa", "12/10/26"),
                TopicItem("h3", "Colonial Rule & African Responses", "08/10/26"),
                TopicItem("h4", "Nationalism & Struggle for Independence", "02/10/26"),
                TopicItem("h5", "Post-Independence Uganda & Political Systems", "25/09/26"),
                TopicItem("h6", "East African Integration & EAC", "17/09/26")
            ),
            "English" to listOf(
                TopicItem("e1", "Comprehension & Summary Writing", "18/10/26"),
                TopicItem("e2", "Grammar: Tenses & Active/Passive Voice", "15/10/26"),
                TopicItem("e3", "Formal & Informal Letter Writing", "11/10/26"),
                TopicItem("e4", "Report Writing & Minutes", "05/10/26"),
                TopicItem("e5", "Composition & Creative Writing", "29/09/26"),
                TopicItem("e6", "Speech Writing & Public Speaking", "21/09/26")
            ),
            "Agriculture" to listOf(
                TopicItem("ag1", "Soil Science & Fertility Management", "17/10/26"),
                TopicItem("ag2", "Crop Production & Pest Control", "13/10/26"),
                TopicItem("ag3", "Animal Husbandry & Nutrition", "09/10/26"),
                TopicItem("ag4", "Agricultural Economics & Marketing", "03/10/26"),
                TopicItem("ag5", "Farm Tools, Machinery & Structures", "26/09/26")
            ),
            "ICT" to listOf(
                TopicItem("ict1", "Computer Systems & Hardware", "18/10/26"),
                TopicItem("ict2", "Word Processing & Documentation", "14/10/26"),
                TopicItem("ict3", "Electronic Spreadsheets (Excel)", "10/10/26"),
                TopicItem("ict4", "Database Management Systems (Access)", "04/10/26"),
                TopicItem("ict5", "Web Design & HTML Basics", "28/09/26"),
                TopicItem("ict6", "Internet, Networking & Cyber Security", "20/09/26")
            ),
            "Entrepreneurship" to listOf(
                TopicItem("ent1", "Business Planning & Feasibility", "17/10/26"),
                TopicItem("ent2", "Financial Record Keeping & Bookkeeping", "12/10/26"),
                TopicItem("ent3", "Marketing Strategies & Customer Care", "07/10/26"),
                TopicItem("ent4", "Business Ethics & Social Responsibility", "01/10/26"),
                TopicItem("ent5", "Taxation & Legal Forms of Business in Uganda", "24/09/26")
            ),
            "C.R.E" to listOf(
                TopicItem("cre1", "Creation & The Nature of God", "18/10/26"),
                TopicItem("cre2", "Covenants & The Ten Commandments", "14/10/26"),
                TopicItem("cre3", "The Life & Teachings of Jesus Christ", "09/10/26"),
                TopicItem("cre4", "Christian Living & Moral Ethics in Society", "03/10/26")
            ),
            "I.R.E" to listOf(
                TopicItem("ire1", "Tawheed & The Articles of Faith", "18/10/26"),
                TopicItem("ire2", "The Holy Quran & Hadith Studies", "14/10/26"),
                TopicItem("ire3", "Pillars of Islam & Practical Worship", "09/10/26"),
                TopicItem("ire4", "Islamic History & The Caliphate", "03/10/26")
            )
        )
    }

    val assignmentsMap = remember {
        mapOf(
            "Math" to listOf(
                TopicItem("a1", "Algebraic Expressions & Factorization Sheet", "20/10/26"),
                TopicItem("a2", "Venn Diagrams & Set Operations Practice", "16/10/26"),
                TopicItem("a3", "Geometrical Construction Assignment 3", "11/10/26"),
                TopicItem("a4", "Matrix Determinants & Inverse Test Prep", "06/10/26"),
                TopicItem("a5", "Trigonometric Ratios Homework", "01/10/26")
            ),
            "Physics" to listOf(
                TopicItem("pa1", "Forces & Newton's Laws Problem Set", "19/10/26"),
                TopicItem("pa2", "Optics Ray Diagrams & Focal Length Lab", "15/10/26"),
                TopicItem("pa3", "Ohm's Law & Circuit Calculations", "09/10/26")
            ),
            "Biology" to listOf(
                TopicItem("ba1", "Plant vs Animal Cell Drawing Lab Work", "19/10/26"),
                TopicItem("ba2", "Enzyme Activity Experiment Report", "13/10/26"),
                TopicItem("ba3", "Genetics Monohybrid Cross Assignment", "08/10/26")
            ),
            "Chemistry" to listOf(
                TopicItem("ca1", "Volumetric Analysis Titration Calculations", "18/10/26"),
                TopicItem("ca2", "Qualitative Analysis Salt Identification", "12/10/26"),
                TopicItem("ca3", "Balancing Redox Equations Practice", "06/10/26")
            )
        )
    }

    val rawTopics = when (selectedContentType) {
        NotesContentType.NOTES -> notesMap[subjectName] ?: listOf(
            TopicItem("def1", "Introduction to $subjectName", "18/10/26"),
            TopicItem("def2", "Core Concepts & Fundamentals", "14/10/26"),
            TopicItem("def3", "Practical Applications", "10/10/26"),
            TopicItem("def4", "Advanced Topics & Examples", "05/10/26"),
            TopicItem("def5", "Exam Review & Summary Notes", "28/09/26")
        )
        NotesContentType.ASSIGNMENTS -> assignmentsMap[subjectName] ?: listOf(
            TopicItem("defa1", "$subjectName Practice Assignment 1", "20/10/26"),
            TopicItem("defa2", "$subjectName Mid-term Coursework", "15/10/26"),
            TopicItem("defa3", "$subjectName Practical Assessment", "10/10/26")
        )
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
                        1 -> onNavigateBack()
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
            .clickable(onClick = onClick)
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
