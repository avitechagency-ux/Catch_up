package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppScreen
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark

import com.example.ui.theme.*

enum class StudyMaterialCategory(
    val title: String,
    val icon: ImageVector,
    val screen: AppScreen,
    val color: Color
) {
    REVISION("Revision Notes", Icons.Outlined.HistoryEdu, AppScreen.REVISION_NOTES, AmberTile),
    PAPERS("Exam Papers", Icons.Outlined.Assignment, AppScreen.EXAM_PAPERS, PurpleTile),
    GUIDES("Topic Guides", Icons.Outlined.MenuBook, AppScreen.TOPIC_GUIDES, BlueTile),
    RESOURCES("Learning Resources", Icons.Outlined.Description, AppScreen.LEARNING_RESOURCES, TealTile)
}

@Composable
fun StudyMaterialScreen(
    onNavigateBack: () -> Unit,
    onCategoryClick: (StudyMaterialCategory) -> Unit
) {
    BackHandler { onNavigateBack() }

    Scaffold(
        containerColor = Color.White,
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
                    Text("Study Material", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Reuse LearningHubSearchBar
                LearningHubSearchBar(
                    query = "",
                    onQueryChange = {},
                    onClearQuery = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(StudyMaterialCategory.REVISION, Modifier.weight(1f), onCategoryClick)
                CategoryCard(StudyMaterialCategory.PAPERS, Modifier.weight(1f), onCategoryClick)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(StudyMaterialCategory.GUIDES, Modifier.weight(1f), onCategoryClick)
                CategoryCard(StudyMaterialCategory.RESOURCES, Modifier.weight(1f), onCategoryClick)
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: StudyMaterialCategory,
    modifier: Modifier = Modifier,
    onCategoryClick: (StudyMaterialCategory) -> Unit
) {
    Surface(
        modifier = modifier
            .height(180.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onCategoryClick(category) },
        shape = RoundedCornerShape(16.dp),
        color = category.color
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(category.icon, category.title, tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(category.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
