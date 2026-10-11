package com.example.data

/**
 * Central Feature Flags configuration for the Catch Up platform.
 *
 * Provides single-source-of-truth toggles for major platform modules.
 */
object AppFeatures {
    /**
     * Temporarily disables the online classroom / live audio classes across Catch Up.
     *
     * In accordance with platform governance:
     * - The underlying implementation (screens, WebRTC audio logic, database tables, models)
     *   is fully preserved in the codebase for future reactivation.
     * - Live classes are hidden from Student and Teacher home dashboards, cards, menus, and shortcuts.
     * - Direct navigation, deep links, and backstack transitions to online class routes are prevented.
     * - Teachers and students cannot create, join, schedule, or start online sessions.
     * - Notifications and background jobs do not promote or dispatch live class alerts.
     * - Platform administrators can monitor the feature's status ("Disabled") under Admin Settings.
     */
    const val ONLINE_CLASSES_ENABLED: Boolean = false
}
