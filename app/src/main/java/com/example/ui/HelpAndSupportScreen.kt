package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * Data class representing a Frequently Asked Question item.
 */
data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String
)

/**
 * Standard 5 FAQ questions defined in Catch Up specifications.
 */
val defaultFaqItems = listOf(
    FaqItem(
        id = 1,
        question = "How do I access notes for my class?",
        answer = "Open the Subjects tab, select your subject, and tap any topic to view the note."
    ),
    FaqItem(
        id = 2,
        question = "Why can't I see notes for a subject?",
        answer = "Your teacher may not have shared any notes yet, or your account may not be linked to the correct class. Contact your school if this seems incorrect."
    ),
    FaqItem(
        id = 3,
        question = "How do I save a note for offline access?",
        answer = "Open any note and tap the bookmark icon in the top bar. It will appear in your Downloads tab."
    ),
    FaqItem(
        id = 4,
        question = "How do I renew or change my plan?",
        answer = "Go to Profile > Account to view and select a new plan."
    ),
    FaqItem(
        id = 5,
        question = "I forgot my password. What do I do?",
        answer = "Contact your school or use the support details below to reach us directly."
    )
)

/**
 * Help & Support Screen for the Catch Up app.
 * Reached by tapping "Help & Support" on the Profile screen (shared structure for both Student and Teacher panels).
 *
 * HEADER CARD:
 * - Solid deep indigo-purple #4B3F8F
 * - Rounded bottom corners: bottomStart = 28dp, bottomEnd = 28dp
 * - statusBarsPadding() with generous internal padding
 * - Left back arrow to return to profile (Color.White)
 * - Bold "Help & Support" header text (24sp, Bold, Color.White)
 *
 * SECTION 1 — FAQ:
 * - 20dp top padding below top bar, 20dp left/right margins
 * - Section label: "Frequently Asked Questions" (13sp, weight Medium, color #8A8A8A)
 * - Accordion style FAQ cards with 10dp spacing between items
 * - Card: background #F7F6FB, corner radius 14dp, padding 16dp
 * - Collapsed: question text (14sp Medium #1A1A1A) + chevron-down (18dp, #8A8A8A)
 * - Expanded: rotated chevron, answer text below (10dp gap, 13sp Regular #6B6B6B, lineHeight 20sp)
 *
 * SECTION 2 — Contact Us:
 * - 28dp spacing below Section 1
 * - Section label: "Contact Us" (13sp, weight Medium, color #8A8A8A)
 * - Grouped card: background #F7F6FB, corner radius 16dp, containing rows with 1dp dividers (#ECEAF5)
 * - Rows (each 56dp height, 16dp horizontal padding):
 *   1. Phone icon + "Call us" -> "+256 704 039 55" (opens dialer)
 *   2. WhatsApp icon + "WhatsApp" -> "+256 704 039 55" (opens WhatsApp)
 *   3. Phone icon + "Alternative line" -> "+256 788 166 600" (opens dialer)
 *   4. Mail icon + "Email us" -> "mosesylv@gmail.com" (opens email client)
 */
@Composable
fun HelpAndSupportScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Track expanded state for FAQ items by id
    val expandedFaqs = remember { mutableStateMapOf<Int, Boolean>() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("help_and_support_screen"),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .navigationBarsPadding()
        ) {
            // ======================================
            // HEADER CARD
            // Signature deep indigo-purple #4B3F8F card header:
            // - Solid deep indigo-purple #4B3F8F
            // - Rounded bottom corners: bottomStart = 28dp, bottomEnd = 28dp
            // - statusBarsPadding() with generous internal padding
            // - Left back arrow to return to profile
            // - Bold "Help & Support" header text (24sp, Bold, Color.White)
            // ======================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
                    .testTag("help_header_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("help_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Help & Support",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("help_title")
                    )
                }
            }

            // ======================================
            // SCROLLABLE BODY
            // ======================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 36.dp)
            ) {
                // ======================================
                // SECTION 1 — FAQ
                // ======================================
                Text(
                    text = "Frequently Asked Questions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8A8A8A),
                    modifier = Modifier.testTag("section_label_faq")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Vertical list of expandable FAQ items (accordion style), 10dp spacing
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    defaultFaqItems.forEach { item ->
                        val isExpanded = expandedFaqs[item.id] == true
                        FaqAccordionCard(
                            item = item,
                            isExpanded = isExpanded,
                            onToggle = {
                                expandedFaqs[item.id] = !isExpanded
                            }
                        )
                    }
                }

                // 28dp spacing below Section 1
                Spacer(modifier = Modifier.height(28.dp))

                // ======================================
                // SECTION 2 — Contact Us
                // ======================================
                Text(
                    text = "Contact Us",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8A8A8A),
                    modifier = Modifier.testTag("section_label_contact_us")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Grouped card: background #F7F6FB, corner radius 16dp
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_us_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = SoftLavenderGrey
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Email us
                        ContactRow(
                            iconVector = Icons.Default.Email,
                            label = "Email us",
                            value = "mosesylv@gmail.com",
                            onClick = {
                                openEmail(context, "mosesylv@gmail.com")
                            },
                            testTag = "contact_row_email_us"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Channel Button Link
                Button(
                    onClick = {
                        openWhatsAppChannel(
                            context,
                            "https://whatsapp.com/channel/0029Vb9JiCGF1YlVKEibAe1x"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("contact_row_whatsapp"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp Channel",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Join WhatsApp Channel",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open Channel Link",
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
 * Expandable FAQ card with smooth rotation and vertical accordion expansion.
 *
 * Card: background #F7F6FB, corner radius 14dp, padding 16dp
 * Collapsed state: question text (14sp, Medium, #1A1A1A) with chevron-down (18dp, #8A8A8A)
 * Expanded state: chevron rotates up, answer text below (10dp gap, 13sp Regular #6B6B6B)
 */
@Composable
fun FaqAccordionCard(
    item: FaqItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "faq_chevron_rotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .testTag("faq_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        color = SoftLavenderGrey
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                        .testTag("faq_question_${item.id}")
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse question" else "Expand question",
                    tint = Color(0xFF8A8A8A),
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotationAngle)
                        .testTag("faq_chevron_${item.id}")
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.answer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF6B6B6B),
                        lineHeight = 20.sp,
                        modifier = Modifier.testTag("faq_answer_${item.id}")
                    )
                }
            }
        }
    }
}

/**
 * 1dp divider (#ECEAF5) between contact rows.
 */
@Composable
private fun ContactDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFECEAF5))
    )
}

/**
 * Shared Contact Row with Material ImageVector icon:
 * - 56dp height, 16dp horizontal padding
 * - Icon (20dp, #4B3F8F) + Label (14sp, #1A1A1A) on the left
 * - Value (13sp, #6B6B6B) right-aligned, tappable
 */
@Composable
fun ContactRow(
    iconVector: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = IndigoPurple,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A1A1A)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Shared Contact Row with Custom drawable resource (e.g. WhatsApp icon).
 */
@Composable
fun ContactRowWithCustomIcon(
    iconResId: Int,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = IndigoPurple,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A1A1A)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Helper to safely open the WhatsApp Channel link.
 */
private fun openWhatsAppChannel(context: Context, channelUrl: String) {
    try {
        val uri = Uri.parse(channelUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open WhatsApp Channel link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Helper to safely open the default email client.
 */
private fun openEmail(context: Context, emailAddress: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open email app for $emailAddress", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HelpAndSupportScreenPreview() {
    HelpAndSupportScreen()
}
