package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TrackPurple
import kotlin.math.abs
import kotlin.math.roundToInt

private val AnalysisGold = Color(0xFFFFB300)
private val AnalysisBrightGold = Color(0xFFFFD54F)
private val AnalysisDeepIndigo = Color(0xFF362B6E)

/**
 * 3-Segment Time Range enum used for analysis filters.
 */
enum class AnalysisTimeRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

/**
 * Data model for a single data point in an analysis chart.
 */
data class AnalysisDataPoint(
    val label: String,
    val value: Float,
    val formattedValue: String
)

/**
 * Data model for configuring an analysis line chart.
 */
data class AnalysisChartData(
    val title: String,
    val subtitle: String,
    val currentFormattedValue: String,
    val growthRate: String,
    val points: List<AnalysisDataPoint>,
    val yMin: Float = 0f,
    val yMax: Float,
    val yAxisLabels: List<String>,
    val icon: ImageVector = Icons.Outlined.AccountBalanceWallet,
    val primaryColor: Color = IndigoPurple,
    val secondaryColor: Color = Color(0xFF5B4EB1)
)

/**
 * Data point model for stacked user bar charts (Students + Teachers).
 */
data class StackedUserBarDataPoint(
    val label: String,
    val students: Int,
    val teachers: Int
) {
    val total: Int get() = students + teachers
}

/**
 * Data container for stacked user bar charts over a time range.
 */
data class StackedUsersChartData(
    val title: String,
    val subtitle: String,
    val totalUsersFormatted: String,
    val growthRate: String,
    val points: List<StackedUserBarDataPoint>,
    val yMax: Int,
    val yAxisLabels: List<String>
)

/**
 * Data model representing a ranked affiliate.
 */
data class AffiliateRankingItem(
    val id: String,
    val rank: Int,
    val name: String,
    val referralCount: Int,
    val earningsFormatted: String
)

// ==========================================
// DEFAULT REVENUE DATA SETS
// ==========================================
val WeekRevenueChartData = AnalysisChartData(
    title = "Weekly Revenue",
    subtitle = "Past 7 Days",
    currentFormattedValue = "UGX 1,450,000",
    growthRate = "+18.2%",
    points = listOf(
        AnalysisDataPoint("Mon", 180f, "UGX 180K"),
        AnalysisDataPoint("Tue", 210f, "UGX 210K"),
        AnalysisDataPoint("Wed", 190f, "UGX 190K"),
        AnalysisDataPoint("Thu", 240f, "UGX 240K"),
        AnalysisDataPoint("Fri", 290f, "UGX 290K"),
        AnalysisDataPoint("Sat", 160f, "UGX 160K"),
        AnalysisDataPoint("Sun", 180f, "UGX 180K")
    ),
    yMin = 0f,
    yMax = 350f,
    yAxisLabels = listOf("350k", "250k", "150k", "50k", "0")
)

val DefaultRevenueChartData = AnalysisChartData(
    title = "Total Revenue",
    subtitle = "May – Sep 2026 Monthly Breakdown",
    currentFormattedValue = "UGX 5,867,000",
    growthRate = "+24.5%",
    points = listOf(
        AnalysisDataPoint(label = "May", value = 1.8f, formattedValue = "UGX 1.8M"),
        AnalysisDataPoint(label = "Jun", value = 2.7f, formattedValue = "UGX 2.7M"),
        AnalysisDataPoint(label = "Jul", value = 3.6f, formattedValue = "UGX 3.6M"),
        AnalysisDataPoint(label = "Aug", value = 4.8f, formattedValue = "UGX 4.8M"),
        AnalysisDataPoint(label = "Sep", value = 5.867f, formattedValue = "UGX 5.86M")
    ),
    yMin = 0f,
    yMax = 6.5f,
    yAxisLabels = listOf("6M", "4.5M", "3M", "1.5M", "0"),
    icon = Icons.Outlined.AccountBalanceWallet,
    primaryColor = IndigoPurple,
    secondaryColor = Color(0xFF5B4EB1)
)

