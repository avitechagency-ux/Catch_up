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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.NoteCountGreen
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Student Affiliate Screen:
 * - Simple, colorful, and organized matching the Catch Up design standard
 * - Colorful elevated wallet card showing real-time balance and referral code
 * - Two vibrant, colorful action buttons (Withdraw in AmberTile, Pay Plans in TealTile)
 * - Two vibrant, colorful stat cards (Referrals in BlueTile, Total Earned in CoralTile)
 * - Improved, colorful transactions section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAffiliateScreen(
    modifier: Modifier = Modifier,
    studentName: String = "Student",
    initialBalance: Int = 1500,
    initialReferralCode: String = "STUDENT100",
    initialReferralCount: Int = 15,
    initialTransactions: List<AffiliateTransaction> = DefaultStudentAffiliateTransactions,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onPlanPurchased: (planDuration: String, planExpiry: String) -> Unit = { _, _ -> }
) {
    var balanceUgx by remember { mutableIntStateOf(initialBalance) }
    val referralCount by remember { mutableIntStateOf(initialReferralCount) }
    var transactions by remember { mutableStateOf(initialTransactions) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Bottom Sheets & Dialogs State
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showPayPlanSheet by remember { mutableStateOf(false) }
    var successReceiptDialog by remember { mutableStateOf<String?>(null) }

    // Withdraw Sheet fields
    var withdrawPhone by remember { mutableStateOf("0772 450 123") }
    var withdrawAmountText by remember { mutableStateOf("1000") }
    var selectedMoMoNetwork by remember { mutableStateOf("MTN") }
    var isWithdrawing by remember { mutableStateOf(false) }
    var withdrawError by remember { mutableStateOf<String?>(null) }

    // Plan payment selection
    var selectedPlanToBuy by remember { mutableStateOf<PlanTier?>(DefaultPlanTiers.firstOrNull()) }
    var isPayingPlan by remember { mutableStateOf(false) }
    var planPaymentError by remember { mutableStateOf<String?>(null) }

    val withdrawSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val payPlanSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Execute withdrawal
    val executeWithdrawal: () -> Unit = {
        val amount = withdrawAmountText.trim().toIntOrNull() ?: 0
        if (amount < 500) {
            withdrawError = "Minimum withdrawal is UGX 500"
        } else if (amount > balanceUgx) {
            withdrawError = "Insufficient balance. Available: ${formatUgx(balanceUgx)}"
        } else if (withdrawPhone.trim().length < 9) {
            withdrawError = "Please enter a valid phone number"
        } else {
            withdrawError = null
            isWithdrawing = true
            coroutineScope.launch {
                delay(1000)
                isWithdrawing = false
                balanceUgx -= amount
                val newTx = AffiliateTransaction(
                    id = "tx_${System.currentTimeMillis()}",
                    type = AffiliateTransactionType.WITHDRAWAL,
                    title = "Mobile Money Payout",
                    description = "$selectedMoMoNetwork MoMo to $withdrawPhone",
                    amountUgx = -amount,
                    date = "Today",
                    status = "Completed"
                )
                transactions = listOf(newTx) + transactions
                showWithdrawSheet = false
                successReceiptDialog = "Withdrawal of ${formatUgx(amount)} to $selectedMoMoNetwork ($withdrawPhone) completed."
            }
        }
    }

    // Execute plan payment
    val executePlanPayment: (PlanTier) -> Unit = { plan ->
        if (balanceUgx < plan.priceUgx) {
            planPaymentError = "Insufficient balance. Available: ${formatUgx(balanceUgx)}"
        } else {
            planPaymentError = null
            isPayingPlan = true
            coroutineScope.launch {
                delay(1000)
                isPayingPlan = false
                balanceUgx -= plan.priceUgx
                val newTx = AffiliateTransaction(
                    id = "tx_${System.currentTimeMillis()}",
                    type = AffiliateTransactionType.PLAN_PAYMENT,
                    title = "Plan Purchase",
                    description = "${plan.duration} pass via affiliate wallet",
                    amountUgx = -plan.priceUgx,
                    date = "Today",
                    status = "Completed"
                )
                transactions = listOf(newTx) + transactions
                showPayPlanSheet = false

                val expiry = when (plan.id) {
                    "1_month" -> "Valid for 30 Days"
                    "1_term" -> "Valid for Current Term"
                    "1_year" -> "Valid for 1 Academic Year"
                    else -> "Active"
                }
                onPlanPurchased(plan.duration, expiry)
                successReceiptDialog = "Subscribed to ${plan.duration} Pass using ${formatUgx(plan.priceUgx)} from your wallet."
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_affiliate_screen"),
        containerColor = Color(0xFFF9F8FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 3,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToSubjects()
                        2 -> onNavigateToDownloads()
                        3 -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(scrollState)
        ) {
            // ==========================================
            // TOP HEADER
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
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("student_affiliate_header")
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("student_affiliate_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Affiliate",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("student_affiliate_title")
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. STUDENT AFFILIATE WALLET CARD (Vibrant Indigo Gradient)
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = IndigoPurple.copy(alpha = 0.35f),
                            spotColor = Color(0x30000000)
                        )
                        .testTag("student_affiliate_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = IndigoPurple
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        IndigoPurple,
                                        Color(0xFF5B49AD),
                                        Color(0xFF3B2F77)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Brand Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.20f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MonetizationOn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CATCH UP WALLET",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LightLavender,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Available Balance
                            Text(
                                text = "Available Balance",
                                fontSize = 12.sp,
                                color = LightLavender,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatUgx(balanceUgx),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.testTag("student_affiliate_balance_text")
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Bottom row: Cardholder Name & Referral Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "CARDHOLDER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightLavender.copy(alpha = 0.8f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = studentName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Referral Code Pill (Tap to Copy)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(initialReferralCode))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Copied code: $initialReferralCode")
                                            }
                                        }
                                        .testTag("student_affiliate_copy_code_badge"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = initialReferralCode,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy code",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. VIBRANT COLORFUL ACTION BUTTONS (WITHDRAW & PAY PLANS)
                // Coloured cards reaching the standards of the application
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: Withdraw (Vibrant AmberTile)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = AmberTile.copy(alpha = 0.35f),
                                spotColor = Color(0x22000000)
                            )
                            .clickable { showWithdrawSheet = true }
                            .testTag("student_affiliate_withdraw_button"),
                        shape = RoundedCornerShape(18.dp),
                        color = AmberTile
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Withdraw",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mobile Money",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f)
                                )
                            }
                        }
                    }

                    // Option 2: Pay Plans (Vibrant TealTile)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = TealTile.copy(alpha = 0.35f),
                                spotColor = Color(0x22000000)
                            )
                            .clickable { showPayPlanSheet = true }
                            .testTag("student_affiliate_pay_plan_button"),
                        shape = RoundedCornerShape(18.dp),
                        color = TealTile
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pay Plans",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "With Balance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 3. COLORFUL STAT CARDS (Blue & Coral)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Referrals Count (BlueTile)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = BlueTile.copy(alpha = 0.35f),
                                spotColor = Color(0x22000000)
                            )
                            .testTag("student_stat_referrals"),
                        shape = RoundedCornerShape(16.dp),
                        color = BlueTile
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.20f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.People,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$referralCount",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Total Referrals",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                    }

                    // Card 2: Total Earned (CoralTile)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = CoralTile.copy(alpha = 0.35f),
                                spotColor = Color(0x22000000)
                            )
                            .testTag("student_stat_earned"),
                        shape = RoundedCornerShape(16.dp),
                        color = CoralTile
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.20f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatUgx(referralCount * UGX_PER_REFERRAL),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Total Earned",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                    }
                }

                // ==========================================
                // 4. IMPROVED TRANSACTIONS SECTION (Vibrant & Clean)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_transactions_section")
                ) {
                    Text(
                        text = "Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.testTag("student_transactions_header")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (transactions.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Text(
                                text = "No transactions yet.",
                                fontSize = 13.sp,
                                color = IndigoPurple,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            transactions.forEach { tx ->
                                ImprovedTransactionCard(tx = tx)
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // WITHDRAW BOTTOM SHEET
    // ==========================================
    if (showWithdrawSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWithdrawSheet = false },
            sheetState = withdrawSheetState,
            containerColor = IndigoPurple,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .testTag("student_withdraw_sheet"),
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
                            text = "Withdraw Funds",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Available Balance: ${formatUgx(balanceUgx)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F) // Bright Gold
                        )
                    }

                    IconButton(
                        onClick = { showWithdrawSheet = false },
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

                // Mobile Network Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Mobile Money Network",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("MTN", "Airtel").forEach { provider ->
                            val isSelected = selectedMoMoNetwork == provider
                            val providerColor = if (provider == "MTN") Color(0xFFFF8F00) else Color(0xFFE53935)
                            val bgSelected = if (provider == "MTN") Color(0xFFFFB300) else Color(0xFFE53935)

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedMoMoNetwork = provider }
                                    .testTag("momo_provider_$provider"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) bgSelected else Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.30f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMoMoNetwork = provider },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = if (provider == "MTN") Color(0xFF1A1A1A) else Color.White,
                                            unselectedColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$provider MoMo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected && provider == "MTN") Color(0xFF1A1A1A) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Phone Number
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Phone Number",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = withdrawPhone,
                        onValueChange = { withdrawPhone = it },
                        placeholder = {
                            Text(
                                "0772 123 456",
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_phone_input"),
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

                // Amount
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Amount (UGX)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        placeholder = {
                            Text(
                                "Min 500",
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
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

                withdrawError?.let { err ->
                    Text(
                        text = err,
                        fontSize = 13.sp,
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = executeWithdrawal,
                    enabled = !isWithdrawing && balanceUgx >= 500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("withdraw_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberTile,
                        disabledContainerColor = AmberTile.copy(alpha = 0.5f)
                    )
                ) {
                    if (isWithdrawing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Processing...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Text(
                            text = "Confirm Withdrawal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // PAY FOR PLANS BOTTOM SHEET
    // ==========================================
    if (showPayPlanSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPayPlanSheet = false },
            sheetState = payPlanSheetState,
            containerColor = IndigoPurple,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .testTag("student_pay_plans_sheet"),
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
                            text = "Pay Plan with Balance",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Available Balance: ${formatUgx(balanceUgx)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F) // Bright Gold
                        )
                    }

                    IconButton(
                        onClick = { showPayPlanSheet = false },
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

                Text(
                    text = "Choose Subscription Plan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DefaultPlanTiers.forEach { plan ->
                        val isSelected = selectedPlanToBuy?.id == plan.id
                        val canAfford = balanceUgx >= plan.priceUgx

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedPlanToBuy = plan }
                                .testTag("plan_option_${plan.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPlanToBuy = plan },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFFFD54F),
                                            unselectedColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = plan.duration,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = plan.caption,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LightLavender
                                        )
                                    }
                                }

                                Text(
                                    text = plan.price,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (canAfford) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                planPaymentError?.let { err ->
                    Text(
                        text = err,
                        fontSize = 13.sp,
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold
                    )
                }

                selectedPlanToBuy?.let { plan ->
                    val canAfford = balanceUgx >= plan.priceUgx
                    Button(
                        onClick = { executePlanPayment(plan) },
                        enabled = !isPayingPlan && canAfford,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pay_plan_confirm_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealTile,
                            disabledContainerColor = TealTile.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isPayingPlan) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Activating...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Text(
                                text = if (canAfford) "Pay ${plan.price}" else "Insufficient Balance",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Success dialog
    successReceiptDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { successReceiptDialog = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = IndigoPurple,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Success",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightLavender,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { successReceiptDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = Color(0xFF1A1A1A), fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }
}

/**
 * Improved Transaction Card:
 * Vibrant, colorful, and clean card styling for each transaction type.
 */
@Composable
private fun ImprovedTransactionCard(
    tx: AffiliateTransaction,
    modifier: Modifier = Modifier
) {
    val isCredit = tx.isCredit
    val (themeColor, icon) = when (tx.type) {
        AffiliateTransactionType.REFERRAL_EARNING -> Pair(TealTile, Icons.Outlined.CardGiftcard)
        AffiliateTransactionType.WITHDRAWAL -> Pair(AmberTile, Icons.Outlined.AccountBalanceWallet)
        AffiliateTransactionType.PLAN_PAYMENT -> Pair(IndigoPurple, Icons.Outlined.ReceiptLong)
    }

    val cardBg = when (tx.type) {
        AffiliateTransactionType.REFERRAL_EARNING -> Color(0xFFF0FDF4)
        AffiliateTransactionType.WITHDRAWAL -> Color(0xFFFFFBEB)
        AffiliateTransactionType.PLAN_PAYMENT -> Color(0xFFF5F3FF)
    }

    val cardBorder = when (tx.type) {
        AffiliateTransactionType.REFERRAL_EARNING -> Color(0xFFBBF7D0)
        AffiliateTransactionType.WITHDRAWAL -> Color(0xFFFDE68A)
        AffiliateTransactionType.PLAN_PAYMENT -> Color(0xFFDDD6FE)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = themeColor.copy(alpha = 0.25f),
                spotColor = Color(0x15000000)
            )
            .testTag("transaction_row_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Icon container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColor.copy(alpha = 0.18f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.description,
                        fontSize = 12.sp,
                        color = TextDark.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tx.date,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Colored Amount Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCredit) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
            ) {
                Text(
                    text = tx.formattedAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCredit) Color(0xFF047857) else Color(0xFFB91C1C),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
