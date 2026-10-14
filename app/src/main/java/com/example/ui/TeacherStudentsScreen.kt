package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.normalizeStream
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import java.util.Locale

/**
 * Metadata for a Secondary Class card in the Teacher Student Management portal.
 */
data class TeacherClassInfo(
    val classId: String,       // "S.1" .. "S.6"
    val fullName: String,      // "Senior One" .. "Senior Six"
    val shortLabel: String,    // "S.1"
    val themeColor: Color,     // Distinct card primary accent color
    val containerColor: Color  // Light tinted background
)

/**
 * Standard Ugandan Secondary Classes with distinct vibrant colors.
 */
val UgandanSecondaryClassList = listOf(
    TeacherClassInfo("S.1", "Senior One", "S.1", TealTile, Color(0xFFE8F6F4)),
    TeacherClassInfo("S.2", "Senior Two", "S.2", CoralTile, Color(0xFFFDF0ED)),
    TeacherClassInfo("S.3", "Senior Three", "S.3", IndigoPurple, Color(0xFFEEF1FD)),
    TeacherClassInfo("S.4", "Senior Four", "S.4", PurpleTile, Color(0xFFF3EFFF)),
    TeacherClassInfo("S.5", "Senior Five", "S.5", PeachOrange, Color(0xFFFDF4EB)),
    TeacherClassInfo("S.6", "Senior Six", "S.6", NavyTile, Color(0xFFEBF0F8))
)

/**
 * Contrasting vibrant colors for stream cards, distinct from class cards.
 */
val StreamPillPalette = listOf(
    Color(0xFF0077B6), // Deep Ocean Blue
    Color(0xFF059669), // Emerald Green
    Color(0xFFD97706), // Warm Amber
    Color(0xFF9333EA), // Royal Violet
    Color(0xFFDB2777), // Deep Rose
    Color(0xFF0284C7), // Sky Blue
    Color(0xFF4F46E5), // Electric Indigo
    Color(0xFF0D9488), // Teal
    Color(0xFFB45309), // Ochre
    Color(0xFF7C3AED)  // Bright Violet
)

/**
 * Resolves a stream's card accent color based on its name.
 */
fun resolveStreamColor(streamName: String, index: Int = 0): Color {
    val clean = streamName.trim().uppercase(Locale.ROOT)
    return when {
        clean.contains("BLUE") -> Color(0xFF1E88E5)
        clean.contains("RED") -> Color(0xFFE53935)
        clean.contains("GREEN") -> Color(0xFF2E7D32)
        clean.contains("YELLOW") || clean.contains("GOLD") -> Color(0xFFD97706)
        clean.contains("WHITE") -> Color(0xFF475569) // Charcoal slate accent for white stream
        clean.contains("PURPLE") || clean.contains("VIOLET") -> Color(0xFF8E24AA)
        clean.contains("ORANGE") -> Color(0xFFEA580C)
        clean.contains("NORTH") -> Color(0xFF0077B6)
        clean.contains("SOUTH") -> Color(0xFF059669)
        clean.contains("EAST") -> Color(0xFFD97706)
        clean.contains("WEST") -> Color(0xFF7C3AED)
        clean.contains("CENTRAL") -> Color(0xFF4F46E5)
        else -> StreamPillPalette[kotlin.math.abs(clean.hashCode() + index) % StreamPillPalette.size]
    }
}

/**
 * Maps arbitrary class strings (e.g. "Senior 1", "Senior One", "s1", "S.1") to canonical "S.1" .. "S.6".
 */
fun toCanonicalClassId(raw: String): String {
    val lower = raw.trim().lowercase(Locale.ROOT)
    return when {
        lower.contains("senior one") || lower.contains("senior 1") || lower == "s1" || lower == "s.1" -> "S.1"
        lower.contains("senior two") || lower.contains("senior 2") || lower == "s2" || lower == "s.2" -> "S.2"
        lower.contains("senior three") || lower.contains("senior 3") || lower == "s3" || lower == "s.3" -> "S.3"
        lower.contains("senior four") || lower.contains("senior 4") || lower == "s4" || lower == "s.4" -> "S.4"
        lower.contains("senior five") || lower.contains("senior 5") || lower == "s5" || lower == "s.5" -> "S.5"
        lower.contains("senior six") || lower.contains("senior 6") || lower == "s6" || lower == "s.6" -> "S.6"
        else -> normalizeSecondaryClass(raw)
    }
}

