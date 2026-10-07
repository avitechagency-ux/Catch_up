package com.example.rtc

import org.webrtc.PeerConnection

/**
 * Configuration model for STUN and TURN ICE servers.
 * Supports dynamic configuration from a secure backend without hardcoding TURN credentials.
 */
data class RtcIceServerConfig(
    val uri: String,
    val username: String? = null,
    val credential: String? = null
) {
    fun toPeerConnectionIceServer(): PeerConnection.IceServer {
        val builder = PeerConnection.IceServer.builder(uri)
        if (!username.isNullOrBlank()) {
            builder.setUsername(username)
        }
        if (!credential.isNullOrBlank()) {
            builder.setPassword(credential)
        }
        return builder.createIceServer()
    }
}

object RtcIceConfig {
    /**
     * Standard open-source public STUN servers for NAT traversal discovery.
     */
    val defaultStunServers = listOf(
        RtcIceServerConfig(uri = "stun:stun.l.google.com:19302"),
        RtcIceServerConfig(uri = "stun:stun1.l.google.com:19302"),
        RtcIceServerConfig(uri = "stun:stun2.l.google.com:19302")
    )

    /**
     * Resolves ICE servers combining public STUN and optional self-hosted TURN servers.
     */
    fun resolveIceServers(customTurnServers: List<RtcIceServerConfig> = emptyList()): List<PeerConnection.IceServer> {
        val allConfigs = defaultStunServers + customTurnServers
        return allConfigs.map { it.toPeerConnectionIceServer() }
    }
}
