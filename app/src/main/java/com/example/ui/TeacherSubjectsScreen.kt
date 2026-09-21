package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Data representation of a subject taught by the teacher.
 */
data class TeacherSubjectItem(
    val id: String,
    val name: String,
    val classes: String,
    val noteCount: Int,
    val assignmentCount: Int,
    val icon: ImageVector,
    val color: Color
)

/**
 * Data representation of an upload (Note or Assignment) within a subject.
 */
data class SubjectUploadItem(
    val id: String,
    val title: String,
    val subject: String,
    val targetClass: String,
    val type: String, // "Notes" or "Assignment"
    val dateUploaded: String,
    val dueDate: String? = null,
    val isOwnUpload: Boolean = true,
    val uploaderName: String = "Mr. Okello",
    val fileName: String = "document.pdf"
)

/**
 * Default subjects taught by Mr. Okello:
 * - Geography: S.4, S.5
 * - Math: S.3, S.4
 */
val DefaultTeacherSubjects = listOf(
    TeacherSubjectItem(
        id = "geography",
        name = "Geography",
        classes = "S.4, S.5",
        noteCount = 5,
        assignmentCount = 2,
        icon = Icons.Outlined.Public,
        color = CoralTile
    ),
    TeacherSubjectItem(
        id = "math",
        name = "Math",
        classes = "S.3, S.4",
        noteCount = 4,
        assignmentCount = 3,
        icon = Icons.Outlined.Architecture,
        color = TealTile
    )
)

val DefaultGeographyUploads = listOf(
    SubjectUploadItem(
        id = "geo-1",
        title = "East African Climate & Vegetation",
        subject = "Geography",
        targetClass = "S.4",
        type = "Notes",
        dateUploaded = "Yesterday"
    ),
    SubjectUploadItem(
        id = "geo-2",
        title = "Weather Station Instruments Assignment",
        subject = "Geography",
        targetClass = "S.4",
        type = "Assignment",
        dateUploaded = "Sep 18",
        dueDate = "Due: Sep 25"
    ),
    SubjectUploadItem(
        id = "geo-3",
        title = "Map Reading & Contour Analysis",
        subject = "Geography",
        targetClass = "S.5",
        type = "Notes",
        dateUploaded = "Sep 17"
    ),
    SubjectUploadItem(
        id = "geo-4",
        title = "Soil Formation & Agriculture Systems",
        subject = "Geography",
        targetClass = "S.4",
        type = "Notes",
        dateUploaded = "Sep 14"
    ),
    SubjectUploadItem(
        id = "geo-5",
        title = "Plate Tectonics & Rift Valley Formations",
        subject = "Geography",
        targetClass = "S.5",
        type = "Notes",
        dateUploaded = "Sep 10"
    )
)

val DefaultMathUploads = listOf(
    SubjectUploadItem(
        id = "math-1",
        title = "Quadratic Equations & Graphs",
        subject = "Math",
        targetClass = "S.3",
        type = "Notes",
        dateUploaded = "2 days ago"
    ),
    SubjectUploadItem(
        id = "math-2",
        title = "Trigonometry & Bearings Problem Set",
        subject = "Math",
        targetClass = "S.4",
        type = "Assignment",
        dateUploaded = "Sep 16",
        dueDate = "Due: Sep 24"
    ),
    SubjectUploadItem(
        id = "math-3",
        title = "Matrices & Linear Transformations",
        subject = "Math",
        targetClass = "S.4",
        type = "Notes",
        dateUploaded = "Sep 12"
    ),
    SubjectUploadItem(
        id = "math-4",
        title = "Simultaneous Equations Exercise 4B",
        subject = "Math",
        targetClass = "S.3",
        type = "Assignment",
        dateUploaded = "Sep 08",
        dueDate = "Due: Sep 15"
    )
)

/**
 * Teacher Subjects Screen:
 * Displays cards for the subjects that the teacher teaches (e.g. Geography and Math),
 * styled identically to the subject cards in the student's panel.
 * Tapping a card directs to that subject's configuration page.
 */
