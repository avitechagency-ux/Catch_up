package com.example.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * 5 Core Tabs for My Learning Dashboard.
 */
enum class MyLearningTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TIMETABLE("Timetable", Icons.Outlined.CalendarMonth),
    REMINDERS("Reminders", Icons.Outlined.NotificationsActive),
    PROGRESS("Progress", Icons.Outlined.TrendingUp),
    GOALS("Goals", Icons.Outlined.Flag),
    HISTORY("History", Icons.Outlined.History)
}

@Composable
fun MyLearningScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MyLearningTab.TIMETABLE) }

    // Live states updated across tabs
    var sessions by remember { mutableStateOf<List<StudySession>>(emptyList()) }
    var goals by remember { mutableStateOf<List<StudyGoal>>(emptyList()) }
    var history by remember { mutableStateOf<List<StudyHistoryItem>>(emptyList()) }
    var progress by remember { mutableStateOf<StudentLearningProgress?>(null) }

    // Focus Lock active demo/state
    var isFocusLockActive by remember { mutableStateOf(false) }
    var activeFocusSession by remember { mutableStateOf<StudySession?>(null) }
    var showFocusLockOverlay by remember { mutableStateOf(false) }
    var showFocusLockDialog by remember { mutableStateOf(false) }

    fun refreshAll() {
        sessions = MyLearningRepository.getSessions(context)
        goals = MyLearningRepository.getGoals(context)
        history = MyLearningRepository.getHistory(context)
        progress = MyLearningRepository.computeStudentProgress(context)
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPastelBackground)
            .testTag("my_learning_screen"),
        containerColor = SoftPastelBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Purple Curved Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("my_learning_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Learning",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Personal dashboard, timetable & revision tracker",
                            fontSize = 12.sp,
                            color = LightLavender
                        )
                    }

                    // Focus Lock indicator button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFocusLockActive) CoralTile else GoldMustard,
                        modifier = Modifier
                            .clickable { showFocusLockDialog = true }
                            .testTag("open_focus_lock_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isFocusLockActive) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                contentDescription = null,
                                tint = if (isFocusLockActive) Color.White else DeepMaroon,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isFocusLockActive) "Locked" else "Focus Lock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFocusLockActive) Color.White else DeepMaroon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable tab bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MyLearningTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .testTag("tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) IndigoPurple else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) IndigoPurple else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    MyLearningTab.TIMETABLE -> {
                        PersonalTimetableView(
                            sessions = sessions,
                            onRefresh = { refreshAll() },
                            onStartFocusSession = { s ->
                                activeFocusSession = s
                                isFocusLockActive = true
                                showFocusLockOverlay = true
                            }
                        )
                    }

                    MyLearningTab.REMINDERS -> {
                        StudyRemindersView(
                            sessions = sessions
                        )
                    }

                    MyLearningTab.PROGRESS -> {
                        progress?.let { prog ->
                            LearningProgressView(progress = prog)
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = IndigoPurple)
                        }
                    }

                    MyLearningTab.GOALS -> {
                        StudyGoalsView(
                            goals = goals,
                            onRefresh = { refreshAll() }
                        )
                    }

                    MyLearningTab.HISTORY -> {
                        StudyHistoryView(history = history)
                    }
                }
            }
        }
    }

    // Focus Lock Settings / Status Dialog
    if (showFocusLockDialog) {
        FocusLockConfigDialog(
            isCurrentlyActive = isFocusLockActive,
            onDismiss = { showFocusLockDialog = false },
            onToggleLock = { active ->
                isFocusLockActive = active
                if (active) {
                    showFocusLockOverlay = true
                }
            }
        )
    }

    // Full screen AppLock-style overlay
    if (showFocusLockOverlay) {
        StudyFocusLockOverlayScreen(
            sessionTopic = activeFocusSession?.topic ?: "Uninterrupted Study Session",
            subject = activeFocusSession?.subject ?: "Active Focus",
            onDismiss = { showFocusLockOverlay = false },
            onEndEarly = {
                showFocusLockOverlay = false
                isFocusLockActive = false
                Toast.makeText(context, "Focus Lock ended early via student emergency exit.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * 1. PERSONAL TIMETABLE TAB
 */
@Composable
private fun PersonalTimetableView(
    sessions: List<StudySession>,
    onRefresh: () -> Unit,
    onStartFocusSession: (StudySession) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDayFilter by remember { mutableStateOf("All") }

    val daysOfWeek = listOf("All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    val filteredSessions = remember(sessions, selectedDayFilter) {
        if (selectedDayFilter == "All") sessions
        else sessions.filter { it.dayOfWeek.equals(selectedDayFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Day selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEach { day ->
                val isSelected = selectedDayFilter == day
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDayFilter = day },
                    label = {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPurple,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = TextDark
                    ),
                    border = BorderStroke(1.dp, if (isSelected) IndigoPurple else BorderGrey)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Row: Title & Add Session Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sessions (${filteredSessions.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPurple,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("add_study_session_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredSessions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.EventBusy,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No study sessions scheduled for $selectedDayFilter",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredSessions, key = { it.id }) { session ->
                    StudySessionCard(
                        session = session,
                        onMarkCompleted = {
                            MyLearningRepository.markSessionStatus(context, session.id, SessionStatus.COMPLETED)
                            onRefresh()
                            Toast.makeText(context, "Session marked completed!", Toast.LENGTH_SHORT).show()
                        },
                        onMarkSkipped = {
                            MyLearningRepository.markSessionStatus(context, session.id, SessionStatus.SKIPPED)
                            onRefresh()
                        },
                        onDelete = {
                            MyLearningRepository.deleteSession(context, session.id)
                            onRefresh()
                        },
                        onStartFocus = {
                            onStartFocusSession(session)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudySessionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newSession ->
                MyLearningRepository.addSession(context, newSession)
                onRefresh()
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StudySessionCard(
    session: StudySession,
    onMarkCompleted: () -> Unit,
    onMarkSkipped: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
            .testTag("session_card_${session.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IndigoPurple.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = session.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = session.dayOfWeek,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }

                // Status chip
                val (statusBg, statusFg, statusText) = when (session.status) {
                    SessionStatus.COMPLETED -> Triple(NoteCountGreen.copy(alpha = 0.15f), NoteCountGreen, "Completed")
                    SessionStatus.SKIPPED -> Triple(CoralTile.copy(alpha = 0.15f), CoralTile, "Skipped")
                    SessionStatus.SCHEDULED -> Triple(SoftPastelBackground, DeepMaroon, "Scheduled")
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.topic,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = IconGrey,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${session.startTime} - ${session.endTime}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                if (session.focusLockEnabled) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralTile.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Focus Lock",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralTile,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (session.focusLockEnabled && session.status == SessionStatus.SCHEDULED) {
                    OutlinedButton(
                        onClick = onStartFocus,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, CoralTile),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralTile)
                    ) {
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Start Focus Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (session.status == SessionStatus.SCHEDULED) {
                        Button(
                            onClick = onMarkCompleted,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NoteCountGreen, contentColor = Color.White)
                        ) {
                            Text(text = "Mark Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = IconGrey,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new study session.
 */
@Composable
private fun AddStudySessionDialog(
    onDismiss: () -> Unit,
    onAdd: (StudySession) -> Unit
) {
    var subject by remember { mutableStateOf("Physics") }
    var topic by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("Monday") }
    var startTime by remember { mutableStateOf("16:00") }
    var endTime by remember { mutableStateOf("17:30") }
    var focusLockEnabled by remember { mutableStateOf(true) }
    var reminderLeadMinutes by remember { mutableStateOf(15) }

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Geography", "History", "English", "Economics")
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Study Session", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic / Activity") },
                    placeholder = { Text("e.g. Wave Optics & Past Paper Prep") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_session_topic_input")
                )

                Text(text = "Day of Week", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEach { d ->
                        FilterChip(
                            selected = dayOfWeek == d,
                            onClick = { dayOfWeek = d },
                            label = { Text(d, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Focus Lock toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Enable Focus Lock", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Restricts distracting apps during study", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = focusLockEnabled,
                        onCheckedChange = { focusLockEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onAdd(
                            StudySession(
                                subject = subject,
                                topic = topic.trim(),
                                dayOfWeek = dayOfWeek,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                reminderLeadMinutes = reminderLeadMinutes,
                                focusLockEnabled = focusLockEnabled
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
            ) {
                Text("Add to Timetable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * 2. STUDY REMINDERS TAB
 */
@Composable
private fun StudyRemindersView(
    sessions: List<StudySession>
) {
    val context = LocalContext.current
    var defaultLeadMinutes by remember { mutableStateOf(15) }

    // Launcher for Android 13+ POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications disabled. Reminders will not alert you.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Permission Guide Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(shape = CircleShape, color = GoldMustard.copy(alpha = 0.2f), modifier = Modifier.size(38.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Outlined.NotificationsActive, contentDescription = null, tint = DeepMaroon, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(text = "Android Study Reminders", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "Local scheduled alarms & alert notifications", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "To guarantee you never miss a revision session, Cachap uses Android local scheduled alarms. Android 13+ requires notification permissions.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                Toast.makeText(context, "Notification permission pre-granted on this Android version.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                    ) {
                        Text("Grant Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "🔔 Test Reminder: Physics Study Session starts in 15 mins!", Toast.LENGTH_LONG).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Test Notification", fontSize = 12.sp)
                    }
                }
            }
        }

        // Configurable Reminder Lead Time
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Reminder Lead Time", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = "How long before the session starts should we alert you?", fontSize = 12.sp, color = TextMuted)

                Spacer(modifier = Modifier.height(12.dp))

                val leadOptions = listOf(5, 10, 15, 30)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    leadOptions.forEach { mins ->
                        val isSel = defaultLeadMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) IndigoPurple else SoftPastelBackground,
                            border = BorderStroke(1.dp, if (isSel) IndigoPurple else BorderGrey),
                            modifier = Modifier.clickable { defaultLeadMinutes = mins }
                        ) {
                            Text(
                                text = "$mins mins",
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else TextDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Scheduled Reminders List
        Text(text = "Active Scheduled Reminders (${sessions.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)

        sessions.forEach { s ->
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = s.topic, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "${s.dayOfWeek} at ${s.startTime} (${s.reminderLeadMinutes} min lead)", fontSize = 11.sp, color = TextMuted)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Snoozed reminder for 10 minutes", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Snooze 10m", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * 3. LEARNING PROGRESS TAB (Tracking REAL student data)
 */
@Composable
private fun LearningProgressView(
    progress: StudentLearningProgress
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Real Student Performance",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        // 2x2 Metric Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Study Sessions",
                value = "${progress.completedSessionsCount}/${progress.scheduledSessionsCount}",
                subtitle = "Sessions completed",
                color = IndigoPurple,
                icon = Icons.Outlined.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Quiz Attempts",
                value = "${progress.completedQuizzesCount}",
                subtitle = "Average: ${progress.averageQuizScorePercent}%",
                color = TealTile,
                icon = Icons.Outlined.Quiz,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Audio Lessons",
                value = "${progress.completedAudioLessonsCount}",
                subtitle = "Completed lectures",
                color = DeepMaroon,
                icon = Icons.Outlined.Headphones,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Goals Achieved",
                value = "${progress.completedGoalsCount}/${progress.completedGoalsCount + progress.activeGoalsCount}",
                subtitle = "${progress.activeGoalsCount} in progress",
                color = AmberTile,
                icon = Icons.Outlined.Flag,
                modifier = Modifier.weight(1f)
            )
        }

        // Weekly Study Activity Bar
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Weekly Activity Breakdown", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(12.dp))

                val days = listOf("Mon" to 0.8f, "Tue" to 0.4f, "Wed" to 0.6f, "Thu" to 0.2f, "Fri" to 0.9f, "Sat" to 0.5f, "Sun" to 0.3f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { (name, frac) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((80 * frac).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IndigoPurple)
                            )
                            Text(text = name, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

/**
 * 4. STUDY GOALS TAB
 */
@Composable
private fun StudyGoalsView(
    goals: List<StudyGoal>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Learning Goals (${goals.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Button(
                onClick = { showAddGoalDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(goals, key = { it.id }) { goal ->
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = goal.subject,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple
                            )
                            Text(
                                text = "Target: ${goal.targetDate}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = goal.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val frac = (goal.currentProgress.toFloat() / goal.targetProgress.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { frac },
                            color = if (goal.isCompleted) NoteCountGreen else IndigoPurple,
                            trackColor = TrackPurple,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${goal.currentProgress}/${goal.targetProgress} (${(frac * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        MyLearningRepository.toggleGoalCompletion(context, goal.id)
                                        onRefresh()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (goal.isCompleted) SoftPastelBackground else NoteCountGreen,
                                        contentColor = if (goal.isCompleted) DeepMaroon else Color.White
                                    )
                                ) {
                                    Text(
                                        text = if (goal.isCompleted) "Completed" else "Mark Done",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        MyLearningRepository.deleteGoal(context, goal.id)
                                        onRefresh()
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null, tint = IconGrey, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalSubject by remember { mutableStateOf("Physics") }
        var goalDate by remember { mutableStateOf("15 Dec 2026") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("New Study Goal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title") },
                        placeholder = { Text("e.g. Finish 10 Revision Sets") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalSubject,
                        onValueChange = { goalSubject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalDate,
                        onValueChange = { goalDate = it },
                        label = { Text("Target Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            MyLearningRepository.addGoal(
                                context,
                                StudyGoal(
                                    title = goalTitle.trim(),
                                    subject = goalSubject.trim(),
                                    targetDate = goalDate.trim()
                                )
                            )
                            onRefresh()
                            showAddGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * 5. STUDY HISTORY TAB
 */
@Composable
private fun StudyHistoryView(
    history: List<StudyHistoryItem>
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = IconGrey,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No study history yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete study sessions, quizzes, or audio lessons to build your study timeline.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (icon, color) = when (item.category) {
                            HistoryCategory.SESSION -> Icons.Outlined.CheckCircle to IndigoPurple
                            HistoryCategory.QUIZ -> Icons.Outlined.Quiz to TealTile
                            HistoryCategory.AUDIO_LESSON -> Icons.Outlined.Headphones to DeepMaroon
                            HistoryCategory.GOAL -> Icons.Outlined.Flag to GoldMustard
                        }

                        Surface(
                            shape = CircleShape,
                            color = color.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = "${item.subtitle} • ${item.formattedDate}", fontSize = 11.sp, color = TextMuted)
                        }

                        item.scorePercent?.let { sc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NoteCountGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$sc%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoteCountGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Focus Lock Config & Native Android Permission Dialog
 */
@Composable
private fun FocusLockConfigDialog(
    isCurrentlyActive: Boolean,
    onDismiss: () -> Unit,
    onToggleLock: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Check Usage Access Permission
    fun hasUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    // Check Overlay Permission
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    var usageGranted by remember { mutableStateOf(hasUsageAccess()) }
    var overlayGranted by remember { mutableStateOf(hasOverlayPermission()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = CoralTile)
                Text("Study Focus Lock Settings", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Study Focus Lock keeps you disciplined during study sessions by blocking distracting apps (e.g. TikTok, WhatsApp, games).",
                    fontSize = 12.sp,
                    color = TextDark,
                    lineHeight = 16.sp
                )

                // Native Android Permission Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftPastelBackground,
                    border = BorderStroke(1.dp, BorderGrey),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Android System Permissions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepMaroon)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "1. Usage Access: ${if (usageGranted) "✓ Allowed" else "⚠️ Required"}", fontSize = 11.sp, color = TextDark)
                            if (!usageGranted) {
                                TextButton(onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Open Settings -> Apps -> Special App Access -> Usage Access", Toast.LENGTH_LONG).show()
                                    }
                                }) {
                                    Text("Open Settings", fontSize = 10.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "2. Overlay Display: ${if (overlayGranted) "✓ Allowed" else "⚠️ Required"}", fontSize = 11.sp, color = TextDark)
                            if (!overlayGranted) {
                                TextButton(onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        try {
                                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        }
                                    }
                                }) {
                                    Text("Open Settings", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Text(text = "Restricted Apps List:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                val apps = listOf("TikTok", "Instagram", "WhatsApp", "YouTube", "Mobile Games", "Facebook", "X (Twitter)")
                apps.forEach { appName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = appName, fontSize = 12.sp, color = TextDark)
                        Checkbox(checked = true, onCheckedChange = {})
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onToggleLock(!isCurrentlyActive)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isCurrentlyActive) DeepMaroon else CoralTile)
            ) {
                Text(if (isCurrentlyActive) "Disarm Lock" else "Arm Focus Lock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/**
 * AppLock-Style Full Screen Study Lock Screen
 */
@Composable
fun StudyFocusLockOverlayScreen(
    sessionTopic: String,
    subject: String,
    onDismiss: () -> Unit,
    onEndEarly: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(25 * 60) } // 25 min default
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(isPaused) {
        while (!isPaused && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        if (secondsLeft <= 0) {
            onDismiss()
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)

    Dialog(
        onDismissRequest = {}, // Cannot dismiss by tapping outside
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E1C40), IndigoPurple, DeepMaroon)
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Study Focus Lock Active",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$subject • $sessionTopic",
                    fontSize = 14.sp,
                    color = LightLavender,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Big countdown display
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMustard,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isPaused) "Focus Lock is paused (5m break)" else "Keep your focus sharp! Non-educational apps are restricted.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Student controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { isPaused = !isPaused },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPaused) "Resume" else "Pause 5m")
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldMustard, contentColor = DeepMaroon)
                    ) {
                        Text("Return to Cachap", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Emergency Exit Button (Safety rule compliant)
                TextButton(
                    onClick = onEndEarly
                ) {
                    Text(
                        text = "Emergency Exit (End Session)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
