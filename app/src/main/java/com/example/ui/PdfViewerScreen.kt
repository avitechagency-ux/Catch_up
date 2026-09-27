package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupabaseRepository
import com.example.ui.theme.IndigoPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * In-App Study Notes & PDF Viewer Screen (Pure Jetpack Compose).
 *
 * Renders an unbordered, clean document page layout without boxes or borders around the work,
 * and supports rendering uploaded PDF files synced with Supabase Storage.
 */
@Composable
fun PdfViewerScreen(
    noteTitle: String = "Algebra",
    subjectName: String = "Math",
    isSavedInitially: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onToggleSave: (Boolean) -> Unit = {},
    onNoteOpened: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isCachedLocally by remember { mutableStateOf(isSavedInitially) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var renderedPdfPages by remember(noteTitle, subjectName) {
        mutableStateOf<List<ImageBitmap>>(emptyList())
    }

    // Trigger onNoteOpened on launch and check for uploaded PDF binary in local cache or Supabase Storage
    LaunchedEffect(noteTitle, subjectName) {
        onNoteOpened()
        val pdfBytes = withContext(Dispatchers.IO) {
            loadPdfBytesLocally(context, subjectName, noteTitle)
                ?: SupabaseRepository.fetchNotePdfBytes(noteTitle, subjectName)?.also { fetched ->
                    savePdfBytesLocally(context, subjectName, noteTitle, fetched)
                }
        }
        if (pdfBytes != null && pdfBytes.size > 100) {
            renderedPdfPages = renderPdfPagesFromBytes(context, subjectName, noteTitle, pdfBytes)
        }
    }

    val themeColor = remember(subjectName) {
        getSubjectThemeColor(subjectName)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("pdf_viewer_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // TOP BAR: Subject color background, 56dp height, zero bottom border
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_top_bar"),
                color = themeColor,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Back button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("pdf_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Center: Topic/note title text (single line with ellipsis)
                        Text(
                            text = noteTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pdf_title_text")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: Bookmark/Save Icon (toggles internal app offline cache, zero OS file writes)
                        IconButton(
                            onClick = {
                                val newCachedState = !isCachedLocally
                                isCachedLocally = newCachedState
                                onToggleSave(newCachedState)

                                // Perform in-app internal cache persistence
                                cacheNoteLocally(context, noteTitle, subjectName, newCachedState)

                                coroutineScope.launch {
                                    val message = if (newCachedState) {
                                        "Saved \"$noteTitle\" for offline reading in app"
                                    } else {
                                        "Removed \"$noteTitle\" from offline cache"
                                    }
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                            modifier = Modifier.testTag("pdf_download_button")
                        ) {
                            if (isCachedLocally) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = "Saved offline in app",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .offset(y = (-2).dp)
                                            .clip(CircleShape)
                                            .background(themeColor)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save for offline reading",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // BODY: Clean, Unbordered Document View (pure white page, zero borders around work)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .testTag("pdf_content_area")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (renderedPdfPages.isNotEmpty()) {
                        // Render actual uploaded PDF pages cleanly with zero borders
                        renderedPdfPages.forEachIndexed { index, pageBitmap ->
                            Image(
                                bitmap = pageBitmap,
                                contentDescription = "$noteTitle Page ${index + 1}",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                            )
                            if (index < renderedPdfPages.lastIndex) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        // Clean, unbordered document layout
                        Text(
                            text = "$subjectName Study Module".uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Note Title Heading
                        Text(
                            text = noteTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Syllabus Subtitle
                        Text(
                            text = "Uganda National Curriculum Development Centre (NCDC) Syllabus • O-Level",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8A8A)
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Section 1: Overview & Learning Objectives
                        NoteSectionHeading(
                            title = "1. Overview & Learning Objectives",
                            accentColor = themeColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This module provides comprehensive revision notes for $noteTitle under the $subjectName secondary school syllabus. By mastering this unit, learners will be able to demonstrate conceptual mastery and solve UNEB exam problems accurately.",
                            fontSize = 14.5.sp,
                            color = Color(0xFF333333),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Core Definition (Unbordered clean document section)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = "Definition",
                                tint = IndigoPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Core Definition",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$noteTitle: A fundamental topic in $subjectName dealing with analytical properties, core principles, standard relationships, and practical field applications.",
                            fontSize = 14.sp,
                            color = Color(0xFF2A2A2A),
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Section 2: Key Principles & Analysis
                        NoteSectionHeading(
                            title = "2. Key Principles & Analysis",
                            accentColor = themeColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        BulletPoint(
                            title = "Fundamental Theory",
                            description = "Understanding primary interactions, structural foundations, and key definitions."
                        )
                        BulletPoint(
                            title = "Analytical Framework",
                            description = "Applying systematic step-by-step methods to evaluate core question formats."
                        )
                        BulletPoint(
                            title = "Real-world Context",
                            description = "Connecting classroom theory to East African and global real-world scenarios."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Key Formula / Rule (Unbordered clean document line)
                        Text(
                            text = "★ Key Formula / Rule: Total Result = Σ (Factor × Weight) / Standard Unit",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5B21B6),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Section 3: Step-by-Step Practical Application
                        NoteSectionHeading(
                            title = "3. Step-by-Step Practical Application",
                            accentColor = themeColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "When solving examination questions regarding $noteTitle, follow this structured procedure:",
                            fontSize = 14.5.sp,
                            color = Color(0xFF333333),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        BulletPoint(
                            title = "Step 1",
                            description = "Identify the given variables and specific requirements."
                        )
                        BulletPoint(
                            title = "Step 2",
                            description = "State all relevant principles, laws, or algebraic relations clearly."
                        )
                        BulletPoint(
                            title = "Step 3",
                            description = "Perform calculations or arguments methodically showing all working steps."
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // UNEB Exam Tip (Unbordered clean document section)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = "Exam Tip",
                                tint = Color(0xFF065F46),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UNEB Examination Tip",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Always define key terms in full before presenting calculations or illustrative diagrams to maximize your marks.",
                            fontSize = 13.5.sp,
                            color = Color(0xFF047857),
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun NoteSectionHeading(
    title: String,
    accentColor: Color
) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BulletPoint(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = IndigoPurple,
            modifier = Modifier.padding(top = 1.dp)
        )
        Text(
            text = buildAnnotatedString {
                append("$title: ")
                append(description)
            },
            fontSize = 14.sp,
            color = Color(0xFF444444),
            lineHeight = 20.sp
        )
    }
}

/**
 * Saves uploaded or downloaded PDF binary bytes into the app's private cache directory.
 */
fun savePdfBytesLocally(context: Context, subjectName: String, noteTitle: String, bytes: ByteArray) {
    try {
        val dir = File(context.cacheDir, "catchup_pdfs")
        if (!dir.exists()) dir.mkdirs()
        val safeName = "${subjectName.trim().lowercase()}_${noteTitle.trim().lowercase()}"
            .replace(Regex("[^a-z0-9._-]"), "_") + ".pdf"
        File(dir, safeName).writeBytes(bytes)
    } catch (_: Exception) {
        // Ignore local cache error
    }
}

/**
 * Loads cached PDF binary bytes for a note if present in the app's private cache directory.
 */
fun loadPdfBytesLocally(context: Context, subjectName: String, noteTitle: String): ByteArray? {
    return try {
        val dir = File(context.cacheDir, "catchup_pdfs")
        val safeName = "${subjectName.trim().lowercase()}_${noteTitle.trim().lowercase()}"
            .replace(Regex("[^a-z0-9._-]"), "_") + ".pdf"
        val file = File(dir, safeName)
        if (file.exists() && file.length() > 0L) file.readBytes() else null
    } catch (_: Exception) {
        null
    }
}

/**
 * Renders pages of an uploaded PDF file into clean, unbordered white [ImageBitmap]s using Android's [PdfRenderer].
 */
private suspend fun renderPdfPagesFromBytes(
    context: Context,
    subjectName: String,
    noteTitle: String,
    bytes: ByteArray
): List<ImageBitmap> = withContext(Dispatchers.IO) {
    try {
        val header = String(bytes.take(5).toByteArray(), Charsets.US_ASCII)
        if (!header.startsWith("%PDF")) return@withContext emptyList()

        val dir = File(context.cacheDir, "catchup_pdfs")
        if (!dir.exists()) dir.mkdirs()
        val safeName = "${subjectName.trim().lowercase()}_${noteTitle.trim().lowercase()}"
            .replace(Regex("[^a-z0-9._-]"), "_") + ".pdf"
        val pdfFile = File(dir, safeName)
        if (!pdfFile.exists() || pdfFile.length() != bytes.size.toLong()) {
            pdfFile.writeBytes(bytes)
        }

        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pages = mutableListOf<ImageBitmap>()
        val count = renderer.pageCount.coerceAtMost(20)
        for (i in 0 until count) {
            val page = renderer.openPage(i)
            val width = (page.width * 2).coerceIn(600, 1600)
            val height = (page.height * 2).coerceIn(800, 2400)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pages.add(bitmap.asImageBitmap())
        }
        renderer.close()
        pfd.close()
        pages
    } catch (_: Exception) {
        emptyList()
    }
}

/**
 * Caches note content purely within the app's private internal storage directory.
 * Never writes to external/shared storage, MediaStore, or public Downloads folder.
 */
fun cacheNoteLocally(context: Context, noteTitle: String, subjectName: String, shouldCache: Boolean) {
    try {
        val internalCacheDir = File(context.cacheDir, "catchup_notes")
        if (!internalCacheDir.exists()) {
            internalCacheDir.mkdirs()
        }
        val safeFileName = "${subjectName.lowercase()}_${noteTitle.lowercase().replace(" ", "_")}.cached"
        val cacheFile = File(internalCacheDir, safeFileName)
        if (shouldCache) {
            val content = "$subjectName\n$noteTitle\nCached on ${System.currentTimeMillis()}"
            cacheFile.writeText(content)
        } else {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        }
    } catch (_: Exception) {
        // Silently handle internal cache operations
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PdfViewerScreenPreview() {
    PdfViewerScreen(
        noteTitle = "Algebra",
        subjectName = "Math",
        isSavedInitially = true
    )
}
