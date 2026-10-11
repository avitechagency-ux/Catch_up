package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.BottomNavBackground
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.PeachOrangeBorder
import com.example.ui.theme.PeachOrangeLight
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Navigation item data class for the Admin Bottom Navigation Bar.
 */
data class AdminNavItem(
    val label: String,
    val icon: ImageVector
)

/**
 * Full items set for the Admin Bottom Navigation Bar:
 * 1. "Home" — icon: Home
 * 2. "Analysis" — icon: BarChart
 * 3. "Users" — icon: People
 * 4. "More" — icon: Menu
 */
val AdminNavItems: List<AdminNavItem> = listOf(
    AdminNavItem("Home", Icons.Outlined.Home),
    AdminNavItem("Analysis", Icons.Outlined.BarChart),
    AdminNavItem("Users", Icons.Outlined.People),
    AdminNavItem("More", Icons.Outlined.Menu)
)

val DefaultAdminNavItems: List<AdminNavItem> = AdminNavItems

/**
 * Admin-specific Bottom Navigation Bar composable.
 * - Matches the visual styling of student/teacher BottomNavBar components across the entire platform
 * - Background: Peach Orange (#FF7A53)
 * - 64dp height with navigation bar inset padding
 * - 1dp top border (#FFAB91) with soft elevation shadow
 * - Active tab styled as a clean white pill indicator with deep indigo (#4B3F8F) icon and bold label
 * - Inactive tabs styled with high-contrast white outline icon + label (Color.White with 88% opacity)
 * - 4 items: Home, Analysis, Users, More
 */
