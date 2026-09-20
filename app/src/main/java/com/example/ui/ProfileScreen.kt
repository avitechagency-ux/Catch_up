package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TrackPurple
import kotlinx.coroutines.launch

/**
 * Data representation for an achievement badge.
 */
data class AchievementBadge(
    val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedOrder: Int = 0, // Higher indicates more recently unlocked
    val progressCurrent: Int = 0,
    val progressMax: Int = 100,
    val progressUnit: String = "days",
    val primaryRibbonColor: Color = Color.Transparent,
    val secondaryRibbonColor: Color = Color.Transparent,
    val medallionBaseColor: Color = Color.Transparent,
    val medallionAccentColor: Color = Color.Transparent,
    val centerIcon: ImageVector = Icons.Outlined.EmojiEvents,
    val centerLabel: String = "",
    val drawableResId: Int = R.drawable.badge_first_note
)

/**
 * Helper to convert any Color to its standard perceptual CIE luminance greyscale.
 */
fun Color.toGreyscale(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return Color(luminance, luminance, luminance, alpha)
}

/**
 * Profile Screen (Catch Up design system).
 *
 * Screen Structure:
 * 1. HEADER (Purple #4B3F8F, ~180dp, centered 72dp avatar with initial, student name, class + school)
 * 2. WHITE BODY (Rounded top 28dp overlapping header, 20dp left/right, 24dp top, 20dp section spacing)
 * 3. Access Status (10dp dot + Active/Expired, validity info)
 * 4. Your Activity (Time this term, Notes read cards)
 * 5. Achievements (Trophy shelf: Featured spotlight badge + 2-column grid for remaining 7 badges)
 * 6. Account (52dp rows: Settings, Help & Support, Log out in red)
 * 7. Bottom Navigation Bar (reused BottomNavBar composable, Profile tab active)
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    studentName: String = "Sarah Ssenyonga",
    schoolName: String = "Jjungo Senior Secondary School",
    academicClass: String = "S.5 Blue",
    streakDays: Int = 6,
    notesReadCount: Int = 47,
    timeThisTermText: String = "18h",
    isAccessActive: Boolean = true,
    validUntilText: String = "Valid until Term 2, 2026",
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToTeacher: () -> Unit = {},
    onUpdateProfile: (name: String, school: String, studentClass: String) -> Unit = { _, _, _ -> }
) {
    var currentName by remember { mutableStateOf(studentName) }
    var currentSchool by remember { mutableStateOf(schoolName) }
    var currentClass by remember { mutableStateOf(academicClass) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedBadgeDetail by remember { mutableStateOf<AchievementBadge?>(null) }

    // Settings state
    var studyRemindersEnabled by remember { mutableStateOf(true) }
    var offlineAutoCacheEnabled by remember { mutableStateOf(true) }

    // Base order of the 8 badges according to specification:
    // 1. First Note, 2. Bookworm, 3. Subject Explorer, 4. 30-Day Streak,
    // 5. 100-Day Streak, 6. Perfect Term, 7. Night Owl, 8. Century Club
    val allBadges = remember(notesReadCount, streakDays) {
        listOf(
            AchievementBadge(
                id = "first_note",
                name = "First Note",
                description = "Started your academic journey by opening your first revision note.",
                isUnlocked = true,
                unlockedOrder = 1,
                progressCurrent = 1,
                progressMax = 1,
                progressUnit = "note",
                primaryRibbonColor = Color(0xFF0D9488), // Teal
                secondaryRibbonColor = Color(0xFFF59E0B), // Warm Gold
                medallionBaseColor = Color(0xFF0F766E),
                medallionAccentColor = Color(0xFFFBBF24),
                centerIcon = Icons.Outlined.MenuBook,
                drawableResId = R.drawable.badge_first_note
            ),
            AchievementBadge(
                id = "bookworm",
                name = "Bookworm",
                description = "Completed reading 10 full syllabus topics across academic subjects.",
                isUnlocked = true,
                unlockedOrder = 2,
                progressCurrent = 10,
                progressMax = 10,
                progressUnit = "notes",
                primaryRibbonColor = Color(0xFF4338CA), // Indigo
                secondaryRibbonColor = Color(0xFFA5B4FC), // Lavender
                medallionBaseColor = Color(0xFF3730A3),
                medallionAccentColor = Color(0xFFF59E0B),
                centerIcon = Icons.Outlined.WorkspacePremium,
                drawableResId = R.drawable.badge_bookworm
            ),
            AchievementBadge(
                id = "subject_explorer",
                name = "Subject Explorer",
                description = "Studied notes across 4 distinct subject departments.",
                isUnlocked = true,
                unlockedOrder = 3,
                progressCurrent = 4,
                progressMax = 4,
                progressUnit = "subjects",
                primaryRibbonColor = Color(0xFF0284C7), // Sky Blue
                secondaryRibbonColor = Color(0xFF38BDF8),
                medallionBaseColor = Color(0xFF0369A1),
                medallionAccentColor = Color(0xFFFCD34D),
                centerIcon = Icons.Outlined.Explore,
                drawableResId = R.drawable.badge_subject_explorer
            ),
            AchievementBadge(
                id = "streak_30",
                name = "30-Day Streak",
                description = "Maintained an unbroken daily study streak for 30 consecutive days.",
                isUnlocked = false,
                progressCurrent = streakDays.coerceAtMost(30),
                progressMax = 30,
                progressUnit = "days",
                primaryRibbonColor = Color(0xFFDC2626), // Fire Red
                secondaryRibbonColor = Color(0xFFF97316), // Orange
                medallionBaseColor = Color(0xFFB91C1C),
                medallionAccentColor = Color(0xFFFBBF24),
                centerIcon = Icons.Outlined.LocalFireDepartment,
                centerLabel = "30",
                drawableResId = R.drawable.badge_30_day_streak
            ),
            AchievementBadge(
                id = "streak_100",
                name = "100-Day Streak",
                description = "Attained the legendary century milestone of 100 consecutive days of revision.",
                isUnlocked = false,
                progressCurrent = streakDays.coerceAtMost(100),
                progressMax = 100,
                progressUnit = "days",
                primaryRibbonColor = Color(0xFF7C3AED), // Royal Violet
                secondaryRibbonColor = Color(0xFFFBBF24), // Gold
                medallionBaseColor = Color(0xFF5B21B6),
                medallionAccentColor = Color(0xFFFDE047),
                centerIcon = Icons.Outlined.Stars,
                centerLabel = "100",
                drawableResId = R.drawable.badge_100_day_streak
            ),
            AchievementBadge(
                id = "perfect_term",
                name = "Perfect Term",
                description = "Met all weekly revision goals throughout the entire term.",
                isUnlocked = false,
                progressCurrent = 4,
                progressMax = 12,
                progressUnit = "weeks",
                primaryRibbonColor = Color(0xFF059669), // Emerald
                secondaryRibbonColor = Color(0xFF6EE7B7), // Mint
                medallionBaseColor = Color(0xFF047857),
                medallionAccentColor = Color(0xFFFDE047),
                centerIcon = Icons.Outlined.School,
                drawableResId = R.drawable.badge_perfect_term
            ),
            AchievementBadge(
                id = "night_owl",
                name = "Night Owl",
                description = "Completed an intensive evening revision session past 8:00 PM.",
                isUnlocked = true,
                unlockedOrder = 4, // Most recently unlocked
                progressCurrent = 1,
                progressMax = 1,
                progressUnit = "session",
                primaryRibbonColor = Color(0xFF1E1B4B), // Midnight
                secondaryRibbonColor = Color(0xFF818CF8), // Periwinkle
                medallionBaseColor = Color(0xFF312E81),
                medallionAccentColor = Color(0xFFFBBF24),
                centerIcon = Icons.Outlined.NightsStay,
                drawableResId = R.drawable.badge_night_owl
            ),
            AchievementBadge(
                id = "century_club",
                name = "Century Club",
                description = "Read and reviewed 100 comprehensive revision notes.",
                isUnlocked = false,
                progressCurrent = notesReadCount.coerceAtMost(100),
                progressMax = 100,
                progressUnit = "notes",
                primaryRibbonColor = Color(0xFF581C87), // Imperial Purple
                secondaryRibbonColor = Color(0xFFFACC15), // Champion Gold
                medallionBaseColor = Color(0xFF4C1D95),
                medallionAccentColor = Color(0xFFFEF08A),
                centerIcon = Icons.Outlined.EmojiEvents,
                centerLabel = "100",
                drawableResId = R.drawable.badge_century_club
            )
        )
    }

    // Determine featured badge: most recently unlocked, or nearest-to-unlock preview
    val featuredBadge = remember(allBadges) {
        allBadges.filter { it.isUnlocked }.maxByOrNull { it.unlockedOrder }
            ?: allBadges.minByOrNull { (it.progressMax - it.progressCurrent).coerceAtLeast(0) }
            ?: allBadges.first()
    }

    // Remaining 7 badges in base order (skipping whichever is featured)
    val remainingBadges = remember(allBadges, featuredBadge) {
        allBadges.filter { it.id != featuredBadge.id }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 3, // "Profile" is index 3
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToSubjects()
                        2 -> onNavigateToDownloads()
                        3 -> { /* Already on Profile screen */ }
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
            // 1. HEADER
            // Purple background (#4B3F8F), ~180dp, rounded bottom corners 28dp
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .testTag("profile_header"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Circular avatar, 72dp diameter, background #FFFFFF, centered initial in #4B3F8F 24sp Bold
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showEditProfileDialog = true }
                            .testTag("profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = currentName.trim().firstOrNull()?.uppercase() ?: "S"
                        Text(
                            text = initial,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }

                    // 10dp spacing below avatar
                    Spacer(modifier = Modifier.height(10.dp))

                    // Student name: 18sp Bold #FFFFFF
                    Text(
                        text = currentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("profile_student_name")
                    )

                    // 2dp spacing below name
                    Spacer(modifier = Modifier.height(2.dp))

                    // Class + school text: 13sp Regular #D6D0F0
                    Text(
                        text = "$currentClass • $currentSchool",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("profile_school_class")
                    )
                }
            }

            // ==========================================
            // 2. WHITE BODY
            // Background #FFFFFF, rounded top corners 28dp, overlapping header slightly
            // 20dp left/right padding, 24dp top padding, 20dp spacing between sections
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-14).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp)
            ) {
                // ==========================================
                // 3. SECTION — "Access Status"
                // ==========================================
                Text(
                    text = "Access Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.testTag("section_access_status_title")
                )

                // 12dp gap below section label
                Spacer(modifier = Modifier.height(12.dp))

                // Card: #F7F6FB, 16dp corner radius, 16dp padding
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("access_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = SoftLavenderGrey
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Left: status dot (10dp circle) + text
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isAccessActive) Color(0xFF2A9D8F) else Color(0xFFE76F51))
                                    .testTag("status_dot")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAccessActive) "Active" else "Expired",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAccessActive) Color(0xFF2A9D8F) else Color(0xFFE76F51),
                                modifier = Modifier.testTag("status_text")
                            )
                        }

                        // 4dp gap below status
                        Spacer(modifier = Modifier.height(4.dp))

                        // Expiry info: 12sp #8A8A8A
                        Text(
                            text = validUntilText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextMuted,
                            modifier = Modifier.testTag("status_validity_text")
                        )
                    }
                }

                // 20dp spacing between sections
                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 4. SECTION — "Your Activity"
                // ==========================================
                Text(
                    text = "Your Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.testTag("section_activity_title")
                )

                // 12dp gap below section label
                Spacer(modifier = Modifier.height(12.dp))

                // Row of 2 stat cards, 12dp gap between them, each equal width
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Time this term
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("activity_card_time"),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = timeThisTermText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("activity_time_value")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Time this term",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card 2: Notes read
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("activity_card_notes"),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = notesReadCount.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("activity_notes_value")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes read",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 20dp spacing between sections
                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 5. SECTION — "Achievements" (Trophy Shelf)
                // ==========================================
                Text(
                    text = "Achievements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.testTag("section_achievements_title")
                )

                // 12dp gap below section label
                Spacer(modifier = Modifier.height(12.dp))

                // 1. FEATURED SPOTLIGHT (Top of section)
                val isSpotlightUnlocked = featuredBadge.isUnlocked
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedBadgeDetail = featuredBadge }
                        .testTag("achievements_spotlight_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = SoftLavenderGrey
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Small label above badge
                        Text(
                            text = if (isSpotlightUnlocked) "Latest achievement" else "Next up",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("spotlight_header_label")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large medal image (180dp width)
                        MedalBadgeGraphic(
                            badge = featuredBadge,
                            sizeDp = 180.dp,
                            isLocked = !isSpotlightUnlocked,
                            alphaVal = if (isSpotlightUnlocked) 1f else 0.5f,
                            modifier = Modifier.testTag("spotlight_medal_graphic")
                        )

                        // 12dp gap below medal
                        Spacer(modifier = Modifier.height(12.dp))

                        // Dedicated badge name text element: 16sp SemiBold #1A1A1A
                        Text(
                            text = featuredBadge.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("spotlight_badge_name")
                        )

                        // If locked "Next up" preview: show progress caption + thin progress bar
                        if (!isSpotlightUnlocked) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${featuredBadge.progressCurrent}/${featuredBadge.progressMax} ${featuredBadge.progressUnit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Thin progress bar: 6dp height, 3dp corner radius, track #E5E2F2, fill #4B3F8F
                            val progressFraction = (featuredBadge.progressCurrent.toFloat() / featuredBadge.progressMax.toFloat())
                                .coerceIn(0f, 1f)

                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(TrackPurple)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(IndigoPurple)
                                )
                            }
                        }
                    }
                }

                // 20dp positioned below spotlight card
                Spacer(modifier = Modifier.height(20.dp))

                // 2. GRID BELOW (Remaining 7 badges)
                // 2-column grid, 12dp gap horizontal and vertical, height approx 130dp
                val chunkedBadges = remainingBadges.chunked(2)
                chunkedBadges.forEachIndexed { rowIndex, rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp)
                            ) {
                                AchievementGridCard(
                                    badge = badge,
                                    onClick = { selectedBadgeDetail = badge }
                                )
                            }
                        }

                        // If row has only 1 item (odd count), place an empty equal-weight spacer
                        if (rowBadges.size == 1) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp)
                            )
                        }
                    }

                    if (rowIndex < chunkedBadges.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // 20dp spacing between sections
                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 6. SECTION — "Account"
                // ==========================================
                Text(
                    text = "Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.testTag("section_account_title")
                )

                // 12dp gap below section label
                Spacer(modifier = Modifier.height(12.dp))

                // Vertical list of simple rows (52dp height, full width, 1dp bottom border #F0F0F0)
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Settings
                    AccountRow(
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        isDestructive = false,
                        showChevron = true,
                        testTag = "account_row_settings",
                        onClick = { showSettingsDialog = true }
                    )

                    // Row 2: Help & Support
                    AccountRow(
                        icon = Icons.Outlined.HelpOutline,
                        label = "Help & Support",
                        isDestructive = false,
                        showChevron = true,
                        testTag = "account_row_help",
                        onClick = { showHelpDialog = true }
                    )

                    // Row 3: Switch to Teacher View
                    AccountRow(
                        icon = Icons.Outlined.SwapHoriz,
                        label = "Switch to Teacher View",
                        isDestructive = false,
                        showChevron = true,
                        testTag = "account_row_switch_teacher",
                        onClick = onNavigateToTeacher
                    )

                    // Row 4: Log out (label in #E76F51 red, no chevron)
                    AccountRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        label = "Log out",
                        isDestructive = true,
                        showChevron = false,
                        testTag = "account_row_logout",
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & ACTION MODALS
    // ==========================================

    // Edit Profile Modal
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentName) }
        var editSchool by remember { mutableStateOf(currentSchool) }
        var editClass by remember { mutableStateOf(currentClass) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("edit_profile_dialog"),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Edit Student Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editClass,
                        onValueChange = { editClass = it },
                        label = { Text("Class / Stream (e.g. S.5 Blue)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_class_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editSchool,
                        onValueChange = { editSchool = it },
                        label = { Text("School Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_school_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                currentName = editName.trim()
                                currentSchool = editSchool.trim()
                                currentClass = editClass.trim()
                                onUpdateProfile(currentName, currentSchool, currentClass)
                                showEditProfileDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Profile updated successfully")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Settings Modal
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("settings_dialog"),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Revision Reminders",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Text(
                                text = "Get notified at 6:00 PM to keep your streak",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = studyRemindersEnabled,
                            onCheckedChange = { studyRemindersEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IndigoPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Auto-Caching",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Text(
                                text = "Save opened notes to local internal storage",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = offlineAutoCacheEnabled,
                            onCheckedChange = { offlineAutoCacheEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IndigoPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit details link
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showSettingsDialog = false
                                showEditProfileDialog = true
                            },
                        color = SoftLavenderGrey,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Profile",
                                tint = IndigoPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Student Profile Details",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IndigoPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showSettingsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    // Help & Support Modal
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "Help & Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Catch Up is the official revision platform aligned with the Uganda National Curriculum Development Centre (NCDC) syllabus.",
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Offline Notes: Cached notes are readable without internet data in the Downloads tab.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Subscription Renewal: Handled directly through your school administrator or school bursar.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Support Email: support@catchup.ug",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Logout Confirmation Modal
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out? Your downloaded notes and reading streak will remain safely stored on this device.",
                    fontSize = 13.sp,
                    color = TextDark,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged out. Welcome back, $currentName!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE76F51)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Badge Details Modal (Shown when clicking any badge in spotlight or grid)
    selectedBadgeDetail?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (badge.isUnlocked) Icons.Outlined.Check else Icons.Outlined.Lock,
                        contentDescription = if (badge.isUnlocked) "Unlocked" else "Locked",
                        tint = if (badge.isUnlocked) Color(0xFF2A9D8F) else IconGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badge.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MedalBadgeGraphic(
                        badge = badge,
                        sizeDp = 90.dp,
                        isLocked = !badge.isUnlocked,
                        alphaVal = if (badge.isUnlocked) 1f else 0.5f
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = badge.description,
                        fontSize = 13.sp,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (badge.isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "Unlocked Achievement",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Current Progress: ${badge.progressCurrent} / ${badge.progressMax} ${badge.progressUnit}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBadgeDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

/**
 * 2-Column Achievement Grid Card.
 *
 * Sizing:
 * - Width = (screen width - 40dp margin - 12dp gap) / 2
 * - Height: approximately 130dp
 * - Card style: white background, 1dp border #ECEAF5, 16dp corner radius, 12dp padding
 * - Unlocked: full color, full opacity
 * - Locked: 40% opacity, greyscale filter, small lock icon (16dp, #9C9C9C) overlaid top-right corner
 */
@Composable
fun AchievementGridCard(
    badge: AchievementBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFECEAF5), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Lock icon overlaid top-right corner if locked (16dp, #9C9C9C)
            if (!badge.isUnlocked) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Locked",
                    tint = IconGrey,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .testTag("badge_lock_${badge.id}")
                )
            }

            // Medal image and text label centered inside card
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MedalBadgeGraphic(
                    badge = badge,
                    sizeDp = 68.dp,
                    isLocked = !badge.isUnlocked,
                    alphaVal = if (badge.isUnlocked) 1f else 0.4f
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = badge.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("badge_name_${badge.id}")
                )
            }
        }
    }
}

/**
 * Account Row item (Height 52dp, full width, 1dp bottom border #F0F0F0 between rows).
 */
@Composable
private fun AccountRow(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean,
    showChevron: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: icon (20dp) + label text (14sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isDestructive) Color(0xFFE76F51) else TextDark,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) Color(0xFFE76F51) else TextDark
                )
            }

            // Right: chevron-right icon (18dp, #9C9C9C) if showChevron
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = "Navigate",
                    tint = IconGrey,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // 1dp bottom border #F0F0F0
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGrey)
        )
    }
}

/**
 * Medal Badge Graphic using actual drawable medal assets.
 *
 * Sizing & Display:
 * - Displays the actual medal illustration via painterResource(id = badge.drawableResId)
 * - Applies greyscale ColorFilter when isLocked = true
 * - Applies specified opacity (alphaVal)
 */
@Composable
fun MedalBadgeGraphic(
    badge: AchievementBadge,
    sizeDp: Dp,
    isLocked: Boolean,
    alphaVal: Float,
    modifier: Modifier = Modifier
) {
    val colorFilter = if (isLocked) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    } else {
        null
    }

    Image(
        painter = painterResource(id = badge.drawableResId),
        contentDescription = badge.name,
        colorFilter = colorFilter,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(sizeDp)
            .alpha(alphaVal)
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
