package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Category enum for platform-wide transactions.
 */
enum class PlatformTransactionCategory(val displayName: String) {
    ALL("All"),
    SUBSCRIPTION("Subscriptions"),
    TEACHER_ROYALTY("Teacher Royalties"),
    TAX_OPS("Tax & Server Ops")
}

/**
 * Status of a transaction.
 */
enum class PlatformTransactionStatus(val displayName: String) {
    COMPLETED("Completed"),
    PENDING("Pending"),
    FAILED("Failed")
}

/**
 * Model representing a comprehensive transaction across the Catch Up platform.
 */
data class AdminPlatformTransaction(
    val id: String,
    val referenceNumber: String,
    val title: String,
    val category: PlatformTransactionCategory,
    val userName: String,
    val userRole: String,
    val amountUgx: Long, // Positive = Inflow (Credit), Negative = Outflow (Debit)
    val paymentMethod: String,
    val date: String,
    val time: String,
    val status: PlatformTransactionStatus = PlatformTransactionStatus.COMPLETED,
    val feeUgx: Long = 0,
    val netUgx: Long = amountUgx,
    val notes: String = "",
    val studentClassOrSubject: String? = null
) {
    val isCredit: Boolean get() = amountUgx > 0

    val formattedAmount: String
        get() {
            val absAmount = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(kotlin.math.abs(amountUgx))
            return if (isCredit) "+UGX $absAmount" else "-UGX $absAmount"
        }

    val formattedNet: String
        get() {
            val absAmount = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(kotlin.math.abs(netUgx))
            return if (netUgx >= 0) "+UGX $absAmount" else "-UGX $absAmount"
        }

    val formattedFee: String
        get() = "UGX " + java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(feeUgx)
}

/**
 * Default transactions representing all performed platform activities.
 */
