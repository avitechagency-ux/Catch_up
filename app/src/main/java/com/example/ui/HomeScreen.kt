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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.BottomNavBackground
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LavenderPlaceholder
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.PeachOrangeBorder
import com.example.ui.theme.PeachOrangeLight
import com.example.ui.theme.PeachOrangeMuted
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TrackPurple
import kotlinx.coroutines.launch

// Data Models
data class StreakData(
    val streakDays: Int = 6,
    val isTodayCompleted: Boolean = false,
    val nextMilestoneDays: Int = 7,
    val accumulatedMinutes: Int = 42,
    val accumulatedMillisToday: Long = 42L * 60L * 1000L,
    val lastActiveDate: String = "2026-09-19"
)

/**
 * Adds elapsed time to today's total session time.
 * When today's accumulated time reaches 60 minutes (1 hour):
 * - Marks today's goal complete
 * - Increments streak if yesterday was also complete
 * - Updates flame icon to lit state (#E76F51)
 * - Fills the daily-goal ring with checkmark
 */
fun recordSessionTime(
    currentStreak: StreakData,
    durationMillis: Long,
    wasYesterdayCompleted: Boolean = true
): StreakData {
    val updatedMillis = currentStreak.accumulatedMillisToday + durationMillis
    val updatedMinutes = (updatedMillis / (60 * 1000L)).toInt()

    return if (updatedMinutes >= 60 && !currentStreak.isTodayCompleted) {
        val updatedStreakDays = if (wasYesterdayCompleted) currentStreak.streakDays + 1 else 1
        checkStreakMilestone(updatedStreakDays)
        val nextMilestone = calculateNextMilestone(updatedStreakDays)
        currentStreak.copy(
            streakDays = updatedStreakDays,
            isTodayCompleted = true,
            nextMilestoneDays = nextMilestone,
            accumulatedMinutes = updatedMinutes,
            accumulatedMillisToday = updatedMillis
        )
    } else {
        currentStreak.copy(
            accumulatedMinutes = updatedMinutes,
            accumulatedMillisToday = updatedMillis
        )
    }
}

/**
 * Checks for date changes. If a full calendar day passed with less than 60 minutes total,
 * resets the streak to 0 on next app open.
 */
fun evaluateDayRollover(
    currentStreak: StreakData,
    todayDateStr: String,
    wasYesterdayCompleted: Boolean = true
): StreakData {
    if (currentStreak.lastActiveDate == todayDateStr) {
        return currentStreak
    }
    // New calendar day:
    val hadMetGoalYesterday = currentStreak.isTodayCompleted || currentStreak.accumulatedMinutes >= 60
    return if (hadMetGoalYesterday && wasYesterdayCompleted) {
        // Yesterday's 1-hour goal was met, start today fresh
        currentStreak.copy(
            isTodayCompleted = false,
            accumulatedMinutes = 0,
            accumulatedMillisToday = 0L,
            lastActiveDate = todayDateStr
        )
    } else {
        // Full day passed with less than 60 minutes: reset streak to 0
        currentStreak.copy(
            streakDays = 0,
            isTodayCompleted = false,
            accumulatedMinutes = 0,
            accumulatedMillisToday = 0L,
            lastActiveDate = todayDateStr
        )
    }
}

/**
 * Marks today's 1-hour goal complete immediately.
 */
fun markTodayGoalComplete(currentStreak: StreakData, wasYesterdayCompleted: Boolean = true): StreakData {
    if (currentStreak.isTodayCompleted) return currentStreak
    val updatedStreakDays = if (wasYesterdayCompleted) currentStreak.streakDays + 1 else 1
    checkStreakMilestone(updatedStreakDays)
    val nextMilestone = calculateNextMilestone(updatedStreakDays)
    return currentStreak.copy(
        streakDays = updatedStreakDays,
        isTodayCompleted = true,
        nextMilestoneDays = nextMilestone,
        accumulatedMinutes = 60.coerceAtLeast(currentStreak.accumulatedMinutes),
        accumulatedMillisToday = (60L * 60L * 1000L).coerceAtLeast(currentStreak.accumulatedMillisToday)
    )
}

