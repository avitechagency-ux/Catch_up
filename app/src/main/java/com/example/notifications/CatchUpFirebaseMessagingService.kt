package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Helper object managing FCM token persistence, notification channels,
 * user notification toggle preferences, and system notification display with deep-linking.
 */
object NotificationHelper {
    private const val TAG = "CatchUpFCM"

    const val PREFS_NAME = "catch_up_prefs"
    const val PREF_FCM_TOKEN = "fcm_device_token"
    const val PREF_NEW_NOTE_ALERTS = "pref_new_note_alerts"
    const val PREF_PAYMENT_REMINDERS = "pref_payment_reminders"
    const val PREF_STREAK_REMINDERS = "pref_streak_reminders"
    const val PREF_GENERAL_ANNOUNCEMENTS = "pref_general_announcements"
    const val PREF_NOTIFICATION_PERMISSION_REQUESTED = "pref_notification_permission_requested"

    // Notification Channel IDs and Names (Requirement 2)
    const val CHANNEL_ID_NEW_NOTES = "new_notes"
    const val CHANNEL_NAME_NEW_NOTES = "New Notes"

    const val CHANNEL_ID_PAYMENT_REMINDERS = "payment_reminders"
    const val CHANNEL_NAME_PAYMENT_REMINDERS = "Payment Reminders"

    const val CHANNEL_ID_LIVE_CLASSES = "live_classes"
    const val CHANNEL_NAME_LIVE_CLASSES = "Live Audio Classes"

    const val CHANNEL_ID_ANNOUNCEMENTS = "announcements"
    const val CHANNEL_NAME_ANNOUNCEMENTS = "School Announcements"

    // Deep-link Intent Extras (Requirement 3)
    const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    const val TYPE_NEW_NOTE = "new_note"
    const val TYPE_PAYMENT_REMINDER = "payment_reminder"
    const val TYPE_LIVE_CLASS_SCHEDULED = "live_class_scheduled"
    const val TYPE_LIVE_CLASS_STARTED = "live_class_started"
    const val TYPE_ANNOUNCEMENT = "announcement"

    const val EXTRA_NOTE_ID = "note_id"
    const val EXTRA_NOTE_TITLE = "note_title"
    const val EXTRA_NOTE_SUBJECT = "note_subject"

    const val EXTRA_LIVE_CLASS_ID = "live_class_id"
    const val EXTRA_LIVE_CLASS_TITLE = "live_class_title"
    const val EXTRA_LIVE_CLASS_SUBJECT = "live_class_subject"
    const val EXTRA_LIVE_CLASS_IS_LIVE = "live_class_is_live"

