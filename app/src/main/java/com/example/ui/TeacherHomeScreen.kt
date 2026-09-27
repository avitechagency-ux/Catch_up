package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.abs

const val DEFAULT_TEACHER_ID_CODE = "K7M4P9"

/**
 * Generates a 6-character Teacher ID code containing ONLY capital letters (A-Z) and numbers (0-9).
 */
fun generateTeacherIdCode(seed: String = ""): String {
    val trimmedSeed = seed.trim()
    if (trimmedSeed.isEmpty() || trimmedSeed.equals("Teacher", ignoreCase = true)) {
        return DEFAULT_TEACHER_ID_CODE
    }
    val letters = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val digits = "23456789"
    val hash = abs(trimmedSeed.uppercase().hashCode())
    val c0 = letters[hash % letters.length]
    val c1 = digits[(hash / 7) % digits.length]
    val c2 = letters[(hash / 13) % letters.length]
    val c3 = digits[(hash / 29) % digits.length]
    val c4 = letters[(hash / 53) % letters.length]
    val c5 = digits[(hash / 97) % digits.length]
    return "$c0$c1$c2$c3$c4$c5"
}

/**
 * Validates that a Teacher ID code is a 6-character code with capital letters (A-Z) and numbers (0-9) only.
 */
fun isValidTeacherIdCode(code: String): Boolean {
    if (code.length != 6) return false
    return code.all { it in 'A'..'Z' || it in '0'..'9' }
}

/**
 * Data model for a student who connected to a teacher using their 6-character ID code.
 */
data class ConnectedStudent(
    val id: String,
    val studentName: String,
    val studentClass: String,
    val subjectName: String,
    val teacherIdCode: String,
    val connectedDate: String
)

fun serializeConnectedStudents(students: List<ConnectedStudent>): String {
    return students.joinToString(";;") { item ->
        listOf(
            item.id.replace("|", " ").replace(";", " "),
            item.studentName.replace("|", " ").replace(";", " "),
            item.studentClass.replace("|", " ").replace(";", " "),
            item.subjectName.replace("|", " ").replace(";", " "),
            item.teacherIdCode.replace("|", " ").replace(";", " ").uppercase(),
            item.connectedDate.replace("|", " ").replace(";", " ")
        ).joinToString("|")
    }
}

fun deserializeConnectedStudents(serialized: String): List<ConnectedStudent> {
    if (serialized.isBlank()) return emptyList()
    return serialized.split(";;").mapNotNull { record ->
        val parts = record.split("|")
        if (parts.size >= 6 && parts[1].isNotBlank() && parts[4].isNotBlank()) {
            ConnectedStudent(
                id = parts[0],
                studentName = parts[1],
                studentClass = parts[2],
                subjectName = parts[3],
                teacherIdCode = parts[4].uppercase(),
                connectedDate = parts[5]
            )
        } else null
    }
}

/**
 * Data model for recently uploaded notes in the Teacher panel.
 */
data class TeacherRecentUpload(
    val id: String,
    val title: String,
    val subject: String,
    val targetClass: String,
    val dateUploaded: String
)

val DefaultTeacherRecentUploads: List<TeacherRecentUpload> = emptyList()

/**
 * Teacher Home Screen for Cachap.
 */