/**
 * Function to reset streak to 0 if a full day is missed.
 */
fun resetStreakIfDayMissed(currentStreak: StreakData): StreakData {
    return currentStreak.copy(
        streakDays = 0,
        isTodayCompleted = false,
        nextMilestoneDays = 7,
        accumulatedMinutes = 0,
        accumulatedMillisToday = 0L
    )
}

/**
 * Stub function for a future celebration dialog at 7/30/100 days — empty body for now.
 */
fun checkStreakMilestone(streakDays: Int) {
    // Stub for celebration dialog at 7, 30, 100 days
    if (streakDays == 7 || streakDays == 30 || streakDays == 100) {
        // Will trigger milestone celebration dialog
    }
}

/**
 * Helper to determine the next milestone based on current streak.
 */
fun calculateNextMilestone(streakDays: Int): Int {
    val milestones = listOf(7, 30, 100, 365)
    return milestones.firstOrNull { it > streakDays } ?: (streakDays + 7)
}

data class SubjectTileData(
    val name: String,
    val lastOpenedDate: String,
    val backgroundColor: Color,
    val icon: ImageVector
)

data class RecentNoteData(
    val title: String,
    val subject: String,
    val date: String = "18/09/2026"
)

enum class NavItem(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    SUBJECTS("Subjects", Icons.Outlined.MenuBook),
    DOWNLOADS("Downloads", Icons.Outlined.Download),
    PROFILE("Profile", Icons.Outlined.Person)
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

val StudentNavItems: List<BottomNavItem> = listOf(
    BottomNavItem("Home", Icons.Outlined.Home),
    BottomNavItem("Subjects", Icons.Outlined.MenuBook),
    BottomNavItem("Downloads", Icons.Outlined.Download),
    BottomNavItem("Profile", Icons.Outlined.Person)
)

val TeacherNavItems: List<BottomNavItem> = listOf(
    BottomNavItem("Home", Icons.Outlined.Home),
    BottomNavItem("My Subjects", Icons.Outlined.MenuBook),
    BottomNavItem("Uploads", Icons.Outlined.CloudUpload),
    BottomNavItem("Profile", Icons.Outlined.Person)
)

/**
 * Main Home screen for the "Catch Up" secondary school notes-sharing app.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    studentName: String = "Sarah Ssenyonga",
    greeting: String = "Good morning 👋",
    streakData: StreakData = StreakData(streakDays = 6, isTodayCompleted = false, nextMilestoneDays = 7),
    recentNotes: List<RecentNoteData> = listOf(
        RecentNoteData(
            title = "Population Density",
            subject = "Geography",
            date = "18/09/2026"
        ),
        RecentNoteData(
            title = "Cell Division",
            subject = "Biology",
            date = "16/09/2026"
        ),
        RecentNoteData(
            title = "Algebra",
            subject = "Math",
            date = "15/09/2026"
        )
    ),
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToSubjectDetail: (String) -> Unit = {},
    onOpenNote: (title: String, subject: String) -> Unit = { _, _ -> },
    onNavigateToTeacher: () -> Unit = {}
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val quickAccessTiles = remember {
        listOf(
            SubjectTileData(
                name = "Geography",
                lastOpenedDate = "18/09/2026",
                backgroundColor = CoralTile,
                icon = Icons.Outlined.Public
            ),
            SubjectTileData(
                name = "Biology",
                lastOpenedDate = "16/09/2026",
                backgroundColor = NavyTile,
                icon = Icons.Outlined.Eco
            ),
            SubjectTileData(
                name = "Math",
                lastOpenedDate = "15/09/2026",
                backgroundColor = TealTile,
                icon = Icons.Outlined.Architecture
            ),
            SubjectTileData(
                name = "History",
                lastOpenedDate = "12/09/2026",
                backgroundColor = BlueTile,
                icon = Icons.Outlined.AccountBalance
            )
        )
    }

    val onActionNotice: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        0 -> { /* Already on Home */ }
                        1 -> onNavigateToSubjects()
                        2 -> onNavigateToDownloads()
                        3 -> onNavigateToProfile()
                        else -> {
                            val tabName = NavItem.values().getOrNull(index)?.label ?: "Tab"
                            onActionNotice("Opening $tabName...")
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
                .verticalScroll(scrollState)
        ) {
            // Purple Header Block with Platform Name at top, lowered Welcome Text, and Profile Avatar
            AppHeader(
                platformName = "Catch Up",
                greeting = greeting,
                studentName = studentName,
                onProfileClick = onNavigateToProfile,
                onNavigateToTeacher = onNavigateToTeacher
            )

            // White Body Content
            WhiteBody(
                streakData = streakData,
                quickAccessTiles = quickAccessTiles,
                recentNotes = recentNotes,
                onTileClick = { subject ->
                    onNavigateToSubjectDetail(subject)
                    onActionNotice("Opening $subject notes")
                },
                onQuickAccessSeeAll = {
                    onNavigateToSubjects()
                },
                onRecentClick = { note ->
                    onOpenNote(note.title, note.subject)
                },
                onRecentSeeAll = { onActionNotice("Opening all recent notes") }
            )
        }
    }
}

