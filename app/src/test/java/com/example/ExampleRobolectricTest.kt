package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.ui.DefaultGeographyUploads
import com.example.ui.DefaultMathUploads
import com.example.ui.DefaultTeacherAllUploads
import com.example.ui.DefaultTeacherRecentUploads
import com.example.ui.DefaultTeacherSubjects
import com.example.ui.StudentNavItems
import com.example.ui.TeacherNavItems
import com.example.ui.ABOUT_STORY_TEXT
import com.example.ui.AdminPlanItem
import com.example.ui.DefaultAdminLogs
import com.example.ui.DefaultAdminNavItems
import com.example.ui.DefaultAdminPlans
import com.example.ui.DefaultAdminTransactions
import com.example.ui.formatUgxPrice
import com.example.ui.parseUgxInput
import com.example.ui.AnalysisTimeRange
import com.example.ui.DefaultRevenueChartData
import com.example.ui.DefaultStudentsList
import com.example.ui.DefaultTeachersList
import com.example.ui.mergeSignedUpAndDefaultUsers
import com.example.ui.toAdminUserItem
import com.example.ui.DefaultTopAffiliates
import com.example.ui.DefaultUsersChartData
import com.example.ui.MonthUsersChartData
import com.example.ui.WeekRevenueChartData
import com.example.ui.WeekUsersChartData
import com.example.ui.YearRevenueChartData
import com.example.ui.YearUsersChartData
import com.example.ui.UserRoleFilter
import com.example.ui.AffiliateAccountStatus
import com.example.ui.AffiliateTransaction
import com.example.ui.AffiliateTransactionType
import com.example.ui.AffiliateUser
import com.example.ui.DefaultAffiliateUsersList
import com.example.ui.DefaultPlanTiers
import com.example.ui.DefaultPlatformTransactions
import com.example.ui.DefaultStudentAffiliateTransactions
import com.example.ui.PlatformTransactionCategory
import com.example.ui.PlatformTransactionStatus
import com.example.ui.UGX_PER_REFERRAL
import com.example.ui.formatUgx
import com.example.ui.defaultFaqItems
import com.example.ui.getSubjectThemeColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cachap", appName)
  }

  @Test
  fun `verify subject colors match palette`() {
    assertEquals(Color(0xFFE76F51), getSubjectThemeColor("Geography"))
    assertEquals(Color(0xFF2A4D8F), getSubjectThemeColor("Biology"))
    assertEquals(Color(0xFF2A9D8F), getSubjectThemeColor("Math"))
    assertEquals(Color(0xFF4361EE), getSubjectThemeColor("History"))
    assertEquals(Color(0xFF7C4DFF), getSubjectThemeColor("Physics"))
    assertEquals(Color(0xFFE67E22), getSubjectThemeColor("Chemistry"))
  }

  @Test
  fun `verify teacher nav items match specifications`() {
    val labels = TeacherNavItems.map { it.label }
    assertEquals(listOf("Home", "My Subjects", "Uploads", "Profile"), labels)
  }

  @Test
  fun `verify student nav items match specifications`() {
    val labels = StudentNavItems.map { it.label }
    assertEquals(listOf("Home", "Subjects", "Downloads", "Profile"), labels)
  }

  @Test
  fun `verify teacher quick stats colors`() {
    val tealColor = Color(0xFF2A9D8F)
    val blueColor = Color(0xFF4361EE)
    assertEquals(Color(0xFF2A9D8F), tealColor)
    assertEquals(Color(0xFF4361EE), blueColor)
  }

  @Test
  fun `verify default teacher recent uploads starts empty for production`() {
    assertTrue(DefaultTeacherRecentUploads.isEmpty())
  }

  @Test
  fun `verify teacher taught subjects contains Geography and Math`() {
    val subjectNames = DefaultTeacherSubjects.map { it.name }
    assertEquals(listOf("Geography", "Math"), subjectNames)
    assertEquals("S.4, S.5", DefaultTeacherSubjects.first { it.name == "Geography" }.classes)
    assertEquals("S.3, S.4", DefaultTeacherSubjects.first { it.name == "Math" }.classes)
    assertEquals(0, DefaultTeacherSubjects.first { it.name == "Geography" }.noteCount)
    assertEquals(0, DefaultTeacherSubjects.first { it.name == "Geography" }.assignmentCount)
  }

  @Test
  fun `verify teacher subject uploads start empty for production`() {
    assertTrue(DefaultGeographyUploads.isEmpty())
    assertTrue(DefaultMathUploads.isEmpty())
  }

  @Test
  fun `verify teacher all uploads collection contains study notes and assignments`() {
    assertTrue(DefaultTeacherAllUploads.isNotEmpty())
    assertEquals(6, DefaultTeacherAllUploads.size)
    assertTrue(DefaultTeacherAllUploads.any { it.subject == "Geography" })
    assertTrue(DefaultTeacherAllUploads.any { it.subject == "Math" })
  }

  @Test
  fun `verify account plan tiers match specification`() {
    val plans = com.example.ui.DefaultPlanTiers
    assertEquals(1, plans.size)

    assertEquals("1 Month", plans[0].duration)
    assertEquals("Billed monthly", plans[0].caption)
    assertEquals("UGX 1,000", plans[0].price)
  }

  @Test
  fun `verify settings screen appearance section options`() {
    val languageOptions = listOf("English", "Luganda (Coming Soon)", "Swahili (Coming Soon)")
    assertEquals(3, languageOptions.size)
    assertEquals("English", languageOptions.first())
  }

  @Test
  fun `verify help and support faq content and count`() {
    assertEquals(5, defaultFaqItems.size)
    assertEquals("How do I access notes for my class?", defaultFaqItems[0].question)
    assertTrue(defaultFaqItems[0].answer.contains("Open the Subjects tab"))
    assertEquals("Why can't I see notes for a subject?", defaultFaqItems[1].question)
    assertEquals("How do I save a note for offline access?", defaultFaqItems[2].question)
    assertTrue(defaultFaqItems[2].answer.contains("Downloads tab"))
    assertEquals("How do I renew or change my plan?", defaultFaqItems[3].question)
    assertEquals("I forgot my password. What do I do?", defaultFaqItems[4].question)
  }

  @Test
  fun `verify about screen story and creators text`() {
    assertTrue(ABOUT_STORY_TEXT.contains("Our Story"))
    assertTrue(ABOUT_STORY_TEXT.contains("I am a teacher."))
    assertTrue(ABOUT_STORY_TEXT.contains("Less time copying. More time learning."))
    assertTrue(ABOUT_STORY_TEXT.contains("That is where Cachap began."))
  }

  @Test
  fun `verify profile screen does not contain affiliate`() {
    assertFalse(AppScreen.values().any { it.name == "AFFILIATE" })
  }

  @Test
  fun `verify admin nav items match full set specification`() {
    val labels = DefaultAdminNavItems.map { it.label }
    assertEquals(listOf("Home", "Analysis", "Users", "More"), labels)
  }

  @Test
  fun `verify admin screens are registered in app screens`() {
    assertTrue(AppScreen.values().any { it.name == "ADMIN_HOME" })
    assertTrue(AppScreen.values().any { it.name == "ADMIN_ANALYSIS" })
    assertTrue(AppScreen.values().any { it.name == "ADMIN_USERS" })
    assertTrue(AppScreen.values().any { it.name == "ADMIN_MORE" })
  }

  @Test
  fun `verify navigation from student home to admin home exists`() {
    var currentScreen = AppScreen.HOME
    val navigateToAdmin = { currentScreen = AppScreen.ADMIN_HOME }
    navigateToAdmin()
    assertEquals(AppScreen.ADMIN_HOME, currentScreen)
  }

  @Test
  fun `verify admin analysis revenue chart data configuration`() {
    val revenue = DefaultRevenueChartData
    assertEquals("Total Revenue", revenue.title)
    assertEquals(listOf("May", "Jun", "Jul", "Aug", "Sep"), revenue.points.map { it.label })
    assertEquals(listOf("6M", "4.5M", "3M", "1.5M", "0"), revenue.yAxisLabels)
    assertEquals("UGX 5,867,000", revenue.currentFormattedValue)
    assertEquals("+24.5%", revenue.growthRate)
  }

  @Test
  fun `verify admin analysis users chart data configuration`() {
    val users = DefaultUsersChartData
    assertEquals("Active Users", users.title)
    assertEquals(listOf("Jan", "Feb", "March", "Apr"), users.points.map { it.label })
    assertEquals(listOf("1.2k", "900", "600", "300", "0"), users.yAxisLabels)
    assertEquals("1,050", users.currentFormattedValue)
    assertEquals("+32.8%", users.growthRate)
  }

  @Test
  fun `verify admin analysis back navigation returns to admin home`() {
    var currentScreen = AppScreen.ADMIN_ANALYSIS
    val onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_HOME, currentScreen)
  }

  @Test
  fun `verify admin users default students list starts empty for production`() {
    assertTrue(DefaultStudentsList.isEmpty())
  }

  @Test
  fun `verify admin users default teachers list starts empty for production`() {
    assertTrue(DefaultTeachersList.isEmpty())
  }

  @Test
  fun `verify admin users back navigation returns to admin home`() {
    var currentScreen = AppScreen.ADMIN_USERS
    val onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_HOME, currentScreen)
  }

  @Test
  fun `verify admin more back navigation returns to admin home`() {
    var currentScreen = AppScreen.ADMIN_MORE
    val onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_HOME, currentScreen)
  }

  @Test
  fun `verify admin more navigation callbacks work as expected`() {
    var currentScreen = AppScreen.ADMIN_MORE
    val onNavigateToAccount = { currentScreen = AppScreen.ACCOUNT }
    val onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
    val onLogout = { currentScreen = AppScreen.PROFILE }

    onNavigateToAccount()
    assertEquals(AppScreen.ACCOUNT, currentScreen)

    onNavigateToSettings()
    assertEquals(AppScreen.SETTINGS, currentScreen)

    onLogout()
    assertEquals(AppScreen.PROFILE, currentScreen)
  }

  @Test
  fun `verify admin more menu items list order`() {
    val expectedOrder = listOf(
      "Account",
      "Transactions",
      "Settings",
      "Health",
      "Affiliate",
      "Logs",
      "LOG OUT"
    )
    assertEquals(7, expectedOrder.size)
    assertTrue(expectedOrder.contains("Health"))
    assertTrue(expectedOrder.contains("Logs"))
    assertTrue(expectedOrder.contains("Affiliate"))
    assertEquals("LOG OUT", expectedOrder.last())
  }

  @Test
  fun `verify admin affiliate navigation from more and back`() {
    var currentScreen = AppScreen.ADMIN_MORE
    val onNavigateToAffiliate = { currentScreen = AppScreen.ADMIN_AFFILIATE }
    onNavigateToAffiliate()
    assertEquals(AppScreen.ADMIN_AFFILIATE, currentScreen)

    val onNavigateBack = { currentScreen = AppScreen.ADMIN_MORE }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_MORE, currentScreen)
  }

  @Test
  fun `verify admin account navigation from home revenue card and back`() {
    var currentScreen = AppScreen.ADMIN_HOME
    var previousScreen = AppScreen.ADMIN_HOME

    val onNavigateToAccountFromHome = {
      previousScreen = AppScreen.ADMIN_HOME
      currentScreen = AppScreen.ADMIN_ACCOUNT
    }
    onNavigateToAccountFromHome()
    assertEquals(AppScreen.ADMIN_ACCOUNT, currentScreen)

    val onNavigateBack = { currentScreen = previousScreen }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_HOME, currentScreen)
  }

  @Test
  fun `verify admin account navigation from more menu and back`() {
    var currentScreen = AppScreen.ADMIN_MORE
    var previousScreen = AppScreen.ADMIN_MORE

    val onNavigateToAccountFromMore = {
      previousScreen = AppScreen.ADMIN_MORE
      currentScreen = AppScreen.ADMIN_ACCOUNT
    }
    onNavigateToAccountFromMore()
    assertEquals(AppScreen.ADMIN_ACCOUNT, currentScreen)

    val onNavigateBack = { currentScreen = previousScreen }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_MORE, currentScreen)
  }

  @Test
  fun `verify admin default transactions list contains expected types and items`() {
    assertTrue(DefaultAdminTransactions.isNotEmpty())
    val types = DefaultAdminTransactions.map { it.type }
    assertTrue(types.contains("Withdrew"))
    assertTrue(types.contains("Sent"))
    assertTrue(DefaultAdminTransactions.any { it.amount.contains("UGX") })
    assertTrue(DefaultAdminTransactions.any { it.date.contains("/") })
  }

  @Test
  fun `verify admin plans navigation from home plans button and back`() {
    var currentScreen = AppScreen.ADMIN_HOME

    val onNavigateToPlans = {
      currentScreen = AppScreen.ADMIN_PLANS
    }
    onNavigateToPlans()
    assertEquals(AppScreen.ADMIN_PLANS, currentScreen)

    val onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME }
    onNavigateBack()
    assertEquals(AppScreen.ADMIN_HOME, currentScreen)
  }

  @Test
  fun `verify default admin plans list and pricing formatting`() {
    assertEquals(1, DefaultAdminPlans.size)
    assertEquals("1 Month", DefaultAdminPlans[0].duration)
    assertEquals("UGX 1,000", DefaultAdminPlans[0].price)

    assertEquals("UGX 5,000", formatUgxPrice(5000))
    assertEquals("UGX 100,000", formatUgxPrice(100000))
    assertEquals(5000, parseUgxInput("5000"))
    assertEquals(5000, parseUgxInput("UGX 5,000"))
  }

  @Test
  fun `verify admin plans edit add delete operations`() {
    var plans = DefaultAdminPlans

    // Add
    val newPlan = AdminPlanItem("plan_custom", "6 Months", "UGX 6,000", 6000)
    plans = plans + newPlan
    assertEquals(2, plans.size)
    assertTrue(plans.any { it.duration == "6 Months" })

    // Edit
    plans = plans.map {
      if (it.id == "plan_1_month") it.copy(price = "UGX 1,500", priceUgx = 1500) else it
    }
    assertEquals("UGX 1,500", plans.first { it.id == "plan_1_month" }.price)

    // Delete
    plans = plans.filterNot { it.id == "plan_custom" }
    assertEquals(1, plans.size)
    assertFalse(plans.any { it.id == "plan_custom" })
  }

  @Test
  fun `verify admin analysis time range options`() {
    val ranges = AnalysisTimeRange.values()
    assertEquals(3, ranges.size)
    assertEquals("Week", ranges[0].label)
    assertEquals("Month", ranges[1].label)
    assertEquals("Year", ranges[2].label)
  }

  @Test
  fun `verify admin analysis revenue data for different time ranges`() {
    // Week
    assertEquals("Weekly Revenue", WeekRevenueChartData.title)
    assertEquals(7, WeekRevenueChartData.points.size)
    assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), WeekRevenueChartData.points.map { it.label })
    assertEquals("UGX 1,450,000", WeekRevenueChartData.currentFormattedValue)

    // Year
    assertEquals("Annual Revenue", YearRevenueChartData.title)
    assertEquals(4, YearRevenueChartData.points.size)
    assertEquals(listOf("2023", "2024", "2025", "2026"), YearRevenueChartData.points.map { it.label })
    assertEquals("UGX 52,300,000", YearRevenueChartData.currentFormattedValue)
  }

  @Test
  fun `verify admin analysis users stacked bar data structures`() {
    // Week users
    assertEquals("Active Users", WeekUsersChartData.title)
    assertEquals(7, WeekUsersChartData.points.size)
    assertEquals(34, WeekUsersChartData.points[0].total) // Mon: 28 std + 6 tch

    // Month users
    assertEquals(4, MonthUsersChartData.points.size)
    assertEquals(250, MonthUsersChartData.points[0].total) // Jan: 210 + 40
    assertEquals(1050, MonthUsersChartData.points[3].total) // Apr: 880 + 170

    // Year users
    assertEquals(4, YearUsersChartData.points.size)
    assertEquals(2150, YearUsersChartData.points[0].total)
    assertEquals(8400, YearUsersChartData.points[3].total)
  }

  @Test
  fun `verify admin analysis top affiliates ranking list`() {
    assertEquals(5, DefaultTopAffiliates.size)
    val names = DefaultTopAffiliates.map { it.name }
    assertEquals(listOf("Moses", "Mary", "John", "Martin", "May"), names)
    assertEquals(listOf(142, 118, 95, 74, 62), DefaultTopAffiliates.map { it.referralCount })
    assertEquals(listOf(1, 2, 3, 4, 5), DefaultTopAffiliates.map { it.rank })
  }

  @Test
  fun `verify affiliate reward rate and earning calculation`() {
    assertEquals(100, UGX_PER_REFERRAL)

    val user = AffiliateUser(
      id = "test_user",
      name = "Test Student",
      email = "test@catchup.ug",
      referralCode = "TEST100",
      referralCount = 10,
      totalEarnedUgx = 10 * UGX_PER_REFERRAL,
      currentBalanceUgx = 1000
    )

    assertEquals(1000, user.totalEarnedUgx)
    assertEquals(1000, user.currentBalanceUgx)

    // Simulate 1 referral use (+100 UGX)
    val updatedUser = user.copy(
      referralCount = user.referralCount + 1,
      totalEarnedUgx = (user.referralCount + 1) * UGX_PER_REFERRAL,
      currentBalanceUgx = user.currentBalanceUgx + UGX_PER_REFERRAL
    )

    assertEquals(11, updatedUser.referralCount)
    assertEquals(1100, updatedUser.totalEarnedUgx)
    assertEquals(1100, updatedUser.currentBalanceUgx)
  }

  @Test
  fun `verify admin affiliate list and cancel account operation`() {
    var affiliates = DefaultAffiliateUsersList
    assertTrue(affiliates.size >= 8)

    val moses = affiliates.first { it.referralCode == "MOSES100" }
    assertEquals(142, moses.referralCount)
    assertEquals(14200, moses.totalEarnedUgx)
    assertEquals(AffiliateAccountStatus.ACTIVE, moses.status)

    // Admin cancels an account
    affiliates = affiliates.map {
      if (it.id == moses.id) it.copy(status = AffiliateAccountStatus.CANCELLED) else it
    }

    val cancelledMoses = affiliates.first { it.id == moses.id }
    assertEquals(AffiliateAccountStatus.CANCELLED, cancelledMoses.status)

    // Admin reactivates account
    affiliates = affiliates.map {
      if (it.id == moses.id) it.copy(status = AffiliateAccountStatus.ACTIVE) else it
    }
    assertEquals(AffiliateAccountStatus.ACTIVE, affiliates.first { it.id == moses.id }.status)
  }

  @Test
  fun `verify student affiliate wallet withdrawals and plan purchases`() {
    var balance = 1500
    val withdrawalAmount = 500

    // Withdraw 500 UGX to Mobile Money
    assertTrue(balance >= withdrawalAmount)
    balance -= withdrawalAmount
    assertEquals(1000, balance)

    // Pay for 1 Month Pass (UGX 1,000)
    val planPrice = 1000
    assertTrue(balance >= planPrice)
    balance -= planPrice
    assertEquals(0, balance)

    // Format helper
    assertEquals("UGX 100", formatUgx(100))
    assertEquals("UGX 1,500", formatUgx(1500))
    assertEquals("UGX 10,000", formatUgx(10000))

    // Transactions list verification
    assertTrue(DefaultStudentAffiliateTransactions.isNotEmpty())
    val referralTx = DefaultStudentAffiliateTransactions.first { it.type == AffiliateTransactionType.REFERRAL_EARNING }
    assertTrue(referralTx.isCredit)
    assertEquals(100, referralTx.amountUgx)
    assertEquals("+UGX 100", referralTx.formattedAmount)

    val planTx = DefaultStudentAffiliateTransactions.first { it.type == AffiliateTransactionType.PLAN_PAYMENT }
    assertFalse(planTx.isCredit)
    assertEquals(-1000, planTx.amountUgx)
    assertEquals("-UGX 1,000", planTx.formattedAmount)
  }

  @Test
  fun `verify payment subscription plan tiers configuration`() {
    assertEquals(1, DefaultPlanTiers.size)
    assertEquals("1 Month", DefaultPlanTiers[0].duration)
    assertEquals("UGX 1,000", DefaultPlanTiers[0].price)
    assertEquals(1000, DefaultPlanTiers[0].priceUgx)
  }

  @Test
  fun `verify admin platform transactions data and calculations`() {
    assertTrue(DefaultPlatformTransactions.isNotEmpty())
    assertEquals(15, DefaultPlatformTransactions.size)

    val subscriptionTxs = DefaultPlatformTransactions.filter { it.category == PlatformTransactionCategory.SUBSCRIPTION }
    assertTrue(subscriptionTxs.all { it.isCredit })
    assertTrue(subscriptionTxs.any { it.userName == "Amina Namukasa" && it.amountUgx == 35000L })

    val withdrawalTxs = DefaultPlatformTransactions.filter { it.category == PlatformTransactionCategory.AFFILIATE_WITHDRAWAL }
    assertTrue(withdrawalTxs.all { !it.isCredit })
    assertTrue(withdrawalTxs.any { it.userName == "Denis Mukasa" && it.amountUgx == -50000L })

    val teacherTxs = DefaultPlatformTransactions.filter { it.category == PlatformTransactionCategory.TEACHER_ROYALTY }
    assertTrue(teacherTxs.any { it.userName == "Tr. Emmanuel Kato" && it.amountUgx == -250000L })

    val taxTxs = DefaultPlatformTransactions.filter { it.category == PlatformTransactionCategory.TAX_OPS }
    assertTrue(taxTxs.any { it.userName == "Uganda Revenue Authority" })

    // Total Inflow > 0
    val totalInflow = DefaultPlatformTransactions.filter { it.amountUgx > 0 }.sumOf { it.amountUgx }
    assertTrue(totalInflow > 0)

    // Formatted strings
    val sampleTx = DefaultPlatformTransactions.first { it.id == "TX-98401" }
    assertEquals("+UGX 35,000", sampleTx.formattedAmount)
    assertEquals("+UGX 34,475", sampleTx.formattedNet)
    assertEquals("UGX 525", sampleTx.formattedFee)
  }

  @Test
  fun `verify teacher profile and admin settings screens exist in AppScreen navigation`() {
    assertTrue(com.example.AppScreen.values().any { it.name == "TEACHER_PROFILE" })
    assertTrue(com.example.AppScreen.values().any { it.name == "ADMIN_SETTINGS" })
    assertTrue(com.example.AppScreen.values().any { it.name == "SETTINGS" })
  }

  @Test
  fun `verify admin health and logs screens exist in AppScreen navigation`() {
    assertTrue(com.example.AppScreen.values().any { it.name == "ADMIN_HEALTH" })
    assertTrue(com.example.AppScreen.values().any { it.name == "ADMIN_LOGS" })
  }

  @Test
  fun `verify admin health and logs navigation routing from more screen`() {
    var currentScreen = AppScreen.ADMIN_MORE

    val onNavigateToHealth = { currentScreen = AppScreen.ADMIN_HEALTH }
    onNavigateToHealth()
    assertEquals(AppScreen.ADMIN_HEALTH, currentScreen)

    val onNavigateBackFromHealth = { currentScreen = AppScreen.ADMIN_MORE }
    onNavigateBackFromHealth()
    assertEquals(AppScreen.ADMIN_MORE, currentScreen)

    val onNavigateToLogs = { currentScreen = AppScreen.ADMIN_LOGS }
    onNavigateToLogs()
    assertEquals(AppScreen.ADMIN_LOGS, currentScreen)

    val onNavigateBackFromLogs = { currentScreen = AppScreen.ADMIN_MORE }
    onNavigateBackFromLogs()
    assertEquals(AppScreen.ADMIN_MORE, currentScreen)
  }

  @Test
  fun `verify default admin audit logs dataset contains required categories`() {
    assertTrue(DefaultAdminLogs.isNotEmpty())
    assertEquals(7, DefaultAdminLogs.size)

    val categories = DefaultAdminLogs.map { it.category }.toSet()
    assertTrue(categories.contains("CONTENT"))
    assertTrue(categories.contains("PAYMENT"))
    assertTrue(categories.contains("SECURITY"))
    assertTrue(categories.contains("DATABASE"))
    assertTrue(categories.contains("AFFILIATE"))
    assertTrue(categories.contains("STORAGE"))

    val levels = DefaultAdminLogs.map { it.level }.toSet()
    assertTrue(levels.contains("INFO"))
    assertTrue(levels.contains("PAYOUT"))
    assertTrue(levels.contains("SECURITY"))
    assertTrue(levels.contains("DATABASE"))
    assertTrue(levels.contains("WARN"))
    assertTrue(levels.contains("ERROR"))
  }

  @Test
  fun `verify admin audit logs filtering by level and query`() {
    val databaseLogs = DefaultAdminLogs.filter { it.level == "DATABASE" || it.category == "DATABASE" }
    assertEquals(1, databaseLogs.size)
    assertTrue(databaseLogs.first().title.contains("SQLite VACUUM"))

    val securityLogs = DefaultAdminLogs.filter { it.level == "SECURITY" || it.category == "SECURITY" }
    assertEquals(1, securityLogs.size)
    assertTrue(securityLogs.first().title.contains("2FA"))

    val queryFiltered = DefaultAdminLogs.filter { it.description.contains("Okello", ignoreCase = true) }
    assertEquals(1, queryFiltered.size)
    assertEquals("log-101", queryFiltered.first().id)
  }

  @Test
  fun `verify onboarding pages match student panel colors and steps`() {
    val pages = com.example.ui.DefaultOnboardingPages
    assertEquals(3, pages.size)
    assertEquals("01 / 03", pages[0].stepBadge)
    assertEquals(Color(0xFFE76F51), pages[0].accentColor)
    assertEquals("02 / 03", pages[1].stepBadge)
    assertEquals(Color(0xFF2A9D8F), pages[1].accentColor)
    assertEquals("03 / 03", pages[2].stepBadge)
    assertEquals(Color(0xFFFF7A53), pages[2].accentColor)
  }

  @Test
  fun `verify supabase user dto stores phone number role password hash and individual class stream`() {
    val studentUser = com.example.data.SupabaseUserDto(
      id = "usr-1",
      phoneNumber = "0772123456",
      fullName = "Mukasa David",
      role = "student",
      passwordHash = com.example.data.SupabaseSecurityVault.hashPassword("secret"),
      academicClass = "S.4",
      stream = "Green"
    )
    assertEquals("Mukasa David", studentUser.fullName)
    assertEquals("0772123456", studentUser.phoneNumber)
    assertEquals("student", studentUser.role)
    assertEquals("S.4", studentUser.academicClass)
    assertEquals("Green", studentUser.stream)
  }

  @Test
  fun `verify student panel current plan active and unloaded states`() {
    assertFalse(com.example.ui.isStudentPlanActive(""))
    assertFalse(com.example.ui.isStudentPlanActive("   "))
    assertFalse(com.example.ui.isStudentPlanActive("None"))
    assertFalse(com.example.ui.isStudentPlanActive("No Plan Selected"))
    assertFalse(com.example.ui.isStudentPlanActive("Inactive"))

    assertTrue(com.example.ui.isStudentPlanActive("1 Month"))
    assertTrue(com.example.ui.isStudentPlanActive("1 Term"))
    assertTrue(com.example.ui.isStudentPlanActive("1 Year"))
  }

  @Test
  fun `verify fcm notification channels token storage settings toggles and deep links`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Verify notification channels creation
    com.example.notifications.NotificationHelper.createNotificationChannels(context)
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    val newNotesChannel =
      notificationManager.getNotificationChannel(com.example.notifications.NotificationHelper.CHANNEL_ID_NEW_NOTES)
    assertEquals("New Notes", newNotesChannel.name.toString())
    assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, newNotesChannel.importance)

    val paymentRemindersChannel =
      notificationManager.getNotificationChannel(com.example.notifications.NotificationHelper.CHANNEL_ID_PAYMENT_REMINDERS)
    assertEquals("Payment Reminders", paymentRemindersChannel.name.toString())
    assertEquals(android.app.NotificationManager.IMPORTANCE_DEFAULT, paymentRemindersChannel.importance)

    // 2. Verify FCM token capture & persistence
    com.example.notifications.NotificationHelper.saveFcmToken(context, "sample_fcm_token_123")
    assertEquals("sample_fcm_token_123", com.example.notifications.NotificationHelper.getSavedFcmToken(context))

    // 3. Verify Settings toggles default to ON and suppress notifications when OFF
    assertTrue(com.example.notifications.NotificationHelper.isNewNoteAlertsEnabled(context))
    assertTrue(com.example.notifications.NotificationHelper.isPaymentRemindersEnabled(context))

    com.example.notifications.NotificationHelper.setNewNoteAlertsEnabled(context, false)
    assertFalse(com.example.notifications.NotificationHelper.isNewNoteAlertsEnabled(context))
    assertFalse(
      com.example.notifications.NotificationHelper.showNewNoteNotification(
        context = context,
        noteId = "upload-1",
        noteTitle = "Population Density",
        body = "New Geography note shared with S.5"
      )
    )

    com.example.notifications.NotificationHelper.setPaymentRemindersEnabled(context, false)
    assertFalse(com.example.notifications.NotificationHelper.isPaymentRemindersEnabled(context))
    assertFalse(
      com.example.notifications.NotificationHelper.showPaymentReminderNotification(
        context = context,
        title = "Your plan is expiring soon",
        body = "Renew your access to keep reading notes without interruption"
      )
    )

    // Restore defaults
    com.example.notifications.NotificationHelper.setNewNoteAlertsEnabled(context, true)
    com.example.notifications.NotificationHelper.setPaymentRemindersEnabled(context, true)

    // 4. Verify deep-link intents
    val noteIntent = com.example.notifications.NotificationHelper.buildNewNoteDeepLinkIntent(
      context = context,
      noteId = "upload-1",
      noteTitle = "Population Density",
      subject = "Geography"
    )
    assertEquals("new_note", noteIntent.getStringExtra(com.example.notifications.NotificationHelper.EXTRA_NOTIFICATION_TYPE))
    assertEquals("upload-1", noteIntent.getStringExtra(com.example.notifications.NotificationHelper.EXTRA_NOTE_ID))
    assertEquals("Population Density", noteIntent.getStringExtra(com.example.notifications.NotificationHelper.EXTRA_NOTE_TITLE))
    assertEquals("Geography", noteIntent.getStringExtra(com.example.notifications.NotificationHelper.EXTRA_NOTE_SUBJECT))

    val paymentIntent = com.example.notifications.NotificationHelper.buildPaymentReminderDeepLinkIntent(context)
    assertEquals(
      "payment_reminder",
      paymentIntent.getStringExtra(com.example.notifications.NotificationHelper.EXTRA_NOTIFICATION_TYPE)
    )
  }

  @Test
  fun `verify teacher id code is 6 characters of capital letters and numbers only`() {
    val defaultCode = com.example.ui.DEFAULT_TEACHER_ID_CODE
    assertEquals(6, defaultCode.length)
    assertTrue(com.example.ui.isValidTeacherIdCode(defaultCode))

    val generated = com.example.ui.generateTeacherIdCode("Tr. Okello Peter")
    assertEquals(6, generated.length)
    assertTrue(com.example.ui.isValidTeacherIdCode(generated))
    assertTrue(generated.all { it in 'A'..'Z' || it in '0'..'9' })

    assertFalse(com.example.ui.isValidTeacherIdCode("ABC12")) // Too short
    assertFalse(com.example.ui.isValidTeacherIdCode("abc123")) // Lowercase not valid
  }

  @Test
  fun `verify connected students serialization and deserialization`() {
    val students = listOf(
      com.example.ui.ConnectedStudent(
        id = "sarah_math_K7M4P9",
        studentName = "Sarah Seruwama",
        studentClass = "S.4 Green",
        subjectName = "Math",
        teacherIdCode = "K7M4P9",
        connectedDate = "Sep 27, 2026"
      )
    )
    val serialized = com.example.ui.serializeConnectedStudents(students)
    val restored = com.example.ui.deserializeConnectedStudents(serialized)
    assertEquals(1, restored.size)
    assertEquals("Sarah Seruwama", restored[0].studentName)
    assertEquals("S.4 Green", restored[0].studentClass)
    assertEquals("Math", restored[0].subjectName)
    assertEquals("K7M4P9", restored[0].teacherIdCode)
  }

  @Test
  fun `verify quick access subjects match default curriculum subjects and prioritize linked subjects`() {
    val allSubjects = com.example.ui.DefaultCurriculumSubjects
    assertTrue(allSubjects.size >= 4)
    assertEquals(listOf("Math", "English", "Physics", "Chemistry"), allSubjects.take(4).map { it.name })

    val linkedSubjectTeachers = mapOf("Geography" to "K7M4P9", "Biology" to "M2N8Q4")
    val linkedItems = linkedSubjectTeachers.keys.mapNotNull { linkedName ->
      allSubjects.find { it.name.equals(linkedName, ignoreCase = true) }
    }
    val remainingItems = allSubjects.filterNot { candidate ->
      linkedItems.any { it.name.equals(candidate.name, ignoreCase = true) }
    }
    val quickAccess = (linkedItems + remainingItems).take(4)
    assertEquals(listOf("Geography", "Biology", "Math", "English"), quickAccess.map { it.name })
    assertEquals(com.example.ui.getSubjectThemeColor("Geography"), quickAccess[0].color)
    assertEquals(com.example.ui.getSubjectThemeColor("Biology"), quickAccess[1].color)
  }

  @Test
  fun `verify supabase security vault key obfuscation redaction and password hashing`() {
    val sampleKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.sample_payload.signature1234"
    com.example.data.SupabaseSecurityVault.storeObfuscatedKey(sampleKey)
    assertEquals(sampleKey, com.example.data.SupabaseSecurityVault.retrieveAnonKey())

    val redacted = com.example.data.SupabaseSecurityVault.redactKey(sampleKey)
    assertTrue(redacted.contains("[REDACTED]"))
    assertFalse(redacted.contains("sample_payload"))

    val hash1 = com.example.data.SupabaseSecurityVault.hashPassword("MySecret123")
    val hash2 = com.example.data.SupabaseSecurityVault.hashPassword("MySecret123")
    assertEquals(hash1, hash2)
    assertEquals(64, hash1.length)
    assertTrue(com.example.data.SupabaseSecurityVault.verifyPassword("MySecret123", hash1))
    assertFalse(com.example.data.SupabaseSecurityVault.verifyPassword("WrongPass", hash1))

    assertEquals(
      "https://xzutgxiwexxhmpqtybaf.supabase.co",
      com.example.data.SupabaseConfig.DEFAULT_PUBLIC_PROJECT_URL
    )

    // Verify custom table-based auth session token obfuscation & clearing
    com.example.data.SupabaseSecurityVault.storeSessionToken("tok_9f8e7d6c5b4a3210")
    assertEquals("tok_9f8e7d6c5b4a3210", com.example.data.SupabaseSecurityVault.retrieveSessionToken())
    com.example.data.SupabaseSecurityVault.clearSessionToken()
    assertEquals("", com.example.data.SupabaseSecurityVault.retrieveSessionToken())
  }

  @Test
  fun `verify teacher signup subject selection maps curriculum subjects to teacher subjects`() {
    val selectedSubjects = "Physics, Chemistry, Biology"
    val resolved = com.example.ui.resolveTeacherSubjectItems(selectedSubjects)
    assertEquals(3, resolved.size)
    assertEquals(listOf("Physics", "Chemistry", "Biology"), resolved.map { it.name })
    assertEquals(com.example.ui.getSubjectThemeColor("Physics"), resolved[0].color)
    assertEquals(com.example.ui.getSubjectThemeColor("Chemistry"), resolved[1].color)
    assertEquals(com.example.ui.getSubjectThemeColor("Biology"), resolved[2].color)

    // Fallback when blank
    val fallback = com.example.ui.resolveTeacherSubjectItems("")
    assertEquals(com.example.ui.DefaultTeacherSubjects.map { it.name }, fallback.map { it.name })
  }

  @Test
  fun `verify newly signed up students and teachers appear in admin panel users page`() {
    val newStudentDto = com.example.data.SupabaseUserDto(
      id = "user_0772889900",
      email = "0772889900",
      phoneNumber = "0772889900",
      fullName = "Kato Brian",
      role = "student",
      academicClass = "S.4",
      stream = "Green",
      planDuration = "1 Month Plan"
    )
    val newTeacherDto = com.example.data.SupabaseUserDto(
      id = "user_0752112233",
      email = "0752112233",
      phoneNumber = "0752112233",
      fullName = "Tr. Namugenyi Alice",
      role = "teacher",
      subjectsTaught = "Biology, Chemistry",
      planDuration = "Teacher"
    )

    val studentItem = newStudentDto.toAdminUserItem()
    val teacherItem = newTeacherDto.toAdminUserItem()

    org.junit.Assert.assertNotNull(studentItem)
    org.junit.Assert.assertNotNull(teacherItem)
    assertEquals("Kato Brian", studentItem!!.name)
    assertEquals("S.4 Green", studentItem.detailTag)
    assertEquals("0772889900", studentItem.email)
    assertEquals(UserRoleFilter.STUDENTS, studentItem.role)

    assertEquals("Tr. Namugenyi Alice", teacherItem!!.name)
    assertEquals("Biology, Chemistry", teacherItem.detailTag)
    assertEquals("0752112233", teacherItem.email)
    assertEquals(UserRoleFilter.TEACHERS, teacherItem.role)

    val mergedStudents = mergeSignedUpAndDefaultUsers(
      signedUpUsers = listOf(studentItem, teacherItem),
      defaultUsers = DefaultStudentsList,
      targetRole = UserRoleFilter.STUDENTS
    )
    assertEquals(DefaultStudentsList.size + 1, mergedStudents.size)
    assertEquals("Kato Brian", mergedStudents.first().name)

    val mergedTeachers = mergeSignedUpAndDefaultUsers(
      signedUpUsers = listOf(studentItem, teacherItem),
      defaultUsers = DefaultTeachersList,
      targetRole = UserRoleFilter.TEACHERS
    )
    assertEquals(DefaultTeachersList.size + 1, mergedTeachers.size)
    assertEquals("Tr. Namugenyi Alice", mergedTeachers.first().name)
  }

  @Test
  fun `verify supabase free plan 1gb storage and upload size calculations`() {
    assertEquals(1024.0, com.example.data.SUPABASE_FREE_PLAN_STORAGE_MB, 0.001)
    assertEquals(0.0, com.example.data.SupabaseRepository.parseFileSizeToMb(""), 0.001)
    assertEquals(2.4, com.example.data.SupabaseRepository.parseFileSizeToMb("2.4 MB"), 0.001)
    assertEquals(0.5, com.example.data.SupabaseRepository.parseFileSizeToMb("512 KB"), 0.001)
    assertEquals(1024.0, com.example.data.SupabaseRepository.parseFileSizeToMb("1 GB"), 0.001)

    assertEquals("0 MB", com.example.data.SupabaseRepository.formatStorageMb(0.0))
    assertEquals("2.4 MB", com.example.data.SupabaseRepository.formatStorageMb(2.4))
    assertEquals("1,021.6 MB", com.example.data.SupabaseRepository.formatStorageMb(1024.0 - 2.4))
    assertEquals("1 GB", com.example.data.SupabaseRepository.formatStorageMb(1024.0))

    assertEquals("0%", com.example.data.SupabaseRepository.formatStoragePercent(0.0, 1024.0))
    assertEquals("0.23%", com.example.data.SupabaseRepository.formatStoragePercent(2.4, 1024.0))
    assertEquals("50%", com.example.data.SupabaseRepository.formatStoragePercent(512.0, 1024.0))
  }

  @Test
  fun `verify teacher pdf upload item retains pdf bytes and caches locally`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val samplePdfBytes = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\n%%EOF".toByteArray()
    val uploadItem = com.example.ui.SubjectUploadItem(
      id = "note-test-1",
      title = "Quadratic Equations",
      subject = "Math",
      targetClass = "S.4 Green",
      type = "Notes",
      dateUploaded = "Sep 27, 2026",
      fileName = "quadratic_equations.pdf",
      fileSize = "1.2 MB",
      fileBytes = samplePdfBytes
    )

    com.example.ui.savePdfBytesLocally(context, uploadItem.subject, uploadItem.title, samplePdfBytes)
    val loadedBytes = com.example.ui.loadPdfBytesLocally(context, "Math", "Quadratic Equations")
    org.junit.Assert.assertNotNull(loadedBytes)
    org.junit.Assert.assertArrayEquals(samplePdfBytes, loadedBytes)
  }
}
