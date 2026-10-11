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
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(color = IndigoPurple, shape = CircleShape)
                            .testTag("about_app_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Cachup Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Cachup",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("about_app_name")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Helping every student keep up, learn better, and move forward.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF8A8A8A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("about_app_tagline")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Version 2.4.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF9C9C9C),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("about_app_version")
                    )
                }

                // ======================================
                // SECTION 2 — About Content
                // ======================================
                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("about_section_story")
                ) {
                    Text(
                        text = "About Cachup",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_about_catch_up")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                // ======================================
                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("about_section_made_by")
                ) {
                    Text(
                        text = "Built By",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_made_by")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_made_by_card"),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MadeByRow(
                                name = "Ssenyonga Moses",
                                role = "Developer & Creator",
                                testTag = "made_by_row_moses"
                            )
                        }
                    }
                }
            }
        }
    }
}

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
 * Updated About Cachup story and overview text.
 */
val ABOUT_STORY_TEXT = """
Helping every student keep up, learn better, and move forward.

Our Story

Cachup was created to address a common challenge in schools: students can spend so much time copying notes from the chalkboard that they struggle to keep up with lessons. When students miss school, fall behind while copying notes, or fail to complete their notes in time, catching up with the rest of the class becomes difficult.

Cachup was built to make learning materials more accessible and help students focus on what matters most — understanding what they learn.

Through Cachup, teachers can share learning materials with their students in one place, allowing students to access notes for their subjects without having to rely entirely on copying everything from the board. This gives teachers more time to explain concepts, guide discussions, and complete their lessons while giving students a better opportunity to learn at their own pace.

Our Mission

Our mission is to make studying easier, more organised, and more accessible by connecting teachers and students through a simple learning platform.

We believe that every student deserves an opportunity to learn effectively, access their study materials, and improve their academic performance.

How Cachup Helps Students

• Access notes easily: Find and read learning materials shared by your teachers.
• Catch up on missed lessons: Review available notes when you miss a class or need to revisit a topic.
• Revise more effectively: Use subject notes to prepare for tests, examinations, and classroom assessments.
• Stay organised: Keep learning materials accessible in one place instead of struggling with scattered notes.
• Learn at your own pace: Revisit topics whenever you need more time to understand them.
• Make better use of lesson time: Spend more time understanding concepts and less time worrying about copying notes.

Empowering Teachers

Cachup helps teachers share notes with their classes more conveniently. Instead of spending too much lesson time writing extensive notes on the board, teachers can make learning materials available digitally and dedicate more time to explaining difficult concepts, answering questions, and supporting students.

Our Vision

We envision a learning environment where students can access the materials they need, teachers can teach more effectively, and falling behind does not have to mean staying behind.

Cachup is more than a place to read notes. It is a step towards making education more accessible, organised, and supportive for every learner.

Built By

Ssenyonga Moses

Cachup was designed and developed by Ssenyonga Moses with a vision of using technology to solve real challenges in education.

The idea grew from recognising a problem experienced in everyday classrooms and finding a practical way to help teachers and students overcome it. Cachup represents a commitment to building useful technology that makes a meaningful difference in people's lives, starting with education.

Cachup — Learn today. Catch up tomorrow. Move forward always.
""".trimIndent()

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
