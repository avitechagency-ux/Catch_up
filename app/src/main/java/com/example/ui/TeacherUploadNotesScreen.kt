package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Validates that an uploaded file name is a Word (.docx) document.
 */
fun isAllowedDocxFile(fileName: String): Boolean {
    val trimmed = fileName.trim().lowercase(Locale.ROOT)
    return trimmed.endsWith(".docx")
}

val DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

/**
 * Teacher Upload Notes Screen (Original layout restored).
 *
 * Fields synced with `public.notes_uploads` in Supabase:
 * 1. Upload Document (.docx file picker)
 * 2. Content Type (Notes vs Assignment)
 * 3. Note / Assignment Title (Text field where teacher types the title)
 * 4. Subject (Dropdown selector)
 * 5. Target Class (Dropdown selector S.1 - S.6 only)
 * 6. Due Date (When Assignment is selected)
 * 7. Description / Summary (Optional text field)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherUploadNotesScreen(
    modifier: Modifier = Modifier,
    subjectName: String = "Geography",
    subjectIcon: ImageVector = Icons.Outlined.Public,
    availableSubjects: List<TeacherSubjectItem> = DefaultTeacherSubjects,
    assignedClasses: List<String> = UgandanSecondaryClasses,
    teacherIdCode: String = "",
    connectedStudents: List<ConnectedStudent> = emptyList(),
    existingNote: SubjectUploadItem? = null,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: (SubjectUploadItem) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isEditMode = existingNote != null

    val initialSubject = remember(availableSubjects, subjectName, existingNote) {
        existingNote?.subject
            ?: subjectName.takeIf { sub -> sub.isNotBlank() && availableSubjects.any { it.name.equals(sub, ignoreCase = true) } }
            ?: availableSubjects.firstOrNull()?.name
            ?: "Geography"
    }

    var noteTitle by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    var selectedSubject by remember(initialSubject) { mutableStateOf(initialSubject) }
    var uploadType by remember(existingNote) { mutableStateOf(existingNote?.type ?: "Notes") }
    var noteDescription by remember(existingNote) { mutableStateOf(existingNote?.description ?: "") }

    val availableClassesForSubject = remember(assignedClasses) {
        if (assignedClasses.isNotEmpty()) assignedClasses else UgandanSecondaryClasses
    }

    var selectedClassStream by remember(existingNote, availableClassesForSubject) {
        val initialClass = existingNote?.targetClass?.let { normalizeSecondaryClass(it) }
        mutableStateOf(
            if (initialClass != null && initialClass in availableClassesForSubject) {
                initialClass
            } else {
                availableClassesForSubject.firstOrNull() ?: "S.1"
            }
        )
    }

    // Dynamic stream resolution strictly based on the selected class and this teacher's connected students
    val availableStreamsForClass = remember(connectedStudents, teacherIdCode, selectedClassStream) {
        val cleanCode = teacherIdCode.trim().uppercase()
        val canonicalClass = toCanonicalClassId(selectedClassStream)
        connectedStudents
            .filter { it.teacherIdCode.trim().uppercase() == cleanCode }
            .filter { toCanonicalClassId(it.studentClass) == canonicalClass }
            .map { com.example.data.normalizeStream(it.stream) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val initialTargetStreams = existingNote?.targetStreams ?: "ALL"
    val parsedInitialStreams = remember(initialTargetStreams) { com.example.data.parseTargetStreams(initialTargetStreams) }
    var isAllStreamsSelected by remember(initialTargetStreams) { mutableStateOf("ALL" in parsedInitialStreams) }
    val selectedStreamsList = remember(initialTargetStreams) {
        mutableStateListOf<String>().apply {
            if ("ALL" !in parsedInitialStreams) {
                addAll(parsedInitialStreams)
            }
        }
    }

    // When target class changes, auto-clean streams that don't belong to the newly selected class
    androidx.compose.runtime.LaunchedEffect(availableStreamsForClass) {
        if (availableStreamsForClass.isEmpty()) {
            isAllStreamsSelected = true
            selectedStreamsList.clear()
        } else {
            val invalid = selectedStreamsList.filter { it !in availableStreamsForClass }
            selectedStreamsList.removeAll(invalid)
            if (selectedStreamsList.isEmpty() && !isAllStreamsSelected) {
                isAllStreamsSelected = true
            }
        }
    }

    var selectedFileName by remember(existingNote) { mutableStateOf<String?>(existingNote?.fileName) }
    var selectedFileSize by remember(existingNote) { mutableStateOf<String?>(existingNote?.fileSize) }
    var selectedFileBytes by remember(existingNote) { mutableStateOf<ByteArray?>(existingNote?.fileBytes) }

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showSubjectSheet by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showClassSheet by remember { mutableStateOf(false) }
    var showStreamDropdown by remember { mutableStateOf(false) }

    // File Picker Launcher strictly for Word (.docx) documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawName = uri.lastPathSegment
                ?.substringAfterLast('/')
                ?.substringAfterLast(':')
                ?.takeIf { it.isNotBlank() }
                ?: "study_notes.docx"

            var resolvedDisplayName = rawName
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        val cursorName = cursor.getString(nameIndex)
                        if (!cursorName.isNullOrBlank()) {
                            resolvedDisplayName = cursorName
                        }
                    }
                }
            } catch (_: Exception) {
            }

            if (!isAllowedDocxFile(resolvedDisplayName) && !isAllowedDocxFile(rawName)) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Only Word (.docx) documents are allowed so students can read clearly without zooming.")
                }
                return@rememberLauncherForActivityResult
            }

            val finalDocxName = if (resolvedDisplayName.lowercase(Locale.ROOT).endsWith(".docx")) {
                resolvedDisplayName
            } else {
                "${resolvedDisplayName.substringBeforeLast('.')}.docx"
            }

            val bytes = try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (_: Exception) {
                null
            }

            selectedFileName = finalDocxName
            selectedFileBytes = bytes
            selectedFileSize = if (bytes != null && bytes.isNotEmpty()) {
                val kb = bytes.size / 1024.0
                if (kb >= 1024.0) {
                    String.format(Locale.US, "%.1f MB", kb / 1024.0)
                } else {
                    String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
                }
            } else {
                "1.2 MB"
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("teacher_upload_notes_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // TOP HEADER
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndigoPurple)
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
                            .size(40.dp)
                            .testTag("upload_notes_back_button")
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
                            text = if (isEditMode) "Edit ${existingNote?.type ?: "Note"}" else "Upload Notes",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("upload_notes_title")
                        )
                        Text(
                            text = "Upload Word (.docx) notes or assignments for your students",
                            fontSize = 12.sp,
                            color = LightLavender
                        )
                    }
                }
            }

            // ==========================================
            // FORM BODY
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. FIELD — UPLOAD FILE (.DOCX)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Upload Document (.docx)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    val stroke = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                    val dashedBorderColor = IndigoPurple.copy(alpha = 0.45f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftLavenderGrey.copy(alpha = 0.65f))
                            .drawBehind {
                                drawRoundRect(
                                    color = dashedBorderColor,
                                    style = stroke,
                                    cornerRadius = CornerRadius(14.dp.toPx())
                                )
                            }
                            .clickable {
                                filePickerLauncher.launch(arrayOf(DOCX_MIME_TYPE))
                            }
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .testTag("upload_file_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPurple.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = "Upload File",
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Text(
                                text = if (selectedFileName != null) {
                                    "Tap to change selected .docx file"
                                } else {
                                    "Tap to select a Word (.docx) file"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Only .docx files are accepted so notes adjust clearly to any phone screen",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        filePickerLauncher.launch(arrayOf(DOCX_MIME_TYPE))
                                    }
                                    .testTag("browse_files_button"),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, IndigoPurple)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FolderOpen,
                                        contentDescription = null,
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Choose .docx File",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPurple
                                    )
                                }
                            }
                        }
                    }

                    if (selectedFileName != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("file_preview_card"),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftLavenderGrey,
                            border = BorderStroke(1.dp, IndigoPurple.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedFileName ?: "",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("selected_file_name")
                                        )
                                        Text(
                                            text = "${selectedFileSize ?: "1.2 MB"} • DOCX Document",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        selectedFileName = null
                                        selectedFileSize = null
                                        selectedFileBytes = null
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("remove_file_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove File",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. FIELD — CONTENT TYPE (Notes vs Assignment)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Content Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            "Notes" to "Study Notes",
                            "Assignment" to "Assignment"
                        ).forEach { (typeKey, label) ->
                            val isSelected = uploadType.equals(typeKey, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { uploadType = typeKey }
                                    .testTag("upload_type_${typeKey.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) IndigoPurple else SoftLavenderGrey,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) IndigoPurple else BorderGrey
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (typeKey == "Notes") {
                                            Icons.Outlined.Description
                                        } else {
                                            Icons.Outlined.Assignment
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else TextDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextDark
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. FIELD — NOTE / ASSIGNMENT TITLE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (uploadType.equals("Assignment", ignoreCase = true)) {
                            "Assignment Title"
                        } else {
                            "Note Title"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        placeholder = {
                            Text(
                                text = if (uploadType.equals("Assignment", ignoreCase = true)) {
                                    "Enter assignment title (e.g. Term 3 Exercise)"
                                } else {
                                    "Enter note title (e.g. Plate Tectonics & Vulcanicity)"
                                },
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            unfocusedBorderColor = BorderGrey,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = IndigoPurple,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note_title")
                    )
                }

                // 4. FIELD — SUBJECT
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Subject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSubjectDropdown = true }
                                .testTag("selector_subject"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = selectedSubject,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Select Subject",
                                    tint = IconGrey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        val subjectOptions = if (availableSubjects.isNotEmpty()) {
                            availableSubjects.map { it.name }
                        } else {
                            listOf("Geography", "Math", "Physics", "Chemistry", "Biology", "English", "History")
                        }

                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            subjectOptions.forEach { subjectOption ->
                                val isSelected = subjectOption.equals(selectedSubject, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = subjectOption,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) IndigoPurple else TextDark
                                        )
                                    },
                                    onClick = {
                                        selectedSubject = subjectOption
                                        showSubjectDropdown = false
                                    },
                                    modifier = Modifier.testTag("subject_option_$subjectOption")
                                )
                            }
                        }
                    }
                }

                // 5. FIELD — TARGET CLASS (Dropdown Only: S.1 - S.6)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Target Class",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showClassDropdown = true }
                                .testTag("selector_class_stream"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Group,
                                        contentDescription = null,
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = selectedClassStream,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Select Target Class",
                                    tint = IconGrey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showClassDropdown,
                            onDismissRequest = { showClassDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .testTag("dropdown_class_selection")
                        ) {
                            availableClassesForSubject.forEach { classOption ->
                                val isSelected = classOption == selectedClassStream
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = classOption,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) IndigoPurple else TextDark
                                        )
                                    },
                                    onClick = {
                                        selectedClassStream = classOption
                                        showClassDropdown = false
                                    },
                                    modifier = Modifier.testTag("class_option_$classOption")
                                )
                            }
                        }
                    }
                }

                // 5b. FIELD — STREAM TARGETING (Dropdown filtered by selected class and teacher's connected streams)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Streams",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        if (availableStreamsForClass.isNotEmpty()) {
                            Text(
                                text = "${availableStreamsForClass.size} available in $selectedClassStream",
                                fontSize = 12.sp,
                                color = IndigoPurple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showStreamDropdown = true
                                }
                                .testTag("selector_target_stream"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (availableStreamsForClass.isEmpty()) SoftLavenderGrey.copy(alpha = 0.5f) else Color.White,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Group,
                                        contentDescription = null,
                                        tint = if (availableStreamsForClass.isEmpty()) IconGrey else IndigoPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    if (availableStreamsForClass.isEmpty()) {
                                        Text(
                                            text = "No streams",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else {
                                        val streamDisplayText = when {
                                            isAllStreamsSelected || selectedStreamsList.isEmpty() -> "All Streams ($selectedClassStream)"
                                            selectedStreamsList.size == 1 -> "Stream ${selectedStreamsList.first()}"
                                            else -> "Streams: ${selectedStreamsList.joinToString(", ")}"
                                        }
                                        Text(
                                            text = streamDisplayText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoPurple,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Select Stream",
                                    tint = IconGrey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showStreamDropdown,
                            onDismissRequest = { showStreamDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .testTag("dropdown_stream_selection")
                        ) {
                            if (availableStreamsForClass.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "No streams",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                            Text(
                                                text = "No streams connected yet in $selectedClassStream",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    },
                                    onClick = { showStreamDropdown = false },
                                    modifier = Modifier.testTag("stream_option_no_streams")
                                )
                            } else {
                                val isAllActive = isAllStreamsSelected || selectedStreamsList.isEmpty()
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "All Streams ($selectedClassStream)",
                                                fontSize = 14.sp,
                                                fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isAllActive) IndigoPurple else TextDark
                                            )
                                            if (isAllActive) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = IndigoPurple,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        isAllStreamsSelected = true
                                        selectedStreamsList.clear()
                                        showStreamDropdown = false
                                    },
                                    modifier = Modifier.testTag("stream_option_all")
                                )

                                HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))

                                availableStreamsForClass.forEach { streamOption ->
                                    val isStreamChecked = !isAllStreamsSelected && streamOption in selectedStreamsList
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Stream $streamOption",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isStreamChecked) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isStreamChecked) IndigoPurple else TextDark
                                                )
                                                if (isStreamChecked) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = IndigoPurple,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            isAllStreamsSelected = false
                                            selectedStreamsList.clear()
                                            selectedStreamsList.add(streamOption)
                                            showStreamDropdown = false
                                        },
                                        modifier = Modifier.testTag("stream_option_$streamOption")
                                    )
                                }
                            }
                        }
                    }

                    if (availableStreamsForClass.isEmpty()) {
                        Text(
                            text = "No streams connected yet for $selectedClassStream. Notes will be sent to all students in $selectedClassStream.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        Text(
                            text = if (isAllStreamsSelected || selectedStreamsList.isEmpty()) {
                                "All students in $selectedClassStream will receive these ${uploadType.lowercase()}."
                            } else {
                                "Only students enrolled in $selectedClassStream (${selectedStreamsList.joinToString(", ")}) will receive these ${uploadType.lowercase()}."
                            },
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                // 7. FIELD — DESCRIPTION / INSTRUCTIONS (Optional)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Description (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    OutlinedTextField(
                        value = noteDescription,
                        onValueChange = { noteDescription = it },
                        placeholder = {
                            Text(
                                text = "Add a short summary or instructions for students...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            unfocusedBorderColor = BorderGrey,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = IndigoPurple,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note_description")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 8. SUBMIT BUTTON
                Button(
                    onClick = {
                        val currentFileName = selectedFileName
                        when {
                            noteTitle.isBlank() -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please enter a title for the ${uploadType.lowercase()}")
                                }
                            }
                            currentFileName == null -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please select a Word (.docx) file to upload")
                                }
                            }
                            !isAllowedDocxFile(currentFileName) -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Only Word (.docx) files are supported")
                                }
                            }
                            else -> {
                                val normalizedClass = normalizeSecondaryClass(selectedClassStream)
                                val resolvedDescription = noteDescription.trim().ifBlank {
                                    "$uploadType for $normalizedClass ($selectedSubject)"
                                }
                                val generatedOrSelectedBytes = selectedFileBytes ?: generateDocxBytes(
                                    title = noteTitle.trim(),
                                    subject = selectedSubject,
                                    targetClass = normalizedClass,
                                    authorName = "Subject Teacher",
                                    description = resolvedDescription
                                )
                                val resolvedTargetStreams = com.example.data.formatTargetStreams(
                                    if (isAllStreamsSelected) emptyList() else selectedStreamsList
                                )
                                val newUpload = SubjectUploadItem(
                                    id = existingNote?.id ?: "upload_${System.currentTimeMillis()}",
                                    title = noteTitle.trim(),
                                    subject = selectedSubject,
                                    targetClass = normalizedClass,
                                    targetStreams = resolvedTargetStreams,
                                    type = uploadType,
                                    dateUploaded = existingNote?.dateUploaded
                                        ?: SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()),
                                    dueDate = null,
                                    description = resolvedDescription,
                                    fileName = currentFileName,
                                    fileSize = selectedFileSize ?: "1.2 MB",
                                    fileBytes = generatedOrSelectedBytes,
                                    teacherIdCode = existingNote?.teacherIdCode ?: ""
                                )
                                onSaveSuccess(newUpload)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_upload_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPurple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) {
                            "Save Changes"
                        } else if (uploadType.equals("Assignment", ignoreCase = true)) {
                            "Upload Assignment"
                        } else {
                            "Upload Notes"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showSubjectSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSubjectSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            modifier = Modifier.testTag("sheet_subject_selection")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Select Subject",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                val subjectOptions = if (availableSubjects.isNotEmpty()) {
                    availableSubjects.map { it.name }
                } else {
                    listOf("Geography", "Math", "Physics", "Chemistry", "Biology", "English", "History")
                }
                subjectOptions.forEach { subjectOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSubject = subjectOption
                                showSubjectSheet = false
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = subjectOption, fontSize = 15.sp, color = TextDark)
                        if (subjectOption.equals(selectedSubject, ignoreCase = true)) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Selected",
                                tint = IndigoPurple
                            )
                        }
                    }
                    HorizontalDivider(color = BorderGrey.copy(alpha = 0.4f))
                }
            }
        }
    }

    if (showClassSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showClassSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            modifier = Modifier.testTag("sheet_class_selection")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Select Target Class",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                availableClassesForSubject.forEach { classOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedClassStream = classOption
                                showClassSheet = false
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = classOption, fontSize = 15.sp, color = TextDark)
                        if (classOption == selectedClassStream) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Selected",
                                tint = IndigoPurple
                            )
                        }
                    }
                    HorizontalDivider(color = BorderGrey.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun TeacherUploadNotesScreenPreview() {
    TeacherUploadNotesScreen()
}
