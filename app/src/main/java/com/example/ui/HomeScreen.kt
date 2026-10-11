package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolumeUp
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.GoldMustard
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LavenderPlaceholder
import com.example.ui.theme.LightLavender
import com.example.ui.theme.LiveIndicatorRed
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
 * Main Home screen for the "Cachap" secondary school notes-sharing app.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    studentName: String = "Student",
    greeting: String = "Good morning 👋",
    streakData: StreakData = StreakData(streakDays = 0, isTodayCompleted = false, nextMilestoneDays = 7),
    activePlanDuration: String = "",
    activePlanExpiry: String = "",
    recentNotes: List<RecentNoteData> = emptyList(),
    linkedSubjectTeachers: Map<String, String> = emptyMap(),
    onLinkSubjectTeacher: (subjectName: String, teacherId: String) -> Unit = { _, _ -> },
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToSubjectDetail: (String) -> Unit = {},
    onOpenNote: (title: String, subject: String) -> Unit = { _, _ -> },
    onNavigateToLiveClasses: () -> Unit = {},
    onNavigateToTeacher: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {}
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var localLinkedTeachers by remember(linkedSubjectTeachers) {
        mutableStateOf(linkedSubjectTeachers)
    }
    var subjectPendingLink by remember { mutableStateOf<String?>(null) }

    // Synchronized with DefaultCurriculumSubjects from SubjectsScreen:
    // Prioritizes subjects the student has linked, then fills up to 4 from DefaultCurriculumSubjects
    val quickAccessTiles = remember(localLinkedTeachers) {
        val linkedItems = localLinkedTeachers.keys.mapNotNull { linkedName ->
            DefaultCurriculumSubjects.find { it.name.equals(linkedName, ignoreCase = true) }
        }
        val remainingItems = DefaultCurriculumSubjects.filterNot { candidate ->
            linkedItems.any { it.name.equals(candidate.name, ignoreCase = true) }
        }
        (linkedItems + remainingItems).take(4).map { subjectItem ->
            SubjectTileData(
                name = subjectItem.name,
                lastOpenedDate = "Tap to open",
                backgroundColor = subjectItem.color,
                icon = subjectItem.icon
            )
        }
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
            // Purple Header Block with Platform Name at top and Welcome Text
            AppHeader(
                platformName = "Cachap",
                greeting = greeting,
                studentName = studentName,
                onProfileClick = onNavigateToProfile,
                onNavigateToTeacher = onNavigateToTeacher,
                onNavigateToAdmin = onNavigateToAdmin
            )

            // White Body Content
            WhiteBody(
                activePlanDuration = activePlanDuration,
                activePlanExpiry = activePlanExpiry,
                quickAccessTiles = quickAccessTiles,
                recentNotes = recentNotes,
                onSelectOrManagePlan = onNavigateToAccount,
                onTileClick = { subject ->
                    val linkedTeacherId = localLinkedTeachers.entries
                        .firstOrNull { it.key.equals(subject, ignoreCase = true) }
                        ?.value
                    if (!linkedTeacherId.isNullOrBlank()) {
                        onNavigateToSubjectDetail(subject)
                        onActionNotice("Opening $subject notes...")
                    } else {
                        subjectPendingLink = subject
                    }
                },
                onQuickAccessSeeAll = {
                    onNavigateToSubjects()
                },
                onRecentClick = { note ->
                    onOpenNote(note.title, note.subject)
                },
                onRecentSeeAll = { onActionNotice("Opening all recent notes") },
                onNavigateToLiveClasses = onNavigateToLiveClasses,
                onNavigateToAdmin = onNavigateToAdmin
            )
        }
    }

    // Teacher ID code input popup when student clicks an unlinked Quick Access subject
    subjectPendingLink?.let { targetSubjectName ->
        TeacherIdLinkPopup(
            subjectName = targetSubjectName,
            onDismiss = { subjectPendingLink = null },
            onSubmitTeacherId = { enteredCode ->
                localLinkedTeachers = localLinkedTeachers + (targetSubjectName to enteredCode)
                onLinkSubjectTeacher(targetSubjectName, enteredCode)
                subjectPendingLink = null
                onNavigateToSubjectDetail(targetSubjectName)
                onActionNotice("Opening $targetSubjectName notes...")
            }
        )
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
    platformName: String = "Cachap",
    greeting: String,
    studentName: String,
    onProfileClick: () -> Unit = {},
    onNavigateToTeacher: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
    activePlanDuration: String = "",
    activePlanExpiry: String = "",
    quickAccessTiles: List<SubjectTileData>,
    recentNotes: List<RecentNoteData>,
    onSelectOrManagePlan: () -> Unit = {},
    onTileClick: (String) -> Unit,
    onQuickAccessSeeAll: () -> Unit,
    onRecentClick: (RecentNoteData) -> Unit,
    onRecentSeeAll: () -> Unit,
    onNavigateToLiveClasses: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 28.dp)
    ) {
        // Section: Current Plan (replaces streaks on the student panel)
        CurrentPlanSection(
            activePlanDuration = activePlanDuration,
            activePlanExpiry = activePlanExpiry,
            onSelectOrManagePlan = onSelectOrManagePlan
        )

        // Section: Live Audio Classes (Governed by central feature flag)
        if (com.example.data.AppFeatures.ONLINE_CLASSES_ENABLED) {
            Spacer(modifier = Modifier.height(24.dp))
            LiveClassesBannerSection(onNavigateToLiveClasses = onNavigateToLiveClasses)
        }

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
 * Helper to determine if a student plan is currently active.
 */
fun isStudentPlanActive(planDuration: String): Boolean {
    val trimmed = planDuration.trim()
    return trimmed.isNotEmpty() &&
        !trimmed.equals("None", ignoreCase = true) &&
        !trimmed.equals("No Plan", ignoreCase = true) &&
        !trimmed.equals("No Plan Selected", ignoreCase = true) &&
        !trimmed.equals("Inactive", ignoreCase = true)
}

/**
 * 4. SECTION — "Current Plan"
 * Replaces streaks on the Student Home panel.
 * - If a plan is active: displays the brand-purple Current Plan card with active badge, duration, and expiry.
 * - If no plan is active: displays a custom website-style "unloaded plan card" placeholder specifically
 *   crafted for our subscription plan card (dashed border frame, custom unloaded Plan Pass graphic,
 *   skeleton placeholder lines, and "No Plan Selected" prompt).
 */
@Composable
fun CurrentPlanSection(
    activePlanDuration: String,
    activePlanExpiry: String,
    onSelectOrManagePlan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasActivePlan = isStudentPlanActive(activePlanDuration)

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Plan",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.testTag("section_title_current_plan")
            )
            Text(
                text = if (hasActivePlan) "Manage" else "Select Plan",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = IndigoPurple,
                modifier = Modifier
                    .clickable(onClick = onSelectOrManagePlan)
                    .padding(vertical = 4.dp)
                    .testTag("current_plan_action_link")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (hasActivePlan) {
            ActiveStudentPlanCard(
                planDuration = activePlanDuration,
                planExpiry = activePlanExpiry.ifBlank { "Active subscription" },
                onClick = onSelectOrManagePlan
            )
        } else {
            UnloadedPlanCardPlaceholder(
                onClick = onSelectOrManagePlan
            )
        }
    }
}

