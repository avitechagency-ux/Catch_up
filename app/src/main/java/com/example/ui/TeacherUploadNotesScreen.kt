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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowDown
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
import androidx.compose.ui.geometry.CornerRadius
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
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Upload & Edit Notes Screen for the Teacher Panel.
 * Allows teachers to upload new study notes or assignments for their assigned classes,
 * or edit an existing note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherUploadNotesScreen(
    subjectName: String = "Geography",
    subjectIcon: ImageVector = Icons.Outlined.Public,
    assignedClasses: List<String> = listOf("S.4 Green", "S.4 Blue", "S.5 Blue", "S.5 Arts"),
    existingNote: SubjectUploadItem? = null,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: (SubjectUploadItem) -> Unit = {}
) {
    val isEditing = existingNote != null
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form states
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var selectedClass by remember {
        mutableStateOf(
            existingNote?.targetClass ?: ""
        )
    }
    var selectedType by remember {
        mutableStateOf(
            if (existingNote?.type?.equals("Assignment", ignoreCase = true) == true) "Assignment" else "Notes"
        )
    }
    var selectedFileName by remember {
        mutableStateOf(
            if (existingNote != null) "${existingNote.title.lowercase().replace(" ", "_")}.pdf" else null
        )
    }

    var showClassPickerSheet by remember { mutableStateOf(false) }
    var showSampleFilePicker by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = uri.lastPathSegment ?: "document.pdf"
            val cleanName = if (path.contains("/")) path.substringAfterLast("/") else path
            selectedFileName = if (cleanName.endsWith(".pdf", ignoreCase = true)) cleanName else "$cleanName.pdf"
        } else {
            // Fallback sample file option if device picker was canceled or has no files
            showSampleFilePicker = true
        }
    }

    val isFormValid = title.isNotBlank() && selectedClass.isNotBlank() && !selectedFileName.isNullOrBlank()

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
            // TOP BAR: Height 56dp, white background, 1dp #F0F0F0 bottom border
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .statusBarsPadding()
                    .testTag("upload_notes_top_bar"),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
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
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = if (isEditing) "Edit Notes" else "Upload Notes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("upload_notes_header_title")
                    )
                }
            }

            // BODY: Scrollable, 20dp horizontal padding, 20dp top padding, 20dp spacing between fields
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. SUBJECT INDICATOR (Read-only)
                Column {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_indicator_card"),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF7F6FB)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = subjectIcon,
                                contentDescription = subjectName,
                                tint = IndigoPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = subjectName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.testTag("subject_indicator_name")
                            )
                        }
                    }
                }

                // 2. FIELD — NOTE TITLE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Title",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE5E2F2), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g. Population Density",
                                fontSize = 14.sp,
                                color = Color(0xFF9C9C9C)
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
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1A1A1A)
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(IndigoPurple)
                        )
                    }
                }

                // 3. FIELD — CLASS + STREAM
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Class",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showClassPickerSheet = true }
                            .testTag("selector_class_stream"),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E2F2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedClass.isNotBlank()) selectedClass else "Select class",
                                fontSize = 14.sp,
                                color = if (selectedClass.isNotBlank()) Color(0xFF1A1A1A) else Color(0xFF9C9C9C),
                                fontWeight = if (selectedClass.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                            )

                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Dropdown chevron",
                                tint = Color(0xFF9C9C9C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 4. FIELD — TYPE (Notes or Assignments toggle)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Type",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A)
                    )

                    // Pill-shaped two-segment toggle style
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("upload_type_segmented_toggle"),
                        shape = RoundedCornerShape(23.dp),
                        color = Color(0xFFF1EFF8)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Notes Segment
                            val isNotesActive = selectedType == "Notes"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isNotesActive) IndigoPurple else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedType = "Notes" }
                                    .testTag("toggle_notes_type"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Notes",
                                    fontSize = 14.sp,
                                    fontWeight = if (isNotesActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isNotesActive) Color.White else TextMuted
                                )
                            }

                            // Assignments Segment
                            val isAssignmentActive = selectedType == "Assignment"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isAssignmentActive) IndigoPurple else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedType = "Assignment" }
                                    .testTag("toggle_assignment_type"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Assignments",
                                    fontSize = 14.sp,
                                    fontWeight = if (isAssignmentActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAssignmentActive) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }

                // 5. FIELD — DOCUMENT SELECTION
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Document",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A)
                    )

                    if (selectedFileName == null) {
                        // Empty state: Dashed border card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFBFAFD))
                                .drawBehind {
                                    drawRoundRect(
                                        color = Color(0xFFC9C4DE),
                                        cornerRadius = CornerRadius(12.dp.toPx()),
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
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = "Upload Cloud",
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Tap to select PDF",
                                    fontSize = 13.sp,
                                    color = Color(0xFF8A8A8A)
                                )
                            }
                        }
                    } else {
                        // File Selected: Solid card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("card_selected_document"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF7F6FB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECEAF5))
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = "Document",
                                        tint = IndigoPurple,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Text(
                                        text = selectedFileName ?: "",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1A1A1A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { selectedFileName = null },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("btn_remove_document")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove file",
                                        tint = Color(0xFF9C9C9C),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 6. SUBMIT BUTTON
                Button(
                    onClick = {
                        if (isFormValid) {
                            val finalItem = SubjectUploadItem(
                                id = existingNote?.id ?: "upload-${System.currentTimeMillis()}",
                                title = title.trim(),
                                subject = subjectName,
                                targetClass = selectedClass,
                                type = selectedType,
                                dateUploaded = existingNote?.dateUploaded ?: "Today",
                                dueDate = if (selectedType == "Assignment") "Due in 7 days" else null
                            )
                            onSaveSuccess(finalItem)
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 4.dp)
                        .testTag("btn_share_notes"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPurple,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E2F2),
                        disabledContentColor = Color(0xFF9C9C9C)
                    )
                ) {
                    Text(
                        text = if (isEditing) "Save Changes" else "Share Notes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Bottom Sheet for Class Selection
    if (showClassPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showClassPickerSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("class_picker_bottom_sheet")
            ) {
                Text(
                    text = "Select Assigned Class",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Text(
                    text = "Classes and streams assigned to you for $subjectName",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                assignedClasses.forEach { cls ->
                    val isSelected = selectedClass == cls
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedClass = cls
                                showClassPickerSheet = false
                            }
                            .testTag("class_option_${cls.lowercase().replace(" ", "_")}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFF1EFF8) else Color(0xFFFBFAFD),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPurple else BorderGrey
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cls,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) IndigoPurple else Color(0xFF1A1A1A)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Quick Sample File Picker Dialog (fallback if storage permission/picker is empty)
    if (showSampleFilePicker) {
        val sampleFiles = listOf(
            "${title.ifBlank { "chapter_notes" }.lowercase().replace(" ", "_")}.pdf",
            "${subjectName.lowercase()}_term3_comprehensive_guide.pdf",
            "fieldwork_and_case_studies.pdf",
            "revision_questions_and_marking_scheme.pdf"
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSampleFilePicker = false },
            title = {
                Text(
                    text = "Select Document PDF",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose from your prepared curriculum documents:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    sampleFiles.forEach { file ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedFileName = file
                                    showSampleFilePicker = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF7F6FB)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = file,
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFileName = "${title.ifBlank { subjectName.lowercase() }}.pdf"
                        showSampleFilePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                ) {
                    Text("Attach Document")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showSampleFilePicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherUploadNotesScreenPreview() {
    TeacherUploadNotesScreen()
}

/**
 * Helper to get available classes for a given subject.
 */
fun getAssignedClassesForSubject(subjectName: String): List<String> {
    return when {
        subjectName.contains("Math", ignoreCase = true) -> listOf("S.3 Red", "S.3 Blue", "S.4 Green", "S.4 Blue")
        subjectName.contains("Bio", ignoreCase = true) -> listOf("S.4 East", "S.5 Biology", "S.6 Sci")
        subjectName.contains("Chem", ignoreCase = true) -> listOf("S.3 West", "S.4 East", "S.5 Sci")
        subjectName.contains("Phys", ignoreCase = true) -> listOf("S.4 Blue", "S.5 Sci", "S.6 Sci")
        else -> listOf("S.4 Green", "S.4 Blue", "S.5 Blue", "S.5 Arts")
    }
}