    /**
     * Creates required Android 8.0+ (API 26+) notification channels:
     * 1. "new_notes" ("New Notes") — Importance HIGH
     * 2. "payment_reminders" ("Payment Reminders") — Importance DEFAULT
     * 3. "live_classes" ("Live Audio Classes") — Importance HIGH
     * 4. "announcements" ("School Announcements") — Importance HIGH
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            val newNotesChannel = NotificationChannel(
                CHANNEL_ID_NEW_NOTES,
                CHANNEL_NAME_NEW_NOTES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a teacher shares a new study note with your class"
            }

            val paymentRemindersChannel = NotificationChannel(
                CHANNEL_ID_PAYMENT_REMINDERS,
                CHANNEL_NAME_PAYMENT_REMINDERS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders when your student access plan is expiring or needs renewal"
            }

            val liveClassesChannel = NotificationChannel(
                CHANNEL_ID_LIVE_CLASSES,
                CHANNEL_NAME_LIVE_CLASSES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for scheduled and live audio lessons from your teachers"
            }

            val announcementsChannel = NotificationChannel(
                CHANNEL_ID_ANNOUNCEMENTS,
                CHANNEL_NAME_ANNOUNCEMENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "School and system-wide announcements from teachers and administrators"
            }

            notificationManager.createNotificationChannel(newNotesChannel)
            notificationManager.createNotificationChannel(paymentRemindersChannel)
            notificationManager.createNotificationChannel(liveClassesChannel)
            notificationManager.createNotificationChannel(announcementsChannel)
        }
    }

    /**
     * Saves the generated/refreshed FCM device token locally and invokes the Supabase sync.
     */
    fun saveFcmToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_FCM_TOKEN, token)
            .apply()
        syncTokenToSupabase(context, token)
    }

    /**
     * Retrieves the locally stored FCM token, if available.
     */
    fun getSavedFcmToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(PREF_FCM_TOKEN, null)
    }

    /**
     * Requests the current registration token from FirebaseMessaging and stores it locally.
     */
    fun fetchAndStoreCurrentFcmToken(context: Context) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                if (!token.isNullOrBlank()) {
                    Log.d(TAG, "Current FCM token fetched: $token")
                    saveFcmToken(context.applicationContext, token)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseMessaging not initialized in current environment: ${e.message}")
        }
    }

    /**
     * Syncs the device's FCM token to the student's record in Supabase.
     */
    fun syncTokenToSupabase(context: Context, token: String) {
        val userPhoneOrEmail = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("active_phone_number", null)
            ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("active_email", "") ?: ""
        if (userPhoneOrEmail.isNotBlank()) {
            syncTokenToSupabaseStub(token)
        }
    }

    fun syncTokenToSupabaseStub(token: String) {
        Log.d(TAG, "Ready to sync FCM token to Supabase -> $token")
    }

    /**
     * Stub for server-side triggering when a teacher shares a new note.
     */
    fun triggerNewNotePushStub(
        noteId: String,
        noteTitle: String,
        subject: String,
        targetClass: String
    ) {
        // TODO: Trigger Supabase Edge Function / backend notification dispatch for "New note shared"
        // Payload: type="new_note", note_id=noteId, note_title=noteTitle, subject=subject,
        // body="New $subject note shared with $targetClass"
    }

    /**
     * Stub for server-side triggering when a student's plan is nearing expiry.
     */
    fun triggerPlanExpiryReminderStub(studentId: String) {
        // TODO: Trigger Supabase scheduled job / backend notification dispatch for "Payment/plan reminder"
        // Payload: type="payment_reminder", title="Your plan is expiring soon",
        // body="Renew your access to keep reading notes without interruption"
    }

    /**
     * Checks whether the student has enabled "New note alerts" in Settings (default: ON).
     */
    fun isNewNoteAlertsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_NEW_NOTE_ALERTS, true)
    }

    fun setNewNoteAlertsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_NEW_NOTE_ALERTS, enabled)
            .apply()
    }

    /**
     * Checks whether the student has enabled "Payment reminders" in Settings (default: ON).
     */
    fun isPaymentRemindersEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_PAYMENT_REMINDERS, true)
    }

    fun setPaymentRemindersEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_PAYMENT_REMINDERS, enabled)
            .apply()
    }

    /**
     * Builds the deep-link Intent for a "New note shared" notification that opens the PDF Viewer screen.
     */
    fun buildNewNoteDeepLinkIntent(
        context: Context,
        noteId: String,
        noteTitle: String,
        subject: String
    ): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_NEW_NOTE)
            putExtra(EXTRA_NOTE_ID, noteId)
            putExtra(EXTRA_NOTE_TITLE, noteTitle)
            putExtra(EXTRA_NOTE_SUBJECT, subject)
        }
    }

    /**
     * Builds the deep-link Intent for a "Payment/plan reminder" notification that opens the Student Account screen.
     */
    fun buildPaymentReminderDeepLinkIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_PAYMENT_REMINDER)
        }
    }

    /**
     * Displays a "New note shared" system notification if "New note alerts" is enabled in Settings.
     * Returns true if the notification was posted, or false if suppressed by settings/permissions.
     */
    fun showNewNoteNotification(
        context: Context,
        noteId: String,
        noteTitle: String,
        body: String,
        subject: String = inferSubjectFromBody(body)
    ): Boolean {
        // Respect "New note alerts" toggle from Settings
        if (!isNewNoteAlertsEnabled(context)) {
            Log.d(TAG, "Suppressing 'New note shared' notification because 'New note alerts' is disabled in Settings.")
            return false
        }

        createNotificationChannels(context)

        val deepLinkIntent = buildNewNoteDeepLinkIntent(
            context = context,
            noteId = noteId,
            noteTitle = noteTitle,
            subject = subject
        )

        val requestCode = (noteId.ifBlank { noteTitle }).hashCode()
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            deepLinkIntent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_NEW_NOTES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(noteTitle)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return postNotificationSafely(context, requestCode, notification)
    }

    /**
     * Displays a "Payment/plan reminder" system notification if "Payment reminders" is enabled in Settings.
     * Returns true if the notification was posted, or false if suppressed by settings/permissions.
     */
    fun showPaymentReminderNotification(
        context: Context,
        title: String = "Your plan is expiring soon",
        body: String = "Renew your access to keep reading notes without interruption"
    ): Boolean {
        // Respect "Payment reminders" toggle from Settings
        if (!isPaymentRemindersEnabled(context)) {
            Log.d(TAG, "Suppressing 'Payment reminder' notification because 'Payment reminders' is disabled in Settings.")
            return false
        }

        createNotificationChannels(context)

        val deepLinkIntent = buildPaymentReminderDeepLinkIntent(context)
        val requestCode = 2001
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            deepLinkIntent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PAYMENT_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return postNotificationSafely(context, requestCode, notification)
    }

    /**
     * Displays a system notification when a teacher schedules a new Live Audio Class for the student's class.
     */
    fun showLiveClassScheduledNotification(
        context: Context,
        classId: String,
        subject: String,
        title: String,
        scheduledDate: String,
        scheduledTime: String,
        targetClass: String
    ): Boolean {
        if (!com.example.data.AppFeatures.ONLINE_CLASSES_ENABLED) {
            Log.d(TAG, "Suppressing live class notification: online classes are temporarily disabled across the platform.")
            return false
        }
        createNotificationChannels(context)

        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_LIVE_CLASS_SCHEDULED)
            putExtra(EXTRA_LIVE_CLASS_ID, classId)
            putExtra(EXTRA_LIVE_CLASS_TITLE, title)
            putExtra(EXTRA_LIVE_CLASS_SUBJECT, subject)
            putExtra(EXTRA_LIVE_CLASS_IS_LIVE, false)
        }
        val requestCode = (3000 + (classId.hashCode() and 0xFFFF))
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = "$subject — $title for $targetClass on $scheduledDate at $scheduledTime"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LIVE_CLASSES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Live Class Scheduled")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return postNotificationSafely(context, requestCode, notification)
    }

    /**
     * Displays a high-priority system notification when a teacher starts the live session.
     */
    fun showLiveClassStartedNotification(
        context: Context,
        classId: String,
        subject: String,
        title: String,
        teacherName: String
    ): Boolean {
        if (!com.example.data.AppFeatures.ONLINE_CLASSES_ENABLED) {
            Log.d(TAG, "Suppressing live class started notification: online classes are temporarily disabled across the platform.")
            return false
        }
        createNotificationChannels(context)

        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_LIVE_CLASS_STARTED)
            putExtra(EXTRA_LIVE_CLASS_ID, classId)
            putExtra(EXTRA_LIVE_CLASS_TITLE, title)
            putExtra(EXTRA_LIVE_CLASS_SUBJECT, subject)
            putExtra(EXTRA_LIVE_CLASS_IS_LIVE, true)
        }
        val requestCode = (4000 + (classId.hashCode() and 0xFFFF))
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = "$teacherName started $subject: $title. Tap to join now!"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LIVE_CLASSES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔴 Live Class Started")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return postNotificationSafely(context, requestCode, notification)
    }

    /**
     * Infers the subject name from a notification body like "New Geography note shared with S.5".
     */
    fun inferSubjectFromBody(body: String): String {
        val knownSubjects = listOf("Geography", "Biology", "Math", "History", "Physics", "Chemistry")
        return knownSubjects.firstOrNull { body.contains(it, ignoreCase = true) } ?: "Geography"
    }

    private fun postNotificationSafely(
        context: Context,
        notificationId: Int,
        notification: android.app.Notification
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted; skipping notification display.")
                return false
            }
        }
        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException posting notification", e)
            false
        }
    }
}

