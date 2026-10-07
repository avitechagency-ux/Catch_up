package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model representing a Live Audio Class session in Catch Up.
 * Table: `public.live_classes`
 */
@JsonClass(generateAdapter = true)
data class LiveClassDto(
    @Json(name = "id") val id: String,
    @Json(name = "school_id") val schoolId: String = "UG_SEC_01",
    @Json(name = "teacher_id") val teacherId: String,
    @Json(name = "teacher_name") val teacherName: String = "Teacher",
    @Json(name = "class_id") val classId: String, // e.g. "S.1", "S.4"
    @Json(name = "target_streams") val targetStreams: String = "ALL", // "ALL" or "NORTH,EAST"
    @Json(name = "subject_id") val subjectId: String, // e.g. "Physics", "Geography"
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "scheduled_date") val scheduledDate: String, // YYYY-MM-DD
    @Json(name = "scheduled_start_time") val scheduledStartTime: String, // HH:mm
    @Json(name = "duration_minutes") val durationMinutes: Int = 45,
    @Json(name = "status") val status: String = "scheduled", // scheduled, live, completed, cancelled
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "started_at") val startedAt: String? = null,
    @Json(name = "ended_at") val endedAt: String? = null
)

/**
 * Model representing a participant connected or enrolled in a Live Class.
 * Table: `public.live_class_participants`
 */
@JsonClass(generateAdapter = true)
data class LiveClassParticipantDto(
    @Json(name = "id") val id: String,
    @Json(name = "live_class_id") val liveClassId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String = "Student",
    @Json(name = "role") val role: String = "student", // teacher, student
    @Json(name = "joined_at") val joinedAt: String = "",
    @Json(name = "left_at") val leftAt: String? = null,
    @Json(name = "is_connected") val isConnected: Boolean = true,
    @Json(name = "is_muted") val isMuted: Boolean = true,
    @Json(name = "removed_at") val removedAt: String? = null
)

/**
 * Model representing a student hand-raising event.
 * Table: `public.live_class_hand_raises`
 */
@JsonClass(generateAdapter = true)
data class LiveClassHandRaiseDto(
    @Json(name = "id") val id: String,
    @Json(name = "live_class_id") val liveClassId: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "student_name") val studentName: String = "Student",
    @Json(name = "status") val status: String = "raised", // raised, approved, lowered, rejected
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "handled_at") val handledAt: String? = null
)

/**
 * Model representing a real-time reaction sent by a student or teacher.
 * Table: `public.live_class_reactions`
 */
@JsonClass(generateAdapter = true)
data class LiveClassReactionDto(
    @Json(name = "id") val id: String,
    @Json(name = "live_class_id") val liveClassId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String = "Student",
    @Json(name = "reaction") val reaction: String, // 👍, ❤️, 👏, 🤔, 🙋
    @Json(name = "created_at") val createdAt: String = ""
)

/**
 * Model for WebRTC signaling messages exchanged through Supabase.
 * Table: `public.live_class_signaling`
 */
@JsonClass(generateAdapter = true)
data class LiveClassSignalingDto(
    @Json(name = "id") val id: String,
    @Json(name = "live_class_id") val liveClassId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "recipient_id") val recipientId: String? = null,
    @Json(name = "type") val type: String, // offer, answer, ice_candidate, peer_joined, peer_left, mute_state, speaking_state, session_state
    @Json(name = "payload") val payload: String, // JSON payload containing SDP or ICE details
    @Json(name = "created_at") val createdAt: String = ""
)

/**
 * Active connection states for Live Classes.
 */
enum class LiveConnectionState {
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTED
}

/**
 * Speaking permission state for a student.
 */
enum class SpeakingPermission {
    MUTED_LISTENING,
    HAND_RAISED,
    ALLOWED_TO_SPEAK,
    REJECTED
}
