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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
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
    // data class Image(val rId: String) : DocxBlock()
}

// ... (Rest of parseDocxDocument and parseWordDocumentXml updates) ...

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
                else -> ""
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
 * Does NOT inject synthetic metadata headers or subtitles.
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
        val descText = description.trim()
        if (descText.isNotBlank()) {
            listOf(title, descText)
        } else {
            listOf(
                title,
                "Study notes for $title in $subject ($targetClass)."
            )
        }
    }

    val bodyXml = buildString {
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
 * Parses a Word (.docx) byte array (or fallback note content) into reflowable [DocxParsedDocument]
 * so students can read notes clearly on mobile without zooming.
 * Does NOT inject synthetic metadata banners or artificial section page splits.
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

    // Keep all extracted blocks in a single, continuous, natural document flow (no artificial page breaks)
    val sections = mutableListOf<DocxSectionPage>()
    if (extractedBlocks.isNotEmpty()) {
        sections.add(
            DocxSectionPage(
                sectionNumber = 1,
                sectionTitle = fallbackTitle,
                blocks = extractedBlocks
            )
        )
    } else {
        sections.add(
            DocxSectionPage(
                sectionNumber = 1,
                sectionTitle = fallbackTitle,
                blocks = listOf(DocxBlock.Paragraph(fallbackTitle))
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
 * Clean, Mobile-Reflowable DOCX Study Note Viewer Screen.
 *
 * Renders the actual document text naturally and cleanly without artificial headers,
 * banners, or page count pills, supporting adjustable font sizes for comfortable reading.
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

    // Parse DOCX bytes into reflowable document content
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

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
            .testTag("pdf_viewer_screen"),
        containerColor = Color(0xFFF8F7FC),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
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
                        Text(
                            text = subjectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = LightLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("pdf_viewer_subtitle")
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick font decrease
                        IconButton(
                            onClick = { bodyFontSp = (bodyFontSp - 2).coerceAtLeast(13) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("docx_font_decrease_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TextDecrease,
                                contentDescription = "Decrease font size",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Quick font increase
                        IconButton(
                            onClick = { bodyFontSp = (bodyFontSp + 2).coerceAtMost(28) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("docx_font_increase_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TextIncrease,
                                contentDescription = "Increase font size",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Bookmark save button
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
                                .size(44.dp)
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
                            text = "Opening study note...",
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
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(parsedDocument.sections) { index, section ->
                        DocxSectionCard(
                            section = section,
                            bodyFontSp = bodyFontSp,
                            modifier = Modifier.testTag("pdf_page_card_${index + 1}")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the actual document text directly and clearly without added headers or banners.
 */
@Composable
private fun DocxSectionCard(
    section: DocxSectionPage,
    bodyFontSp: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x0F000000),
                spotColor = Color(0x0F000000)
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGrey.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            section.blocks.forEach { block ->
                when (block) {
                    is DocxBlock.Heading -> {
                        val headingSize = if (block.level == 1) (bodyFontSp + 3).sp else (bodyFontSp + 1).sp
                        Text(
                            text = block.text,
                            fontSize = headingSize,
                            lineHeight = (headingSize.value + 6).sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is DocxBlock.Paragraph -> {
                        Text(
                            text = block.text,
                            fontSize = bodyFontSp.sp,
                            lineHeight = (bodyFontSp + 8).sp,
                            fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
                            color = TextDark
                        )
                    }
                    is DocxBlock.BulletItem -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                fontSize = bodyFontSp.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple
                            )
                            Text(
                                text = block.text,
                                fontSize = bodyFontSp.sp,
                                lineHeight = (bodyFontSp + 8).sp,
                                color = TextDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    is DocxBlock.TableBlock -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
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
                                                if (isHeaderRow) SoftLavenderGrey
                                                else if (rowIndex % 2 == 1) Color(0xFFFAFAFD)
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
                                        HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))
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
