package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NoteCountGreen
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.PeachOrangeLight
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Data representation of an uploaded file, note, or assignment in the teacher portal.
 */
data class TeacherUploadItem(
    val id: String,
    val title: String,
    val subject: String,
    val targetClass: String,
    val term: String, // "Term 1", "Term 2", "Term 3"
    val type: String, // "Notes" or "Assignment"
    val dateUploaded: String,
    val fileSize: String = "2.4 MB",
    val fileFormat: String = "PDF",
    val downloadCount: Int = 18,
    val viewCount: Int = 34,
    val dueDate: String? = null,
    val submissionCount: Int? = null,
    val status: String = "Published" // "Published", "Active Assignment", "Completed"
)

/**
 * Collection of all uploaded notes and published files across every term.
 */
val DefaultTeacherAllUploads: List<TeacherUploadItem> = listOf(
    // ----------------- Term 3 (Current Academic Term) -----------------
    TeacherUploadItem(
        id = "t3-1",
        title = "East African Climate & Vegetation",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 3",
        type = "Notes",
        dateUploaded = "Sep 19, 2026",
        fileSize = "3.2 MB",
        downloadCount = 28,
        viewCount = 45,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t3-2",
        title = "Weather Station Instruments Assignment",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 3",
        type = "Assignment",
        dateUploaded = "Sep 18, 2026",
        fileSize = "1.8 MB",
        downloadCount = 32,
        viewCount = 40,
        dueDate = "Due: Sep 25, 2026",
        submissionCount = 32,
        status = "Active Assignment"
    ),
    TeacherUploadItem(
        id = "t3-3",
        title = "Map Reading & Contour Analysis",
        subject = "Geography",
        targetClass = "S.5",
        term = "Term 3",
        type = "Notes",
        dateUploaded = "Sep 17, 2026",
        fileSize = "4.1 MB",
        downloadCount = 24,
        viewCount = 38,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t3-4",
        title = "Quadratic Equations & Graphs",
        subject = "Math",
        targetClass = "S.3",
        term = "Term 3",
        type = "Notes",
        dateUploaded = "Sep 17, 2026",
        fileSize = "2.6 MB",
        downloadCount = 30,
        viewCount = 52,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t3-5",
        title = "Trigonometry & Bearings Problem Set",
        subject = "Math",
        targetClass = "S.4",
        term = "Term 3",
        type = "Assignment",
        dateUploaded = "Sep 16, 2026",
        fileSize = "1.4 MB",
        downloadCount = 28,
        viewCount = 36,
        dueDate = "Due: Sep 24, 2026",
        submissionCount = 28,
        status = "Active Assignment"
    ),
    TeacherUploadItem(
        id = "t3-6",
        title = "Soil Formation & Agriculture Systems",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 3",
        type = "Notes",
        dateUploaded = "Sep 14, 2026",
        fileSize = "3.8 MB",
        downloadCount = 19,
        viewCount = 40,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t3-7",
        title = "Matrices & Linear Transformations",
        subject = "Math",
        targetClass = "S.4",
        term = "Term 3",
        type = "Notes",
        dateUploaded = "Sep 12, 2026",
        fileSize = "2.9 MB",
        downloadCount = 22,
        viewCount = 35,
        status = "Published"
    ),

    // ----------------- Term 2 -----------------
    TeacherUploadItem(
        id = "t2-1",
        title = "Rift Valley & Volcanicity Comprehensive Guide",
        subject = "Geography",
        targetClass = "S.5",
        term = "Term 2",
        type = "Notes",
        dateUploaded = "Jul 28, 2026",
        fileSize = "5.2 MB",
        downloadCount = 48,
        viewCount = 64,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t2-2",
        title = "Mining & Industrialization in East Africa",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 2",
        type = "Notes",
        dateUploaded = "Jul 15, 2026",
        fileSize = "3.5 MB",
        downloadCount = 39,
        viewCount = 58,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t2-3",
        title = "Population Distribution & Settlement Project",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 2",
        type = "Assignment",
        dateUploaded = "Jun 30, 2026",
        fileSize = "1.9 MB",
        downloadCount = 41,
        viewCount = 46,
        dueDate = "Closed: Jul 07, 2026",
        submissionCount = 41,
        status = "Completed"
    ),
    TeacherUploadItem(
        id = "t2-4",
        title = "Calculus Fundamentals & Differentiation",
        subject = "Math",
        targetClass = "S.4",
        term = "Term 2",
        type = "Notes",
        dateUploaded = "Jul 22, 2026",
        fileSize = "4.0 MB",
        downloadCount = 55,
        viewCount = 70,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t2-5",
        title = "Probability & Statistics Mid-Term Assignment",
        subject = "Math",
        targetClass = "S.3",
        term = "Term 2",
        type = "Assignment",
        dateUploaded = "Jun 18, 2026",
        fileSize = "2.1 MB",
        downloadCount = 38,
        viewCount = 42,
        dueDate = "Closed: Jun 25, 2026",
        submissionCount = 38,
        status = "Completed"
    ),
    TeacherUploadItem(
        id = "t2-6",
        title = "Coordinate Geometry & Circle Theorems",
        subject = "Math",
        targetClass = "S.4",
        term = "Term 2",
        type = "Notes",
        dateUploaded = "Jun 05, 2026",
        fileSize = "3.4 MB",
        downloadCount = 45,
        viewCount = 62,
        status = "Published"
    ),

    // ----------------- Term 1 -----------------
    TeacherUploadItem(
        id = "t1-1",
        title = "Introduction to Physical Geography & Earth Structure",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 1",
        type = "Notes",
        dateUploaded = "Apr 10, 2026",
        fileSize = "4.5 MB",
        downloadCount = 72,
        viewCount = 88,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t1-2",
        title = "Weathering and Mass Wasting Fieldwork Notes",
        subject = "Geography",
        targetClass = "S.5",
        term = "Term 1",
        type = "Notes",
        dateUploaded = "Mar 24, 2026",
        fileSize = "3.9 MB",
        downloadCount = 60,
        viewCount = 75,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t1-3",
        title = "Topographic Mapping Basics Assignment",
        subject = "Geography",
        targetClass = "S.4",
        term = "Term 1",
        type = "Assignment",
        dateUploaded = "Mar 08, 2026",
        fileSize = "2.0 MB",
        downloadCount = 44,
        viewCount = 48,
        dueDate = "Closed: Mar 15, 2026",
        submissionCount = 44,
        status = "Completed"
    ),
    TeacherUploadItem(
        id = "t1-4",
        title = "Algebraic Expressions & Factorization",
        subject = "Math",
        targetClass = "S.3",
        term = "Term 1",
        type = "Notes",
        dateUploaded = "Apr 02, 2026",
        fileSize = "2.8 MB",
        downloadCount = 80,
        viewCount = 94,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t1-5",
        title = "Indices, Surds & Logarithms",
        subject = "Math",
        targetClass = "S.3",
        term = "Term 1",
        type = "Notes",
        dateUploaded = "Mar 15, 2026",
        fileSize = "3.1 MB",
        downloadCount = 71,
        viewCount = 85,
        status = "Published"
    ),
    TeacherUploadItem(
        id = "t1-6",
        title = "Commercial Arithmetic & Taxation Exercise",
        subject = "Math",
        targetClass = "S.4",
        term = "Term 1",
        type = "Assignment",
        dateUploaded = "Feb 20, 2026",
        fileSize = "1.7 MB",
        downloadCount = 42,
        viewCount = 45,
        dueDate = "Closed: Feb 27, 2026",
        submissionCount = 42,
        status = "Completed"
    )
)

