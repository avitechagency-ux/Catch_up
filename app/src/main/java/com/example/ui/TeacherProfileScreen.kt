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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
 * Teacher Profile Screen.
 *
 * Layout Structure:
 * - Existing purple Header (~200dp) with avatar, teacher name ("Mr. Okello"), subjects taught
 * - White body with clean vertical stack of pill-shaped outlined rows:
 *   - Top Identity row (60dp, 24dp avatar icon on left, teacher name)
 *   - 14dp vertical gaps
 *   1. "Account"
 *   2. "Settings"
 *   3. "Help & Support"
 *   4. "About"
 *   5. "Log Out" (with small exit/arrow icon on the right side)
 * - Reused BottomNavigationBar composable with TeacherNavItems (Profile tab active)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Mr. Okello",
    subjectsTaught: String = "Geography • S.4, S.5",
    schoolName: String = "Jjungo Senior Secondary School",
    onNavigateToHome: () -> Unit = {},
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelpAndSupport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onSwitchToStudentView: () -> Unit = {}
) {
    var currentTeacherName by remember { mutableStateOf(teacherName) }
    var currentSubjects by remember { mutableStateOf(subjectsTaught) }
    var currentSchool by remember { mutableStateOf(schoolName) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAccountDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Settings state
    var uploadNotificationsEnabled by remember { mutableStateOf(true) }
    var highQualityUploadsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_profile_screen"),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 3, // "Profile" is index 3 in TeacherNavItems
                items = TeacherNavItems,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToMySubjects()
                        2 -> onNavigateToUploads()
                        3 -> onNavigateToTeacherProfile()
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
                    .testTag("teacher_profile_header"),
                contentAlignment = Alignment.Center
            ) {
                // Switch to Student Profile button on top right of the banner
                Surface(
                    onClick = onSwitchToStudentView,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp)
                        .testTag("teacher_switch_to_student_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = "Switch to Student View",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Student",
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
                            .testTag("teacher_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = currentTeacherName.replace("Mr.", "").replace("Mrs.", "").replace("Ms.", "").trim().firstOrNull()?.uppercase() ?: "O"
                        Text(
                            text = initial,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Teacher name: 18sp Bold #FFFFFF
                    Text(
                        text = currentTeacherName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("teacher_profile_name")
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Subjects / School: 13sp Regular #D6D0F0
                    Text(
                        text = "$currentSubjects • $currentSchool",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("teacher_profile_subjects")
                    )
                }
            }

            // ==========================================
            // 2. WHITE BODY — Vertical Menu of Outlined Pill Rows
            // 20dp left/right padding, 20dp top padding, 14dp spacing between rows
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-14).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // TOP IDENTITY ROW (60dp height, 26dp corner radius, 24dp white avatar circle with purple initial, teacher name in white)
                ProfilePillRow(
                    text = currentTeacherName,
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
                            val initial = currentTeacherName.replace("Mr.", "").replace("Mrs.", "").replace("Ms.", "").trim().firstOrNull()?.uppercase() ?: "O"
                            Text(
                                text = initial,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple
                            )
                        }
                    },
                    testTag = "teacher_identity_pill",
                    onClick = { showAccountDialog = true }
                )

                // 1. "Account"
                ProfilePillRow(
                    text = "Account",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "teacher_pill_account",
                    onClick = {
                        onNavigateToAccount()
                        showAccountDialog = true
                    }
                )

                // 2. "Settings"
                ProfilePillRow(
                    text = "Settings",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "teacher_pill_settings",
                    onClick = onNavigateToSettings
                )

                // 3. "Help & Support"
                ProfilePillRow(
                    text = "Help & Support",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "teacher_pill_help_and_support",
                    onClick = onNavigateToHelpAndSupport
                )

                // 4. "About"
                ProfilePillRow(
                    text = "About",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "teacher_pill_about",
                    onClick = onNavigateToAbout
                )

                // 5. "Log Out" (with small arrow/exit icon on the right side)
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
                    testTag = "teacher_pill_log_out",
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }

    // ==========================================
    // MODALS & DIALOGS
    // ==========================================

    // Account Modal / Edit Teacher Profile Dialog
    if (showAccountDialog) {
        var editName by remember { mutableStateOf(currentTeacherName) }
        var editSubjects by remember { mutableStateOf(currentSubjects) }
        var editSchool by remember { mutableStateOf(currentSchool) }

        Dialog(onDismissRequest = { showAccountDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("teacher_account_dialog"),
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
                            text = "Teacher Account",
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
                        label = { Text("Teacher Name / Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_account_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editSubjects,
                        onValueChange = { editSubjects = it },
                        label = { Text("Assigned Subjects & Classes") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_account_subjects_input"),
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
                            .testTag("teacher_account_school_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch to Student View link
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAccountDialog = false
                                onSwitchToStudentView()
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
                                    text = "Switch to Student View",
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
                                currentTeacherName = editName.trim()
                                currentSubjects = editSubjects.trim()
                                currentSchool = editSchool.trim()
                                showAccountDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Teacher profile updated")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_teacher_account_button")
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
                    .testTag("teacher_settings_dialog"),
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
                            text = "Teacher Settings",
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
                                text = "Upload Notifications",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Text(
                                text = "Notify when students download uploaded notes",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = uploadNotificationsEnabled,
                            onCheckedChange = { uploadNotificationsEnabled = it },
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
                                text = "High-Quality Compression",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Text(
                                text = "Optimize PDF size for low-bandwidth student downloads",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = highQualityUploadsEnabled,
                            onCheckedChange = { highQualityUploadsEnabled = it },
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

    // Help & Support Modal
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("teacher_help_dialog"),
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
                            text = "Teacher Help & Support",
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
                        text = "Need help managing uploaded notes, syllabus topics, or student access?",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = IndigoPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "teachers@catchup.ac.ug",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.School, contentDescription = null, tint = IndigoPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Academic Dean Office / Staffroom Ext. 104",
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

    // About Modal
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("teacher_about_dialog"),
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
                            text = "About Catch Up Teacher Portal",
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
                        text = "Version 1.4.0 (Teacher Edition)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = IndigoPurple
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The Catch Up Teacher Portal allows educators to distribute syllabus-aligned revision materials, track student study engagement, and publish coursework for secondary school classes across Uganda.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF4A4A4A)
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
                    text = "Are you sure you want to log out of the Teacher Portal?",
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
fun TeacherProfileScreenPreview() {
    TeacherProfileScreen()
}