/**
 * 1. HEADER
 * - Solid deep indigo-purple #4B3F8F
 * - Corner radius: bottom-left & bottom-right 28dp, top corners 0dp
 * - Padding: 20dp left, 20dp right, 20dp top, 28dp bottom
 * - Upper section: Platform name "Catch Up"
 * - Generous vertical space
 * - Lower section: Welcome text (greeting + student name)
 */
@Composable
fun AppHeader(
    platformName: String = "Catch Up",
    greeting: String,
    studentName: String,
    onProfileClick: () -> Unit = {},
    onNavigateToTeacher: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAvatarMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
    ) {
        // Upper Section: Platform Name centered, size h1 (32sp) & bold
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = platformName,
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

        // Lower Section: Welcome text (greeting + student name)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = greeting,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = LightLavender,
                modifier = Modifier.testTag("greeting_text")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = studentName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("student_name_text")
            )
        }
    }
}

/**
 * 3. WHITE BODY
 * - Background: #FFFFFF
 * - Top corners: 28dp radius (left and right)
 * - Top padding: 36dp (to clear overlapping search bar)
 * - Left/Right padding: 20dp
 * - Vertical spacing between sections: 24dp
 */
@Composable
fun WhiteBody(
    streakData: StreakData,
    quickAccessTiles: List<SubjectTileData>,
    recentNotes: List<RecentNoteData>,
    onTileClick: (String) -> Unit,
    onQuickAccessSeeAll: () -> Unit,
    onRecentClick: (RecentNoteData) -> Unit,
    onRecentSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 28.dp)
    ) {
        // Section: Your Streak (Replaces previous Notes Overview section in exact same position)
        StreakSection(
            streakData = streakData
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Quick Access
        QuickAccessSection(
            tiles = quickAccessTiles,
            onTileClick = onTileClick,
            onSeeAllClick = onQuickAccessSeeAll
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Recent
        RecentSection(
            notes = recentNotes,
            onNoteClick = onRecentClick,
            onSeeAllClick = onRecentSeeAll
        )
    }
}

/**
 * 4. SECTION — "Your Streak"
 * - Section label: "Your Streak" (16sp SemiBold #1A1A1A) with no trailing link
 * - 12dp below label: Card (background #F7F6FB, radius 16dp, padding 16dp, no border, no elevation)
 *   - LEFT SIDE: Flame icon (32dp size)
 *     - Color #E76F51 (coral/orange) if today's daily goal is already completed
 *     - Color #C9C4DE (muted grey-lavender) if not yet completed
 *   - 12dp spacing right of icon, then vertical text stack:
 *     - Streak number: 22sp Bold #1A1A1A (e.g. "6")
 *     - Below it (2dp gap): "day streak" label, 13sp Regular #8A8A8A
 *   - Spacer pushes next element to the far right
 *   - RIGHT SIDE: circular progress ring, 48dp diameter, stroke width 5dp
 *     - If today's goal complete: ring fully filled #4B3F8F, white checkmark icon (16dp) centered inside
 *     - If not complete: empty track #E5E2F2, text "0/1" centered inside, 11sp #8A8A8A
 * - Below the card (10dp gap), a thin progress bar:
 *   - Full width, 6dp height, corner radius 3dp
 *   - Track color: #E5E2F2
 *   - Fill color: #4B3F8F, width proportional to progress toward next milestone
 *   - Caption below (4dp gap): 11sp #8A8A8A, e.g. "1 day to your next milestone"
 */
@Composable
fun StreakSection(
    streakData: StreakData,
    modifier: Modifier = Modifier
) {
    val daysRemaining = (streakData.nextMilestoneDays - streakData.streakDays).coerceAtLeast(0)
    val milestoneProgress = if (streakData.nextMilestoneDays > 0) {
        (streakData.streakDays.toFloat() / streakData.nextMilestoneDays.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val dayLabel = if (daysRemaining == 1) "1 day" else "$daysRemaining days"
    val captionText = "$dayLabel to your next milestone"

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Section Label
        Text(
            text = "Your Streak",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.testTag("section_title_your_streak")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Streak Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("streak_card"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF7F6FB),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE: Flame icon (32dp size)
                val flameColor = if (streakData.isTodayCompleted) Color(0xFFE76F51) else Color(0xFFC9C4DE)
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak Flame",
                    tint = flameColor,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("streak_flame_icon")
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Vertical text stack: Streak number + "day streak" label
                Column {
                    Text(
                        text = "${streakData.streakDays}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.testTag("streak_number_text")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "day streak",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("day_streak_label")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // RIGHT SIDE: Circular progress ring (48dp diameter, 5dp stroke)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("streak_goal_ring"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        val strokePx = 5.dp.toPx()
                        if (streakData.isTodayCompleted || streakData.accumulatedMinutes >= 60) {
                            // Ring fully filled #4B3F8F
                            drawCircle(
                                color = Color(0xFF4B3F8F),
                                radius = (size.minDimension - strokePx) / 2f,
                                style = Stroke(width = strokePx)
                            )
                        } else {
                            // Track background #E5E2F2
                            drawCircle(
                                color = Color(0xFFE5E2F2),
                                radius = (size.minDimension - strokePx) / 2f,
                                style = Stroke(width = strokePx)
                            )
                            // Progressive arc fill #4B3F8F
                            val progress = (streakData.accumulatedMinutes / 60f).coerceIn(0f, 1f)
                            if (progress > 0f) {
                                drawArc(
                                    color = Color(0xFF4B3F8F),
                                    startAngle = -90f,
                                    sweepAngle = progress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    if (streakData.isTodayCompleted || streakData.accumulatedMinutes >= 60) {
                        // White checkmark icon (16dp) centered inside
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Goal Complete",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("goal_complete_check")
                        )
                    } else {
                        // Accumulated minutes toward goal: e.g. "42/60", font size 10sp, color #8A8A8A
                        Text(
                            text = "${streakData.accumulatedMinutes}/60",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8A8A8A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("goal_incomplete_text")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Thin progress bar toward next milestone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFE5E2F2))
                .testTag("milestone_progress_bar")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(milestoneProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF4B3F8F))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Caption below progress bar
        Text(
            text = captionText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF8A8A8A),
            modifier = Modifier.testTag("milestone_caption_text")
        )
    }
}

/**
 * 5. SECTION — "Quick Access"
 * - Header row: "Quick Access" (16sp SemiBold #1A1A1A) | "See All" (13sp Regular #8A8A8A)
 * - 12dp below header: 2x2 grid, 12dp gap horizontal and vertical
 * - Tile height: 90dp, corner radius 16dp, padding 14dp
 * - Top-right: 20dp white icon
 * - Bottom-left: Subject name (15sp Bold #FFFFFF) + 2dp + "Last opened: [date]" (11sp Regular 85% white)
 */
@Composable
fun QuickAccessSection(
    tiles: List<SubjectTileData>,
    onTileClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Access",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.testTag("section_title_quick_access")
            )
            Text(
                text = "See All",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                modifier = Modifier
                    .clickable(onClick = onSeeAllClick)
                    .padding(vertical = 4.dp)
                    .testTag("quick_access_see_all")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Grid with 12dp spacing
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Top-Left (Tile 0), Top-Right (Tile 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tiles.isNotEmpty()) {
                    QuickAccessTile(
                        tileData = tiles[0],
                        onClick = { onTileClick(tiles[0].name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (tiles.size > 1) {
                    QuickAccessTile(
                        tileData = tiles[1],
                        onClick = { onTileClick(tiles[1].name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 2: Bottom-Left (Tile 2), Bottom-Right (Tile 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tiles.size > 2) {
                    QuickAccessTile(
                        tileData = tiles[2],
                        onClick = { onTileClick(tiles[2].name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (tiles.size > 3) {
                    QuickAccessTile(
                        tileData = tiles[3],
                        onClick = { onTileClick(tiles[3].name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Single Quick Access Tile
 */
@Composable
fun QuickAccessTile(
    tileData: SubjectTileData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(tileData.backgroundColor)
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("quick_access_tile_${tileData.name.lowercase()}")
    ) {
        // Small icon in top-right corner
        Icon(
            imageVector = tileData.icon,
            contentDescription = tileData.name,
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
        )

        // Subject name and Last opened date in bottom-left
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = tileData.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Last opened: ${tileData.lastOpenedDate}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 6. SECTION — "Recent"
 * - Header row: "Recent" (16sp SemiBold #1A1A1A) | "See All" (13sp Regular #8A8A8A)
 * - Maximum 3 topic cards that the user has opened and closed
 * - Topic card styling:
 *   - Sleek 1cm (~44-48dp) height profile
 *   - Rounded corners (12dp) with subject's distinct theme color
 *   - Topic title bold on left (white text) with subject name
 *   - Date / Subject badge on right (white text)
 *   - Click to open directly in PDF viewer
 */
@Composable
fun RecentSection(
    notes: List<RecentNoteData>,
    onNoteClick: (RecentNoteData) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedNotes = notes.take(3)

    Column(modifier = modifier.fillMaxWidth()) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.testTag("section_title_recent")
            )
            Text(
                text = "See All",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                modifier = Modifier
                    .clickable(onClick = onSeeAllClick)
                    .padding(vertical = 4.dp)
                    .testTag("recent_see_all")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayedNotes.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF7F6FB)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No recent topics opened yet",
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
                displayedNotes.forEach { note ->
                    RecentTopicCard(
                        note = note,
                        onClick = { onNoteClick(note) }
                    )
                }
            }
        }
    }
}

/**
 * Sleek 1cm Topic Card for the Recent Section.
 * - Distinct subject theme background color
 * - 12dp rounded corners
 * - Bold white title on left with subject label
 * - Date/access info on right
 */
@Composable
fun RecentTopicCard(
    note: RecentNoteData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = getSubjectThemeColor(note.subject)

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
            .testTag("recent_topic_card_${note.title.lowercase().replace(" ", "_")}"),
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
                Text(
                    text = note.subject,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = note.date,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * 7. BOTTOM NAVIGATION BAR
 * - Standard Android docked navigation bar fitted edge-to-edge
 * - Arrangement: Home, Subjects, Downloads, Profile
 * - Background: Peach Orange (#FF7A53)
 * - 1dp top border (#FFAB91) with soft upward elevation shadow
 * - Full width docked to the bottom with navigation bar inset padding
 * - Active item: clean white pill indicator with deep indigo (#4B3F8F) icon and bold label
 * - Inactive item: high-contrast white outline icon + label (#FFFFFF with 88% opacity)
 */
@Composable
fun BottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = StudentNavItems
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = PeachOrange.copy(alpha = 0.35f),
                spotColor = Color(0x33000000)
            )
            .border(
                width = 1.dp,
                color = PeachOrangeBorder,
                shape = RoundedCornerShape(0.dp)
            )
            .testTag("bottom_navigation_bar"),
        shape = RoundedCornerShape(0.dp),
        color = PeachOrange,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                val itemTestTag = "nav_item_${item.label.lowercase().replace(" ", "_")}"

                if (isSelected) {
                    // Active Item: Pill shape with white background & indigo theme accent
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .clickable { onItemSelected(index) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag(itemTestTag),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = IndigoPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }
                } else {
                    // Inactive Item: High-contrast white outline icon + label below
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onItemSelected(index) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag(itemTestTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
