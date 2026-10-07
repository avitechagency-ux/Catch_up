package com.example.ui.live

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiveClassDto
import com.example.data.LiveClassRepository
import com.example.notifications.NotificationHelper
import com.example.ui.UgandanSecondaryClasses
import com.example.ui.getSubjectThemeColor
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen enabling teachers to schedule a new Live Audio Lesson or Start Immediately.
 * Designed with signature Catch Up header, theme coloring, and polished input boxes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLiveClassScreen(
    teacherId: String = "teacher_01",
    teacherName: String = "Teacher",
    assignedSubjects: List<String> = listOf("Physics", "Mathematics", "Geography", "Biology"),
    assignedClasses: List<String> = UgandanSecondaryClasses,
    onNavigateBack: () -> Unit = {},
    onClassScheduled: (LiveClassDto) -> Unit = {},
    onStartImmediately: (classId: String, title: String, subject: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(assignedSubjects.firstOrNull() ?: "Physics") }
    var selectedClass by remember { mutableStateOf(assignedClasses.firstOrNull() ?: "S.4") }

    var isAllStreamsSelected by remember { mutableStateOf(true) }
    val selectedStreamsList = remember { mutableStateListOf<String>() }

    // Start mode: "schedule" vs "instant"
    var isInstantStart by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var selectedDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }
    calendar.add(Calendar.HOUR_OF_DAY, 1)
    var selectedTime by remember { mutableStateOf(timeFormatter.format(calendar.time)) }
    var selectedDurationMinutes by remember { mutableIntStateOf(45) }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // DatePicker Dialog
    val datePickerDialog = remember {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                selectedDate = dateFormatter.format(chosen.time)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    // TimePicker Dialog
    val timePickerDialog = remember {
        val now = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val chosenStr = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                selectedTime = chosenStr
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        )
    }

    val durationOptions = listOf(15, 30, 45, 60, 90)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("create_live_class_screen"),
        containerColor = Color.White,
        topBar = {
            // Signature Catch Up Header with Deep Maroon & rounded bottom (matching platform header)
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
                            .testTag("create_live_class_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isInstantStart) "Start Live Lesson" else "Schedule Live Lesson",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("create_live_class_header_title")
                        )
                        Text(
                            text = "Catch Up Audio Classroom",
                            fontSize = 12.sp,
                            color = GoldMustard
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            errorMessage?.let { err ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LiveIndicatorRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, LiveIndicatorRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = LiveIndicatorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // MODE SELECTOR: Schedule Lesson vs Start Immediately
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftPastelBackground, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isInstantStart = false
                            errorMessage = null
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isInstantStart) DeepMaroon else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = if (!isInstantStart) Color.White else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Schedule",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isInstantStart) Color.White else TextMuted
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isInstantStart = true
                            errorMessage = null
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isInstantStart) GoldMustard else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = null,
                                tint = if (isInstantStart) DeepMaroon else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Start Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInstantStart) DeepMaroon else TextMuted
                            )
                        }
                    }
                }
            }

            // 1. Lesson Title Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Lesson Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = "Enter lesson topic (e.g. Electromagnetic Induction)",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_class_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepMaroon,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = SoftPastelBackground,
                        cursorColor = DeepMaroon,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )
            }

            // 2. Subject & Class Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject Dropdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Subject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { subjectDropdownExpanded = true }
                                .testTag("subject_selector"),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPastelBackground,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedSubject,
                                    fontSize = 14.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            assignedSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = {
                                        selectedSubject = subj
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Class Dropdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Class",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { classDropdownExpanded = true }
                                .testTag("class_selector"),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPastelBackground,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedClass,
                                    fontSize = 14.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = classDropdownExpanded,
                            onDismissRequest = { classDropdownExpanded = false }
                        ) {
                            assignedClasses.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls) },
                                    onClick = {
                                        selectedClass = cls
                                        classDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2b. Stream Targeting (All Streams vs Specific Streams)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Target Streams",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isAllStreamsSelected = true
                                selectedStreamsList.clear()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isAllStreamsSelected,
                            onClick = {
                                isAllStreamsSelected = true
                                selectedStreamsList.clear()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = DeepMaroon)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "All $selectedClass Streams",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isAllStreamsSelected = false
                                if (selectedStreamsList.isEmpty()) selectedStreamsList.add("NORTH")
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isAllStreamsSelected,
                            onClick = {
                                isAllStreamsSelected = false
                                if (selectedStreamsList.isEmpty()) selectedStreamsList.add("NORTH")
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = DeepMaroon)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Specific Streams",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }
                }

                if (!isAllStreamsSelected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftPastelBackground, RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("NORTH", "SOUTH", "EAST", "WEST").forEach { st ->
                            val isChecked = st in selectedStreamsList
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (isChecked) selectedStreamsList.remove(st) else selectedStreamsList.add(st)
                                    }
                                    .padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedStreamsList.add(st) else selectedStreamsList.remove(st)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = DeepMaroon)
                                )
                                Text(
                                    text = st,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            }

            // 3. Date & Start Time Pickers (Visible in Schedule mode)
            if (!isInstantStart) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date picker
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { datePickerDialog.show() }
                                .testTag("date_picker_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPastelBackground,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedDate,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Time picker
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Start Time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { timePickerDialog.show() }
                                .testTag("time_picker_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPastelBackground,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedTime,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Duration Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Expected Duration",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durationOptions.forEach { dur ->
                        val isSelected = selectedDurationMinutes == dur
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { selectedDurationMinutes = dur },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) DeepMaroon else SoftPastelBackground,
                            border = BorderStroke(1.dp, if (isSelected) DeepMaroon else BorderGrey)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${dur}m",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Action Button
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        errorMessage = "Please enter a lesson title."
                        return@Button
                    }

                    val now = Calendar.getInstance()
                    val nowDateStr = dateFormatter.format(now.time)
                    val nowTimeStr = timeFormatter.format(now.time)

                    val finalDate = if (isInstantStart) nowDateStr else selectedDate
                    val finalTime = if (isInstantStart) nowTimeStr else selectedTime

                    if (!isInstantStart) {
                        try {
                            val checkFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val scheduledDateTime = checkFormat.parse("$finalDate $finalTime")
                            if (scheduledDateTime != null && scheduledDateTime.time < (System.currentTimeMillis() - 60000)) {
                                errorMessage = "Cannot schedule a live class in the past. Please choose a future time."
                                return@Button
                            }
                        } catch (_: Exception) {}
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        val resolvedTargetStreams = com.example.data.formatTargetStreams(
                            if (isAllStreamsSelected) emptyList() else selectedStreamsList
                        )
                        val newClass = LiveClassDto(
                            id = UUID.randomUUID().toString(),
                            schoolId = "UG_SEC_01",
                            teacherId = teacherId,
                            teacherName = teacherName,
                            classId = selectedClass,
                            targetStreams = resolvedTargetStreams,
                            subjectId = selectedSubject,
                            title = title.trim(),
                            description = "",
                            scheduledDate = finalDate,
                            scheduledStartTime = finalTime,
                            durationMinutes = selectedDurationMinutes,
                            status = if (isInstantStart) "live" else "scheduled"
                        )
                        val res = LiveClassRepository.createLiveClass(newClass)
                        if (res.isSuccess) {
                            if (isInstantStart) {
                                NotificationHelper.showLiveClassStartedNotification(
                                    context = context,
                                    classId = newClass.id,
                                    subject = newClass.subjectId,
                                    title = newClass.title,
                                    teacherName = teacherName
                                )
                                onStartImmediately(newClass.id, newClass.title, newClass.subjectId)
                            } else {
                                NotificationHelper.showLiveClassScheduledNotification(
                                    context = context,
                                    classId = newClass.id,
                                    subject = newClass.subjectId,
                                    title = newClass.title,
                                    scheduledDate = newClass.scheduledDate,
                                    scheduledTime = newClass.scheduledStartTime,
                                    targetClass = newClass.classId
                                )
                                onClassScheduled(res.getOrThrow())
                            }
                        } else {
                            errorMessage = "Failed to create class. Please try again."
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("schedule_live_class_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInstantStart) GoldMustard else DeepMaroon,
                    contentColor = if (isInstantStart) DeepMaroon else Color.White
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = if (isInstantStart) DeepMaroon else Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isInstantStart) Icons.Outlined.PlayArrow else Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isInstantStart) "Start Lesson Immediately" else "Schedule Live Lesson",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