@Composable
fun TeacherSubjectsScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Mr. Okello",
    subjects: List<TeacherSubjectItem> = DefaultTeacherSubjects,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {},
    onSubjectClick: (TeacherSubjectItem) -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("teacher_subjects_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 1, // "My Subjects" active
                items = TeacherNavItems,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToMySubjects()
                        2 -> onNavigateToUploads()
                        3 -> onNavigateToTeacherProfile()
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
            // Purple Gradient Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B3F8F), Color(0xFF5F4FA8))
                        )
                    )
                    .testTag("teacher_subjects_header")
            ) {
                // Subtle decorative background circles
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFF6B5CA5).copy(alpha = 0.30f),
                        radius = 120.dp.toPx(),
                        center = Offset(size.width + 25.dp.toPx(), 20.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF6B5CA5).copy(alpha = 0.30f),
                        radius = 70.dp.toPx(),
                        center = Offset(-10.dp.toPx(), size.height - 15.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
                ) {
                    Text(
                        text = "My Subjects",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("teacher_subjects_title")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$teacherName • ${subjects.size} assigned subjects",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        modifier = Modifier.testTag("teacher_subjects_subtitle")
                    )
                }
            }

            // White Body with Subject Cards
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_subjects_grid"),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(subjects, key = { it.id }) { subjectItem ->
                        TeacherSubjectCard(
                            subject = subjectItem,
                            onClick = { onSubjectClick(subjectItem) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Subject card in Teacher's Subjects page.
 * Follows the design of the student panel SubjectCard:
 * - Distinct solid background color (Coral for Geography, Teal for Math)
 * - 18dp rounded corners, frosted border, color-matched ambient elevation
 * - Crisp 44dp white outline icon
 * - Bold white title with class badges
 */
@Composable
fun TeacherSubjectCard(
    subject: TeacherSubjectItem,
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
            .testTag("teacher_subject_card_${subject.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        color = subject.color,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = subject.icon,
                contentDescription = subject.name,
                tint = Color.White,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("teacher_subject_icon_${subject.name.lowercase()}")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subject.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("teacher_subject_name_${subject.name.lowercase()}")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subject.classes,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("teacher_subject_classes_${subject.name.lowercase()}")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${subject.noteCount} notes • ${subject.assignmentCount} tasks",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Subject Configuration Page:
 * Direct destination when a teacher clicks on a subject card.
 * - Displays subject information and assigned classes
 * - Allows uploading notes and assignments
 * - Shows recent uploads for this subject as topic cards
 */
@Composable
fun TeacherSubjectConfigScreen(
    subject: TeacherSubjectItem,
    initialUploads: List<SubjectUploadItem>,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onUploadNotesClick: () -> Unit = {},
    onEditNote: (SubjectUploadItem) -> Unit = {},
    onOpenNote: (String, String) -> Unit = { _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var uploadsList by remember { mutableStateOf(initialUploads) }

    var showUploadNotesDialog by remember { mutableStateOf(false) }
    var showUploadAssignmentDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Notes", "Assignment"
    var noteToDelete by remember { mutableStateOf<SubjectUploadItem?>(null) }

    val filteredUploads = remember(uploadsList, selectedFilter) {
        if (selectedFilter == "All") {
            uploadsList
        } else {
            uploadsList.filter { it.type.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("teacher_subject_config_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Dynamic Subject Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(subject.color)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("subject_config_top_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("config_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subject.name} Configuration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("config_subject_title")
                        )
                        Text(
                            text = "Assigned to: ${subject.classes}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.testTag("config_subject_subtitle")
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Summary Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(subject.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = subject.icon,
                                        contentDescription = null,
                                        tint = subject.color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${subject.name} Portal",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "${uploadsList.count { it.type == "Notes" }} Notes • ${uploadsList.count { it.type == "Assignment" }} Assignments",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, BorderGrey, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = subject.classes,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IndigoPurple
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Upload Notes & Upload Assignment
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Actions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Upload Notes Button Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onUploadNotesClick() }
                                    .testTag("action_upload_notes_card"),
                                shape = RoundedCornerShape(16.dp),
                                color = IndigoPurple
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Upload Notes",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Upload Notes",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Share PDF notes",
                                            fontSize = 11.sp,
                                            color = LightLavender
                                        )
                                    }
                                }
                            }

                            // Upload Assignment Button Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showUploadAssignmentDialog = true }
                                    .testTag("action_upload_assignment_card"),
                                shape = RoundedCornerShape(16.dp),
                                color = subject.color
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Assignment,
                                        contentDescription = "Upload Assignment",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "New Assignment",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Set tasks & due dates",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Uploads Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Uploads",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark,
                                modifier = Modifier.testTag("config_recent_uploads_heading")
                            )

                            // Filter Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("All", "Notes", "Assignment").forEach { filter ->
                                    val isSelected = selectedFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilter = filter },
                                        label = {
                                            Text(
                                                text = filter,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = IndigoPurple,
                                            selectedLabelColor = Color.White,
                                            containerColor = SoftLavenderGrey,
                                            labelColor = TextDark
                                        ),
                                        modifier = Modifier.testTag("filter_chip_${filter.lowercase()}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (filteredUploads.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftLavenderGrey
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No $selectedFilter uploads found.",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(filteredUploads, key = { it.id }) { item ->
                        ConfigUploadTopicCard(
                            uploadItem = item,
                            subjectColor = subject.color,
                            onClick = {
                                onOpenNote(item.title, item.subject)
                            },
                            onEdit = {
                                onEditNote(item)
                            },
                            onDelete = {
                                noteToDelete = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Deletion confirmation dialog for teacher's notes
    if (noteToDelete != null) {
        val item = noteToDelete!!
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = {
                Text(
                    text = "Delete Note",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Remove this note? Students will no longer be able to access it.",
                    fontSize = 14.sp,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        uploadsList = uploadsList.filterNot { it.id == item.id }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Removed \"${item.title}\"")
                        }
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // DIALOG: UPLOAD NOTES
    // ==========================================
    if (showUploadNotesDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var targetClass by remember { mutableStateOf(subject.classes.split(",").firstOrNull()?.trim() ?: "S.4") }
        val availableClasses = subject.classes.split(",").map { it.trim() }

        AlertDialog(
            onDismissRequest = { showUploadNotesDialog = false },
            title = {
                Text(
                    text = "Upload Notes for ${subject.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Share textbook chapters, revision summaries or classroom notes with students.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g. Fieldwork Techniques & Data Collection") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note_title")
                    )

                    Column {
                        Text(
                            text = "Target Class",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableClasses.forEach { cls ->
                                FilterChip(
                                    selected = targetClass == cls,
                                    onClick = { targetClass = cls },
                                    label = { Text(cls) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IndigoPurple,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Attached Document Preview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SoftLavenderGrey
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = IndigoPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "document_attached.pdf (2.4 MB)",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val titleToAdd = noteTitle.ifBlank { "Untitled Note - ${subject.name}" }
                        val newUpload = SubjectUploadItem(
                            id = System.currentTimeMillis().toString(),
                            title = titleToAdd,
                            subject = subject.name,
                            targetClass = targetClass,
                            type = "Notes",
                            dateUploaded = "Just now"
                        )
                        uploadsList = listOf(newUpload) + uploadsList
                        showUploadNotesDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Notes \"$titleToAdd\" uploaded to $targetClass!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    modifier = Modifier.testTag("btn_confirm_upload_notes")
                ) {
                    Text("Publish Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadNotesDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // DIALOG: UPLOAD ASSIGNMENT
    // ==========================================
    if (showUploadAssignmentDialog) {
        var assignmentTitle by remember { mutableStateOf("") }
        var targetClass by remember { mutableStateOf(subject.classes.split(",").firstOrNull()?.trim() ?: "S.4") }
        var dueDate by remember { mutableStateOf("Due in 7 days") }
        val availableClasses = subject.classes.split(",").map { it.trim() }

        AlertDialog(
            onDismissRequest = { showUploadAssignmentDialog = false },
            title = {
                Text(
                    text = "New ${subject.name} Assignment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Assign coursework, homework exercises or term project questions with submission deadlines.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = assignmentTitle,
                        onValueChange = { assignmentTitle = it },
                        label = { Text("Assignment Title") },
                        placeholder = { Text("e.g. Weather Station Instruments Exercise") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = subject.color,
                            focusedLabelColor = subject.color
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_assignment_title")
                    )

                    Column {
                        Text(
                            text = "Target Class",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableClasses.forEach { cls ->
                                FilterChip(
                                    selected = targetClass == cls,
                                    onClick = { targetClass = cls },
                                    label = { Text(cls) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = subject.color,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Submission Due Date") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = subject.color,
                            focusedLabelColor = subject.color
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_assignment_due_date")
                    )

                    // Attached Instructions Preview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SoftLavenderGrey
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = subject.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "assignment_brief.pdf (1.1 MB)",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val titleToAdd = assignmentTitle.ifBlank { "${subject.name} Homework Exercise" }
                        val newUpload = SubjectUploadItem(
                            id = System.currentTimeMillis().toString(),
                            title = titleToAdd,
                            subject = subject.name,
                            targetClass = targetClass,
                            type = "Assignment",
                            dateUploaded = "Just now",
                            dueDate = dueDate
                        )
                        uploadsList = listOf(newUpload) + uploadsList
                        showUploadAssignmentDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Assignment \"$titleToAdd\" assigned to $targetClass!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = subject.color),
                    modifier = Modifier.testTag("btn_confirm_upload_assignment")
                ) {
                    Text("Publish Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadAssignmentDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

/**
 * Topic Card for notes and assignments inside Subject Configuration.
 * Modeled after the student panel RecentTopicCard with subject color styling.
 */
@Composable
fun ConfigUploadTopicCard(
    uploadItem: SubjectUploadItem,
    subjectColor: Color,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x18000000)
            )
            .clickable(onClick = onClick)
            .testTag("upload_topic_card_${uploadItem.title.lowercase().replace(" ", "_").replace("&", "and")}"),
        shape = RoundedCornerShape(12.dp),
        color = subjectColor,
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
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = uploadItem.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = if (uploadItem.type == "Assignment" && uploadItem.dueDate != null) {
                    "${uploadItem.type} • ${uploadItem.targetClass} • ${uploadItem.dueDate}"
                } else {
                    "${uploadItem.type} • ${uploadItem.targetClass}"
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = uploadItem.dateUploaded,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.90f)
                )

                if (uploadItem.isOwnUpload) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_more_${uploadItem.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Note", fontSize = 13.sp, color = TextDark) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit",
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Note", fontSize = 13.sp, color = Color(0xFFE53935)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun TeacherSubjectsScreenPreview() {
    TeacherSubjectsScreen()
}
