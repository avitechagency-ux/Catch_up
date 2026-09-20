package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.CachedNote
import com.example.ui.DownloadsScreen
import com.example.ui.HomeScreen
import com.example.ui.PdfViewerScreen
import com.example.ui.ProfileScreen
import com.example.ui.RecentNoteData
import com.example.ui.StreakData
import com.example.ui.SubjectNotesScreen
import com.example.ui.SubjectsScreen
import com.example.ui.evaluateDayRollover
import com.example.ui.pruneExpiredDownloads
import com.example.ui.recordSessionTime
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    HOME,
    SUBJECTS,
    SUBJECT_NOTES,
    PDF_VIEWER,
    DOWNLOADS,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("catch_up_prefs", Context.MODE_PRIVATE) }
                val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

                // Load persisted streak data or initialize with 6 days, 42 minutes today
                var streakData by remember {
                    val savedDays = prefs.getInt("streak_days", 6)
                    val savedMillis = prefs.getLong("streak_millis_today", 42L * 60L * 1000L)
                    val savedDate = prefs.getString("streak_last_active_date", todayStr) ?: todayStr
                    val savedCompleted = prefs.getBoolean("streak_today_completed", savedMillis >= 60L * 60L * 1000L)
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

                // Background & active session time tracking
                var sessionStartTime by remember { mutableStateOf(System.currentTimeMillis()) }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                sessionStartTime = System.currentTimeMillis()
                                // Check for day rollover if resumed past midnight
                                val currentDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                streakData = evaluateDayRollover(streakData, currentDay)
                                persistStreak(streakData)
                            }
                            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                                val elapsed = System.currentTimeMillis() - sessionStartTime
                                if (elapsed > 0) {
                                    val updated = recordSessionTime(streakData, elapsed)
                                    streakData = updated
                                    persistStreak(updated)
                                }
                                sessionStartTime = System.currentTimeMillis()
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        val elapsed = System.currentTimeMillis() - sessionStartTime
                        if (elapsed > 0) {
                            val updated = recordSessionTime(streakData, elapsed)
                            persistStreak(updated)
                        }
                    }
                }

                // Continuous ticker: accumulates 1 second every second while active
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000L)
                        val elapsed = System.currentTimeMillis() - sessionStartTime
                        sessionStartTime = System.currentTimeMillis()
                        val updated = recordSessionTime(streakData, elapsed)
                        streakData = updated
                        persistStreak(updated)
                    }
                }

                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var previousScreen by remember { mutableStateOf(AppScreen.HOME) }
                var selectedSubjectName by remember { mutableStateOf("Math") }
                var selectedPdfTitle by remember { mutableStateOf("Algebra") }
                var selectedPdfSubject by remember { mutableStateOf("Math") }

                var studentName by remember {
                    mutableStateOf(prefs.getString("student_name", "Sarah Ssenyonga") ?: "Sarah Ssenyonga")
                }
                var schoolName by remember {
                    mutableStateOf(prefs.getString("student_school", "Jjungo Senior Secondary School") ?: "Jjungo Senior Secondary School")
                }
                var academicClass by remember {
                    mutableStateOf(prefs.getString("student_class", "S.5 Blue") ?: "S.5 Blue")
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

                var recentNotesList by remember {
                    mutableStateOf(
                        listOf(
                            RecentNoteData(
                                title = "Population Density",
                                subject = "Geography",
                                date = "18/09/2026"
                            ),
                            RecentNoteData(
                                title = "Cell Division",
                                subject = "Biology",
                                date = "16/09/2026"
                            ),
                            RecentNoteData(
                                title = "Algebra",
                                subject = "Math",
                                date = "15/09/2026"
                            )
                        )
                    )
                }

                var downloadsList by remember {
                    mutableStateOf(
                        listOf(
                            CachedNote(
                                id = "1",
                                title = "Algebra",
                                subject = "Math",
                                dateAdded = "18/09/2026",
                                lastAccessedTimestamp = System.currentTimeMillis()
                            ),
                            CachedNote(
                                id = "2",
                                title = "Physical Geography of East Africa",
                                subject = "Geography",
                                dateAdded = "17/09/2026",
                                lastAccessedTimestamp = System.currentTimeMillis()
                            )
                        )
                    )
                }

                val recordRecentTopic: (String, String, String?) -> Unit = { title, subject, date ->
                    val newRecent = RecentNoteData(
                        title = title,
                        subject = subject,
                        date = date ?: "19/09/2026"
                    )
                    val updated = listOf(newRecent) + recentNotesList.filterNot {
                        it.title.equals(title, ignoreCase = true) && it.subject.equals(subject, ignoreCase = true)
                    }
                    recentNotesList = updated.take(3)
                }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = when (currentScreen) {
                        AppScreen.PDF_VIEWER -> previousScreen
                        AppScreen.SUBJECT_NOTES -> AppScreen.SUBJECTS
                        AppScreen.DOWNLOADS -> AppScreen.HOME
                        AppScreen.SUBJECTS -> AppScreen.HOME
                        AppScreen.PROFILE -> AppScreen.HOME
                        else -> AppScreen.HOME
                    }
                }

                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        AppScreen.HOME -> HomeScreen(
                            studentName = studentName,
                            streakData = streakData,
                            recentNotes = recentNotesList,
                            onNavigateToSubjects = {
                                currentScreen = AppScreen.SUBJECTS
                            },
                            onNavigateToDownloads = {
                                currentScreen = AppScreen.DOWNLOADS
                            },
                            onNavigateToProfile = {
                                currentScreen = AppScreen.PROFILE
                            },
                            onNavigateToSubjectDetail = { subject ->
                                selectedSubjectName = subject
                                currentScreen = AppScreen.SUBJECT_NOTES
                            },
                            onOpenNote = { title, subject ->
                                selectedPdfTitle = title
                                selectedPdfSubject = subject
                                recordRecentTopic(title, subject, null)
                                previousScreen = AppScreen.HOME
                                currentScreen = AppScreen.PDF_VIEWER
                            }
                        )
                        AppScreen.SUBJECTS -> SubjectsScreen(
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
                                currentScreen = AppScreen.SUBJECT_NOTES
                            }
                        )
                        AppScreen.SUBJECT_NOTES -> SubjectNotesScreen(
                            subjectName = selectedSubjectName,
                            onNavigateBack = {
                                currentScreen = AppScreen.SUBJECTS
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
                            onTopicClick = { topic ->
                                selectedPdfTitle = topic.title
                                selectedPdfSubject = selectedSubjectName
                                recordRecentTopic(topic.title, selectedSubjectName, topic.date)
                                previousScreen = AppScreen.SUBJECT_NOTES
                                currentScreen = AppScreen.PDF_VIEWER
                            }
                        )
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
                            onNavigateToHome = {
                                currentScreen = AppScreen.HOME
                            },
                            onNavigateToSubjects = {
                                currentScreen = AppScreen.SUBJECTS
                            },
                            onNavigateToDownloads = {
                                currentScreen = AppScreen.DOWNLOADS
                            },
                            onUpdateProfile = onSaveProfile
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




