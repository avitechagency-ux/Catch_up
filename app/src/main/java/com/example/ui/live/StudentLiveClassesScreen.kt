package com.example.ui.live

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiveClassDto
import com.example.data.LiveClassRepository
import com.example.ui.getSubjectThemeColor
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Returns an appropriate subject icon matching the Catch Up curriculum subjects.
 */
fun getLiveSubjectIcon(subjectName: String): ImageVector {
    return when (subjectName.trim().lowercase()) {
        "physics" -> Icons.Outlined.Architecture
        "chemistry", "science" -> Icons.Outlined.Science
        "biology", "agriculture" -> Icons.Outlined.Eco
        "geography" -> Icons.Outlined.Public
        "history" -> Icons.Outlined.HistoryEdu
        "math", "mathematics" -> Icons.Outlined.Calculate
        "english", "literature" -> Icons.Outlined.AutoStories
        "ict", "computer" -> Icons.Outlined.Computer
        "commerce", "economics", "entrepreneurship" -> Icons.Outlined.AccountBalance
        else -> Icons.Outlined.MenuBook
    }
}

/**
 * Student Live Classes screen displaying Live Now, Upcoming, and Past audio lessons.
 * Redesigned with clean minimal cards (title + icon only) to maintain Catch Up's simplicity and uncluttered aesthetic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLiveClassesScreen(
    studentClass: String = "S.4",
    studentStream: String = "",
    studentName: String = "Student",
    studentId: String = "student_01",
    onNavigateBack: () -> Unit = {},
    onJoinLiveClass: (classId: String, title: String, subject: String, teacherName: String) -> Unit = { _, _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    var classesList by remember { mutableStateOf<List<LiveClassDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val refreshClasses: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            classesList = LiveClassRepository.fetchLiveClasses(classId = studentClass)
            isLoading = false
        }
    }

    LaunchedEffect(studentClass, studentStream) {
        refreshClasses()
    }

    val targetedClasses = remember(classesList, studentClass, studentStream) {
        classesList.filter { liveClass ->
            com.example.data.doesContentTargetStudent(
                targetClass = liveClass.classId,
                targetStreams = liveClass.targetStreams,
                studentClass = studentClass,
                studentStream = studentStream
            )
        }
    }

    val liveNowClasses = remember(targetedClasses) {
        targetedClasses.filter { it.status == "live" }
    }
    val upcomingClasses = remember(targetedClasses) {
        targetedClasses.filter { it.status == "scheduled" }
    }
    val pastClasses = remember(targetedClasses) {
        targetedClasses.filter { it.status == "completed" || it.status == "cancelled" }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_live_classes_screen"),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("live_classes_back_button")
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
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("live_classes_header_title")
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldMustard.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = studentClass,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldMustard,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                        MinimalLiveNowCard(
                            liveClass = liveItem,
                            onJoin = {
                                onJoinLiveClass(liveItem.id, liveItem.title, liveItem.subjectId, liveItem.teacherName)
                            }
                        )
                    }
                } else {
                    item {
                        EmptyLiveStateCard()
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
                        MinimalUpcomingCard(liveClass = upcomingItem)
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
                                text = "No upcoming classes scheduled",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(20.dp)
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
                        MinimalPastCard(liveClass = pastItem)
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

/**
 * Minimal Live Now Card: Focused strictly on Title + Icon + prominent Join action.
 */
@Composable
private fun MinimalLiveNowCard(
    liveClass: LiveClassDto,
    onJoin: () -> Unit
) {
    val subjectColor = getSubjectThemeColor(liveClass.subjectId)
    val subjectIcon = getLiveSubjectIcon(liveClass.subjectId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onJoin)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag("live_now_card_${liveClass.id}"),
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
                // Subject Icon inside colored circle
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

                // Title and minimal subject pill
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = liveClass.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = liveClass.subjectId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = subjectColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Join Button
            Button(
                onClick = onJoin,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldMustard,
                    contentColor = DeepMaroon
                ),
                modifier = Modifier.testTag("join_live_class_button")
            ) {
                Text(
                    text = "JOIN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Minimal Upcoming Card: Title + Icon + subtle tag.
 */
@Composable
private fun MinimalUpcomingCard(liveClass: LiveClassDto) {
    val subjectColor = getSubjectThemeColor(liveClass.subjectId)
    val subjectIcon = getLiveSubjectIcon(liveClass.subjectId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_class_card_${liveClass.id}"),
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = liveClass.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = liveClass.subjectId,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SoftPastelBackground
            ) {
                Text(
                    text = liveClass.scheduledStartTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepMaroon,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Minimal Past Card: Title + Icon only.
 */
@Composable
private fun MinimalPastCard(liveClass: LiveClassDto) {
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

@Composable
private fun EmptyLiveStateCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = DeepMaroon.copy(alpha = 0.08f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = DeepMaroon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = "No live class is happening right now",
                fontSize = 13.sp,
                color = TextMuted
            )
        }
    }
}
