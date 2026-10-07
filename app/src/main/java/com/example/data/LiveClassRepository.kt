package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Repository managing Live Audio Classes, Participants, Speaking Requests, Reactions, and Signaling.
 */
object LiveClassRepository {
    private const val TAG = "LiveClassRepo"
    private const val PREFS_NAME = "live_classes_prefs"

    // In-memory cache for fast responsive UI and offline fallback
    private val localLiveClasses = mutableListOf<LiveClassDto>()
    private val localParticipants = mutableMapOf<String, MutableList<LiveClassParticipantDto>>() // classId -> list
    private val localHandRaises = mutableMapOf<String, MutableList<LiveClassHandRaiseDto>>() // classId -> list
    private val localReactions = mutableMapOf<String, MutableList<LiveClassReactionDto>>() // classId -> list
    private val localSignaling = mutableMapOf<String, MutableList<LiveClassSignalingDto>>() // classId -> list

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        loadPersistedClasses()
    }

    private fun loadPersistedClasses() {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString("cached_classes_json", null)
        if (!rawJson.isNullOrBlank()) {
            runCatching {
                val moshi = com.squareup.moshi.Moshi.Builder().build()
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, LiveClassDto::class.java)
                val adapter = moshi.adapter<List<LiveClassDto>>(listType)
                val loaded = adapter.fromJson(rawJson)
                if (loaded != null) {
                    synchronized(localLiveClasses) {
                        localLiveClasses.clear()
                        localLiveClasses.addAll(loaded)
                    }
                }
            }
        }

        // Initialize with default sample upcoming classes if empty
        if (localLiveClasses.isEmpty()) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val sampleClass1 = LiveClassDto(
                id = "live-sample-1",
                schoolId = "UG_SEC_01",
                teacherId = "tr-physics-01",
                teacherName = "Mr. Ssempa",
                classId = "S.4",
                subjectId = "Physics",
                title = "Electromagnetic Induction & Faraday's Law",
                description = "Live audio discussion and Q&A on electromagnetic induction principles for UNEB preparation.",
                scheduledDate = todayStr,
                scheduledStartTime = "15:00",
                durationMinutes = 45,
                status = "scheduled",
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            val sampleClass2 = LiveClassDto(
                id = "live-sample-2",
                schoolId = "UG_SEC_01",
                teacherId = "tr-geo-02",
                teacherName = "Mrs. Namusoke",
                classId = "S.3",
                subjectId = "Geography",
                title = "Weather & Climate: Rainfall Patterns in East Africa",
                description = "Understanding relief and convectional rainfall mechanisms across the Lake Victoria basin.",
                scheduledDate = todayStr,
                scheduledStartTime = "16:30",
                durationMinutes = 45,
                status = "scheduled",
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            synchronized(localLiveClasses) {
                localLiveClasses.add(sampleClass1)
                localLiveClasses.add(sampleClass2)
            }
            persistClassesLocally()
        }
    }

    private fun persistClassesLocally() {
        val ctx = appContext ?: return
        runCatching {
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, LiveClassDto::class.java)
            val adapter = moshi.adapter<List<LiveClassDto>>(listType)
            val json = adapter.toJson(localLiveClasses)
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("cached_classes_json", json)
                .apply()
        }
    }

    /**
     * Schedules and persists a new Live Audio Class.
     */
    suspend fun createLiveClass(liveClass: LiveClassDto): Result<LiveClassDto> = withContext(Dispatchers.IO) {
        val resolvedId = if (liveClass.id.isBlank()) UUID.randomUUID().toString() else liveClass.id
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val finalClass = liveClass.copy(
            id = resolvedId,
            createdAt = if (liveClass.createdAt.isBlank()) nowStr else liveClass.createdAt
        )

        // Cache locally first
        synchronized(localLiveClasses) {
            localLiveClasses.removeAll { it.id == finalClass.id }
            localLiveClasses.add(0, finalClass)
        }
        persistClassesLocally()

        // Sync to Supabase
        runCatching {
            val response = SupabaseRepository.api.insertLiveClass(finalClass)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val remoteCreated = response.body()!!.first()
                synchronized(localLiveClasses) {
                    localLiveClasses.removeAll { it.id == remoteCreated.id }
                    localLiveClasses.add(0, remoteCreated)
                }
                persistClassesLocally()
                return@withContext Result.success(remoteCreated)
            }
        }.onFailure { e ->
            Log.w(TAG, "Supabase createLiveClass sync failed, using local: ${e.message}")
        }

        Result.success(finalClass)
    }

    /**
     * Fetches Live Classes filtered by target academic class, teacher, or status.
     */
    suspend fun fetchLiveClasses(
        classId: String? = null,
        teacherId: String? = null,
        status: String? = null
    ): List<LiveClassDto> = withContext(Dispatchers.IO) {
        // Try fetching remote
        runCatching {
            val response = SupabaseRepository.api.getLiveClasses(
                classFilter = if (!classId.isNullOrBlank()) "eq.$classId" else null,
                teacherFilter = if (!teacherId.isNullOrBlank()) "eq.$teacherId" else null,
                statusFilter = if (!status.isNullOrBlank()) "eq.$status" else null
            )
            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!
                if (remoteList.isNotEmpty()) {
                    synchronized(localLiveClasses) {
                        remoteList.forEach { remoteItem ->
                            localLiveClasses.removeAll { it.id == remoteItem.id }
                            localLiveClasses.add(remoteItem)
                        }
                    }
                    persistClassesLocally()
                }
            }
        }.onFailure { e ->
            Log.d(TAG, "Supabase fetchLiveClasses fallback to local: ${e.message}")
        }

        synchronized(localLiveClasses) {
            localLiveClasses.filter { item ->
                val matchClass = classId.isNullOrBlank() || item.classId.equals(classId, ignoreCase = true)
                val matchTeacher = teacherId.isNullOrBlank() || item.teacherId.equals(teacherId, ignoreCase = true)
                val matchStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)
                matchClass && matchTeacher && matchStatus
            }.sortedWith(
                compareByDescending<LiveClassDto> { it.status == "live" }
                    .thenBy { it.scheduledDate }
                    .thenBy { it.scheduledStartTime }
            )
        }
    }

    /**
     * Retrieves a single Live Class session by ID.
     */
    suspend fun fetchLiveClassById(classId: String): LiveClassDto? = withContext(Dispatchers.IO) {
        synchronized(localLiveClasses) {
            val cached = localLiveClasses.find { it.id == classId }
            if (cached != null) return@withContext cached
        }
        val list = fetchLiveClasses()
        list.find { it.id == classId }
    }

    /**
     * Updates the status of a live class ("scheduled", "live", "completed", "cancelled").
     */
    suspend fun updateLiveClassStatus(
        classId: String,
        newStatus: String,
        startedAt: String? = null,
        endedAt: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val effectiveStarted = if (newStatus == "live") (startedAt ?: nowStr) else startedAt
        val effectiveEnded = if (newStatus == "completed" || newStatus == "cancelled") (endedAt ?: nowStr) else endedAt

        synchronized(localLiveClasses) {
            val idx = localLiveClasses.indexOfFirst { it.id == classId }
            if (idx != -1) {
                val current = localLiveClasses[idx]
                localLiveClasses[idx] = current.copy(
                    status = newStatus,
                    startedAt = effectiveStarted ?: current.startedAt,
                    endedAt = effectiveEnded ?: current.endedAt
                )
            }
        }
        persistClassesLocally()

        // Sync to Supabase
        runCatching {
            val fields = mutableMapOf<String, Any?>("status" to newStatus)
            if (effectiveStarted != null) fields["started_at"] = effectiveStarted
            if (effectiveEnded != null) fields["ended_at"] = effectiveEnded
            val response = SupabaseRepository.api.updateLiveClassById("eq.$classId", fields)
            return@withContext response.isSuccessful
        }.onFailure { e ->
            Log.w(TAG, "Supabase updateLiveClassStatus error: ${e.message}")
        }
        true
    }

    /**
     * Adds or updates a participant record when joining a session.
     */
    suspend fun joinLiveClass(
        classId: String,
        userId: String,
        userName: String,
        role: String
    ): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val participant = LiveClassParticipantDto(
            id = "${classId}_$userId",
            liveClassId = classId,
            userId = userId,
            userName = userName,
            role = role,
            joinedAt = nowStr,
            isConnected = true,
            isMuted = role != "teacher" // Teachers start unmuted or controlled, students start muted
        )

        synchronized(localParticipants) {
            val list = localParticipants.getOrPut(classId) { mutableListOf() }
            list.removeAll { it.userId == userId }
            list.add(participant)
        }

        runCatching {
            SupabaseRepository.api.upsertLiveClassParticipant(participant)
        }
        true
    }

    /**
     * Marks a participant disconnected/left.
     */
    suspend fun leaveLiveClass(classId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        synchronized(localParticipants) {
            val list = localParticipants[classId]
            val item = list?.find { it.userId == userId }
            if (item != null) {
                list.remove(item)
                list.add(item.copy(isConnected = false, leftAt = nowStr))
            }
        }

        runCatching {
            val fields = mapOf(
                "is_connected" to false,
                "left_at" to nowStr
            )
            SupabaseRepository.api.updateLiveClassParticipant("eq.$classId", "eq.$userId", fields)
        }
        true
    }

    /**
     * Updates participant mute and connection state.
     */
    suspend fun updateParticipantState(
        classId: String,
        userId: String,
        isMuted: Boolean,
        isConnected: Boolean = true,
        removed: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        synchronized(localParticipants) {
            val list = localParticipants[classId]
            val item = list?.find { it.userId == userId }
            if (item != null) {
                list.remove(item)
                list.add(
                    item.copy(
                        isMuted = isMuted,
                        isConnected = isConnected,
                        removedAt = if (removed) nowStr else item.removedAt
                    )
                )
            }
        }

        runCatching {
            val fields = mutableMapOf<String, Any?>(
                "is_muted" to isMuted,
                "is_connected" to isConnected
            )
            if (removed) fields["removed_at"] = nowStr
            SupabaseRepository.api.updateLiveClassParticipant("eq.$classId", "eq.$userId", fields)
        }
        true
    }

    /**
     * Fetches current participants for a class.
     */
    suspend fun fetchParticipants(classId: String): List<LiveClassParticipantDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = SupabaseRepository.api.getLiveClassParticipants("eq.$classId")
            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!
                if (remoteList.isNotEmpty()) {
                    synchronized(localParticipants) {
                        localParticipants[classId] = remoteList.toMutableList()
                    }
                }
            }
        }
        synchronized(localParticipants) {
            localParticipants[classId]?.toList() ?: emptyList()
        }
    }

    /**
     * Student raises their hand to request speaking permission.
     */
    suspend fun raiseHand(classId: String, studentId: String, studentName: String): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val handRaise = LiveClassHandRaiseDto(
            id = "${classId}_$studentId",
            liveClassId = classId,
            studentId = studentId,
            studentName = studentName,
            status = "raised",
            createdAt = nowStr
        )

        synchronized(localHandRaises) {
            val list = localHandRaises.getOrPut(classId) { mutableListOf() }
            list.removeAll { it.studentId == studentId }
            list.add(handRaise)
        }

        runCatching {
            SupabaseRepository.api.upsertLiveClassHandRaise(handRaise)
        }
        true
    }

    /**
     * Student lowers their own hand.
     */
    suspend fun lowerHand(classId: String, studentId: String): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        synchronized(localHandRaises) {
            val list = localHandRaises[classId]
            val item = list?.find { it.studentId == studentId }
            if (item != null) {
                list.remove(item)
                list.add(item.copy(status = "lowered", handledAt = nowStr))
            }
        }

        runCatching {
            val fields = mapOf(
                "status" to "lowered",
                "handled_at" to nowStr
            )
            SupabaseRepository.api.updateLiveClassHandRaise("eq.$classId", "eq.$studentId", fields)
        }
        true
    }

    /**
     * Teacher approves or rejects speaking request.
     */
    suspend fun handleHandRaise(classId: String, studentId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        synchronized(localHandRaises) {
            val list = localHandRaises[classId]
            val item = list?.find { it.studentId == studentId }
            if (item != null) {
                list.remove(item)
                list.add(item.copy(status = newStatus, handledAt = nowStr))
            }
        }

        // If approved, unmute participant state
        if (newStatus == "approved") {
            updateParticipantState(classId, studentId, isMuted = false, isConnected = true)
        } else if (newStatus == "rejected" || newStatus == "lowered") {
            updateParticipantState(classId, studentId, isMuted = true, isConnected = true)
        }

        runCatching {
            val fields = mapOf(
                "status" to newStatus,
                "handled_at" to nowStr
            )
            SupabaseRepository.api.updateLiveClassHandRaise("eq.$classId", "eq.$studentId", fields)
        }
        true
    }

    /**
     * Fetches active hand raises for a class session.
     */
    suspend fun fetchHandRaises(classId: String): List<LiveClassHandRaiseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = SupabaseRepository.api.getLiveClassHandRaises("eq.$classId")
            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!
                if (remote.isNotEmpty()) {
                    synchronized(localHandRaises) {
                        localHandRaises[classId] = remote.toMutableList()
                    }
                }
            }
        }
        synchronized(localHandRaises) {
            localHandRaises[classId]?.filter { it.status == "raised" } ?: emptyList()
        }
    }

    /**
     * Broadcasts a real-time emoji reaction (with lightweight cooldown).
     */
    suspend fun sendReaction(
        classId: String,
        userId: String,
        userName: String,
        reaction: String
    ): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val reactionDto = LiveClassReactionDto(
            id = UUID.randomUUID().toString(),
            liveClassId = classId,
            userId = userId,
            userName = userName,
            reaction = reaction,
            createdAt = nowStr
        )

        synchronized(localReactions) {
            val list = localReactions.getOrPut(classId) { mutableListOf() }
            list.add(0, reactionDto)
            // Keep at most 20 recent reactions in memory
            if (list.size > 20) {
                list.subList(20, list.size).clear()
            }
        }

        runCatching {
            SupabaseRepository.api.insertLiveClassReaction(reactionDto)
        }
        true
    }

    /**
     * Fetches recent reactions for display.
     */
    suspend fun fetchRecentReactions(classId: String): List<LiveClassReactionDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = SupabaseRepository.api.getLiveClassReactions("eq.$classId")
            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!
                if (remote.isNotEmpty()) {
                    synchronized(localReactions) {
                        localReactions[classId] = remote.toMutableList()
                    }
                }
            }
        }
        synchronized(localReactions) {
            localReactions[classId]?.toList() ?: emptyList()
        }
    }

    /**
     * Sends an SDP offer/answer or ICE candidate signaling message.
     */
    suspend fun sendSignalingMessage(message: LiveClassSignalingDto): Boolean = withContext(Dispatchers.IO) {
        val resolved = if (message.id.isBlank()) message.copy(id = UUID.randomUUID().toString()) else message
        synchronized(localSignaling) {
            val list = localSignaling.getOrPut(resolved.liveClassId) { mutableListOf() }
            list.add(resolved)
        }

        runCatching {
            SupabaseRepository.api.insertLiveClassSignaling(resolved)
        }
        true
    }

    /**
     * Fetches signaling messages for this peer session.
     */
    suspend fun fetchSignalingMessages(classId: String): List<LiveClassSignalingDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = SupabaseRepository.api.getLiveClassSignaling("eq.$classId")
            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!
                if (remote.isNotEmpty()) {
                    synchronized(localSignaling) {
                        localSignaling[classId] = remote.toMutableList()
                    }
                }
            }
        }
        synchronized(localSignaling) {
            localSignaling[classId]?.toList() ?: emptyList()
        }
    }
}
