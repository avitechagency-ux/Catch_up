package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TrackPurple
import kotlinx.coroutines.launch

/**
 * Admin Settings Screen.
 *
 * Tailored specifically for Platform Administrators:
 * - Platform & system governance (Maintenance mode, self-registration, user policies)
 * - Financial & payout rules (Auto-approvals, minimum withdrawal threshold, currency)
 * - Real-time security alerts & fraud monitoring
 * - Security & audit management (2FA, session timeout, audit export, cache purge)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    modifier: Modifier = Modifier,
    adminName: String = "Administrator",
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 1. Platform Governance
    var maintenanceMode by remember { mutableStateOf(false) }
    var userRegistrationEnabled by remember { mutableStateOf(true) }

    // 2. Financial & Payout Rules
    var autoApproveMicroPayouts by remember { mutableStateOf(true) }
    var minWithdrawalUgx by remember { mutableStateOf(500) }
    var showMinWithdrawalPicker by remember { mutableStateOf(false) }
    var liveGatewayWebhooks by remember { mutableStateOf(true) }

    // 3. Security & Alerts
    var highValueAlerts by remember { mutableStateOf(true) }
    var fraudDetectionAlerts by remember { mutableStateOf(true) }
    var teacherPayoutReminders by remember { mutableStateOf(true) }
    var dailySummaryDigest by remember { mutableStateOf(true) }

    // 4. Security & Audit
    var enforceTwoFactor by remember { mutableStateOf(true) }
    var sessionTimeoutMinutes by remember { mutableStateOf("15 mins") }
    var showTimeoutPicker by remember { mutableStateOf(false) }

    // Maintenance Confirmation Dialog
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showFlushCacheDialog by remember { mutableStateOf(false) }

    val timeoutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val withdrawalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_settings_screen"),
        containerColor = Color(0xFFF9F8FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .navigationBarsPadding()
        ) {
            // ==========================================
            // HEADER: Signature IndigoPurple curved banner
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
                    .testTag("admin_settings_header")
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("admin_settings_back_button")
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
                            text = "Admin Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("admin_settings_title")
                        )
                        Text(
                            text = "Platform governance, finance & security",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender
                        )
                    }
                }
            }

            // ==========================================
            // BODY: GROUPED ADMIN SETTINGS
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ADMIN MASTER BADGE CARD
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = IndigoPurple.copy(alpha = 0.15f)
                        )
                        .testTag("admin_settings_master_card"),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NavyTile.copy(alpha = 0.12f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = NavyTile,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = adminName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CoralTile.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "SUPERADMIN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CoralTile,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Cachap Platform • Uganda Production (v2.4.0)",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ------------------------------------------
                // SECTION 1 — Platform Governance & System Control
                // ------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PLATFORM & SYSTEM GOVERNANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Column {
                            AdminSettingsSwitchRow(
                                title = "Platform Maintenance Mode",
                                subtitle = if (maintenanceMode) "ACTIVE — Students & teachers are in read-only mode" else "Disabled — Platform operating normally",
                                checked = maintenanceMode,
                                onCheckedChange = {
                                    if (it) {
                                        showMaintenanceDialog = true
                                    } else {
                                        maintenanceMode = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Maintenance mode deactivated")
                                        }
                                    }
                                },
                                isHighlight = maintenanceMode,
                                testTag = "switch_maintenance_mode"
                            )
                            AdminSettingsDivider()
                            AdminSettingsSwitchRow(
                                title = "New User Registration",
                                subtitle = "Allow new students and teachers to create accounts",
                                checked = userRegistrationEnabled,
                                onCheckedChange = {
                                    userRegistrationEnabled = it
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(if (it) "User registration enabled" else "User registration temporarily disabled")
                                    }
                                },
                                testTag = "switch_user_registration"
                            )
                            AdminSettingsDivider()
                            AdminSettingsActionRow(
                                title = "Online Classroom Feature",
                                value = if (com.example.data.AppFeatures.ONLINE_CLASSES_ENABLED) "Active" else "Disabled (Preserved)",
                                onClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Online classroom is temporarily disabled. Underlying code & data preserved.")
                                    }
                                },
                                testTag = "action_online_classes_status"
                            )
                        }
                    }
                }

                // ------------------------------------------
                // SECTION 2 — Financial & Payout Rules
                // ------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FINANCIAL & PAYOUT RULES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Column {
                            AdminSettingsSwitchRow(
                                title = "Auto-Approve Micro Payouts",
                                subtitle = "Automatically process teacher and platform payouts < UGX 50,000",
                                checked = autoApproveMicroPayouts,
                                onCheckedChange = { autoApproveMicroPayouts = it },
                                testTag = "switch_auto_payouts"
                            )
                            AdminSettingsDivider()
                            AdminSettingsActionRow(
                                title = "Minimum Withdrawal Threshold",
                                value = "UGX $minWithdrawalUgx",
                                onClick = { showMinWithdrawalPicker = true },
                                testTag = "action_min_withdrawal"
                            )
                            AdminSettingsDivider()
                            AdminSettingsSwitchRow(
                                title = "Live Payment Webhooks",
                                subtitle = "MarzPay IPN webhook: whsec_BtNjMmQvMaRxylJBw8Wd0uUUY0zgWm0hUbLJLdbe",
                                checked = liveGatewayWebhooks,
                                onCheckedChange = { liveGatewayWebhooks = it },
                                testTag = "switch_live_webhooks"
                            )
                            if (liveGatewayWebhooks) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = SoftLavenderGrey,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "MarzPay Webhook Secret (Verified)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoPurple
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "whsec_BtNjMmQvMaRxylJBw8Wd0uUUY0zgWm0hUbLJLdbe",
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Edge Function Endpoint: /functions/v1/marzpay-webhook",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                            AdminSettingsDivider()
                            AdminSettingsActionRow(
                                title = "Platform Currency",
                                value = "UGX (Uganda Shilling)",
                                onClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Currency is locked to Uganda Shilling (UGX)")
                                    }
                                },
                                testTag = "action_platform_currency"
                            )
                        }
                    }
                }

                // ------------------------------------------
                // SECTION 3 — Real-Time Admin Alerts & Push
                // ------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ADMIN PUSH ALERTS & MONITORING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Column {
                            AdminSettingsSwitchRow(
                                title = "High-Value Transaction Alerts",
                                subtitle = "Push notification for subscriptions > UGX 50,000",
                                checked = highValueAlerts,
                                onCheckedChange = { highValueAlerts = it },
                                testTag = "switch_high_value_alerts"
                            )
                            AdminSettingsDivider()
                            AdminSettingsSwitchRow(
                                title = "Fraud & Suspicious Spikes",
                                subtitle = "Flag rapid multi-account signups and duplicate IPs",
                                checked = fraudDetectionAlerts,
                                onCheckedChange = { fraudDetectionAlerts = it },
                                testTag = "switch_fraud_alerts"
                            )
                            AdminSettingsDivider()
                            AdminSettingsSwitchRow(
                                title = "Teacher Royalty Reminders",
                                subtitle = "Monthly notification to review and disburse royalties",
                                checked = teacherPayoutReminders,
                                onCheckedChange = { teacherPayoutReminders = it },
                                testTag = "switch_teacher_payout_alerts"
                            )
                            AdminSettingsDivider()
                            AdminSettingsSwitchRow(
                                title = "Daily Platform Digest",
                                subtitle = "Evening revenue & active student analytics summary",
                                checked = dailySummaryDigest,
                                onCheckedChange = { dailySummaryDigest = it },
                                testTag = "switch_daily_digest"
                            )
                        }
                    }
                }

                // ------------------------------------------
                // SECTION 4 — Security, Logs & Data Cache
                // ------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SECURITY, AUDIT & DATA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Column {
                            AdminSettingsSwitchRow(
                                title = "Enforce Two-Factor Auth (2FA)",
                                subtitle = "Require OTP authenticator for admin portal access",
                                checked = enforceTwoFactor,
                                onCheckedChange = { enforceTwoFactor = it },
                                testTag = "switch_two_factor"
                            )
                            AdminSettingsDivider()
                            AdminSettingsActionRow(
                                title = "Admin Inactivity Lock",
                                value = sessionTimeoutMinutes,
                                onClick = { showTimeoutPicker = true },
                                testTag = "action_session_timeout"
                            )
                            AdminSettingsDivider()
                            AdminSettingsDestructiveRow(
                                title = "Export Financial & Audit Logs",
                                subtitle = "Generate encrypted CSV report of all transactions",
                                actionText = "Export CSV",
                                isDanger = false,
                                icon = Icons.Outlined.Download,
                                onClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Exporting 2026_Q3_Cachap_Audit_Logs.csv...")
                                    }
                                },
                                testTag = "button_export_audit_logs"
                            )
                            AdminSettingsDivider()
                            AdminSettingsDestructiveRow(
                                title = "Flush Analytical Aggregation Cache",
                                subtitle = "Forces re-computation of charts and revenue totals",
                                actionText = "Flush Cache",
                                isDanger = false,
                                icon = Icons.Outlined.Refresh,
                                onClick = { showFlushCacheDialog = true },
                                testTag = "button_flush_admin_cache"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal: Inactivity Timeout Picker
    if (showTimeoutPicker) {
        ModalBottomSheet(
            onDismissRequest = { showTimeoutPicker = false },
            sheetState = timeoutSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Admin Portal Inactivity Timeout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(14.dp))

                listOf("5 mins", "15 mins", "30 mins", "1 hour", "Never").forEach { timeout ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                sessionTimeoutMinutes = timeout
                                showTimeoutPicker = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Session timeout set to $timeout")
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (sessionTimeoutMinutes == timeout) IndigoPurple.copy(alpha = 0.10f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timeout,
                                fontSize = 15.sp,
                                fontWeight = if (sessionTimeoutMinutes == timeout) FontWeight.Bold else FontWeight.Medium,
                                color = if (sessionTimeoutMinutes == timeout) IndigoPurple else TextDark
                            )
                            if (sessionTimeoutMinutes == timeout) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Modal: Min Withdrawal Picker
    if (showMinWithdrawalPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMinWithdrawalPicker = false },
            sheetState = withdrawalSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Minimum Payout Threshold",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(14.dp))

                listOf(500, 1000, 2000, 5000).forEach { amount ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                minWithdrawalUgx = amount
                                showMinWithdrawalPicker = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Minimum payout threshold updated to UGX $amount")
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (minWithdrawalUgx == amount) IndigoPurple.copy(alpha = 0.10f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UGX $amount",
                                fontSize = 15.sp,
                                fontWeight = if (minWithdrawalUgx == amount) FontWeight.Bold else FontWeight.Medium,
                                color = if (minWithdrawalUgx == amount) IndigoPurple else TextDark
                            )
                            if (minWithdrawalUgx == amount) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Dialog: Maintenance Mode Confirmation
    if (showMaintenanceDialog) {
        AlertDialog(
            onDismissRequest = { showMaintenanceDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Warning",
                        tint = CoralTile,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Enable Maintenance Mode?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Text(
                    text = "Enabling maintenance mode will temporarily lock student and teacher uploads and subscriptions into read-only mode while database backups are conducted.",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        maintenanceMode = true
                        showMaintenanceDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Maintenance mode activated across platform")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralTile),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enable Maintenance", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showMaintenanceDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Flush Cache Confirmation
    if (showFlushCacheDialog) {
        AlertDialog(
            onDismissRequest = { showFlushCacheDialog = false },
            title = {
                Text(
                    text = "Flush Analytics Cache?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "This will clear temporary in-memory aggregation tables and force real-time recalculation of all charts and user counts from the database.",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFlushCacheDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Analytical caches successfully purged & re-indexed")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Flush Cache", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFlushCacheDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Shared Settings Switch Row for Admin panel.
 */
@Composable
private fun AdminSettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isHighlight: Boolean = false,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isHighlight) CoralTile else TextDark
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isHighlight) CoralTile.copy(alpha = 0.8f) else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isHighlight) CoralTile else IndigoPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TrackPurple,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/**
 * Shared Settings Action Row with Chevron for Admin panel.
 */
@Composable
private fun AdminSettingsActionRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = IndigoPurple,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = IconGrey,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Destructive/Action row with button for Admin panel.
 */
@Composable
private fun AdminSettingsDestructiveRow(
    title: String,
    subtitle: String,
    actionText: String,
    isDanger: Boolean,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDanger) Color(0xFFD32F2F) else TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDanger) Color(0xFFFFEBEE) else IndigoPurple.copy(alpha = 0.10f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDanger) Color(0xFFD32F2F) else IndigoPurple,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDanger) Color(0xFFD32F2F) else IndigoPurple
                )
            }
        }
    }
}

@Composable
private fun AdminSettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(BorderGrey)
    )
}

@Preview(showBackground = true)
@Composable
fun AdminSettingsScreenPreview() {
    AdminSettingsScreen()
}