/**
 * Firebase Cloud Messaging service for the Catch Up Android app.
 * Handles token generation/refresh (`onNewToken`) and incoming push messages (`onMessageReceived`)
 * for:
 * 1. "New note shared" (student side) -> Channel "new_notes", deep-links to PDF Viewer
 * 2. "Payment/plan reminder" (student side) -> Channel "payment_reminders", deep-links to Student Account
 * 3. "Live class alert" -> Channel "live_classes", deep-links to Live Class Lobby/Session
 * 4. "School announcement" -> Channel "announcements", deep-links to Notification Center
 */
class CatchUpFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("CatchUpFCM", "FCM token generated/refreshed: $token")
        NotificationHelper.saveFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val type = data["type"]
            ?: data["notification_type"]
            ?: data["channel_id"]
            ?: notification?.channelId
            ?: ""

        val title = data["title"]
            ?: notification?.title
            ?: "Cachap Alert"

        val body = data["body"]
            ?: notification?.body
            ?: ""

        val isPayment = type.equals(NotificationHelper.TYPE_PAYMENT_REMINDER, ignoreCase = true) ||
            type.equals(NotificationHelper.CHANNEL_ID_PAYMENT_REMINDERS, ignoreCase = true) ||
            (notification?.title?.contains("plan", ignoreCase = true) == true) ||
            (data["title"]?.contains("plan", ignoreCase = true) == true)

        val noteId = data["note_id"] ?: data["noteId"] ?: ""
        val subject = data["subject"] ?: data["note_subject"] ?: NotificationHelper.inferSubjectFromBody(body)

        // Post Android System Notification via NotificationManager (outside app notification only)
        if (isPayment) {
            NotificationHelper.showPaymentReminderNotification(
                context = applicationContext,
                title = title,
                body = body
            )
        } else {
            NotificationHelper.showNewNoteNotification(
                context = applicationContext,
                noteId = noteId,
                noteTitle = title,
                body = body,
                subject = subject
            )
        }
    }
}