/**
 * Active Plan Card shown when the student has an active subscription plan.
 */
@Composable
fun ActiveStudentPlanCard(
    planDuration: String,
    planExpiry: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("home_active_plan_card"),
        shape = RoundedCornerShape(16.dp),
        color = IndigoPurple,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Current Plan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = LightLavender,
                modifier = Modifier.testTag("home_current_plan_label")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = planDuration,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("home_current_plan_duration")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = planExpiry,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = LightLavender,
                modifier = Modifier.testTag("home_current_plan_expiry")
            )
        }
    }
}

/**
 * Section banner for Live Audio Classes on the Student Home dashboard.
 */
@Composable
fun LiveClassesBannerSection(
    onNavigateToLiveClasses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onNavigateToLiveClasses() }
            .testTag("home_live_classes_banner"),
        shape = RoundedCornerShape(16.dp),
        color = DeepMaroon,
        border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.6f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GoldMustard,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = "Live Classes",
                            tint = DeepMaroon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Live Audio Classes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = LiveIndicatorRed
            ) {
                Text(
                    text = "LIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Website-style "Unloaded Plan Card" placeholder shown when no plan is active.
 * Styled after web unloaded-component placeholders, but custom-built specifically for a Plan Card
 * (NOT an image/mountain placeholder):
 * - Dashed inset border around the plan card slot
 * - Custom-drawn Unloaded Plan Pass icon (miniature subscription card with header band, tier badge,
 *   skeleton lines, and diagonal unloaded slash)
 * - Skeleton placeholder bars representing the unloaded plan title & expiry fields
 * - Clear "No Plan Selected" label + "Tap to choose a plan" call-to-action
 */
@Composable
fun UnloadedPlanCardPlaceholder(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dashBorderColor = Color(0xFFC5BFE0)
    val cardBgColor = Color(0xFFF7F6FB)
    val skeletonBarColor = Color(0xFFE4E0F2)
    val skeletonBarLight = Color(0xFFECE9F7)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
            .drawBehind {
                val strokeWidthPx = 1.5.dp.toPx()
                val halfStroke = strokeWidthPx / 2f
                // Subtle diagonal unloaded web-slot guide lines in background
                drawLine(
                    color = Color(0xFFECE9F6),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                // Dashed border around the entire unloaded plan card slot
                drawRoundRect(
                    color = dashBorderColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f), 0f)
                    )
                )
            }
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("unloaded_plan_card_placeholder")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Unloaded status tag + ghost skeleton badge on right (mirrors ActivePlanCard top row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PeachOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PLAN CARD NOT LOADED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = Color(0xFF7E789A),
                        modifier = Modifier.testTag("unloaded_plan_status_tag")
                    )
                }

                // Website-style unloaded badge placeholder pill
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(skeletonBarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background( Color(0xFFD0CBE6))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Middle Row: Custom Unloaded Plan Card Graphic + "No Plan Selected" & Skeleton Lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom-drawn Unloaded Plan Pass Card Icon (specifically for plan card, NOT an image icon)
                UnloadedPlanCardIcon(
                    modifier = Modifier
                        .size(58.dp)
                        .testTag("unloaded_plan_card_icon")
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No Plan Selected",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D284B),
                        modifier = Modifier.testTag("no_plan_selected_title")
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "No active study plan loaded for your account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF7E789A),
                        modifier = Modifier.testTag("no_plan_selected_subtitle")
                    )

                    Spacer(modifier = Modifier.height(9.dp))

                    // Skeleton bars mimicking the unloaded duration & expiry rows of a Plan Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(76.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(skeletonBarColor)
                        )
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(skeletonBarLight)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Row: Unloaded expiry skeleton bar on left + "Select a Plan ->" pill on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(skeletonBarColor)
                    )
                    Text(
                        text = "0 / 3 plans active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E88A8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IndigoPurple.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, IndigoPurple.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose Plan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Choose Plan",
                            tint = IndigoPurple,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas graphic built specifically for an unloaded Subscription Plan Card (NOT a broken image icon).
 * Renders:
 * - A recessed rounded square frame with a dashed inner border
 * - A miniature horizontal "Plan Pass / Subscription Card" with a purple top header strip,
 *   a plan chip/badge circle on the right, and two horizontal plan text lines on the left
 * - A crisp diagonal "unloaded / disconnected" break line with an orange status indicator badge
 */
@Composable
fun UnloadedPlanCardIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFECE9F8))
            .border(
                width = 1.dp,
                color = Color(0xFFD2CCE8),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val w = size.width
            val h = size.height

            // Miniature Plan Card dimensions
            val cardLeft = w * 0.08f
            val cardTop = h * 0.18f
            val cardWidth = w * 0.84f
            val cardHeight = h * 0.64f
            val corner = 5.dp.toPx()

            // 1. Plan Card soft fill
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(corner, corner)
            )

            // 2. Plan Card top header band (represents the subscription tier header strip)
            drawRoundRect(
                color = Color(0xFF4B3F8F).copy(alpha = 0.22f),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardWidth, cardHeight * 0.32f),
                cornerRadius = CornerRadius(corner, corner)
            )

            // 3. Plan Card dashed outline
            drawRoundRect(
                color = Color(0xFF4B3F8F).copy(alpha = 0.65f),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                )
            )

            // 4. Miniature Plan lines inside the card (duration bar + expiry bar)
            val lineStartX = cardLeft + cardWidth * 0.14f
            val line1Y = cardTop + cardHeight * 0.54f
            val line2Y = cardTop + cardHeight * 0.74f

            drawLine(
                color = Color(0xFF4B3F8F).copy(alpha = 0.55f),
                start = Offset(lineStartX, line1Y),
                end = Offset(cardLeft + cardWidth * 0.56f, line1Y),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color(0xFF4B3F8F).copy(alpha = 0.30f),
                start = Offset(lineStartX, line2Y),
                end = Offset(cardLeft + cardWidth * 0.42f, line2Y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 5. Miniature Plan Badge ring on the right side of the mini card
            val badgeCenter = Offset(cardLeft + cardWidth * 0.78f, cardTop + cardHeight * 0.62f)
            drawCircle(
                color = Color(0xFF4B3F8F).copy(alpha = 0.35f),
                radius = 3.5.dp.toPx(),
                center = badgeCenter,
                style = Stroke(width = 1.4.dp.toPx())
            )

            // 6. Diagonal "Unloaded / No Plan" slash across the mini plan card
            drawLine(
                color = Color(0xFFECE9F8),
                start = Offset(w * 0.18f, h * 0.84f),
                end = Offset(w * 0.82f, h * 0.16f),
                strokeWidth = 4.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFE76F51),
                start = Offset(w * 0.20f, h * 0.82f),
                end = Offset(w * 0.80f, h * 0.18f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Dedicated linking button / card on the Student page to navigate directly to the Admin Portal.
 */
@Composable
fun AdminPortalCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_admin_portal_card"),
        shape = RoundedCornerShape(18.dp),
        color = SoftLavenderGrey,
        border = BorderStroke(1.dp, IndigoPurple.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IndigoPurple,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Admin Portal",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Portal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple,
                            modifier = Modifier.testTag("student_admin_portal_title")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndigoPurple.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Admin",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IndigoPurple,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Access revenue, analytics, users & platform controls",
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        modifier = Modifier.testTag("student_admin_portal_subtitle")
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Navigate to Admin Portal",
                tint = IndigoPurple,
                modifier = Modifier.size(20.dp)
            )
        }
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
 * - Bottom-left: Subject name (15sp Bold #FFFFFF)
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

        // 2x2 Grid with 16dp spacing matching SubjectsScreen
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Row 1: Top-Left (Tile 0), Top-Right (Tile 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
 * Single Quick Access Subject Card — styled and linked identically to SubjectCard in SubjectsScreen
 */
@Composable
fun QuickAccessTile(
    tileData: SubjectTileData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = tileData.backgroundColor.copy(alpha = 0.35f),
                spotColor = tileData.backgroundColor.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("quick_access_tile_${tileData.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        color = tileData.backgroundColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tileData.icon,
                contentDescription = tileData.name,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = tileData.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
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
