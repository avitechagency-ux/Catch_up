package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data representation of a selectable student subscription tier.
 */
data class PlanTier(
    val id: String,
    val duration: String,
    val caption: String,
    val price: String,
    val priceUgx: Int
)

/**
 * Single student subscription plan:
 * "1 Month" — caption "Billed monthly" — price "UGX 1,000"
 */
val DefaultPlanTiers = listOf(
    PlanTier(
        id = "1_month",
        duration = "1 Month",
        caption = "Billed monthly",
        price = "UGX 1,000",
        priceUgx = 1000
    )
)

/**
 * Dedicated Payment Screen shown immediately after Student Signup before entering the app.
 * Uses the student's signup phone number and the pre-selected 1 Month (UGX 1,000) plan.
 */
@Composable
fun StudentPaymentScreen(
    modifier: Modifier = Modifier,
    studentName: String = "Student",
    signupPhoneNumber: String = "",
    studentEmail: String = "",
    selectedPlan: PlanTier = DefaultPlanTiers.first(),
    onNavigateBack: () -> Unit = {},
    onPaymentSuccess: (PlanTier, String) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember(signupPhoneNumber) {
        mutableStateOf(if (signupPhoneNumber.contains("@")) "" else signupPhoneNumber)
    }
    val defaultNetwork = remember(signupPhoneNumber) {
        val cleaned = signupPhoneNumber.replace(" ", "")
        if (cleaned.startsWith("070") || cleaned.startsWith("074") || cleaned.startsWith("075") ||
            cleaned.startsWith("+25670") || cleaned.startsWith("+25674") || cleaned.startsWith("+25675")
        ) {
            "Airtel"
        } else {
            "MTN"
        }
    }
    var selectedNetwork by remember(defaultNetwork) { mutableStateOf(defaultNetwork) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_payment_screen"),
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payment",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag("payment_screen_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Complete your ${selectedPlan.duration} subscription to continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightLavender
                    )
                }
                IconButton(
                    onClick = onNavigateBack,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .testTag("payment_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Selected Plan Card (1 Month — UGX 1,000)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_selected_plan_card"),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD54F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECTED PLAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F),
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = Color(0xFFE5F5F0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2A9D8F),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1,000 UGX / Month",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2A9D8F)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedPlan.duration,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = selectedPlan.caption,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightLavender
                            )
                        }
                        Text(
                            text = selectedPlan.price,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }

            // Mobile Network Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Mobile Network",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isMtn = selectedNetwork == "MTN"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedNetwork = "MTN" }
                            .testTag("network_mtn"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isMtn) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(
                            1.5.dp,
                            if (isMtn) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.30f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isMtn,
                                onClick = { selectedNetwork = "MTN" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isMtn) Color(0xFF1A1A1A) else Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MTN MoMo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isMtn) Color(0xFF1A1A1A) else Color.White
                            )
                        }
                    }

                    val isAirtel = selectedNetwork == "Airtel"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedNetwork = "Airtel" }
                            .testTag("network_airtel"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isAirtel) Color(0xFFE53935) else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(
                            1.5.dp,
                            if (isAirtel) Color(0xFFFF8A80) else Color.White.copy(alpha = 0.30f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isAirtel,
                                onClick = { selectedNetwork = "Airtel" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Airtel Money",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Phone Number Input for Mobile Money
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Mobile Money Phone Number",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
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
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_phone_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                        focusedBorderColor = Color(0xFFFFD54F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        cursorColor = Color(0xFFFFD54F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            if (statusMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2A9D8F).copy(alpha = 0.25f),
                    border = BorderStroke(1.5.dp, Color(0xFF80CBC4))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = statusMessage!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE53935).copy(alpha = 0.25f),
                    border = BorderStroke(1.5.dp, Color(0xFFFF8A80))
                ) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Pay Button
            Button(
                onClick = {
                    val cleanPhone = phoneNumber.trim()
                    if (cleanPhone.isBlank()) {
                        errorMessage = "Please enter your phone number to process payment"
                        return@Button
                    }
                    isProcessing = true
                    errorMessage = null
                    statusMessage = "Sending STK push prompt to $cleanPhone ($selectedNetwork)..."
                    coroutineScope.launch {
                        val initResult = com.example.data.MarzPayPaymentService.initiateSubscriptionPayment(
                            phoneNumber = cleanPhone,
                            studentName = studentName,
                            userEmail = studentEmail,
                            planDuration = selectedPlan.duration,
                            amountUgx = selectedPlan.priceUgx.toLong()
                        )

                        when (initResult) {
                            is com.example.data.PaymentResult.Success -> {
                                statusMessage = null
                                isProcessing = false
                                onPaymentSuccess(selectedPlan, cleanPhone)
                            }
                            is com.example.data.PaymentResult.Pending -> {
                                statusMessage = "STK push prompt sent! Please enter your Mobile Money PIN on your phone..."
                                // Poll MarzPay status up to 5 times (15 seconds total)
                                var pollSuccess = false
                                for (attempt in 1..5) {
                                    delay(3000L)
                                    val check = com.example.data.MarzPayPaymentService.checkPaymentStatus(
                                        transactionId = initResult.transactionId,
                                        marzpayTxId = initResult.marzpayTxId,
                                        email = studentEmail,
                                        phone = cleanPhone
                                    )
                                    if (check?.isCompleted == true) {
                                        pollSuccess = true
                                        break
                                    } else if (check?.isFailed == true) {
                                        errorMessage = check.error ?: "Payment was cancelled or failed on your mobile money."
                                        break
                                    }
                                }
                                isProcessing = false
                                statusMessage = null
                                if (pollSuccess || errorMessage == null) {
                                    onPaymentSuccess(selectedPlan, cleanPhone)
                                }
                            }
                            is com.example.data.PaymentResult.Failed -> {
                                isProcessing = false
                                statusMessage = null
                                errorMessage = initResult.error
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    disabledContainerColor = Color(0xFFFFB300).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("pay_button")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Waiting for PIN approval...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                } else {
                    Text(
                        text = "Pay ${selectedPlan.price}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

/**
 * Student Account Screen
 *
 * Reached by tapping "Account" on the Student Profile screen.
 * Follows established Catch Up design system (brand purple #4B3F8F, clean spacing, high readability).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    studentName: String = "Student",
    studentEmail: String = "",
    activePlanDuration: String = "",
    activePlanExpiry: String = "",
    initialPhoneNumber: String = "",
    plans: List<PlanTier> = DefaultPlanTiers,
    onNavigateBack: () -> Unit = {},
    onNavigateToPayment: (PlanTier) -> Unit = {},
    onPlanUpdated: (PlanTier) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedPlanForPayment by remember { mutableStateOf<PlanTier?>(null) }
    var currentActiveDuration by remember(activePlanDuration) { mutableStateOf(activePlanDuration) }
    var currentActiveExpiry by remember(activePlanExpiry) { mutableStateOf(activePlanExpiry) }
    val hasActivePlan = isStudentPlanActive(currentActiveDuration)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_account_screen"),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ======================================
            // TOP BAR
            // - Background #FFFFFF, height 56dp, bottom border 1dp #F0F0F0
            // - Left: back arrow icon (24dp, #1A1A1A), navigates back to Profile
            // - Center: title "Account" (16sp, weight SemiBold, color #1A1A1A)
            // ======================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White)
                    .testTag("account_top_bar")
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                        .size(48.dp)
                        .testTag("account_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Profile",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("account_title")
                )

                // 1dp bottom border #F0F0F0
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF0F0F0))
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // ======================================
                // SECTION 1 — Current Plan
                // - 20dp top padding below top bar, 20dp left/right margins
                // - Section label: "Plan" (13sp, weight Medium, color #8A8A8A)
                // - Below it (8dp gap), a card showing the current active plan:
                //   - Background: solid #4B3F8F (brand purple)
                //   - Corner radius: 16dp
                //   - Padding: 18dp
                //   - Content: small label "Current Plan" (12sp, color #D6D0F0)
                //     below it (6dp gap) duration in large bold text (22sp, weight Bold, color #FFFFFF)
                //     below that (8dp gap), small caption showing expiry (12sp, color #D6D0F0)
                // ======================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = "Plan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("account_section_plan_label")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (hasActivePlan) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_current_plan_card"),
                            shape = RoundedCornerShape(16.dp),
                            color = IndigoPurple,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Current Plan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = LightLavender,
                                    modifier = Modifier.testTag("current_plan_label")
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = currentActiveDuration,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("current_plan_duration")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentActiveExpiry,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = LightLavender,
                                    modifier = Modifier.testTag("current_plan_expiry")
                                )
                            }
                        }
                    } else {
                        UnloadedPlanCardPlaceholder(
                            onClick = {
                                val firstPlan = plans.firstOrNull()
                                if (firstPlan != null) {
                                    onNavigateToPayment(firstPlan)
                                    selectedPlanForPayment = firstPlan
                                }
                            },
                            modifier = Modifier.testTag("account_unloaded_plan_card")
                        )
                    }
                }

                // ======================================
                // SECTION 2 — Plans (selectable tiers)
                // - 28dp spacing below Section 1
                // - Section label: "Plans" (16sp, weight SemiBold, color #1A1A1A)
                // - Below it (12dp gap), vertical list of 3 plan option cards, 12dp spacing between them
                // ======================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = "Plans",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.testTag("account_section_plans_label")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        plans.forEach { plan ->
                            val isActive = plan.duration.equals(currentActiveDuration, ignoreCase = true)
                            PlanOptionCard(
                                plan = plan,
                                isActive = isActive,
                                onSelect = {
                                    onNavigateToPayment(plan)
                                    selectedPlanForPayment = plan
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ======================================
    // PAYMENT BOTTOM SHEET (Phone + Mobile Network Selection + Pay Button)
    // Invoked when user taps a non-active plan
    // ======================================
    selectedPlanForPayment?.let { targetPlan ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var phoneNumber by remember(initialPhoneNumber) {
            mutableStateOf(if (initialPhoneNumber.contains("@")) "" else initialPhoneNumber)
        }
        val defaultNetwork = remember(initialPhoneNumber) {
            val cleaned = initialPhoneNumber.replace(" ", "")
            if (cleaned.startsWith("070") || cleaned.startsWith("074") || cleaned.startsWith("075") ||
                cleaned.startsWith("+25670") || cleaned.startsWith("+25674") || cleaned.startsWith("+25675")
            ) {
                "Airtel"
            } else {
                "MTN"
            }
        }
        var selectedNetwork by remember(defaultNetwork) { mutableStateOf(defaultNetwork) } // "MTN" or "Airtel"
        var isProcessing by remember { mutableStateOf(false) }
        var bottomStatusMessage by remember { mutableStateOf<String?>(null) }
        var bottomErrorMessage by remember { mutableStateOf<String?>(null) }

        ModalBottomSheet(
            onDismissRequest = {
                if (!isProcessing) selectedPlanForPayment = null
            },
            sheetState = sheetState,
            containerColor = IndigoPurple,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("payment_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Row: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Payment",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Subscribe to ${targetPlan.duration} plan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightLavender
                        )
                    }
                    IconButton(
                        onClick = { selectedPlanForPayment = null },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Plan & Price Row (Clean, bold texts directly on popup background)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = targetPlan.duration,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = targetPlan.caption,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightLavender
                        )
                    }
                    Text(
                        text = targetPlan.price,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD54F) // Bright Gold
                    )
                }

                // Mobile Network Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Mobile Network",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // MTN Mobile Money
                        val isMtn = selectedNetwork == "MTN"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedNetwork = "MTN" }
                                .testTag("network_mtn"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isMtn) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.5.dp,
                                if (isMtn) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.30f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isMtn,
                                    onClick = { selectedNetwork = "MTN" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = if (isMtn) Color(0xFF1A1A1A) else Color.White,
                                        unselectedColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MTN MoMo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isMtn) Color(0xFF1A1A1A) else Color.White
                                )
                            }
                        }

                        // Airtel Money
                        val isAirtel = selectedNetwork == "Airtel"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedNetwork = "Airtel" }
                                .testTag("network_airtel"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAirtel) Color(0xFFE53935) else Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.5.dp,
                                if (isAirtel) Color(0xFFFF8A80) else Color.White.copy(alpha = 0.30f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isAirtel,
                                    onClick = { selectedNetwork = "Airtel" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White,
                                        unselectedColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Airtel Money",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Phone Number input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Phone Number",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
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
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_phone_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = Color(0xFFFFD54F),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            cursorColor = Color(0xFFFFD54F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                if (bottomStatusMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2A9D8F).copy(alpha = 0.25f),
                        border = BorderStroke(1.5.dp, Color(0xFF80CBC4))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = bottomStatusMessage!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (bottomErrorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.25f),
                        border = BorderStroke(1.5.dp, Color(0xFFFF8A80))
                    ) {
                        Text(
                            text = bottomErrorMessage!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Pay Button
                Button(
                    onClick = {
                        val cleanPhone = phoneNumber.trim()
                        if (cleanPhone.isBlank()) {
                            bottomErrorMessage = "Please enter your phone number to process payment"
                            return@Button
                        }
                        isProcessing = true
                        bottomErrorMessage = null
                        bottomStatusMessage = "Sending STK push prompt to $cleanPhone ($selectedNetwork)..."
                        coroutineScope.launch {
                            val initResult = com.example.data.MarzPayPaymentService.initiateSubscriptionPayment(
                                phoneNumber = cleanPhone,
                                studentName = studentName,
                                userEmail = studentEmail,
                                planDuration = targetPlan.duration,
                                amountUgx = targetPlan.priceUgx.toLong()
                            )

                            when (initResult) {
                                is com.example.data.PaymentResult.Success -> {
                                    isProcessing = false
                                    bottomStatusMessage = null
                                    currentActiveDuration = targetPlan.duration
                                    currentActiveExpiry = "Valid for 30 days"
                                    onPlanUpdated(targetPlan)
                                    selectedPlanForPayment = null
                                    snackbarHostState.showSnackbar("Payment received! Plan updated to ${targetPlan.duration}.")
                                }
                                is com.example.data.PaymentResult.Pending -> {
                                    bottomStatusMessage = "STK push prompt sent! Please enter your Mobile Money PIN on your phone..."
                                    var pollSuccess = false
                                    for (attempt in 1..5) {
                                        delay(3000L)
                                        val check = com.example.data.MarzPayPaymentService.checkPaymentStatus(
                                            transactionId = initResult.transactionId,
                                            marzpayTxId = initResult.marzpayTxId,
                                            email = studentEmail,
                                            phone = cleanPhone
                                        )
                                        if (check?.isCompleted == true) {
                                            pollSuccess = true
                                            break
                                        } else if (check?.isFailed == true) {
                                            bottomErrorMessage = check.error ?: "Payment was cancelled or failed."
                                            break
                                        }
                                    }
                                    isProcessing = false
                                    bottomStatusMessage = null
                                    if (pollSuccess || bottomErrorMessage == null) {
                                        currentActiveDuration = targetPlan.duration
                                        currentActiveExpiry = "Valid for 30 days"
                                        onPlanUpdated(targetPlan)
                                        selectedPlanForPayment = null
                                        snackbarHostState.showSnackbar("Payment received! Plan updated to ${targetPlan.duration}.")
                                    }
                                }
                                is com.example.data.PaymentResult.Failed -> {
                                    isProcessing = false
                                    bottomStatusMessage = null
                                    bottomErrorMessage = initResult.error
                                }
                            }
                        }
                    },
                    enabled = !isProcessing && phoneNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        disabledContainerColor = Color(0xFFFFB300).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pay_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Waiting for PIN approval...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                    } else {
                        Text(
                            text = "Pay ${targetPlan.price}",
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

/**
 * Single Plan Option Card
 *
 * Requirements:
 * - Full width, height 68dp
 * - Background: #FFFFFF
 * - Border: 1.5dp solid #ECEAF5
 * - Corner radius: 16dp
 * - Padding: 16dp horizontal
 * - Subtle shadow: 1dp, #000000 at 5% opacity (barely visible)
 * - Content layout, horizontal row:
 *   - Left: vertical accent bar (4dp width, full card height, corner radius 2dp, color #4B3F8F)
 *     flush against the card's left inner edge
 *   - 14dp spacing after accent bar, then vertical text stack:
 *     - Plan duration: font 15sp, weight SemiBold, color #1A1A1A
 *     - Below it (2dp gap): small descriptive caption, font 11sp, color #8A8A8A
 *   - Spacer pushes price to the far right
 *   - Price text: font 16sp, weight Bold, color #4B3F8F, right-aligned
 * - If CURRENTLY active plan:
 *   - add small "Active" badge (pill: background #E5F5F0, text color #2A9D8F, font 10sp weight SemiBold,
 *     padding 4dp horizontal 2dp vertical, corner radius 8dp) positioned top-right corner of the card
 *   - reduce card tap-interactivity (not re-selectable)
 */
@Composable
fun PlanOptionCard(
    plan: PlanTier,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .then(
                if (!isActive) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // Custom clean feedback or ripple
                        onClick = onSelect
                    )
                } else {
                    Modifier
                }
            )
            .testTag("plan_card_${plan.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, IndigoPurple),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Horizontal row layout with 16dp horizontal padding
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vertical accent bar (4dp width, corner radius 2dp, color #4B3F8F)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IndigoPurple)
                        .testTag("plan_accent_bar_${plan.id}")
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Vertical text stack: Plan duration + Caption
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = plan.duration,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.testTag("plan_duration_${plan.id}")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("plan_caption_${plan.id}")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price text: font 16sp, weight Bold, color #4B3F8F, right-aligned
                Text(
                    text = plan.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndigoPurple,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("plan_price_${plan.id}")
                )
            }

            // If this card represents the student's CURRENTLY active plan:
            // small "Active" badge — tiny pill (background #E5F5F0, text color #2A9D8F, font 10sp SemiBold,
            // padding 4dp horizontal 2dp vertical, corner radius 8dp) positioned top-right corner of card
            if (isActive) {
                Surface(
                    color = Color(0xFFE5F5F0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 10.dp)
                        .testTag("plan_active_badge_${plan.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color(0xFF2A9D8F),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2A9D8F)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AccountScreenPreview() {
    AccountScreen()
}
