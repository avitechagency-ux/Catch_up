package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.Locale

/**
 * Real-time MarzPay payment verification stages.
 */
enum class MarzPayVerificationStage {
    IDLE,
    PROCESSING, // Background communication with MarzPay API to dispatch STK push
    PENDING,    // STK prompt dispatched to phone; waiting for user PIN approval & background polling
    SUCCESS,    // Confirmed completed & receipt verified
    FAILED      // Reported as failed, cancelled, declined, or timed out
}

/**
 * State representing a live payment verification cycle.
 */
data class PaymentVerificationState(
    val stage: MarzPayVerificationStage = MarzPayVerificationStage.PROCESSING,
    val transactionId: String = "",
    val marzpayTxId: String = "",
    val phoneNumber: String = "",
    val networkProvider: String = "Mobile Money",
    val planDuration: String = "1 Month",
    val amountUgx: Long = 2000L,
    val statusMessage: String = "Processing payment in background...",
    val errorMessage: String? = null,
    val currentCheckAttempt: Int = 0,
    val maxCheckAttempts: Int = 12,
    val isCheckingNow: Boolean = false
)

/**
 * Modern, animated payment verification popup that performs network operations
 * and polling cleanly in the background while displaying an engaging, creative animated loader.
 */
@Composable
fun PaymentVerificationDialog(
    state: PaymentVerificationState,
    onDismiss: () -> Unit,
    onCheckNow: () -> Unit,
    onRetry: () -> Unit,
    onConfirmedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinished = state.stage == MarzPayVerificationStage.SUCCESS || state.stage == MarzPayVerificationStage.FAILED

    Dialog(
        onDismissRequest = {
            if (isFinished) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isFinished,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("payment_verification_dialog"),
            shape = RoundedCornerShape(30.dp),
            color = Color(0xFF170F28),
            border = BorderStroke(
                width = 1.5.dp,
                brush = when (state.stage) {
                    MarzPayVerificationStage.SUCCESS -> Brush.linearGradient(
                        listOf(Color(0xFF00E676), Color(0xFF10B981))
                    )
                    MarzPayVerificationStage.FAILED -> Brush.linearGradient(
                        listOf(Color(0xFFFF5252), Color(0xFFEF4444))
                    )
                    else -> Brush.linearGradient(
                        listOf(Color(0xFFFFB300), Color(0xFF8B5CF6), Color(0xFF38BDF8))
                    )
                }
            ),
            shadowElevation = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF22163D),
                                Color(0xFF140D24)
                            )
                        )
                    )
            ) {
                // Top-right close button (only available if completed or failed)
                if (isFinished) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .testTag("close_verification_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Smooth animated transition between Verification Stages
                AnimatedContent(
                    targetState = state.stage,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.94f, animationSpec = tween(350)))
                            .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
                    },
                    label = "verification_stage_transition",
                    modifier = Modifier.fillMaxWidth()
                ) { currentStage ->
                    when (currentStage) {
                        MarzPayVerificationStage.SUCCESS -> {
                            AnimatedSuccessContent(
                                state = state,
                                onConfirmedSuccess = onConfirmedSuccess
                            )
                        }
                        MarzPayVerificationStage.FAILED -> {
                            AnimatedFailedContent(
                                state = state,
                                onRetry = onRetry,
                                onDismiss = onDismiss
                            )
                        }
                        MarzPayVerificationStage.PROCESSING,
                        MarzPayVerificationStage.PENDING,
                        MarzPayVerificationStage.IDLE -> {
                            AnimatedProcessingContent(
                                state = state,
                                onCheckNow = onCheckNow,
                                onCancel = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 1. CREATIVE ANIMATED PROCESSING & PIN APPROVAL CONTENT
// ============================================================================

@Composable
private fun AnimatedProcessingContent(
    state: PaymentVerificationState,
    onCheckNow: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Creative Animated Cosmic / Telecom Orb Loader
        CosmicPaymentLoader(isCheckingNow = state.isCheckingNow)

        // Status Header & Dynamic Amount Pill
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFB300).copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300))
                    )
                    Text(
                        text = "UGX ${NumberFormat.getNumberInstance(Locale.US).format(state.amountUgx)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD54F),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Text(
                text = if (state.stage == MarzPayVerificationStage.PROCESSING) {
                    "Processing Payment"
                } else {
                    "Approve Prompt on Handset"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (state.phoneNumber.isNotBlank()) {
                    "A Mobile Money prompt was sent to ${state.phoneNumber}. Please enter your PIN on your phone to complete authorization."
                } else {
                    "Connecting securely with Mobile Money. Please approve the prompt when it appears on your handset."
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD1C4E9),
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }

        // Live Background Checking Badge & Pulse Dots
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF130D23),
            border = BorderStroke(1.dp, Color(0xFF38265C))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Encrypted",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (state.isCheckingNow) {
                            "Verifying transaction status..."
                        } else {
                            "Listening for PIN confirmation..."
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2D9F3)
                    )
                }

                AnimatedPulsingDots()
            }
        }

        // Action Buttons: "I Entered My PIN — Check Now" & "Cancel"
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCheckNow,
                enabled = !state.isCheckingNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color(0xFF140D24),
                    disabledContainerColor = Color(0xFFFFB300).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("check_payment_status_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isCheckingNow) "Checking Status..." else "I Entered My PIN — Check Now",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD1C4E9)
                ),
                border = BorderStroke(1.dp, Color(0xFF6B46C1).copy(alpha = 0.45f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("cancel_payment_verification_button")
            ) {
                Text(
                    text = "Cancel Transaction",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Creative concentric multi-ring animated orbital loader.
 * Renders pulsing aura rings, rotating gradient arcs, and a breathing mobile handset icon.
 */
@Composable
private fun CosmicPaymentLoader(isCheckingNow: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_loader")

    // Continuous rotation for outer arc
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse for inner core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Outer ripple expanding wave
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(110.dp)
            .padding(4.dp)
    ) {
        // Expanding ripple aura ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(rippleScale)
                .border(
                    width = 2.dp,
                    color = Color(0xFFFFB300).copy(alpha = rippleAlpha),
                    shape = CircleShape
                )
        )

        // Orbital rotating gradient ring
        Canvas(
            modifier = Modifier
                .size(92.dp)
                .rotate(rotation)
        ) {
            val strokeWidth = 3.5.dp.toPx()
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFFFB300),
                        Color(0xFF8B5CF6),
                        Color(0xFF38BDF8),
                        Color(0xFFFFB300)
                    )
                ),
                startAngle = 0f,
                sweepAngle = 290f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner glowing orb with mobile money handset
        Surface(
            shape = CircleShape,
            color = Color(0xFF2A1B4E),
            border = BorderStroke(2.dp, Color(0xFFFFD54F).copy(alpha = 0.65f)),
            modifier = Modifier
                .size(62.dp)
                .scale(pulseScale)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFB300).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = if (isCheckingNow) Color(0xFFFFE082) else Color(0xFFFFD54F),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

/**
 * Animated three-dot bouncing indicator to show real-time background activity.
 */
@Composable
private fun AnimatedPulsingDots() {
    val transition = rememberInfiniteTransition(label = "dots")

    val dot1Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD54F).copy(alpha = dot1Alpha))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD54F).copy(alpha = dot2Alpha))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD54F).copy(alpha = dot3Alpha))
        )
    }
}

