package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.GoogleAccountIdentity
import com.example.data.GoogleAuthManager
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.TextDark
import com.example.ui.theme.TrackPurple

private val HeaderSplashOrange = Color(0xFFFF7A53)
private val HeaderSplashPink = Color(0xFFFF6B9D)
private val PopupGold = Color(0xFFFFB300)
private val PopupBrightGold = Color(0xFFFFD54F)
private val PopupCrimson = Color(0xFFE53935)
private val PopupCrimsonBorder = Color(0xFFFF8A80)
private val PopupMintBg = Color(0xFFE5F5F0)
private val PopupMintText = Color(0xFF2A9D8F)

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

/**
 * Authentic 4-color Google "G" logo rendered in a 20dp canvas.
 */
@Composable
fun GoogleGLogoIcon(
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.21f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        // Red top-left arc
        drawArc(
            color = GoogleRed,
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Yellow left arc
        drawArc(
            color = GoogleYellow,
            startAngle = 145f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Green bottom arc
        drawArc(
            color = GoogleGreen,
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue bottom-right arc
        drawArc(
            color = GoogleBlue,
            startAngle = -12f,
            sweepAngle = 57f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue horizontal crossbar
        drawLine(
            color = GoogleBlue,
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width - strokeWidth * 0.18f, size.height * 0.50f),
            strokeWidth = strokeWidth * 0.92f,
            cap = StrokeCap.Butt
        )
    }
}

/**
 * Shared "Continue with Google" pill button:
 * - Full width, height 52dp
 * - Crisp white background (#FFFFFF)
 * - 1.5dp border #E5E2F2 (TrackPurple)
 * - Corner radius: 26dp
 * - Google "G" logo icon (20dp) on the left inside the button
 * - Text: "Continue with Google" (15sp, weight Medium/Bold, color #1A1A1A) centered
 */
@Composable
fun ContinueWithGoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    text: String = "Continue with Google",
    testTag: String = "continue_with_google_button"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickable(enabled = !isLoading, onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, TrackPurple),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = IndigoPurple,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Connecting to Google...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            } else {
                // Google "G" logo icon (20dp) on the left inside the button
                Box(
                    modifier = Modifier.align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleGLogoIcon(modifier = Modifier.size(20.dp))
                }

                // Centered "Continue with Google" text (15sp, weight Bold, color #1A1A1A)
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Illustrated purple header treatment shared by the Login screen and the New Signup Entry screen:
 * Matches the Payment screen & Welcome screen aesthetic with:
 * - Rich #4B3F8F purple background with Orange & Pink radial glow
 * - Top bar with title + subtitle and translucent circular close button
 * - Large illustrated Orange app icon + Orange & Pink accent pills
 */
@Composable
fun IllustratedPurpleAuthHeader(
    title: String,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    subtitle: String? = null,
    titleTestTag: String = "auth_header_title",
    backButtonTestTag: String = "auth_back_button",
    iconTestTag: String = "auth_top_app_icon",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val w = size.width
            val h = size.height
            drawRect(color = IndigoPurple, size = size)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HeaderSplashOrange.copy(alpha = 0.36f),
                        IndigoPurple.copy(alpha = 0.0f)
                    ),
                    center = Offset(w * 0.5f, h * 0.56f),
                    radius = w * 0.50f
                ),
                radius = w * 0.50f,
                center = Offset(w * 0.5f, h * 0.56f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HeaderSplashPink.copy(alpha = 0.28f),
                        IndigoPurple.copy(alpha = 0.0f)
                    ),
                    center = Offset(w * 0.74f, h * 0.44f),
                    radius = w * 0.40f
                ),
                radius = w * 0.40f,
                center = Offset(w * 0.74f, h * 0.44f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header Row matching Payment Screen & Popup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag(titleTestTag)
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightLavender
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateBack,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .testTag(backButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Catch Up App Icon",
                    tint = HeaderSplashOrange,
                    modifier = Modifier
                        .size(138.dp)
                        .testTag(iconTestTag)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(HeaderSplashOrange)
                )
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(HeaderSplashPink)
                )
            }
        }
    }
}

/**
 * Read-only Google account confirmation banner displayed near the top of the profile completion form:
 * Shows the Google account's profile photo (if available) and email address as read-only confirmation
 * text: "Signing up as: [email]".
 */
