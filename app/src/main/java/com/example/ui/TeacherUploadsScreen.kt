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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
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
    onOpenUpload: (String, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var uploadsList by remember(initialUploads) { mutableStateOf(initialUploads) }
    var searchQuery by remember { mutableStateOf("") }

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
                        text = "Uploads",
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
                            onClick = { onOpenUpload(item.title, item.subject) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Topic Pill Card for Uploads page, styled identically to topic cards in the student's subjects page.
 */
@Composable
fun TeacherUploadTopicCard(
    item: TeacherUploadItem,
    onClick: () -> Unit,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.subject} • ${item.type} • ${item.targetClass}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = item.dateUploaded,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherUploadsScreenPreview() {
    TeacherUploadsScreen()
}
