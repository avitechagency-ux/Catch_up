package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

data class SubjectTheme(val iconColor: Color, val borderColor: Color)

fun getSubjectTheme(subject: String): SubjectTheme {
    return when {
        subject.contains("Biology", true) -> SubjectTheme(NavyTile, PurpleTile)
        subject.contains("Chemistry", true) -> SubjectTheme(AmberTile, TealTile)
        subject.contains("Geography", true) -> SubjectTheme(CoralTile, BlueTile)
        subject.contains("History", true) -> SubjectTheme(BlueTile, CoralTile)
        subject.contains("Math", true) -> SubjectTheme(TealTile, NavyTile)
        subject.contains("Physics", true) -> SubjectTheme(BlueTile, AmberTile)
        else -> SubjectTheme(IndigoPurple, DeepMaroon)
    }
}

@Composable
fun StudyMaterialListScreen(
    category: StudyMaterialCategory,
    onNavigateBack: () -> Unit,
    onResourceClick: (StudyMaterialResource) -> Unit
) {
    BackHandler { onNavigateBack() }

    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All") }
    var selectedFileType by remember { mutableStateOf("All") } // "All", "DOCX", "PDF"

    var resources by remember { mutableStateOf<List<StudyMaterialResource>>(emptyList()) }
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

    LaunchedEffect(Unit) {
        isLoading = true
        val hubResources = SupabaseRepository.fetchAllHubResources()
        if (hubResources.isNotEmpty()) {
            val studyMaterials = hubResources.filter { res ->
                val cat = res.category.name.uppercase()
                cat == "STUDY_MATERIAL" || cat == "REVISION" || cat == "PAPERS" || cat == "GUIDES" || cat == "RESOURCES" || cat == "LIBRARY"
            }
            if (studyMaterials.isNotEmpty()) {
                resources = studyMaterials.map { res ->
                    val isPdf = res.documentUrl?.lowercase()?.endsWith(".pdf") == true || res.type.equals("PDF", true)
                    StudyMaterialResource(
                        id = res.id,
                        title = res.title,
                        subject = res.subject,
                        targetClass = res.targetClass.ifBlank { "S.4" },
                        author = res.author.ifBlank { "UNEB Senior Examiner" },
                        fileType = if (isPdf) "PDF" else "DOCX",
                        fileSize = "1.8 MB",
                        documentUrl = res.documentUrl,
                        dateUploaded = res.date
                    )
                }
            } else {
                resources = DefaultStudyMaterialResources
            }
        } else {
            resources = DefaultStudyMaterialResources
        }
        isLoading = false
    }

    val filteredResources = remember(searchQuery, selectedSubject, selectedFileType, resources) {
        resources.filter { res ->
            val matchesSubject = selectedSubject.equals("All", ignoreCase = true) ||
                res.subject.equals(selectedSubject, ignoreCase = true)

            val matchesFileType = selectedFileType.equals("All", ignoreCase = true) ||
                res.fileType.equals(selectedFileType, ignoreCase = true)

            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                res.title.lowercase().contains(q) ||
                    res.subject.lowercase().contains(q) ||
                    res.targetClass.lowercase().contains(q) ||
                    res.author.lowercase().contains(q)
            }

            matchesSubject && matchesFileType && matchesQuery
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F7FC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${filteredResources.size} document${if (filteredResources.size == 1) "" else "s"} synced from Admin", fontSize = 12.sp, color = LightLavender)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LearningHubSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearQuery = {
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Filter Chips
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
                            onClick = { selectedSubject = subject },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFFFB300) else Color(0xFFDC8CC0).copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFB300) else Color(0xFFDC8CC0))
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

                // Document Format Filter (All vs DOCX vs PDF)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Format:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    listOf("All", "DOCX", "PDF").forEach { fType ->
                        val isSelected = fType.equals(selectedFileType, ignoreCase = true)
                        Surface(
                            onClick = { selectedFileType = fType },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = fType,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) IndigoPurple else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IndigoPurple)
                }
            } else if (filteredResources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "No study material documents found matching your filter.",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedSubject = "All"
                                selectedFileType = "All"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Filters", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResources, key = { it.id }) { resource ->
                        ResourceItemCard(resource = resource, onClick = { onResourceClick(resource) })
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceItemCard(resource: StudyMaterialResource, onClick: () -> Unit) {
    val theme = getSubjectTheme(resource.subject)
    val isPdf = resource.fileType.equals("PDF", ignoreCase = true)
    val badgeBg = if (isPdf) Color(0xFFEF4444) else Color(0xFF2563EB)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(1.5.dp, theme.borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = theme.iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPdf) Icons.Outlined.PictureAsPdf else Icons.Outlined.Description,
                        contentDescription = resource.fileType,
                        tint = theme.iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = resource.fileType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IndigoPurple.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = resource.targetClass,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = resource.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralTile
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = resource.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${resource.author} • ${resource.fileSize}",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(Icons.Outlined.ChevronRight, contentDescription = "Open Document", tint = TextMuted)
        }
    }
}
