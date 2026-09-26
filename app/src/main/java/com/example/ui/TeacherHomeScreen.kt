package com.example.ui

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
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
 * Teacher Home Screen for Catch Up.
 */
@Composable
fun TeacherHomeScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Teacher",
    subjectsTaught: String = "Secondary Curriculum",
    notesUploadedCount: Int = 0,
    classesCount: Int = 0,
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
                        text = "Catch Up",
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

                // Lower Section: Welcome text (greetings below)
                Column(
                    modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subjectsTaught,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        modifier = Modifier.testTag("teacher_subjects_text")
                    )
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

                    // Tile 2: background #4361EE (blue)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
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
                                    contentDescription = "Classes",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = classesCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("stat_classes_count")
                                )
                                Text(
                                    text = "Classes",
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

                // SECTION — "This Week" highlight:
                Text(
                    text = "This Week",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.testTag("this_week_heading")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("this_week_highlight_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = SoftLavenderGrey
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Most active class",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextMuted,
                                modifier = Modifier.testTag("this_week_active_class_label")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mostActiveClass,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark,
                                modifier = Modifier.testTag("this_week_active_class_name")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$notesOpenedThisWeekCount notes opened this week",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextMuted,
                                modifier = Modifier.testTag("this_week_notes_opened_text")
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE76F51).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Most Active Class Trophy",
                                tint = Color(0xFFE76F51),
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag("this_week_trophy_icon")
                            )
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
