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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import com.example.ui.pencilSketchBorder

private val AccountGoldAccent = Color(0xFFFFB300)
private val AccountBrightGold = Color(0xFFFFD54F)
private val AccountPageBg = Color(0xFFF4F2FA)
private val AccountFieldBg = Color(0xFFF8F7FC)

/**
 * Teacher Account Screen.
 *
 * Simplified, organized, and colorful full-screen page for Teacher Profile Details,
 * Subject Management (via colorful cards + popup selector), and Account Security.
 */
@Composable
fun TeacherAccountScreen(
    modifier: Modifier = Modifier,
    teacherName: String = "Teacher",
    teacherIdCode: String = DEFAULT_TEACHER_ID_CODE,
    subjectsTaught: String = "Secondary Curriculum",
    schoolName: String = "Secondary School",
    onNavigateBack: () -> Unit = {},
    onUpdateProfile: (name: String, school: String, subjects: String) -> Unit = { _, _, _ -> },
    onSwitchToStudentView: () -> Unit = {},
    onArchiveAndStartAfresh: ((newCode: String) -> Unit)? = null,
    onDeleteAccount: () -> Unit = {}
) {
    var editName by remember(teacherName) { mutableStateOf(teacherName) }
    var editSubjects by remember(subjectsTaught) { mutableStateOf(subjectsTaught) }
    var editSchool by remember(schoolName) { mutableStateOf(schoolName) }
    var currentTeacherId by remember(teacherIdCode) { mutableStateOf(teacherIdCode) }

    var showArchiveConfirmDialog by remember { mutableStateOf(false) }
    var archiveSuccessCode by remember { mutableStateOf<String?>(null) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(subjectsTaught) {
        if (subjectsTaught.isNotBlank()) {
            editSubjects = subjectsTaught
        }
    }

    val normalizedTeacherId = remember(currentTeacherId, editName) {
        val candidate = currentTeacherId.trim().uppercase()
        if (isValidTeacherIdCode(candidate)) candidate else generateTeacherIdCode(editName)
    }

    val activeSubjectSet = remember(editSubjects) {
        editSubjects.split(",", ";", "|", "/")
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals("Secondary Curriculum", ignoreCase = true) }
            .toSet()
    }

    val selectedSubjectItems = remember(activeSubjectSet) {
        DefaultCurriculumSubjects.filter { subject ->
            activeSubjectSet.any { it.equals(subject.name, ignoreCase = true) }
        }
    }

    // Subject selector popup dialog state
    var showSubjectsDialog by remember { mutableStateOf(false) }
    var tempSelectedSubjects by remember { mutableStateOf<Set<String>>(emptySet()) }

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
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_account_screen"),
        containerColor = AccountPageBg,
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
            // 1. COLORFUL HERO HEADER
            // Deep IndigoPurple (#4B3F8F) with rounded bottom corners (28dp),
            // Back button, centered title, and Teacher Identity Summary card
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
                    .testTag("teacher_account_header")
            ) {
                // Top Bar Row
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .testTag("teacher_account_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "Teacher Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("teacher_account_title")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Identity & Teacher ID Summary Banner inside Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Avatar with Gold border
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(AccountBrightGold)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                val initial = editName
                                    .replace("Mr.", "", ignoreCase = true)
                                    .replace("Mrs.", "", ignoreCase = true)
                                    .replace("Ms.", "", ignoreCase = true)
                                    .replace("Tr.", "", ignoreCase = true)
                                    .trim()
                                    .firstOrNull()
                                    ?.uppercase() ?: "T"
                                Text(
                                    text = initial,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = IndigoPurple
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = editName.ifBlank { "Teacher" },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.Verified,
                                        contentDescription = "Verified Educator",
                                        tint = AccountBrightGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = editSchool.ifBlank { "Secondary School" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LightLavender,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Copyable Teacher ID Badge
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(normalizedTeacherId))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Teacher ID $normalizedTeacherId copied")
                                    }
                                }
                                .testTag("teacher_account_id_badge"),
                            shape = RoundedCornerShape(12.dp),
                            color = AccountGoldAccent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TEACHER ID",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A).copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = normalizedTeacherId,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy Teacher ID",
                                    tint = Color(0xFF1A1A1A),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. ORGANIZED SCROLLABLE BODY
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // SECTION CARD 1: Profile & Subjects
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x14000000),
                            spotColor = Color(0x18000000)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Colorful Card Top Accent Strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(TealTile)
                        )

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Card Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(TealTile.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = TealTile,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Profile Information",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Update your name, school & teaching subjects",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            // Teacher Name Input
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Teacher Name / Title") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pencilSketchBorder()
                                    .testTag("teacher_account_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedLabelColor = IndigoPurple,
                                    unfocusedContainerColor = AccountFieldBg,
                                    focusedContainerColor = Color.White
                                )
                            )

                            // School Name Input
                            OutlinedTextField(
                                value = editSchool,
                                onValueChange = { editSchool = it },
                                label = { Text("School Name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = TealTile,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pencilSketchBorder()
                                    .testTag("teacher_account_school_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedLabelColor = TealTile,
                                    unfocusedContainerColor = AccountFieldBg,
                                    focusedContainerColor = Color.White
                                )
                            )

                            // Subjects Taught Header + Count Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = null,
                                        tint = CoralTile,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Subjects You Teach",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedSubjectItems.isNotEmpty()) CoralTile.copy(alpha = 0.14f) else SoftLavenderGrey
                                ) {
                                    Text(
                                        text = "${selectedSubjectItems.size} selected",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedSubjectItems.isNotEmpty()) CoralTile else TextMuted,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Colorful Badges for Currently Selected Subjects
                            if (selectedSubjectItems.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("teacher_account_subject_chips"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedSubjectItems.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowItems.forEach { subject ->
                                                val chipId = subject.name.lowercase().replace(" ", "_")
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(42.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            tempSelectedSubjects = activeSubjectSet
                                                            showSubjectsDialog = true
                                                        }
                                                        .testTag("teacher_account_subject_chip_$chipId"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = subject.color
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(
                                                                imageVector = subject.icon,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = subject.name,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = Icons.Outlined.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            if (rowItems.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            // Clean Dropdown Field Trigger to open Subject Popup
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = editSubjects,
                                    onValueChange = { editSubjects = it },
                                    readOnly = true,
                                    label = { Text("Tap to select or edit subjects") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                tempSelectedSubjects = activeSubjectSet
                                                showSubjectsDialog = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (showSubjectsDialog) {
                                                    Icons.Default.KeyboardArrowUp
                                                } else {
                                                    Icons.Default.KeyboardArrowDown
                                                },
                                                contentDescription = "Select subjects",
                                                tint = IndigoPurple
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("teacher_account_subjects_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IndigoPurple,
                                        unfocusedBorderColor = BorderGrey,
                                        focusedLabelColor = IndigoPurple,
                                        unfocusedContainerColor = AccountFieldBg,
                                        focusedContainerColor = Color.White
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            tempSelectedSubjects = activeSubjectSet
                                            showSubjectsDialog = true
                                        }
                                        .testTag("teacher_account_subjects_trigger")
                                )
                            }

                            // Save Profile Button
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
                                colors = ButtonDefaults.buttonColors(containerColor = TealTile),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("save_teacher_account_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save Profile",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION CARD 2: Security & Password
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x14000000),
                            spotColor = Color(0x18000000)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Colorful Card Top Accent Strip (Royal Blue)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(BlueTile)
                        )

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(BlueTile.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = BlueTile,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Change Password",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Update your password to keep your account secure",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Current Password
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = {
                                    currentPassword = it
                                    passwordError = null
                                    passwordSuccess = null
                                },
                                label = { Text("Current Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = BlueTile,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
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
                                    focusedBorderColor = BlueTile,
                                    unfocusedBorderColor = BorderGrey,
                                    focusedLabelColor = BlueTile,
                                    unfocusedContainerColor = AccountFieldBg,
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
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = BlueTile,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
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
                                    focusedBorderColor = BlueTile,
                                    unfocusedBorderColor = BorderGrey,
                                    focusedLabelColor = BlueTile,
                                    unfocusedContainerColor = AccountFieldBg,
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
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = BlueTile,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
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
                                    focusedBorderColor = BlueTile,
                                    unfocusedBorderColor = BorderGrey,
                                    focusedLabelColor = BlueTile,
                                    unfocusedContainerColor = AccountFieldBg,
                                    focusedContainerColor = Color.White
                                )
                            )

                            // Password Feedback Message
                            if (passwordError != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE53935).copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = passwordError!!,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE53935),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("teacher_password_error_text")
                                    )
                                }
                            }
                            if (passwordSuccess != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = TealTile.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = passwordSuccess!!,
                                        fontSize = 12.sp,
                                        color = TealTile,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("teacher_password_success_text")
                                    )
                                }
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
                                colors = ButtonDefaults.buttonColors(containerColor = BlueTile),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("update_teacher_password_button")
                            ) {
                                Text(
                                    text = "Update Password",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION CARD 3: School Transfer & Account Archive (Start Afresh)
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x14000000),
                            spotColor = Color(0x18000000)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Top accent strip (Amber/Gold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(AccountBrightGold)
                        )

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(AccountBrightGold.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "School Transfer & Archive",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Transferred or teaching new students? Start fresh with a new ID",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Informational card explaining how it works
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftLavenderGrey.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, BorderGrey.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(text = "• ", fontWeight = FontWeight.Bold, color = IndigoPurple)
                                        Text(
                                            text = "Generates a fresh 6-digit Teacher ID for your new learners.",
                                            fontSize = 12.sp,
                                            color = TextDark
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(text = "• ", fontWeight = FontWeight.Bold, color = IndigoPurple)
                                        Text(
                                            text = "All your previous notes remain safe in your Uploads library to share anytime without re-uploading.",
                                            fontSize = 12.sp,
                                            color = TextDark
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(text = "• ", fontWeight = FontWeight.Bold, color = IndigoPurple)
                                        Text(
                                            text = "Students linked to your old ID are automatically removed and will be asked to add their new teacher's ID.",
                                            fontSize = 12.sp,
                                            color = TextDark
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { showArchiveConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("teacher_archive_account_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sync,
                                    contentDescription = null,
                                    tint = Color(0xFF1A1A1A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Archive Account & Start Afresh",
                                    color = Color(0xFF1A1A1A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // SECTION 5: DANGER ZONE — ACCOUNT DELETION
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Danger Zone",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralTile
                            )
                        }

                        Text(
                            text = "Permanently delete your Teacher profile, syllabus uploads, and student links. This cannot be undone.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = { showDeleteAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralTile),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("teacher_delete_account_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Teacher Account Permanently",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Confirmation dialog before archiving account
    if (showArchiveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = IndigoPurple,
            modifier = Modifier.testTag("teacher_archive_confirm_dialog"),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFB300).copy(alpha = 0.20f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "Transfer",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Start Afresh / Transfer?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Are you transferring to a new school or teaching new students?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "• A new 6-digit Teacher ID will be generated for your new school.\n• All previously uploaded notes will be kept safe in your Uploads library to share easily.\n• Previous students will be disconnected and requested to add their new teacher's ID.",
                        fontSize = 12.sp,
                        color = LightLavender,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showArchiveConfirmDialog = false
                        val freshCode = generateFreshTeacherIdCode(editName, editSubjects)
                        currentTeacherId = freshCode
                        onArchiveAndStartAfresh?.invoke(freshCode)
                        archiveSuccessCode = freshCode
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_archive_button")
                ) {
                    Text(
                        text = "Yes, Archive & Start Afresh",
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showArchiveConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
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

    // Success dialog showing the new Teacher ID
    val successCode = archiveSuccessCode
    if (successCode != null) {
        val newCode = successCode
        AlertDialog(
            onDismissRequest = { archiveSuccessCode = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = IndigoPurple,
            modifier = Modifier.testTag("teacher_archive_success_dialog"),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFB300).copy(alpha = 0.20f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = "Success",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "New Teacher ID Generated!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Give this new ID to your new students:",
                        fontSize = 13.sp,
                        color = LightLavender
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFB300),
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(newCode))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Copied $newCode")
                            }
                        }
                    ) {
                        Text(
                            text = newCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A),
                            letterSpacing = 4.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                    Text(
                        text = "Previous students have been disconnected. Your existing notes are saved in your Uploads library ready to share!",
                        fontSize = 12.sp,
                        color = LightLavender,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { archiveSuccessCode = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Done",
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        )
    }

    // ==========================================================
    // TEACHER SUBJECTS POPUP DIALOG (Matches Signup Subject Selector)
    // ==========================================================
    if (showSubjectsDialog) {
        val popupScrollState = rememberScrollState()

        Dialog(
            onDismissRequest = { showSubjectsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 560.dp)
                    .testTag("teacher_account_subjects_dialog"),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF241142),
                border = BorderStroke(2.dp, AccountBrightGold),
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Popup Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Subjects You Teach",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mark all subjects that you teach and tap Save",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightLavender
                            )
                        }

                        IconButton(
                            onClick = { showSubjectsDialog = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close popup",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Scrollable 2-Column Subject Selection Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(popupScrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DefaultCurriculumSubjects.chunked(2).forEach { rowSubjects ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowSubjects.forEach { subjectItem ->
                                    val isSelected = tempSelectedSubjects.any {
                                        it.equals(subjectItem.name, ignoreCase = true)
                                    }
                                    val safeTag = subjectItem.name.lowercase().replace(" ", "_")
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                tempSelectedSubjects = if (isSelected) {
                                                    tempSelectedSubjects
                                                        .filterNot { it.equals(subjectItem.name, ignoreCase = true) }
                                                        .toSet()
                                                } else {
                                                    tempSelectedSubjects + subjectItem.name
                                                }
                                            }
                                            .testTag("teacher_account_popup_subject_$safeTag"),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) subjectItem.color else Color.White.copy(alpha = 0.12f),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) AccountBrightGold else Color.White.copy(alpha = 0.28f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = subjectItem.icon,
                                                    contentDescription = subjectItem.name,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = subjectItem.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) AccountGoldAccent else Color.White.copy(alpha = 0.18f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color(0xFF1A1A1A),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
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

                    // Save Button
                    Button(
                        onClick = {
                            val orderedNames = DefaultCurriculumSubjects
                                .filter { subject ->
                                    tempSelectedSubjects.any { it.equals(subject.name, ignoreCase = true) }
                                }
                                .map { it.name }
                            editSubjects = orderedNames.joinToString(", ")
                            showSubjectsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccountGoldAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("teacher_account_subjects_save_button")
                    ) {
                        Text(
                            text = "Save (${tempSelectedSubjects.size} Selected)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }
    }

    // Delete Teacher Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Delete Teacher Account Permanently?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CoralTile
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This action is permanent and cannot be reversed.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Your teacher code, uploaded curriculum notes, syllabus materials, and student connection records will be permanently erased from Cachap servers in accordance with Google Play data safety policies.",
                        fontSize = 13.5.sp,
                        color = TextMuted,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralTile),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("teacher_account_confirm_delete_button")
                ) {
                    Text(
                        text = "Delete Forever",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    modifier = Modifier.testTag("teacher_account_cancel_delete_button")
                ) {
                    Text(
                        text = "Keep Account",
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            modifier = Modifier.testTag("teacher_account_delete_dialog")
        )
    }
}