val DefaultPlatformTransactions = listOf(
    AdminPlatformTransaction(
        id = "TX-98401",
        referenceNumber = "UG-MOMO-9840124",
        title = "Termly Access Subscription",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Amina Namukasa",
        userRole = "Student",
        amountUgx = 35000,
        paymentMethod = "MTN MoMo (078-***-4921)",
        date = "24 Sep 2026",
        time = "11:42 AM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 525,
        netUgx = 34475,
        notes = "Full term unlimited study notes & revision papers pass for Senior 4 Candidate.",
        studentClassOrSubject = "S.4 All Subjects"
    ),
    AdminPlatformTransaction(
        id = "TX-98402",
        referenceNumber = "UG-MOMO-9840211",
        title = "Teacher Study Notes Remittance",
        category = PlatformTransactionCategory.TEACHER_ROYALTY,
        userName = "Denis Mukasa",
        userRole = "Teacher",
        amountUgx = -50000,
        paymentMethod = "MTN MoMo (078-***-1234)",
        date = "24 Sep 2026",
        time = "10:15 AM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 750,
        netUgx = -50750,
        notes = "Study materials royalty payout to registered mobile money account.",
        studentClassOrSubject = "S.3 Chemistry"
    ),
    AdminPlatformTransaction(
        id = "TX-98403",
        referenceNumber = "UG-AIRTEL-9840398",
        title = "Monthly Unlimited Pass",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Brian Okello",
        userRole = "Student",
        amountUgx = 15000,
        paymentMethod = "Airtel Money (075-***-8821)",
        date = "24 Sep 2026",
        time = "09:30 AM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 225,
        netUgx = 14775,
        notes = "30-Day revision notes access pass subscription.",
        studentClassOrSubject = "S.6 Sciences"
    ),
    AdminPlatformTransaction(
        id = "TX-98404",
        referenceNumber = "UG-PAY-9840455",
        title = "Teacher Royalty Payout",
        category = PlatformTransactionCategory.TEACHER_ROYALTY,
        userName = "Tr. Emmanuel Kato",
        userRole = "Teacher",
        amountUgx = -250000,
        paymentMethod = "Stanbic Bank (Acct: 903001***)",
        date = "23 Sep 2026",
        time = "04:45 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 2500,
        netUgx = -252500,
        notes = "September study material upload royalty payout for Physics & Mathematics A-Level notes.",
        studentClassOrSubject = "Physics / Math"
    ),
    AdminPlatformTransaction(
        id = "TX-98405",
        referenceNumber = "UG-INT-9840510",
        title = "Monthly Student Revision Pass",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Sarah Nabirye",
        userRole = "Student",
        amountUgx = 5000,
        paymentMethod = "MTN MoMo",
        date = "23 Sep 2026",
        time = "02:18 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 0,
        netUgx = 5000,
        notes = "1-Month student revision notes subscription access pass.",
        studentClassOrSubject = "S.2 All Subjects"
    ),
    AdminPlatformTransaction(
        id = "TX-98406",
        referenceNumber = "UG-MOMO-9840632",
        title = "Weekly Access Plan",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Joshua Mukisa",
        userRole = "Student",
        amountUgx = 5000,
        paymentMethod = "MTN MoMo (078-***-3349)",
        date = "23 Sep 2026",
        time = "01:05 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 75,
        netUgx = 4925,
        notes = "7-Day revision notes and past paper downloads access pass.",
        studentClassOrSubject = "S.3 Arts & Sciences"
    ),
    AdminPlatformTransaction(
        id = "TX-98407",
        referenceNumber = "UG-TAX-9840700",
        title = "Withholding Tax Remittance (URA)",
        category = PlatformTransactionCategory.TAX_OPS,
        userName = "Uganda Revenue Authority",
        userRole = "Statutory Authority",
        amountUgx = -293350,
        paymentMethod = "URA E-Tax Portal Direct",
        date = "22 Sep 2026",
        time = "03:00 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 1500,
        netUgx = -294850,
        notes = "Monthly statutory withholding tax remittance on platform educator disbursements."
    ),
    AdminPlatformTransaction(
        id = "TX-98408",
        referenceNumber = "UG-MOMO-9840819",
        title = "Daily Quick Pass",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Grace Akello",
        userRole = "Student",
        amountUgx = 1000,
        paymentMethod = "MTN MoMo (077-***-9021)",
        date = "22 Sep 2026",
        time = "11:10 AM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 15,
        netUgx = 985,
        notes = "24-Hour urgent homework & study materials access.",
        studentClassOrSubject = "S.2 Biology"
    ),
    AdminPlatformTransaction(
        id = "TX-98409",
        referenceNumber = "UG-AIRTEL-9840944",
        title = "Teacher Materials Remittance",
        category = PlatformTransactionCategory.TEACHER_ROYALTY,
        userName = "Sarah Nabirye",
        userRole = "Teacher",
        amountUgx = -30000,
        paymentMethod = "Airtel Money (075-***-5678)",
        date = "21 Sep 2026",
        time = "05:20 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 450,
        netUgx = -30450,
        notes = "Processed monthly study notes author royalty payout via Airtel Money.",
        studentClassOrSubject = "S.5 Biology"
    ),
    AdminPlatformTransaction(
        id = "TX-98410",
        referenceNumber = "UG-OPS-9841001",
        title = "Server & Database Cloud Reserve",
        category = PlatformTransactionCategory.TAX_OPS,
        userName = "Google Cloud & Database Host",
        userRole = "Infrastructure",
        amountUgx = -150000,
        paymentMethod = "Visa Business Card (*** 4102)",
        date = "21 Sep 2026",
        time = "02:00 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 0,
        netUgx = -150000,
        notes = "High-availability cloud notes storage, backups, and CDN bandwidth allocation."
    ),
    AdminPlatformTransaction(
        id = "TX-98411",
        referenceNumber = "UG-PAY-9841183",
        title = "Teacher Royalty Payout",
        category = PlatformTransactionCategory.TEACHER_ROYALTY,
        userName = "Tr. Christine Namutebi",
        userRole = "Teacher",
        amountUgx = -180000,
        paymentMethod = "MTN MoMo (078-***-7712)",
        date = "20 Sep 2026",
        time = "10:45 AM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 1800,
        netUgx = -181800,
        notes = "Biology and Chemistry syllabus lesson summaries royalty disbursement.",
        studentClassOrSubject = "Biology / Chem"
    ),
    AdminPlatformTransaction(
        id = "TX-98412",
        referenceNumber = "UG-MOMO-9841299",
        title = "Annual VIP All-Access Subscription",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "David Ssemwogerere",
        userRole = "Student",
        amountUgx = 90000,
        paymentMethod = "MTN MoMo (077-***-1144)",
        date = "19 Sep 2026",
        time = "06:12 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 1350,
        netUgx = 88650,
        notes = "Full academic year unlimited subscription with offline downloads for all subjects.",
        studentClassOrSubject = "S.5 Sciences VIP"
    ),
    AdminPlatformTransaction(
        id = "TX-98413",
        referenceNumber = "UG-INT-9841341",
        title = "Candidate Termly Pass",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Ivan Tumusiime",
        userRole = "Student",
        amountUgx = 15000,
        paymentMethod = "MTN MoMo (078-***-5521)",
        date = "19 Sep 2026",
        time = "06:14 PM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 225,
        netUgx = 14775,
        notes = "Termly study notes access pass for candidate student.",
        studentClassOrSubject = "S.4 All Subjects"
    ),
    AdminPlatformTransaction(
        id = "TX-98414",
        referenceNumber = "UG-MOMO-9841412",
        title = "Termly Access Subscription",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Rebecca Nakato",
        userRole = "Student",
        amountUgx = 35000,
        paymentMethod = "MTN MoMo (078-***-6510)",
        date = "18 Sep 2026",
        time = "08:50 AM",
        status = PlatformTransactionStatus.COMPLETED,
        feeUgx = 525,
        netUgx = 34475,
        notes = "Full term access to O-Level past exams, topical notes, and marking guides.",
        studentClassOrSubject = "S.3 All Subjects"
    ),
    AdminPlatformTransaction(
        id = "TX-98415",
        referenceNumber = "UG-AIRTEL-9841590",
        title = "Weekly Access Plan",
        category = PlatformTransactionCategory.SUBSCRIPTION,
        userName = "Samuel Kato",
        userRole = "Student",
        amountUgx = 5000,
        paymentMethod = "Airtel Money (070-***-4091)",
        date = "17 Sep 2026",
        time = "03:15 PM",
        status = PlatformTransactionStatus.PENDING,
        feeUgx = 75,
        netUgx = 4925,
        notes = "Pending network confirmation on Airtel Money push gateway.",
        studentClassOrSubject = "S.1 General"
    )
)

