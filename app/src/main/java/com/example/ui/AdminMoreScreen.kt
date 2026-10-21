package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Admin More Screen.
 *
 * Specifications:
 * 1. Header: Clean header at the top with the title "More".
 * 2. Resembles the student's profile screen layout, with menu items listed as full-width
 *    pill-shaped buttons/cards in the exact order:
 *    · Account
 *    · Transactions
 *    · Settings
 *    · Payments
 *    · Logs
 *    · LOG OUT (uppercase text with distinct red styling indicating a destructive action)
 * 3. Bottom Navigation: Reused AdminBottomNavBar with "More" (index 3) active.
 */
@Composable
fun AdminMoreScreen(
    modifier: Modifier = Modifier,
    adminName: String = "Administrator",
    adminRole: String = "Super Admin • Cachap Platform",
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var activeModalTitle by remember { mutableStateOf<String?>(null) }
    var activeModalDescription by remember { mutableStateOf<String?>(null) }

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var targetRoleChoice by remember { mutableStateOf("All Students") }
    var isBroadcasting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_more_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = 3,
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
            // 1. HEADER
            // Resembles student profile screen header:
            // Purple card (#4B3F8F) with rounded bottom corners (28dp),
            // back button, centered title "More", and admin profile badge
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
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp)
                    .testTag("admin_more_header")
            ) {
                // Top Row: Back button on left, "More" title centered
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("admin_more_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "More",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("admin_more_title")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Profile Identity Badge (like student profile header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "AD",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = adminName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = adminRole,
                            fontSize = 13.sp,
                            color = LightLavender
                        )
                    }
                }
            }

            // ==========================================
            // 2. MENU LIST
            // Vertical list of menu items styled like pills in the student's profile page:
            // · Account
            // · Transactions
            // · Settings
            // · Payments
            // · Logs
            // · LOG OUT (uppercase text, red styling for destructive action)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag("admin_more_menu_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Account
                AdminMenuPill(
                    text = "Account",
                    icon = Icons.Outlined.Person,
                    testTag = "admin_more_item_account",
                    onClick = onNavigateToAccount
                )

                // 2. Transactions
                AdminMenuPill(
                    text = "Transactions",
                    icon = Icons.Outlined.ReceiptLong,
                    testTag = "admin_more_item_transactions",
                    onClick = onNavigateToTransactions
                )

                // 3. Settings
                AdminMenuPill(
                    text = "Settings",
                    icon = Icons.Outlined.Settings,
                    testTag = "admin_more_item_settings",
                    onClick = onNavigateToSettings
                )

                // 4. Health (Platform, Database & Storage Health)
                AdminMenuPill(
                    text = "Health",
                    icon = Icons.Outlined.Dns,
                    testTag = "admin_more_item_health",
                    onClick = onNavigateToHealth
                )

                // 5. Logs
                AdminMenuPill(
                    text = "Logs",
                    icon = Icons.Outlined.Article,
                    testTag = "admin_more_item_logs",
                    onClick = onNavigateToLogs
                )

                // 6. Broadcast Announcement (Realtime Notification)
                AdminMenuPill(
                    text = "Broadcast Announcement",
                    icon = Icons.Outlined.Campaign,
                    testTag = "admin_more_item_broadcast",
                    onClick = {
                        announcementTitle = ""
                        announcementMessage = ""
                        showBroadcastDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 7. LOG OUT — Uppercase text, styled with red text & soft-red border/background
                // to clearly indicate a destructive action
                AdminLogoutPill(
                    testTag = "admin_more_item_logout",
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }

    // Broadcast School Announcement Dialog
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBroadcasting) showBroadcastDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Campaign,
                        contentDescription = null,
                        tint = IndigoPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Broadcast Announcement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send a realtime notification to all active student & teacher devices across the platform.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )

                    androidx.compose.material3.OutlinedTextField(
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        label = { Text("Announcement Title") },
                        placeholder = { Text("e.g., Term 3 UNEB Past Papers Live") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    androidx.compose.material3.OutlinedTextField(
                        value = announcementMessage,
                        onValueChange = { announcementMessage = it },
                        label = { Text("Message Body") },
                        placeholder = { Text("Detailed notice text...") },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    // Target Audience Selection
                    Column {
                        Text(
                            text = "Target Audience:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All Students", "S.4", "All Teachers").forEach { option ->
                                val isSelected = targetRoleChoice == option
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) IndigoPurple else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) IndigoPurple else BorderGrey),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { targetRoleChoice = option }
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextDark,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (announcementTitle.isBlank() || announcementMessage.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please enter a title and message")
                            }
                            return@TextButton
                        }
                        coroutineScope.launch {
                            isBroadcasting = false
                            showBroadcastDialog = false
                            snackbarHostState.showSnackbar("Announcement broadcasted successfully! 📢")
                        }
                    },
                    enabled = !isBroadcasting && announcementTitle.isNotBlank() && announcementMessage.isNotBlank()
                ) {
                    Text(
                        text = if (isBroadcasting) "Broadcasting..." else "Send Announcement",
                        fontWeight = FontWeight.Bold,
                        color = IndigoPurple
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBroadcastDialog = false },
                    enabled = !isBroadcasting
                ) {
                    Text("Cancel", color = TextMuted)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Modal Details Dialog for Transactions, Payments, Logs
    if (activeModalTitle != null) {
        AlertDialog(
            onDismissRequest = { activeModalTitle = null },
            title = {
                Text(
                    text = activeModalTitle ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = activeModalDescription ?: "",
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { activeModalTitle = null }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = IndigoPurple)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Destructive Log Out Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = IndigoPurple,
            modifier = Modifier.testTag("admin_logout_dialog"),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Log Out",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Log Out of Admin?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of the admin panel? You will be returned to the main student screen.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightLavender,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("admin_confirm_logout_button")
                ) {
                    Text(
                        text = "LOG OUT",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.30f)),
                    modifier = Modifier.testTag("admin_cancel_logout_button")
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        )
    }
}

/**
 * Standard purple menu pill matching the student's profile page pills.
 */
@Composable
fun AdminMenuPill(
    text: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    cornerRadius: Dp = 26.dp
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = IndigoPurple.copy(alpha = 0.25f),
                spotColor = Color(0x20000000)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(cornerRadius),
        color = IndigoPurple
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.70f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Destructive "LOG OUT" pill with uppercase red text, soft-red container/border,
 * and red exit icon.
 */
@Composable
fun AdminLogoutPill(
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    cornerRadius: Dp = 26.dp
) {
    val redTextColor = Color(0xFFDC2626)
    val redBgColor = Color(0xFFFFF1F2)
    val redBorderColor = Color(0xFFFECACA)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = redTextColor.copy(alpha = 0.15f),
                spotColor = Color(0x15000000)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(cornerRadius),
        color = redBgColor,
        border = BorderStroke(1.dp, redBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Logout icon",
                    tint = redTextColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "LOG OUT",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = redTextColor,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = redTextColor.copy(alpha = 0.70f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
