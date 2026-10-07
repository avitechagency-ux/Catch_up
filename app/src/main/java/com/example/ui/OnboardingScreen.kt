package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.TealTile
import kotlinx.coroutines.launch

private val SplashOrange = Color(0xFFFF7A53)
private val SplashWarmAmber = Color(0xFFFF9E6D)
private val SplashPink = Color(0xFFFF6B9D)
private val SplashSoftPink = Color(0xFFFF8FB8)
private val SplashGold = Color(0xFFC9A227)

data class OnboardingPageData(
    val stepBadge: String,
    val subtitleTag: String,
    val title: String,
    val description: String,
    val accentColor: Color,
    val secondaryColor: Color = SplashPink
)

val DefaultOnboardingPages = listOf(
    OnboardingPageData(
        stepBadge = "01 / 04",
        subtitleTag = "WELCOME",
        title = "Simple notes for calm, focused study.",
        description = "Everything you need to read, revise, and catch up—kept clean and effortless.",
        accentColor = CoralTile,
        secondaryColor = SplashPink
    ),
    OnboardingPageData(
        stepBadge = "02 / 04",
        subtitleTag = "ORGANIZED",
        title = "Your class notes, always within reach.",
        description = "Read clear topic notes from your teachers anytime, even when you are offline.",
        accentColor = TealTile,
        secondaryColor = SplashOrange
    ),
    OnboardingPageData(
        stepBadge = "03 / 04",
        subtitleTag = "LIVE AUDIO",
        title = "Live audio lessons with your teachers.",
        description = "Join interactive audio classes in real time, raise your hand to ask questions, and learn together.",
        accentColor = SplashGold,
        secondaryColor = SplashOrange
    ),
    OnboardingPageData(
        stepBadge = "04 / 04",
        subtitleTag = "BEGIN",
        title = "Small daily steps build great results.",
        description = "Move at your own pace, one simple note and lesson at a time.",
        accentColor = PeachOrange,
        secondaryColor = SplashSoftPink
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinishOnboarding: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { DefaultOnboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = IndigoPurple)
            .testTag("onboarding_screen")
    ) {
        // Pager with pure minimalist purple, orange & pink designer splash pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageData = DefaultOnboardingPages[page]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = IndigoPurple)
            ) {
                // Minimalist Graphic color composition on the empty purple canvas
                MinimalistColorSplashCanvas(
                    pageIndex = page,
                    modifier = Modifier.fillMaxSize()
                )

                // On the final BEGIN page (page 3), display the App Icon in Orange in the upper canvas
                if (page == 3) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Cachap App Icon",
                        tint = SplashOrange,
                        modifier = Modifier
                            .size(220.dp)
                            .align(BiasAlignment(0f, -0.34f))
                            .testTag("onboarding_begin_app_icon")
                    )
                }

                // Small, clean welcoming text positioned with generous empty purple space
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Small color pill bar (Orange + Pink)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(pageData.accentColor)
                        )
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(pageData.secondaryColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pageData.subtitleTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SplashSoftPink,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Small, clean welcoming headline
                    Text(
                        text = pageData.title,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        lineHeight = 28.sp,
                        letterSpacing = (-0.2).sp,
                        modifier = Modifier.testTag("onboarding_title_$page")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Small, gentle note text
                    Text(
                        text = pageData.description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = LightLavender,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("onboarding_desc_$page")
                    )

                    // Space reserved for bottom navigation controls
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }
        }

        // Top Bar: subtle brand mark & small Skip text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 32.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SplashOrange)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SplashPink)
                )
                Text(
                    text = "Cachap",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = 0.8.sp
                )
            }

            Text(
                text = "Skip",
                color = LightLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onFinishOnboarding)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("onboarding_skip_button")
            )
        }

        // Bottom Controls: Rounded indicator bars + sleek Orange Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Modern pill indicators: active is widened (24dp), inactive are round dots (6dp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(DefaultOnboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 6.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "dot_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) SplashOrange else LightLavender.copy(alpha = 0.35f),
                        animationSpec = tween(durationMillis = 300),
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Sleek, compact button in Orange
            Button(
                onClick = {
                    if (pagerState.currentPage < DefaultOnboardingPages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinishOnboarding()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SplashOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("onboarding_next_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == DefaultOnboardingPages.size - 1) {
                            "Start"
                        } else {
                            "Next"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Clean, empty purple splash canvas accented with pure orange and pink geometric color forms.
 * Uses IndigoPurple as the base so there is never any dark or black tint.
 */
@Composable
private fun MinimalistColorSplashCanvas(
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Explicit solid purple canvas fill so it never renders black on any device
        drawRect(color = IndigoPurple, size = size)

        when (pageIndex) {
            0 -> {
                // Soft pink ambient glow in upper right corner (fades to IndigoPurple, never black)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SplashPink.copy(alpha = 0.45f),
                            IndigoPurple.copy(alpha = 0.0f)
                        ),
                        center = Offset(w * 0.82f, h * 0.24f),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(w * 0.82f, h * 0.24f)
                )

                // Clean tilted Pink note card silhouette
                rotate(degrees = -12f, pivot = Offset(w * 0.54f, h * 0.34f)) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(SplashPink, SplashSoftPink)
                        ),
                        topLeft = Offset(w * 0.36f, h * 0.24f),
                        size = Size(w * 0.32f, w * 0.40f),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                }

                // Clean tilted Orange note card silhouette overlapping
                rotate(degrees = 6f, pivot = Offset(w * 0.48f, h * 0.36f)) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(SplashOrange, SplashWarmAmber)
                        ),
                        topLeft = Offset(w * 0.30f, h * 0.27f),
                        size = Size(w * 0.32f, w * 0.40f),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )

                    // Two tiny minimalist note lines on the orange sheet
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.78f),
                        topLeft = Offset(w * 0.35f, h * 0.32f),
                        size = Size(w * 0.16f, 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.48f),
                        topLeft = Offset(w * 0.35f, h * 0.345f),
                        size = Size(w * 0.21f, 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            1 -> {
                // Soft orange ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SplashOrange.copy(alpha = 0.40f),
                            IndigoPurple.copy(alpha = 0.0f)
                        ),
                        center = Offset(w * 0.22f, h * 0.28f),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(w * 0.22f, h * 0.28f)
                )

                // Minimalist horizontal color bars (Pink & Orange)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SplashOrange, SplashPink)
                    ),
                    topLeft = Offset(w * 0.22f, h * 0.28f),
                    size = Size(w * 0.56f, 28.dp.toPx()),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SplashPink, SplashSoftPink)
                    ),
                    topLeft = Offset(w * 0.30f, h * 0.335f),
                    size = Size(w * 0.42f, 28.dp.toPx()),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )

                drawRoundRect(
                    color = SplashWarmAmber,
                    topLeft = Offset(w * 0.26f, h * 0.39f),
                    size = Size(w * 0.28f, 28.dp.toPx()),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )
            }

            2 -> {
                // Live Audio Graphic: Sound wave ripples & golden live broadcast indicator
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SplashGold.copy(alpha = 0.40f),
                            IndigoPurple.copy(alpha = 0.0f)
                        ),
                        center = Offset(w * 0.5f, h * 0.30f),
                        radius = w * 0.50f
                    ),
                    radius = w * 0.50f,
                    center = Offset(w * 0.5f, h * 0.30f)
                )

                // Central Gold Broadcast Circle
                drawCircle(
                    color = SplashGold,
                    radius = 28.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.30f)
                )

                // Concentric dynamic audio sound waves
                drawCircle(
                    color = SplashWarmAmber.copy(alpha = 0.70f),
                    radius = 48.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.30f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = SplashPink.copy(alpha = 0.45f),
                    radius = 70.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.30f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            else -> {
                // Soft warm glow behind the orange App Icon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SplashOrange.copy(alpha = 0.35f),
                            IndigoPurple.copy(alpha = 0.0f)
                        ),
                        center = Offset(w * 0.5f, h * 0.33f),
                        radius = w * 0.48f
                    ),
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, h * 0.33f)
                )
            }
        }
    }
}
