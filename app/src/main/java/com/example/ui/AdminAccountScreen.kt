package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Outbox
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.TrendingUp
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data model for admin transaction entries.
 */
data class AdminTransactionItem(
    val id: String,
    val type: String, // "Withdrew" or "Sent"
    val description: String,
    val amount: String,
    val date: String,
    val isWithdrawal: Boolean = true
)

val DefaultAdminTransactions = listOf(
    AdminTransactionItem(
        id = "tx-1",
        type = "Withdrew",
        description = "MTN MoMo (078-***-1234)",
        amount = "UGX 500,000",
        date = "21/09/27",
        isWithdrawal = true
    ),
    AdminTransactionItem(
        id = "tx-2",
        type = "Sent",
        description = "Teacher Royalty Payout",
        amount = "UGX 250,000",
        date = "20/09/27",
        isWithdrawal = false
    ),
    AdminTransactionItem(
        id = "tx-3",
        type = "Withdrew",
        description = "Airtel Money (075-***-5678)",
        amount = "UGX 1,000,000",
        date = "18/09/27",
        isWithdrawal = true
    ),
    AdminTransactionItem(
        id = "tx-4",
        type = "Sent",
        description = "Server Hosting Reserve (DB)",
        amount = "UGX 150,000",
        date = "15/09/27",
        isWithdrawal = false
    ),
    AdminTransactionItem(
        id = "tx-5",
        type = "Withdrew",
        description = "Bank Transfer (Stanbic)",
        amount = "UGX 800,000",
        date = "11/09/27",
        isWithdrawal = true
    )
)

/**
 * Admin Account Screen for the Catch Up app.
 *
 * Specifications:
 * - TOP BAR: Back arrow on the left, navigates back; centered title "Account"
 * - BODY (scrollable):
 *   1. SECTION 1 — Balance: Card showing small label "Balance" above, large bold balance below
 *   2. SECTION 2 — Tax & DB: Row of 2 stat cards ("Tax" e.g. UGX 293,350 & "DB" e.g. UGX 300,000)
 *   3. SECTION 3 — Actions: Row of 2 buttons ("Send" and "Withdraw") opening stub transfer flows
 *   4. SECTION 4 — Transactions: Vertical list of transaction entries, most recent first
 * - BOTTOM NAVIGATION: No bottom nav (sub-page reached via back navigation)
 */
