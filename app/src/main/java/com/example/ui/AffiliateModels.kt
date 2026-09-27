package com.example.ui

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.TealTile
import java.text.NumberFormat
import java.util.Locale

/**
 * Account status for an affiliate partner.
 */
enum class AffiliateAccountStatus(val displayName: String) {
    ACTIVE("Active"),
    CANCELLED("Cancelled"),
    SUSPENDED("Suspended")
}

/**
 * Standard earnings rate per referral in Uganda Shillings.
 * Rule: For 1 person who uses the affiliate code, the owner gets 100 UGX.
 */
const val UGX_PER_REFERRAL = 100

/**
 * Data model for a registered affiliate user in the Catch Up system.
 */
data class AffiliateUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String = "Student", // "Student" or "Teacher"
    val referralCode: String,
    val referralCount: Int,
    val totalEarnedUgx: Int = referralCount * UGX_PER_REFERRAL,
    val currentBalanceUgx: Int,
    val status: AffiliateAccountStatus = AffiliateAccountStatus.ACTIVE,
    val joinedDate: String = "Sept 2026",
    val phone: String = "+256 772 450123",
    val avatarColor: Color = IndigoPurple
)

/**
 * Type of affiliate ledger transaction.
 */
enum class AffiliateTransactionType {
    REFERRAL_EARNING,
    WITHDRAWAL,
    PLAN_PAYMENT
}

/**
 * Data model for an individual transaction in the student's affiliate wallet.
 */
data class AffiliateTransaction(
    val id: String,
    val type: AffiliateTransactionType,
    val title: String,
    val description: String,
    val amountUgx: Int,
    val date: String,
    val status: String = "Completed"
) {
    val isCredit: Boolean
        get() = amountUgx > 0

    val formattedAmount: String
        get() = if (amountUgx >= 0) "+${formatUgx(amountUgx)}" else "-${formatUgx(-amountUgx)}"
}

/**
 * Helper to format UGX amounts nicely (e.g. "UGX 1,500").
 */
fun formatUgx(amount: Int): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
    return "UGX $formatted"
}

/**
 * Default list of users registered in the Affiliate Program for monitoring.
 */
