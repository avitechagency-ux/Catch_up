package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudyMaterialResource
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
    
    // Placeholder data
    val resources = remember {
        listOf(
            StudyMaterialResource("1", "Physics Revision", "Physics", "S.4 • Physics", null),
            StudyMaterialResource("2", "Chemistry Guide", "Chemistry", "S.3 • Chemistry", null)
        )
    }
    
    val filteredResources = remember(searchQuery, resources) {
        if (searchQuery.isBlank()) resources
        else resources.filter { it.title.contains(searchQuery, ignoreCase = true) }
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
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(category.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                LearningHubSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearQuery = {
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        if (filteredResources.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No resources found.", color = TextMuted)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(filteredResources) { resource ->
                    ResourceItemCard(resource = resource, onClick = { onResourceClick(resource) })
                }
            }
        }
    }
}

@Composable
fun ResourceItemCard(resource: StudyMaterialResource, onClick: () -> Unit) {
    val theme = getSubjectTheme(resource.subject)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .border(2.dp, theme.borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = theme.iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Description, null, tint = theme.iconColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(resource.title, fontWeight = FontWeight.Bold, color = TextDark)
                Text(resource.metadata, fontSize = 12.sp, color = TextMuted)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = TextMuted)
        }
    }
}
