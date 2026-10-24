package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import com.example.data.AccountSecurityVault
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SupabaseRepository
import com.example.data.SupabaseSecurityVault
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import kotlinx.coroutines.launch

val UgandanSecondaryClasses = listOf("S.1", "S.2", "S.3", "S.4", "S.5", "S.6")

private val PaymentGold = Color(0xFFFFB300)
private val PaymentBrightGold = Color(0xFFFFD54F)
private val PaymentCrimson = Color(0xFFE53935)
private val PaymentCrimsonBorder = Color(0xFFFF8A80)
private val PaymentDarkText = Color(0xFF1A1A1A)

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    initialRole: String = "Student",
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onSignupSuccess: (
        role: String,
        email: String,
        name: String,
        studentClass: String,
        studentStream: String,
        teacherSubjects: String
    ) -> Unit = { _, _, _, _, _, _ -> }
) {
    val normalizedInitialRole = if (initialRole.equals("Teacher", ignoreCase = true)) "Teacher" else "Student"

    // Exact field order requested:
    // 1. Full Name
    // 2. Email
    // 3. Role Selection (Student or Teacher)
    // 4. Password
    // 5a. If Student -> Class and Stream (written individually) + Selected Plan
    // 5b. If Teacher -> Select Subjects You Teach (from DefaultCurriculumSubjects)
    // 6. Submit / Sign Up
    var fullNameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(normalizedInitialRole) }
    var passwordInput by remember { mutableStateOf("") }
    var classInput by remember { mutableStateOf("") }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var streamInput by remember { mutableStateOf("") }
    var selectedTeacherSubjects by remember { mutableStateOf(setOf<String>()) }
    var showTeacherSubjectsDialog by remember { mutableStateOf(false) }
    var tempTeacherSubjects by remember { mutableStateOf(setOf<String>()) }
    var subjectSearchQuery by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    remember(context) {
        SupabaseRepository.initialize(context)
        true
    }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isStudentRole = selectedRole.equals("Student", ignoreCase = true)

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("signup_screen"),
        containerColor = IndigoPurple,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(IndigoPurple)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("signup_header"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================================
            // TOP HEADER: Centered "Sign Up" Title Only
            // ==========================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("signup_title")
                )

                IconButton(
                    onClick = onNavigateBack,
                    enabled = !isLoading,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .testTag("signup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ==========================================================
            // 1. FULL NAME
            // ==========================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Full Name",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = fullNameInput,
                    onValueChange = {
                        fullNameInput = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "Enter your full name",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = PaymentBrightGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                        focusedBorderColor = PaymentBrightGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        cursorColor = PaymentBrightGold,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // ==========================================================
            // 2. EMAIL
            // ==========================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Email",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "Enter your email",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = PaymentBrightGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                        focusedBorderColor = PaymentBrightGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        cursorColor = PaymentBrightGold,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // ==========================================================
            // 3. SELECT ROLE (EITHER STUDENT OR TEACHER — Payment Popup Style)
            // ==========================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Role",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Student Option (Styled like MTN MoMo card in Payment Popup)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedRole = "Student"
                                errorMessage = null
                            }
                            .testTag("signup_role_pill_Student"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isStudentRole) PaymentGold else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(
                            1.5.dp,
                            if (isStudentRole) PaymentBrightGold else Color.White.copy(alpha = 0.30f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isStudentRole,
                                onClick = {
                                    selectedRole = "Student"
                                    errorMessage = null
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isStudentRole) PaymentDarkText else Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Student",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isStudentRole) PaymentDarkText else Color.White
                            )
                        }
                    }

                    // Teacher Option (Styled like Airtel Money card in Payment Popup)
                    val isTeacherRole = !isStudentRole
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedRole = "Teacher"
                                errorMessage = null
                            }
                            .testTag("signup_role_pill_Teacher"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isTeacherRole) PaymentCrimson else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(
                            1.5.dp,
                            if (isTeacherRole) PaymentCrimsonBorder else Color.White.copy(alpha = 0.30f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isTeacherRole,
                                onClick = {
                                    selectedRole = "Teacher"
                                    errorMessage = null
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Teacher",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ==========================================================
            // 4. PASSWORD
            // ==========================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Password",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "Create a password",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = PaymentBrightGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = "Toggle password visibility",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                        focusedBorderColor = PaymentBrightGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        cursorColor = PaymentBrightGold,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // ==========================================================
            // 5. IF STUDENT -> CLASS (DROPDOWN S.1 - S.6) & STREAM
            // ==========================================================
            AnimatedVisibility(visible = isStudentRole) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Class Dropdown Field (S.1 - S.6 only)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Class",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = classInput,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "Select your class (S.1 - S.6)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = PaymentBrightGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { classDropdownExpanded = !classDropdownExpanded }
                                    ) {
                                        Icon(
                                            imageVector = if (classDropdownExpanded) {
                                                Icons.Default.KeyboardArrowUp
                                            } else {
                                                Icons.Default.KeyboardArrowDown
                                            },
                                            contentDescription = "Select class",
                                            tint = PaymentBrightGold
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_class_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                    focusedBorderColor = if (classDropdownExpanded) PaymentBrightGold else Color.White.copy(alpha = 0.35f),
                                    unfocusedBorderColor = if (classDropdownExpanded) PaymentBrightGold else Color.White.copy(alpha = 0.35f),
                                    cursorColor = PaymentBrightGold,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            // Transparent clickable overlay so tapping anywhere on the field opens the dropdown
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        classDropdownExpanded = !classDropdownExpanded
                                    }
                                    .testTag("signup_class_dropdown_trigger")
                            )

                            DropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .background(Color(0xFF281549))
                                    .border(
                                        width = 1.5.dp,
                                        color = PaymentBrightGold,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .testTag("signup_class_dropdown_menu")
                            ) {
                                UgandanSecondaryClasses.forEach { classOption ->
                                    val isSelected = classInput == classOption
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = classOption,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelected) PaymentBrightGold else Color.White
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = PaymentBrightGold,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            classInput = classOption
                                            errorMessage = null
                                            classDropdownExpanded = false
                                        },
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.14f)
                                                else Color.Transparent
                                            )
                                            .testTag("signup_class_option_$classOption")
                                    )
                                }
                            }
                        }
                    }

                    // Stream Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Stream Status",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val isStreamed = streamInput != "Unstreamed"
                            
                            // Streamed Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { streamInput = "" }
                                    .testTag("signup_stream_pill_Streamed"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isStreamed) PaymentGold else Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isStreamed) PaymentBrightGold else Color.White.copy(alpha = 0.30f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isStreamed,
                                        onClick = { streamInput = "" },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = if (isStreamed) PaymentDarkText else Color.White,
                                            unselectedColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Streamed",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isStreamed) PaymentDarkText else Color.White
                                    )
                                }
                            }

                            // Unstreamed Option
                            val isUnstreamed = streamInput == "Unstreamed"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { streamInput = "Unstreamed" }
                                    .testTag("signup_stream_pill_Unstreamed"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isUnstreamed) PaymentCrimson else Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isUnstreamed) PaymentCrimsonBorder else Color.White.copy(alpha = 0.30f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isUnstreamed,
                                        onClick = { streamInput = "Unstreamed" },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color.White,
                                            unselectedColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Unstreamed",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Individual Stream Field (Only show if Streamed)
                    if (streamInput != "Unstreamed") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Stream Name",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            OutlinedTextField(
                                value = streamInput,
                                onValueChange = { streamInput = it },
                                placeholder = { Text("Write your stream (e.g. Green)", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }
                    }
                }
            }

            // ==========================================================
            // 5B. IF TEACHER -> CLEAN SELECTOR FIELD + POPUP MODAL
            // ==========================================================
            AnimatedVisibility(visible = !isStudentRole) {
                val orderedSelectedList = remember(selectedTeacherSubjects) {
                    DefaultCurriculumSubjects
                        .filter { it.name in selectedTeacherSubjects }
                        .map { it.name }
                }
                val selectedDisplayValue = remember(orderedSelectedList) {
                    orderedSelectedList.joinToString(", ")
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_teacher_subjects_section"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subjects You Teach",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTeacherSubjects.isNotEmpty()) PaymentGold else Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(
                                1.dp,
                                if (selectedTeacherSubjects.isNotEmpty()) PaymentBrightGold else Color.White.copy(alpha = 0.30f)
                            )
                        ) {
                            Text(
                                text = "${selectedTeacherSubjects.size} selected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedTeacherSubjects.isNotEmpty()) PaymentDarkText else Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("signup_teacher_subjects_count")
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDisplayValue,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Tap to select subjects you teach",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Book,
                                    contentDescription = null,
                                    tint = PaymentBrightGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        tempTeacherSubjects = selectedTeacherSubjects
                                        showTeacherSubjectsDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (showTeacherSubjectsDialog) {
                                            Icons.Default.KeyboardArrowUp
                                        } else {
                                            Icons.Default.KeyboardArrowDown
                                        },
                                        contentDescription = "Select subjects",
                                        tint = PaymentBrightGold
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_teacher_subjects_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = if (selectedTeacherSubjects.isNotEmpty() || showTeacherSubjectsDialog) PaymentBrightGold else Color.White.copy(alpha = 0.35f),
                                unfocusedBorderColor = if (selectedTeacherSubjects.isNotEmpty() || showTeacherSubjectsDialog) PaymentBrightGold else Color.White.copy(alpha = 0.35f),
                                cursorColor = PaymentBrightGold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Full-field clickable trigger to open the Subjects Popup
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    tempTeacherSubjects = selectedTeacherSubjects
                                    showTeacherSubjectsDialog = true
                                }
                                .testTag("signup_teacher_subjects_trigger")
                        )
                    }
                }
            }

            // ==========================================================
            // TEACHER SUBJECTS POPUP DIALOG
            // ==========================================================
            if (showTeacherSubjectsDialog) {
                val popupScrollState = rememberScrollState()

                Dialog(
                    onDismissRequest = { showTeacherSubjectsDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .heightIn(max = 560.dp)
                            .testTag("signup_teacher_subjects_dialog"),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF241142),
                        border = BorderStroke(2.dp, PaymentBrightGold),
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
                                        text = "Mark all subjects that you teach and click Save",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightLavender
                                    )
                                }

                                IconButton(
                                    onClick = { showTeacherSubjectsDialog = false },
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
                                val subjectPairs = DefaultCurriculumSubjects.chunked(2)
                                subjectPairs.forEach { rowSubjects ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowSubjects.forEach { subjectItem ->
                                            val isSelected = subjectItem.name in tempTeacherSubjects
                                            val safeTag = subjectItem.name.lowercase().replace(" ", "_")
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        tempTeacherSubjects = if (isSelected) {
                                                            tempTeacherSubjects - subjectItem.name
                                                        } else {
                                                            tempTeacherSubjects + subjectItem.name
                                                        }
                                                    }
                                                    .testTag("signup_teacher_subject_$safeTag"),
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isSelected) subjectItem.color else Color.White.copy(alpha = 0.12f),
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) PaymentBrightGold else Color.White.copy(alpha = 0.28f)
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
                                                                if (isSelected) PaymentGold else Color.White.copy(alpha = 0.18f)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = PaymentDarkText,
                                                                modifier = Modifier.size(14.dp)
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

                            // Popup Footer: Selected summary + Cancel & Save buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showTeacherSubjectsDialog = false },
                                    modifier = Modifier
                                        .weight(0.38f)
                                        .height(48.dp)
                                        .testTag("signup_teacher_subjects_cancel_button")
                                ) {
                                    Text(
                                        text = "Cancel",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LightLavender
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedTeacherSubjects = tempTeacherSubjects
                                        if (tempTeacherSubjects.isNotEmpty()) {
                                            errorMessage = null
                                        }
                                        showTeacherSubjectsDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PaymentGold
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(0.62f)
                                        .height(48.dp)
                                        .testTag("signup_teacher_subjects_save_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PaymentDarkText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (tempTeacherSubjects.isEmpty()) {
                                            "Save"
                                        } else {
                                            "Save (${tempTeacherSubjects.size})"
                                        },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PaymentDarkText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = PaymentCrimson.copy(alpha = 0.25f),
                    border = BorderStroke(1.5.dp, PaymentCrimsonBorder)
                ) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("signup_error_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================================
            // 6. THEN LOGIN / SUBMIT BUTTON (Payment Popup Gold Button Style)
            // ==========================================================
            Button(
                onClick = {
                    when {
                        fullNameInput.isBlank() -> {
                            errorMessage = "Please enter your full name"
                        }
                        emailInput.isBlank() -> {
                            errorMessage = "Please enter your email"
                        }
                        passwordInput.isBlank() -> {
                            errorMessage = "Please enter a password"
                        }
                        passwordInput.length < 4 -> {
                            errorMessage = "Password must be at least 4 characters"
                        }
                        isStudentRole && classInput !in UgandanSecondaryClasses -> {
                            errorMessage = "Please select your Class (S.1 - S.6)"
                        }
                        isStudentRole && streamInput.isBlank() -> {
                            errorMessage = "Please write your Stream (e.g. Green or North)"
                        }
                        !isStudentRole && selectedTeacherSubjects.isEmpty() -> {
                            errorMessage = "Please select at least one subject you will be teaching"
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val cleanName = fullNameInput.trim()
                                val cleanEmail = emailInput.trim()
                                val cleanClass = if (isStudentRole) normalizeSecondaryClass(classInput) else ""
                                val cleanStream = if (isStudentRole) com.example.data.normalizeStream(streamInput) else ""
                                val cleanTeacherSubjects = if (!isStudentRole) {
                                    DefaultCurriculumSubjects
                                        .filter { it.name in selectedTeacherSubjects }
                                        .joinToString(", ") { it.name }
                                } else ""

                                val teacherCode = if (!isStudentRole) {
                                    generateTeacherIdCode(cleanName.ifBlank { cleanEmail })
                                } else null
                                SupabaseRepository.syncUserToSupabase(
                                    phoneNumber = cleanEmail,
                                    fullName = cleanName,
                                    role = selectedRole.lowercase(),
                                    plainPassword = passwordInput,
                                    academicClass = cleanClass,
                                    stream = cleanStream,
                                    teacherIdCode = teacherCode,
                                    subjectsTaught = cleanTeacherSubjects,
                                    isPlanActive = !isStudentRole,
                                    planDuration = if (isStudentRole) "1 Month" else "Teacher"
                                )
                                if (!isStudentRole && teacherCode != null) {
                                    SupabaseRepository.syncTeacherSubjects(
                                        teacherIdCode = teacherCode,
                                        subjectNames = DefaultCurriculumSubjects
                                            .filter { it.name in selectedTeacherSubjects }
                                            .map { it.name }
                                    )
                                }
                                runCatching {
                                    context.getSharedPreferences("catch_up_prefs", android.content.Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("user_password_hash", SupabaseSecurityVault.hashPassword(passwordInput))
                                        .apply()
                                }
                                isLoading = false
                                onSignupSuccess(
                                    selectedRole.lowercase(),
                                    cleanEmail,
                                    cleanName,
                                    cleanClass,
                                    cleanStream,
                                    cleanTeacherSubjects
                                )
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaymentGold,
                    disabledContainerColor = PaymentGold.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = PaymentDarkText,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Creating account...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaymentDarkText
                    )
                } else {
                    Text(
                        text = if (isStudentRole) "Continue to Payment" else "Sign Up & Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PaymentDarkText
                    )
                }
            }

            // Bottom Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightLavender
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.testTag("signup_login_link")
                ) {
                    Text(
                        text = "Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PaymentBrightGold
                    )
                }
            }
        }
    }
}
