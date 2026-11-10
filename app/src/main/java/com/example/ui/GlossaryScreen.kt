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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private val AccountGoldAccent = Color(0xFFFFB300)
private val AccountFieldBg = Color(0xFFF8F7FC)

/**
 * Glossary: Searchable subject-based educational glossary for secondary students.
 * Connected to live Supabase database for dynamic admin updates.
 */
@Composable
fun GlossaryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var terms by remember { mutableStateOf<List<GlossaryTerm>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSubject by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTermForDetail by remember { mutableStateOf<GlossaryTerm?>(null) }

    // Fetch live terms from Supabase with fallback to local repository
    LaunchedEffect(Unit) {
        isLoading = true
        val remoteTerms = SupabaseRepository.fetchAllGlossaryTerms()
        if (remoteTerms.isNotEmpty()) {
            terms = remoteTerms
        } else {
            terms = GlossaryRepository.getAllTerms(context)
        }
        isLoading = false
    }

    val availableSubjects = remember(terms) {
        val preset = listOf("All", "Mathematics", "Physics", "Chemistry", "Biology", "Geography", "History", "English", "Economics", "Agriculture")
        val fromData = terms.map { it.subject }.distinct()
        (preset + fromData).distinct()
    }

    val filteredTerms = remember(terms, selectedSubject, searchQuery) {
        terms.filter { term ->
            val matchSubj = selectedSubject == "All" || term.subject.equals(selectedSubject, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                term.term.contains(searchQuery, ignoreCase = true) ||
                term.definition.contains(searchQuery, ignoreCase = true) ||
                (term.exampleUsage?.contains(searchQuery, ignoreCase = true) == true)
            matchSubj && matchQuery
        }.sortedBy { it.term.lowercase() }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPastelBackground)
            .testTag("glossary_screen"),
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
                            .testTag("glossary_back_button")
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
                            text = "Glossary",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Academic terminology & subject definitions.",
                            fontSize = 13.sp,
                            color = LightLavender
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldMustard
                    ) {
                        Text(
                            text = "${terms.size} Terms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search terms, definitions, formulas...",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.White),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("glossary_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subject Filter Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSubjects.forEach { subj ->
                    val isSelected = subj.equals(selectedSubject, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedSubject = subj }
                            .testTag("glossary_filter_$subj"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) IndigoPurple else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) IndigoPurple else BorderGrey)
                    ) {
                        Text(
                            text = subj,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Content Area
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IndigoPurple)
                }
            } else if (filteredTerms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = SoftPastelBackground,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No glossary entries found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your search query or selecting a different subject filter.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("glossary_list"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredTerms, key = { it.id }) { termItem ->
                        GlossaryTermCard(
                            term = termItem,
                            onClick = { selectedTermForDetail = termItem }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedTermForDetail?.let { term ->
        GlossaryTermDetailDialog(
            term = term,
            onDismiss = { selectedTermForDetail = null }
        )
    }
}

/**
 * Individual Glossary Term Card.
 */
@Composable
private fun GlossaryTermCard(
    term: GlossaryTerm,
    onClick: () -> Unit
) {
    val cardColors = listOf(SoftLavender, SoftBlue, SoftGreen, SoftPeach, SoftNeutral)
    val cardColor = cardColors[term.id.hashCode().absoluteValue % cardColors.size]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("glossary_item_${term.id}"),
        shape = RoundedCornerShape(18.dp),
        color = cardColor
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = term.term,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowOutward,
                    contentDescription = "Open definition",
                    tint = TextDark.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.05f)
                ) {
                    Text(
                        text = term.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = IndigoPurple.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = term.targetClass,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = term.definition,
                fontSize = 14.sp,
                color = TextDark.copy(alpha = 0.85f),
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Glossary Term Detail Modal Dialog.
 */
@Composable
private fun GlossaryTermDetailDialog(
    term: GlossaryTerm,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .testTag("glossary_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, IndigoPurple.copy(alpha = 0.3f)),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = CoralTile.copy(alpha = 0.15f)) {
                                Text(text = term.subject, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralTile, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = IndigoPurple.copy(alpha = 0.12f)) {
                                Text(text = term.targetClass, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IndigoPurple, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = term.term,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Divider(color = BorderGrey.copy(alpha = 0.5f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Definition", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoPurple)
                    Text(
                        text = term.definition,
                        fontSize = 14.5.sp,
                        color = TextDark,
                        lineHeight = 22.sp
                    )
                }

                if (!term.exampleUsage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccountFieldBg,
                        border = BorderStroke(1.dp, BorderGrey.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Example Usage / Application", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TealTile)
                            Text(text = term.exampleUsage, fontSize = 13.sp, color = TextDark, lineHeight = 19.sp)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Got It", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
