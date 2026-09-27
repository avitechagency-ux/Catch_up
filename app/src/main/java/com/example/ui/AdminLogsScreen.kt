package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupabaseRepository
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.NoteCountGreen
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Audit and System Log Item.
 */
data class AdminLogItem(
    val id: String,
    val timestamp: String,
    val level: String, // "INFO", "WARN", "ERROR", "SECURITY", "PAYOUT", "DATABASE"
    val title: String,
    val description: String,
    val actor: String,
    val ipAddress: String,
    val category: String,
    val detailsJson: String? = null
)

/**
 * Default System Audit Logs.
 */
val DefaultAdminLogs: List<AdminLogItem> = listOf(
    AdminLogItem(
        id = "log-101",
        timestamp = "24 Sep 2026, 19:42:15",
        level = "INFO",
        title = "Study Material Published",
        description = "Teacher 'Tr. Okello Peter' published 'Plate Tectonics & Volcanicity.pdf' (3.2 MB) for S.4 Geography.",
        actor = "Tr. Okello Peter (Teacher)",
        ipAddress = "197.239.14.88",
        category = "CONTENT",
        detailsJson = "{\n  \"event\": \"NOTE_PUBLISHED\",\n  \"subject\": \"Geography\",\n  \"class\": \"S.4\",\n  \"file_size\": \"3.2 MB\",\n  \"status\": \"SUCCESS\"\n}"
    ),
    AdminLogItem(
        id = "log-102",
        timestamp = "24 Sep 2026, 19:30:04",
        level = "PAYOUT",
        title = "MTN MoMo Subscription IPN Verified",
        description = "Student 'Sarah Namubiru' completed Termly Plan payment: UGX 3,000 via MTN MoMo Pay.",
        actor = "Sarah Namubiru (Student)",
        ipAddress = "41.210.150.22",
        category = "PAYMENT",
        detailsJson = "{\n  \"gateway\": \"MTN_MOMO_PAY\",\n  \"transaction_id\": \"TX-984210\",\n  \"amount\": 3000,\n  \"currency\": \"UGX\",\n  \"status\": \"SUCCESS\"\n}"
    ),
    AdminLogItem(
        id = "log-103",
        timestamp = "24 Sep 2026, 19:15:22",
        level = "SECURITY",
        title = "Admin Login 2FA Verified",
        description = "Superadministrator authenticated successfully from Kampala, Uganda via OTP token.",
        actor = "Superadministrator",
        ipAddress = "102.134.42.11",
        category = "SECURITY",
        detailsJson = "{\n  \"auth_method\": \"2FA_TOTP\",\n  \"session_id\": \"sess-90218\",\n  \"device\": \"Android 14 (SM-S918B)\",\n  \"result\": \"ALLOW\"\n}"
    ),
    AdminLogItem(
        id = "log-104",
        timestamp = "24 Sep 2026, 18:50:11",
        level = "DATABASE",
        title = "SQLite VACUUM & Index Optimization",
        description = "Automated nightly maintenance completed on study_notes and subscriptions tables in 142ms.",
        actor = "SYSTEM_SCHEDULER",
        ipAddress = "127.0.0.1",
        category = "DATABASE",
        detailsJson = "{\n  \"task\": \"VACUUM_ANALYZE\",\n  \"tables_indexed\": 6,\n  \"execution_time_ms\": 142,\n  \"disk_reclaimed_mb\": 12.4\n}"
    ),
    AdminLogItem(
        id = "log-105",
        timestamp = "24 Sep 2026, 18:22:45",
        level = "WARN",
        title = "Affiliate Referral Spike Detected",
        description = "User #ST-4122 generated 12 referral clicks in 3 minutes. Velocity limiter monitored.",
        actor = "ST-4122 (Student)",
        ipAddress = "197.239.20.104",
        category = "AFFILIATE",
        detailsJson = "{\n  \"rule\": \"RATE_LIMIT_VELOCITY\",\n  \"rate\": \"12 req / 3 min\",\n  \"action\": \"LOG_AND_MONITOR\",\n  \"fraud_score\": 0.28\n}"
    ),
    AdminLogItem(
        id = "log-106",
        timestamp = "24 Sep 2026, 17:05:00",
        level = "INFO",
        title = "Teacher Royalty Remittance Disbursed",
        description = "Monthly royalty payment of UGX 145,000 sent to Tr. Mugisha David via Airtel Money.",
        actor = "ADMIN_FINANCE_AUTO",
        ipAddress = "102.134.42.11",
        category = "PAYMENT",
        detailsJson = "{\n  \"recipient\": \"Tr. Mugisha David\",\n  \"phone\": \"0755-123456\",\n  \"amount\": 145000,\n  \"reference\": \"ROYALTY-AUG-2026\"\n}"
    ),
    AdminLogItem(
        id = "log-107",
        timestamp = "24 Sep 2026, 15:40:19",
        level = "ERROR",
        title = "PDF Upload Timeout Handled",
        description = "Student document preview timed out due to slow client connection. Gracefully aborted.",
        actor = "ST-1940 (Student)",
        ipAddress = "41.210.160.9",
        category = "STORAGE",
        detailsJson = "{\n  \"error_code\": \"ERR_SOCKET_TIMEOUT\",\n  \"duration_ms\": 30000,\n  \"handled\": true\n}"
    )
)

