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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("upload_notes_title")
                        )
                    }
                }
            }

            // ==========================================
            // FORM BODY — BRIGHT, SPREAD & COLOURED DESIGN
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // 1. FIELD — UPLOAD FILE (.DOCX)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEDE9FE),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = null,
                                    tint = Color(0xFF4B3F8F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Document File (.docx)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    val stroke = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )
                    val dashedBorderColor = Color(0xFFCBD5E1)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .drawBehind {
                                drawRoundRect(
                                    color = dashedBorderColor,
                                    style = stroke,
                                    cornerRadius = CornerRadius(16.dp.toPx())
                                )
                            }
                            .clickable {
                                filePickerLauncher.launch(arrayOf(DOCX_MIME_TYPE))
                            }
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                            .testTag("upload_file_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = Color(0xFFEDE9FE)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Upload File",
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (selectedFileName != null) {
                                    "Tap to change selected Word document"
                                } else {
                                    "Select a Word (.docx) file"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        filePickerLauncher.launch(arrayOf(DOCX_MIME_TYPE))
                                    }
                                    .testTag("browse_files_button"),
                                shape = RoundedCornerShape(12.dp),
                                color = IndigoPurple,
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FolderOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Browse .docx File",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFECFDF5)
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
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Description,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedFileName ?: "",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("selected_file_name")
                                        )
                                        Text(
                                            text = selectedFileSize ?: "1.2 MB",
                                            fontSize = 12.sp,
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable {
                                            selectedFileName = null
                                            selectedFileSize = null
                                            selectedFileBytes = null
                                        }
                                        .testTag("remove_file_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove File",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. FIELD — CONTENT TYPE (Notes vs Assignment)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF7E22CE),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Content Type",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            "Notes" to "Study Notes",
                            "Assignment" to "Assignment"
                        ).forEach { (typeKey, label) ->
                            val isSelected = uploadType.equals(typeKey, ignoreCase = true)
                            val isAssignment = typeKey == "Assignment"

                            val filledBg = if (isSelected) {
                                if (isAssignment) Color(0xFFEA580C) else IndigoPurple
                            } else {
                                Color(0xFFF1F5F9)
                            }
                            val contentColor = if (isSelected) Color.White else Color(0xFF475569)

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { uploadType = typeKey }
                                    .testTag("upload_type_${typeKey.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                color = filledBg,
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
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
                                        tint = contentColor,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = contentColor
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. FIELD — NOTE / ASSIGNMENT TITLE (Coloured Input Field)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uploadType.equals("Assignment", ignoreCase = true)) {
                                "Assignment Title"
                            } else {
                                "Note Title"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        placeholder = {
                            Text(
                                text = if (uploadType.equals("Assignment", ignoreCase = true)) {
                                    "Enter assignment title (e.g. End of Term Revision)"
                                } else {
                                    "Enter note title (e.g. Plate Tectonics & Landforms)"
                                },
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            cursorColor = IndigoPurple,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note_title")
                    )
                }

                // 4. FIELD — SUBJECT (Coloured Dropdown Selector)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val currentSubjectColor = getSubjectThemeColor(selectedSubject)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEDD5),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Subject",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showSubjectDropdown = true }
                                .testTag("selector_subject"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC)
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
                                    Surface(
                                        shape = CircleShape,
                                        color = currentSubjectColor,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.MenuBook,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = selectedSubject,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = "Select Subject",
                                            tint = TextDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
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
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            subjectOptions.forEach { subjectOption ->
                                val isSelected = subjectOption.equals(selectedSubject, ignoreCase = true)
                                val optColor = getSubjectThemeColor(subjectOption)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = optColor,
                                                    modifier = Modifier.size(12.dp)
                                                ) {}
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = subjectOption,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) optColor else TextDark
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = optColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
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

                // 5. FIELD — TARGET CLASS (Coloured Dropdown Selector: S.1 - S.6)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFCCFBF1),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Target Class",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showClassDropdown = true }
                                .testTag("selector_class_stream"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC)
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
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0D9488),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.School,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    val classLabel = when (selectedClassStream) {
                                        "S.1" -> "S.1 (Senior One)"
                                        "S.2" -> "S.2 (Senior Two)"
                                        "S.3" -> "S.3 (Senior Three)"
                                        "S.4" -> "S.4 (Senior Four)"
                                        "S.5" -> "S.5 (Senior Five)"
                                        "S.6" -> "S.6 (Senior Six)"
                                        else -> selectedClassStream
                                    }
                                    Text(
                                        text = classLabel,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = "Select Target Class",
                                            tint = TextDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showClassDropdown,
                            onDismissRequest = { showClassDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("dropdown_class_selection")
                        ) {
                            availableClassesForSubject.forEach { classOption ->
                                val isSelected = classOption == selectedClassStream
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFFCCFBF1),
                                                    modifier = Modifier.padding(end = 10.dp)
                                                ) {
                                                    Text(
                                                        text = classOption,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else Color(0xFF0F766E),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                                Text(
                                                    text = when (classOption) {
                                                        "S.1" -> "Senior One"
                                                        "S.2" -> "Senior Two"
                                                        "S.3" -> "Senior Three"
                                                        "S.4" -> "Senior Four"
                                                        "S.5" -> "Senior Five"
                                                        "S.6" -> "Senior Six"
                                                        else -> classOption
                                                    },
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFF0D9488) else TextDark
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
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

                // 6. FIELD — STREAM TARGETING (Coloured Dropdown filtered by selected class and teacher's connected streams)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Group,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Target Streams",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val hasStreams = availableStreamsForClass.isNotEmpty()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    showStreamDropdown = true
                                }
                                .testTag("selector_target_stream"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC)
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
                                    Surface(
                                        shape = CircleShape,
                                        color = if (hasStreams) Color(0xFF059669) else Color(0xFFD97706),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (hasStreams) Icons.Outlined.Group else Icons.Outlined.Info,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    val streamDisplayText = when {
                                        !hasStreams -> "All Streams ($selectedClassStream)"
                                        isAllStreamsSelected || selectedStreamsList.isEmpty() -> "All Streams ($selectedClassStream)"
                                        selectedStreamsList.size == 1 -> "Stream ${selectedStreamsList.first()}"
                                        else -> "Streams: ${selectedStreamsList.joinToString(", ")}"
                                    }
                                    Text(
                                        text = streamDisplayText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = "Select Stream",
                                            tint = TextDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showStreamDropdown,
                            onDismissRequest = { showStreamDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("dropdown_stream_selection")
                        ) {
                            if (availableStreamsForClass.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFEF3C7),
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Info,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "No streams",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                                Text(
                                                    text = "No streams connected yet in $selectedClassStream. Notes will go to all students.",
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                            }
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isAllActive) Color(0xFF059669) else Color(0xFFD1FAE5),
                                                    modifier = Modifier.padding(end = 10.dp)
                                                ) {
                                                    Text(
                                                        text = "ALL",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAllActive) Color.White else Color(0xFF065F46),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "All Streams ($selectedClassStream)",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isAllActive) Color(0xFF059669) else TextDark
                                                )
                                            }
                                            if (isAllActive) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF059669),
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

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                availableStreamsForClass.forEach { streamOption ->
                                    val isStreamChecked = !isAllStreamsSelected && streamOption in selectedStreamsList
                                    val streamAccentColor = resolveStreamColor(streamOption)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = streamAccentColor,
                                                        modifier = Modifier.size(12.dp)
                                                    ) {}
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "Stream $streamOption",
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isStreamChecked) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isStreamChecked) streamAccentColor else TextDark
                                                    )
                                                }
                                                if (isStreamChecked) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = streamAccentColor,
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
                }

                // 7. FIELD — DESCRIPTION / INSTRUCTIONS (Coloured Input Field)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF7E22CE),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Description / Summary (Optional)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    OutlinedTextField(
                        value = noteDescription,
                        onValueChange = { noteDescription = it },
                        placeholder = {
                            Text(
                                text = "Add instructions or summary for your students' revision...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
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
                        .height(54.dp)
                        .testTag("submit_upload_button"),
                    shape = RoundedCornerShape(14.dp),
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
