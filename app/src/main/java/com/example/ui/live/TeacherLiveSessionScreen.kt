package com.example.ui.live

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
 * Full-screen dedicated teacher Live Audio Lesson interface with full moderation controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLiveSessionScreen(
    classId: String,
    title: String,
    subject: String,
    teacherId: String,
    teacherName: String,
    onEndClass: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var rtcSession by remember { mutableStateOf<RtcSession?>(null) }
    var showEndConfirmation by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isSpeakerphoneOn by remember { mutableStateOf(true) }

    // Elapsed timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Microphone runtime permission
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            rtcSession?.setMuted(false)
        }
    }

    // Initialize WebRTC session
    DisposableEffect(classId) {
        val hasRecordPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasRecordPerm) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        val session = RtcEngine.createSession(
            context = context,
            classId = classId,
            userId = teacherId,
            userName = teacherName,
            role = "teacher"
        )
        rtcSession = session
        session.start()

        onDispose {
            RtcEngine.endActiveSession()
        }
    }

    val isLocalMuted by rtcSession?.isLocalMuted?.collectAsState()
        ?: remember { mutableStateOf(false) }
    val participants by rtcSession?.participantManager?.participants?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val handRaises by rtcSession?.participantManager?.handRaises?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val currentSpeaker by rtcSession?.participantManager?.currentSpeaker?.collectAsState()
        ?: remember { mutableStateOf(null) }

    BackHandler {
        showEndConfirmation = true
    }

    val studentParticipants = remember(participants) {
        participants.filter { it.role == "student" && it.isConnected }
    }

    val formattedDuration = remember(elapsedSeconds) {
        val mins = elapsedSeconds / 60
        val secs = elapsedSeconds % 60
        String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_live_session_screen"),
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
                                text = "$subject • $formattedDuration",
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

                    // Connected count
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.People,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${studentParticipants.size} students",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Local Mic Mute/Unmute
                    Button(
                        onClick = { rtcSession?.toggleMute() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocalMuted) Color.White.copy(alpha = 0.2f) else GoldMustard,
                            contentColor = if (isLocalMuted) Color.White else DeepMaroon
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocalMuted) Icons.Outlined.MicOff else Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLocalMuted) "Unmute Mic" else "Mute Mic",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Speakerphone Toggle
                    IconButton(
                        onClick = {
                            rtcSession?.toggleSpeakerphone()
                            isSpeakerphoneOn = rtcSession?.isSpeakerphoneOn() ?: true
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSpeakerphoneOn) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeDown,
                            contentDescription = "Speakerphone",
                            tint = Color.White
                        )
                    }

                    // End Class Button
                    Button(
                        onClick = { showEndConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiveIndicatorRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CallEnd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "End Class",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PROMINENT RAISED HANDS PANEL
            if (handRaises.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = GoldMustard.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, GoldMustard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✋", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Raised Hands (${handRaises.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldMustard
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        handRaises.forEach { raise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = raise.studentName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                rtcSession?.participantManager?.allowStudentToSpeak(raise.studentId)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GreenAccent,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Allow to Speak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                LiveClassRepository.handleHandRaise(classId, raise.studentId, "rejected")
                                                rtcSession?.participantManager?.refresh()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f)),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PARTICIPANT PANEL
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "STUDENTS CONNECTED (${studentParticipants.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (studentParticipants.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Waiting for students to join...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(studentParticipants) { student ->
                                val isSpeaking = !student.isMuted
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSpeaking) GoldMustard.copy(alpha = 0.08f) else SoftLavenderGrey,
                                    border = BorderStroke(1.dp, if (isSpeaking) GoldMustard else BorderGrey)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSpeaking) GoldMustard else DeepMaroon.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Person,
                                                    contentDescription = null,
                                                    tint = if (isSpeaking) DeepMaroon else DeepMaroon,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = student.userName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                                Text(
                                                    text = if (isSpeaking) "Speaking 🎙" else "Muted (Listening)",
                                                    fontSize = 11.sp,
                                                    color = if (isSpeaking) GreenAccent else TextMuted
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (isSpeaking) {
                                                OutlinedButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            rtcSession?.participantManager?.muteStudent(student.userId)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(30.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepMaroon)
                                                ) {
                                                    Text("Mute", fontSize = 11.sp)
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            rtcSession?.participantManager?.allowStudentToSpeak(student.userId)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(30.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = GoldMustard,
                                                        contentColor = DeepMaroon
                                                    )
                                                ) {
                                                    Text("Allow Mic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        rtcSession?.participantManager?.removeStudent(student.userId)
                                                    }
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Close,
                                                    contentDescription = "Remove",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // End class confirmation dialog
    if (showEndConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndConfirmation = false },
            title = { Text("End Live Class?", fontWeight = FontWeight.Bold) },
            text = { Text("Ending the class will disconnect all participating students and mark the lesson completed.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEndConfirmation = false
                        coroutineScope.launch {
                            LiveClassRepository.updateLiveClassStatus(classId, "completed")
                            RtcEngine.endActiveSession()
                            onEndClass()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveIndicatorRed)
                ) {
                    Text("End Class Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
