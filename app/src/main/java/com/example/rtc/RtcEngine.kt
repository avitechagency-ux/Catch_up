package com.example.rtc

import android.content.Context
import android.util.Log
import com.example.data.LiveClassRepository
import com.example.data.LiveConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.PeerConnection

/**
 * High-level Live Audio Class Session coordinating RTC transport, signaling, audio, and participants.
 */
class RtcSession(
    private val context: Context,
    val classId: String,
    val userId: String,
    val userName: String,
    val role: String, // "teacher" or "student"
    private val transport: RtcTransport = WebRtcPeerTransport()
) {
    companion object {
        private const val TAG = "RtcSession"
    }

    private val sessionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val audioManager = LiveClassAudioManager(context)
    val signalingManager = SignalingManager(classId, userId)
    val participantManager = ParticipantManager(classId)

    private val _connectionState = MutableStateFlow(LiveConnectionState.CONNECTING)
    val connectionState: StateFlow<LiveConnectionState> = _connectionState.asStateFlow()

    private val _isLocalMuted = MutableStateFlow(role != "teacher")
    val isLocalMuted: StateFlow<Boolean> = _isLocalMuted.asStateFlow()

    private val _isRemoteAudioActive = MutableStateFlow(false)
    val isRemoteAudioActive: StateFlow<Boolean> = _isRemoteAudioActive.asStateFlow()

    private var pollJob: Job? = null

    fun start() {
        Log.d(TAG, "Starting Live Class RTC session for $userName ($role)")
        _connectionState.value = LiveConnectionState.CONNECTING

        // 1. Initialize audio manager and system audio focus
        audioManager.startCommunicationAudio(useSpeakerphone = true)

        // 2. Initialize RTC Transport
        transport.initialize(context)
        val isInitiator = role == "teacher"

        transport.start(
            sessionId = classId,
            isInitiator = isInitiator,
            onLocalIceCandidate = { candidate ->
                sessionScope.launch(Dispatchers.IO) {
                    signalingManager.sendLocalIceCandidate(candidate)
                }
            },
            onLocalSdp = { sdp ->
                sessionScope.launch(Dispatchers.IO) {
                    signalingManager.sendLocalSdp(sdp)
                }
            },
            onConnectionChange = { state ->
                sessionScope.launch(Dispatchers.Main) {
                    _connectionState.value = when (state) {
                        PeerConnection.PeerConnectionState.CONNECTED -> LiveConnectionState.CONNECTED
                        PeerConnection.PeerConnectionState.CONNECTING -> LiveConnectionState.CONNECTING
                        PeerConnection.PeerConnectionState.DISCONNECTED -> LiveConnectionState.RECONNECTING
                        PeerConnection.PeerConnectionState.FAILED -> LiveConnectionState.RECONNECTING
                        PeerConnection.PeerConnectionState.CLOSED -> LiveConnectionState.DISCONNECTED
                        else -> LiveConnectionState.CONNECTING
                    }
                }
            },
            onRemoteAudioStateChange = { active ->
                _isRemoteAudioActive.value = active
            }
        )

        // Set initial mic state (teacher unmuted, student muted)
        val initialMuted = role != "teacher"
        _isLocalMuted.value = initialMuted
        transport.setMicrophoneMuted(initialMuted)

        // 3. Connect Signaling callbacks
        signalingManager.onRemoteSdpReceived = { sdp ->
            transport.handleRemoteSdp(sdp)
        }
        signalingManager.onRemoteIceCandidateReceived = { candidate ->
            transport.handleRemoteIceCandidate(candidate)
        }
        signalingManager.onPeerMuteStateChanged = { peerId, muted ->
            sessionScope.launch {
                participantManager.refresh()
            }
        }
        signalingManager.startListening(sessionScope)

        // 4. Join participant in repository
        sessionScope.launch(Dispatchers.IO) {
            LiveClassRepository.joinLiveClass(classId, userId, userName, role)
            participantManager.refresh()
        }

        // Periodic participant and session refresh
        pollJob = sessionScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(3000L)
                participantManager.refresh()
            }
        }
    }

    fun setMuted(muted: Boolean) {
        _isLocalMuted.value = muted
        transport.setMicrophoneMuted(muted)
        sessionScope.launch(Dispatchers.IO) {
            LiveClassRepository.updateParticipantState(classId, userId, isMuted = muted, isConnected = true)
            signalingManager.sendMuteState(muted)
            participantManager.refresh()
        }
    }

    fun toggleMute() {
        setMuted(!_isLocalMuted.value)
    }

    fun toggleSpeakerphone() {
        val current = audioManager.isSpeakerphoneOn()
        audioManager.setSpeakerphone(!current)
    }

    fun isSpeakerphoneOn(): Boolean = audioManager.isSpeakerphoneOn()

    fun stop() {
        Log.d(TAG, "Stopping Live Class RTC session")
        pollJob?.cancel()
        signalingManager.stop()

        sessionScope.launch(Dispatchers.IO) {
            LiveClassRepository.leaveLiveClass(classId, userId)
        }

        transport.stop()
        audioManager.stopCommunicationAudio()
        _connectionState.value = LiveConnectionState.DISCONNECTED
        sessionScope.cancel()
    }
}

object RtcEngine {
    private var activeSession: RtcSession? = null

    fun getActiveSession(): RtcSession? = activeSession

    fun createSession(
        context: Context,
        classId: String,
        userId: String,
        userName: String,
        role: String
    ): RtcSession {
        activeSession?.stop()
        val session = RtcSession(
            context = context.applicationContext,
            classId = classId,
            userId = userId,
            userName = userName,
            role = role
        )
        activeSession = session
        return session
    }

    fun endActiveSession() {
        activeSession?.stop()
        activeSession = null
    }
}
