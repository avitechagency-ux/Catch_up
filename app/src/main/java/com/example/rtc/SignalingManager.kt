package com.example.rtc

import android.util.Base64
import android.util.Log
import com.example.data.LiveClassRepository
import com.example.data.LiveClassSignalingDto
import kotlinx.coroutines.*
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

/**
 * Coordinates SDP and ICE candidate signaling through Supabase repository.
 */
class SignalingManager(
    private val classId: String,
    private val localUserId: String
) {
    companion object {
        private const val TAG = "SignalingManager"
        private const val POLL_INTERVAL_MS = 2500L
    }

    private var pollingJob: Job? = null
    private val processedMessageIds = mutableSetOf<String>()

    var onRemoteSdpReceived: ((SessionDescription) -> Unit)? = null
    var onRemoteIceCandidateReceived: ((IceCandidate) -> Unit)? = null
    var onPeerMuteStateChanged: ((String, Boolean) -> Unit)? = null

    fun startListening(scope: CoroutineScope) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val messages = LiveClassRepository.fetchSignalingMessages(classId)
                    messages.filter { it.senderId != localUserId && !processedMessageIds.contains(it.id) }
                        .forEach { msg ->
                            processedMessageIds.add(msg.id)
                            processMessage(msg)
                        }
                } catch (e: Exception) {
                    Log.w(TAG, "Signaling poll error: ${e.message}")
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun processMessage(msg: LiveClassSignalingDto) {
        try {
            when (msg.type) {
                "offer", "answer" -> {
                    val json = JSONObject(msg.payload)
                    val sdpString = json.getString("sdp")
                    val typeEnum = if (msg.type == "offer") SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER
                    val sdp = SessionDescription(typeEnum, sdpString)
                    onRemoteSdpReceived?.invoke(sdp)
                }
                "ice_candidate" -> {
                    val json = JSONObject(msg.payload)
                    val sdpMid = json.optString("sdpMid", "0")
                    val sdpMLineIndex = json.optInt("sdpMLineIndex", 0)
                    val candidateStr = json.getString("candidate")
                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, candidateStr)
                    onRemoteIceCandidateReceived?.invoke(candidate)
                }
                "mute_state" -> {
                    val json = JSONObject(msg.payload)
                    val isMuted = json.optBoolean("is_muted", true)
                    onPeerMuteStateChanged?.invoke(msg.senderId, isMuted)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing signaling message (${msg.type}): ${e.message}")
        }
    }

    suspend fun sendLocalSdp(sdp: SessionDescription) {
        val json = JSONObject().apply {
            put("type", sdp.type.canonicalForm())
            put("sdp", sdp.description)
        }
        val msg = LiveClassSignalingDto(
            id = "",
            liveClassId = classId,
            senderId = localUserId,
            type = if (sdp.type == SessionDescription.Type.OFFER) "offer" else "answer",
            payload = json.toString()
        )
        LiveClassRepository.sendSignalingMessage(msg)
    }

    suspend fun sendLocalIceCandidate(candidate: IceCandidate) {
        val json = JSONObject().apply {
            put("sdpMid", candidate.sdpMid)
            put("sdpMLineIndex", candidate.sdpMLineIndex)
            put("candidate", candidate.sdp)
        }
        val msg = LiveClassSignalingDto(
            id = "",
            liveClassId = classId,
            senderId = localUserId,
            type = "ice_candidate",
            payload = json.toString()
        )
        LiveClassRepository.sendSignalingMessage(msg)
    }

    suspend fun sendMuteState(isMuted: Boolean) {
        val json = JSONObject().apply {
            put("is_muted", isMuted)
        }
        val msg = LiveClassSignalingDto(
            id = "",
            liveClassId = classId,
            senderId = localUserId,
            type = "mute_state",
            payload = json.toString()
        )
        LiveClassRepository.sendSignalingMessage(msg)
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
