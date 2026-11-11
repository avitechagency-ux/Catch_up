package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.VisualAspectRatio
import com.example.data.VisualLearningItem
import com.example.data.VisualLearningRepository
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Redesigned Cachap Visual Learning Screen.
 * Connected to Supabase for dynamic image discovery.
 */
@Composable
fun VisualLearningScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All") }
    var selectedItemIndex by remember { mutableIntStateOf(-1) }
    
    var isLoading by remember { mutableStateOf(true) }
    var approvedVisuals by remember { mutableStateOf<List<VisualLearningItem>>(emptyList()) }
    var availableSubjects by remember { mutableStateOf<List<String>>(listOf("All")) }

    LaunchedEffect(searchQuery, selectedSubject) {
        isLoading = true
        availableSubjects = VisualLearningRepository.getAvailableSubjects()
        approvedVisuals = if (searchQuery.isBlank()) {
            VisualLearningRepository.getApprovedVisuals(selectedSubject)
        } else {
            VisualLearningRepository.searchApprovedVisuals(searchQuery).filter {
                selectedSubject.equals("All", ignoreCase = true) || it.subject.equals(selectedSubject, ignoreCase = true)
            }
        }
        isLoading = false
    }

    val galleryState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(IndigoPurple)) {
        if (selectedItemIndex == -1) {
            Column(modifier = Modifier.fillMaxSize()) {
                VisualLearningHeroSection(
                    selectedSubject = selectedSubject,
                    onSelectSubject = { selectedSubject = it },
                    availableSubjects = availableSubjects,
                    onBackClick = onNavigateBack
                )
                
                VisualLearningStickySearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (approvedVisuals.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.ImageNotSupported, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No visual materials found.", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Check back soon for new uploads 😎", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                } else {
                    VisualLearningGalleryContent(
                        items = approvedVisuals,
                        listState = galleryState,
                        onItemClick = { item ->
                            selectedItemIndex = approvedVisuals.indexOf(item)
                        }
                    )
                }
            }
            
            BackHandler {
                if (!selectedSubject.equals("All", ignoreCase = true)) {
                    selectedSubject = "All"
                } else if (searchQuery.isNotEmpty()) {
                    searchQuery = ""
                } else {
                    onNavigateBack()
                }
            }
        } else {
            VisualLearningImmersiveViewer(
                items = approvedVisuals,
                initialIndex = selectedItemIndex,
                onClose = { selectedItemIndex = -1 }
            )
        }
    }
}

