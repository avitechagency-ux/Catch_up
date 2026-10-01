package com.example.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TextDecrease
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupabaseNoteUploadDto
import com.example.data.SupabaseRepository
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Structured block inside a parsed Word (.docx) study document.
 */
sealed class DocxBlock {
    data class Heading(val text: String, val level: Int = 1) : DocxBlock()
    data class Paragraph(val text: String, val isBold: Boolean = false, val isItalic: Boolean = false) : DocxBlock()
    data class BulletItem(val text: String) : DocxBlock()
    data class TableBlock(val rows: List<List<String>>) : DocxBlock()
}

/**
 * Section/Page grouping of reflowable DOCX content so students can read clearly on mobile without zooming.
 */
data class DocxSectionPage(
    val sectionNumber: Int,
    val sectionTitle: String,
    val blocks: List<DocxBlock>
)

/**
 * Parsed representation of a .docx document ready for mobile-reflowable rendering.
 */
data class DocxParsedDocument(
    val title: String,
    val subjectName: String,
    val targetClass: String,
    val authorName: String,
    val dateUploaded: String,
    val sections: List<DocxSectionPage>
) {
    val allText: String
        get() = sections.flatMap { it.blocks }.joinToString("\n") { block ->
            when (block) {
                is DocxBlock.Heading -> block.text
                is DocxBlock.Paragraph -> block.text
                is DocxBlock.BulletItem -> "• ${block.text}"
                is DocxBlock.TableBlock -> block.rows.joinToString("\n") { it.joinToString(" | ") }
            }
        }
}

/**
 * Validates that an uploaded file is strictly a Word (.docx) document.
 */
fun isAllowedDocxFile(fileName: String, mimeType: String? = null): Boolean {
    val cleanName = fileName.trim()
    val hasDocxExtension = cleanName.endsWith(".docx", ignoreCase = true)
    if (!hasDocxExtension) return false
    if (mimeType != null && mimeType.isNotBlank()) {
        val cleanMime = mimeType.trim().lowercase()
        if (cleanMime.contains("pdf") || (cleanMime.contains("msword") && !cleanMime.contains("officedocument"))) {
            return false
        }
    }
    return true
}

/**
 * Generates a real, standards-compliant OpenXML Word (.docx) ZIP byte array containing
 * `[Content_Types].xml`, `_rels/.rels`, and `word/document.xml`.
 */
fun generateDocxBytes(
    title: String,
    subject: String,
    targetClass: String = "S.4",
    authorName: String = "Teacher",
    description: String = "",
    paragraphs: List<String> = emptyList()
): ByteArray {
    fun escapeXml(input: String): String = input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    val resolvedParagraphs = paragraphs.ifEmpty {
        listOf(
            description.ifBlank { "Comprehensive $subject study notes prepared for $targetClass students." },
            "1. Key Concepts & Definitions",
            "• Topic: $title ($subject - $targetClass)",
            "• Prepared by: $authorName",
            "• Review the core principles, worked examples, and curriculum definitions in this section carefully.",
            "2. Detailed Curriculum Explanation",
            "Understanding $title in $subject helps $targetClass students apply concepts accurately in classroom exercises and UNEB examinations.",
            "• Read each section clearly at your preferred font size without needing to zoom.",
            "• Summarize the main formulas, terms, and structured points in your revision notebook."
        )
    }

    val bodyXml = buildString {
        // Title heading paragraph
        append("""<w:p><w:pPr><w:pStyle w:val="Title"/></w:pPr><w:r><w:rPr><w:b/></w:rPr><w:t>${escapeXml(title)}</w:t></w:r></w:p>""")
        // Metadata subtitle paragraph
        append("""<w:p><w:pPr><w:pStyle w:val="Subtitle"/></w:pPr><w:r><w:rPr><w:i/></w:rPr><w:t>${escapeXml("Subject: $subject | Class: $targetClass | Author: $authorName")}</w:t></w:r></w:p>""")

        resolvedParagraphs.forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isNotEmpty()) {
                when {
                    line.startsWith("# ") || Regex("^[0-9]+\\.\\s+.+").matches(line) -> {
                        val cleanHeading = line.removePrefix("# ").trim()
                        append("""<w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:rPr><w:b/></w:rPr><w:t>${escapeXml(cleanHeading)}</w:t></w:r></w:p>""")
                    }
                    line.startsWith("•") || line.startsWith("- ") || line.startsWith("* ") -> {
                        val cleanBullet = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                        append("""<w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr><w:r><w:t>${escapeXml(cleanBullet)}</w:t></w:r></w:p>""")
                    }
                    else -> {
                        append("""<w:p><w:r><w:t>${escapeXml(line)}</w:t></w:r></w:p>""")
                    }
                }
            }
        }
    }

    val documentXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    $bodyXml
  </w:body>