@Composable
fun AdminBottomNavBar(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    items: List<AdminNavItem> = AdminNavItems
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
            .testTag("admin_bottom_navigation_bar"),
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
                val itemTestTag = "admin_nav_item_${item.label.lowercase().replace(" ", "_")}"

                if (isSelected) {
                    // Active Item: Pill shape with white background & indigo theme accent
                    Row(
                        modifier = Modifier
                            .animateContentSize()
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
                    // Inactive Item: High-contrast white icon + label below
                    Column(
                        modifier = Modifier
                            .animateContentSize()
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

/**
 * Backward compatibility alias for AdminBottomNavigationBar.
 */
@Composable
fun AdminBottomNavigationBar(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    items: List<AdminNavItem> = AdminNavItems
) {
    AdminBottomNavBar(
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        items = items
    )
}

/**
 * Admin Home Screen for the Catch Up app.
 * A dedicated dashboard for administrator/owner access only.
 */
@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    revenueBalance: String = "0",
    studentsCount: String = "0",
    teachersCount: String = "0",
    notesCount: String = "0",
    assignmentsCount: String = "0",
    affiliatesCount: String = "0",
    onNavigateToPlans: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToAffiliate: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNotificationClick: (() -> Unit)? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {},
    onSwitchToStudentView: () -> Unit = {},
    onSwitchToTeacherView: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showRoleMenu by remember { mutableStateOf(false) }

    var liveRevenueBalance by remember(revenueBalance) { mutableStateOf(revenueBalance) }
    var liveStudentsCount by remember(studentsCount) { mutableStateOf(studentsCount) }
    var liveTeachersCount by remember(teachersCount) { mutableStateOf(teachersCount) }
    var liveNotesCount by remember(notesCount) { mutableStateOf(notesCount) }
    var liveAssignmentsCount by remember(assignmentsCount) { mutableStateOf(assignmentsCount) }
    var liveAffiliatesCount by remember(affiliatesCount) { mutableStateOf(affiliatesCount) }
    var isSyncingBalance by remember { mutableStateOf(false) }

    suspend fun syncOverviewStats() {
        isSyncingBalance = true
        val stats = com.example.data.SupabaseRepository.fetchAdminOverviewStats()
        if (stats != null) {
            // Live MarzPay balance synced with Admin Homepage Balance card
            liveRevenueBalance = if (stats.formattedMarzpayBalance != "0") stats.formattedMarzpayBalance else stats.formattedRevenue
            liveStudentsCount = stats.studentsCount.toString()
            liveTeachersCount = stats.teachersCount.toString()
            liveNotesCount = stats.notesCount.toString()
            liveAssignmentsCount = stats.assignmentsCount.toString()
            liveAffiliatesCount = stats.affiliatesCount.toString()
        }
        isSyncingBalance = false
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        syncOverviewStats()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_home_screen"),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToAnalysis()
                        2 -> onNavigateToUsers()
                        3 -> onNavigateToMore()
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
            // HEADER
            // - Solid purple background (#4B3F8F), rounded bottom corners (28dp)
            // - Top row: "Admin" text (bold white, 20sp), notification bell (24dp white)
            // - Below it (8dp gap): "Welcome back" text (14sp, Regular, #D6D0F0)
            // ==========================================
            Box(
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
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 36.dp)
                    .testTag("admin_header")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top row: "Admin" text on left, notification bell on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Admin",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("admin_header_title")
                        )

                        IconButton(
                            onClick = {
                                if (onNotificationClick != null) {
                                    onNotificationClick()
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("No new admin notifications")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("admin_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Admin Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // "Welcome back" text
                    Text(
                        text = "Welcome back",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        modifier = Modifier.testTag("admin_welcome_text")
                    )
                }
            }

            // ==========================================
            // WHITE BODY
            // - Rounded top corners (28dp), overlapping header (-14dp offset)
            // - 20dp left/right padding, 24dp top padding, 24dp spacing between sections
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-14).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 32.dp)
                        .testTag("admin_white_body")
                ) {
                    // ------------------------------------------
                    // SECTION 1 — Revenue
                    // ------------------------------------------
                    Text(
                        text = "Revenue",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        modifier = Modifier.testTag("admin_revenue_label")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = IndigoPurple.copy(alpha = 0.35f),
                                spotColor = Color(0x33000000)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onNavigateToAccount)
                            .testTag("admin_revenue_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = IndigoPurple
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(IndigoPurple, Color(0xFF5B4EB1))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.20f),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                                    contentDescription = "Revenue Wallet",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Balance",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = LightLavender,
                                                modifier = Modifier.testTag("admin_revenue_balance_label")
                                            )
                                            Text(
                                                text = "MarzPay Live Sync",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.White.copy(alpha = 0.80f)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.20f),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable {
                                                    coroutineScope.launch {
                                                        syncOverviewStats()
                                                        snackbarHostState.showSnackbar("MarzPay balance synced: $liveRevenueBalance")
                                                    }
                                                }
                                                .testTag("admin_sync_marzpay_button")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Refresh,
                                                    contentDescription = "Sync MarzPay Balance",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.20f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.TrendingUp,
                                                    contentDescription = "Growth",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isSyncingBalance) "Syncing..." else "MarzPay",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = liveRevenueBalance,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.testTag("admin_revenue_balance_text")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ------------------------------------------
                    // SECTION 2 — Students & Teachers (Colored Cards)
                    // ------------------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_students_teachers_row"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Students (Vibrant Blue Card)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = BlueTile.copy(alpha = 0.30f),
                                    spotColor = Color(0x22000000)
                                )
                                .testTag("admin_stat_card_students"),
                            shape = RoundedCornerShape(16.dp),
                            color = BlueTile
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.School,
                                            contentDescription = "Students",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = liveStudentsCount,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("admin_students_count")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Students",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f),
                                    modifier = Modifier.testTag("admin_students_label")
                                )
                            }
                        }

                        // Card 2: Teachers (Vibrant Coral Card)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = CoralTile.copy(alpha = 0.30f),
                                    spotColor = Color(0x22000000)
                                )
                                .testTag("admin_stat_card_teachers"),
                            shape = RoundedCornerShape(16.dp),
                            color = CoralTile
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = "Teachers",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = liveTeachersCount,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("admin_teachers_count")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Teachers",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f),
                                    modifier = Modifier.testTag("admin_teachers_label")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ------------------------------------------
                    // SECTION 3 — Content & Affiliates (Colored Cards)
                    // ------------------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_content_affiliates_row"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Notes (Vibrant Teal Card)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = TealTile.copy(alpha = 0.30f),
                                    spotColor = Color(0x22000000)
                                )
                                .testTag("admin_stat_card_notes"),
                            shape = RoundedCornerShape(16.dp),
                            color = TealTile
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = "Notes",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = liveNotesCount,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("admin_notes_count")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Notes",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f),
                                    modifier = Modifier.testTag("admin_notes_label")
                                )
                            }
                        }

                        // Card 2: Assignments (Vibrant Navy Card)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = NavyTile.copy(alpha = 0.30f),
                                    spotColor = Color(0x22000000)
                                )
                                .testTag("admin_stat_card_assignments"),
                            shape = RoundedCornerShape(16.dp),
                            color = NavyTile
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Assignment,
                                            contentDescription = "Assignments",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = liveAssignmentsCount,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("admin_assignments_count")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Assignments",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f),
                                    modifier = Modifier.testTag("admin_assignments_label")
                                )
                            }
                        }

                        // Card 3: Affiliates (Vibrant Purple Card)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = PurpleTile.copy(alpha = 0.30f),
                                    spotColor = Color(0x22000000)
                                )
                                .clickable { onNavigateToAffiliate() }
                                .testTag("admin_stat_card_affiliates"),
                            shape = RoundedCornerShape(16.dp),
                            color = PurpleTile
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Affiliates",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = liveAffiliatesCount,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("admin_affiliates_count")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Affiliates",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f),
                                    modifier = Modifier.testTag("admin_affiliates_label")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ------------------------------------------
                    // SECTION 4 — Plans & Transactions (Colored Pill Buttons)
                    // Filled pill shapes, 52dp height, bold white text & icons (NOT white cards)
                    // ------------------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_plans_transactions_row"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Plans Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(26.dp),
                                    ambientColor = IndigoPurple.copy(alpha = 0.35f),
                                    spotColor = Color(0x22000000)
                                )
                                .testTag("admin_plans_button"),
                            shape = RoundedCornerShape(26.dp),
                            color = IndigoPurple,
                            onClick = onNavigateToPlans
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = "Plans Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Plans",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = "Open Plans",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Transactions Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(26.dp),
                                    ambientColor = NavyTile.copy(alpha = 0.35f),
                                    spotColor = Color(0x22000000)
                                )
                                .testTag("admin_transactions_button"),
                            shape = RoundedCornerShape(26.dp),
                            color = NavyTile,
                            onClick = onNavigateToTransactions
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = "Transactions Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Transactions",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = "Open Transactions",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AdminHomeScreenPreview() {
    MyApplicationTheme(dynamicColor = false) {
        AdminHomeScreen()
    }
}