/**
 * Admin Transactions Screen.
 *
 * Provides complete administrative overview of all performed transactions in the entire platform:
 * - Header matching platform design with embedded Search bar
 * - 4 Summary stat cards (Total Inflow, Total Outflow, Net Balance, Total Count)
 * - Category and Status filter chips
 * - Interactive Transaction Cards with distinct type badges, colors, and amounts
 * - Detailed Receipt Bottom Sheet modal showing all transaction details when clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTransactionsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PlatformTransactionCategory.ALL) }
    var selectedStatus by remember { mutableStateOf<PlatformTransactionStatus?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<AdminPlatformTransaction?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var allTransactions by remember { mutableStateOf<List<AdminPlatformTransaction>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        allTransactions = com.example.data.SupabaseRepository.fetchAllPlatformTransactions()
    }

    // Filtered transaction list
    val filteredTransactions = remember(searchQuery, selectedCategory, selectedStatus, allTransactions) {
        allTransactions.filter { tx ->
            val matchesCategory = (selectedCategory == PlatformTransactionCategory.ALL || tx.category == selectedCategory)
            val matchesStatus = (selectedStatus == null || tx.status == selectedStatus)
            val query = searchQuery.trim()
            val matchesSearch = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.userName.contains(query, ignoreCase = true) ||
                    tx.id.contains(query, ignoreCase = true) ||
                    tx.referenceNumber.contains(query, ignoreCase = true) ||
                    tx.paymentMethod.contains(query, ignoreCase = true) ||
                    tx.formattedAmount.contains(query, ignoreCase = true)

            matchesCategory && matchesStatus && matchesSearch
        }
    }

    // Dynamic stats calculations
    val totalInflow = remember(allTransactions) {
        allTransactions.filter { it.amountUgx > 0 }.sumOf { it.amountUgx }
    }
    val totalOutflow = remember(allTransactions) {
        allTransactions.filter { it.amountUgx < 0 }.sumOf { kotlin.math.abs(it.amountUgx) }
    }
    val netBalance = remember(allTransactions) {
        allTransactions.sumOf { it.amountUgx }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_transactions_screen"),
        containerColor = Color(0xFFF9F8FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = 3,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToAnalysis()
                        2 -> onNavigateToUsers()
                        3 -> onNavigateToMore()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // 1. HEADER: Purple card with Title & Search Bar
            // Matching Students Subjects & Admin pages
            // ==========================================
            Column(
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
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp)
                    .testTag("admin_transactions_header")
            ) {
                // Top Row: Back Button, Centered Title, and Count Badge
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("admin_transactions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("admin_transactions_title")
                    )

                    // Refresh / Filter pill on right
                    Surface(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                coroutineScope.launch {
                                    allTransactions = com.example.data.SupabaseRepository.fetchAllPlatformTransactions()
                                    snackbarHostState.showSnackbar("All platform transactions up to date")
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.20f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = "Live",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${filteredTransactions.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar embedded inside the purple header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0x10000000),
                            spotColor = Color(0x15000000)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("admin_transactions_search_bar"),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = IconGrey,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search transactions, users, refs...",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_transactions_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = IconGrey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. BODY LIST: STATS, FILTERS & CARDS
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("admin_transactions_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // SECTION 1: Stat Summary Cards
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Platform Overview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Inflow Card (Teal)
                        PlatformStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Inflow",
                            amount = if (totalInflow > 0L) "UGX " + java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalInflow) else "0",
                            icon = Icons.Outlined.TrendingUp,
                            backgroundColor = TealTile,
                            testTag = "stat_card_inflow"
                        )
                        // Outflow Card (Coral)
                        PlatformStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Outflow",
                            amount = if (totalOutflow > 0L) "UGX " + java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalOutflow) else "0",
                            icon = Icons.Outlined.TrendingDown,
                            backgroundColor = CoralTile,
                            testTag = "stat_card_outflow"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Net Balance Card (Indigo Purple)
                        PlatformStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Net Revenue",
                            amount = if (netBalance != 0L) "UGX " + java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(netBalance) else "0",
                            icon = Icons.Outlined.AccountBalanceWallet,
                            backgroundColor = IndigoPurple,
                            testTag = "stat_card_net_balance"
                        )
                        // Count Card (Amber / Navy)
                        PlatformStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Volume",
                            amount = "${allTransactions.size}",
                            icon = Icons.Outlined.Payments,
                            backgroundColor = NavyTile,
                            testTag = "stat_card_volume"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // SECTION 2: Category Filter Chips (Horizontal scrollable)
                item {
                    Text(
                        text = "Filter By Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlatformTransactionCategory.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                modifier = Modifier
                                    .shadow(
                                        elevation = if (isSelected) 3.dp else 0.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = IndigoPurple.copy(alpha = 0.25f)
                                    )
                                    .clickable { selectedCategory = category }
                                    .testTag("filter_chip_${category.name.lowercase()}"),
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) IndigoPurple else Color.White,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, BorderGrey) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Filter Sub-Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        // All Statuses
                        Surface(
                            modifier = Modifier
                                .clickable { selectedStatus = null }
                                .testTag("status_filter_all"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedStatus == null) Color(0xFF4B3F8F).copy(alpha = 0.15f) else Color.Transparent,
                            border = if (selectedStatus == null) androidx.compose.foundation.BorderStroke(1.dp, IndigoPurple) else null
                        ) {
                            Text(
                                text = "All",
                                fontSize = 11.sp,
                                fontWeight = if (selectedStatus == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStatus == null) IndigoPurple else TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Completed
                        Surface(
                            modifier = Modifier
                                .clickable { selectedStatus = PlatformTransactionStatus.COMPLETED }
                                .testTag("status_filter_completed"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedStatus == PlatformTransactionStatus.COMPLETED) GreenAccent.copy(alpha = 0.15f) else Color.Transparent,
                            border = if (selectedStatus == PlatformTransactionStatus.COMPLETED) androidx.compose.foundation.BorderStroke(1.dp, GreenAccent) else null
                        ) {
                            Text(
                                text = "Completed",
                                fontSize = 11.sp,
                                fontWeight = if (selectedStatus == PlatformTransactionStatus.COMPLETED) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStatus == PlatformTransactionStatus.COMPLETED) GreenAccent else TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Pending
                        Surface(
                            modifier = Modifier
                                .clickable { selectedStatus = PlatformTransactionStatus.PENDING }
                                .testTag("status_filter_pending"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedStatus == PlatformTransactionStatus.PENDING) AmberTile.copy(alpha = 0.15f) else Color.Transparent,
                            border = if (selectedStatus == PlatformTransactionStatus.PENDING) androidx.compose.foundation.BorderStroke(1.dp, AmberTile) else null
                        ) {
                            Text(
                                text = "Pending",
                                fontSize = 11.sp,
                                fontWeight = if (selectedStatus == PlatformTransactionStatus.PENDING) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStatus == PlatformTransactionStatus.PENDING) AmberTile else TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Performed Transactions (${filteredTransactions.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Tap card for details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // SECTION 3: Transaction Cards
                if (filteredTransactions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = LightLavender.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = "Empty",
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or filters.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        PlatformTransactionCard(
                            transaction = tx,
                            onClick = { selectedTransactionForDetail = tx }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // ==========================================
    // 3. TRANSACTION DETAIL BOTTOM SHEET MODAL
    // When a transaction card is clicked, shows comprehensive details
    // ==========================================
    selectedTransactionForDetail?.let { tx ->
        ModalBottomSheet(
            onDismissRequest = { selectedTransactionForDetail = null },
            sheetState = sheetState,
            containerColor = Color(0xFFFBFBFE),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("transaction_detail_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Top drag / header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        modifier = Modifier.testTag("detail_modal_title")
                    )

                    IconButton(
                        onClick = { selectedTransactionForDetail = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = IconGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Receipt Header Card
                val themeColor = when (tx.category) {
                    PlatformTransactionCategory.SUBSCRIPTION -> TealTile
                    PlatformTransactionCategory.TEACHER_ROYALTY -> BlueTile
                    PlatformTransactionCategory.TAX_OPS -> NavyTile
                    PlatformTransactionCategory.ALL -> IndigoPurple
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = themeColor.copy(alpha = 0.35f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = themeColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(themeColor, themeColor.copy(alpha = 0.85f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Category & Status pill
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = tx.category.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (tx.status == PlatformTransactionStatus.COMPLETED)
                                        Color(0xFF2E7D32).copy(alpha = 0.85f)
                                    else
                                        Color(0xFFE65100).copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = tx.status.displayName.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = tx.formattedAmount,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.testTag("detail_modal_amount")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = tx.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.95f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Breakdown Items
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Ref Number + Copy
                        DetailRowWithCopy(
                            label = "Reference ID",
                            value = tx.referenceNumber,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(tx.referenceNumber))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Reference copied: ${tx.referenceNumber}")
                                }
                            }
                        )

                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))

                        // User & Role
                        DetailRow(
                            label = "Participant / User",
                            value = "${tx.userName} (${tx.userRole})"
                        )

                        if (tx.studentClassOrSubject != null) {
                            HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))
                            DetailRow(
                                label = "Class / Subject",
                                value = tx.studentClassOrSubject
                            )
                        }

                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))

                        // Payment Gateway
                        DetailRow(
                            label = "Payment Gateway",
                            value = tx.paymentMethod
                        )

                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))

                        // Date & Time
                        DetailRow(
                            label = "Timestamp",
                            value = "${tx.date} at ${tx.time}"
                        )

                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))

                        // Processing Fee
                        DetailRow(
                            label = "Gateway Processing Fee",
                            value = tx.formattedFee
                        )

                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))

                        // Net Settled
                        DetailRow(
                            label = "Net Settled Amount",
                            value = tx.formattedNet,
                            isHighlighted = true
                        )

                        if (tx.notes.isNotBlank()) {
                            HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Description & Notes",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tx.notes,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("${tx.title} - ${tx.formattedAmount} (Ref: ${tx.referenceNumber})"))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Transaction receipt summary copied!")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, IndigoPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = IndigoPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Receipt",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }

                    Button(
                        onClick = { selectedTransactionForDetail = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndigoPurple
                        )
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

/**
 * Stat Card for the top summary grid.
 */