</w:document>"""

    val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

    val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zip ->
        zip.putNextEntry(ZipEntry("[Content_Types].xml"))
        zip.write(contentTypesXml.toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("_rels/.rels"))
        zip.write(relsXml.toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("word/document.xml"))
        zip.write(documentXml.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
    return output.toByteArray()
}

/**
 * Parses a Word (.docx) byte array (or fallback note metadata) into reflowable [DocxParsedDocument] sections
 * so students can read notes clearly on mobile without zooming.
 */
fun parseDocxDocument(
    bytes: ByteArray?,
    fallbackTitle: String,
    fallbackSubject: String,
    fallbackTargetClass: String = "S.4",
    fallbackAuthor: String = "Teacher",
    fallbackDate: String = "Today",
    fallbackDescription: String = ""
): DocxParsedDocument {
    val extractedBlocks = mutableListOf<DocxBlock>()

    if (bytes != null && bytes.size >= 4) {
        val isZipArchive = bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
        if (isZipArchive) {
            runCatching {
                var documentXmlContent: String? = null
                ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name.equals("word/document.xml", ignoreCase = true)) {
                            documentXmlContent = zis.readBytes().toString(Charsets.UTF_8)
                            break
                        }
                        entry = zis.nextEntry
                    }
                }
                if (!documentXmlContent.isNullOrBlank()) {
                    extractedBlocks.addAll(parseWordDocumentXml(documentXmlContent!!))
                }
            }
        } else {
            val rawString = runCatching { bytes.toString(Charsets.UTF_8) }.getOrNull().orEmpty().trim()
            if (rawString.isNotBlank() && !rawString.startsWith("%PDF")) {
                rawString.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
                    when {
                        line.startsWith("# ") -> extractedBlocks.add(DocxBlock.Heading(line.removePrefix("# ").trim(), 1))
                        line.startsWith("## ") -> extractedBlocks.add(DocxBlock.Heading(line.removePrefix("## ").trim(), 2))
                        line.startsWith("•") || line.startsWith("- ") || line.startsWith("* ") -> {
                            extractedBlocks.add(
                                DocxBlock.BulletItem(
                                    line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                                )
                            )
                        }
                        else -> extractedBlocks.add(DocxBlock.Paragraph(line))
                    }
                }
            }
        }
    }

    if (extractedBlocks.isEmpty()) {
        val generatedBytes = generateDocxBytes(
            title = fallbackTitle,
            subject = fallbackSubject,
            targetClass = fallbackTargetClass,
            authorName = fallbackAuthor,
            description = fallbackDescription
        )
        runCatching {
            ZipInputStream(ByteArrayInputStream(generatedBytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name.equals("word/document.xml", ignoreCase = true)) {
                        val xml = zis.readBytes().toString(Charsets.UTF_8)
                        extractedBlocks.addAll(parseWordDocumentXml(xml))
                        break
                    }
                    entry = zis.nextEntry
                }
            }
        }
    }

    // Group blocks into clean reading sections so long documents are organized into digestible cards
    val sections = mutableListOf<DocxSectionPage>()
    var currentHeading = fallbackTitle
    var currentList = mutableListOf<DocxBlock>()

    extractedBlocks.forEach { block ->
        if (block is DocxBlock.Heading && currentList.isNotEmpty() && currentList.size >= 3) {
            sections.add(
                DocxSectionPage(
                    sectionNumber = sections.size + 1,
                    sectionTitle = currentHeading,
                    blocks = currentList.toList()
                )
            )
            currentHeading = block.text
            currentList = mutableListOf(block)
        } else {
            if (block is DocxBlock.Heading && currentList.isEmpty()) {
                currentHeading = block.text
            }
            currentList.add(block)
        }
    }
    if (currentList.isNotEmpty()) {
        sections.add(
            DocxSectionPage(
                sectionNumber = sections.size + 1,
                sectionTitle = currentHeading,
                blocks = currentList.toList()
            )
        )
    }

    return DocxParsedDocument(
        title = fallbackTitle,
        subjectName = fallbackSubject,
        targetClass = fallbackTargetClass.ifBlank { "S.4" },
        authorName = fallbackAuthor.ifBlank { "Teacher" },
        dateUploaded = fallbackDate.ifBlank { "Today" },
        sections = sections
    )
}

/**
 * Parses OpenXML `word/document.xml` content into structured [DocxBlock] items.
 */
private fun parseWordDocumentXml(xml: String): List<DocxBlock> {
    val blocks = mutableListOf<DocxBlock>()
    try {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var inParagraph = false
        var inTable = false
        var inTableRow = false
        var inTableCell = false
        var inText = false

        var currentStyle = ""
        var isListParagraph = false
        var isBoldRun = false
        var isItalicRun = false
        val paragraphBuilder = StringBuilder()

        val tableRows = mutableListOf<List<String>>()
        var currentRowCells = mutableListOf<String>()
        val cellBuilder = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name?.substringAfterLast(':') ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "tbl" -> {
                            inTable = true
                            tableRows.clear()
                        }
                        "tr" -> if (inTable) {
                            inTableRow = true
                            currentRowCells = mutableListOf()
                        }
                        "tc" -> if (inTableRow) {
                            inTableCell = true
                            cellBuilder.clear()
                        }
                        "p" -> if (!inTable) {
                            inParagraph = true
                            currentStyle = ""
                            isListParagraph = false
                            isBoldRun = false
                            isItalicRun = false
                            paragraphBuilder.clear()
                        }
                        "pStyle" -> if (inParagraph) {
                            for (i in 0 until parser.attributeCount) {
                                val attrName = parser.getAttributeName(i).substringAfterLast(':')
                                if (attrName == "val") {
                                    currentStyle = parser.getAttributeValue(i) ?: ""
                                }
                            }
                        }
                        "numPr", "ilvl", "numId" -> if (inParagraph) {
                            isListParagraph = true
                        }
                        "b" -> if (inParagraph) {
                            isBoldRun = true
                        }
                        "i" -> if (inParagraph) {
                            isItalicRun = true
                        }
                        "br" -> {
                            if (inTableCell) cellBuilder.append("\n")
                            else if (inParagraph) paragraphBuilder.append("\n")
                        }
                        "t" -> {
                            inText = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inText) {
                        val txt = parser.text.orEmpty()
                        if (inTableCell) {
                            cellBuilder.append(txt)
                        } else if (inParagraph) {
                            paragraphBuilder.append(txt)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "t" -> inText = false
                        "tc" -> if (inTableRow) {
                            inTableCell = false
                            currentRowCells.add(cellBuilder.toString().trim())
                        }
                        "tr" -> if (inTable) {
                            inTableRow = false
                            if (currentRowCells.any { it.isNotBlank() }) {
                                tableRows.add(currentRowCells.toList())
                            }
                        }
                        "tbl" -> {
                            inTable = false
                            if (tableRows.isNotEmpty()) {
                                blocks.add(DocxBlock.TableBlock(tableRows.toList()))
                            }
                        }
                        "p" -> if (inParagraph && !inTable) {
                            inParagraph = false
                            val text = paragraphBuilder.toString().trim()
                            if (text.isNotEmpty()) {
                                val styleLower = currentStyle.lowercase()
                                when {
                                    styleLower.contains("title") || styleLower.contains("heading1") || styleLower == "h1" -> {
                                        blocks.add(DocxBlock.Heading(text, level = 1))
                                    }
                                    styleLower.contains("heading") || styleLower.contains("subtitle") -> {
                                        blocks.add(DocxBlock.Heading(text, level = 2))
                                    }
                                    isListParagraph || styleLower.contains("list") || text.startsWith("•") -> {
                                        blocks.add(DocxBlock.BulletItem(text.removePrefix("•").trim()))
                                    }
                                    else -> {
                                        blocks.add(
                                            DocxBlock.Paragraph(
                                                text = text,
                                                isBold = isBoldRun,
                                                isItalic = isItalicRun
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (_: Exception) {
        val textMatches = Regex("<w:t[^>]*>(.*?)</w:t>").findAll(xml)
            .map { it.groupValues[1] }
            .filter { it.isNotBlank() }
            .toList()
        textMatches.forEach { line ->
            blocks.add(DocxBlock.Paragraph(line.trim()))
        }
    }
    return blocks
}

/**
 * Helper to persist uploaded document bytes (.docx or legacy bytes) locally on device by subject & title.
 */
fun savePdfBytesLocally(context: Context, subjectName: String, noteTitle: String, bytes: ByteArray) {
    runCatching {
        val dir = File(context.filesDir, "uploaded_pdfs")
        if (!dir.exists()) dir.mkdirs()
        val safeKey = "${subjectName.trim().lowercase()}_${noteTitle.trim().lowercase()}"
            .replace(Regex("[^a-z0-9._-]"), "_")
        File(dir, "$safeKey.docx").writeBytes(bytes)
    }
}

/**
 * Helper to load locally cached document bytes (.docx or legacy .pdf) by subject & title.
 */
fun loadPdfBytesLocally(context: Context, subjectName: String, noteTitle: String): ByteArray? {
    return runCatching {
        val dir = File(context.filesDir, "uploaded_pdfs")
        val safeKey = "${subjectName.trim().lowercase()}_${noteTitle.trim().lowercase()}"
            .replace(Regex("[^a-z0-9._-]"), "_")
        val docxFile = File(dir, "$safeKey.docx")
        val pdfFile = File(dir, "$safeKey.pdf")
        when {
            docxFile.exists() && docxFile.length() > 0L -> docxFile.readBytes()
            pdfFile.exists() && pdfFile.length() > 0L -> pdfFile.readBytes()
            else -> null
        }
    }.getOrNull()
}

/**
 * Clear, Mobile-Reflowable DOCX Study Note Viewer Screen.
 *
 * Parses Word (.docx) documents and renders them in a crisp, responsive mobile reading layout
 * with adjustable font sizes so students can read notes clearly without needing to zoom or pan.
 */
@Composable
fun PdfViewerScreen(
    noteTitle: String = "Algebra",
    subjectName: String = "Math",
    isSavedInitially: Boolean = false,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNoteOpened: () -> Unit = {},
    onToggleSave: (Boolean) -> Unit = {}
) {
    var isSaved by remember(isSavedInitially) { mutableStateOf(isSavedInitially) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var remoteNoteDetail by remember(noteTitle, subjectName) {
        mutableStateOf<SupabaseNoteUploadDto?>(null)
    }
    var uploadedDocxBytes by remember(noteTitle, subjectName) {
        mutableStateOf<ByteArray?>(loadPdfBytesLocally(context, subjectName, noteTitle))
    }
    var isFetchingDocument by remember(noteTitle, subjectName) {
        mutableStateOf(uploadedDocxBytes == null)
    }
    // Adjustable reading font scale (default 17sp body text for effortless reading without zooming)
    var bodyFontSp by remember { mutableIntStateOf(17) }

    val subjectThemeColor = remember(subjectName) { getSubjectThemeColor(subjectName) }

    // Trigger callback when note is opened & sync read history + fetch remote .docx bytes
    LaunchedEffect(noteTitle, subjectName) {
        onNoteOpened()
        val prefs = context.getSharedPreferences("catch_up_prefs", Context.MODE_PRIVATE)
        val studentPhone = (prefs.getString("user_email", null) ?: prefs.getString("user_phone", "")) ?: ""
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        SupabaseRepository.recordRecentNoteInSupabase(
            studentPhone = studentPhone,
            noteTitle = noteTitle,
            subjectName = subjectName,
            openedDate = todayStr
        )
        remoteNoteDetail = SupabaseRepository.fetchNoteDetail(
            noteTitle = noteTitle,
            subjectName = subjectName
        )
        if (uploadedDocxBytes == null) {
            val fetchedBytes = SupabaseRepository.fetchNotePdfBytes(
                noteTitle = noteTitle,
                subjectName = subjectName
            )
            if (fetchedBytes != null && fetchedBytes.isNotEmpty()) {
                savePdfBytesLocally(context, subjectName, noteTitle, fetchedBytes)
                uploadedDocxBytes = fetchedBytes
            }
        }
        isFetchingDocument = false
    }

    // Parse DOCX bytes into reflowable mobile reading sections
    val parsedDocument by remember(uploadedDocxBytes, remoteNoteDetail, noteTitle, subjectName) {
        derivedStateOf {
            parseDocxDocument(
                bytes = uploadedDocxBytes,
                fallbackTitle = remoteNoteDetail?.title ?: noteTitle,
                fallbackSubject = remoteNoteDetail?.subjectName ?: subjectName,
                fallbackTargetClass = remoteNoteDetail?.targetClass ?: "S.4",
                fallbackAuthor = remoteNoteDetail?.authorName ?: "Teacher",
                fallbackDate = remoteNoteDetail?.dateUploaded ?: "Today",
                fallbackDescription = remoteNoteDetail?.description ?: ""
            )
        }
    }

    val totalSections = parsedDocument.sections.size.coerceAtLeast(1)

    val currentPage by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                1
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val centerItem = visibleItems.minByOrNull { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                }
                (centerItem?.index?.plus(1) ?: 1).coerceIn(1, totalSections)
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5FA))
            .testTag("pdf_viewer_screen"),
        containerColor = Color(0xFFF6F5FA),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 64.dp)
            )
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndigoPurple)
                    .statusBarsPadding()
                    .testTag("pdf_viewer_top_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pdf_viewer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = noteTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("pdf_viewer_title")
                        )
                        val subtitleText = buildString {
                            append(subjectName)
                            remoteNoteDetail?.targetClass?.takeIf { it.isNotBlank() }?.let {
                                append(" • ")
                                append(it)
                            }
                            append(" • DOCX")
                        }
                        Text(
                            text = subtitleText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("pdf_viewer_subtitle")
                        )
                    }

                    IconButton(
                        onClick = {
                            isSaved = !isSaved
                            onToggleSave(isSaved)
                            val message = if (isSaved) {
                                "Saved for offline access"
                            } else {
                                "Removed from offline cache"
                            }
                            val prefs = context.getSharedPreferences("catch_up_prefs", Context.MODE_PRIVATE)
                            val studentPhone = (prefs.getString("user_email", null) ?: prefs.getString("user_phone", "")) ?: ""
                            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            coroutineScope.launch {
                                SupabaseRepository.syncSavedNoteInSupabase(
                                    studentPhone = studentPhone,
                                    noteTitle = noteTitle,
                                    subjectName = subjectName,
                                    dateAdded = todayStr,
                                    isSaved = isSaved,
                                    fileUrl = remoteNoteDetail?.fileUrl ?: ""
                                )
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pdf_viewer_save_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved for offline access" else "Save for offline access",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Clear Reading Toolbar: DOCX Reflowable Badge + Text Size Controls (No Zoom Needed)
                Surface(
                    color = Color(0xFF3E337A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TealTile.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "DOCX READER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64FFDA),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "Clear mobile view • Auto-fit text",
                                fontSize = 11.sp,
                                color = LightLavender
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .clickable { bodyFontSp = (bodyFontSp - 2).coerceAtLeast(14) }
                                    .testTag("docx_font_decrease_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.TextDecrease,
                                        contentDescription = "Decrease font size",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("A-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Text(
                                text = "${bodyFontSp}sp",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .clickable { bodyFontSp = (bodyFontSp + 2).coerceAtMost(26) }
                                    .testTag("docx_font_increase_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.TextIncrease,
                                        contentDescription = "Increase font size",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
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
            if (isFetchingDocument) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = IndigoPurple)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Opening DOCX study note...",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pdf_pages_list"),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 14.dp,
                        bottom = 84.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(parsedDocument.sections) { index, section ->
                        DocxSectionCard(
                            section = section,
                            document = parsedDocument,
                            subjectThemeColor = subjectThemeColor,
                            bodyFontSp = bodyFontSp,
                            isFirstSection = index == 0,
                            modifier = Modifier.testTag("pdf_page_card_${index + 1}")
                        )
                    }
                }
            }

            // Bottom floating section indicator pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = IndigoPurple,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("pdf_bottom_page_pill")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Page $currentPage of $totalSections",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.testTag("pdf_page_indicator_text")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders a reflowable Word (.docx) section card with large, clear, wrapped text that requires zero zooming.
 */
@Composable
private fun DocxSectionCard(
    section: DocxSectionPage,
    document: DocxParsedDocument,
    subjectThemeColor: Color,
    bodyFontSp: Int,
    isFirstSection: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGrey.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isFirstSection) {
                // Top Subject & Class Header Banner inside the first document card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = subjectThemeColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = document.subjectName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = subjectThemeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoPurple.copy(alpha = 0.10f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = document.targetClass,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPurple
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealTile.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = TealTile,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = ".DOCX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealTile
                            )
                        }
                    }
                }

                // Document Title
                Text(
                    text = document.title,
                    fontSize = (bodyFontSp + 5).sp,
                    lineHeight = (bodyFontSp + 12).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                // Author & Date Metadata Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = document.authorName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = document.dateUploaded,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                HorizontalDivider(color = BorderGrey.copy(alpha = 0.7f))
            }

            // Render each structured block from the DOCX file
            section.blocks.forEach { block ->
                when (block) {
                    is DocxBlock.Heading -> {
                        if (!isFirstSection || !block.text.equals(document.title, ignoreCase = true)) {
                            val headingSize = if (block.level == 1) (bodyFontSp + 3).sp else (bodyFontSp + 1).sp
                            Text(
                                text = block.text,
                                fontSize = headingSize,
                                lineHeight = (headingSize.value + 8).sp,
                                fontWeight = FontWeight.Bold,
                                color = subjectThemeColor,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    is DocxBlock.Paragraph -> {
                        Text(
                            text = block.text,
                            fontSize = bodyFontSp.sp,
                            lineHeight = (bodyFontSp + 9).sp,
                            fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
                            color = TextDark
                        )
                    }
                    is DocxBlock.BulletItem -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftLavenderGrey.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(subjectThemeColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = block.text,
                                    fontSize = bodyFontSp.sp,
                                    lineHeight = (bodyFontSp + 8).sp,
                                    color = TextDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    is DocxBlock.TableBlock -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderGrey),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                block.rows.forEachIndexed { rowIndex, rowCells ->
                                    val isHeaderRow = rowIndex == 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isHeaderRow) subjectThemeColor.copy(alpha = 0.12f)
                                                else if (rowIndex % 2 == 1) SoftLavenderGrey.copy(alpha = 0.35f)
                                                else Color.White
                                            )
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowCells.forEach { cellText ->
                                            Text(
                                                text = cellText,
                                                fontSize = (bodyFontSp - 1).sp,
                                                lineHeight = (bodyFontSp + 5).sp,
                                                fontWeight = if (isHeaderRow) FontWeight.Bold else FontWeight.Normal,
                                                color = TextDark,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    if (rowIndex < block.rows.lastIndex) {
                                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PdfViewerScreenPreview() {
    PdfViewerScreen(
        noteTitle = "Algebra",
        subjectName = "Math",
        isSavedInitially = false
    )
}