/**
 * Placeholder screen for future Admin sub-screens (Analysis, Users, More).
 * Allows full navigation across the admin bottom navigation bar while screens are being built.
 */
@Composable
fun AdminPlaceholderScreen(
    title: String,
    subtitle: String,
    selectedIndex: Int,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {},
    onSwitchToStudentView: () -> Unit = {},
    onSwitchToTeacherView: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_placeholder_screen_${title.lowercase().replace(" ", "_")}"),
        containerColor = Color.White,
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToAnalysis()
                        2 -> onNavigateToUsers()
                        3 -> onNavigateToMore()
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
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
                    .testTag("admin_placeholder_header")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("admin_placeholder_title")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        modifier = Modifier.testTag("admin_placeholder_subtitle")
                    )
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("admin_placeholder_icon_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = PeachOrangeLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (selectedIndex) {
                                1 -> Icons.Outlined.BarChart
                                2 -> Icons.Outlined.People
                                3 -> Icons.Outlined.Menu
                                else -> Icons.Outlined.Home
                            },
                            contentDescription = title,
                            tint = PeachOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "$title Screen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.testTag("admin_placeholder_heading")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "This screen will be built next.\nUse the bottom navigation bar to switch between screens.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("admin_placeholder_description")
                )
            }
        }
    }
}
