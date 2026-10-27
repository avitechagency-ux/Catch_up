package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwipeLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.VisualLearningArticle
import com.example.data.VisualLearningItem
import com.example.data.VisualLearningRepository
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.GoldMustard
import com.example.ui.theme.SoftPastelBackground
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Main Cachap Visual Learning Screen.
 * Implements:
 * 1. Deep maroon top header with back navigation and title.
 * 2. Compact horizontal subject filter row (All, Biology, Physics, Chemistry, etc.).
 * 3. Pinterest-inspired two-column masonry discovery feed with variable aspect ratios (1:1, 4:5, 3:4, 16:9).
 * 4. Immersive full-screen image viewer on tap.
 * 5. Fluid horizontal swipe from image to connected educational article.
 * 6. Preserved scroll positions and filter states during navigation.
 */
@Composable
fun VisualLearningScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        VisualLearningRepository.initialize(context)
    }

    var selectedSubject by remember { mutableStateOf("All") }
    var selectedVisualItem by remember { mutableStateOf<VisualLearningItem?>(null) }
    val feedGridState = rememberLazyStaggeredGridState()

    val availableSubjects = remember { VisualLearningRepository.getAvailableSubjects() }
    val approvedVisuals = remember(selectedSubject) {
        VisualLearningRepository.getApprovedVisuals(selectedSubject)
    }

    // When an immersive viewer is active, show the dual image/article pager
    if (selectedVisualItem != null) {
        ImmersiveVisualViewer(
            item = selectedVisualItem!!,
            onClose = { selectedVisualItem = null }
        )
    } else {
        BackHandler {
            onNavigateBack()
        }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(SoftPastelBackground)
                .testTag("visual_learning_screen"),
            containerColor = SoftPastelBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. TOP HEADER (Deep Maroon Brand Bar)
                VisualLearningTopHeader(
                    onBackClick = onNavigateBack
                )

                // 2. HORIZONTAL SUBJECT FILTER ROW
                VisualLearningSubjectFilters(
                    subjects = availableSubjects,
                    selectedSubject = selectedSubject,
                    onSelectSubject = { subject ->
                        selectedSubject = subject
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 3. PINTEREST-INSPIRED TWO-COLUMN MASONRY FEED
                if (approvedVisuals.isEmpty()) {
                    VisualLearningEmptyState(
                        subject = selectedSubject,
                        onResetFilter = { selectedSubject = "All" }
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = feedGridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("visual_learning_grid"),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 8.dp,
                            bottom = 32.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(approvedVisuals, key = { it.id }) { item ->
                            VisualLearningCard(
                                item = item,
                                onClick = {
                                    selectedVisualItem = item
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Header bar following Cachap deep maroon styling.
 */
@Composable
fun VisualLearningTopHeader(
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = DeepMaroon,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("btn_visual_learning_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back to Learning Hub",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Visual Learning",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("visual_learning_title")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldMustard.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Discovery",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldMustard,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Browse curriculum diagrams & swipe left to explore",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Compact horizontal subject filter row.
 * Selected chip uses Deep Maroon with white text and gold border.
 */
@Composable
fun VisualLearningSubjectFilters(
    subjects: List<String>,
    selectedSubject: String,
    onSelectSubject: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("visual_subject_filters"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        subjects.forEach { subject ->
            val isSelected = subject.equals(selectedSubject, ignoreCase = true)
            val backgroundColor = if (isSelected) DeepMaroon else Color.White
            val textColor = if (isSelected) Color.White else TextDark
            val borderColor = if (isSelected) GoldMustard else BorderGrey

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectSubject(subject) }
                    .testTag("filter_chip_$subject"),
                shape = RoundedCornerShape(20.dp),
                color = backgroundColor,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = if (isSelected) 3.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = GoldMustard,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = subject,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Single Pinterest-style visual card in the discovery masonry feed.
 * Respects source image's aspect ratio (1:1, 4:5, 3:4, 16:9), renders a bottom gradient overlay
 * with clear readable title and subject pill.
 */
@Composable
fun VisualLearningCard(
    item: VisualLearningItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("visual_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFEFECE9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(item.aspectRatio.ratio)
        ) {
            // 1. IMAGE CONTENT
            if (item.drawableResId != null) {
                Image(
                    painter = painterResource(id = item.drawableResId),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!item.imageUrl.isNullOrBlank()) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Safe Fallback Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2C243B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 2. GRADIENT OVERLAY (Subtle dark scrim at bottom for readability)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66000000),
                                Color(0xCC000000)
                            )
                        )
                    )
            )

            // 3. TITLE & SUBJECT BADGE OVERLAY
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DeepMaroon.copy(alpha = 0.9f),
                    border = BorderStroke(0.5.dp, GoldMustard.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = item.subject,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    modifier = Modifier.testTag("card_title_${item.id}")
                )
            }
        }
    }
}

/**
 * Immersive Image Viewer & Connected Article Screen.
 * Uses HorizontalPager with 2 pages:
 * Page 0: Full-focus Immersive Image with dark background and gesture hint.
 * Page 1: Structured Educational Article connected directly to this visual.
 * Swiping left on image reveals article; swiping right on article returns to image.
 */
@Composable
fun ImmersiveVisualViewer(
    item: VisualLearningItem,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Handle back button: if in article (page 1), return to image (page 0); if in image, close viewer.
    BackHandler {
        if (pagerState.currentPage == 1) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("immersive_viewer_root")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            when (pageIndex) {
                0 -> ImmersiveImagePage(
                    item = item,
                    onClose = onClose,
                    onNavigateToArticle = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                1 -> ConnectedArticlePage(
                    item = item,
                    onNavigateBackToImage = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Page 0: The Immersive Image Viewer.
 * Preserves complete image without cropping, respects original aspect ratio,
 * shows title near bottom, provides an unobtrusive close button, and a subtle
 * "Swipe left to explore" indicator.
 */
@Composable
fun ImmersiveImagePage(
    item: VisualLearningItem,
    onClose: () -> Unit,
    onNavigateToArticle: () -> Unit
) {
    val context = LocalContext.current
    var showHint by remember { mutableStateOf(true) }

    // Subtle horizontal bobbing animation for the swipe hint
    val hintOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        hintOffset.animateTo(
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E13))
            .testTag("immersive_image_page")
    ) {
        // 1. FULL-FOCUS EDUCATIONAL VISUAL (Fitted cleanly without cropping)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 70.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.drawableResId != null) {
                Image(
                    painter = painterResource(id = item.drawableResId),
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("immersive_image_view")
                )
            } else if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("immersive_image_view")
                )
            } else {
                Text(
                    text = "Educational visual preview unavailable",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // 2. UNOBTRUSIVE TOP BAR (Close Button & Page Indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_viewer")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close Viewer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "1 of 2 • Visual",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // 3. BOTTOM SCRIM OVERLAY (Title, Subject, and Interactive Swipe Hint)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xAA000000),
                            Color(0xF5000000)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepMaroon,
                    border = BorderStroke(1.dp, GoldMustard)
                ) {
                    Text(
                        text = "${item.subject} • ${item.targetClass}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // SUBTLE HINT: Swipe left to explore
                AnimatedVisibility(
                    visible = showHint,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .offset { IntOffset(x = hintOffset.value.roundToInt(), y = 0) }
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onNavigateToArticle)
                            .testTag("hint_swipe_left"),
                        shape = RoundedCornerShape(20.dp),
                        color = GoldMustard,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Swipe left to explore",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepMaroon
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = DeepMaroon,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 26.sp,
                modifier = Modifier.testTag("immersive_title")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Author: ${item.author}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Page 1: The Connected Educational Article.
 * Beautifully formatted for comfortable reading on a phone, with introduction,
 * structured subheadings, concise paragraphs, key facts callout, and UNEB tips.
 * Allows independent vertical scrolling without triggering accidental horizontal navigation.
 */
@Composable
fun ConnectedArticlePage(
    item: VisualLearningItem,
    onNavigateBackToImage: () -> Unit
) {
    val scrollState = rememberScrollState()
    val article = item.article

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("connected_article_page"),
        containerColor = SoftPastelBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TOP BAR (Back to Image button & Article Indicator)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeepMaroon,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onNavigateBackToImage)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Image",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Back to Visual",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GoldMustard.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "2 of 2 • Article",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldMustard,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // VERTICAL SCROLLING ARTICLE CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // SUBJECT & CLASS BADGES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DeepMaroon.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, DeepMaroon.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = item.subject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = item.targetClass,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ARTICLE TITLE
                Text(
                    text = article.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon,
                    lineHeight = 28.sp,
                    modifier = Modifier.testTag("article_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Prepared by ${item.author} • Updated ${item.date}",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // MINI DIAGRAM PREVIEW BANNER
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onNavigateBackToImage),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (item.drawableResId != null) {
                            Image(
                                painter = painterResource(id = item.drawableResId),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (!item.imageUrl.isNullOrBlank()) {
                            val context = LocalContext.current
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Gradient banner overlay with tap instruction
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xB3000000)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = GoldMustard,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Connected diagram • Tap to view full size",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CONCISE INTRODUCTION
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEAE7E4)),
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = article.introduction,
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextDark,
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("article_introduction")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // KEY FACTS / SUMMARY HIGHLIGHT CARD
                if (article.keyFacts.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFFBEB), // Warm yellow/gold tint
                        border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Core Takeaways",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepMaroon
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            article.keyFacts.forEach { fact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldMustard,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = fact,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // STRUCTURED ARTICLE SECTIONS
                article.sections.forEach { section ->
                    ArticleSectionBlock(section = section)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // CURRICULUM EXAMINATION TIP
                if (!article.curriculumTip.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = DeepMaroon.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, DeepMaroon.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = DeepMaroon,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Curriculum Tip",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepMaroon
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.curriculumTip,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = TextDark
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // BOTTOM SWIPE RIGHT PROMPT
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNavigateBackToImage),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderGrey)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = DeepMaroon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Swipe right or tap to return to visual",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepMaroon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

/**
 * Single section in an educational article with bold heading, concise paragraphs, and bullet points.
 */
@Composable
fun ArticleSectionBlock(
    section: com.example.data.ArticleSection
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEAE7E4)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = section.heading,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepMaroon
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = section.body,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = TextDark
            )

            if (section.keyPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                section.keyPoints.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = point,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state when a subject has no visuals or when no approved items exist.
 */
@Composable
fun VisualLearningEmptyState(
    subject: String,
    onResetFilter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderGrey),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = SoftPastelBackground,
                    border = BorderStroke(1.dp, GoldMustard.copy(alpha = 0.5f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = DeepMaroon,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (subject.equals("All", ignoreCase = true)) {
                        "No Visuals Available Yet"
                    } else {
                        "No Visuals for $subject Yet"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Approved educational illustrations and diagrams for this subject will appear here once verified by curriculum instructors.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                if (!subject.equals("All", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onResetFilter,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepMaroon,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "View All Visuals", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
