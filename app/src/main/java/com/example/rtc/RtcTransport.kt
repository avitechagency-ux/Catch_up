package com.example.rtc

import android.content.Context
import android.util.Log
import org.webrtc.*

/**
 * Transport abstraction for real-time audio.
 * Allows replacing peer-to-peer transport with an SFU transport in the future
 * without altering Live Classes UI, database, or permissions.
 */
interface RtcTransport {
    fun initialize(context: Context)
    fun start(
        sessionId: String,
        isInitiator: Boolean,
        onLocalIceCandidate: (IceCandidate) -> Unit,
        onLocalSdp: (SessionDescription) -> Unit,
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit,
        onRemoteAudioStateChange: (Boolean) -> Unit
    )
    fun stop()
    fun setMicrophoneMuted(muted: Boolean)
    fun handleRemoteSdp(sdp: SessionDescription)
    fun handleRemoteIceCandidate(candidate: IceCandidate)
    val isConnected: Boolean
    val isMuted: Boolean
}

/**
 * Production-ready WebRTC implementation for audio communication using libwebrtc.
 */
class WebRtcPeerTransport(
    private val customIceServers: List<RtcIceServerConfig> = emptyList()
) : RtcTransport {

    companion object {
        private const val TAG = "WebRtcPeerTransport"
        private const val AUDIO_TRACK_ID = "catchup_audio_track"
        private const val LOCAL_STREAM_ID = "catchup_audio_stream"
        private var isFactoryInitialized = false
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null

    private var onIceCandidateCallback: ((IceCandidate) -> Unit)? = null
    private var onSdpCallback: ((SessionDescription) -> Unit)? = null
    private var onConnectionChangeCallback: ((PeerConnection.PeerConnectionState) -> Unit)? = null
    private var onRemoteAudioCallback: ((Boolean) -> Unit)? = null

    private var _isConnected = false
    override val isConnected: Boolean get() = _isConnected

    private var _isMuted = true
    override val isMuted: Boolean get() = _isMuted

    override fun initialize(context: Context) {
        if (!isFactoryInitialized) {
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)
            isFactoryInitialized = true
        }

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()
    }

    override fun start(
        sessionId: String,
        isInitiator: Boolean,
        onLocalIceCandidate: (IceCandidate) -> Unit,
        onLocalSdp: (SessionDescription) -> Unit,
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit,
        onRemoteAudioStateChange: (Boolean) -> Unit
    ) {
        this.onIceCandidateCallback = onLocalIceCandidate
        this.onSdpCallback = onLocalSdp
        this.onConnectionChangeCallback = onConnectionChange
        this.onRemoteAudioCallback = onRemoteAudioStateChange

        val factory = peerConnectionFactory ?: return

        // 1. Create AudioSource with WebRTC audio processing (echo cancellation, noise suppression, AGC)
        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
        }
        audioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource).apply {
            setEnabled(!_isMuted)
        }

        // 2. Configure PeerConnection with STUN/TURN ICE servers
        val iceServers = RtcIceConfig.resolveIceServers(customIceServers)
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        val pcObserver = object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "onSignalingChange: $state")
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "onIceConnectionChange: $state")
                _isConnected = state == PeerConnection.IceConnectionState.CONNECTED ||
                        state == PeerConnection.IceConnectionState.COMPLETED
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Log.d(TAG, "onConnectionChange: $newState")
                if (newState != null) {
                    _isConnected = newState == PeerConnection.PeerConnectionState.CONNECTED
                    onConnectionChangeCallback?.invoke(newState)
                }
            }

            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate != null) {
                    Log.d(TAG, "onIceCandidate: ${candidate.sdpMid}")
                    onIceCandidateCallback?.invoke(candidate)
                }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "onAddStream: ${stream?.id}")
                val audioTracks = stream?.audioTracks
                if (!audioTracks.isNullOrEmpty()) {
                    audioTracks[0].setEnabled(true)
                    onRemoteAudioCallback?.invoke(true)
                }
            }

            override fun onRemoveStream(stream: MediaStream?) {
                Log.d(TAG, "onRemoveStream: ${stream?.id}")
                onRemoteAudioCallback?.invoke(false)
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is AudioTrack) {
                    Log.d(TAG, "Remote audio track added: ${track.id()}")
                    track.setEnabled(true)
                    onRemoteAudioCallback?.invoke(true)
                }
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                val track = transceiver?.receiver?.track()
                if (track is AudioTrack) {
                    Log.d(TAG, "Remote audio track received via transceiver")
                    track.setEnabled(true)
                    onRemoteAudioCallback?.invoke(true)
                }
            }

            override fun onRemoveTrack(receiver: RtpReceiver?) {
                onRemoteAudioCallback?.invoke(false)
            }

            override fun onDataChannel(dataChannel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
        }

        peerConnection = factory.createPeerConnection(rtcConfig, pcObserver)

        // 3. Add local audio track
        localAudioTrack?.let { track ->
            peerConnection?.addTrack(track, listOf(LOCAL_STREAM_ID))
        }

        // 4. If session initiator (e.g. teacher starting), create SDP offer
        if (isInitiator) {
            createSdpOffer()
        }
    }

    private fun createSdpOffer() {
        val pc = peerConnection ?: return
        val sdpConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (desc != null) {
                    pc.setLocalDescription(this, desc)
                    onSdpCallback?.invoke(desc)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "createOffer failure: $error")
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "setLocalDescription failure: $error")
            }
        }, sdpConstraints)
    }

    override fun handleRemoteSdp(sdp: SessionDescription) {
        val pc = peerConnection ?: return
        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "setRemoteDescription success (${sdp.type})")
                if (sdp.type == SessionDescription.Type.OFFER) {
                    createSdpAnswer()
                }
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "setRemoteDescription failure: $error")
            }
        }, sdp)
    }

    private fun createSdpAnswer() {
        val pc = peerConnection ?: return
        val sdpConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        pc.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (desc != null) {
                    pc.setLocalDescription(this, desc)
                    onSdpCallback?.invoke(desc)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "createAnswer failure: $error")
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "setLocalDescription (answer) failure: $error")
            }
        }, sdpConstraints)
    }

    override fun handleRemoteIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    override fun setMicrophoneMuted(muted: Boolean) {
        _isMuted = muted
        localAudioTrack?.setEnabled(!muted)
    }

    override fun stop() {
        _isConnected = false
        runCatching {
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null

            localAudioTrack?.dispose()
            localAudioTrack = null

            audioSource?.dispose()
            audioSource = null

            peerConnectionFactory?.dispose()
            peerConnectionFactory = null
        }.onFailure { e ->
            Log.w(TAG, "Error disposing WebRTC objects: ${e.message}")
        }
    }
}

/**
 * Placeholder for Future SFU Media Transport (mediasoup / janus / ion).
 * Proves that media architecture is decoupled and can scale to massive classes
 * without rewriting UI, permissions, or database signaling.
 */
class FutureSfuTransport : RtcTransport {
    private var _isConnected = false
    private var _isMuted = true

    override fun initialize(context: Context) {}
    override fun start(
        sessionId: String,
        isInitiator: Boolean,
        onLocalIceCandidate: (IceCandidate) -> Unit,
        onLocalSdp: (SessionDescription) -> Unit,
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit,
        onRemoteAudioStateChange: (Boolean) -> Unit
    ) {
        _isConnected = true
        onConnectionChange(PeerConnection.PeerConnectionState.CONNECTED)
        onRemoteAudioStateChange(true)
    }
    override fun stop() {
        _isConnected = false
    }
    override fun setMicrophoneMuted(muted: Boolean) {
        _isMuted = muted
    }
    override fun handleRemoteSdp(sdp: SessionDescription) {}
    override fun handleRemoteIceCandidate(candidate: IceCandidate) {}
    override val isConnected: Boolean get() = _isConnected
    override val isMuted: Boolean get() = _isMuted
}
