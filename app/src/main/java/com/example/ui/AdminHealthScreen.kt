package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.NoteCountGreen
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean, card-based Platform Health monitoring screen for Administrators.
 * Displays essential numeric metrics only:
 * 1. Platform Health
 * 2. Storage Used
 * 3. API Connection Status
 * 4. Database Connection
 */
@Composable
fun AdminHealthScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    // Essential live monitoring numbers
    var platformUptime by remember { mutableStateOf("99.9%") }
    var storageUsedGb by remember { mutableFloatStateOf(42.6f) }
    val storageTotalGb = 100f
    val storageFreeGb = ((storageTotalGb - storageUsedGb) * 10).toInt() / 10f
    val storagePercent = (storageUsedGb / storageTotalGb * 100).toInt()

    var apiActiveCount by remember { mutableStateOf("4 / 4") }
    var apiLatencyMs by remember { mutableIntStateOf(38) }
    var apiSuccessRate by remember { mutableStateOf("99.9%") }
    var apiErrorsCount by remember { mutableIntStateOf(0) }

    var dbConnections by remember { mutableIntStateOf(18) }
    var dbLatencyMs by remember { mutableIntStateOf(12) }
    var dbRecordsCount by remember { mutableStateOf("4,190") }
    var dbSyncRate by remember { mutableStateOf("100%") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_health_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .navigationBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // ==========================================
            // HEADER: Clean purple curved banner
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
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 34.dp)
                    .testTag("admin_health_header")
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("admin_health_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Platform Health",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("admin_health_title")
                )

                IconButton(
                    onClick = {
                        if (!isRefreshing) {
                            isRefreshing = true
                            coroutineScope.launch {
                                delay(600)
                                apiLatencyMs = (32..44).random()
                                dbLatencyMs = (10..15).random()
                                dbConnections = (16..22).random()
                                isRefreshing = false
                                snackbarHostState.showSnackbar("Metrics updated")
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .testTag("admin_health_refresh_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ==========================================
            // BODY: Card-Based Sections with Numbers
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
                        .padding(top = 24.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // ------------------------------------------
                    // 1. PLATFORM HEALTH HERO CARD
                    // ------------------------------------------
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = IndigoPurple.copy(alpha = 0.35f),
                                spotColor = Color(0x33000000)
                            )
                            .testTag("overall_health_banner"),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.20f),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = "Platform Health",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Platform Uptime",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = LightLavender
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = platformUptime,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NoteCountGreen
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Text(
                                            text = "HEALTHY",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ------------------------------------------
                    // 2. STORAGE USED SECTION
                    // ------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(title = "Storage Used")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SolidMetricCard(
                                modifier = Modifier.weight(1f),
                                value = "${storageUsedGb} GB",
                                label = "Used",
                                icon = Icons.Outlined.CloudQueue,
                                color = CoralTile,
                                testTag = "tile_storage_health"
                            )
                            SolidMetricCard(
                                modifier = Modifier.weight(1f),
                                value = "${storageFreeGb} GB",
                                label = "Available",
                                icon = Icons.Outlined.Storage,
                                color = TealTile,
                                testTag = "tile_storage_free"
                            )
                            SolidMetricCard(
                                modifier = Modifier.weight(1f),
                                value = "${storageTotalGb.toInt()} GB",
                                label = "Total",
                                icon = Icons.Outlined.Folder,
                                color = PurpleTile,
                                testTag = "tile_storage_total"
                            )
                        }

                        // Compact Storage % Bar Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SoftLavenderGrey.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Capacity Used",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "$storagePercent%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = IndigoPurple
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { storageUsedGb / storageTotalGb },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = CoralTile,
                                    trackColor = Color.White,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // ------------------------------------------
                    // 3. API CONNECTION STATUS SECTION
                    // ------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(title = "API Connection")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = apiActiveCount,
                                label = "APIs Online",
                                badge = "ONLINE",
                                icon = Icons.Outlined.CloudDone,
                                accentColor = NoteCountGreen,
                                testTag = "card_api_online"
                            )
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = "${apiLatencyMs} ms",
                                label = "Latency",
                                badge = "FAST",
                                icon = Icons.Outlined.Speed,
                                accentColor = BlueTile,
                                testTag = "card_api_latency"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = apiSuccessRate,
                                label = "Success Rate",
                                badge = "STABLE",
                                icon = Icons.Outlined.NetworkCheck,
                                accentColor = TealTile,
                                testTag = "card_api_success"
                            )
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = "$apiErrorsCount",
                                label = "Failed Calls",
                                badge = "0 ERRORS",
                                icon = Icons.Outlined.CheckCircle,
                                accentColor = PurpleTile,
                                testTag = "card_api_errors"
                            )
                        }
                    }

                    // ------------------------------------------
                    // 4. DATABASE CONNECTION SECTION
                    // ------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(title = "Database Connection")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = "$dbConnections",
                                label = "Connections",
                                badge = "CONNECTED",
                                icon = Icons.Outlined.Dns,
                                accentColor = TealTile,
                                testTag = "tile_db_health"
                            )
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = "${dbLatencyMs} ms",
                                label = "Query Time",
                                badge = "OPTIMAL",
                                icon = Icons.Outlined.Speed,
                                accentColor = NavyTile,
                                testTag = "card_db_latency"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = dbRecordsCount,
                                label = "Total Records",
                                badge = "ACTIVE",
                                icon = Icons.Outlined.Storage,
                                accentColor = CoralTile,
                                testTag = "card_db_records"
                            )
                            NumberStatusCard(
                                modifier = Modifier.weight(1f),
                                value = dbSyncRate,
                                label = "Sync Status",
                                badge = "SYNCED",
                                icon = Icons.Outlined.Sync,
                                accentColor = BlueTile,
                                testTag = "tile_compute_health"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.padding(start = 2.dp)
    )
}

/**
 * Solid colored card with prominent number and short label (matches AdminHomeScreen stat cards).
 */
@Composable
private fun SolidMetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    testTag: String
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = color.copy(alpha = 0.30f),
                spotColor = Color(0x22000000)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = color
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.20f),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Clean number-focused status card for API & Database monitoring.
 */
@Composable
private fun NumberStatusCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = accentColor.copy(alpha = 0.20f)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHealthScreenPreview() {
    AdminHealthScreen()
}