/**
 * Teacher Uploads Screen:
 * Displays a complete collection of all published notes, assignments, and files
 * across every academic term with filtering, term switching, search, and uploading capabilities.
 */
@Composable
fun TeacherUploadsScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Mr. Okello",
    initialUploads: List<TeacherUploadItem> = DefaultTeacherAllUploads,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {},
    onOpenUpload: (String, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var uploadsList by remember { mutableStateOf(initialUploads) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTerm by remember { mutableStateOf("All") } // "All", "Term 3", "Term 2", "Term 1"
    var selectedTypeFilter by remember { mutableStateOf("All") } // "All", "Notes", "Assignment"
    var selectedSubjectFilter by remember { mutableStateOf("All") } // "All", "Geography", "Math"
    var showUploadDialog by remember { mutableStateOf(false) }

    // Computed filtered items
    val filteredUploads = remember(uploadsList, searchQuery, selectedTerm, selectedTypeFilter, selectedSubjectFilter) {
        uploadsList.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subject.contains(searchQuery, ignoreCase = true) ||
                item.targetClass.contains(searchQuery, ignoreCase = true)

            val matchesTerm = if (selectedTerm == "All") true else item.term.equals(selectedTerm, ignoreCase = true)
            val matchesType = if (selectedTypeFilter == "All") true else item.type.equals(selectedTypeFilter, ignoreCase = true)
            val matchesSubject = if (selectedSubjectFilter == "All") true else item.subject.equals(selectedSubjectFilter, ignoreCase = true)

            matchesSearch && matchesTerm && matchesType && matchesSubject
        }
    }

    // Stats calculations
    val totalCount = uploadsList.size
    val term3Count = uploadsList.count { it.term == "Term 3" }
    val term2Count = uploadsList.count { it.term == "Term 2" }
    val term1Count = uploadsList.count { it.term == "Term 1" }
    val notesCount = filteredUploads.count { it.type == "Notes" }
    val assignmentsCount = filteredUploads.count { it.type == "Assignment" }
    val totalDownloads = filteredUploads.sumOf { it.downloadCount }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("teacher_uploads_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = IndigoPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = "Upload new file"
                    )
                },
                text = {
                    Text(
                        text = "Upload File",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("fab_upload_file")
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 2, // "Uploads" is active (index 2)
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
            // Header Banner
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
                    .testTag("teacher_uploads_header")
            ) {
                // Background decorative circles
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFF6B5CA5).copy(alpha = 0.25f),
                        radius = 120.dp.toPx(),
                        center = Offset(size.width + 25.dp.toPx(), 20.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF6B5CA5).copy(alpha = 0.25f),
                        radius = 70.dp.toPx(),
                        center = Offset(-10.dp.toPx(), size.height - 15.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Uploaded Notes & Files",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("teacher_uploads_title")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$teacherName • Collection across every term",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = LightLavender,
                                modifier = Modifier.testTag("teacher_uploads_subtitle")
                            )
                        }

                        // Fast Add Icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { showUploadDialog = true }
                                .testTag("header_add_upload_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add upload",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_uploads_input"),
                        placeholder = {
                            Text(
                                text = "Search notes, assignments, topics...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.16f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }
            }

            // Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("uploads_lazy_list"),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Term Navigation Pills
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "ACADEMIC TERMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TermPill(
                                label = "All Terms",
                                count = totalCount,
                                isSelected = selectedTerm == "All",
                                onClick = { selectedTerm = "All" },
                                testTag = "term_pill_all"
                            )
                            TermPill(
                                label = "Term 3 (Current)",
                                count = term3Count,
                                isSelected = selectedTerm == "Term 3",
                                onClick = { selectedTerm = "Term 3" },
                                testTag = "term_pill_term3"
                            )
                            TermPill(
                                label = "Term 2",
                                count = term2Count,
                                isSelected = selectedTerm == "Term 2",
                                onClick = { selectedTerm = "Term 2" },
                                testTag = "term_pill_term2"
                            )
                            TermPill(
                                label = "Term 1",
                                count = term1Count,
                                isSelected = selectedTerm == "Term 1",
                                onClick = { selectedTerm = "Term 1" },
                                testTag = "term_pill_term1"
                            )
                        }
                    }
                }

                // Overview Metrics Cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UploadMetricCard(
                            title = "Published",
                            value = "${filteredUploads.size}",
                            subtitle = if (selectedTerm == "All") "Total files" else selectedTerm,
                            icon = Icons.Outlined.CloudUpload,
                            iconTint = IndigoPurple,
                            backgroundColor = SoftLavenderGrey,
                            modifier = Modifier.weight(1f)
                        )
                        UploadMetricCard(
                            title = "Notes",
                            value = "$notesCount",
                            subtitle = "Study guides",
                            icon = Icons.Outlined.Description,
                            iconTint = TealTile,
                            backgroundColor = Color(0xFFE8F7F5),
                            modifier = Modifier.weight(1f)
                        )
                        UploadMetricCard(
                            title = "Assignments",
                            value = "$assignmentsCount",
                            subtitle = "Exercises",
                            icon = Icons.Outlined.Assignment,
                            iconTint = CoralTile,
                            backgroundColor = Color(0xFFFFF0EC),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filter Chips (Type & Subject)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )

                            // Type filters
                            FilterChip(
                                selected = selectedTypeFilter == "All",
                                onClick = { selectedTypeFilter = "All" },
                                label = { Text("All Types", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedTypeFilter == "Notes",
                                onClick = { selectedTypeFilter = "Notes" },
                                label = { Text("Notes Only", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedTypeFilter == "Assignment",
                                onClick = { selectedTypeFilter = "Assignment" },
                                label = { Text("Assignments Only", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPurple,
                                    selectedLabelColor = Color.White
                                )
                            )

                            // Subject filters
                            FilterChip(
                                selected = selectedSubjectFilter == "Geography",
                                onClick = {
                                    selectedSubjectFilter = if (selectedSubjectFilter == "Geography") "All" else "Geography"
                                },
                                label = { Text("Geography", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralTile,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedSubjectFilter == "Math",
                                onClick = {
                                    selectedSubjectFilter = if (selectedSubjectFilter == "Math") "All" else "Math"
                                },
                                label = { Text("Math", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealTile,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Section Header with count
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTerm == "All") "All Published Items (${filteredUploads.size})" else "$selectedTerm Uploads (${filteredUploads.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Text(
                            text = "$totalDownloads total downloads",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }

                // Empty state
                if (filteredUploads.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp)
                                .testTag("uploads_empty_state"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(SoftLavenderGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = null,
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No uploaded files found",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Try adjusting your term or type filters, or publish a new file for this term.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedTerm = "All"
                                    selectedTypeFilter = "All"
                                    selectedSubjectFilter = "All"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear Filters", fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    // Upload items list
                    items(filteredUploads, key = { it.id }) { item ->
                        TeacherUploadCard(
                            item = item,
                            onOpen = { onOpenUpload(item.title, item.subject) },
                            onDelete = {
                                uploadsList = uploadsList.filterNot { it.id == item.id }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Removed \"${item.title}\"")
                                }
                            },
                            onShare = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Shared \"${item.title}\" with ${item.targetClass}")
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // Upload New File Modal Dialog
    if (showUploadDialog) {
        TeacherPublishFileDialog(
            onDismiss = { showUploadDialog = false },
            onPublish = { newUpload ->
                uploadsList = listOf(newUpload) + uploadsList
                showUploadDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Successfully published \"${newUpload.title}\" to ${newUpload.targetClass}!")
                }
            }
        )
    }
}

/**
 * Academic Term Pill Selector.
 */
@Composable
private fun TermPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) IndigoPurple else SoftLavenderGrey)
            .border(
                width = 1.dp,
                color = if (isSelected) IndigoPurple else BorderGrey,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextDark
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.White)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextMuted
                )
            }
        }
    }
}

