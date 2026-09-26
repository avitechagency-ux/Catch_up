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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender

private val WelcomeOrange = Color(0xFFFF7A53)
private val WelcomePink = Color(0xFFFF6B9D)
private val WelcomeSoftPink = Color(0xFFFF8FB8)

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    onExploreAsGuest: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = IndigoPurple)
            .testTag("welcome_screen")
    ) {
        // Minimalist Orange & Pink graphic color splash on the empty purple background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Explicit solid purple fill so it never looks black
            drawRect(color = IndigoPurple, size = size)

            // Soft Pink ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WelcomePink.copy(alpha = 0.42f),
                        IndigoPurple.copy(alpha = 0.0f)
                    ),
                    center = Offset(w * 0.78f, h * 0.22f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.78f, h * 0.22f)
            )

            // Soft Orange ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WelcomeOrange.copy(alpha = 0.35f),
                        IndigoPurple.copy(alpha = 0.0f)
                    ),
                    center = Offset(w * 0.25f, h * 0.34f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(w * 0.25f, h * 0.34f)
            )

            // Minimalist Orange & Pink overlapping accent pills in upper canvas
            rotate(degrees = -14f, pivot = Offset(w * 0.5f, h * 0.29f)) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(WelcomeOrange, WelcomePink)
                    ),
                    topLeft = Offset(w * 0.34f, h * 0.24f),
                    size = Size(w * 0.32f, 26.dp.toPx()),
                    cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(WelcomePink, WelcomeSoftPink)
                    ),
                    topLeft = Offset(w * 0.40f, h * 0.285f),
                    size = Size(w * 0.22f, 26.dp.toPx()),
                    cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx())
                )
            }
        }

        // Top minimal brand mark
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 32.dp, vertical = 20.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WelcomeOrange)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WelcomePink)
            )
            Text(
                text = "Catch Up",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = 0.6.sp,
                modifier = Modifier.testTag("welcome_app_name")
            )
        }

        // Bottom welcoming note & simple auth buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(WelcomeOrange)
                )
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(WelcomePink)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WELCOME",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WelcomeSoftPink,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Simple notes for students and teachers.",
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in or create an account to start reading and sharing class notes.",
                fontSize = 13.sp,
                color = LightLavender,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_login_button")
            ) {
                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onNavigateToSignup,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_signup_button")
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onExploreAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_guest_button")
            ) {
                Text(
                    text = "Continue as guest",
                    color = LightLavender,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