@Composable
fun GoogleSigningUpAsBanner(
    googleIdentity: GoogleAccountIdentity,
    onSwitchAccount: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signup_google_account_banner"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(1.5.dp, PopupBrightGold)
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
                // Google account profile photo (if available) or circular avatar badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .testTag("signup_google_profile_photo"),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = googleIdentity.photoUrl
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Google Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        val initial = googleIdentity.displayName.trim().firstOrNull()?.uppercase()
                            ?: googleIdentity.email.trim().firstOrNull()?.uppercase()
                            ?: "G"
                        Text(
                            text = initial,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = IndigoPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GoogleGLogoIcon(modifier = Modifier.size(14.dp))
                        Text(
                            text = "GOOGLE ACCOUNT VERIFIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PopupBrightGold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Signing up as: ${googleIdentity.email}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("signup_signing_up_as_text")
                    )
                }
            }

            if (onSwitchAccount != null) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onSwitchAccount)
                        .testTag("signup_change_google_account_button"),
                    shape = RoundedCornerShape(10.dp),
                    color = PopupGold
                ) {
                    Text(
                        text = "Change",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Payment-Popup-styled Google Account Bottom Sheet (`ModalBottomSheet`) shown when Android
 * Credential Manager falls back (or when switching Google accounts).
 * Styled identically to the Payment Popup in `AccountScreen.kt`:
 * - `IndigoPurple` (#4B3F8F) container with 28.dp top rounded corners
 * - Top header with bold white title, `LightLavender` subtitle, and circular translucent close button
 * - Highlight card with `PopupBrightGold` (#FFD54F) border and mint badge
 * - Frosted glass input fields with gold focus borders and icons
 * - Bright Gold (#FFB300) primary button with #1A1A1A ExtraBold text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAccountSelectorDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (GoogleAccountIdentity) -> Unit
) {
    val context = LocalContext.current
    val recentAccounts = remember(context) { GoogleAuthManager.getRecentGoogleAccounts(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showCustomAccountInput by remember { mutableStateOf(recentAccounts.isEmpty()) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IndigoPurple,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("google_account_selector_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Title & Close Button (Matches Payment Popup 100%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Google Sign-In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose or enter your Google account to continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightLavender
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .testTag("google_picker_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Highlight Card (Matches Selected Plan Card in Payment Screen / Popup)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.5.dp, PopupBrightGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoogleGLogoIcon(modifier = Modifier.size(15.dp))
                            Text(
                                text = "GOOGLE AUTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PopupBrightGold,
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            color = PopupMintBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = PopupMintText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Identity",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopupMintText
                                )
                            }
                        }
                    }

                    Text(
                        text = "Sign in once with Google — no password needed.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (recentAccounts.isNotEmpty() && !showCustomAccountInput) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Saved Google Accounts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    recentAccounts.forEachIndexed { idx, account ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    val completed = GoogleAuthManager.completeGoogleSignInWithAccount(
                                        context = context,
                                        email = account.email,
                                        displayName = account.displayName,
                                        photoUrl = account.photoUrl,
                                        explicitGoogleUid = account.googleUserId
                                    )
                                    onAccountSelected(completed)
                                }
                                .testTag("google_saved_account_$idx"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, PopupBrightGold.copy(alpha = 0.85f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PopupGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = account.displayName.firstOrNull()?.uppercase()
                                        ?: account.email.firstOrNull()?.uppercase()
                                        ?: "G"
                                    Text(
                                        text = initial,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    if (account.displayName.isNotBlank()) {
                                        Text(
                                            text = account.displayName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = account.email,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightLavender
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.White.copy(alpha = 0.20f)
                    )

                    TextButton(
                        onClick = { showCustomAccountInput = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_use_another_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = PopupBrightGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Use another Google account",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PopupBrightGold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Google Email Input (Matches Payment Phone Input style)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Google Email Address",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                inputError = null
                            },
                            placeholder = {
                                Text(
                                    "yourname@gmail.com",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Email
                            ),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = PopupBrightGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_picker_email_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = PopupBrightGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = PopupBrightGold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Account Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Full Name (Optional)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                inputError = null
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
                                    tint = PopupBrightGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_picker_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = PopupBrightGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = PopupBrightGold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    if (inputError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = PopupCrimson.copy(alpha = 0.25f),
                            border = BorderStroke(1.5.dp, PopupCrimsonBorder)
                        ) {
                            Text(
                                text = inputError!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("google_picker_error_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Continue Button (Matches Pay Button in Payment Popup 100%)
                    Button(
                        onClick = {
                            val cleanEmail = emailInput.trim().lowercase()
                            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                inputError = "Please enter a valid Google email address (e.g. name@gmail.com)"
                                return@Button
                            }
                            val completed = GoogleAuthManager.completeGoogleSignInWithAccount(
                                context = context,
                                email = cleanEmail,
                                displayName = nameInput.trim()
                            )
                            onAccountSelected(completed)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PopupGold,
                            disabledContainerColor = PopupGold.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_picker_continue_button")
                    ) {
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }
    }
}
