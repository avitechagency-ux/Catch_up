package com.example.ui.live

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.*
import com.example.rtc.ActiveSpeaker
import com.example.rtc.RtcEngine
import com.example.rtc.RtcSession
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen dedicated student Live Audio Lesson interface.
 * Audio only (no video, no screen share, no chat).
 */
@Composable
fun StudentLiveSessionScreen(
    classId: String,
    title: String,
    subject: String,
    teacherName: String,
    studentId: String,
    studentName: String,
    onLeaveClass: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var rtcSession by remember {
        mutableStateOf<RtcSession?>(null)
    }

    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var showReactionMenu by remember { mutableStateOf(false) }
    var reactionCooldown by remember { mutableStateOf(false) }
    var floatingReactions by remember { mutableStateOf<List<LiveClassReactionDto>>(emptyList()) }
    var permissionDeniedBanner by remember { mutableStateOf(false) }

    // Hand raise state
    var isHandRaised by remember { mutableStateOf(false) }
    var speakingPermission by remember { mutableStateOf(SpeakingPermission.MUTED_LISTENING) }

    // Microphone runtime permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedBanner = false
            rtcSession?.setMuted(false)
        } else {
            permissionDeniedBanner = true
        }
    }

    // Initialize WebRTC session
    DisposableEffect(classId) {
        val session = RtcEngine.createSession(
            context = context,
            classId = classId,
            userId = studentId,
            userName = studentName,
            role = "student"
        )
        rtcSession = session
        session.start()

        onDispose {
            RtcEngine.endActiveSession()
        }
    }

    val connectionState by rtcSession?.connectionState?.collectAsState()
        ?: remember { mutableStateOf(LiveConnectionState.CONNECTING) }
    val isLocalMuted by rtcSession?.isLocalMuted?.collectAsState()
        ?: remember { mutableStateOf(true) }
    val participants by rtcSession?.participantManager?.participants?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val currentSpeaker by rtcSession?.participantManager?.currentSpeaker?.collectAsState()
        ?: remember { mutableStateOf(null) }
    val handRaises by rtcSession?.participantManager?.handRaises?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }

    // Check if teacher approved our hand raise
    LaunchedEffect(participants, handRaises) {
        val selfParticipant = participants.find { it.userId == studentId }
        val selfRaise = handRaises.find { it.studentId == studentId }

        isHandRaised = selfRaise?.status == "raised"

        speakingPermission = when {
            selfParticipant != null && !selfParticipant.isMuted -> SpeakingPermission.ALLOWED_TO_SPEAK
            selfRaise?.status == "raised" -> SpeakingPermission.HAND_RAISED
            selfRaise?.status == "rejected" -> SpeakingPermission.REJECTED
            else -> SpeakingPermission.MUTED_LISTENING
        }

        // If teacher muted us back
        if (selfParticipant?.isMuted == true && !isLocalMuted) {
            rtcSession?.setMuted(true)
        }
    }

    // Reaction polling
    LaunchedEffect(classId) {
        while (true) {
            delay(2000L)
            val recent = LiveClassRepository.fetchRecentReactions(classId)
            floatingReactions = recent.take(5)
        }
    }

    BackHandler {
        showLeaveConfirmation = true
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_live_session_screen"),
        containerColor = DeepMaroon,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LiveIndicatorRed
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = subject,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldMustard
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Connection status badge
                    ConnectionStatusBadge(state = connectionState)
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color(0xFF2E0913),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Speaking permission feedback banner
                    AnimatedVisibility(visible = speakingPermission == SpeakingPermission.ALLOWED_TO_SPEAK) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GreenAccent.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, GreenAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = GreenAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You can speak now! Tap the mic to unmute.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // 1. Raise / Lower Hand Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (isHandRaised) {
                                            LiveClassRepository.lowerHand(classId, studentId)
                                            isHandRaised = false
                                        } else {
                                            LiveClassRepository.raiseHand(classId, studentId, studentName)
                                            isHandRaised = true
                                        }
                                        rtcSession?.participantManager?.refresh()
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("raise_hand_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isHandRaised) GoldMustard else Color.White.copy(alpha = 0.15f),
                                    contentColor = if (isHandRaised) DeepMaroon else Color.White
                                )
                            ) {
                                Text(
                                    text = "✋",
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHandRaised) "Lower Hand" else "Raise Hand",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // 2. Reaction Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = { showReactionMenu = !showReactionMenu },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("reaction_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(text = "👏", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "React",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // 3. Microphone Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val canSpeak = speakingPermission == SpeakingPermission.ALLOWED_TO_SPEAK
                            FilledIconButton(
                                onClick = {
                                    if (canSpeak) {
                                        val hasRecordPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (!hasRecordPerm) {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            rtcSession?.toggleMute()
                                        }
                                    }
                                },
                                enabled = canSpeak,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("student_mic_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (!isLocalMuted) LiveIndicatorRed else Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.White.copy(alpha = 0.08f),
                                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (!canSpeak) Icons.Outlined.Lock else if (isLocalMuted) Icons.Outlined.MicOff else Icons.Filled.Mic,
                                    contentDescription = "Microphone",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (!canSpeak) "Listening" else if (isLocalMuted) "Unmute" else "Mute",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // 4. Leave Class Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = { showLeaveConfirmation = true },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("leave_class_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFFC62828),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CallEnd,
                                    contentDescription = "Leave Class",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Leave",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main visualizer area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Speaker Visualizer
                TeacherSpeakerVisualizer(
                    speaker = currentSpeaker ?: ActiveSpeaker(
                        userId = "teacher",
                        name = teacherName,
                        role = "teacher",
                        isSpeaking = false
                    ),
                    isLive = connectionState == LiveConnectionState.CONNECTED
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Connected participants count
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${participants.size.coerceAtLeast(1)} connected",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Floating reactions bar (when toggled)
            if (showReactionMenu) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val reactions = listOf("👍", "❤️", "👏", "🤔", "🙋")
                        reactions.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .clickable {
                                        if (!reactionCooldown) {
                                            reactionCooldown = true
                                            coroutineScope.launch {
                                                LiveClassRepository.sendReaction(classId, studentId, studentName, emoji)
                                                delay(1500L) // Anti-spam cooldown
                                                reactionCooldown = false
                                            }
                                            showReactionMenu = false
                                        }
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Floating recent reaction bubbles
            if (floatingReactions.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(floatingReactions) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.92f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.reaction, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.userName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Leave confirmation dialog
    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text("Leave Live Class?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to leave this lesson? You can rejoin anytime while it remains live.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveConfirmation = false
                        onLeaveClass()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveIndicatorRed)
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) {
                    Text("Stay")
                }
            }
        )
    }
}

@Composable
private fun ConnectionStatusBadge(state: LiveConnectionState) {
    val (label, bg, dot) = when (state) {
        LiveConnectionState.CONNECTED -> Triple("Connected", GreenAccent.copy(alpha = 0.2f), GreenAccent)
        LiveConnectionState.CONNECTING -> Triple("Connecting...", GoldMustard.copy(alpha = 0.2f), GoldMustard)
        LiveConnectionState.RECONNECTING -> Triple("Reconnecting...", GoldMustard.copy(alpha = 0.3f), GoldMustard)
        LiveConnectionState.DISCONNECTED -> Triple("Disconnected", Color.White.copy(alpha = 0.2f), Color.White)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TeacherSpeakerVisualizer(
    speaker: ActiveSpeaker,
    isLive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (speaker.isSpeaking && isLive) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing ring when speaking
            if (speaker.isSpeaking && isLive) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(GoldMustard.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(GoldMustard.copy(alpha = 0.35f))
                )
            }

            // Center avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(GoldMustard, DeepMaroon)
                        )
                    )
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = speaker.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (speaker.isSpeaking) GoldMustard else Color.White.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (speaker.isSpeaking) "Currently Speaking 🎙" else "Listening",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (speaker.isSpeaking) DeepMaroon else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
