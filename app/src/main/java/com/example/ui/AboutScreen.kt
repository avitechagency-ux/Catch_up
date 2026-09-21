package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.SoftLavenderGrey

/**
 * About Page for the Catch Up app.
 * Reached by tapping "About" on the Profile screen (shared structure for both Student and Teacher panels).
 *
 * HEADER CARD:
 * - Solid deep indigo-purple #4B3F8F
 * - Rounded bottom corners: bottomStart = 28dp, bottomEnd = 28dp
 * - statusBarsPadding() with generous internal padding
 * - Left back arrow to return to profile (Color.White)
 * - Bold "About" header text (24sp, Bold, Color.White)
 *
 * SECTION 1 — App Identity:
 * - 32dp top padding below header, centered content
 * - App icon/logo: circular container, 72dp diameter, background #4B3F8F, centered book/graduation-cap icon (32dp, white)
 * - 12dp spacing below icon
 * - App name: "Catch Up" — font 20sp, weight Bold, color #1A1A1A, centered
 * - 4dp spacing below
 * - Tagline: "Get the notes. Catch up. Keep learning." — font 13sp, weight Regular, color #8A8A8A, centered, italic style
 * - 8dp spacing below
 * - Version text: "Version 1.0.0" — font 12sp, weight Regular, color #9C9C9C, centered
 *
 * SECTION 2 — About the App:
 * - 28dp spacing below Section 1, 20dp left/right margins
 * - Section label: "About Catch Up" (13sp, weight Medium, color #8A8A8A)
 * - Below it (10dp gap), body paragraph text: font 14sp, weight Regular, color #1A1A1A, comfortable line height (22sp)
 *
 * SECTION 3 — Made By:
 * - 28dp spacing below Section 2
 * - Section label: "Made By" (13sp, weight Medium, color #8A8A8A)
 * - Grouped card: background #F7F6FB, corner radius 16dp, containing 2 rows with a 1dp divider (#ECEAF5)
 * - ROW 1: Ssenyonga Moses (Developer)
 * - ROW 2: Nakintu Sylivia (Teacher & Product Lead)
 */
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen"),
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
            // - Bold "About" header text (24sp, Bold, Color.White)
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
                    .testTag("about_header_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("about_back_button")
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
                        text = "About",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("about_title")
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
                    .padding(bottom = 36.dp)
            ) {
                // ======================================
                // SECTION 1 — App Identity
                // ======================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 20.dp, end = 20.dp)
                        .testTag("about_section_identity"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App icon/logo: circular container, 72dp diameter, background #4B3F8F, centered book/graduation-cap icon (32dp, white)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(color = IndigoPurple, shape = CircleShape)
                            .testTag("about_app_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Catch Up Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 12dp spacing below icon
                    Spacer(modifier = Modifier.height(12.dp))

                    // App name: "Catch Up" — font 20sp, weight Bold, color #1A1A1A, centered
                    Text(
                        text = "Catch Up",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("about_app_name")
                    )

                    // 4dp spacing below
                    Spacer(modifier = Modifier.height(4.dp))

                    // Tagline: "Get the notes. Catch up. Keep learning." — font 13sp, weight Regular, color #8A8A8A, centered, italic style
                    Text(
                        text = "Get the notes. Catch up. Keep learning.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF8A8A8A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("about_app_tagline")
                    )

                    // 8dp spacing below
                    Spacer(modifier = Modifier.height(8.dp))

                    // Version text: "Version 1.0.0" — font 12sp, weight Regular, color #9C9C9C, centered
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF9C9C9C),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("about_app_version")
                    )
                }

                // ======================================
                // SECTION 2 — About the App
                // 28dp spacing below Section 1, 20dp left/right margins
                // ======================================
                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("about_section_story")
                ) {
                    // Section label: "About Catch Up" (13sp, weight Medium, color #8A8A8A)
                    Text(
                        text = "About Catch Up",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_about_catch_up")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Body paragraph text: font 14sp, weight Regular, color #1A1A1A, comfortable line height
                    Text(
                        text = ABOUT_STORY_TEXT,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 22.sp,
                        modifier = Modifier.testTag("about_story_body")
                    )
                }

                // ======================================
                // SECTION 3 — Made By
                // 28dp spacing below Section 2
                // ======================================
                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("about_section_made_by")
                ) {
                    // Section label: "Made By" (13sp, weight Medium, color #8A8A8A)
                    Text(
                        text = "Made By",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_made_by")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Grouped card: background #F7F6FB, corner radius 16dp, containing 2 rows with a 1dp divider (#ECEAF5)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_made_by_card"),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // ROW 1: Ssenyonga Moses
                            MadeByRow(
                                name = "Ssenyonga Moses",
                                role = "Developer",
                                testTag = "made_by_row_moses"
                            )

                            // 1dp divider (#ECEAF5)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFECEAF5))
                            )

                            // ROW 2: Nakintu Sylivia
                            MadeByRow(
                                name = "Nakintu Sylivia",
                                role = "Teacher & Product Lead",
                                testTag = "made_by_row_sylivia"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Made By row without circular avatar placeholders:
 * - Clean vertical text stack: name (14sp, SemiBold, #1A1A1A), 2dp gap, role (12sp, Regular, #8A8A8A)
 * - 16dp padding per row
 */
@Composable
private fun MadeByRow(
    name: String,
    role: String,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag(testTag)
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = role,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF8A8A8A)
        )
    }
}

/**
 * Exact story text specified in the Catch Up specification.
 */
val ABOUT_STORY_TEXT = """
Our Story

I am a teacher.

Every day, I see how much my students want to learn. But I have also seen how easily they can fall behind—not because they don't care, but because there is simply not enough time.

A large part of my lessons can be spent writing notes on the board while students copy them into their books. By the time everyone is finished, valuable teaching time has already passed.

And when this happens lesson after lesson, the work begins to pile up.

Topics are left unfinished. Students have more notes to copy. Teachers have more ground to cover.

I kept asking myself:

Can we do this differently?

What if I could prepare my notes and share them with my students before or after the lesson, so that they could access them directly instead of spending so much of our classroom time copying?

That is where Catch Up began.

Catch Up allows teachers to share their prepared notes directly with their students. Students can open the app, find their class and subject, and access the notes they need.

For me, this isn't about replacing what happens in the classroom.

The teacher still teaches. The students still learn.

It is about giving us more time to do the things that matter most—explaining concepts, asking questions, discussing ideas, doing practical work and making sure students actually understand.

I started Catch Up because I know what it feels like to look at the syllabus and know there is still so much to cover, while valuable lesson time is being spent on copying.

I believe our students deserve more time to learn. And our teachers deserve more time to teach.

That's why We built the idea of Catch Up.

Less time copying. More time learning.
""".trimIndent()

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
