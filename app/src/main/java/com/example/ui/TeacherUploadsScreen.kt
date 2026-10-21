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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.notifications.NotificationHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Data representation of an uploaded file, note, or assignment in the teacher portal.
 */
data class TeacherUploadItem(
    val id: String,
    val title: String,
    val subject: String = "General",
    val targetClass: String = "S.4",
    val targetStreams: String = "ALL", // "ALL" or "NORTH,SOUTH"
    val term: String = "Term 3",
    val type: String, // "Notes" or "Assignment"
    val dateUploaded: String,
    val fileSize: String = "2.4 MB",
    val fileFormat: String = "DOCX",
    val downloadCount: Int = 18,
    val viewCount: Int = 34,
    val dueDate: String? = null,
    val submissionCount: Int? = null,
    val status: String = "Published",
    val teacherIdCode: String = ""
)

val DefaultTeacherAllUploads: List<TeacherUploadItem> = emptyList()

/**
 * Teacher Uploads Screen showing only uploaded items in topic pills like student subjects page, with no shortcut to upload.
 */
@Composable
fun TeacherUploadsScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Teacher",
    initialUploads: List<TeacherUploadItem> = DefaultTeacherAllUploads,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToUploadNotes: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {},
    onDeleteUpload: (TeacherUploadItem) -> Unit = {},
    onShareUpload: ((TeacherUploadItem, String, String) -> Unit)? = null,
    onOpenUpload: (String, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var uploadsList by remember(initialUploads) { mutableStateOf(initialUploads) }
    var searchQuery by remember { mutableStateOf("") }

    var noteToShare by remember { mutableStateOf<TeacherUploadItem?>(null) }

    val filteredUploads = remember(uploadsList, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            uploadsList
        } else {
            uploadsList.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                item.subject.contains(query, ignoreCase = true) ||
                item.type.contains(query, ignoreCase = true) ||
                item.dateUploaded.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_uploads_screen"),
        containerColor = Color(0xFFF9F8FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 2,
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
            // Header with title and search bar, but NO upload shortcut
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
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp)
                    .testTag("teacher_uploads_header")
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Uploads & Archives",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("teacher_uploads_title")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0x10000000),
                            spotColor = Color(0x15000000)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("search_uploads_bar"),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
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
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by title, subject, type, date...",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_uploads_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = IconGrey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Body: Uploads displayed in pills like topic pills in student subjects page
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("uploads_lazy_list"),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredUploads.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 20.dp)
                                .testTag("uploads_empty_state"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = LightLavender.copy(alpha = 0.5f),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Empty",
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No uploads found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No results matching \"$searchQuery\"" else "Uploaded notes and assessments will appear here.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredUploads, key = { it.id }) { item ->
                        TeacherUploadTopicCard(
                            item = item,
                            onClick = { onOpenUpload(item.title, item.subject) },
                            onShareClick = { noteToShare = item }
                        )
                    }
                }
            }
        }
    }

    // Share Dialog allowing teachers to reuse notes and share with new classes and streams
    val note = noteToShare
    if (note != null) {
        ShareUploadDialog(
            item = note,
            onDismiss = { noteToShare = null },
            onConfirmShare = { targetClass, targetStreams ->
                val updatedList = uploadsList.map {
                    if (it.id == note.id) it.copy(targetClass = targetClass, targetStreams = targetStreams) else it
                }
                uploadsList = updatedList
                noteToShare = null
                onShareUpload?.invoke(note, targetClass, targetStreams)
                coroutineScope.launch {
                    val streamDesc = if (targetStreams.isBlank() || targetStreams.equals("ALL", ignoreCase = true)) {
                        "All Streams"
                    } else {
                        "Stream $targetStreams"
                    }
                    snackbarHostState.showSnackbar("Shared '${note.title}' with $targetClass ($streamDesc) learners!")
                }
            }
        )
    }
}

/**
 * Topic Pill Card for Uploads page with Share action.
 */
