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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * - Existing purple Header (~200dp) with avatar, teacher name, subjects taught
 * - White body with clean vertical stack of pill-shaped rows:
 *   1. "Account" (View profile, update details, change password)
 *   2. "Help & Support"
 *   3. "About"
 *   4. "Privacy Policy"
 *   5. "Log Out" (with small exit/arrow icon on the right side)
 * - Reused BottomNavigationBar composable with TeacherNavItems (Profile tab active)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Teacher",
    subjectsTaught: String = "Secondary Curriculum",
    schoolName: String = "Secondary School",
    onNavigateToHome: () -> Unit = {},
    onNavigateToMySubjects: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToTeacherProfile: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToHelpAndSupport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onUpdateProfile: (name: String, school: String, subjects: String) -> Unit = { _, _, _ -> },
    onSwitchToStudentView: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var currentTeacherName by remember { mutableStateOf(teacherName) }
    var currentSubjects by remember { mutableStateOf(subjectsTaught) }
    var currentSchool by remember { mutableStateOf(schoolName) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                            .clickable { onNavigateToAccount() }
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
                // 1. "Account" (View profile, update details, change password)
                ProfilePillRow(
                    text = "Account",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "teacher_pill_account",
                    onClick = onNavigateToAccount
                )

                // 2. "Help & Support"
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

                // 3. "About"
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

                // 4. "Privacy Policy"
                ProfilePillRow(
                    text = "Privacy Policy",
                    height = 52.dp,
                    cornerRadius = 26.dp,
                    backgroundColor = IndigoPurple,
                    textColor = Color.White,
                    startPadding = 20.dp,
                    elevation = 2.dp,
                    testTag = "teacher_pill_privacy_policy",
                    onClick = onNavigateToPrivacyPolicy
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
                            text = "teachers@cachap.ac.ug",
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
                            text = "About Cachap Teacher Portal",
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
                        text = "The Cachap Teacher Portal allows educators to distribute syllabus-aligned revision materials, track student study engagement, and publish coursework for secondary school classes across Uganda.",
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
            shape = RoundedCornerShape(22.dp),
            containerColor = IndigoPurple,
            modifier = Modifier.testTag("teacher_logout_dialog"),
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
                    text = "Log Out",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of the Teacher Portal?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightLavender,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged out")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("teacher_confirm_logout_button")
                ) {
                    Text(
                        text = "Log Out",
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.ExtraBold
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
                    modifier = Modifier.testTag("teacher_cancel_logout_button")
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
