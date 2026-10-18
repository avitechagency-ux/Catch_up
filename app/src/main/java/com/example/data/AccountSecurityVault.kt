package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.AppScreen

/**
 * Top maximum security vault and role-based access control (RBAC) engine for Cachap accounts.
 *
 * Guarantees zero-leak isolation between Teacher, Student, and Admin accounts:
 * 1. Strict Screen Authorization: No teacher can ever be routed to or view student panels,
 *    and no student can access teacher or administrative controls.
 * 2. Safe App Closing & Root Navigation: Closing the app or pressing back from a role's home screen
 *    (e.g., TEACHER_HOME) will never redirect to another role's profile or dashboard.
 * 3. Session Sanitization: Ensures persisted state from another role (e.g., student name or plan)
 *    never cross-contaminates an active teacher session, and clears sensitive data on logout.
 * 4. Strict Student-Teacher Isolation: Teachers only ever see students explicitly connected
 *    to their unique 6-character Teacher ID code. Never falls back to unlinked students.
 */
object AccountSecurityVault {
    private const val TAG = "AccountSecurityVault"

    enum class UserRole {
        STUDENT,
        TEACHER,
        ADMIN,
        UNAUTHENTICATED
    }

    val UNAUTHENTICATED_SCREENS: Set<AppScreen> = setOf(
        AppScreen.ONBOARDING,
        AppScreen.WELCOME,
        AppScreen.LOGIN,
        AppScreen.SIGNUP
    )

    val STUDENT_EXCLUSIVE_SCREENS: Set<AppScreen> = setOf(
        AppScreen.HOME,
        AppScreen.SUBJECTS,
        AppScreen.SUBJECT_NOTES,
        AppScreen.DOWNLOADS,
        AppScreen.PROFILE,
        AppScreen.ACCOUNT,
        AppScreen.STUDENT_PAYMENT,
        AppScreen.STUDENT_LIVE_CLASSES,
        AppScreen.STUDENT_LIVE_SESSION
    )

    val TEACHER_EXCLUSIVE_SCREENS: Set<AppScreen> = setOf(
        AppScreen.TEACHER_HOME,
        AppScreen.TEACHER_MY_SUBJECTS,
        AppScreen.TEACHER_SUBJECT_CONFIG,
        AppScreen.TEACHER_UPLOAD_NOTES,
        AppScreen.TEACHER_UPLOADS,
        AppScreen.TEACHER_PROFILE,
        AppScreen.TEACHER_ACCOUNT,
        AppScreen.TEACHER_STUDENTS_CLASSES,
        AppScreen.TEACHER_CLASS_STREAMS,
        AppScreen.TEACHER_STREAM_STUDENTS,
        AppScreen.TEACHER_STUDENT_DETAIL,
        AppScreen.TEACHER_LIVE_CLASSES,
        AppScreen.CREATE_LIVE_CLASS,
        AppScreen.TEACHER_LIVE_SESSION
    )

    val ADMIN_EXCLUSIVE_SCREENS: Set<AppScreen> = setOf(
        AppScreen.ADMIN_HOME,
        AppScreen.ADMIN_ANALYSIS,
        AppScreen.ADMIN_USERS,
        AppScreen.ADMIN_MORE,
        AppScreen.ADMIN_ACCOUNT,
        AppScreen.ADMIN_PLANS,
        AppScreen.ADMIN_TRANSACTIONS,
        AppScreen.ADMIN_SETTINGS,
        AppScreen.ADMIN_HEALTH,
        AppScreen.ADMIN_LOGS
    )

    val COMMON_AUTHENTICATED_SCREENS: Set<AppScreen> = setOf(
        AppScreen.PDF_VIEWER,
        AppScreen.SETTINGS,
        AppScreen.HELP_AND_SUPPORT,
        AppScreen.ABOUT,
        AppScreen.PRIVACY_POLICY
    )

    fun parseUserRole(roleString: String?): UserRole {
        val clean = roleString?.trim()?.lowercase() ?: return UserRole.STUDENT
        return when (clean) {
            "teacher" -> UserRole.TEACHER
            "admin" -> UserRole.ADMIN
            "student" -> UserRole.STUDENT
            else -> UserRole.STUDENT
        }
    }