val YearRevenueChartData = AnalysisChartData(
    title = "Annual Revenue",
    subtitle = "2023 – 2026 Trend",
    currentFormattedValue = "UGX 52,300,000",
    growthRate = "+42.0%",
    points = listOf(
        AnalysisDataPoint("2023", 12f, "UGX 12M"),
        AnalysisDataPoint("2024", 24f, "UGX 24M"),
        AnalysisDataPoint("2025", 38f, "UGX 38M"),
        AnalysisDataPoint("2026", 52.3f, "UGX 52.3M")
    ),
    yMin = 0f,
    yMax = 60f,
    yAxisLabels = listOf("60M", "45M", "30M", "15M", "0")
)

// ==========================================
// DEFAULT USERS DATA SETS (STACKED BARS)
// ==========================================
val WeekUsersChartData = StackedUsersChartData(
    title = "Active Users",
    subtitle = "Past 7 Days",
    totalUsersFormatted = "271 Users",
    growthRate = "+14.2%",
    points = listOf(
        StackedUserBarDataPoint("Mon", 28, 6),
        StackedUserBarDataPoint("Tue", 35, 8),
        StackedUserBarDataPoint("Wed", 42, 7),
        StackedUserBarDataPoint("Thu", 38, 9),
        StackedUserBarDataPoint("Fri", 45, 10),
        StackedUserBarDataPoint("Sat", 20, 4),
        StackedUserBarDataPoint("Sun", 16, 3)
    ),
    yMax = 60,
    yAxisLabels = listOf("60", "45", "30", "15", "0")
)

val MonthUsersChartData = StackedUsersChartData(
    title = "Active Users",
    subtitle = "Jan – Apr 2026 Platform Growth",
    totalUsersFormatted = "1,050 Users",
    growthRate = "+32.8%",
    points = listOf(
        StackedUserBarDataPoint("Jan", 210, 40),
        StackedUserBarDataPoint("Feb", 410, 70),
        StackedUserBarDataPoint("March", 640, 120),
        StackedUserBarDataPoint("Apr", 880, 170)
    ),
    yMax = 1200,
    yAxisLabels = listOf("1.2k", "900", "600", "300", "0")
)

val DefaultUsersChartData = AnalysisChartData(
    title = "Active Users",
    subtitle = "Jan – Apr 2026 Platform Growth",
    currentFormattedValue = "1,050",
    growthRate = "+32.8%",
    points = listOf(
        AnalysisDataPoint(label = "Jan", value = 250f, formattedValue = "250 Users"),
        AnalysisDataPoint(label = "Feb", value = 480f, formattedValue = "480 Users"),
        AnalysisDataPoint(label = "March", value = 760f, formattedValue = "760 Users"),
        AnalysisDataPoint(label = "Apr", value = 1050f, formattedValue = "1,050 Users")
    ),
    yMin = 0f,
    yMax = 1250f,
    yAxisLabels = listOf("1.2k", "900", "600", "300", "0"),
    icon = Icons.Outlined.People,
    primaryColor = IndigoPurple,
    secondaryColor = Color(0xFF5B4EB1)
)

val YearUsersChartData = StackedUsersChartData(
    title = "Active Users",
    subtitle = "2023 – 2026 Total Growth",
    totalUsersFormatted = "8,400 Users",
    growthRate = "+48.5%",
    points = listOf(
        StackedUserBarDataPoint("2023", 1800, 350),
        StackedUserBarDataPoint("2024", 3600, 650),
        StackedUserBarDataPoint("2025", 5400, 900),
        StackedUserBarDataPoint("2026", 7200, 1200)
    ),
    yMax = 10000,
    yAxisLabels = listOf("10k", "7.5k", "5k", "2.5k", "0")
)

