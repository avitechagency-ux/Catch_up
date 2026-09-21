package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Shared Pill-shaped Outlined Row Component.
 * - Full width (minus 20dp side margins), height 52dp (or 60dp for top identity)
 * - Background: transparent / white (no fill)
 * - Border: 1.5dp solid #D9D9D9 (light grey outline)
 * - Corner radius: 26dp (fully rounded pill shape) / 30dp for 60dp height
 * - Text: font 15sp, weight Medium, color #1A1A1A
 */
@Composable
fun ProfilePillRow(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    cornerRadius: Dp = 26.dp,
    backgroundColor: Color = IndigoPurple,
    borderColor: Color? = null,
    borderWidth: Dp = 0.dp,
    textColor: Color = Color.White,
    startPadding: Dp = 20.dp,
    elevation: Dp = 2.dp,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    testTag: String = "profile_pill_row_${text.lowercase().replace(" ", "_").replace("&", "and")}",
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        shadowElevation = elevation,
        border = if (borderColor != null && borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPadding, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

/**
 * Student Profile Screen.
 *
 * Layout Structure:
 * - Existing purple Header (~200dp) with avatar, student name, class + school
 * - White body with clean vertical stack of pill-shaped outlined rows:
 *   - Top Identity row (60dp, 24dp avatar icon on left, student name)
 *   - 14dp vertical gaps
 *   1. "Account"
 *   2. "Settings"
 *   3. "About"
 *   4. "Help & Support"
 *   5. "Privacy Policy"
 *   6. "Log Out" (with small exit/arrow icon on the right side)
 * - Reused BottomNavigationBar composable (Profile tab active)
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToAccount: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelpAndSupport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onUpdateProfile: (name: String, school: String, studentClass: String) -> Unit = { _, _, _ -> }
) {
    var currentName by remember { mutableStateOf(studentName) }
    var currentSchool by remember { mutableStateOf(schoolName) }
    var currentClass by remember { mutableStateOf(academicClass) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAccountDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Settings state
    var studyRemindersEnabled by remember { mutableStateOf(true) }
    var offlineAutoCacheEnabled by remember { mutableStateOf(true) }

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
            // 1. HEADER (Preserved Purple Header)
            // Purple background (#4B3F8F), ~200dp, rounded bottom corners 28dp
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
                // Switch to Teacher Profile button on top right of the banner
                Surface(
                    onClick = onNavigateToTeacher,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp)
                        .testTag("profile_switch_to_teacher_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = "Switch to Teacher View",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Teacher",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

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
                            .clickable { showAccountDialog = true }
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
            // 2. WHITE BODY — Vertical Menu of Solid Purple Pill Rows
            // 20dp left/right padding, 16dp top padding, 14dp spacing between rows
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-14).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Identity row (60dp height, solid #4B3F8F, small 24dp white circular avatar with #4B3F8F initial, student name in white)
                ProfilePillRow(
                    text = currentName,
                    height = 60.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = currentName.trim().firstOrNull()?.uppercase() ?: "S"
                            Text(
                                text = initial,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple
                            )
                        }
                    },
                    testTag = "profile_identity_pill",
                    onClick = { showAccountDialog = true }
                )

                // 2. "Account" — plain purple pill, white text
                ProfilePillRow(
                    text = "Account",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "profile_pill_account",
                    onClick = onNavigateToAccount
                )

                // 3. "Settings" — plain purple pill, white text
                ProfilePillRow(
                    text = "Settings",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "profile_pill_settings",
                    onClick = onNavigateToSettings
                )

                // 4. "About" — plain purple pill, white text
                ProfilePillRow(
                    text = "About",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "profile_pill_about",
                    onClick = onNavigateToAbout
                )

                // 5. "Help & Support" — plain purple pill, white text
                ProfilePillRow(
                    text = "Help & Support",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "profile_pill_help_and_support",
                    onClick = onNavigateToHelpAndSupport
                )

                // 6. "Privacy Policy" — plain purple pill, white text
                ProfilePillRow(
                    text = "Privacy Policy",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "profile_pill_privacy_policy",
                    onClick = { showPrivacyDialog = true }
                )

                // 7. "Log Out" — same purple pill style, white text, with small exit arrow icon (18dp, white) on right
                ProfilePillRow(
                    text = "Log Out",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Log Out Icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    testTag = "profile_pill_log_out",
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }

    // ==========================================
    // MODALS & DIALOGS
    // ==========================================

    // Account Modal / Edit Profile Dialog
    if (showAccountDialog) {
        var editName by remember { mutableStateOf(currentName) }
        var editSchool by remember { mutableStateOf(currentSchool) }
        var editClass by remember { mutableStateOf(currentClass) }

        Dialog(onDismissRequest = { showAccountDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("account_dialog"),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Student Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(
                            onClick = { showAccountDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_name_input"),
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
                            .testTag("account_class_input"),
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
                            .testTag("account_school_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch to Teacher View link
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAccountDialog = false
                                onNavigateToTeacher()
                            },
                        color = SoftLavenderGrey,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.SwapHoriz,
                                    contentDescription = null,
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Switch to Teacher View",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IndigoPurple
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = IndigoPurple,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAccountDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                currentName = editName.trim()
                                currentSchool = editSchool.trim()
                                currentClass = editClass.trim()
                                onUpdateProfile(currentName, currentSchool, currentClass)
                                showAccountDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Profile updated successfully")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_account_button")
                        ) {
                            Text("Save Changes", color = Color.White)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(
                            onClick = { showSettingsDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

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

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                showSettingsDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // About Modal
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("about_dialog"),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "About Catch Up",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(
                            onClick = { showAboutDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Version 1.4.0 (Uganda Secondary Curriculum)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = IndigoPurple
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Catch Up empowers secondary students and teachers across Uganda with instant, offline-first access to high-quality syllabus revision notes, practice assignments, and curriculum guides.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF4A4A4A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Crafted for Jjungo Senior Secondary School and secondary learners nationwide.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showAboutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Help & Support Modal
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("help_dialog"),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Help & Support",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(
                            onClick = { showHelpDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Need assistance with your notes, downloads, or account?",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = IndigoPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "support@catchup.ac.ug",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // School coordinator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.School, contentDescription = null, tint = IndigoPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "School IT Coordinator: Room B4",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showHelpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Got it", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Privacy Policy Modal
    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("privacy_dialog"),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privacy Policy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(
                            onClick = { showPrivacyDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Catch Up is designed with student privacy in mind:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• All downloaded revision notes are cached locally on your device.\n• Your study streak and reading time metrics remain private to your student profile.\n• No unauthorized personal data is sold or shared with third-party advertisers.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF4A4A4A)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showPrivacyDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of Catch Up? Offline notes will remain saved on this device.",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged out")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE76F51))
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
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