// ============================================================================
// 1. TEACHER STUDENTS MANAGEMENT — CLASSES SCREEN (FULL PAGE)
// ============================================================================

/**
 * Full Page 1: Displays class cards such as Senior One, Senior Two, Senior Three, Senior Four
 * in pill-shaped cards (width ~4cm / ~155dp) with live student counts and search bar.
 */
@Composable
fun TeacherStudentsClassesScreen(
    teacherName: String,
    teacherIdCode: String,
    connectedStudents: List<ConnectedStudent>,
    onSelectClass: (classId: String, className: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val teacherStudents = remember(connectedStudents, teacherIdCode) {
        val cleanCode = teacherIdCode.trim().uppercase()
        connectedStudents.filter { it.teacherIdCode.trim().uppercase() == cleanCode }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_students_classes_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Full Page Header Banner (Purple Gradient Matching Cachap Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B3F8F), Color(0xFF5F4FA8))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag("teacher_classes_header")
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFF6B5CA5).copy(alpha = 0.25f),
                        radius = 110.dp.toPx(),
                        center = Offset(size.width + 10.dp.toPx(), 20.dp.toPx())
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("btn_back_to_teacher_home")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Students by Class",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("teacher_students_title")
                        )
                    }
                }
            }

            // Body: Pill Cards Grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Select a class to view its streams",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("teacher_classes_grid"),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(UgandanSecondaryClassList, key = { it.classId }) { classInfo ->
                        ClassPillCard(
                            classInfo = classInfo,
                            onClick = { onSelectClass(classInfo.classId, classInfo.fullName) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Class Card styled as a wide solid-coloured pill card displaying only the class name and icon.
 */
@Composable
fun ClassPillCard(
    classInfo: TeacherClassInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = classInfo.themeColor.copy(alpha = 0.35f),
                spotColor = classInfo.themeColor.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .bounceClick(onClick = onClick)
            .testTag("class_card_${classInfo.classId.replace(".", "")}"),
        shape = RoundedCornerShape(18.dp),
        color = classInfo.themeColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = classInfo.fullName,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = classInfo.fullName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// 2. TEACHER CLASS STREAMS SCREEN (FULL PAGE)
// ============================================================================

/**
 * Full Page 2: Displays streams belonging to the selected class as individual pill cards.
 * Streams are created dynamically when students register and connect to the teacher via ID.
 */
@Composable
fun TeacherClassStreamsScreen(
    classId: String,
    className: String,
    teacherIdCode: String,
    connectedStudents: List<ConnectedStudent>,
    onSelectStream: (streamName: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val teacherStudentsInClass = remember(connectedStudents, classId, teacherIdCode) {
        val cleanCode = teacherIdCode.trim().uppercase()
        connectedStudents
            .filter { it.teacherIdCode.trim().uppercase() == cleanCode }
            .filter { toCanonicalClassId(it.studentClass) == classId }
    }

    // Dynamic stream grouping:
    // If a student registers with BLUE, BLUE stream is shown. If a new student signs up with WHITE,
    // the app automatically creates a WHITE stream card and places that student in it!
    val streamGroups = remember(teacherStudentsInClass) {
        teacherStudentsInClass
            .groupBy { student ->
                val s = normalizeStream(student.stream)
                if (s.isBlank()) "GENERAL" else s
            }
            .map { (streamName, list) ->
                Pair(streamName, list.size)
            }
            .sortedByDescending { it.second }
    }

    val classColor = remember(classId) {
        UgandanSecondaryClassList.firstOrNull { it.classId == classId }?.themeColor ?: IndigoPurple
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_class_streams_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Full Page Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B3F8F), Color(0xFF5F4FA8))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag("teacher_streams_header")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("btn_back_to_classes")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Classes",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "$className ($classId)",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("teacher_streams_class_title")
                        )
                        Text(
                            text = "${teacherStudentsInClass.size} students across ${streamGroups.size} stream${if (streamGroups.size == 1) "" else "s"}",
                            fontSize = 12.sp,
                            color = LightLavender,
                            modifier = Modifier.testTag("teacher_streams_subtitle")
                        )
                    }
                }
            }

            // Breadcrumbs bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftLavenderGrey.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color(0xFFECEBF2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Classes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateBack)
                            .testTag("breadcrumb_back_to_classes")
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = className,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            // Body: Stream Pills
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                if (streamGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = SoftLavenderGrey
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = classColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.School,
                                            contentDescription = null,
                                            tint = classColor,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "No streams yet in $className",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "When students in $classId register with their stream and link your Teacher ID ($teacherIdCode), their streams (e.g. Blue, North, West, White) will appear here automatically.",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Streams in $className",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("teacher_streams_list"),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(streamGroups, key = { _, item -> item.first }) { index, (streamName, count) ->
                            val streamColor = resolveStreamColor(streamName)
                            StreamBannerCard(
                                streamName = streamName,
                                studentCount = count,
                                streamColor = streamColor,
                                ranking = index + 1,
                                onClick = { onSelectStream(streamName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stream Banner Card matching the reference design:
 * - Full-width horizontal rounded banner card
 * - Circular icon on the left
 * - Title, subtitle, and details in the center
 * - Ranking badge on the right and 3-dots menu at top-right
 */
@Composable
fun StreamBannerCard(
    streamName: String,
    studentCount: Int,
    streamColor: Color,
    ranking: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = streamColor.copy(alpha = 0.35f),
                spotColor = streamColor.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .bounceClick(onClick = onClick)
            .testTag("stream_card_${streamName.lowercase(Locale.ROOT).replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        color = streamColor,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$streamName Stream",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (studentCount == 1) "1 student registered" else "$studentCount students registered",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap to view students",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "$ranking",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Ranking",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. TEACHER STREAM STUDENT LIST SCREEN (FULL PAGE)
// ============================================================================

/**
 * Full Page 3: Displays students belonging strictly to the selected class and stream.
 * Student pills/cards match the high-contrast aesthetic established in the Admin Users screen.
 */
@Composable
fun TeacherStreamStudentsScreen(
    classId: String,
    className: String,
    streamName: String,
    teacherIdCode: String,
    connectedStudents: List<ConnectedStudent>,
    onSelectStudent: (ConnectedStudent) -> Unit,
    onNavigateBackToStreams: () -> Unit,
    onNavigateBackToClasses: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBackToStreams() }

    val cleanStream = normalizeStream(streamName)

    val streamStudents = remember(connectedStudents, classId, streamName, teacherIdCode) {
        val cleanCode = teacherIdCode.trim().uppercase()
        connectedStudents
            .filter { it.teacherIdCode.trim().uppercase() == cleanCode }
            .filter { student ->
                val classMatches = toCanonicalClassId(student.studentClass) == classId
                val streamMatches = if (cleanStream == "GENERAL") {
                    normalizeStream(student.stream).isBlank() || normalizeStream(student.stream) == "GENERAL"
                } else {
                    normalizeStream(student.stream) == cleanStream
                }
                classMatches && streamMatches
            }
    }

    val streamColor = remember(streamName) { resolveStreamColor(streamName) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_stream_students_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Full Page Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B3F8F), Color(0xFF5F4FA8))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag("teacher_stream_students_header")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBackToStreams,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("btn_back_to_streams")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Streams",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "$streamName Stream",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("stream_students_title")
                        )
                        Text(
                            text = "$className ($classId) • ${streamStudents.size} members",
                            fontSize = 12.sp,
                            color = LightLavender,
                            modifier = Modifier.testTag("stream_students_subtitle")
                        )
                    }
                }
            }

            // Breadcrumb Navigation Context
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftLavenderGrey.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFFECEBF2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Classes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateBackToClasses)
                            .testTag("breadcrumb_classes")
                    )

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = className,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateBackToStreams)
                            .testTag("breadcrumb_class_name")
                    )

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "$streamName Stream",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.testTag("breadcrumb_stream_name")
                    )
                }
            }

            // Body: Student Cards
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                if (streamStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = SoftLavenderGrey
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = streamColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No students in this stream yet",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "When students in $classId register with $streamName stream, they will appear here.",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("stream_students_list"),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(streamStudents, key = { it.id }) { student ->
                            TeacherStudentPillCard(
                                student = student,
                                streamColor = streamColor,
                                onClick = { onSelectStudent(student) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Student Pill Card styled like the Admin Users page (`AdminUserCard`),
 * with deep purple gradient, circular white avatar with bold initials, and stream badge.
 */
@Composable
fun TeacherStudentPillCard(
    student: ConnectedStudent,
    streamColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initials = remember(student.studentName) {
        val parts = student.studentName.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase(Locale.ROOT)
        } else {
            "ST"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.30f),
                spotColor = Color(0x20000000)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("student_card_${student.studentName.lowercase(Locale.ROOT).replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        color = IndigoPurple
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(IndigoPurple, Color(0xFF5A4EB0))
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Crisp White Avatar Circle with Bold Initials
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Subject Information
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.studentName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.subjectName.ifBlank { "Connected Student" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (student.connectedDate.isNotBlank()) {
                            Text(
                                text = " • ${student.connectedDate}",
                                fontSize = 11.sp,
                                color = LightLavender.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stream Pill Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = (if (student.stream.isNotBlank()) student.stream else "GENERAL").uppercase(Locale.ROOT),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. TEACHER STUDENT DETAIL SCREEN (FULL PAGE — NO POPUP)
// ============================================================================

/**
 * Full Page 4: Dedicated full-screen page displaying complete profile details of a student.
 * NO POPUP: Fully dedicated view with breadcrumbs, full-page layout, and back navigation.
 */
@Composable
fun TeacherStudentDetailScreen(
    student: ConnectedStudent,
    className: String,
    streamName: String,
    onNavigateBack: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToStreams: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val initials = remember(student.studentName) {
        val parts = student.studentName.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase(Locale.ROOT)
        } else {
            "ST"
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_student_detail_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Full Page Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B3F8F), Color(0xFF5F4FA8))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag("teacher_student_detail_header")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("btn_back_to_stream_students")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Students",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Student Profile",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("student_detail_title")
                        )
                        Text(
                            text = "$className • $streamName Stream",
                            fontSize = 12.sp,
                            color = LightLavender,
                            modifier = Modifier.testTag("student_detail_subtitle")
                        )
                    }
                }
            }

            // Breadcrumb Navigation Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftLavenderGrey.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFFECEBF2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Classes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToClasses)
                            .testTag("breadcrumb_detail_classes")
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = className,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToStreams)
                            .testTag("breadcrumb_detail_class_name")
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = streamName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple,
                        modifier = Modifier
                            .clickable(onClick = onNavigateBack)
                            .testTag("breadcrumb_detail_stream_name")
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = student.studentName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("breadcrumb_detail_student_name")
                    )
                }
            }

            // Full Page Body Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Large Avatar Circle
                Surface(
                    shape = CircleShape,
                    color = IndigoPurple,
                    modifier = Modifier.size(76.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = student.studentName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEDE9FE)
                ) {
                    Text(
                        text = "STUDENT ACCOUNT • CONNECTED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPurple,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Information Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SoftLavenderGrey,
                    border = BorderStroke(1.dp, Color(0xFFECEBF2))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StudentFullDetailRow(
                            icon = Icons.Outlined.School,
                            label = "Academic Class",
                            value = "$className (${toCanonicalClassId(student.studentClass)})"
                        )
                        StudentFullDetailRow(
                            icon = Icons.Outlined.Class,
                            label = "Registered Stream",
                            value = if (student.stream.isNotBlank()) "${student.stream} Stream" else "$streamName Stream"
                        )
                        if (student.subjectName.isNotBlank()) {
                            StudentFullDetailRow(
                                icon = Icons.Outlined.Badge,
                                label = "Connected Subject",
                                value = student.subjectName
                            )
                        }
                        StudentFullDetailRow(
                            icon = Icons.Outlined.Badge,
                            label = "Teacher ID Linked",
                            value = student.teacherIdCode
                        )
                        if (student.connectedDate.isNotBlank()) {
                            StudentFullDetailRow(
                                icon = Icons.Outlined.CalendarToday,
                                label = "Connection Date",
                                value = student.connectedDate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Return to $streamName Stream", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StudentFullDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IndigoPurple,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }
    }
}
