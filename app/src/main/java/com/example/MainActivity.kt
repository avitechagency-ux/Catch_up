package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.SupabaseConfig
import com.example.data.SupabaseRepository
import com.example.notifications.NotificationHelper
import com.example.ui.AccountScreen
import com.example.ui.AboutScreen
import com.example.ui.AdminAccountScreen
import com.example.ui.AdminAffiliateScreen
import com.example.ui.AdminAnalysisScreen
import com.example.ui.AdminHealthScreen
import com.example.ui.AdminHomeScreen
import com.example.ui.AdminLogsScreen
import com.example.ui.AdminMoreScreen
import com.example.ui.AdminPlaceholderScreen
import com.example.ui.AdminPlansScreen
import com.example.ui.AdminSettingsScreen
import com.example.ui.AdminTransactionsScreen
import com.example.ui.AdminUsersScreen
import com.example.ui.CachedNote
import com.example.ui.DefaultTeacherSubjects
import com.example.ui.DefaultPlanTiers
import com.example.ui.DownloadsScreen
import com.example.ui.HelpAndSupportScreen
import com.example.ui.HomeScreen
import com.example.ui.PdfViewerScreen
import com.example.ui.PlanTier
import com.example.ui.PrivacyPolicyScreen
import com.example.ui.ProfileScreen
import com.example.ui.RecentNoteData
import com.example.ui.SettingsScreen
import com.example.ui.StreakData
import com.example.ui.StudentAffiliateScreen
import com.example.ui.StudentPaymentScreen
import com.example.ui.SubjectNotesScreen
import com.example.ui.SubjectsScreen
import com.example.ui.OnboardingScreen
import com.example.ui.WelcomeScreen
import com.example.ui.LoginScreen
import com.example.ui.SignupScreen
import com.example.ui.TeacherAccountScreen
import com.example.ui.ConnectedStudent
import com.example.ui.DEFAULT_TEACHER_ID_CODE
import com.example.ui.TeacherHomeScreen
import com.example.ui.deserializeConnectedStudents
import com.example.ui.generateTeacherIdCode
import com.example.ui.serializeConnectedStudents
import com.example.ui.TeacherPlaceholderScreen
import com.example.ui.TeacherProfileScreen
import com.example.ui.TeacherRecentUpload
import com.example.ui.TeacherSubjectConfigScreen
import com.example.ui.TeacherSubjectItem
import com.example.ui.TeacherSubjectsScreen
import com.example.ui.resolveTeacherSubjectItems
import com.example.ui.DefaultTeacherAllUploads
import com.example.ui.TeacherUploadItem
import com.example.ui.TeacherUploadNotesScreen
import com.example.ui.TeacherUploadsScreen
import com.example.ui.SubjectUploadItem
import com.example.ui.evaluateDayRollover
import com.example.ui.pruneExpiredDownloads
import com.example.ui.recordSessionTime
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    ONBOARDING,
    WELCOME,
    LOGIN,
    SIGNUP,
    STUDENT_PAYMENT,
    HOME,
    SUBJECTS,
    SUBJECT_NOTES,
    PDF_VIEWER,
    DOWNLOADS,
    PROFILE,
    STUDENT_AFFILIATE,
    ACCOUNT,
    SETTINGS,
    HELP_AND_SUPPORT,
    ABOUT,
    PRIVACY_POLICY,
    TEACHER_HOME,
    TEACHER_MY_SUBJECTS,
    TEACHER_SUBJECT_CONFIG,
    TEACHER_UPLOAD_NOTES,
    TEACHER_UPLOADS,
    TEACHER_PROFILE,
    TEACHER_ACCOUNT,
    ADMIN_HOME,
    ADMIN_ANALYSIS,
    ADMIN_USERS,
    ADMIN_MORE,
    ADMIN_AFFILIATE,
    ADMIN_ACCOUNT,
    ADMIN_PLANS,
    ADMIN_TRANSACTIONS,
    ADMIN_SETTINGS,
    ADMIN_HEALTH,
    ADMIN_LOGS
}

