package com.example.rtc

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * Manages device audio settings, speakerphone, communication mode, and audio focus.
 */
class LiveClassAudioManager(context: Context) {
    companion object {
        private const val TAG = "LiveClassAudioManager"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousAudioMode: Int = audioManager.mode
    private var previousSpeakerphoneState: Boolean = audioManager.isSpeakerphoneOn
    private var focusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    /**
     * Configures the device for real-time live audio lesson communication.
     */
    fun startCommunicationAudio(useSpeakerphone: Boolean = true) {
        previousAudioMode = audioManager.mode
        previousSpeakerphoneState = audioManager.isSpeakerphoneOn

        requestAudioFocus()

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            setSpeakerphone(useSpeakerphone)
        } catch (e: Exception) {
            Log.e(TAG, "Failed configuring audio mode: ${e.message}")
        }
    }

    fun setSpeakerphone(on: Boolean) {
        try {
            audioManager.isSpeakerphoneOn = on
            Log.d(TAG, "Speakerphone set to $on")
        } catch (e: Exception) {
            Log.w(TAG, "Could not set speakerphone: ${e.message}")
        }
    }

    fun isSpeakerphoneOn(): Boolean = audioManager.isSpeakerphoneOn

    private fun requestAudioFocus() {
        if (hasAudioFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    Log.d(TAG, "Audio focus change: $focusChange")
                }
                .build()

            val res = audioManager.requestAudioFocus(focusRequest!!)
            hasAudioFocus = res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val res = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
            hasAudioFocus = res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**
     * Restores previous audio settings and releases audio focus.
     */
    fun stopCommunicationAudio() {
        try {
            audioManager.isSpeakerphoneOn = previousSpeakerphoneState
            audioManager.mode = previousAudioMode
        } catch (e: Exception) {
            Log.w(TAG, "Error restoring audio mode: ${e.message}")
        }

        if (hasAudioFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
            hasAudioFocus = false
        }
    }
}