// ============================================================================
// 2. CREATIVE ANIMATED SUCCESS CONTENT
// ============================================================================

@Composable
private fun AnimatedSuccessContent(
    state: PaymentVerificationState,
    onConfirmedSuccess: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "success_celebration")
    val scaleAnim by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "success_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Glowing animated emerald check badge
        Surface(
            shape = CircleShape,
            color = Color(0xFF00C853).copy(alpha = 0.18f),
            border = BorderStroke(2.5.dp, Color(0xFF00E676)),
            modifier = Modifier
                .size(84.dp)
                .scale(scaleAnim)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF00E676).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Title and confirmed amount
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Payment Confirmed!",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "UGX ${NumberFormat.getNumberInstance(Locale.US).format(state.amountUgx)} successfully received via ${state.networkProvider.ifBlank { "Mobile Money" }}.",
                fontSize = 13.sp,
                color = Color(0xFFB9F6CA),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        // Clean Modern Receipt Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF130D23),
            border = BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptRow(label = "Subscription", value = state.planDuration)
                ReceiptRow(label = "Account", value = state.phoneNumber)
                if (state.transactionId.isNotBlank()) {
                    ReceiptRow(label = "Reference ID", value = state.transactionId, valueColor = Color(0xFFFFD54F))
                }
                ReceiptRow(
                    label = "Gateway Status",
                    value = "COMPLETED (Verified)",
                    valueColor = Color(0xFF00E676)
                )
            }
        }

        // Large filled emerald button
        Button(
            onClick = onConfirmedSuccess,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("continue_after_success_button")
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continue to Platform",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

// ============================================================================
// 3. CREATIVE ANIMATED FAILED CONTENT
// ============================================================================

@Composable
private fun AnimatedFailedContent(
    state: PaymentVerificationState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Glowing crimson alert icon
        Surface(
            shape = CircleShape,
            color = Color(0xFFE53935).copy(alpha = 0.2f),
            border = BorderStroke(2.dp, Color(0xFFFF5252)),
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFF5252).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Failed",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Payment Not Completed",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = state.errorMessage ?: "The transaction was cancelled on your phone, timed out, or had insufficient funds. Please try again.",
                fontSize = 13.sp,
                color = Color(0xFFFF8A80),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color(0xFF140D24)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("retry_failed_payment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Retry Payment",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD1C4E9)
                ),
                border = BorderStroke(1.dp, Color(0xFF6B46C1).copy(alpha = 0.45f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("dismiss_failed_payment_button")
            ) {
                Text(
                    text = "Change Number / Dismiss",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFB39DDB)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