@Composable
private fun PlatformStatCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    icon: ImageVector,
    backgroundColor: Color,
    testTag: String
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = backgroundColor.copy(alpha = 0.30f),
                spotColor = Color(0x22000000)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.90f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Individual Transaction Card in the list.
 */
@Composable
private fun PlatformTransactionCard(
    modifier: Modifier = Modifier,
    transaction: AdminPlatformTransaction,
    onClick: () -> Unit
) {
    val categoryColor = when (transaction.category) {
        PlatformTransactionCategory.SUBSCRIPTION -> TealTile
        PlatformTransactionCategory.TEACHER_ROYALTY -> BlueTile
        PlatformTransactionCategory.TAX_OPS -> NavyTile
        PlatformTransactionCategory.ALL -> IndigoPurple
    }

    val icon = when (transaction.category) {
        PlatformTransactionCategory.SUBSCRIPTION -> Icons.Outlined.CreditCard
        PlatformTransactionCategory.TEACHER_ROYALTY -> Icons.Outlined.Description
        PlatformTransactionCategory.TAX_OPS -> Icons.Outlined.AccountBalance
        PlatformTransactionCategory.ALL -> Icons.Outlined.ReceiptLong
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x10000000),
                spotColor = Color(0x12000000)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("admin_transaction_card_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Category Icon Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = categoryColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.category.displayName,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle: Title, User, Gateway & Time
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${transaction.userName} • ${transaction.userRole}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF3F0FA)
                    ) {
                        Text(
                            text = transaction.id,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${transaction.date}, ${transaction.time}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Amount and Status Pill
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = transaction.formattedAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.isCredit) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (transaction.status == PlatformTransactionStatus.COMPLETED)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = transaction.status.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.status == PlatformTransactionStatus.COMPLETED)
                            Color(0xFF2E7D32)
                        else
                            Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = if (isHighlighted) 14.sp else 13.sp,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHighlighted) IndigoPurple else TextDark,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailRowWithCopy(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onCopy)
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = IndigoPurple
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy",
                tint = IndigoPurple,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