class MainActivity : ComponentActivity() {
    private var pendingNotificationIntent by mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationIntent = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        NotificationHelper.fetchAndStoreCurrentFcmToken(this)
        pendingNotificationIntent = intent
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("catch_up_prefs", Context.MODE_PRIVATE) }
                val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

                // Load persisted streak data or initialize with 6 days, 42 minutes today
                var streakData by remember {
                    val savedDays = prefs.getInt("streak_days", 0)
                    val savedMillis = prefs.getLong("streak_millis_today", 0L)
                    val savedDate = prefs.getString("streak_last_active_date", todayStr) ?: todayStr
                    val savedCompleted = prefs.getBoolean("streak_today_completed", false)
                    val savedMilestone = prefs.getInt("streak_next_milestone", 7)

                    val initial = StreakData(
                        streakDays = savedDays,
                        isTodayCompleted = savedCompleted,
                        nextMilestoneDays = savedMilestone,
                        accumulatedMinutes = (savedMillis / (60 * 1000)).toInt(),
                        accumulatedMillisToday = savedMillis,
                        lastActiveDate = savedDate
                    )
                    mutableStateOf(evaluateDayRollover(initial, todayStr))
                }

                // Persist streak changes
                fun persistStreak(data: StreakData) {
                    prefs.edit()
                        .putInt("streak_days", data.streakDays)
                        .putLong("streak_millis_today", data.accumulatedMillisToday)
                        .putString("streak_last_active_date", data.lastActiveDate)
                        .putBoolean("streak_today_completed", data.isTodayCompleted)
                        .putInt("streak_next_milestone", data.nextMilestoneDays)
                        .apply()
                }

                // Background & active session time tracking without triggering 1-second root recompositions
                val sessionRef = remember {
                    object {
                        var startTimeMs: Long = System.currentTimeMillis()
                        var currentStreak: StreakData = streakData
                    }
                }

                fun syncStreakIfVisibleChanged(updated: StreakData) {
                    sessionRef.currentStreak = updated
                    if (
                        updated.streakDays != streakData.streakDays ||
                        updated.isTodayCompleted != streakData.isTodayCompleted ||
                        updated.nextMilestoneDays != streakData.nextMilestoneDays ||
                        updated.accumulatedMinutes != streakData.accumulatedMinutes
                    ) {
                        streakData = updated
                    }
                    persistStreak(updated)
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                sessionRef.startTimeMs = System.currentTimeMillis()
                                // Check for day rollover if resumed past midnight
                                val currentDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val rolled = evaluateDayRollover(sessionRef.currentStreak, currentDay)
                                syncStreakIfVisibleChanged(rolled)
                            }
                            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                                val elapsed = System.currentTimeMillis() - sessionRef.startTimeMs
                                if (elapsed > 0) {
                                    val updated = recordSessionTime(sessionRef.currentStreak, elapsed)
                                    syncStreakIfVisibleChanged(updated)
                                }
                                sessionRef.startTimeMs = System.currentTimeMillis()
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        val elapsed = System.currentTimeMillis() - sessionRef.startTimeMs
                        if (elapsed > 0) {
                            val updated = recordSessionTime(sessionRef.currentStreak, elapsed)
                            sessionRef.currentStreak = updated
                            persistStreak(updated)
                        }
                    }
                }

                // Lightweight ticker: checks every 15 seconds and only triggers recomposition when visible minutes/streak change
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(15_000L)
                        val elapsed = System.currentTimeMillis() - sessionRef.startTimeMs
                        sessionRef.startTimeMs = System.currentTimeMillis()
                        if (elapsed > 0) {
                            val updated = recordSessionTime(sessionRef.currentStreak, elapsed)
                            syncStreakIfVisibleChanged(updated)
                        }
                    }
                }

                val initialScreen = remember {
                    val hasResetForV6 = prefs.getBoolean("onboarding_v6_reinstall_reset", false)
                    if (!hasResetForV6) {
                        prefs.edit()
                            .putBoolean("onboarding_v6_reinstall_reset", true)
                            .putBoolean("is_first_time", true)
                            .putBoolean("is_logged_in", false)
                            .remove("last_active_screen")
                            .apply()
                    }
                    val isLoggedIn = prefs.getBoolean("is_logged_in", false)
                    val isFirstTime = prefs.getBoolean("is_first_time", true)
                    val savedScreen = prefs.getString("last_active_screen", null)
                    when {
                        isFirstTime -> AppScreen.ONBOARDING
                        !isLoggedIn -> AppScreen.WELCOME
                        savedScreen != null -> {
                            try { AppScreen.valueOf(savedScreen) } catch (e: Exception) { AppScreen.HOME }
                        }
                        else -> AppScreen.HOME
                    }
                }
                var currentScreen by remember { mutableStateOf(initialScreen) }
                var previousScreen by remember { mutableStateOf(AppScreen.TEACHER_HOME) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(currentScreen) {
                    prefs.edit().putString("last_active_screen", currentScreen.name).apply()
                }
                var selectedSubjectName by remember { mutableStateOf("Math") }
                var subjectNotesPreviousScreen by remember { mutableStateOf(AppScreen.SUBJECTS) }
                var selectedPdfTitle by remember { mutableStateOf("Algebra") }
                var selectedPdfSubject by remember { mutableStateOf("Math") }

                var studentName by remember {
                    mutableStateOf(prefs.getString("student_name", "Student") ?: "Student")
                }
                var studentPhone by remember {
                    mutableStateOf(prefs.getString("user_phone", "") ?: "")
                }
                var schoolName by remember {
                    mutableStateOf(prefs.getString("student_school", "Secondary School") ?: "Secondary School")
                }
                var academicClass by remember {
                    mutableStateOf(prefs.getString("student_class", "S.4") ?: "S.4")
                }
                var activePlanDuration by remember {
                    mutableStateOf(prefs.getString("student_active_plan_duration", "") ?: "")
                }
                var activePlanExpiry by remember {
                    mutableStateOf(prefs.getString("student_active_plan_expiry", "") ?: "")
                }

                val onSaveProfile: (String, String, String) -> Unit = { newName, newSchool, newClass ->
                    studentName = newName
                    schoolName = newSchool
                    academicClass = newClass
                    prefs.edit()
                        .putString("student_name", newName)
                        .putString("student_school", newSchool)
                        .putString("student_class", newClass)
                        .apply()
                }

                var teacherName by remember {
                    mutableStateOf(prefs.getString("teacher_name", "Teacher") ?: "Teacher")
                }
                var teacherIdCode by remember {
                    val savedId = prefs.getString("teacher_id_code", null)
                    val initialId = if (!savedId.isNullOrBlank()) savedId else generateTeacherIdCode(teacherName)
                    mutableStateOf(initialId)
                }
                var teacherSchool by remember {
                    mutableStateOf(prefs.getString("teacher_school", "Secondary School") ?: "Secondary School")
                }
                var teacherSubjectsTaught by remember {
                    mutableStateOf(prefs.getString("teacher_subjects", "Secondary Curriculum") ?: "Secondary Curriculum")
                }

                var linkedSubjectTeachers by remember {
                    val raw = prefs.getString("student_linked_subject_teachers", "") ?: ""
                    val initialMap = raw.split(";")
                        .mapNotNull { entry ->
                            val parts = entry.split(":")
                            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                                parts[0] to parts[1]
                            } else null
                        }
                        .toMap()
                    mutableStateOf(initialMap)
                }

                val persistLinkedSubjectTeachers: (Map<String, String>) -> Unit = { updatedMap ->
                    linkedSubjectTeachers = updatedMap
                    val serialized = updatedMap.entries.joinToString(";") { "${it.key}:${it.value}" }
                    prefs.edit().putString("student_linked_subject_teachers", serialized).apply()
                }

                var connectedStudentsList by remember {
                    val raw = prefs.getString("teacher_connected_students", "") ?: ""
                    mutableStateOf(deserializeConnectedStudents(raw))
                }

                val recordConnectedStudent: (String, String) -> Unit = { subjectName, enteredTeacherId ->
                    val normalizedCode = enteredTeacherId.trim().uppercase()
                    val nowDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                    val currentStudentName = studentName.trim().ifBlank { "Student" }
                    val currentStudentClass = academicClass.trim().ifBlank { "S.4" }
                    val newEntry = ConnectedStudent(
                        id = "${currentStudentName.lowercase().replace(" ", "_")}_${subjectName.lowercase().replace(" ", "_")}_$normalizedCode",
                        studentName = currentStudentName,
                        studentClass = currentStudentClass,
                        subjectName = subjectName,
                        teacherIdCode = normalizedCode,
                        connectedDate = nowDate
                    )
                    val updatedList = listOf(newEntry) + connectedStudentsList.filterNot {
                        it.studentName.equals(currentStudentName, ignoreCase = true) &&
                            it.subjectName.equals(subjectName, ignoreCase = true) &&
                            it.teacherIdCode.equals(normalizedCode, ignoreCase = true)
                    }
                    connectedStudentsList = updatedList
                    prefs.edit().putString("teacher_connected_students", serializeConnectedStudents(updatedList)).apply()
                    coroutineScope.launch {
                        SupabaseRepository.syncStudentTeacherLink(
                            studentPhone = studentPhone,
                            studentName = currentStudentName,
                            studentClass = currentStudentClass,
                            subjectName = subjectName,
                            teacherIdCode = normalizedCode,
                            connectedDate = nowDate
                        )
                    }
                }

                // Sync connected students for teacher from Supabase when entering TEACHER_HOME
                LaunchedEffect(currentScreen, teacherIdCode) {
                    if (currentScreen == AppScreen.TEACHER_HOME && SupabaseConfig.isConfigured()) {
                        val remoteStudents = SupabaseRepository.fetchConnectedStudentsForTeacher(teacherIdCode)
                        if (remoteStudents.isNotEmpty()) {
                            val merged = (remoteStudents + connectedStudentsList)
                                .distinctBy { "${it.studentName.lowercase()}_${it.subjectName.lowercase()}_${it.teacherIdCode.uppercase()}" }
                            connectedStudentsList = merged
                            prefs.edit().putString("teacher_connected_students", serializeConnectedStudents(merged)).apply()
                        }
                    }
                }

                val onSaveTeacherProfile: (String, String, String) -> Unit = { newName, newSchool, newSubjects ->
                    teacherName = newName
                    teacherSchool = newSchool
                    teacherSubjectsTaught = newSubjects
                    prefs.edit()
                        .putString("teacher_name", newName)
                        .putString("teacher_school", newSchool)
                        .putString("teacher_subjects", newSubjects)
                        .apply()
                }

                var recentNotesList by remember {
                    mutableStateOf<List<RecentNoteData>>(emptyList())
                }

                var downloadsList by remember {
                    mutableStateOf<List<CachedNote>>(emptyList())
                }

                var teacherUploadsList by remember {
                    mutableStateOf<List<TeacherUploadItem>>(DefaultTeacherAllUploads)
                }

                val recordRecentTopic: (String, String, String?) -> Unit = { title, subject, date ->
                    val currentDate = date ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    val newRecent = RecentNoteData(
                        title = title,
                        subject = subject,
                        date = currentDate
                    )
                    val updated = listOf(newRecent) + recentNotesList.filterNot {
                        it.title.equals(title, ignoreCase = true) && it.subject.equals(subject, ignoreCase = true)
                    }
                    recentNotesList = updated.take(3)
                }

                var selectedTeacherSubjectItem by remember {
                    mutableStateOf(DefaultTeacherSubjects.first())
                }
                var teacherNoteToEdit by remember {
                    mutableStateOf<SubjectUploadItem?>(null)
                }

                var settingsPreviousScreen by remember {
                    mutableStateOf(AppScreen.PROFILE)
                }

                var accountPreviousScreen by remember {
                    mutableStateOf(AppScreen.PROFILE)
                }

                var helpPreviousScreen by remember {
                    mutableStateOf(AppScreen.PROFILE)
                }

                var aboutPreviousScreen by remember {
                    mutableStateOf(AppScreen.PROFILE)
                }

                var privacyPreviousScreen by remember {
                    mutableStateOf(AppScreen.PROFILE)
                }

                var adminPreviousScreen by remember {
                    mutableStateOf(AppScreen.HOME)
                }

                var adminAccountPreviousScreen by remember {
                    mutableStateOf(AppScreen.ADMIN_MORE)
                }

                var adminAffiliatePreviousScreen by remember {
                    mutableStateOf(AppScreen.ADMIN_MORE)
                }

                var adminTransactionsPreviousScreen by remember {
                    mutableStateOf(AppScreen.ADMIN_MORE)
                }

                var adminHealthPreviousScreen by remember {
                    mutableStateOf(AppScreen.ADMIN_MORE)
                }

                var adminLogsPreviousScreen by remember {
                    mutableStateOf(AppScreen.ADMIN_MORE)
                }

                // Request POST_NOTIFICATIONS runtime permission on Android 13+ when entering Home screen
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ ->
                    prefs.edit()
                        .putBoolean(NotificationHelper.PREF_NOTIFICATION_PERMISSION_REQUESTED, true)
                        .apply()
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == AppScreen.HOME && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val alreadyRequested = prefs.getBoolean(
                            NotificationHelper.PREF_NOTIFICATION_PERMISSION_REQUESTED,
                            false
                        )
                        val isGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!isGranted && !alreadyRequested) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Handle deep-linking from tapped push notifications ("new_note" -> PDF_VIEWER, "payment_reminder" -> ACCOUNT)
                LaunchedEffect(pendingNotificationIntent) {
                    val activeIntent = pendingNotificationIntent ?: return@LaunchedEffect
                    val notifType = activeIntent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
                    val noteId = activeIntent.getStringExtra(NotificationHelper.EXTRA_NOTE_ID)

                    when {
                        notifType == NotificationHelper.TYPE_NEW_NOTE || !noteId.isNullOrBlank() -> {
                            val matchedNote = if (!noteId.isNullOrBlank()) {
                                teacherUploadsList.firstOrNull { it.id.equals(noteId, ignoreCase = true) }
                            } else {
                                null
                            }
                            val targetTitle = matchedNote?.title
                                ?: activeIntent.getStringExtra(NotificationHelper.EXTRA_NOTE_TITLE)
                                    ?.takeIf { it.isNotBlank() }
                                ?: "Population Density"
                            val targetSubject = matchedNote?.subject
                                ?: activeIntent.getStringExtra(NotificationHelper.EXTRA_NOTE_SUBJECT)
                                    ?.takeIf { it.isNotBlank() }
                                ?: "Geography"

                            selectedPdfTitle = targetTitle
                            selectedPdfSubject = targetSubject
                            recordRecentTopic(targetTitle, targetSubject, null)
                            previousScreen = AppScreen.HOME
                            currentScreen = AppScreen.PDF_VIEWER

                            activeIntent.removeExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
                            activeIntent.removeExtra(NotificationHelper.EXTRA_NOTE_ID)
                            pendingNotificationIntent = null
                        }
                        notifType == NotificationHelper.TYPE_PAYMENT_REMINDER -> {
                            accountPreviousScreen = AppScreen.HOME
                            currentScreen = AppScreen.ACCOUNT

                            activeIntent.removeExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
                            pendingNotificationIntent = null
                        }
                    }
                }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = when (currentScreen) {
                        AppScreen.LOGIN -> AppScreen.WELCOME
                        AppScreen.SIGNUP -> AppScreen.WELCOME
                        AppScreen.STUDENT_PAYMENT -> AppScreen.SIGNUP
                        AppScreen.WELCOME -> AppScreen.WELCOME
                        AppScreen.ONBOARDING -> AppScreen.ONBOARDING
                        AppScreen.PDF_VIEWER -> previousScreen
                        AppScreen.SUBJECT_NOTES -> AppScreen.SUBJECTS
                        AppScreen.DOWNLOADS -> AppScreen.HOME
                        AppScreen.SUBJECTS -> AppScreen.HOME
                        AppScreen.PROFILE -> AppScreen.HOME
                        AppScreen.STUDENT_AFFILIATE -> AppScreen.PROFILE
                        AppScreen.ACCOUNT -> accountPreviousScreen
                        AppScreen.SETTINGS -> settingsPreviousScreen
                        AppScreen.HELP_AND_SUPPORT -> helpPreviousScreen
                        AppScreen.ABOUT -> aboutPreviousScreen
                        AppScreen.PRIVACY_POLICY -> privacyPreviousScreen
                        AppScreen.TEACHER_UPLOAD_NOTES -> previousScreen
                        AppScreen.TEACHER_SUBJECT_CONFIG -> AppScreen.TEACHER_MY_SUBJECTS
                        AppScreen.TEACHER_MY_SUBJECTS -> AppScreen.TEACHER_HOME
                        AppScreen.TEACHER_UPLOADS -> AppScreen.TEACHER_HOME
                        AppScreen.TEACHER_ACCOUNT -> AppScreen.TEACHER_PROFILE
                        AppScreen.TEACHER_PROFILE -> AppScreen.TEACHER_HOME
                        AppScreen.TEACHER_HOME -> AppScreen.PROFILE
                        AppScreen.ADMIN_ANALYSIS -> AppScreen.ADMIN_HOME
                        AppScreen.ADMIN_USERS -> AppScreen.ADMIN_HOME
                        AppScreen.ADMIN_MORE -> AppScreen.ADMIN_HOME
                        AppScreen.ADMIN_AFFILIATE -> adminAffiliatePreviousScreen
                        AppScreen.ADMIN_ACCOUNT -> adminAccountPreviousScreen
                        AppScreen.ADMIN_PLANS -> AppScreen.ADMIN_HOME
                        AppScreen.ADMIN_TRANSACTIONS -> adminTransactionsPreviousScreen
                        AppScreen.ADMIN_SETTINGS -> AppScreen.ADMIN_MORE
                        AppScreen.ADMIN_HEALTH -> adminHealthPreviousScreen
                        AppScreen.ADMIN_LOGS -> adminLogsPreviousScreen
                        AppScreen.ADMIN_HOME -> adminPreviousScreen
                        else -> AppScreen.HOME
                    }
                }

                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        AppScreen.ONBOARDING -> OnboardingScreen(
                            onFinishOnboarding = {
                                prefs.edit().putBoolean("is_first_time", false).apply()
                                currentScreen = AppScreen.WELCOME
                            }
                        )
                        AppScreen.WELCOME -> WelcomeScreen(
                            onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                            onNavigateToSignup = { currentScreen = AppScreen.SIGNUP }
                        )
                        AppScreen.LOGIN -> LoginScreen(
                            onNavigateBack = { currentScreen = AppScreen.WELCOME },
                            onNavigateToSignup = { currentScreen = AppScreen.SIGNUP },
                            onLoginSuccess = { role, phoneOrIdentifier, fullName, savedClass, savedStream, savedSubjects ->
                                val editor = prefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("user_role", role)
                                    .putString("user_email", phoneOrIdentifier)
                                    .putString("user_phone", phoneOrIdentifier)

                                if (role.equals("student", ignoreCase = true)) {
                                    studentPhone = phoneOrIdentifier
                                    if (!fullName.isNullOrBlank()) {
                                        studentName = fullName
                                        editor.putString("student_name", fullName)
                                    }
                                    val combinedClassStream = listOfNotNull(
                                        savedClass?.takeIf { it.isNotBlank() },
                                        savedStream?.takeIf { it.isNotBlank() }
                                    ).joinToString(" ")
                                    if (combinedClassStream.isNotBlank()) {
                                        academicClass = combinedClassStream
                                        editor.putString("student_class", combinedClassStream)
                                    }
                                } else if (role.equals("teacher", ignoreCase = true)) {
                                    if (!fullName.isNullOrBlank()) {
                                        teacherName = fullName
                                        editor.putString("teacher_name", fullName)
                                    }
                                    val generatedId = generateTeacherIdCode(fullName ?: phoneOrIdentifier)
                                    teacherIdCode = generatedId
                                    editor.putString("teacher_id_code", generatedId)
                                    if (!savedSubjects.isNullOrBlank()) {
                                        teacherSubjectsTaught = savedSubjects
                                        val resolvedItems = resolveTeacherSubjectItems(savedSubjects)
                                        if (resolvedItems.isNotEmpty()) {
                                            selectedTeacherSubjectItem = resolvedItems.first()
                                        }
                                        editor.putString("teacher_subjects", savedSubjects)
                                    }
                                }
                                editor.apply()

                                currentScreen = when (role.lowercase()) {
                                    "teacher" -> AppScreen.TEACHER_HOME
                                    "admin" -> AppScreen.ADMIN_HOME
                                    else -> AppScreen.HOME
                                }
                            }
                        )
                        AppScreen.SIGNUP -> SignupScreen(
                            onNavigateBack = { currentScreen = AppScreen.WELCOME },
                            onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                            onSignupSuccess = { role, phoneNumber, name, studentClass, studentStream, selectedTeacherSubjects ->
                                val editor = prefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("user_role", role)
                                    .putString("user_email", phoneNumber)
                                    .putString("user_phone", phoneNumber)

                                if (role.equals("student", ignoreCase = true)) {
                                    studentName = name
                                    studentPhone = phoneNumber
                                    activePlanDuration = ""
                                    activePlanExpiry = ""
                                    val combinedClassStream = listOf(studentClass.trim(), studentStream.trim())
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ")
                                        .ifBlank { "S.4" }
                                    academicClass = combinedClassStream
                                    editor
                                        .putString("student_name", name)
                                        .putString("student_class", combinedClassStream)
                                        .putString("student_class_only", studentClass.trim())
                                        .putString("student_stream_only", studentStream.trim())
                                        .putString("student_active_plan_duration", "")
                                        .putString("student_active_plan_expiry", "")
                                        .putString("student_linked_subject_teachers", "")
                                    linkedSubjectTeachers = emptyMap()
                                } else if (role.equals("teacher", ignoreCase = true)) {
                                    teacherName = name
                                    val generatedId = generateTeacherIdCode(name.ifBlank { phoneNumber })
                                    teacherIdCode = generatedId
                                    val cleanSubjects = selectedTeacherSubjects.trim().ifBlank { "Secondary Curriculum" }
                                    teacherSubjectsTaught = cleanSubjects
                                    val resolvedTeacherItems = resolveTeacherSubjectItems(cleanSubjects)
                                    if (resolvedTeacherItems.isNotEmpty()) {
                                        selectedTeacherSubjectItem = resolvedTeacherItems.first()
                                    }
                                    editor
                                        .putString("teacher_name", name)
                                        .putString("teacher_id_code", generatedId)
                                        .putString("teacher_subjects", cleanSubjects)
                                }
                                editor.apply()

                                currentScreen = when (role.lowercase()) {
                                    "teacher" -> AppScreen.TEACHER_HOME
                                    "admin" -> AppScreen.ADMIN_HOME
                                    else -> AppScreen.STUDENT_PAYMENT
                                }
                            }
                        )
                        AppScreen.STUDENT_PAYMENT -> StudentPaymentScreen(
                            studentName = studentName,
                            signupPhoneNumber = studentPhone,
                            selectedPlan = DefaultPlanTiers.first(),
                            onNavigateBack = {
                                currentScreen = AppScreen.SIGNUP
                            },
                            onPaymentSuccess = { paidPlan, paidPhone ->
                                studentPhone = paidPhone
                                activePlanDuration = paidPlan.duration
                                activePlanExpiry = "Valid for 30 days"
                                prefs.edit()
                                    .putString("user_phone", paidPhone)
                                    .putString("student_active_plan_duration", activePlanDuration)
                                    .putString("student_active_plan_expiry", activePlanExpiry)
                                    .apply()
                                val nowDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                coroutineScope.launch {
                                    SupabaseRepository.recordStudentPayment(
                                        studentName = studentName,
                                        phoneNumber = paidPhone,
                                        planDuration = paidPlan.duration,
                                        amountUgx = paidPlan.priceUgx.toLong(),
                                        transactionDate = nowDate
                                    )
                                }
                                currentScreen = AppScreen.HOME
                            }
                        )
                        AppScreen.HOME -> HomeScreen(
                            studentName = studentName,
                            streakData = streakData,
                            activePlanDuration = activePlanDuration,
                            activePlanExpiry = activePlanExpiry,
                            recentNotes = recentNotesList,
                            linkedSubjectTeachers = linkedSubjectTeachers,
                            onLinkSubjectTeacher = { subjectName, teacherId ->
                                persistLinkedSubjectTeachers(linkedSubjectTeachers + (subjectName to teacherId))
                                recordConnectedStudent(subjectName, teacherId)
                            },
                            onNavigateToSubjects = {
                                currentScreen = AppScreen.SUBJECTS
                            },
                            onNavigateToDownloads = {
                                currentScreen = AppScreen.DOWNLOADS
                            },
                            onNavigateToProfile = {
                                currentScreen = AppScreen.PROFILE
                            },
                            onNavigateToAccount = {
                                accountPreviousScreen = AppScreen.HOME
                                currentScreen = AppScreen.ACCOUNT
                            },
                            onNavigateToSubjectDetail = { subject ->
                                selectedSubjectName = subject
                                subjectNotesPreviousScreen = AppScreen.HOME
                                currentScreen = AppScreen.SUBJECT_NOTES
                            },
                            onOpenNote = { title, subject ->
                                selectedPdfTitle = title
                                selectedPdfSubject = subject
                                recordRecentTopic(title, subject, null)
                                previousScreen = AppScreen.HOME
                                currentScreen = AppScreen.PDF_VIEWER
                            },
                            onNavigateToTeacher = {
                                currentScreen = AppScreen.TEACHER_HOME
                            },
                            onNavigateToAdmin = {
                                adminPreviousScreen = AppScreen.HOME
                                currentScreen = AppScreen.ADMIN_HOME
                            }
                        )
                        AppScreen.SUBJECTS -> SubjectsScreen(
                            linkedSubjectTeachers = linkedSubjectTeachers,
                            onLinkSubjectTeacher = { subjectName, teacherId ->
                                persistLinkedSubjectTeachers(linkedSubjectTeachers + (subjectName to teacherId))
                                recordConnectedStudent(subjectName, teacherId)
                            },
                            onNavigateToHome = {
                                currentScreen = AppScreen.HOME
                            },
                            onNavigateToDownloads = {
                                currentScreen = AppScreen.DOWNLOADS
                            },
                            onNavigateToProfile = {
                                currentScreen = AppScreen.PROFILE
                            },
                            onSubjectClick = { subjectItem ->
                                selectedSubjectName = subjectItem.name
                                subjectNotesPreviousScreen = AppScreen.SUBJECTS
                                currentScreen = AppScreen.SUBJECT_NOTES
                            }
                        )
                        AppScreen.SUBJECT_NOTES -> {
                            val subjectNotesInitial = teacherUploadsList
                                .filter { it.subject.equals(selectedSubjectName, ignoreCase = true) && !it.type.equals("Assignment", ignoreCase = true) }
                                .map { com.example.ui.TopicItem(id = it.id, title = it.title, date = it.dateUploaded) }
                            val subjectAssignmentsInitial = teacherUploadsList
                                .filter { it.subject.equals(selectedSubjectName, ignoreCase = true) && it.type.equals("Assignment", ignoreCase = true) }
                                .map { com.example.ui.TopicItem(id = it.id, title = it.title, date = it.dateUploaded) }
                            SubjectNotesScreen(
                                subjectName = selectedSubjectName,
                                initialNotes = subjectNotesInitial,
                                initialAssignments = subjectAssignmentsInitial,
                                onNavigateBack = {
                                    currentScreen = subjectNotesPreviousScreen
                                },
                                onNavigateToHome = {
                                    currentScreen = AppScreen.HOME
                                },
                                onNavigateToSubjects = {
                                    currentScreen = AppScreen.SUBJECTS
                                },
                                onNavigateToDownloads = {
                                    currentScreen = AppScreen.DOWNLOADS
                                },
                                onNavigateToProfile = {
                                    currentScreen = AppScreen.PROFILE
                                },
                                onTopicClick = { topic ->
                                    selectedPdfTitle = topic.title
                                    selectedPdfSubject = selectedSubjectName
                                    recordRecentTopic(topic.title, selectedSubjectName, topic.date)
                                    previousScreen = AppScreen.SUBJECT_NOTES
                                    currentScreen = AppScreen.PDF_VIEWER
                                }
                            )
                        }
                        AppScreen.PDF_VIEWER -> {
                            val isCurrentlySaved = downloadsList.any {
                                it.title.equals(selectedPdfTitle, ignoreCase = true) &&
                                it.subject.equals(selectedPdfSubject, ignoreCase = true)
                            }
                            PdfViewerScreen(
                                noteTitle = selectedPdfTitle,
                                subjectName = selectedPdfSubject,
                                isSavedInitially = isCurrentlySaved,
                                onNavigateBack = {
                                    currentScreen = previousScreen
                                },
                                onNoteOpened = {
                                    // Keep recent list up to date
                                    recordRecentTopic(selectedPdfTitle, selectedPdfSubject, null)
                                    // Update last accessed timestamp for this note in cache list
                                    downloadsList = downloadsList.map { note ->
                                        if (note.title.equals(selectedPdfTitle, ignoreCase = true) &&
                                            note.subject.equals(selectedPdfSubject, ignoreCase = true)
                                        ) {
                                            note.copy(lastAccessedTimestamp = System.currentTimeMillis())
                                        } else {
                                            note
                                        }
                                    }
                                },
                                onToggleSave = { save ->
                                    if (save) {
                                        if (!downloadsList.any { it.title.equals(selectedPdfTitle, ignoreCase = true) && it.subject.equals(selectedPdfSubject, ignoreCase = true) }) {
                                            val newNote = CachedNote(
                                                id = System.currentTimeMillis().toString(),
                                                title = selectedPdfTitle,
                                                subject = selectedPdfSubject,
                                                dateAdded = "19/09/2026",
                                                lastAccessedTimestamp = System.currentTimeMillis()
                                            )
                                            downloadsList = listOf(newNote) + downloadsList
                                        }
                                    } else {
                                        downloadsList = downloadsList.filterNot {
                                            it.title.equals(selectedPdfTitle, ignoreCase = true) &&
                                            it.subject.equals(selectedPdfSubject, ignoreCase = true)
                                        }
                                    }
                                }
                            )
                        }
                        AppScreen.DOWNLOADS -> DownloadsScreen(
                            downloadsList = downloadsList,
                            onNavigateToHome = {
                                currentScreen = AppScreen.HOME
                            },
                            onNavigateToSubjects = {
                                currentScreen = AppScreen.SUBJECTS
                            },
                            onNavigateToProfile = {
                                currentScreen = AppScreen.PROFILE
                            },
                            onPruneExpired = {
                                downloadsList = pruneExpiredDownloads(downloadsList)
                            },
                            onNoteClick = { note ->
                                selectedPdfTitle = note.title
                                selectedPdfSubject = note.subject
                                recordRecentTopic(note.title, note.subject, note.dateAdded)
                                previousScreen = AppScreen.DOWNLOADS
                                currentScreen = AppScreen.PDF_VIEWER
                            }
                        )
                        AppScreen.PROFILE -> ProfileScreen(
                            studentName = studentName,
                            schoolName = schoolName,
                            academicClass = academicClass,
                            streakDays = streakData.streakDays,
                            validUntilText = activePlanExpiry,
                            onNavigateToHome = {
                                currentScreen = AppScreen.HOME
                            },
                            onNavigateToSubjects = {
                                currentScreen = AppScreen.SUBJECTS
                            },
                            onNavigateToDownloads = {
                                currentScreen = AppScreen.DOWNLOADS
                            },
                            onNavigateToTeacher = {
                                currentScreen = AppScreen.TEACHER_HOME
                            },
                            onNavigateToAccount = {
                                accountPreviousScreen = AppScreen.PROFILE
                                currentScreen = AppScreen.ACCOUNT
                            },
                            onNavigateToAffiliate = {
                                currentScreen = AppScreen.STUDENT_AFFILIATE
                            },
                            onNavigateToSettings = {
                                settingsPreviousScreen = AppScreen.PROFILE
                                currentScreen = AppScreen.SETTINGS
                            },
                            onNavigateToHelpAndSupport = {
                                helpPreviousScreen = AppScreen.PROFILE
                                currentScreen = AppScreen.HELP_AND_SUPPORT
                            },
                            onNavigateToAbout = {
                                aboutPreviousScreen = AppScreen.PROFILE
                                currentScreen = AppScreen.ABOUT
                            },
                            onNavigateToPrivacyPolicy = {
                                privacyPreviousScreen = AppScreen.PROFILE
                                currentScreen = AppScreen.PRIVACY_POLICY
                            },
                            onNavigateToAdmin = {
                                adminPreviousScreen = AppScreen.PROFILE
                                currentScreen = AppScreen.ADMIN_HOME
                            },
                            onUpdateProfile = onSaveProfile,
                            onLogout = {
                                prefs.edit().putBoolean("is_logged_in", false).apply()
                                coroutineScope.launch { SupabaseRepository.revokeCurrentSession() }
                                currentScreen = AppScreen.WELCOME
                            }
                        )
                        AppScreen.STUDENT_AFFILIATE -> StudentAffiliateScreen(
                            studentName = studentName,
                            initialBalance = 1500,
                            initialReferralCode = "STUDENT100",
                            onNavigateBack = {
                                currentScreen = AppScreen.PROFILE
                            },
                            onNavigateToHome = {
                                currentScreen = AppScreen.HOME
                            },
                            onNavigateToSubjects = {
                                currentScreen = AppScreen.SUBJECTS
                            },
                            onNavigateToDownloads = {
                                currentScreen = AppScreen.DOWNLOADS
                            },
                            onNavigateToProfile = {
                                currentScreen = AppScreen.PROFILE
                            },
                            onPlanPurchased = { planDuration, planExpiry ->
                                activePlanDuration = planDuration
                                activePlanExpiry = planExpiry
                                prefs.edit()
                                    .putString("student_active_plan_duration", planDuration)
                                    .putString("student_active_plan_expiry", planExpiry)
                                    .apply()
                            }
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            onNavigateBack = {
                                currentScreen = settingsPreviousScreen
                            },
                            onClearCachedNotes = {
                                downloadsList = emptyList()
                            }
                        )
                        AppScreen.HELP_AND_SUPPORT -> HelpAndSupportScreen(
                            onNavigateBack = {
                                currentScreen = helpPreviousScreen
                            }
                        )
                        AppScreen.ABOUT -> AboutScreen(
                            onNavigateBack = {
                                currentScreen = aboutPreviousScreen
                            }
                        )
                        AppScreen.PRIVACY_POLICY -> PrivacyPolicyScreen(
                            onNavigateBack = {
                                currentScreen = privacyPreviousScreen
                            }
                        )
                        AppScreen.ACCOUNT -> AccountScreen(
                            activePlanDuration = activePlanDuration,
                            activePlanExpiry = activePlanExpiry,
                            initialPhoneNumber = studentPhone,
                            onNavigateBack = {
                                currentScreen = accountPreviousScreen
                            },
                            onPlanUpdated = { newPlan ->
                                activePlanDuration = newPlan.duration
                                activePlanExpiry = "Valid for 30 days"
                                prefs.edit()
                                    .putString("student_active_plan_duration", activePlanDuration)
                                    .putString("student_active_plan_expiry", activePlanExpiry)
                                    .apply()
                            }
                        )
                        AppScreen.TEACHER_HOME -> {
                            val teacherConnectedStudents = connectedStudentsList.filter {
                                it.teacherIdCode.equals(teacherIdCode, ignoreCase = true)
                            }.ifEmpty { connectedStudentsList }
                            TeacherHomeScreen(
                                teacherName = teacherName,
                                teacherIdCode = teacherIdCode,
                                subjectsTaught = teacherSubjectsTaught,
                                notesUploadedCount = teacherUploadsList.size,
                                classesCount = teacherConnectedStudents.size,
                                connectedStudents = teacherConnectedStudents,
                                notesSharedThisTermCount = teacherUploadsList.size,
                                mostActiveClass = if (teacherUploadsList.isEmpty()) "None yet" else teacherUploadsList.groupBy { it.targetClass }.maxByOrNull { it.value.size }?.key ?: "None yet",
                                notesOpenedThisWeekCount = 0,
                                recentUploads = teacherUploadsList.take(5).map {
                                    TeacherRecentUpload(
                                        id = it.id,
                                        title = it.title,
                                        subject = it.subject,
                                        targetClass = it.targetClass,
                                        dateUploaded = it.dateUploaded
                                    )
                                },
                                onNavigateToMySubjects = {
                                    currentScreen = AppScreen.TEACHER_MY_SUBJECTS
                                },
                                onNavigateToUploads = {
                                    currentScreen = AppScreen.TEACHER_UPLOADS
                                },
                                onNavigateToTeacherProfile = {
                                    currentScreen = AppScreen.TEACHER_PROFILE
                                },
                                onUploadNotesClick = {
                                    teacherNoteToEdit = null
                                    previousScreen = AppScreen.TEACHER_HOME
                                    currentScreen = AppScreen.TEACHER_UPLOAD_NOTES
                                },
                                onRecentNoteClick = { recentNote ->
                                    selectedPdfTitle = recentNote.title
                                    selectedPdfSubject = recentNote.subject
                                    recordRecentTopic(recentNote.title, recentNote.subject, null)
                                    previousScreen = AppScreen.TEACHER_HOME
                                    currentScreen = AppScreen.PDF_VIEWER
                                },
                                onSeeAllRecentClick = {
                                    currentScreen = AppScreen.TEACHER_UPLOADS
                                },
                                onSwitchToStudentView = {
                                    currentScreen = AppScreen.PROFILE
                                }
                            )
                        }
                        AppScreen.TEACHER_MY_SUBJECTS -> {
                            val activeTeacherSubjects = resolveTeacherSubjectItems(teacherSubjectsTaught)
                            TeacherSubjectsScreen(
                                teacherName = teacherName,
                                subjects = activeTeacherSubjects.map { subjectItem ->
                                    val subjectUploads = teacherUploadsList.filter { it.subject.equals(subjectItem.name, ignoreCase = true) }
                                    subjectItem.copy(
                                        noteCount = subjectUploads.count { it.type.equals("Notes", ignoreCase = true) },
                                        assignmentCount = subjectUploads.count { it.type.equals("Assignment", ignoreCase = true) }
                                    )
                                },
                                onNavigateToHome = { currentScreen = AppScreen.TEACHER_HOME },
                                onNavigateToMySubjects = { /* Already on My Subjects */ },
                                onNavigateToUploads = { currentScreen = AppScreen.TEACHER_UPLOADS },
                                onNavigateToTeacherProfile = { currentScreen = AppScreen.TEACHER_PROFILE },
                                onSubjectClick = { subjectItem ->
                                    selectedTeacherSubjectItem = subjectItem
                                    currentScreen = AppScreen.TEACHER_SUBJECT_CONFIG
                                }
                            )
                        }
                        AppScreen.TEACHER_SUBJECT_CONFIG -> TeacherSubjectConfigScreen(
                            subject = selectedTeacherSubjectItem,
                            initialUploads = teacherUploadsList.filter { it.subject.equals(selectedTeacherSubjectItem.name, ignoreCase = true) }.map {
                                SubjectUploadItem(
                                    id = it.id,
                                    title = it.title,
                                    subject = it.subject,
                                    targetClass = it.targetClass,
                                    type = it.type,
                                    dateUploaded = it.dateUploaded,
                                    dueDate = it.dueDate
                                )
                            },
                            onNavigateBack = {
                                currentScreen = AppScreen.TEACHER_MY_SUBJECTS
                            },
                            onUploadNotesClick = {
                                teacherNoteToEdit = null
                                previousScreen = AppScreen.TEACHER_SUBJECT_CONFIG
                                currentScreen = AppScreen.TEACHER_UPLOAD_NOTES
                            },
                            onEditNote = { note ->
                                teacherNoteToEdit = note
                                previousScreen = AppScreen.TEACHER_SUBJECT_CONFIG
                                currentScreen = AppScreen.TEACHER_UPLOAD_NOTES
                            },
                            onOpenNote = { title, subject ->
                                selectedPdfTitle = title
                                selectedPdfSubject = subject
                                recordRecentTopic(title, subject, null)
                                previousScreen = AppScreen.TEACHER_SUBJECT_CONFIG
                                currentScreen = AppScreen.PDF_VIEWER
                            }
                        )
                        AppScreen.TEACHER_UPLOAD_NOTES -> {
                            val activeTeacherSubjects = resolveTeacherSubjectItems(teacherSubjectsTaught)
                            val assignedClasses = if (selectedTeacherSubjectItem.name.equals("Math", ignoreCase = true)) {
                                listOf("S.3 Blue", "S.3 Green", "S.4 Green", "S.4 Blue")
                            } else {
                                listOf("S.4 Green", "S.4 Blue", "S.5 Blue", "S.5 Arts")
                            }
                            TeacherUploadNotesScreen(
                                subjectName = selectedTeacherSubjectItem.name,
                                subjectIcon = selectedTeacherSubjectItem.icon,
                                availableSubjects = activeTeacherSubjects,
                                assignedClasses = assignedClasses,
                                existingNote = teacherNoteToEdit,
                                onNavigateBack = {
                                    currentScreen = previousScreen
                                },
                                onSaveSuccess = { savedItem ->
                                    val nowStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                                    val resolvedDate = if (savedItem.dateUploaded.isBlank() || savedItem.dateUploaded.equals("Today", ignoreCase = true)) {
                                        nowStr
                                    } else {
                                        savedItem.dateUploaded
                                    }
                                    val resolvedSize = savedItem.fileSize.ifBlank { "2.4 MB" }
                                    val newUpload = TeacherUploadItem(
                                        id = savedItem.id.ifBlank { "upload-${System.currentTimeMillis()}" },
                                        title = savedItem.title,
                                        subject = savedItem.subject,
                                        targetClass = savedItem.targetClass,
                                        term = "Term 3",
                                        type = savedItem.type,
                                        dateUploaded = resolvedDate,
                                        fileSize = resolvedSize,
                                        downloadCount = 0,
                                        viewCount = 0,
                                        dueDate = savedItem.dueDate,
                                        status = if (savedItem.type.equals("Assignment", ignoreCase = true)) "Active Assignment" else "Published"
                                    )
                                    val existingIdx = teacherUploadsList.indexOfFirst { it.id == newUpload.id }
                                    teacherUploadsList = if (existingIdx >= 0) {
                                        teacherUploadsList.toMutableList().apply { set(existingIdx, newUpload) }
                                    } else {
                                        listOf(newUpload) + teacherUploadsList
                                    }
                                    val editingSnapshot = teacherNoteToEdit
                                    coroutineScope.launch {
                                        if (editingSnapshot != null) {
                                            SupabaseRepository.updateNoteUploadInSupabase(
                                                noteId = editingSnapshot.id,
                                                originalTitle = editingSnapshot.title,
                                                subjectName = editingSnapshot.subject,
                                                updatedTitle = savedItem.title,
                                                updatedTargetClass = savedItem.targetClass,
                                                updatedType = savedItem.type,
                                                updatedDueDate = savedItem.dueDate
                                            )
                                        } else {
                                            SupabaseRepository.uploadNoteAndFileToSupabase(
                                                uploadItem = savedItem.copy(
                                                    dateUploaded = resolvedDate,
                                                    fileSize = resolvedSize
                                                ),
                                                teacherIdCode = teacherIdCode,
                                                teacherName = teacherName,
                                                fileBytes = savedItem.fileBytes
                                            )
                                        }
                                    }
                                    currentScreen = previousScreen
                                }
                            )
                        }
                        AppScreen.TEACHER_UPLOADS -> TeacherUploadsScreen(
                            teacherName = teacherName,
                            initialUploads = teacherUploadsList,
                            onNavigateToHome = { currentScreen = AppScreen.TEACHER_HOME },
                            onNavigateToMySubjects = { currentScreen = AppScreen.TEACHER_MY_SUBJECTS },
                            onNavigateToUploads = { /* Already on Uploads */ },
                            onNavigateToUploadNotes = {
                                previousScreen = AppScreen.TEACHER_UPLOADS
                                teacherNoteToEdit = null
                                currentScreen = AppScreen.TEACHER_UPLOAD_NOTES
                            },
                            onNavigateToTeacherProfile = { currentScreen = AppScreen.TEACHER_PROFILE },
                            onOpenUpload = { title, subject ->
                                selectedPdfTitle = title
                                selectedPdfSubject = subject
                                recordRecentTopic(title, subject, null)
                                previousScreen = AppScreen.TEACHER_UPLOADS
                                currentScreen = AppScreen.PDF_VIEWER
                            }
                        )
                        AppScreen.TEACHER_PROFILE -> TeacherProfileScreen(
                            teacherName = teacherName,
                            subjectsTaught = teacherSubjectsTaught,
                            schoolName = teacherSchool,
                            onUpdateProfile = onSaveTeacherProfile,
                            onNavigateToHome = { currentScreen = AppScreen.TEACHER_HOME },
                            onNavigateToMySubjects = { currentScreen = AppScreen.TEACHER_MY_SUBJECTS },
                            onNavigateToUploads = { currentScreen = AppScreen.TEACHER_UPLOADS },
                            onNavigateToTeacherProfile = { /* Already on Profile */ },
                            onNavigateToAccount = { currentScreen = AppScreen.TEACHER_ACCOUNT },
                            onNavigateToHelpAndSupport = {
                                helpPreviousScreen = AppScreen.TEACHER_PROFILE
                                currentScreen = AppScreen.HELP_AND_SUPPORT
                            },
                            onNavigateToAbout = {
                                aboutPreviousScreen = AppScreen.TEACHER_PROFILE
                                currentScreen = AppScreen.ABOUT
                            },
                            onNavigateToPrivacyPolicy = {
                                privacyPreviousScreen = AppScreen.TEACHER_PROFILE
                                currentScreen = AppScreen.PRIVACY_POLICY
                            },
                            onNavigateToAdmin = {
                                adminPreviousScreen = AppScreen.TEACHER_PROFILE
                                currentScreen = AppScreen.ADMIN_HOME
                            },
                            onSwitchToStudentView = { currentScreen = AppScreen.PROFILE },
                            onLogout = {
                                prefs.edit().putBoolean("is_logged_in", false).apply()
                                coroutineScope.launch { SupabaseRepository.revokeCurrentSession() }
                                currentScreen = AppScreen.WELCOME
                            }
                        )
                        AppScreen.TEACHER_ACCOUNT -> TeacherAccountScreen(
                            teacherName = teacherName,
                            subjectsTaught = teacherSubjectsTaught,
                            schoolName = teacherSchool,
                            onNavigateBack = { currentScreen = AppScreen.TEACHER_PROFILE },
                            onUpdateProfile = onSaveTeacherProfile,
                            onSwitchToStudentView = { currentScreen = AppScreen.PROFILE }
                        )
                        AppScreen.ADMIN_HOME -> AdminHomeScreen(
                            revenueBalance = "0",
                            studentsCount = "0",
                            teachersCount = "0",
                            notesCount = "0",
                            assignmentsCount = "0",
                            affiliatesCount = "0",
                            onNavigateToPlans = {
                                currentScreen = AppScreen.ADMIN_PLANS
                            },
                            onNavigateToAccount = {
                                adminAccountPreviousScreen = AppScreen.ADMIN_HOME
                                currentScreen = AppScreen.ADMIN_ACCOUNT
                            },
                            onNavigateToAffiliate = {
                                adminAffiliatePreviousScreen = AppScreen.ADMIN_HOME
                                currentScreen = AppScreen.ADMIN_AFFILIATE
                            },
                            onNavigateToTransactions = {
                                adminTransactionsPreviousScreen = AppScreen.ADMIN_HOME
                                currentScreen = AppScreen.ADMIN_TRANSACTIONS
                            },
                            onNavigateToHome = { /* Already on Admin Home */ },
                            onNavigateToAnalysis = { currentScreen = AppScreen.ADMIN_ANALYSIS },
                            onNavigateToUsers = { currentScreen = AppScreen.ADMIN_USERS },
                            onNavigateToMore = { currentScreen = AppScreen.ADMIN_MORE },
                            onSwitchToStudentView = { currentScreen = AppScreen.HOME },
                            onSwitchToTeacherView = { currentScreen = AppScreen.TEACHER_HOME }
                        )
                        AppScreen.ADMIN_ANALYSIS -> AdminAnalysisScreen(
                            onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToHome = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToAnalysis = { /* Already on Analysis */ },
                            onNavigateToUsers = { currentScreen = AppScreen.ADMIN_USERS },
                            onNavigateToMore = { currentScreen = AppScreen.ADMIN_MORE }
                        )
                        AppScreen.ADMIN_USERS -> AdminUsersScreen(
                            onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToHome = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToAnalysis = { currentScreen = AppScreen.ADMIN_ANALYSIS },
                            onNavigateToUsers = { /* Already on Users */ },
                            onNavigateToMore = { currentScreen = AppScreen.ADMIN_MORE }
                        )
                        AppScreen.ADMIN_MORE -> AdminMoreScreen(
                            onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToHome = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToAnalysis = { currentScreen = AppScreen.ADMIN_ANALYSIS },
                            onNavigateToUsers = { currentScreen = AppScreen.ADMIN_USERS },
                            onNavigateToMore = { /* Already on More */ },
                            onNavigateToAccount = {
                                adminAccountPreviousScreen = AppScreen.ADMIN_MORE
                                currentScreen = AppScreen.ADMIN_ACCOUNT
                            },
                            onNavigateToTransactions = {
                                adminTransactionsPreviousScreen = AppScreen.ADMIN_MORE
                                currentScreen = AppScreen.ADMIN_TRANSACTIONS
                            },
                            onNavigateToSettings = { currentScreen = AppScreen.ADMIN_SETTINGS },
                            onNavigateToHealth = {
                                adminHealthPreviousScreen = AppScreen.ADMIN_MORE
                                currentScreen = AppScreen.ADMIN_HEALTH
                            },
                            onNavigateToAffiliate = {
                                adminAffiliatePreviousScreen = AppScreen.ADMIN_MORE
                                currentScreen = AppScreen.ADMIN_AFFILIATE
                            },
                            onNavigateToLogs = {
                                adminLogsPreviousScreen = AppScreen.ADMIN_MORE
                                currentScreen = AppScreen.ADMIN_LOGS
                            },
                            onLogout = {
                                prefs.edit().putBoolean("is_logged_in", false).apply()
                                coroutineScope.launch { SupabaseRepository.revokeCurrentSession() }
                                currentScreen = AppScreen.WELCOME
                            }
                        )
                        AppScreen.ADMIN_SETTINGS -> AdminSettingsScreen(
                            adminName = "Superadministrator",
                            onNavigateBack = { currentScreen = AppScreen.ADMIN_MORE }
                        )
                        AppScreen.ADMIN_HEALTH -> AdminHealthScreen(
                            onNavigateBack = { currentScreen = adminHealthPreviousScreen },
                            onNavigateToLogs = {
                                adminLogsPreviousScreen = AppScreen.ADMIN_HEALTH
                                currentScreen = AppScreen.ADMIN_LOGS
                            }
                        )
                        AppScreen.ADMIN_LOGS -> AdminLogsScreen(
                            onNavigateBack = { currentScreen = adminLogsPreviousScreen }
                        )
                        AppScreen.ADMIN_AFFILIATE -> AdminAffiliateScreen(
                            onNavigateBack = { currentScreen = adminAffiliatePreviousScreen },
                            onNavigateToHome = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToAnalysis = { currentScreen = AppScreen.ADMIN_ANALYSIS },
                            onNavigateToUsers = { currentScreen = AppScreen.ADMIN_USERS },
                            onNavigateToMore = { currentScreen = AppScreen.ADMIN_MORE }
                        )
                        AppScreen.ADMIN_ACCOUNT -> AdminAccountScreen(
                            initialBalance = "0",
                            initialTax = "0",
                            initialDb = "0",
                            onNavigateBack = { currentScreen = adminAccountPreviousScreen },
                            onNavigateToTransactions = {
                                adminTransactionsPreviousScreen = AppScreen.ADMIN_ACCOUNT
                                currentScreen = AppScreen.ADMIN_TRANSACTIONS
                            }
                        )
                        AppScreen.ADMIN_PLANS -> AdminPlansScreen(
                            onNavigateBack = { currentScreen = AppScreen.ADMIN_HOME }
                        )
                        AppScreen.ADMIN_TRANSACTIONS -> AdminTransactionsScreen(
                            onNavigateBack = { currentScreen = adminTransactionsPreviousScreen },
                            onNavigateToHome = { currentScreen = AppScreen.ADMIN_HOME },
                            onNavigateToAnalysis = { currentScreen = AppScreen.ADMIN_ANALYSIS },
                            onNavigateToUsers = { currentScreen = AppScreen.ADMIN_USERS },
                            onNavigateToMore = { currentScreen = AppScreen.ADMIN_MORE }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyApplicationTheme(dynamicColor = false) {
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SubjectsScreenPreview() {
    MyApplicationTheme(dynamicColor = false) {
        SubjectsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadsScreenPreview() {
    MyApplicationTheme(dynamicColor = false) {
        DownloadsScreen()
    }
}