@Composable
fun TeacherUploadTopicCard(
    item: TeacherUploadItem,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = getSubjectThemeColor(item.subject)
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
            .bounceClick(onClick = onClick)
            .testTag("teacher_upload_pill_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        color = subjectColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val streamBadge = if (item.targetStreams.isNotBlank() && !item.targetStreams.equals("ALL", ignoreCase = true)) {
                    " • Stream ${item.targetStreams}"
                } else ""
                Text(
                    text = "${item.subject} • ${item.type} • ${item.targetClass}$streamBadge",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.dateUploaded,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Share with learners button
                Surface(
                    onClick = onShareClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.24f),
                    modifier = Modifier.testTag("share_note_button_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share with students",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Share",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to share an existing archived/uploaded note with new learners, classes, and streams.
 */
@Composable
fun ShareUploadDialog(
    item: TeacherUploadItem,
    onDismiss: () -> Unit,
    onConfirmShare: (targetClass: String, targetStreams: String) -> Unit
) {
    var selectedClass by remember { mutableStateOf(item.targetClass.ifBlank { "All Classes" }) }
    val classOptions = listOf("All Classes", "S.1", "S.2", "S.3", "S.4", "S.5", "S.6")

    val initialTargetStreams = item.targetStreams.ifBlank { "ALL" }
    var isAllStreamsSelected by remember {
        mutableStateOf(initialTargetStreams.equals("ALL", ignoreCase = true) || initialTargetStreams.isBlank())
    }

    val standardStreams = listOf(
        "Blue", "Red", "Green", "Yellow", "White",
        "North", "South", "East", "West", "Central",
        "Stream A", "Stream B"
    )

    val selectedStreams = remember {
        mutableStateListOf<String>().apply {
            if (!isAllStreamsSelected) {
                initialTargetStreams.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.equals("ALL", ignoreCase = true) }
                    .forEach { add(it) }
            }
        }
    }

    var showCustomStreamInput by remember { mutableStateOf(false) }
    var customStreamInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = IndigoPurple,
        modifier = Modifier.testTag("share_upload_dialog"),
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFB300).copy(alpha = 0.20f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Share with Students",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Share \"${item.title}\" (${item.subject}) with your learners without re-uploading:",
                    fontSize = 13.sp,
                    color = LightLavender
                )

                // 1. Target Class Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Target Class:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(classOptions.size) { idx ->
                            val opt = classOptions[idx]
                            val isSelected = opt.equals(selectedClass, ignoreCase = true)
                            Surface(
                                onClick = { selectedClass = opt },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.testTag("share_class_opt_$opt")
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF1A1A1A) else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Target Stream Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Stream(s):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isAllStreamsSelected || selectedStreams.isEmpty()) "All Streams" else "${selectedStreams.size} selected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F)
                        )
                    }

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "All Streams" Chip
                        item {
                            val isAllActive = isAllStreamsSelected || selectedStreams.isEmpty()
                            Surface(
                                onClick = {
                                    isAllStreamsSelected = true
                                    selectedStreams.clear()
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAllActive) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f),
                                border = if (isAllActive) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.testTag("share_stream_all")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isAllActive) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF1A1A1A),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "All Streams",
                                        fontSize = 12.sp,
                                        fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isAllActive) Color(0xFF1A1A1A) else Color.White
                                    )
                                }
                            }
                        }

                        // Specific Standard Streams
                        items(standardStreams.size) { idx ->
                            val streamName = standardStreams[idx]
                            val isSelected = !isAllStreamsSelected && streamName in selectedStreams
                            val streamDotColor = resolveStreamColor(streamName)

                            Surface(
                                onClick = {
                                    if (isAllStreamsSelected) {
                                        isAllStreamsSelected = false
                                        selectedStreams.clear()
                                        selectedStreams.add(streamName)
                                    } else {
                                        if (streamName in selectedStreams) {
                                            selectedStreams.remove(streamName)
                                            if (selectedStreams.isEmpty()) {
                                                isAllStreamsSelected = true
                                            }
                                        } else {
                                            selectedStreams.add(streamName)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.testTag("share_stream_$streamName")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) Color(0xFF1A1A1A) else streamDotColor,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = streamName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF1A1A1A) else Color.White
                                    )
                                }
                            }
                        }

                        // "+ Custom" Stream Chip
                        item {
                            Surface(
                                onClick = { showCustomStreamInput = !showCustomStreamInput },
                                shape = RoundedCornerShape(10.dp),
                                color = if (showCustomStreamInput) Color(0xFFFFD54F).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f)),
                                modifier = Modifier.testTag("share_stream_custom_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Custom",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD54F)
                                    )
                                }
                            }
                        }
                    }

                    // Optional Custom Stream Text Field
                    if (showCustomStreamInput) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customStreamInput,
                                onValueChange = { customStreamInput = it },
                                placeholder = {
                                    Text(
                                        text = "e.g. Crane or Albert",
                                        fontSize = 12.sp,
                                        color = LightLavender.copy(alpha = 0.7f)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB300),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.10f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                                ),
                                textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_custom_stream")
                            )

                            Button(
                                onClick = {
                                    val trimmed = customStreamInput.trim()
                                    if (trimmed.isNotBlank()) {
                                        isAllStreamsSelected = false
                                        if (trimmed !in selectedStreams) {
                                            selectedStreams.add(trimmed)
                                        }
                                        customStreamInput = ""
                                        showCustomStreamInput = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Add",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                        }
                    }
                }

                // 3. Target Distribution Summary Card
                val summaryStreams = if (isAllStreamsSelected || selectedStreams.isEmpty()) {
                    "All Streams"
                } else {
                    selectedStreams.joinToString(", ")
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Sharing Target:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightLavender
                        )
                        Text(
                            text = "$selectedClass • $summaryStreams",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalStreams = if (isAllStreamsSelected || selectedStreams.isEmpty()) {
                        "ALL"
                    } else {
                        selectedStreams.joinToString(",")
                    }
                    onConfirmShare(selectedClass, finalStreams)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_share_note_button")
            ) {
                Text(
                    text = "Share Note",
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TeacherUploadsScreenPreview() {
    TeacherUploadsScreen()
}