// ==========================================
// DEFAULT TOP AFFILIATES (RANKED)
// ==========================================
val DefaultTopAffiliates = listOf(
    AffiliateRankingItem("aff-1", 1, "Moses", 142, "UGX 142,000"),
    AffiliateRankingItem("aff-2", 2, "Mary", 118, "UGX 118,000"),
    AffiliateRankingItem("aff-3", 3, "John", 95, "UGX 95,000"),
    AffiliateRankingItem("aff-4", 4, "Martin", 74, "UGX 74,000"),
    AffiliateRankingItem("aff-5", 5, "May", 62, "UGX 62,000")
)

/**
 * Admin Analysis Screen.
 * Designer-crafted analytics dashboard with rich color hierarchy,
 * interactive Revenue curve, Students vs Teachers breakdown tiles & stacked bars,
 * and ranked Top Affiliates leaderboard.
 */
@Composable
fun AdminAnalysisScreen(
    modifier: Modifier = Modifier,
    revenueData: AnalysisChartData = DefaultRevenueChartData,
    usersData: AnalysisChartData = DefaultUsersChartData,
    affiliatesList: List<AffiliateRankingItem> = DefaultTopAffiliates,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Independent time-range state for Section 1 (Revenue)
    var revenueTimeRange by remember { mutableStateOf(AnalysisTimeRange.MONTH) }

    // Independent time-range state for Section 2 (Users)
    var usersTimeRange by remember { mutableStateOf(AnalysisTimeRange.MONTH) }

    val activeRevenueData = when (revenueTimeRange) {
        AnalysisTimeRange.WEEK -> WeekRevenueChartData
        AnalysisTimeRange.MONTH -> revenueData
        AnalysisTimeRange.YEAR -> YearRevenueChartData
    }

    val activeUsersData = when (usersTimeRange) {
        AnalysisTimeRange.WEEK -> WeekUsersChartData
        AnalysisTimeRange.MONTH -> MonthUsersChartData
        AnalysisTimeRange.YEAR -> YearUsersChartData
    }

    val totalReferrals = remember(affiliatesList) {
        affiliatesList.sumOf { it.referralCount }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_analysis_screen"),
        containerColor = Color(0xFFF8F7FC),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = 1,
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
            // 1. DESIGNER PURPLE HEADER + COLORED KPI STRIP
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
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
                    .testTag("admin_analysis_header")
            ) {
                // Top Bar Row: Back Button + Centered "Analysis" Title + Live Pill
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .testTag("admin_analysis_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Analysis",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("admin_analysis_title")
                    )

                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        shape = RoundedCornerShape(12.dp),
                        color = AnalysisGold.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, AnalysisBrightGold.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AnalysisBrightGold)
                            )
                            Text(
                                text = "2026",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AnalysisBrightGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3-Column Colorful KPI Highlight Tiles (Matching Student & Admin Home Palette)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: Revenue Growth (Vibrant Teal Tile)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = TealTile
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "REVENUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.80f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeRevenueData.growthRate,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = revenueTimeRange.label + " trend",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1
                            )
                        }
                    }

                    // KPI 2: Users Growth (Vibrant Blue Tile)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = BlueTile
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "USERS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.80f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeUsersData.growthRate,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = activeUsersData.totalUsersFormatted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1
                            )
                        }
                    }

                    // KPI 3: Affiliates Invites (Vibrant Coral Orange Tile)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = PeachOrange
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "INVITES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalReferrals",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "Top 5 total",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.90f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. MAIN ANALYTICS CONTENT
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                // ------------------------------------------
                // SECTION 1: REVENUE (Line Chart)
                // ------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(IndigoPurple)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Revenue",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            modifier = Modifier.testTag("admin_analysis_revenue_section_label")
                        )
                    }

                    Text(
                        text = "Tap points to inspect",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time-range toggle (Week / Month / Year)
                AnalysisSegmentedToggle(
                    selectedRange = revenueTimeRange,
                    onSelectRange = { revenueTimeRange = it },
                    testTagPrefix = "revenue",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Revenue Line Chart Card
                RevenueLineChartCard(
                    chartData = activeRevenueData,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ------------------------------------------
                // SECTION 2: USERS (Stacked Bar Chart)
                // ------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(BlueTile)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Users",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            modifier = Modifier.testTag("admin_analysis_users_section_label")
                        )
                    }

                    Text(
                        text = "Students & Teachers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time-range toggle (Week / Month / Year) - independent state
                AnalysisSegmentedToggle(
                    selectedRange = usersTimeRange,
                    onSelectRange = { usersTimeRange = it },
                    testTagPrefix = "users",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stacked Bar Chart Card (Students + Teachers)
                UsersStackedBarChartCard(
                    chartData = activeUsersData,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ------------------------------------------
                // SECTION 3: TOP AFFILIATES (Ranked Horizontal Bar Chart)
                // ------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PeachOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top Affiliates",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            modifier = Modifier.testTag("admin_analysis_affiliates_section_label")
                        )
                    }

                    Text(
                        text = "Top 5 leaderboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Top Affiliates Ranked Horizontal Bar Chart Card
                AffiliatesHorizontalBarChartCard(
                    affiliates = affiliatesList,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * 3-Segment Time-Range Pill Control (Week / Month / Year).
 */
@Composable
fun AnalysisSegmentedToggle(
    selectedRange: AnalysisTimeRange,
    onSelectRange: (AnalysisTimeRange) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .testTag("admin_${testTagPrefix}_toggle_container"),
        shape = RoundedCornerShape(23.dp),
        color = Color(0xFFEDEAF6),
        border = BorderStroke(1.dp, IndigoPurple.copy(alpha = 0.12f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnalysisTimeRange.values().forEach { range ->
                val isActive = selectedRange == range
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(19.dp))
                        .background(if (isActive) IndigoPurple else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectRange(range) }
                        .testTag("admin_${testTagPrefix}_toggle_${range.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.label,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isActive) Color.White else IndigoPurple.copy(alpha = 0.70f)
                    )
                }
            }
        }
    }
}

// ==========================================
// SECTION 1: REVENUE LINE CHART CARD
// ==========================================
@Composable
fun RevenueLineChartCard(
    chartData: AnalysisChartData,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(chartData) {
        mutableIntStateOf(chartData.points.lastIndex)
    }

    val selectedPoint = chartData.points.getOrNull(selectedIndex) ?: chartData.points.last()
    val peakPoint = remember(chartData) {
        chartData.points.maxByOrNull { it.value } ?: chartData.points.last()
    }

    Surface(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.40f),
                spotColor = Color(0x33000000)
            )
            .testTag("admin_analysis_revenue_card"),
        shape = RoundedCornerShape(22.dp),
        color = IndigoPurple
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(IndigoPurple, AnalysisDeepIndigo)
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header inside the card: Gold Icon Badge + Title/Subtitle + Growth Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AnalysisGold,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Revenue",
                                    tint = TextDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = chartData.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = chartData.subtitle,
                                fontSize = 12.sp,
                                color = LightLavender
                            )
                        }
                    }

                    // Growth badge pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TealTile
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                contentDescription = "Trend",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = chartData.growthRate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Revenue Figure + Gold Selected Point Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chartData.currentFormattedValue,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.testTag("admin_analysis_revenue_total_text")
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AnalysisGold
                    ) {
                        Text(
                            text = "${selectedPoint.label} · ${selectedPoint.formattedValue}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Line Chart Area with Y-axis & X-axis
                AnalysisLineChartPlot(
                    chartData = chartData,
                    selectedIndex = selectedIndex,
                    onPointSelected = { selectedIndex = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .testTag("admin_analysis_revenue_chart")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Quick Insight Strip inside Revenue Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.10f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = "Selected Point",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = LightLavender
                            )
                            Text(
                                text = "${selectedPoint.label} (${selectedPoint.formattedValue})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.10f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = "Peak Period",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = LightLavender
                            )
                            Text(
                                text = "${peakPoint.label} (${peakPoint.formattedValue})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AnalysisBrightGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom line chart plot using Compose Canvas with smooth curves,
 * gold gradient fill, dashed vertical guide line, and interactive nodes.
 */
@Composable
fun AnalysisLineChartPlot(
    chartData: AnalysisChartData,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val points = chartData.points
    val count = points.size

    Row(modifier = modifier) {
        // Y-axis labels column
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .padding(top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            chartData.yAxisLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightLavender,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Main Chart Canvas + X-Axis Row
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (count > 1) {
                                    val insetX = 14.dp.toPx()
                                    val usableWidth = size.width - (2 * insetX)
                                    val step = usableWidth / (count - 1)

                                    var closestIndex = 0
                                    var minDistance = Float.MAX_VALUE

                                    for (i in 0 until count) {
                                        val x = insetX + (i * step)
                                        val distance = abs(offset.x - x)
                                        if (distance < minDistance) {
                                            minDistance = distance
                                            closestIndex = i
                                        }
                                    }
                                    onPointSelected(closestIndex)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val insetX = 14.dp.toPx()
                    val insetY = 12.dp.toPx()
                    val usableWidth = width - (2 * insetX)
                    val usableHeight = height - (2 * insetY)

                    // Draw 4 horizontal grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = insetY + (i * (usableHeight / gridSteps))
                        drawLine(
                            color = Color.White.copy(alpha = 0.14f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (count > 0) {
                        val yRange = (chartData.yMax - chartData.yMin).coerceAtLeast(0.001f)
                        val stepX = if (count > 1) usableWidth / (count - 1) else 0f

                        val coords = points.mapIndexed { index, point ->
                            val x = insetX + (index * stepX)
                            val normalizedY = ((point.value - chartData.yMin) / yRange).coerceIn(0f, 1f)
                            val y = insetY + ((1f - normalizedY) * usableHeight)
                            Offset(x, y)
                        }

                        // Draw vertical dashed guide line at selected coordinate
                        val selectedCoord = coords.getOrNull(selectedIndex)
                        if (selectedCoord != null) {
                            drawLine(
                                color = AnalysisBrightGold.copy(alpha = 0.55f),
                                start = Offset(selectedCoord.x, insetY),
                                end = Offset(selectedCoord.x, height - insetY),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }

                        // Build smooth curve path
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 1 until coords.size) {
                                val prev = coords[i - 1]
                                val curr = coords[i]
                                val controlX1 = prev.x + (curr.x - prev.x) / 2f
                                val controlY1 = prev.y
                                val controlX2 = prev.x + (curr.x - prev.x) / 2f
                                val controlY2 = curr.y
                                cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                            }
                        }

                        // Rich Gold gradient fill beneath curve
                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(coords.last().x, height - insetY)
                            lineTo(coords.first().x, height - insetY)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AnalysisBrightGold.copy(alpha = 0.38f),
                                    AnalysisBrightGold.copy(alpha = 0.02f)
                                ),
                                startY = coords.minOf { it.y },
                                endY = height - insetY
                            )
                        )

                        // Draw bright gold line stroke
                        drawPath(
                            path = linePath,
                            color = AnalysisBrightGold,
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw point circles
                        coords.forEachIndexed { index, coord ->
                            val isSelected = index == selectedIndex
                            if (isSelected) {
                                drawCircle(
                                    color = AnalysisBrightGold.copy(alpha = 0.35f),
                                    radius = 10.dp.toPx(),
                                    center = coord
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = coord
                                )
                                drawCircle(
                                    color = IndigoPurple,
                                    radius = 3.dp.toPx(),
                                    center = coord
                                )
                            } else {
                                drawCircle(
                                    color = AnalysisBrightGold,
                                    radius = 4.dp.toPx(),
                                    center = coord
                                )
                            }
                        }
                    }
                }
            }

            // X-Axis labels with active pill highlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                points.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AnalysisGold else Color.Transparent,
                        modifier = Modifier.clickable { onPointSelected(index) }
                    ) {
                        Text(
                            text = point.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) TextDark else LightLavender,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION 2: USERS STACKED BAR CHART CARD
// ==========================================
@Composable
fun UsersStackedBarChartCard(
    chartData: StackedUsersChartData,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(chartData) {
        mutableIntStateOf(chartData.points.lastIndex)
    }

    val selectedPoint = chartData.points.getOrNull(selectedIndex) ?: chartData.points.last()
    val totalForSelected = selectedPoint.total.coerceAtLeast(1)
    val studentPct = ((selectedPoint.students.toFloat() / totalForSelected) * 100f).roundToInt()
    val teacherPct = 100 - studentPct

    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.18f),
                spotColor = Color(0x22000000)
            )
            .border(1.dp, IndigoPurple.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .testTag("admin_analysis_users_card"),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Blue Icon Badge + Title + Growth Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BlueTile,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.People,
                                contentDescription = "Users",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = chartData.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = chartData.subtitle,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                // Growth pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TealTile
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                            contentDescription = "Trend",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chartData.growthRate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total readout & Active selection pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chartData.totalUsersFormatted,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.testTag("admin_analysis_users_total_text")
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IndigoPurple
                ) {
                    Text(
                        text = "${selectedPoint.label} · ${selectedPoint.total} Total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // COLORFUL LEGEND & ROLE BREAKDOWN CARDS (Students vs Teachers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_analysis_users_legend"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Students Vibrant Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = IndigoPurple
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Students",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LightLavender
                                )
                                Text(
                                    text = "${selectedPoint.students}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "$studentPct%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AnalysisBrightGold
                        )
                    }
                }

                // Teachers Vibrant Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = CoralTile
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.20f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Teachers",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                                Text(
                                    text = "${selectedPoint.teachers}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "$teacherPct%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked Bar Canvas Plot inside a soft tinted container
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SoftLavenderGrey,
                modifier = Modifier.fillMaxWidth()
            ) {
                StackedUsersBarPlot(
                    chartData = chartData,
                    selectedIndex = selectedIndex,
                    onPointSelected = { selectedIndex = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                        .padding(start = 8.dp, end = 10.dp, top = 12.dp, bottom = 8.dp)
                        .testTag("admin_analysis_users_chart")
                )
            }
        }
    }
}

