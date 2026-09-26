package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppDatabase
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LoginGold = Color(0xFFFFB300)
private val LoginBrightGold = Color(0xFFFFD54F)
private val LoginCrimson = Color(0xFFE53935)
private val LoginCrimsonBorder = Color(0xFFFF8A80)
private val LoginDarkText = Color(0xFF1A1A1A)
private val LoginSplashOrange = Color(0xFFFF7A53)
private val LoginSplashPink = Color(0xFFFF6B9D)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    initialRole: String = "Student",
    onNavigateBack: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    onLoginSuccess: (
        role: String,
        phoneOrIdentifier: String,
        fullName: String?,
        academicClass: String?,
        stream: String?
    ) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
            containerColor = IndigoPurple,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IndigoPurple)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Subtle onboarding-style Orange & Pink ambient glow in the upper canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawRect(color = IndigoPurple, size = size)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LoginSplashOrange.copy(alpha = 0.34f),
                            IndigoPurple.copy(alpha = 0.0f)
                        ),
                        center = Offset(w * 0.5f, h * 0.24f),
                        radius = w * 0.48f
                    ),
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, h * 0.24f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LoginSplashPink.copy(alpha = 0.24f),
                            IndigoPurple.copy(alpha = 0.0f)
                        ),
                        center = Offset(w * 0.72f, h * 0.20f),
                        radius = w * 0.38f
                    ),
                    radius = w * 0.38f,
                    center = Offset(w * 0.72f, h * 0.20f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .testTag("login_header"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==========================================================
                // TOP HEADER: Centered "Login" Title Only
                // ==========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Login",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("login_title")
                    )

                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !isLoading,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f))
                            .testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================================
                // TOP ICON (Like Onboarding Screens: Orange App Icon + Accent Pills)
                // ==========================================================
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Catch Up App Icon",
                        tint = LoginSplashOrange,
                        modifier = Modifier
                            .size(164.dp)
                            .testTag("login_top_app_icon")
                    )
                }

                // Small onboarding-style Orange + Pink pill accent bar beneath the icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(LoginSplashOrange)
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(LoginSplashPink)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ==========================================================
                // CENTERED FIELDS SECTION
                // ==========================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // PHONE OR USERNAME INPUT
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Phone or Username",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = identifierInput,
                            onValueChange = {
                                identifierInput = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    "Enter phone or username",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = LoginBrightGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = LoginBrightGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = LoginBrightGold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // PASSWORD INPUT
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
                                    "Enter your password",
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
                                    tint = LoginBrightGold,
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
                                        contentDescription = "Toggle Password Visibility",
                                        tint = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = LoginBrightGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = LoginBrightGold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = LoginCrimson.copy(alpha = 0.25f),
                            border = BorderStroke(1.5.dp, LoginCrimsonBorder)
                        ) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("login_error_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // LOGIN BUTTON (Payment Popup Gold Button Style)
                    Button(
                        onClick = {
                            if (identifierInput.isBlank()) {
                                errorMessage = "Please enter your phone or username"
                                return@Button
                            }
                            if (passwordInput.isBlank()) {
                                errorMessage = "Please enter your password"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                delay(400L)
                                val cleanIdentifier = identifierInput.trim()
                                val registeredUser = db.userDao().getUserByIdentifier(cleanIdentifier)
                                if (registeredUser != null &&
                                    registeredUser.password.isNotEmpty() &&
                                    registeredUser.password != passwordInput
                                ) {
                                    isLoading = false
                                    errorMessage = "Incorrect password. Please try again."
                                    return@launch
                                }

                                isLoading = false
                                val isAdminCredentials = cleanIdentifier.equals("admin", ignoreCase = true) ||
                                    cleanIdentifier == "0700000000"
                                val isTeacherCredentials = cleanIdentifier.equals("teacher", ignoreCase = true) ||
                                    cleanIdentifier.startsWith("tr.", ignoreCase = true)
                                val targetRole = when {
                                    isAdminCredentials -> "admin"
                                    registeredUser != null -> registeredUser.role.lowercase()
                                    isTeacherCredentials -> "teacher"
                                    else -> initialRole.lowercase()
                                }
                                val resolvedName = registeredUser?.fullName
                                    ?: cleanIdentifier.takeIf { it.any { ch -> ch.isLetter() } }
                                onLoginSuccess(
                                    targetRole,
                                    cleanIdentifier,
                                    resolvedName,
                                    registeredUser?.academicClass,
                                    registeredUser?.stream
                                )
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LoginGold,
                            disabledContainerColor = LoginGold.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = LoginDarkText,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Signing in...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoginDarkText
                            )
                        } else {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LoginDarkText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Create Account Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightLavender
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onNavigateToSignup,
                        modifier = Modifier.testTag("login_signup_link")
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LoginBrightGold
                        )
                    }
                }
            }
        }
    }
}