@Composable
fun AdminAccountScreen(
    modifier: Modifier = Modifier,
    initialBalance: String = "UGX 5,867,000",
    initialTax: String = "UGX 293,350",
    initialDb: String = "UGX 300,000",
    onNavigateBack: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf(DefaultAdminTransactions) }

    // Dialog states for Send and Withdraw stub flows
    var showSendDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    var sendRecipient by remember { mutableStateOf("") }
    var sendAmount by remember { mutableStateOf("") }
    var sendNote by remember { mutableStateOf("") }

    var withdrawMethod by remember { mutableStateOf("MTN MoMo") }
    var withdrawAccount by remember { mutableStateOf("") }
    var withdrawAmount by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_account_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // TOP BAR
            // Solid purple header (#4B3F8F) with rounded bottom corners (28dp)
            // Back arrow on the left, centered title "Account"
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
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .testTag("admin_account_header")
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("admin_account_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("admin_account_title")
                )
            }

            // ==========================================
            // BODY (Scrollable)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                // ------------------------------------------
                // SECTION 1 — Balance
                // Same visual weight/style as Revenue card on Admin Home
                // ------------------------------------------
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = IndigoPurple.copy(alpha = 0.35f),
                            spotColor = Color(0x33000000)
                        )
                        .testTag("admin_account_balance_card"),
                    shape = RoundedCornerShape(18.dp),
                    color = IndigoPurple
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(IndigoPurple, Color(0xFF5B4EB1))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.20f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = "Balance Wallet",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Balance",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LightLavender,
                                        modifier = Modifier.testTag("admin_account_balance_label")
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.20f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.TrendingUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Available",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = initialBalance,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.testTag("admin_account_balance_text")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ------------------------------------------
                // SECTION 2 — Tax & DB (Vibrant Colored Cards)
                // ------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_account_tax_db_row"),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card 1: Tax (Warm Coral / Orange Card)
                    AccountStatCard(
                        label = "Tax",
                        value = initialTax,
                        subtitle = "5% Withholding",
                        icon = Icons.Outlined.ReceiptLong,
                        cardColor = CoralTile,
                        gradientEndColor = Color(0xFFF08A5D),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_account_stat_card_tax")
                    )

                    // Card 2: DB (Tech Teal Card)
                    AccountStatCard(
                        label = "DB",
                        value = initialDb,
                        subtitle = "Operating Reserve",
                        icon = Icons.Outlined.Dns,
                        cardColor = TealTile,
                        gradientEndColor = Color(0xFF38B2A3),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_account_stat_card_db")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ------------------------------------------
                // SECTION 3 — Actions
                // Row of 2 buttons, equal width, side by side: Send & Withdraw
                // ------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_account_actions_row"),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Send Button
                    AccountActionButton(
                        text = "Send",
                        icon = Icons.Outlined.Send,
                        testTag = "admin_account_action_send",
                        modifier = Modifier.weight(1f),
                        onClick = { showSendDialog = true }
                    )

                    // Withdraw Button
                    AccountActionButton(
                        text = "Withdraw",
                        icon = Icons.Outlined.Payments,
                        testTag = "admin_account_action_withdraw",
                        modifier = Modifier.weight(1f),
                        onClick = { showWithdrawDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // ------------------------------------------
                // SECTION 4 — Transactions
                // Section label "Transactions", vertical list of transaction entries
                // ------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.testTag("admin_account_transactions_label")
                    )
                    Text(
                        text = "See All (Platform)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToTransactions)
                            .testTag("admin_account_see_all_transactions")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_account_transactions_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    transactions.forEach { item ->
                        AdminTransactionCard(item = item)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ==========================================
    // Send Funds Dialog (UI Shell / Stub)
    // ==========================================
    if (showSendDialog) {
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = {
                Text(
                    text = "Send Funds",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Transfer funds to teachers, staff, or operations partners.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = sendRecipient,
                        onValueChange = { sendRecipient = it },
                        label = { Text("Recipient / Account") },
                        placeholder = { Text("e.g. Teacher Okello Peter") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_send_recipient_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    OutlinedTextField(
                        value = sendAmount,
                        onValueChange = { sendAmount = it },
                        label = { Text("Amount (UGX)") },
                        placeholder = { Text("e.g. 150000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_send_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    OutlinedTextField(
                        value = sendNote,
                        onValueChange = { sendNote = it },
                        label = { Text("Note / Description (Optional)") },
                        placeholder = { Text("e.g. Monthly Curriculum Bonus") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val enteredAmount = sendAmount.trim().ifEmpty { "150,000" }
                        val formattedAmount = if (enteredAmount.startsWith("UGX")) enteredAmount else "UGX $enteredAmount"
                        val recipientText = sendRecipient.trim().ifEmpty { "Partner Account" }
                        val currentDate = SimpleDateFormat("yy/MM/dd", Locale.getDefault()).format(Date())

                        val newTx = AdminTransactionItem(
                            id = "tx-${System.currentTimeMillis()}",
                            type = "Sent",
                            description = recipientText,
                            amount = formattedAmount,
                            date = currentDate,
                            isWithdrawal = false
                        )
                        transactions = listOf(newTx) + transactions
                        showSendDialog = false
                        sendRecipient = ""
                        sendAmount = ""
                        sendNote = ""
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Funds sent successfully: $formattedAmount to $recipientText")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_send_confirm_button")
                ) {
                    Text("Confirm Send", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // ==========================================
    // Withdraw Funds Dialog (UI Shell / Stub)
    // ==========================================
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Text(
                    text = "Withdraw Funds",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Transfer withdrawable balance directly to mobile money or bank.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    // Method selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("MTN MoMo", "Airtel Money", "Bank").forEach { method ->
                            val isSelected = withdrawMethod == method
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) IndigoPurple else SoftLavenderGrey,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { withdrawMethod = method }
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = withdrawAccount,
                        onValueChange = { withdrawAccount = it },
                        label = { Text("Phone / Account Number") },
                        placeholder = { Text("e.g. 078-123-4567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_withdraw_account_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount (UGX)") },
                        placeholder = { Text("e.g. 500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_withdraw_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val enteredAmount = withdrawAmount.trim().ifEmpty { "500,000" }
                        val formattedAmount = if (enteredAmount.startsWith("UGX")) enteredAmount else "UGX $enteredAmount"
                        val acctText = withdrawAccount.trim().ifEmpty { "078-***-XXXX" }
                        val currentDate = SimpleDateFormat("yy/MM/dd", Locale.getDefault()).format(Date())

                        val newTx = AdminTransactionItem(
                            id = "tx-${System.currentTimeMillis()}",
                            type = "Withdrew",
                            description = "$withdrawMethod ($acctText)",
                            amount = formattedAmount,
                            date = currentDate,
                            isWithdrawal = true
                        )
                        transactions = listOf(newTx) + transactions
                        showWithdrawDialog = false
                        withdrawAccount = ""
                        withdrawAmount = ""
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Withdrawal processed: $formattedAmount via $withdrawMethod")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_withdraw_confirm_button")
                ) {
                    Text("Confirm Withdraw", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Stat Card for Tax and DB metrics with rich vibrant background colors and subtle gradients.
 */
@Composable
private fun AccountStatCard(
    label: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cardColor: Color,
    gradientEndColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = cardColor.copy(alpha = 0.35f),
                spotColor = Color(0x22000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = cardColor
    ) {
        val boxModifier = if (gradientEndColor != null) {
            Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(cardColor, gradientEndColor)
                    )
                )
                .padding(16.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        }

        Box(modifier = boxModifier) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.90f)
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Full-width action button for Send and Withdraw actions.
 */
@Composable
private fun AccountActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.25f),
                spotColor = Color(0x20000000)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = IndigoPurple
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * List card style for a single transaction entry.
 * Left: Transaction type (Withdrew / Sent) & description
 * Below/beside: Amount
 * Right: Date
 */
@Composable
private fun AdminTransactionCard(
    item: AdminTransactionItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x10000000),
                spotColor = Color(0x15000000)
            )
            .border(1.dp, BorderGrey, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Direction icon badge
                Surface(
                    shape = CircleShape,
                    color = if (item.isWithdrawal) Color(0xFFFFF1EB) else Color(0xFFE8F5E9),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.isWithdrawal) Icons.AutoMirrored.Outlined.CallMade else Icons.AutoMirrored.Outlined.CallReceived,
                            contentDescription = item.type,
                            tint = if (item.isWithdrawal) Color(0xFFE76F51) else Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.type,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.amount,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Date on the right
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SoftLavenderGrey
            ) {
                Text(
                    text = item.date,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = IconGrey,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
