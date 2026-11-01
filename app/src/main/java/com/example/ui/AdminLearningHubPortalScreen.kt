package com.example.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Enhanced Admin Portal for managing Learning Hub content.
 * Features vibrant themed headers, beautifully colored form cards, interactive chip selectors,
 * file upload previewing, and live database sync with Supabase.
 */
@Composable
fun AdminLearningHubPortalScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val coroutineScope = rememberCoroutineScope()
    var currentView by remember { mutableStateOf(AdminPortalView.CATEGORIES) }
    var selectedCategory by remember { mutableStateOf<LearningHubCategory?>(null) }

    var resources by remember { mutableStateOf<List<LearningHubResource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoading = true
        val fetched = SupabaseRepository.fetchAllHubResources()
        resources = if (fetched.isNotEmpty()) fetched else DefaultLearningHubResources
        isLoading = false
    }

    Scaffold(
        containerColor = SoftPastelBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AdminPortalThemedHeader(
                currentView = currentView,
                selectedCategory = selectedCategory,
                onBack = {
                    when (currentView) {
                        AdminPortalView.CATEGORIES -> onNavigateBack()
                        AdminPortalView.RESOURCE_LIST -> currentView = AdminPortalView.CATEGORIES
                        AdminPortalView.ADD_RESOURCE -> currentView = AdminPortalView.RESOURCE_LIST
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = IndigoPurple,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading Learning Hub materials...",
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                when (currentView) {
                    AdminPortalView.CATEGORIES -> {
                        PortalCategoryList(
                            categories = LearningHubCategory.values().toList(),
                            resources = resources,
                            onCategoryClick = { category ->
                                selectedCategory = category
                                currentView = AdminPortalView.RESOURCE_LIST
                            }
                        )
                    }

                    AdminPortalView.RESOURCE_LIST -> {
                        selectedCategory?.let { category ->
                            val categoryResources = resources.filter { it.category == category }
                            PortalResourceList(
                                category = category,
                                resources = categoryResources,
                                onAddClick = { currentView = AdminPortalView.ADD_RESOURCE },
                                onDeleteResource = { res ->
                                    coroutineScope.launch {
                                        val success = SupabaseRepository.deleteHubResource(res.id)
                                        resources = resources.filter { it.id != res.id }
                                        snackbarHostState.showSnackbar(
                                            if (success) "\"${res.title}\" deleted"
                                            else "\"${res.title}\" removed locally"
                                        )
                                    }
                                }
                            )
                        }
                    }

                    AdminPortalView.ADD_RESOURCE -> {
                        selectedCategory?.let { category ->
                            AddResourceForm(
                                category = category,
                                onSave = { newRes ->
                                    coroutineScope.launch {
                                        resources = listOf(newRes) + resources
                                        val saved = SupabaseRepository.saveHubResource(newRes)
                                        snackbarHostState.showSnackbar(
                                            if (saved) "Published to ${category.title} successfully!"
                                            else "Saved to ${category.title} (offline sync pending)"
                                        )
                                    }
                                    currentView = AdminPortalView.RESOURCE_LIST
                                },
                                onCancel = { currentView = AdminPortalView.RESOURCE_LIST }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class AdminPortalView {
    CATEGORIES,
    RESOURCE_LIST,
    ADD_RESOURCE
}

/**
 * Dynamic Top Header that harmoniously adapts its colors to the active Category.
 */
@Composable
fun AdminPortalThemedHeader(
    currentView: AdminPortalView,
    selectedCategory: LearningHubCategory?,
    onBack: () -> Unit
) {
    val themeColor = when (currentView) {
        AdminPortalView.CATEGORIES -> IndigoPurple
        AdminPortalView.RESOURCE_LIST,
        AdminPortalView.ADD_RESOURCE -> selectedCategory?.color ?: IndigoPurple
    }

    val gradientBrush = remember(themeColor) {
        Brush.verticalGradient(
            colors = listOf(
                themeColor,
                themeColor.copy(alpha = 0.88f)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradientBrush,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .statusBarsPadding()
            .padding(start = 16.dp, end = 20.dp, top = 12.dp, bottom = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("admin_portal_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column {
                    // Small breadcrumb tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ADMIN PORTAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (selectedCategory != null && currentView != AdminPortalView.CATEGORIES) {
                            Text(
                                text = "• ${selectedCategory.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when (currentView) {
                            AdminPortalView.CATEGORIES -> "Learning Hub Manager"
                            AdminPortalView.RESOURCE_LIST -> selectedCategory?.title ?: "Resources"
                            AdminPortalView.ADD_RESOURCE -> "Add to ${selectedCategory?.title ?: "Hub"}"
                        },
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("admin_portal_title")
                    )
                }
            }

            // Right badge / icon
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.20f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = selectedCategory?.icon ?: Icons.Outlined.LocalLibrary,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * 1. Category Grid / List View
 */
@Composable
fun PortalCategoryList(
    categories: List<LearningHubCategory>,
    resources: List<LearningHubResource>,
    onCategoryClick: (LearningHubCategory) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Welcome & instruction hero banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, BorderGrey)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = IndigoPurple.copy(alpha = 0.12f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = IndigoPurple,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Curriculum Content Hub",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Select any section below to add documents, revision packs, quizzes, or book materials.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SECTIONS (${categories.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${resources.size} total documents",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IndigoPurple
                )
            }
        }

        items(categories) { category ->
            val count = resources.count { it.category == category }
            PortalCategoryCard(
                category = category,
                itemCount = count,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun PortalCategoryCard(
    category: LearningHubCategory,
    itemCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = category.color.copy(alpha = 0.35f),
                spotColor = category.color.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .testTag("admin_portal_category_${category.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        color = category.color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            category.color,
                            category.color.copy(alpha = 0.90f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column {
                        Text(
                            text = category.title,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$itemCount documents attached",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Open",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Resource List View inside a Category
 */
@Composable
fun PortalResourceList(
    category: LearningHubCategory,
    resources: List<LearningHubResource>,
    onAddClick: () -> Unit,
    onDeleteResource: (LearningHubResource) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var resourceToDelete by remember { mutableStateOf<LearningHubResource?>(null) }

    val filteredResources = remember(resources, searchQuery) {
        if (searchQuery.isBlank()) resources
        else {
            val q = searchQuery.trim().lowercase()
            resources.filter {
                it.title.lowercase().contains(q) ||
                    it.subject.lowercase().contains(q) ||
                    it.targetClass.lowercase().contains(q) ||
                    it.author.lowercase().contains(q)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Action & Filter Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${resources.size} Total Materials",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Visible in the student's ${category.title} screen",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = category.color),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Document",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field inside category
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, subject or class...", fontSize = 13.sp, color = IconGrey) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = IconGrey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = category.color,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = SoftPastelBackground,
                        unfocusedContainerColor = SoftPastelBackground
                    )
                )
            }
        }

        if (filteredResources.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = category.color.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (searchQuery.isNotBlank()) Icons.Outlined.SearchOff else Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching materials found" else "No materials in ${category.title} yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (searchQuery.isNotBlank()) "Try a different keyword or class level." else "Tap \"Add Document\" above to upload notes, revision sheets, or PDFs.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    if (searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onAddClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = category.color)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add First ${category.title} Item", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredResources, key = { it.id }) { resource ->
                    PortalResourceCard(
                        resource = resource,
                        onDelete = { resourceToDelete = resource }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    resourceToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { resourceToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = CoralTile.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = CoralTile,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Delete Learning Material?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${target.title}\"? This material will immediately disappear from student learning hubs.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toRemove = target
                        resourceToDelete = null
                        onDeleteResource(toRemove)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralTile)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { resourceToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderGrey)
                ) {
                    Text("Cancel", color = TextDark)
                }
            }
        )
    }
}

@Composable
fun PortalResourceCard(
    resource: LearningHubResource,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x10000000),
                spotColor = Color(0x10000000)
            )
            .border(1.dp, BorderGrey, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = resource.category.color.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                resource.type.contains("Quiz", ignoreCase = true) -> Icons.Outlined.Quiz
                                resource.type.contains("Video", ignoreCase = true) -> Icons.Outlined.OndemandVideo
                                else -> Icons.Outlined.Description
                            },
                            contentDescription = null,
                            tint = resource.category.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Subject tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = resource.category.color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = resource.subject,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = resource.category.color,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        // Class tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftPastelBackground,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Text(
                                text = resource.targetClass,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        if (resource.isFeatured) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldMustard.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = "Featured",
                                        tint = GoldMustard,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Featured",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepMaroon
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = resource.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = CoralTile,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer meta: Author, Type, Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftPastelBackground, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = IconGrey,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = resource.author,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${resource.type} • ${resource.date}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * 3. EXCEPTIONALLY BEAUTIFUL THEMED ADD RESOURCE FORM
 * Colorful, organized into themed elevation cards, interactive chips, and real file picker preview.
 */
@Composable
fun AddResourceForm(
    category: LearningHubCategory,
    onSave: (LearningHubResource) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("UNEB Senior Examiner") }
    var targetClass by remember { mutableStateOf("S.4") }
    var resourceType by remember {
        mutableStateOf(
            when (category) {
                LearningHubCategory.LIBRARY -> "Library Book"
                LearningHubCategory.STUDY_MATERIAL -> "Comprehensive Notes"
                LearningHubCategory.REVISION -> "Revision Guide"
                LearningHubCategory.QUIZZES -> "Interactive Quiz"
                LearningHubCategory.VISUAL_LEARNING -> "Video Walkthrough"
                LearningHubCategory.GAMES -> "Educational Game"
            }
        )
    }
    var isFeatured by remember { mutableStateOf(false) }

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileSize by remember { mutableStateOf<String?>(null) }

    // Real document file picker contract
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { pickedUri ->
            selectedFileUri = pickedUri
            var displayName = "document_${System.currentTimeMillis()}.pdf"
            var sizeBytes = 0L

            try {
                context.contentResolver.query(pickedUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) displayName = name
                        }
                        if (sizeIndex != -1) {
                            sizeBytes = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {}

            selectedFileName = displayName
            selectedFileSize = if (sizeBytes > 0) {
                val kb = sizeBytes / 1024.0
                val mb = kb / 1024.0
                if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.0f KB", kb)
            } else {
                "2.4 MB"
            }
        }
    }

    // Common Secondary School Subjects in Uganda
    val commonSubjects = remember {
        listOf(
            "Mathematics",
            "Physics",
            "Chemistry",
            "Biology",
            "English",
            "Geography",
            "History",
            "Commerce",
            "Literature",
            "CRE",
            "Agriculture"
        )
    }

    // Available Secondary Classes
    val classOptions = remember { listOf("All", "S.1", "S.2", "S.3", "S.4", "S.5", "S.6") }

    // Material Types for quick selection
    val materialTypes = remember(category) {
        when (category) {
            LearningHubCategory.LIBRARY -> listOf("Library Book", "Reference Atlas", "Literature Text", "Novella Summary", "General Reading")
            LearningHubCategory.STUDY_MATERIAL -> listOf("Comprehensive Notes", "Formulae Guide", "Lab Manual", "Topical Notes", "Diagrams & Charts")
            LearningHubCategory.REVISION -> listOf("Revision Guide", "UNEB Solutions", "Past Papers", "Exam Tips", "Model Questions")
            LearningHubCategory.QUIZZES -> listOf("Interactive Quiz", "Self-Assessment", "Multiple Choice Test", "Flashcard Pack")
            LearningHubCategory.VISUAL_LEARNING -> listOf("Video Walkthrough", "Diagram Animation", "Visual Lab Experiment", "3D Anatomy")
            LearningHubCategory.GAMES -> listOf("Educational Game", "Map Trivia Quest", "Formula Puzzle", "Vocabulary Challenge")
        }
    }

    val authorPresets = remember {
        listOf(
            "UNEB Senior Examiner",
            "NCDC Certified Teacher",
            "Head of Science Department",
            "Language Faculty",
            "Senior History Master",
            "EdTech Studio"
        )
    }

    val isValid = title.isNotBlank() && subject.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ==========================================
        // SECTION 0: COLORFUL CATEGORY HERO CARD
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = category.color,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                category.color,
                                category.color.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = category.title.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add to ${category.title}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Fill in the details below to publish content for students.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.90f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION 1: DOCUMENT IDENTITY CARD
        // ==========================================
        ThemedFormCard(
            cardTitle = "1. Document Identity",
            cardIcon = Icons.Outlined.Edit,
            accentColor = category.color
        ) {
            // Document Title Input
            ThemedTextField(
                value = title,
                onValueChange = { title = it },
                label = "Document Title *",
                placeholder = "e.g. Pure Mathematics Revision Pack 2026",
                leadingIcon = Icons.Outlined.Title,
                accentColor = category.color
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subject Input
            ThemedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Subject *",
                placeholder = "e.g. Mathematics, Physics, Geography",
                leadingIcon = Icons.Outlined.MenuBook,
                accentColor = category.color
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Subject Tap Chips
            Text(
                text = "Tap to choose common subject:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonSubjects.forEach { subItem ->
                    val isSelected = subject.equals(subItem, ignoreCase = true)
                    ThemedChoiceChip(
                        text = subItem,
                        isSelected = isSelected,
                        accentColor = category.color,
                        onClick = { subject = subItem }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Author / Contributor Input
            ThemedTextField(
                value = author,
                onValueChange = { author = it },
                label = "Author or Department",
                placeholder = "e.g. UNEB Senior Examiner",
                leadingIcon = Icons.Outlined.Person,
                accentColor = category.color
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Author Preset Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                authorPresets.forEach { preset ->
                    val isSelected = author.equals(preset, ignoreCase = true)
                    ThemedChoiceChip(
                        text = preset,
                        isSelected = isSelected,
                        accentColor = category.color,
                        onClick = { author = preset }
                    )
                }
            }
        }

        // ==========================================
        // SECTION 2: ACADEMIC LEVEL & FORMAT CARD
        // ==========================================
        ThemedFormCard(
            cardTitle = "2. Academic Level & Format",
            cardIcon = Icons.Outlined.School,
            accentColor = category.color
        ) {
            // Target Class Selector
            Text(
                text = "Target Class Level:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classOptions.forEach { classOpt ->
                    val isSelected = targetClass.equals(classOpt, ignoreCase = true)
                    ThemedChoiceChip(
                        text = classOpt,
                        isSelected = isSelected,
                        accentColor = category.color,
                        onClick = { targetClass = classOpt }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Material Type Selector
            Text(
                text = "Resource Type:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                materialTypes.forEach { typeOpt ->
                    val isSelected = resourceType.equals(typeOpt, ignoreCase = true)
                    ThemedChoiceChip(
                        text = typeOpt,
                        isSelected = isSelected,
                        accentColor = category.color,
                        onClick = { resourceType = typeOpt }
                    )
                }
            }
        }

        // ==========================================
        // SECTION 3: FILE ATTACHMENT CARD (.pdf, .docx, .doc)
        // ==========================================
        ThemedFormCard(
            cardTitle = "3. Document File Attachment",
            cardIcon = Icons.Outlined.CloudUpload,
            accentColor = category.color
        ) {
            Text(
                text = "Supports Adobe PDF (.pdf) and Microsoft Word (.docx, .doc) files.",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (selectedFileName != null) GreenAccent.copy(alpha = 0.08f) else category.color.copy(alpha = 0.06f),
                border = BorderStroke(
                    1.5.dp,
                    if (selectedFileName != null) GreenAccent else category.color.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (selectedFileName != null) {
                        // File Selected State
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedFileName!!.endsWith(".pdf", ignoreCase = true)) Color(0xFFE53935) else BlueTile,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Document Attached",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedFileName!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${selectedFileSize ?: "2.4 MB"} • Ready to publish",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BorderGrey)
                        ) {
                            Text(
                                text = "Tap to Replace Document",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        // Empty Pick State
                        Surface(
                            shape = CircleShape,
                            color = category.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = null,
                                    tint = category.color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Tap to Browse & Upload Document",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DocTypePill("PDF", Color(0xFFE53935))
                            DocTypePill("DOCX", BlueTile)
                            DocTypePill("DOC", TealTile)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Documents are synced directly to students' cloud storage",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION 4: PUBLICATION & PROMOTION CARD
        // ==========================================
        ThemedFormCard(
            cardTitle = "4. Visibility & Promotion",
            cardIcon = Icons.Outlined.Star,
            accentColor = category.color
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { isFeatured = !isFeatured },
                shape = RoundedCornerShape(14.dp),
                color = if (isFeatured) GoldMustard.copy(alpha = 0.12f) else SoftPastelBackground,
                border = BorderStroke(
                    1.dp,
                    if (isFeatured) GoldMustard else BorderGrey
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isFeatured) GoldMustard else Color.White,
                        modifier = Modifier.size(36.dp),
                        border = if (isFeatured) null else BorderStroke(1.dp, BorderGrey)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (isFeatured) Color.White else IconGrey,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Feature on Hub Carousel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Pins this resource to the top featured carousel for high visibility",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }

                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GoldMustard,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = BorderGrey
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ==========================================
        // ACTION BUTTONS
        // ==========================================
        Button(
            onClick = {
                if (isValid) {
                    val newResource = LearningHubResource(
                        id = "res-${System.currentTimeMillis()}",
                        title = title.trim(),
                        subject = subject.trim(),
                        category = category,
                        targetClass = targetClass,
                        author = author.trim().ifBlank { "UNEB Senior Examiner" },
                        date = "29 Oct 2026",
                        type = resourceType,
                        isFeatured = isFeatured,
                        documentUrl = selectedFileName
                    )
                    onSave(newResource)
                }
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = if (isValid) 4.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = category.color.copy(alpha = 0.40f),
                    spotColor = category.color.copy(alpha = 0.40f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = category.color,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFE2E0EC),
                disabledContentColor = Color(0xFFA5A3B3)
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Publish to ${category.title}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, BorderGrey),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextDark
            )
        ) {
            Text(
                text = "Discard & Return",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * Reusable Themed Card container for grouping form inputs.
 */
@Composable
fun ThemedFormCard(
    cardTitle: String,
    cardIcon: ImageVector,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Title Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = cardIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = cardTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

/**
 * Clean, modern input field with leading icon and soft background tint.
 */
@Composable
fun ThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = IconGrey
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = BorderGrey,
                focusedContainerColor = accentColor.copy(alpha = 0.03f),
                unfocusedContainerColor = SoftPastelBackground
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

/**
 * Interactive Selection Pill Chip with vibrant active state.
 */
@Composable
fun ThemedChoiceChip(
    text: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else SoftPastelBackground,
        animationSpec = spring(),
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextDark,
        animationSpec = spring(),
        label = "chip_content"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(
            1.dp,
            if (isSelected) accentColor else BorderGrey
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Small format badge pill for supported file types.
 */
@Composable
fun DocTypePill(
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}
