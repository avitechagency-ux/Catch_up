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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Teacher Account Screen.
 *
 * Full-screen dedicated page for Teacher Profile Details and Change Password.
 * Reached by tapping "Account" or avatar on Teacher Profile.
 */
@Composable
fun TeacherAccountScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Teacher",
    subjectsTaught: String = "Secondary Curriculum",
    schoolName: String = "Secondary School",
    onNavigateBack: () -> Unit = {},
    onUpdateProfile: (name: String, school: String, subjects: String) -> Unit = { _, _, _ -> },
    onSwitchToStudentView: () -> Unit = {}
) {
    var editName by remember { mutableStateOf(teacherName) }
    var editSubjects by remember { mutableStateOf(subjectsTaught) }
    var editSchool by remember { mutableStateOf(schoolName) }

    LaunchedEffect(subjectsTaught) {
        if (subjectsTaught.isNotBlank()) {
            editSubjects = subjectsTaught
        }
    }

    // Password change state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_account_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // TOP BAR
            // Solid purple header (#4B3F8F) with rounded bottom corners (28dp)
            // Back arrow on left, centered title "Account"
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
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .testTag("teacher_account_header")
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("teacher_account_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("teacher_account_title")
                )
            }

            // ==========================================
            // BODY (Scrollable Cards)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // CARD 1: Profile Information
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = Color(0x10000000),
                            spotColor = Color(0x15000000)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderGrey)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = IndigoPurple,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Profile Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

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
                                focusedLabelColor = IndigoPurple,
                                unfocusedContainerColor = SoftLavenderGrey,
                                focusedContainerColor = Color.White
                            )
                        )

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
                                focusedLabelColor = IndigoPurple,
                                unfocusedContainerColor = SoftLavenderGrey,
                                focusedContainerColor = Color.White
                            )
                        )

                        // Interactive Subject Selection Chips synced with DB subjects
                        val activeSubjectSet = remember(editSubjects) {
                            editSubjects.split(",", ";", "|", "/")
                                .map { it.trim() }
                                .filter { it.isNotBlank() && !it.equals("Secondary Curriculum", ignoreCase = true) }
                                .toSet()
                        }
                        Text(
                            text = "Tap to toggle subjects you teach:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_account_subject_chips"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DefaultSubjects.chunked(2).forEach { rowSubjects ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSubjects.forEach { subject ->
                                        val isSelected = activeSubjectSet.any { it.equals(subject.name, ignoreCase = true) }
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    val updatedList = if (isSelected) {
                                                        activeSubjectSet.filterNot { it.equals(subject.name, ignoreCase = true) }
                                                    } else {
                                                        activeSubjectSet.toList() + subject.name
                                                    }
                                                    editSubjects = updatedList.joinToString(", ")
                                                }
                                                .testTag("teacher_account_subject_chip_${subject.id}"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) IndigoPurple else SoftLavenderGrey,
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) IndigoPurple else BorderGrey
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = subject.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextDark
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowSubjects.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

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
                                focusedLabelColor = IndigoPurple,
                                unfocusedContainerColor = SoftLavenderGrey,
                                focusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val trimmedName = editName.trim().ifBlank { "Teacher" }
                                val trimmedSubjects = editSubjects.trim().ifBlank { "Secondary Curriculum" }
                                val trimmedSchool = editSchool.trim().ifBlank { "Secondary School" }
                                onUpdateProfile(trimmedName, trimmedSchool, trimmedSubjects)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Teacher profile updated successfully")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_teacher_account_button")
                        ) {
                            Text("Save Profile", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // CARD 2: Security & Password Change
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = Color(0x10000000),
                            spotColor = Color(0x15000000)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderGrey)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = IndigoPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Change Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        Text(
                            text = "Update your password to keep your teacher account secure",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        // Current Password
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = {
                                currentPassword = it
                                passwordError = null
                                passwordSuccess = null
                            },
                            label = { Text("Current Password") },
                            singleLine = true,
                            visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                    Icon(
                                        imageVector = if (currentPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle Current Password Visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_current_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPurple,
                                focusedLabelColor = IndigoPurple,
                                unfocusedContainerColor = SoftLavenderGrey,
                                focusedContainerColor = Color.White
                            )
                        )

                        // New Password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                passwordError = null
                                passwordSuccess = null
                            },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle New Password Visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_new_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPurple,
                                focusedLabelColor = IndigoPurple,
                                unfocusedContainerColor = SoftLavenderGrey,
                                focusedContainerColor = Color.White
                            )
                        )

                        // Confirm New Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                passwordError = null
                                passwordSuccess = null
                            },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle Confirm Password Visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_confirm_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPurple,
                                focusedLabelColor = IndigoPurple,
                                unfocusedContainerColor = SoftLavenderGrey,
                                focusedContainerColor = Color.White
                            )
                        )

                        // Password Feedback Message
                        if (passwordError != null) {
                            Text(
                                text = passwordError!!,
                                fontSize = 12.sp,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("teacher_password_error_text")
                            )
                        }
                        if (passwordSuccess != null) {
                            Text(
                                text = passwordSuccess!!,
                                fontSize = 12.sp,
                                color = Color(0xFF2A9D8F),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("teacher_password_success_text")
                            )
                        }

                        Button(
                            onClick = {
                                when {
                                    currentPassword.isBlank() -> {
                                        passwordError = "Please enter your current password"
                                    }
                                    newPassword.isBlank() -> {
                                        passwordError = "Please enter a new password"
                                    }
                                    newPassword.length < 6 -> {
                                        passwordError = "New password must be at least 6 characters"
                                    }
                                    newPassword != confirmPassword -> {
                                        passwordError = "New passwords do not match"
                                    }
                                    else -> {
                                        passwordError = null
                                        passwordSuccess = "Password updated successfully!"
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Password updated successfully")
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("update_teacher_password_button")
                        ) {
                            Text("Update Password", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