/**
 * Quick summary metric card.
 */
@Composable
private fun UploadMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark.copy(alpha = 0.8f)
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Individual Card representing an uploaded note or published assignment.
 */
@Composable
private fun TeacherUploadCard(
    item: TeacherUploadItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val subjectColor = when (item.subject.lowercase()) {
        "geography" -> CoralTile
        "math" -> TealTile
        else -> IndigoPurple
    }

    val isAssignment = item.type.equals("Assignment", ignoreCase = true)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onOpen() }
            .testTag("upload_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Badge (Notes or Assignment)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isAssignment) PeachOrangeLight else SoftLavenderGrey)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.type.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAssignment) PeachOrange else IndigoPurple
                        )
                    }

                    // Subject Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(subjectColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.subject,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor
                        )
                    }

                    // Class Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF0F0F0))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.targetClass,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }

                    // Term Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoftLavenderGrey)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.term,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }

                // Overflow Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Options",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open / View PDF", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share with Class", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Upload", fontSize = 13.sp, color = Color(0xFFD32F2F)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // File Details Row (PDF format, size, uploaded date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${item.fileFormat} • ${item.fileSize}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.dateUploaded,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Assignment Due Date Bar if present
            if (item.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.dueDate.contains("Closed")) Color(0xFFF5F5F5) else Color(0xFFFFF4EC))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (item.dueDate.contains("Closed")) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = if (item.dueDate.contains("Closed")) TextMuted else PeachOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = item.dueDate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.dueDate.contains("Closed")) TextMuted else TextDark
                        )
                    }

                    if (item.submissionCount != null) {
                        Text(
                            text = "${item.submissionCount} submissions",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (item.dueDate.contains("Closed")) TextMuted else PeachOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row: Views / Downloads stats and Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${item.viewCount}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            tint = NoteCountGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${item.downloadCount} dl",
                            fontSize = 12.sp,
                            color = NoteCountGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftLavenderGrey,
                        contentColor = IndigoPurple
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Open PDF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Modal Dialog for Publishing/Uploading a new note or assignment.
 */
@Composable
private fun TeacherPublishFileDialog(
    onDismiss: () -> Unit,
    onPublish: (TeacherUploadItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Notes") }
    var selectedSubject by remember { mutableStateOf("Geography") }
    var selectedClass by remember { mutableStateOf("S.4") }
    var selectedTerm by remember { mutableStateOf("Term 3") }
    var dueDate by remember { mutableStateOf("Sep 30, 2026") }
    var attachedFileName by remember { mutableStateOf("Geography_Form4_Notes.pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Publish Notes or Assignment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector (Notes vs Assignment)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "Notes",
                        onClick = { selectedType = "Notes" },
                        label = { Text("Study Notes", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedType == "Assignment",
                        onClick = { selectedType = "Assignment" },
                        label = { Text("Assignment", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralTile,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Topic") },
                    placeholder = { Text("e.g. Plate Tectonics & Earthquakes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Subject & Class row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Subject", fontSize = 11.sp, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = selectedSubject == "Geography",
                                onClick = { selectedSubject = "Geography" },
                                label = { Text("Geo", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedSubject == "Math",
                                onClick = { selectedSubject = "Math" },
                                label = { Text("Math", fontSize = 11.sp) }
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Class", fontSize = 11.sp, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = selectedClass == "S.4",
                                onClick = { selectedClass = "S.4" },
                                label = { Text("S.4", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedClass == "S.5",
                                onClick = { selectedClass = "S.5" },
                                label = { Text("S.5", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedClass == "S.3",
                                onClick = { selectedClass = "S.3" },
                                label = { Text("S.3", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Term selector
                Column {
                    Text("Academic Term", fontSize = 11.sp, color = TextMuted)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        listOf("Term 3", "Term 2", "Term 1").forEach { term ->
                            FilterChip(
                                selected = selectedTerm == term,
                                onClick = { selectedTerm = term },
                                label = { Text(term, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // If Assignment: Due date
                if (selectedType == "Assignment") {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        placeholder = { Text("Due: Sep 30, 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Simulated File Picker Attachment
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SoftLavenderGrey,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = attachedFileName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "PDF document • 2.4 MB",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                attachedFileName = "Published_${selectedSubject}_${selectedClass}_Doc.pdf"
                            }
                        ) {
                            Text("Change", fontSize = 11.sp, color = IndigoPurple)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newUpload = TeacherUploadItem(
                            id = "upload-${System.currentTimeMillis()}",
                            title = title.trim(),
                            subject = selectedSubject,
                            targetClass = selectedClass,
                            term = selectedTerm,
                            type = selectedType,
                            dateUploaded = "Just now",
                            fileSize = "2.4 MB",
                            downloadCount = 0,
                            viewCount = 1,
                            dueDate = if (selectedType == "Assignment") "Due: $dueDate" else null,
                            submissionCount = if (selectedType == "Assignment") 0 else null,
                            status = if (selectedType == "Assignment") "Active Assignment" else "Published"
                        )
                        onPublish(newUpload)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publish to Students")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