@Composable
fun VisualLearningGalleryContent(
    items: List<VisualLearningItem>,
    listState: LazyListState,
    onItemClick: (VisualLearningItem) -> Unit
) {
    val groupedItems = remember(items) {
        val rows = mutableListOf<GalleryRow>()
        val queue = items.toMutableList()
        
        while (queue.isNotEmpty()) {
            val item = queue[0]
            if (item.aspectRatio == VisualAspectRatio.RATIO_1_1 || item.aspectRatio == VisualAspectRatio.RATIO_16_9) {
                rows.add(GalleryRow.FullWidth(item))
                queue.removeAt(0)
                continue
            }
            
            val matchIndex = queue.drop(1).indexOfFirst { it.aspectRatio == item.aspectRatio }
            if (matchIndex != -1) {
                val matchItem = queue.removeAt(matchIndex + 1)
                queue.removeAt(0)
                rows.add(GalleryRow.TwoEqual(item, matchItem))
            } else {
                rows.add(GalleryRow.FullWidth(item))
                queue.removeAt(0)
            }
        }
        rows
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("visual_gallery_list"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "feed_subtitle_header") {
            Text(
                text = "Explore Discovery",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        items(groupedItems, key = { row -> 
            when (row) {
                is GalleryRow.FullWidth -> row.item.id
                is GalleryRow.TwoEqual -> "${row.item1.id}_${row.item2.id}"
            }
        }) { row ->
            when (row) {
                is GalleryRow.FullWidth -> {
                    GalleryItem(
                        item = row.item,
                        ratio = row.item.aspectRatio.ratio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        onClick = { onItemClick(row.item) }
                    )
                }
                is GalleryRow.TwoEqual -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GalleryItem(
                            item = row.item1,
                            ratio = row.item1.aspectRatio.ratio,
                            modifier = Modifier.weight(1f),
                            onClick = { onItemClick(row.item1) }
                        )
                        GalleryItem(
                            item = row.item2,
                            ratio = row.item2.aspectRatio.ratio,
                            modifier = Modifier.weight(1f),
                            onClick = { onItemClick(row.item2) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisualLearningHeroSection(
    selectedSubject: String,
    onSelectSubject: (String) -> Unit,
    availableSubjects: List<String>,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = IndigoPurple,
                shape = RoundedCornerShape(
                    bottomStart = 28.dp,
                    bottomEnd = 28.dp
                )
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("btn_visual_learning_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Discovery",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("cachap_wordmark")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableSubjects.forEach { subject ->
                val isSelected = subject.equals(selectedSubject, ignoreCase = true)
                Surface(
                    onClick = { onSelectSubject(subject) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("chip_$subject")
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
    }
}

@Composable
fun VisualLearningStickySearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("sticky_search_input"),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium),
                cursorBrush = SolidColor(Color.Black),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("Search concepts...", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Normal)
                    }
                    innerTextField()
                }
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

sealed class GalleryRow {
    data class FullWidth(val item: VisualLearningItem) : GalleryRow()
    data class TwoEqual(val item1: VisualLearningItem, val item2: VisualLearningItem) : GalleryRow()
}

@Composable
fun GalleryItem(
    item: VisualLearningItem,
    ratio: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .testTag("gallery_item_${item.id}")
    ) {
        val context = LocalContext.current
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (item.drawableResId != null) {
            Image(
                painter = painterResource(id = item.drawableResId),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA000000))
                    )
                )
                .padding(10.dp)
        ) {
            Column {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFB300)
                ) {
                    Text(
                        text = item.subject,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VisualLearningImmersiveViewer(
    items: List<VisualLearningItem>,
    initialIndex: Int,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().testTag("immersive_vertical_pager")
        ) { index ->
            ImmersiveViewItem(
                item = items[index],
                onClose = onClose
            )
        }
        
        BackHandler {
            onClose()
        }
    }
}

@Composable
fun ImmersiveViewItem(
    item: VisualLearningItem,
    onClose: () -> Unit
) {
    var isInfoVisible by remember { mutableStateOf(false) }
    val horizontalOffset = remember { Animatable(0f) }
    val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val coroutineScope = rememberCoroutineScope()

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            val newOffset = horizontalOffset.value + delta
            if (!isInfoVisible && newOffset <= 0) {
                horizontalOffset.snapTo(newOffset)
            } else if (isInfoVisible && newOffset >= -screenWidth) {
                horizontalOffset.snapTo(newOffset)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        if (!isInfoVisible) {
                            if (horizontalOffset.value < -screenWidth / 4 || velocity < -500f) {
                                horizontalOffset.animateTo(-screenWidth, spring(stiffness = Spring.StiffnessLow))
                                isInfoVisible = true
                            } else {
                                horizontalOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                            }
                        } else {
                            if (horizontalOffset.value > -screenWidth * 0.75f || velocity > 500f) {
                                horizontalOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                                isInfoVisible = false
                            } else {
                                horizontalOffset.animateTo(-screenWidth, spring(stiffness = Spring.StiffnessLow))
                            }
                        }
                    }
                }
            )
    ) {
        // Background Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val progress = abs(horizontalOffset.value) / screenWidth
                    scaleX = 1f - (progress * 0.1f)
                    scaleY = 1f - (progress * 0.1f)
                    alpha = 1f - (progress * 0.3f)
                },
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (item.drawableResId != null) {
                Image(
                    painter = painterResource(id = item.drawableResId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Top Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xCC000000), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Topic: ${item.subject}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("btn_close_viewer")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Information Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(horizontalOffset.value.roundToInt() + screenWidth.roundToInt(), 0) }
                .graphicsLayer {
                    val progress = (horizontalOffset.value + screenWidth) / screenWidth
                    rotationY = -15f * (1f - progress)
                    cameraDistance = 12f * density
                    shadowElevation = 8f * progress
                }
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .testTag("info_card"),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.article.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.article.introduction,
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
