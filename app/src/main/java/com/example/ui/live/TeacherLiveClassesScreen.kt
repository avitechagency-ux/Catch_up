package com.example.ui.live

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiveClassDto
import com.example.data.LiveClassRepository
import com.example.notifications.NotificationHelper
import com.example.ui.getSubjectThemeColor
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Teacher dashboard for Live Audio Classes.
 * Redesigned with clean minimal cards (title + icon only) to maintain Catch Up's simplicity and uncluttered aesthetic.
 */
@Composable
fun TeacherLiveClassesScreen(
    teacherId: String = "teacher_01",
    teacherName: String = "Teacher",
    onNavigateBack: () -> Unit = {},
    onCreateLiveClassClick: () -> Unit = {},
    onStartOrResumeClass: (classId: String, title: String, subject: String) -> Unit = { _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var classesList by remember { mutableStateOf<List<LiveClassDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val refreshClasses: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            classesList = LiveClassRepository.fetchLiveClasses(teacherId = teacherId)
            isLoading = false
        }
    }

    LaunchedEffect(teacherId) {
        refreshClasses()
    }

    val liveNowClasses = remember(classesList) { classesList.filter { it.status == "live" } }
    val upcomingClasses = remember(classesList) { classesList.filter { it.status == "scheduled" } }
    val pastClasses = remember(classesList) { classesList.filter { it.status == "completed" || it.status == "cancelled" } }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_live_classes_screen"),
        containerColor = SoftPastelBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DeepMaroon,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("teacher_live_classes_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Classes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onCreateLiveClassClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldMustard,
                            contentColor = DeepMaroon
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("teacher_create_live_class_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create Class",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading && classesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DeepMaroon)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1 — LIVE NOW
                item {
                    SectionHeader(
                        title = "LIVE NOW",
                        icon = Icons.Default.FiberManualRecord,
                        iconTint = LiveIndicatorRed
                    )
                }

                if (liveNowClasses.isNotEmpty()) {
                    items(liveNowClasses) { liveItem ->
                        TeacherMinimalLiveCard(
                            liveClass = liveItem,
                            onContinue = {
                                onStartOrResumeClass(liveItem.id, liveItem.title, liveItem.subjectId)
                            },
                            onEnd = {
                                coroutineScope.launch {
                                    LiveClassRepository.updateLiveClassStatus(liveItem.id, "completed")
                                    refreshClasses()
                                }
                            }
                        )
                    }
                } else {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Text(
                                text = "No active live class right now",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // SECTION 2 — UPCOMING
                item {
                    SectionHeader(
                        title = "UPCOMING",
                        icon = Icons.Outlined.Schedule,
                        iconTint = GoldMustard
                    )
                }

                if (upcomingClasses.isNotEmpty()) {
                    items(upcomingClasses) { upcomingItem ->
                        TeacherMinimalUpcomingCard(
                            liveClass = upcomingItem,
                            onStart = {
                                coroutineScope.launch {
                                    LiveClassRepository.updateLiveClassStatus(upcomingItem.id, "live")
                                    NotificationHelper.showLiveClassStartedNotification(
                                        context = context,
                                        classId = upcomingItem.id,
                                        subject = upcomingItem.subjectId,
                                        title = upcomingItem.title,
                                        teacherName = teacherName
                                    )
                                    onStartOrResumeClass(upcomingItem.id, upcomingItem.title, upcomingItem.subjectId)
                                }
                            }
                        )
                    }
                } else {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Text(
                                text = "No scheduled lessons",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // SECTION 3 — PAST
                if (pastClasses.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "PAST",
                            icon = Icons.Outlined.History,
                            iconTint = TextMuted
                        )
                    }

                    items(pastClasses) { pastItem ->
                        TeacherMinimalPastCard(liveClass = pastItem)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun TeacherMinimalLiveCard(
    liveClass: LiveClassDto,
    onContinue: () -> Unit,
    onEnd: () -> Unit
) {
    val subjectColor = getSubjectThemeColor(liveClass.subjectId)
    val subjectIcon = getLiveSubjectIcon(liveClass.subjectId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, GoldMustard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = subjectColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = subjectIcon,
                            contentDescription = liveClass.subjectId,
                            tint = subjectColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = liveClass.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldMustard,
                        contentColor = DeepMaroon
                    )
                ) {
                    Text("Resume", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onEnd,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveIndicatorRed),
                    border = BorderStroke(1.dp, LiveIndicatorRed.copy(alpha = 0.5f))
                ) {
                    Text("End", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TeacherMinimalUpcomingCard(
    liveClass: LiveClassDto,
    onStart: () -> Unit
) {
    val subjectColor = getSubjectThemeColor(liveClass.subjectId)
    val subjectIcon = getLiveSubjectIcon(liveClass.subjectId)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = subjectColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = subjectIcon,
                            contentDescription = liveClass.subjectId,
                            tint = subjectColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = liveClass.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepMaroon,
                    contentColor = Color.White
                )
            ) {
                Text("Start", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TeacherMinimalPastCard(liveClass: LiveClassDto) {
    val subjectColor = getSubjectThemeColor(liveClass.subjectId)
    val subjectIcon = getLiveSubjectIcon(liveClass.subjectId)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SoftLavenderGrey,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = subjectIcon,
                        contentDescription = liveClass.subjectId,
                        tint = subjectColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = liveClass.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
