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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.theme.*

private val AccountBrightGold = Color(0xFFFFD54F)
private val AccountFieldBg = Color(0xFFF8F7FC)

enum class ClassesStep {
    SELECT_CLASS,
    SELECT_SUBJECT,
    BROWSE_LESSONS
}

@Composable
fun ClassesScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var currentStep by remember { mutableStateOf(ClassesStep.SELECT_CLASS) }
    var selectedClass by remember { mutableStateOf<ClassLevel?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var activePlayingLesson by remember { mutableStateOf<VideoLesson?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val allLessons = remember { DefaultVideoLessons }

    BackHandler {
        when {
            searchQuery.isNotBlank() -> searchQuery = ""
            currentStep == ClassesStep.BROWSE_LESSONS -> currentStep = ClassesStep.SELECT_SUBJECT
            currentStep == ClassesStep.SELECT_SUBJECT -> currentStep = ClassesStep.SELECT_CLASS
            currentStep == ClassesStep.SELECT_CLASS -> onNavigateBack()
        }
    }

    val globalSearchResults = remember(searchQuery, allLessons) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allLessons.filter {
                it.title.lowercase().contains(q) ||
                    it.subject.lowercase().contains(q) ||
                    it.topic.lowercase().contains(q) ||
                    it.teacherName.lowercase().contains(q) ||
                    it.targetClass.lowercase().contains(q)
            }
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
                currentStep = currentStep,
                selectedClass = selectedClass,
                selectedSubject = selectedSubject,
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onClearSearch = {
                    searchQuery = ""
                    focusManager.clearFocus()
                },
                onBack = {
                    when {
                        searchQuery.isNotBlank() -> searchQuery = ""
                        currentStep == ClassesStep.BROWSE_LESSONS -> currentStep = ClassesStep.SELECT_SUBJECT
                        currentStep == ClassesStep.SELECT_SUBJECT -> currentStep = ClassesStep.SELECT_CLASS
                        currentStep == ClassesStep.SELECT_CLASS -> onNavigateBack()
                    }
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isNotBlank()) {
                    if (globalSearchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Outlined.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Text(text = "No video lessons found for \"$searchQuery\"", color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(globalSearchResults, key = { it.id }) { lesson ->
                                VideoLessonDropdownCard(
                                    lesson = lesson,
                                    onPlayClick = { activePlayingLesson = lesson }
                                )
                            }
                        }
                    }
                } else {
                    when (currentStep) {
                        ClassesStep.SELECT_CLASS -> {
                            ClassesSelectClassView(
                                onSelectClass = { level ->
                                    selectedClass = level
                                    currentStep = ClassesStep.SELECT_SUBJECT
                                }
                            )
                        }

                        ClassesStep.SELECT_SUBJECT -> {
                            selectedClass?.let { cls ->
                                ClassesSelectSubjectView(
                                    classLevel = cls,
                                    onSelectSubject = { subj ->
                                        selectedSubject = subj
                                        currentStep = ClassesStep.BROWSE_LESSONS
                                    }
                                )
                            }
                        }

                        ClassesStep.BROWSE_LESSONS -> {
                            selectedClass?.let { cls ->
                                selectedSubject?.let { subj ->
                                    ClassesBrowseLessonsView(
                                        classLevel = cls,
                                        subject = subj,
                                        allLessons = allLessons,
                                        onPlayLesson = { lesson ->
                                            activePlayingLesson = lesson
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    activePlayingLesson?.let { lesson ->
        VideoPlayerModalDialog(
            lesson = lesson,
            onDismiss = { activePlayingLesson = null }
        )
    }
}

@Composable
private fun ClassesTopHeader(
    currentStep: ClassesStep,
    selectedClass: ClassLevel?,
    selectedSubject: String?,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onBack: () -> Unit
) {
    val title = when (currentStep) {
        ClassesStep.SELECT_CLASS -> "Lessons"
        ClassesStep.SELECT_SUBJECT -> "${selectedClass?.code ?: "Lessons"} Subjects"
        ClassesStep.BROWSE_LESSONS -> "${selectedSubject ?: "Lessons"} • ${selectedClass?.code ?: ""}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = IndigoPurple,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
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
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SubjectSearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ClassesSelectClassView(onSelectClass: (ClassLevel) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ClassLevel.values().forEach { level ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clickable { onSelectClass(level) }
                    .testTag("class_level_card_${level.code.lowercase().replace(".", "")}"),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = IndigoPurple.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = level.code,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = IndigoPurple
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = level.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Select",
                        tint = IndigoPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassesSelectSubjectView(
    classLevel: ClassLevel,
    onSelectSubject: (String) -> Unit
) {
    val subjects = DefaultCurriculumSubjects

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        subjects.forEach { subj ->
            RevisionSubjectCard(
                subject = subj,
                onClick = { onSelectSubject(subj.name) }
            )
        }
    }
}

@Composable
fun ClassesBrowseLessonsView(
    classLevel: ClassLevel,
    subject: String,
    allLessons: List<VideoLesson>,
    onPlayLesson: (VideoLesson) -> Unit
) {
    val filteredLessons = remember(classLevel, subject, allLessons) {
        allLessons.filter {
            it.targetClass.equals(classLevel.code, ignoreCase = true) &&
                it.subject.equals(subject, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (filteredLessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Outlined.OndemandVideo, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text(text = "No video lessons available for $subject (${classLevel.code}).", textAlign = TextAlign.Center, color = TextMuted, fontSize = 14.sp)
                }
            }
        } else {
            filteredLessons.forEach { lesson ->
                VideoLessonDropdownCard(
                    lesson = lesson,
                    onPlayClick = { onPlayLesson(lesson) }
                )
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

@Composable
fun VideoPlayerModalDialog(
    lesson: VideoLesson,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val embedUrl = getYouTubeEmbedUrl(lesson.youtubeVideoUrl)

    LaunchedEffect(lesson.id) {
        VideoLessonProgressManager.markCompleted(context, lesson.id, lesson.title, lesson.subject)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .testTag("video_player_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1934),
            border = BorderStroke(1.dp, AccountBrightGold),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.subject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccountBrightGold
                        )
                        Text(
                            text = lesson.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
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
                                webChromeClient = WebChromeClient()
                                webViewClient = WebViewClient()
                                val html = """
                                    <!DOCTYPE html>
                                    <html>
                                    <body style="margin:0;padding:0;background-color:#000000;">
                                        <iframe width="100%" height="100%" src="$embedUrl" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                                    </body>
                                    </html>
                                """.trimIndent()
                                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                            }
                        }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Topic: ${lesson.topic} • By ${lesson.teacherName}",
                        fontSize = 12.sp,
                        color = LightLavender
                    )
                    Text(
                        text = lesson.description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AccountBrightGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Done Watching", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
                }
            }
        }
    }
}