/**
 * Custom Stacked Bar Plot on Compose Canvas with separated rounded segments.
 */
@Composable
fun StackedUsersBarPlot(
    chartData: StackedUsersChartData,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val points = chartData.points
    val count = points.size

    Row(modifier = modifier) {
        // Y-axis labels column
        Column(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
                .padding(top = 4.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            chartData.yAxisLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Stacked Bar Plot Canvas + X-Axis Row
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (count > 0) {
                                    val slotWidth = size.width / count
                                    val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                    onPointSelected(clickedIndex)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val insetY = 10.dp.toPx()
                    val usableHeight = height - (2 * insetY)

                    // Draw 4 subtle horizontal grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = insetY + (i * (usableHeight / gridSteps))
                        drawLine(
                            color = IndigoPurple.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (count > 0) {
                        val slotWidth = width / count
                        val barWidth = (slotWidth * 0.46f).coerceIn(16.dp.toPx(), 32.dp.toPx())
                        val yMaxFloat = chartData.yMax.toFloat().coerceAtLeast(1f)
                        val segmentGap = 3.dp.toPx()

                        points.forEachIndexed { index, item ->
                            val isSelected = index == selectedIndex
                            val centerX = (index * slotWidth) + (slotWidth / 2f)
                            val left = centerX - (barWidth / 2f)

                            val studentHeight = ((item.students.toFloat() / yMaxFloat) * usableHeight).coerceAtLeast(4.dp.toPx())
                            val teacherHeight = ((item.teachers.toFloat() / yMaxFloat) * usableHeight).coerceAtLeast(4.dp.toPx())

                            val bottomY = height - insetY
                            val studentsTopY = bottomY - studentHeight
                            val teachersTopY = (studentsTopY - segmentGap - teacherHeight).coerceAtLeast(insetY)

                            // Background column pill highlight for selected bar
                            if (isSelected) {
                                drawRoundRect(
                                    color = IndigoPurple.copy(alpha = 0.10f),
                                    topLeft = Offset(centerX - (slotWidth * 0.42f), insetY),
                                    size = Size(slotWidth * 0.84f, usableHeight),
                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                )
                            }

                            // 1. Bottom Segment: Students (IndigoPurple)
                            drawRoundRect(
                                color = if (isSelected) IndigoPurple else IndigoPurple.copy(alpha = 0.78f),
                                topLeft = Offset(left, studentsTopY),
                                size = Size(barWidth, studentHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            // 2. Top Segment: Teachers (CoralTile)
                            drawRoundRect(
                                color = if (isSelected) CoralTile else CoralTile.copy(alpha = 0.82f),
                                topLeft = Offset(left, teachersTopY),
                                size = Size(barWidth, teacherHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }
            }

            // X-Axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                points.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = point.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) IndigoPurple else TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPointSelected(index) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SECTION 3: TOP AFFILIATES (RANKED HORIZONTAL BAR CHART)
// ==========================================
@Composable
fun AffiliatesHorizontalBarChartCard(
    affiliates: List<AffiliateRankingItem>,
    modifier: Modifier = Modifier
) {
    val maxReferrals = (affiliates.maxOfOrNull { it.referralCount } ?: 100).coerceAtLeast(1)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.18f),
                spotColor = Color(0x20000000)
            )
            .border(1.dp, IndigoPurple.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .testTag("admin_analysis_affiliates_card"),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PeachOrange,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Referral Leaders",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Top 5 earners ranked by total invites",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IndigoPurple
                ) {
                    Text(
                        text = "Ranked",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ranked List of Horizontal Bars
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_analysis_affiliates_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                affiliates.forEach { affiliate ->
                    AffiliateRankRow(
                        item = affiliate,
                        maxReferrals = maxReferrals
                    )
                }
            }
        }
    }
}

/**
 * Single Row for a Ranked Affiliate in the Horizontal Bar Chart.
 */
@Composable
private fun AffiliateRankRow(
    item: AffiliateRankingItem,
    maxReferrals: Int,
    modifier: Modifier = Modifier
) {
    val fraction = (item.referralCount.toFloat() / maxReferrals).coerceIn(0.08f, 1f)
    val accentColor = when (item.rank) {
        1 -> PeachOrange
        2 -> TealTile
        3 -> BlueTile
        4 -> PurpleTile
        else -> IndigoPurple
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_affiliate_row_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        color = SoftLavenderGrey,
        border = BorderStroke(
            width = 1.dp,
            color = if (item.rank == 1) PeachOrange.copy(alpha = 0.35f) else BorderGrey
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top label row: Rank badge + Name + Earnings badge on left, Referral count on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank badge
                    Surface(
                        shape = CircleShape,
                        color = when (item.rank) {
                            1 -> AnalysisGold
                            2 -> TealTile
                            3 -> PeachOrange
                            else -> IndigoPurple
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${item.rank}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.rank == 1) TextDark else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = item.earningsFormatted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Referral Count at end
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.referralCount}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IndigoPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "referrals",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Bar Track & Filled Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(TrackPurple)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    IndigoPurple,
                                    accentColor
                                )
                            )
                        )
                )
            }
        }
    }
}