    fun getDefaultScreenForRole(role: UserRole): AppScreen {
        return when (role) {
            UserRole.TEACHER -> AppScreen.TEACHER_HOME
            UserRole.ADMIN -> AppScreen.ADMIN_HOME
            UserRole.STUDENT -> AppScreen.HOME
            UserRole.UNAUTHENTICATED -> AppScreen.WELCOME
        }
    }

    fun getRootScreenForRole(role: UserRole): AppScreen {
        return getDefaultScreenForRole(role)
    }

    fun isRootScreen(screen: AppScreen, role: UserRole): Boolean {
        return when (role) {
            UserRole.TEACHER -> screen == AppScreen.TEACHER_HOME
            UserRole.ADMIN -> screen == AppScreen.ADMIN_HOME
            UserRole.STUDENT -> screen == AppScreen.HOME
            UserRole.UNAUTHENTICATED -> screen == AppScreen.WELCOME || screen == AppScreen.ONBOARDING
        }
    }

    /**
     * Checks if a screen is strictly authorized for the given role and login state.
     */
    fun isScreenAuthorized(screen: AppScreen, role: UserRole, isLoggedIn: Boolean): Boolean {
        if (!isLoggedIn) {
            return screen in UNAUTHENTICATED_SCREENS
        }

        // Once logged in, onboarding or welcome screens are not accessible
        if (screen == AppScreen.ONBOARDING || screen == AppScreen.WELCOME) {
            return false
        }

        return when (role) {
            UserRole.TEACHER -> {
                screen in TEACHER_EXCLUSIVE_SCREENS || screen in COMMON_AUTHENTICATED_SCREENS
            }
            UserRole.ADMIN -> {
                screen in ADMIN_EXCLUSIVE_SCREENS || screen in COMMON_AUTHENTICATED_SCREENS
            }
            UserRole.STUDENT -> {
                screen in STUDENT_EXCLUSIVE_SCREENS || screen in COMMON_AUTHENTICATED_SCREENS
            }
            UserRole.UNAUTHENTICATED -> screen in UNAUTHENTICATED_SCREENS
        }
    }

    /**
     * Sanitizes a requested screen target to guarantee it strictly belongs to the current user's role.
     * If unauthorized or mismatched, falls back to the role's default home screen.
     */
    fun sanitizeScreen(targetScreen: AppScreen?, role: UserRole, isLoggedIn: Boolean): AppScreen {
        val defaultScreen = if (isLoggedIn) getDefaultScreenForRole(role) else AppScreen.WELCOME
        if (targetScreen == null) return defaultScreen

        return if (isScreenAuthorized(targetScreen, role, isLoggedIn)) {
            targetScreen
        } else {
            Log.w(
                TAG,
                "Security violation blocked: Attempted navigation to $targetScreen by role=$role (isLoggedIn=$isLoggedIn). Redirected to $defaultScreen."
            )
            defaultScreen
        }
    }

    /**
     * Sanitizes a persisted screen name from SharedPreferences against the active user's role.
     */
    fun sanitizeScreenByName(screenName: String?, role: UserRole, isLoggedIn: Boolean): AppScreen {
        val parsed = if (!screenName.isNullOrBlank()) {
            try {
                AppScreen.valueOf(screenName)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
        return sanitizeScreen(parsed, role, isLoggedIn)
    }

    /**
     * Clears cross-role sensitive session state when switching roles or logging out.
     */
    fun clearSessionOnLogout(prefs: SharedPreferences) {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_role")
            .remove("last_active_screen")
            .remove("student_linked_subject_teachers")
            .remove("teacher_connected_students")
            .apply()
        SupabaseSecurityVault.clearSessionToken()
    }

    /**
     * Isolates teacher connected students strictly to those whose teacherIdCode matches the active teacher.
     * Never falls back to exposing unrelated students from other teachers.
     */
    fun <T> filterStudentsByTeacherCode(
        students: List<T>,
        teacherIdCode: String,
        getCode: (T) -> String
    ): List<T> {
        val cleanCode = teacherIdCode.trim().uppercase()
        if (cleanCode.isBlank()) return emptyList()
        return students.filter { getCode(it).trim().uppercase() == cleanCode }
    }
}