val DefaultAffiliateUsersList = listOf(
    AffiliateUser(
        id = "aff_moses",
        name = "Moses",
        email = "moses.k@student.cachap.ug",
        role = "Student",
        referralCode = "MOSES100",
        referralCount = 142,
        totalEarnedUgx = 142 * UGX_PER_REFERRAL,
        currentBalanceUgx = 14200,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Jul 2026",
        phone = "+256 772 101010",
        avatarColor = TealTile
    ),
    AffiliateUser(
        id = "aff_mary",
        name = "Mary",
        email = "mary.n@student.cachap.ug",
        role = "Student",
        referralCode = "MARY256",
        referralCount = 118,
        totalEarnedUgx = 118 * UGX_PER_REFERRAL,
        currentBalanceUgx = 11800,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Aug 2026",
        phone = "+256 701 222333",
        avatarColor = CoralTile
    ),
    AffiliateUser(
        id = "aff_john",
        name = "John",
        email = "john.o@student.cachap.ug",
        role = "Student",
        referralCode = "JOHN99",
        referralCount = 95,
        totalEarnedUgx = 95 * UGX_PER_REFERRAL,
        currentBalanceUgx = 8500,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Jun 2026",
        phone = "+256 782 445566",
        avatarColor = NavyTile
    ),
    AffiliateUser(
        id = "aff_martin",
        name = "Martin",
        email = "martin.b@student.cachap.ug",
        role = "Student",
        referralCode = "MARTIN04",
        referralCount = 74,
        totalEarnedUgx = 74 * UGX_PER_REFERRAL,
        currentBalanceUgx = 4400,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Aug 2026",
        phone = "+256 752 998877",
        avatarColor = BlueTile
    ),
    AffiliateUser(
        id = "aff_may",
        name = "May",
        email = "may.t@student.cachap.ug",
        role = "Student",
        referralCode = "MAY77",
        referralCount = 62,
        totalEarnedUgx = 62 * UGX_PER_REFERRAL,
        currentBalanceUgx = 6200,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Sept 2026",
        phone = "+256 703 112233",
        avatarColor = PurpleTile
    ),
    AffiliateUser(
        id = "aff_ezekiel",
        name = "Ezekiel Joseph",
        email = "ezekiel.j@student.cachap.ug",
        role = "Student",
        referralCode = "EZEKIEL01",
        referralCount = 38,
        totalEarnedUgx = 38 * UGX_PER_REFERRAL,
        currentBalanceUgx = 2800,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Sept 2026",
        phone = "+256 774 334455",
        avatarColor = IndigoPurple
    ),
    AffiliateUser(
        id = "aff_sarah",
        name = "Sarah Seruwama",
        email = "sarah.s@student.cachap.ug",
        role = "Student",
        referralCode = "SARAH88",
        referralCount = 19,
        totalEarnedUgx = 19 * UGX_PER_REFERRAL,
        currentBalanceUgx = 900,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Sept 2026",
        phone = "+256 705 667788",
        avatarColor = AmberTile
    ),
    AffiliateUser(
        id = "aff_apollo",
        name = "Apollo Kenh",
        email = "apollo.k@student.cachap.ug",
        role = "Student",
        referralCode = "APOLLO20",
        referralCount = 12,
        totalEarnedUgx = 12 * UGX_PER_REFERRAL,
        currentBalanceUgx = 1200,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "Aug 2026",
        phone = "+256 781 990011",
        avatarColor = TealTile
    ),
    AffiliateUser(
        id = "aff_denis",
        name = "Denis Mukasa",
        email = "denis.m@student.cachap.ug",
        role = "Student",
        referralCode = "DENIS99",
        referralCount = 4,
        totalEarnedUgx = 4 * UGX_PER_REFERRAL,
        currentBalanceUgx = 400,
        status = AffiliateAccountStatus.ACTIVE,
        joinedDate = "May 2026",
        phone = "+256 771 889900",
        avatarColor = CoralTile
    )
)

/**
 * Default sample transactions for the student's affiliate wallet.
 */
val DefaultStudentAffiliateTransactions = listOf(
    AffiliateTransaction(
        id = "tx_1",
        type = AffiliateTransactionType.REFERRAL_EARNING,
        title = "Referral Bonus",
        description = "Referral reward from Brenda A. via STUDENT100",
        amountUgx = 100,
        date = "22 Sept 2026",
        status = "Completed"
    ),
    AffiliateTransaction(
        id = "tx_2",
        type = AffiliateTransactionType.REFERRAL_EARNING,
        title = "Referral Bonus",
        description = "Referral reward from Samuel K. via STUDENT100",
        amountUgx = 100,
        date = "21 Sept 2026",
        status = "Completed"
    ),
    AffiliateTransaction(
        id = "tx_3",
        type = AffiliateTransactionType.REFERRAL_EARNING,
        title = "Referral Bonus",
        description = "Referral reward from David O. via STUDENT100",
        amountUgx = 100,
        date = "20 Sept 2026",
        status = "Completed"
    ),
    AffiliateTransaction(
        id = "tx_4",
        type = AffiliateTransactionType.PLAN_PAYMENT,
        title = "Plan Purchase",
        description = "Paid for 1 Month Pass via Affiliate Balance",
        amountUgx = -1000,
        date = "18 Sept 2026",
        status = "Completed"
    ),
    AffiliateTransaction(
        id = "tx_5",
        type = AffiliateTransactionType.WITHDRAWAL,
        title = "Mobile Money Payout",
        description = "MTN Mobile Money withdrawal to 0772***456",
        amountUgx = -500,
        date = "15 Sept 2026",
        status = "Completed"
    ),
    AffiliateTransaction(
        id = "tx_6",
        type = AffiliateTransactionType.REFERRAL_EARNING,
        title = "Affiliate Welcome Bonus",
        description = "Program enrollment initial bonus",
        amountUgx = 500,
        date = "10 Sept 2026",
        status = "Completed"
    )
)