@Composable
fun TeacherHomeScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Teacher",
    teacherIdCode: String = DEFAULT_TEACHER_ID_CODE,
    subjectsTaught: String = "Secondary Curriculum",
    notesUploadedCount: Int = 0,
    classesCount: Int = 0,
    connectedStudents: List<ConnectedStudent> = emptyList(),
    notesSharedThisTermCount: Int = 0,
    mostActiveClass: String = "None yet",
    notesOpenedThisWeekCount: Int = 0,
    recentUploads: List<TeacherRecentUpload> = DefaultTeacherRecentUploads,
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {},
    onUploadNotesClick: () -> Unit = {},
    onRecentNoteClick: (TeacherRecentUpload) -> Unit = {},
    onSeeAllRecentClick: () -> Unit = {},
    onSwitchToStudentView: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val normalizedTeacherId = remember(teacherIdCode, teacherName) {
        val candidate = teacherIdCode.trim().uppercase()
        if (isValidTeacherIdCode(candidate)) candidate else generateTeacherIdCode(teacherName)
    }
    var showStudentsDialog by remember { mutableStateOf(false) }
    val studentsCount = if (connectedStudents.isNotEmpty()) connectedStudents.size else classesCount

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_home_screen"),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 0, // "Home" is active on Teacher Home screen
                items = TeacherNavItems,
                onItemSelected = { index ->
                    when (index) {
                        0 -> { /* Already on Teacher Home */ }
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
                .verticalScroll(scrollState)
        ) {
            // ==========================================
            // 1. APP HEADER (Like Student Portal Header)
            // ==========================================
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
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
                    .testTag("teacher_app_header")
            ) {
                // Upper Section: Platform Name centered, size h1 (32sp) & bold
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cachap",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("platform_name_header")
                    )
                }

                // Generous vertical spacing to lower the welcome text
                Spacer(modifier = Modifier.height(36.dp))

                // Lower Section: Welcome text on the left and Teacher ID on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Good morning 👋",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender,
                            modifier = Modifier.testTag("teacher_greeting_text")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = teacherName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("teacher_name_text")
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Teacher ID badge on the right of the header card with text "ID" and 6-digit code
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(normalizedTeacherId))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Teacher ID $normalizedTeacherId copied")
                                }
                            }
                            .testTag("teacher_id_card"),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightLavender,
                                letterSpacing = 1.sp,
                                modifier = Modifier.testTag("teacher_id_label")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = normalizedTeacherId,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.testTag("teacher_id_code_text")
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. WHITE BODY
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 28.dp)
                    .testTag("teacher_white_body")
            ) {
                // SECTION — Quick Stats (colorful tiles matching Student Home Quick Access treatment)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_quick_stats_row"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tile 1: background #2A9D8F (teal)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .testTag("teacher_stat_card_notes"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF2A9D8F)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = "Uploads",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = notesUploadedCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("stat_notes_uploaded_count")
                                )
                                Text(
                                    text = "Notes uploaded",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.testTag("stat_notes_uploaded_label")
                                )
                            }
                        }
                    }

                    // Tile 2: Students connected via Teacher ID
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showStudentsDialog = true }
                            .testTag("teacher_stat_card_classes"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF4361EE)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Groups,
                                    contentDescription = "Students",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = studentsCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("stat_classes_count")
                                )
                                Text(
                                    text = "Students",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.testTag("stat_classes_label")
                                )
                            }
                        }
                    }
                }

                // 24dp spacing between sections
                Spacer(modifier = Modifier.height(24.dp))

                // SECTION — Upload (primary action for notes and assessments)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            // Navigation to Uploads / Upload Notes flow
                            onUploadNotesClick()
                        }
                        .testTag("upload_notes_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = IndigoPurple
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = "Upload",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("upload_notes_icon")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Upload",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("upload_notes_title")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Share notes or assessments with your class",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender,
                            modifier = Modifier.testTag("upload_notes_subtitle")
                        )
                    }
                }

                // 24dp spacing between sections
                Spacer(modifier = Modifier.height(24.dp))

                // SECTION — Recently Uploaded (Topic Cards like in the student panel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Uploaded",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.testTag("section_title_recently_uploaded")
                    )
                    Text(
                        text = "See All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMuted,
                        modifier = Modifier
                            .clickable(onClick = onSeeAllRecentClick)
                            .padding(vertical = 4.dp)
                            .testTag("recent_uploads_see_all")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (recentUploads.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SoftLavenderGrey
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No notes uploaded yet",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        recentUploads.forEach { note ->
                            TeacherRecentTopicCard(
                                note = note,
                                onClick = { onRecentNoteClick(note) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStudentsDialog) {
        Dialog(onDismissRequest = { showStudentsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("teacher_students_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Connected Students",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Teacher ID: $normalizedTeacherId",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = IndigoPurple
                            )
                        }
                        IconButton(
                            onClick = { showStudentsDialog = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SoftLavenderGrey)
                                .testTag("teacher_students_dialog_close")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (connectedStudents.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftLavenderGrey
                        ) {
                            Text(
                                text = "No students have connected with your ID ($normalizedTeacherId) yet.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            connectedStudents.forEach { student ->
                                TeacherConnectedStudentCard(student = student)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a student who connected to the teacher using the teacher's 6-digit ID.
 */
@Composable
fun TeacherConnectedStudentCard(
    student: ConnectedStudent,
    modifier: Modifier = Modifier
) {
    val subjectColor = getSubjectThemeColor(student.subjectName)
    val initial = student.studentName.trim().firstOrNull()?.uppercase() ?: "S"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("connected_student_card_${student.id}"),
        shape = RoundedCornerShape(14.dp),
        color = SoftLavenderGrey,
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(subjectColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.studentName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("connected_student_name_${student.id}")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${student.subjectName} • ${student.studentClass}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndigoPurple.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "ID: ${student.teacherIdCode}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = student.connectedDate,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Topic card for recently uploaded notes in the Teacher panel.
 * Matches the sleek 1cm height profile, 12dp rounded corners,
 * and distinct subject theme background colors of the student panel.
 */
@Composable
fun TeacherRecentTopicCard(
    note: TeacherRecentUpload,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = getSubjectThemeColor(note.subject)

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
            .testTag("recent_topic_card_${note.title.lowercase().replace(" ", "_").replace("&", "and")}"),
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
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
                Text(
                    text = note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${note.subject} • ${note.targetClass}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = note.dateUploaded,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.90f)
            )
        }
    }
}

/**
 * Placeholder screen for future teacher sub-screens (My Subjects, Uploads, Profile).
 * Keeps bottom bar interactive while sub-screens are being built.
 */
@Composable
fun TeacherPlaceholderScreen(
    title: String,
    subtitle: String,
    selectedIndex: Int,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_placeholder_screen_${title.lowercase().replace(" ", "_")}"),
        containerColor = Color.White,
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = selectedIndex,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B3F8F), Color(0xFF5F4FA8))
                        ),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun TeacherHomeScreenPreview() {
    TeacherHomeScreen()
}
