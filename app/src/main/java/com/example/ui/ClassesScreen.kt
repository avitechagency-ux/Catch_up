package com.example.ui

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import com.example.ui.theme.*

private val AccountBrightGold = Color(0xFFFFD54F)
private val AccountFieldBg = Color(0xFFF8F7FC)
private val PlayerDarkBg = Color(0xFF0D0B18)

enum class PlayerViewMode {
    FIT_16_9,
    FULL_FILL
}

@Composable
fun ClassesScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All") }
    var selectedClass by remember { mutableStateOf("All") }
    var activePlayingLesson by remember { mutableStateOf<VideoLesson?>(null) }

    var allLessons by remember { mutableStateOf<List<VideoLesson>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val availableSubjects = remember {
        listOf(
            "All",
            "Physics",
            "Chemistry",
            "Biology",
            "Mathematics",
            "English",
            "History",
            "Geography",
            "Economics",
            "Agriculture"
        )
    }

    val availableClasses = remember {
        listOf("All", "S.1", "S.2", "S.3", "S.4", "S.5", "S.6")
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val remoteLessons = SupabaseRepository.fetchAllVideoLessons()
        if (remoteLessons.isNotEmpty()) {
            allLessons = remoteLessons
        } else {
            allLessons = DefaultVideoLessons
        }
        isLoading = false
    }

    if (activePlayingLesson != null) {
        VideoLessonPlayerScreen(
            lesson = activePlayingLesson!!,
            onNavigateBack = { activePlayingLesson = null }
        )
    } else {
        BackHandler {
            when {
                searchQuery.isNotBlank() -> searchQuery = ""
                selectedSubject != "All" -> selectedSubject = "All"
                selectedClass != "All" -> selectedClass = "All"
                else -> onNavigateBack()
            }
        }

        val filteredLessons = remember(searchQuery, selectedSubject, selectedClass, allLessons) {
            allLessons.filter { lesson ->
                val matchesSubject = selectedSubject.equals("All", ignoreCase = true) ||
                    lesson.subject.equals(selectedSubject, ignoreCase = true)

                val matchesClass = selectedClass.equals("All", ignoreCase = true) ||
                    lesson.targetClass.equals(selectedClass, ignoreCase = true) ||
                    lesson.targetClass.contains(selectedClass, ignoreCase = true)

                val matchesQuery = if (searchQuery.isBlank()) true else {
                    val q = searchQuery.trim().lowercase()
                    lesson.title.lowercase().contains(q) ||
                        lesson.subject.lowercase().contains(q) ||
                        lesson.topic.lowercase().contains(q) ||
                        lesson.teacherName.lowercase().contains(q) ||
                        lesson.targetClass.lowercase().contains(q) ||
                        lesson.description.lowercase().contains(q)
                }

                matchesSubject && matchesClass && matchesQuery
            }
        }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(SoftPastelBackground)
                .testTag("classes_screen"),
            containerColor = SoftPastelBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                ClassesTopHeader(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearSearch = {
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    selectedSubject = selectedSubject,
                    onSelectSubject = { selectedSubject = it },
                    availableSubjects = availableSubjects,
                    selectedClass = selectedClass,
                    onSelectClass = { selectedClass = it },
                    availableClasses = availableClasses,
                    totalCount = filteredLessons.size,
                    onBack = {
                        when {
                            searchQuery.isNotBlank() -> searchQuery = ""
                            selectedSubject != "All" -> selectedSubject = "All"
                            selectedClass != "All" -> selectedClass = "All"
                            else -> onNavigateBack()
                        }
                    }
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = IndigoPurple)
                        }
                    } else if (filteredLessons.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OndemandVideo,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        "No video lessons found for \"$searchQuery\""
                                    } else {
                                        "No video lessons available for the selected filter."
                                    },
                                    textAlign = TextAlign.Center,
                                    color = TextMuted,
                                    fontSize = 15.sp
                                )
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        selectedSubject = "All"
                                        selectedClass = "All"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset Filters", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredLessons, key = { it.id }) { lesson ->
                                VideoLessonDropdownCard(
                                    lesson = lesson,
                                    onPlayClick = { activePlayingLesson = lesson }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassesTopHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    selectedSubject: String,
    onSelectSubject: (String) -> Unit,
    availableSubjects: List<String>,
    selectedClass: String,
    onSelectClass: (String) -> Unit,
    availableClasses: List<String>,
    totalCount: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = IndigoPurple,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .testTag("classes_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lessons",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$totalCount video lesson${if (totalCount == 1) "" else "s"} available",
                    fontSize = 12.sp,
                    color = LightLavender
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SubjectSearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Subject Filter Chips
        val subjectScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(subjectScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableSubjects.forEach { subject ->
                val isSelected = subject.equals(selectedSubject, ignoreCase = true)
                Surface(
                    onClick = { onSelectSubject(subject) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFFB300) else Color(0xFFDC8CC0).copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFB300) else Color(0xFFDC8CC0)),
                    modifier = Modifier.testTag("chip_subject_$subject")
                ) {
                    Text(
                        text = subject,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF1A1A1A) else Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Class Level Filter Chips
        val classScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(classScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Class:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(end = 4.dp)
            )

            availableClasses.forEach { cls ->
                val isSelected = cls.equals(selectedClass, ignoreCase = true)
                Surface(
                    onClick = { onSelectClass(cls) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("chip_class_$cls")
                ) {
                    Text(
                        text = cls,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) IndigoPurple else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoLessonDropdownCard(
    lesson: VideoLesson,
    onPlayClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isCompleted = VideoLessonProgressManager.isCompleted(context, lesson.id)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .border(
                width = if (isCompleted) 1.5.dp else 0.dp,
                color = if (isCompleted) TealTile else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 88.dp, height = 50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IndigoPurple.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = IndigoPurple,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndigoPurple.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = lesson.targetClass,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = lesson.subject,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralTile
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = lesson.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Toggle drop-down",
                        tint = TextMuted
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccountFieldBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Topic: ${lesson.topic}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPurple
                    )
                    Text(
                        text = lesson.description,
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${lesson.teacherName} • ${lesson.durationFormatted}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("play_video_lesson_${lesson.id}")
                        ) {
                            Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Watch", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Full-Page Android Media Player Screen for Video Lessons.
 */
@Composable
fun VideoLessonPlayerScreen(
    lesson: VideoLesson,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val embedUrl = getYouTubeEmbedUrl(lesson.youtubeVideoUrl)
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentSpeed by remember { mutableStateOf(1.0f) }
    var viewMode by remember { mutableStateOf(PlayerViewMode.FIT_16_9) }

    LaunchedEffect(lesson.id) {
        VideoLessonProgressManager.markCompleted(context, lesson.id, lesson.title, lesson.subject)
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerDarkBg)
            .testTag("video_lesson_player_screen"),
        containerColor = PlayerDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // --- TOP NAVIGATION HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141026))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to lessons",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${lesson.subject} (${lesson.targetClass})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccountBrightGold
                            )
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = lesson.teacherName,
                                fontSize = 12.sp,
                                color = LightLavender
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- VIDEO PLAYER DISPLAY AREA ---
            val videoModifier = when (viewMode) {
                PlayerViewMode.FIT_16_9 -> Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                PlayerViewMode.FULL_FILL -> Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            }

            Box(
                modifier = videoModifier,
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false

                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    // Intercept external URLs/redirects
                                    return true
                                }
                            }

                            val html = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                    <style>
                                        body { margin: 0; padding: 0; background-color: #000000; overflow: hidden; display: flex; justify-content: center; align-items: center; height: 100vh; }
                                        .video-container { position: relative; width: 100%; height: 100%; }
                                        iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
                                    </style>
                                </head>
                                <body>
                                    <div class="video-container">
                                        <iframe id="player" src="$embedUrl?enablejsapi=1&autoplay=1&rel=0&controls=1" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                                    </div>
                                    <script>
                                        function setSpeed(rate) {
                                            var iframe = document.getElementById('player');
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage(JSON.stringify({
                                                    'event': 'command',
                                                    'func': 'setPlaybackRate',
                                                    'args': [rate]
                                                }), '*');
                                            }
                                        }
                                        function seekBy(seconds) {
                                            var iframe = document.getElementById('player');
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage(JSON.stringify({
                                                    'event': 'command',
                                                    'func': 'seekTo',
                                                    'args': [seconds, true]
                                                }), '*');
                                            }
                                        }
                                    </script>
                                </body>
                                </html>
                            """.trimIndent()

                            loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- MEDIA CONTROLS & TOOLS PANEL ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF19142E),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Quick Action Tools: Speed & View Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Speed,
                                contentDescription = "Speed",
                                tint = AccountBrightGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Speed:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Viewing Mode Toggle
                        Surface(
                            onClick = {
                                viewMode = if (viewMode == PlayerViewMode.FIT_16_9) PlayerViewMode.FULL_FILL else PlayerViewMode.FIT_16_9
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoPurple.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, IndigoPurple)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewMode == PlayerViewMode.FIT_16_9) Icons.Outlined.CropFree else Icons.Outlined.AspectRatio,
                                    contentDescription = "View Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (viewMode == PlayerViewMode.FIT_16_9) "16:9 Fit" else "Wide Fill",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Speed Selector Chips
                    val speedOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        speedOptions.forEach { speed ->
                            val isSelected = currentSpeed == speed
                            val label = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x"
                            Surface(
                                onClick = {
                                    currentSpeed = speed
                                    webViewRef?.evaluateJavascript(
                                        "if(typeof setSpeed==='function'){ setSpeed($speed); }",
                                        null
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccountBrightGold else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.testTag("speed_chip_$speed")
                            ) {
                                Text(
                                    text = if (speed == 1.0f) "1.0x" else label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF141026) else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Player Control Buttons (Rewind / Forward 10s & Completion Status)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                webViewRef?.evaluateJavascript("if(typeof seekBy==='function'){ seekBy(-10); }", null)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Outlined.Replay10, contentDescription = "Rewind 10s", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("10s Back", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                webViewRef?.evaluateJavascript("if(typeof seekBy==='function'){ seekBy(10); }", null)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("10s Forward", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Outlined.Forward10, contentDescription = "Forward 10s", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- MINIMAL LESSON METADATA (No wordy over-explanation) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IndigoPurple.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "Topic: ${lesson.topic}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccountBrightGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealTile.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = TealTile, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Completed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealTile
                            )
                        }
                    }
                }

                if (lesson.description.isNotBlank()) {
                    Text(
                        text = lesson.description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("done_watching_button")
                ) {
                    Text(
                        text = "Back to Video Lessons",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