/**
 * System and Audit Logs Screen for Admin Panel.
 */
@Composable
fun AdminLogsScreen(
    modifier: Modifier = Modifier,
    initialLogs: List<AdminLogItem> = emptyList(),
    onNavigateBack: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var logsList by remember { mutableStateOf(initialLogs) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("All") }
    var selectedLogForModal by remember { mutableStateOf<AdminLogItem?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isRefreshing = true
        val remoteLogs = SupabaseRepository.fetchSystemAuditLogs()
        logsList = remoteLogs
        isRefreshing = false
    }

    // Filtered logs computed
    val filteredLogs = remember(logsList, searchQuery, selectedLevel) {
        logsList.filter { log ->
            val matchesLevel = when (selectedLevel) {
                "All" -> true
                else -> log.level.equals(selectedLevel, ignoreCase = true) || log.category.equals(selectedLevel, ignoreCase = true)
            }

            val query = searchQuery.trim()
            val matchesSearch = query.isBlank() ||
                    log.title.contains(query, ignoreCase = true) ||
                    log.description.contains(query, ignoreCase = true) ||
                    log.actor.contains(query, ignoreCase = true) ||
                    log.ipAddress.contains(query, ignoreCase = true) ||
                    log.level.contains(query, ignoreCase = true)

            matchesLevel && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_logs_screen"),
        containerColor = Color(0xFFF9F8FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .navigationBarsPadding()
                .imePadding()
        ) {
            // ==========================================
            // 1. HEADER: Purple banner with Search Bar
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
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp)
                    .testTag("admin_logs_header")
            ) {
                // Top Row: Back button, Title, Refresh & Export CSV Button
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("admin_logs_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "System Audit Logs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("admin_logs_title")
                        )
                        Text(
                            text = "Live database & storage audit trail",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (!isRefreshing) {
                                    isRefreshing = true
                                    coroutineScope.launch {
                                        logsList = SupabaseRepository.fetchSystemAuditLogs()
                                        isRefreshing = false
                                        snackbarHostState.showSnackbar("Audit logs synced with Supabase")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("admin_logs_refresh_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Refresh Logs",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clickable { showExportDialog = true }
                                .testTag("admin_logs_export_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.20f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = "Export",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CSV",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar embedded inside header
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
                        .testTag("search_logs_bar"),
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
                                    text = "Search logs by title, user, IP, level...",
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
                                    .testTag("search_logs_input")
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

            // ==========================================
            // 2. BODY: FILTER CHIPS & LOG ENTRIES
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("admin_logs_lazy_list"),
                contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
            ) {
                // Section 1: Level Filter Chips
                item {
                    val filterLevels = listOf(
                        "All",
                        "INFO",
                        "STORAGE",
                        "PAYOUT",
                        "SECURITY",
                        "DATABASE",
                        "WARN",
                        "ERROR"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterLevels.forEach { level ->
                            val isSelected = selectedLevel == level
                            Surface(
                                modifier = Modifier
                                    .shadow(
                                        elevation = if (isSelected) 3.dp else 0.dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = IndigoPurple.copy(alpha = 0.25f)
                                    )
                                    .clickable { selectedLevel = level }
                                    .testTag("filter_level_${level.lowercase()}"),
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) IndigoPurple else Color.White,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, BorderGrey) else null
                            ) {
                                Text(
                                    text = if (level == "All") "All (${logsList.size})" else level,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Stream Indicator Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NoteCountGreen)
                            )
                            Text(
                                text = "LIVE AUDIT FEED (${filteredLogs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = "Tap log to inspect",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Section 2: Log Items
                if (filteredLogs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 20.dp)
                                .testTag("admin_logs_empty_state"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = LightLavender.copy(alpha = 0.5f),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Article,
                                        contentDescription = "Empty",
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No logs matching criteria",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing your search query or selecting a different level filter.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedLevel = "All"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reset Filters", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    items(filteredLogs, key = { it.id }) { item ->
                        AdminLogCard(
                            log = item,
                            onClick = { selectedLogForModal = item }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // Modal: Inspect Full Log JSON
    if (selectedLogForModal != null) {
        val currentLog = selectedLogForModal!!
        AlertDialog(
            onDismissRequest = { selectedLogForModal = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = getLevelColor(currentLog.level).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = currentLog.level,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = getLevelColor(currentLog.level),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = currentLog.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentLog.description,
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 18.sp
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SoftLavenderGrey,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Timestamp: ${currentLog.timestamp}", fontSize = 11.sp, color = TextMuted)
                            Text("Actor: ${currentLog.actor}", fontSize = 11.sp, color = TextMuted)
                            Text("Client IP: ${currentLog.ipAddress}", fontSize = 11.sp, color = TextMuted)
                            Text("Category: ${currentLog.category}", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    if (currentLog.detailsJson != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1E2E)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PAYLOAD (JSON)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LightLavender)
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(currentLog.detailsJson))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Log payload copied to clipboard")
                                            }
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentLog.detailsJson,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFA6E3A1),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedLogForModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal: Export CSV confirmation
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text("Export Audit Logs CSV?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            },
            text = {
                Text("An encrypted CSV file containing all ${logsList.size} audit events, timestamps, actors, and IP logs will be prepared for download.", fontSize = 14.sp, color = TextMuted)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Exported Cachap_Audit_Logs_2026.csv successfully")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Export CSV", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExportDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Log Card component.
 */
@Composable
private fun AdminLogCard(
    log: AdminLogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = getLevelColor(log.level)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x10000000)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("admin_log_card_${log.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Level Icon Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = levelColor.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getLevelIcon(log.level),
                        contentDescription = log.level,
                        tint = levelColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                // Header row: Level pill & Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = levelColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = log.level,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = log.timestamp,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = log.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Description
                Text(
                    text = log.description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Meta: Actor & IP
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = log.actor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = log.ipAddress,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Color mapper for log severity.
 */
private fun getLevelColor(level: String): Color {
    return when (level.uppercase()) {
        "INFO" -> TealTile
        "PAYOUT" -> NoteCountGreen
        "SECURITY" -> PurpleTile
        "DATABASE" -> BlueTile
        "WARN" -> AmberTile
        "ERROR" -> CoralTile
        else -> IndigoPurple
    }
}

/**
 * Icon mapper for log severity.
 */
private fun getLevelIcon(level: String): ImageVector {
    return when (level.uppercase()) {
        "INFO" -> Icons.Outlined.Info
        "PAYOUT" -> Icons.Outlined.Payments
        "SECURITY" -> Icons.Outlined.Security
        "DATABASE" -> Icons.Outlined.Storage
        "WARN" -> Icons.Outlined.Warning
        "ERROR" -> Icons.Outlined.ErrorOutline
        else -> Icons.Outlined.Article
    }
}

@Preview(showBackground = true)
@Composable
fun AdminLogsScreenPreview() {
    AdminLogsScreen()
}
