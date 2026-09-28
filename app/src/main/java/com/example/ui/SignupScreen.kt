package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupabaseRepository
import com.example.data.SupabaseSecurityVault
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import kotlinx.coroutines.launch

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
        phoneNumber: String,
        name: String,
        studentClass: String,
        studentStream: String,
        teacherSubjects: String
    ) -> Unit = { _, _, _, _, _, _ -> }
) {
    val normalizedInitialRole = if (initialRole.equals("Teacher", ignoreCase = true)) "Teacher" else "Student"

    // Exact field order requested:
    // 1. Full Name
    // 2. Phone Number
    // 3. Role Selection (Student or Teacher)
    // 4. Password
    // 5a. If Student -> Class and Stream (written individually) + Selected Plan
    // 5b. If Teacher -> Select Subjects You Teach (from DefaultCurriculumSubjects)
    // 6. Submit / Sign Up
    var fullNameInput by remember { mutableStateOf("") }
    var phoneNumberInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(normalizedInitialRole) }
    var passwordInput by remember { mutableStateOf("") }
    var classInput by remember { mutableStateOf("") }
    var streamInput by remember { mutableStateOf("") }
    var selectedTeacherSubjects by remember { mutableStateOf(setOf<String>()) }
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
            // 2. PHONE NUMBER
            // ==========================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Phone Number",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = phoneNumberInput,
                    onValueChange = {
                        phoneNumberInput = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "07XX XXXXXX",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PhoneAndroid,
                            contentDescription = null,
                            tint = PaymentBrightGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_phone_input"),
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
            // 5. IF STUDENT -> CLASS & STREAM (WRITTEN INDIVIDUALLY)
            // ==========================================================
            AnimatedVisibility(visible = isStudentRole) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Individual Class Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Class",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = classInput,
                            onValueChange = {
                                classInput = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    "Write your class (e.g. S.4)",
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_class_input"),
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

                    // Individual Stream Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Stream",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = streamInput,
                            onValueChange = {
                                streamInput = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    "Write your stream (e.g. Green / North)",
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
                                    imageVector = Icons.Outlined.Book,
                                    contentDescription = null,
                                    tint = PaymentBrightGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_stream_input"),
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

                    // Selected Monthly Plan (UGX 1,000 / Month)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Selected Plan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_selected_plan_card"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.5.dp, PaymentBrightGold)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = true,
                                        onClick = {},
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = PaymentBrightGold,
                                            unselectedColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "1 Month Plan",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Billed monthly • Paid via signup phone",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LightLavender
                                        )
                                    }
                                }

                                Text(
                                    text = "UGX 1,000",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PaymentBrightGold
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================================
            // 5B. IF TEACHER -> SELECT SUBJECTS TO TEACH (FROM SUBJECTS PAGE)
            // ==========================================================
            AnimatedVisibility(visible = !isStudentRole) {
                val filteredCurriculumSubjects = remember(subjectSearchQuery) {
                    if (subjectSearchQuery.isBlank()) {
                        DefaultCurriculumSubjects
                    } else {
                        DefaultCurriculumSubjects.filter {
                            it.name.contains(subjectSearchQuery.trim(), ignoreCase = true)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_teacher_subjects_section"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Subjects You Teach",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Choose all subjects you will upload notes & assignments for",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightLavender
                            )
                        }

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
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("signup_teacher_subjects_count")
                            )
                        }
                    }

                    // Filter / search input for subjects
                    OutlinedTextField(
                        value = subjectSearchQuery,
                        onValueChange = { subjectSearchQuery = it },
                        placeholder = {
                            Text(
                                "Search subjects (e.g. Math, Physics, Biology)...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search subjects",
                                tint = PaymentBrightGold,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (subjectSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { subjectSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_teacher_subject_search"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                            focusedBorderColor = PaymentBrightGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.30f),
                            cursorColor = PaymentBrightGold,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Selected subject pills summary (if any selected)
                    if (selectedTeacherSubjects.isNotEmpty()) {
                        val orderedSelected = DefaultCurriculumSubjects
                            .filter { it.name in selectedTeacherSubjects }
                            .joinToString(", ") { it.name }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, PaymentBrightGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Teaching: $orderedSelected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PaymentBrightGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // 2-Column Grid of all Subjects from SubjectsScreen (DefaultCurriculumSubjects)
                    val subjectPairs = filteredCurriculumSubjects.chunked(2)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        subjectPairs.forEach { rowSubjects ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowSubjects.forEach { subjectItem ->
                                    val isSelected = subjectItem.name in selectedTeacherSubjects
                                    val safeTag = subjectItem.name.lowercase().replace(" ", "_")
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                selectedTeacherSubjects = if (isSelected) {
                                                    selectedTeacherSubjects - subjectItem.name
                                                } else {
                                                    selectedTeacherSubjects + subjectItem.name
                                                }
                                                errorMessage = null
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
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
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
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = subjectItem.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    maxLines = 2
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
                        phoneNumberInput.isBlank() -> {
                            errorMessage = "Please enter your phone number"
                        }
                        passwordInput.isBlank() -> {
                            errorMessage = "Please enter a password"
                        }
                        passwordInput.length < 4 -> {
                            errorMessage = "Password must be at least 4 characters"
                        }
                        isStudentRole && classInput.isBlank() -> {
                            errorMessage = "Please write your Class (e.g. S.4)"
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
                                val cleanPhone = phoneNumberInput.trim()
                                val cleanClass = if (isStudentRole) classInput.trim() else ""
                                val cleanStream = if (isStudentRole) streamInput.trim() else ""
                                val cleanTeacherSubjects = if (!isStudentRole) {
                                    DefaultCurriculumSubjects
                                        .filter { it.name in selectedTeacherSubjects }
                                        .joinToString(", ") { it.name }
                                } else ""

                                val teacherCode = if (!isStudentRole) {
                                    generateTeacherIdCode(cleanName.ifBlank { cleanPhone })
                                } else null
                                SupabaseRepository.syncUserToSupabase(
                                    phoneNumber = cleanPhone,
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
                                    cleanPhone,
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
                        text = "Sign Up & Login",
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
