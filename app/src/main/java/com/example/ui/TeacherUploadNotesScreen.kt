package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberTile
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Upload & Edit Notes/Assessments Screen for the Teacher Panel.
 * Allows teachers to upload new study notes or assignments for their assigned subjects and classes,
 * with vibrant colors matching the platform style.
 *
 * Subject Selection Rule: Teachers can ONLY select from their configured subjects in the Subjects page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherUploadNotesScreen(
    subjectName: String = "Geography",
    subjectIcon: ImageVector = Icons.Outlined.Public,
    availableSubjects: List<TeacherSubjectItem> = DefaultTeacherSubjects,
    assignedClasses: List<String> = listOf("S.4 Green", "S.4 Blue", "S.5 Blue", "S.5 Arts"),
    existingNote: SubjectUploadItem? = null,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: (SubjectUploadItem) -> Unit = {}
) {
    val isEditing = existingNote != null
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Find initial subject from availableSubjects or fallback to subjectName
    var currentSubjectItem by remember(subjectName, availableSubjects) {
        mutableStateOf(
            availableSubjects.firstOrNull { it.name.equals(existingNote?.subject ?: subjectName, ignoreCase = true) }
                ?: availableSubjects.firstOrNull()
                ?: TeacherSubjectItem(
                    id = "geography",
                    name = subjectName,
                    classes = "S.4, S.5",
                    noteCount = 0,
                    assignmentCount = 0,
                    icon = subjectIcon,
                    color = CoralTile
                )
        )
    }

    // Dynamic classes based on the active subject
    val dynamicClasses = remember(currentSubjectItem) {
        if (currentSubjectItem.name.equals("Math", ignoreCase = true)) {
            listOf("S.3 Blue", "S.3 Green", "S.4 Green", "S.4 Blue")
        } else if (currentSubjectItem.name.equals("Geography", ignoreCase = true)) {
            listOf("S.4 Green", "S.4 Blue", "S.5 Blue", "S.5 Arts")
        } else {
            assignedClasses
        }
    }

    // Form states
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var selectedClass by remember(currentSubjectItem) {
        mutableStateOf(
            if (existingNote != null && existingNote.subject.equals(currentSubjectItem.name, ignoreCase = true)) {
                existingNote.targetClass
            } else {
                dynamicClasses.firstOrNull() ?: ""
            }
        )
    }
    var selectedType by remember {
        mutableStateOf(
            if (existingNote?.type?.equals("Assignment", ignoreCase = true) == true) "Assignment" else "Notes"
        )
    }
    var selectedFileName by remember {
        mutableStateOf(
            if (existingNote != null) existingNote.fileName.ifBlank { "${existingNote.title.lowercase().replace(" ", "_")}.pdf" } else null
        )
    }
    var selectedFileSize by remember {
        mutableStateOf(existingNote?.fileSize?.ifBlank { "2.4 MB" } ?: "2.4 MB")
    }
    var selectedFileBytes by remember {
        mutableStateOf<ByteArray?>(existingNote?.fileBytes)
    }

    var showClassPickerSheet by remember { mutableStateOf(false) }
    var showSubjectPicker by remember { mutableStateOf(false) }
    var showSampleFilePicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var queriedName: String? = null
            var queriedSize: Long = -1L
            runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIdx >= 0) queriedName = cursor.getString(nameIdx)
                        val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) queriedSize = cursor.getLong(sizeIdx)
                    }
                }
            }
            val rawBytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()
            selectedFileBytes = rawBytes

            val fallbackPath = uri.lastPathSegment ?: "document.pdf"
            val rawName = queriedName?.takeIf { it.isNotBlank() }
                ?: if (fallbackPath.contains("/")) fallbackPath.substringAfterLast("/") else fallbackPath
            val cleanName = rawName.substringAfterLast(":")
            selectedFileName = if (cleanName.endsWith(".pdf", ignoreCase = true)) cleanName else "$cleanName.pdf"

            val lengthBytes = when {
                rawBytes != null && rawBytes.isNotEmpty() -> rawBytes.size.toLong()
                queriedSize > 0L -> queriedSize
                else -> runCatching {
                    context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
                }.getOrDefault(-1L)
            }
            if (lengthBytes > 0L) {
                val mb = lengthBytes.toDouble() / (1024.0 * 1024.0)
                selectedFileSize = if (mb >= 0.1) {
                    String.format(java.util.Locale.US, "%.2f MB", mb)
                } else {
                    val kb = (lengthBytes.toDouble() / 1024.0).coerceAtLeast(1.0)
                    String.format(java.util.Locale.US, "%.1f KB", kb)
                }
            } else {
                selectedFileSize = "2.4 MB"
            }
        } else {
            showSampleFilePicker = true
        }
    }

    val isFormValid = title.isNotBlank() && selectedClass.isNotBlank() && !selectedFileName.isNullOrBlank()

    val subjectColor = currentSubjectItem.color

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_upload_notes_screen"),
        containerColor = Color(0xFFF9F8FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // 1. TOP HEADER: Purple curved banner
            // Matching Students & Admin headers
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
                    .testTag("upload_notes_top_bar")
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("upload_notes_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isEditing) "Edit Material" else "Upload",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("upload_notes_header_title")
                        )
                        Text(
                            text = "Notes & Assessments",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender
                        )
                    }
                }
            }

            // ==========================================
            // 2. BODY: COLORFUL, CLEAN FORM
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. VIBRANT SUBJECT SELECTION CARD
                // Displays teacher's selected subject and allows switching ONLY between their own subjects
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = subjectColor.copy(alpha = 0.25f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (availableSubjects.size > 1) {
                                showSubjectPicker = true
                            }
                        }
                        .testTag("subject_indicator_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = subjectColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(subjectColor, subjectColor.copy(alpha = 0.85f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = currentSubjectItem.icon,
                                            contentDescription = currentSubjectItem.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Subject Taught",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = currentSubjectItem.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.testTag("subject_indicator_name")
                                    )
                                }
                            }

                            if (availableSubjects.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.20f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Switch",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dropdown limited STRICTLY to teacher's actual subjects from the Subjects page
                DropdownMenu(
                    expanded = showSubjectPicker,
                    onDismissRequest = { showSubjectPicker = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    availableSubjects.forEach { subjItem ->
                        val isSelected = subjItem.id == currentSubjectItem.id
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = subjItem.icon,
                                        contentDescription = null,
                                        tint = subjItem.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${subjItem.name} (${subjItem.classes})",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) subjItem.color else TextDark
                                    )
                                }
                            },
                            onClick = {
                                currentSubjectItem = subjItem
                                showSubjectPicker = false
                            }
                        )
                    }
                }

                // 2. DOCUMENT TYPE SELECTOR (Notes vs Assessment)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Document Type",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Notes Button
                        val isNotes = selectedType == "Notes"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(
                                    elevation = if (isNotes) 3.dp else 0.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    ambientColor = TealTile.copy(alpha = 0.3f)
                                )
                                .clickable { selectedType = "Notes" }
                                .testTag("toggle_notes_type"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isNotes) TealTile else Color.White,
                            border = if (!isNotes) androidx.compose.foundation.BorderStroke(1.dp, BorderGrey) else null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = if (isNotes) Color.White else TealTile,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Study Notes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNotes) Color.White else TextDark
                                )
                            }
                        }

                        // Assignment / Assessment Button
                        val isAssignment = selectedType == "Assignment"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(
                                    elevation = if (isAssignment) 3.dp else 0.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    ambientColor = CoralTile.copy(alpha = 0.3f)
                                )
                                .clickable { selectedType = "Assignment" }
                                .testTag("toggle_assignment_type"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAssignment) CoralTile else Color.White,
                            border = if (!isAssignment) androidx.compose.foundation.BorderStroke(1.dp, BorderGrey) else null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Assignment,
                                    contentDescription = null,
                                    tint = if (isAssignment) Color.White else CoralTile,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Assessment",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAssignment) Color.White else TextDark
                                )
                            }
                        }
                    }
                }

                // 3. FIELD — TITLE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Title / Topic",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (title.isEmpty()) {
                                Text(
                                    text = "e.g. Plate Tectonics & Volcanicity",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_upload_title"),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(IndigoPurple)
                            )
                        }
                    }
                }

                // 4. FIELD — TARGET CLASS & STREAM (Filtered to this subject's classes)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Target Class & Stream (${currentSubjectItem.classes})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showClassPickerSheet = true }
                            .testTag("selector_class_stream"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedClass.isNotBlank()) selectedClass else "Select Class",
                                fontSize = 14.sp,
                                color = if (selectedClass.isNotBlank()) TextDark else TextMuted,
                                fontWeight = if (selectedClass.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                            )

                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Dropdown chevron",
                                tint = IconGrey,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 5. FIELD — DOCUMENT ATTACHMENT
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PDF Document",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    if (selectedFileName == null) {
                        // Empty State: Dashed Purple Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .drawBehind {
                                    drawRoundRect(
                                        color = IndigoPurple.copy(alpha = 0.4f),
                                        cornerRadius = CornerRadius(16.dp.toPx()),
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                        )
                                    )
                                }
                                .clickable {
                                    try {
                                        pdfPickerLauncher.launch("application/pdf")
                                    } catch (e: Exception) {
                                        showSampleFilePicker = true
                                    }
                                }
                                .testTag("card_select_document_empty"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = LightLavender.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.CloudUpload,
                                            contentDescription = "Upload Cloud",
                                            tint = IndigoPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Tap to choose PDF from device",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPurple
                                )
                                Text(
                                    text = "Supports notes, revision papers & tests",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        // File Selected: Rich PDF Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("card_selected_document"),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPurple.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFEBEE),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.PictureAsPdf,
                                                contentDescription = "PDF",
                                                tint = Color(0xFFE53935),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = selectedFileName ?: "",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "PDF Document ($selectedFileSize) • Ready to upload",
                                            fontSize = 11.sp,
                                            color = TealTile,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        selectedFileName = null
                                        selectedFileBytes = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove file",
                                        tint = IconGrey,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. ACTION BUTTON: PUBLISH / UPLOAD
                Button(
                    onClick = {
                        if (isFormValid) {
                            val newItem = SubjectUploadItem(
                                id = existingNote?.id ?: "note-${System.currentTimeMillis()}",
                                title = title.trim(),
                                targetClass = selectedClass,
                                dateUploaded = existingNote?.dateUploaded ?: "Today",
                                subject = currentSubjectItem.name,
                                type = selectedType,
                                fileName = selectedFileName ?: "document.pdf",
                                fileSize = selectedFileSize,
                                fileBytes = selectedFileBytes
                            )
                            if (selectedFileBytes != null && selectedFileBytes!!.isNotEmpty()) {
                                savePdfBytesLocally(context, currentSubjectItem.name, title.trim(), selectedFileBytes!!)
                            }
                            onSaveSuccess(newItem)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please fill in Title, Class, and select a PDF file.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = if (isFormValid) 4.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = IndigoPurple.copy(alpha = 0.4f)
                        )
                        .testTag("button_save_upload_notes"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPurple,
                        disabledContainerColor = IndigoPurple.copy(alpha = 0.4f)
                    ),
                    enabled = isFormValid
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Save Changes" else "Upload to Students",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Class Picker Bottom Sheet (Filtered strictly to dynamic classes for this subject)
    if (showClassPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showClassPickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Select Class & Stream for ${currentSubjectItem.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(14.dp))

                dynamicClasses.forEach { cls ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedClass = cls
                                showClassPickerSheet = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedClass == cls) IndigoPurple.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cls,
                                fontSize = 15.sp,
                                fontWeight = if (selectedClass == cls) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedClass == cls) IndigoPurple else TextDark
                            )
                            if (selectedClass == cls) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Sample File Fallback Picker
    if (showSampleFilePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSampleFilePicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Select Sample Document",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(14.dp))

                listOf(
                    "${currentSubjectItem.name}_Comprehensive_Notes.pdf",
                    "${currentSubjectItem.name}_Topical_Revision_PastPapers.pdf",
                    "${currentSubjectItem.name}_Assessment_Questions.pdf"
                ).forEach { sampleName ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedFileName = sampleName
                                showSampleFilePicker = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9F8FD)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = sampleName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherUploadNotesScreenPreview() {
    TeacherUploadNotesScreen()
}
