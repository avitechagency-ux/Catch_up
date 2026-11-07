package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

/**
 * Tips: Educational study tips, memory hacks, exam skills, and daily challenges.
 */
@Composable
fun TipsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    var tips by remember { mutableStateOf<List<StudyTip>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<TipCategory?>(null) } // null = All
    var selectedDetailTip by remember { mutableStateOf<StudyTip?>(null) }
    var isDailyChallengeDone by remember { mutableStateOf(false) }
    var showAddTipDialog by remember { mutableStateOf(false) }

    fun refresh() {
        tips = TipsRepository.getAllTips(context)
        isDailyChallengeDone = TipsRepository.isChallengeCompleted(context)
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val tipOfTheDay = remember(tips) {
        if (tips.isNotEmpty()) tips.first() else DefaultStudyTips.first()
    }

    val filteredTips = remember(tips, selectedCategory) {
        if (selectedCategory == null) tips
        else tips.filter { it.category == selectedCategory }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPastelBackground)
            .testTag("tips_screen"),
        containerColor = SoftPastelBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("tips_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Study Tips & Hacks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Proven memory, revision & UNEB exam strategies",
                            fontSize = 12.sp,
                            color = LightLavender
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldMustard
                    ) {
                        Text(
                            text = "Tips",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. TIP OF THE DAY CARD
                item {
                    TipOfTheDayCard(
                        tip = tipOfTheDay,
                        onClick = { selectedDetailTip = tipOfTheDay }
                    )
                }

                // 2. TODAY'S CHALLENGE CARD
                item {
                    TodayChallengeCard(
                        isCompleted = isDailyChallengeDone,
                        onToggle = {
                            val done = TipsRepository.toggleChallenge(context)
                            isDailyChallengeDone = done
                            Toast.makeText(
                                context,
                                if (done) "🎉 Challenge marked as complete!" else "Challenge uncompleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                // 3. CATEGORIES FILTER CHIPS
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Browse by Category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All (${tips.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPurple,
                                    selectedLabelColor = Color.White
                                )
                            )

                            TipCategory.values().forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.title) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IndigoPurple,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. TIP CARDS LIST
                items(filteredTips, key = { it.id }) { tip ->
                    TipItemCard(
                        tip = tip,
                        onClick = { selectedDetailTip = tip }
                    )
                }
            }
        }
    }

    // Tip Details Dialog
    selectedDetailTip?.let { tip ->
        TipDetailDialog(
            tip = tip,
            onDismiss = { selectedDetailTip = null }
        )
    }
}

/**
 * Hero Tip of the Day Card.
 */
@Composable
private fun TipOfTheDayCard(
    tip: StudyTip,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = IndigoPurple.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .testTag("tip_of_the_day_card"),
        shape = RoundedCornerShape(20.dp),
        color = IndigoPurple
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldMustard
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = DeepMaroon,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tip of the Day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )
                    }
                }

                Text(
                    text = tip.category.title,
                    fontSize = 11.sp,
                    color = LightLavender
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tip.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tip.summary,
                fontSize = 12.sp,
                color = LightLavender,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Read full strategy & action plan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Today's Challenge Card with complete toggle.
 */
@Composable
private fun TodayChallengeCard(
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isCompleted) NoteCountGreen.copy(alpha = 0.15f) else GoldMustard.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = if (isCompleted) NoteCountGreen else DeepMaroon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Today's Study Challenge",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                Text(
                    text = "Revise 15 minutes of formula sheets before lunch",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggle,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) NoteCountGreen else IndigoPurple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isCompleted) "Done! ✓" else "Mark Done",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * List Item Card for Study Tip.
 */
@Composable
private fun TipItemCard(
    tip: StudyTip,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("tip_card_${tip.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tip.category.color.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = tip.category.icon,
                            contentDescription = null,
                            tint = tip.category.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = tip.category.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tip.category.color
                        )
                    }
                }

                tip.subjectTag?.let { subj ->
                    Text(
                        text = subj,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tip.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tip.summary,
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Detailed Tip Dialog.
 */
@Composable
private fun TipDetailDialog(
    tip: StudyTip,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = tip.category.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = tip.category.color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    lineHeight = 24.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = tip.detailedExplanation,
                    fontSize = 13.sp,
                    color = TextDark,
                    lineHeight = 19.sp
                )

                // Actionable step callout
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftPastelBackground,
                    border = BorderStroke(1.dp, BorderGrey),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Checklist,
                                contentDescription = null,
                                tint = DeepMaroon,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Actionable Takeaway:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepMaroon
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tip.actionableStep,
                            fontSize = 12.sp,
                            color = TextDark,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
            ) {
                Text("Got It!")
            }
        }
    )
}
