package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Real-time MarzPay payment verification stages.
 */
enum class MarzPayVerificationStage {
    IDLE,
    PROCESSING, // Communicating with MarzPay API to initiate mobile money STK push
    PENDING,    // STK prompt dispatched to phone; waiting for user PIN approval & active polling
    SUCCESS,    // Officially confirmed by MarzPay as COMPLETED/PAID
    FAILED      // Officially reported by MarzPay as FAILED, CANCELLED, DECLINED, or TIMED OUT
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
    val amountUgx: Long = 1000L,
    val statusMessage: String = "Connecting to MarzPay Gateway...",
    val errorMessage: String? = null,
    val currentCheckAttempt: Int = 0,
    val maxCheckAttempts: Int = 12,
    val isCheckingNow: Boolean = false
)

/**
 * Full verification modal dialog enforcing zero-fake payments with MarzPay.
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
    Dialog(
        onDismissRequest = {
            if (state.stage == MarzPayVerificationStage.SUCCESS || state.stage == MarzPayVerificationStage.FAILED) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = state.stage != MarzPayVerificationStage.PROCESSING && state.stage != MarzPayVerificationStage.PENDING,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("payment_verification_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1630),
            border = BorderStroke(
                width = 1.5.dp,
                color = when (state.stage) {
                    MarzPayVerificationStage.SUCCESS -> Color(0xFF00C853)
                    MarzPayVerificationStage.FAILED -> Color(0xFFE53935)
                    MarzPayVerificationStage.PENDING -> Color(0xFFFFB300)
                    else -> Color(0xFF9575CD)
                }
            ),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header with Security Badge and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2E204D)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Secure",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MARZPAY SECURE VERIFICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F),
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Direct Telecom Anti-Fraud System",
                                fontSize = 9.sp,
                                color = Color(0xFFB39DDB)
                            )
                        }
                    }

                    if (state.stage == MarzPayVerificationStage.SUCCESS || state.stage == MarzPayVerificationStage.FAILED) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
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
                }

                // Stage Stepper Bar
                VerificationStepperBar(stage = state.stage)

                // Central Status Content based on state
                when (state.stage) {
                    MarzPayVerificationStage.PROCESSING -> {
                        ProcessingContent(state = state)
                    }
                    MarzPayVerificationStage.PENDING -> {
                        PendingContent(
                            state = state,
                            onCheckNow = onCheckNow,
                            onCancel = onDismiss
                        )
                    }
                    MarzPayVerificationStage.SUCCESS -> {
                        SuccessContent(
                            state = state,
                            onConfirmedSuccess = onConfirmedSuccess
                        )
                    }
                    MarzPayVerificationStage.FAILED -> {
                        FailedContent(
                            state = state,
                            onRetry = onRetry,
                            onDismiss = onDismiss
                        )
                    }
                    MarzPayVerificationStage.IDLE -> {
                        ProcessingContent(state = state)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationStepperBar(stage: MarzPayVerificationStage) {
    val step1Active = true
    val step2Active = stage == MarzPayVerificationStage.PENDING || stage == MarzPayVerificationStage.SUCCESS
    val step3Active = stage == MarzPayVerificationStage.SUCCESS

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF140E22))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepItem(label = "1. Processing", isActive = step1Active, isDone = stage != MarzPayVerificationStage.PROCESSING)
        Text(text = "›", color = Color(0xFF7E57C2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        StepItem(label = "2. PIN Prompt", isActive = step2Active, isDone = stage == MarzPayVerificationStage.SUCCESS)
        Text(text = "›", color = Color(0xFF7E57C2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        StepItem(
            label = if (stage == MarzPayVerificationStage.FAILED) "Failed" else "3. Confirmed",
            isActive = stage == MarzPayVerificationStage.SUCCESS || stage == MarzPayVerificationStage.FAILED,
            isDone = stage == MarzPayVerificationStage.SUCCESS,
            isError = stage == MarzPayVerificationStage.FAILED
        )
    }
}

@Composable
private fun StepItem(
    label: String,
    isActive: Boolean,
    isDone: Boolean,
    isError: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isError -> Color(0xFFE53935)
                        isDone -> Color(0xFF00C853)
                        isActive -> Color(0xFFFFB300)
                        else -> Color(0xFF5E35B1).copy(alpha = 0.4f)
                    }
                )
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isError -> Color(0xFFFF8A80)
                isDone -> Color(0xFFB9F6CA)
                isActive -> Color(0xFFFFD54F)
                else -> Color(0xFF9E86C8).copy(alpha = 0.6f)
            }
        )
    }
}

// ----------------------------------------------------------------------------
// 1. PROCESSING CONTENT
// ----------------------------------------------------------------------------
@Composable
private fun ProcessingContent(state: PaymentVerificationState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .scale(scale)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFFB300),
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(68.dp)
            )
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(30.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Initiating MarzPay STK Push",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Connecting to telecom provider for ${state.phoneNumber.ifBlank { "Mobile Money" }}...",
                fontSize = 12.sp,
                color = Color(0xFFD1C4E9),
                textAlign = TextAlign.Center
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF140E22)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Amount to Pay", fontSize = 12.sp, color = Color(0xFFB39DDB))
                Text(
                    text = "UGX ${java.text.NumberFormat.getNumberInstance().format(state.amountUgx)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD54F)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 2. PENDING CONTENT (Awaiting PIN approval on user's handset)
// ----------------------------------------------------------------------------
@Composable
private fun PendingContent(
    state: PaymentVerificationState,
    onCheckNow: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_phone")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFB300).copy(alpha = 0.18f),
            border = BorderStroke(2.dp, Color(0xFFFFB300).copy(alpha = alphaAnim)),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Approve on Your Phone",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "A mobile money PIN prompt was sent to ${state.phoneNumber}. Enter your PIN to approve payment of UGX ${java.text.NumberFormat.getNumberInstance().format(state.amountUgx)}.",
                fontSize = 12.sp,
                color = Color(0xFFD1C4E9),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }

        // Live Polling Status Indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF140E22),
            border = BorderStroke(1.dp, Color(0xFF39285C))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
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
                        if (state.isCheckingNow) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = Color(0xFFFFB300)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300))
                            )
                        }
                        Text(
                            text = if (state.isCheckingNow) "Confirming with MarzPay..." else "Awaiting PIN Approval",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }

                    Text(
                        text = "Check ${state.currentCheckAttempt} of ${state.maxCheckAttempts}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB39DDB)
                    )
                }

                val progress = (state.currentCheckAttempt.toFloat() / state.maxCheckAttempts.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFB300),
                    trackColor = Color(0xFF281C40),
                )

                if (state.transactionId.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Reference ID", fontSize = 10.sp, color = Color(0xFF9E86C8))
                        Text(
                            text = state.transactionId,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Action Buttons: "I Entered PIN — Check Now" & "Cancel"
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCheckNow,
                enabled = !state.isCheckingNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color(0xFF140E22)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("check_payment_status_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isCheckingNow) "Checking MarzPay..." else "I Entered My PIN — Check Now",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD1C4E9)
                ),
                border = BorderStroke(1.dp, Color(0xFF5E35B1).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("cancel_payment_verification_button")
            ) {
                Text(
                    text = "Cancel Transaction",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3. SUCCESS CONTENT (Confirmed from MarzPay)
// ----------------------------------------------------------------------------
@Composable
private fun SuccessContent(
    state: PaymentVerificationState,
    onConfirmedSuccess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF00C853).copy(alpha = 0.2f),
            border = BorderStroke(2.dp, Color(0xFF00C853)),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Payment Verified!",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "MarzPay officially confirmed receipt of UGX ${java.text.NumberFormat.getNumberInstance().format(state.amountUgx)}.",
                fontSize = 13.sp,
                color = Color(0xFFB9F6CA),
                textAlign = TextAlign.Center
            )
        }

        // Receipt Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF140E22),
            border = BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Subscription Plan", fontSize = 12.sp, color = Color(0xFFB39DDB))
                    Text(
                        text = state.planDuration,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Phone Number", fontSize = 12.sp, color = Color(0xFFB39DDB))
                    Text(
                        text = state.phoneNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (state.transactionId.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Transaction ID", fontSize = 12.sp, color = Color(0xFFB39DDB))
                        Text(
                            text = state.transactionId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gateway Status", fontSize = 12.sp, color = Color(0xFFB39DDB))
                    Text(
                        text = "COMPLETED (Verified)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676)
                    )
                }
            }
        }

        Button(
            onClick = onConfirmedSuccess,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("continue_after_success_button")
        ) {
            Text(
                text = "Continue to Platform",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

// ----------------------------------------------------------------------------
// 4. FAILED CONTENT (Reported by MarzPay as FAILED/DECLINED/TIMEOUT)
// ----------------------------------------------------------------------------
@Composable
private fun FailedContent(
    state: PaymentVerificationState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE53935).copy(alpha = 0.2f),
            border = BorderStroke(2.dp, Color(0xFFE53935)),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Failed",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Payment Not Completed",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = state.errorMessage ?: "MarzPay reported the transaction failed, was cancelled on your device, or timed out.",
                fontSize = 12.sp,
                color = Color(0xFFFF8A80),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }

        // Fraud prevention note
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF140E22),
            border = BorderStroke(1.dp, Color(0xFFB71C1C).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFFF8A80),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Subscription is not activated until funds are confirmed by MarzPay. Please try again with valid credentials.",
                    fontSize = 11.sp,
                    color = Color(0xFFD1C4E9)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color(0xFF140E22)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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
                border = BorderStroke(1.dp, Color(0xFF5E35B1).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("dismiss_failed_payment_button")
            ) {
                Text(
                    text = "Close",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
