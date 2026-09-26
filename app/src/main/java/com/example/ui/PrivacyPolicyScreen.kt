package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * Dedicated Privacy Policy Page for Catch Up.
 * Reached by tapping "Privacy Policy" on Profile screens (No popups).
 */
@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen"),
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
            // Signature deep indigo-purple #4B3F8F card header
            // ======================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
                    .testTag("privacy_header_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("privacy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Privacy Policy",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("privacy_title")
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
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag("privacy_scrollable_content"),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Intro Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SoftLavenderGrey,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = IndigoPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Catch Up Privacy Policy",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Last Updated: September 23, 2026",
                                    fontSize = 12.sp,
                                    color = IndigoPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Catch Up (“Catch Up,” “we,” “our,” or “us”) is an educational platform designed to help schools and teachers share learning notes with students digitally.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF2D2D2D)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "This Privacy Policy explains what information we collect, how we use it, how we protect it, and the choices available to users of the Catch Up application.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF2D2D2D)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "By using Catch Up, you agree to the practices described in this Privacy Policy.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                    }
                }

                // Section 1
                PrivacySectionCard(
                    title = "1. Who This Policy Applies To",
                    content = {
                        Text(
                            text = "This Privacy Policy applies to users of the Catch Up mobile application, including:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Students")
                        PrivacyBulletPoint("Teachers")
                        PrivacyBulletPoint("School administrators")
                        PrivacyBulletPoint("Other authorized school users")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Catch Up is intended to be used within participating schools. Accounts may be created or authorized by the school or through the registration process provided by Catch Up.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 2
                PrivacySectionCard(
                    title = "2. Information We Collect",
                    content = {
                        Text(
                            text = "Depending on how Catch Up is configured by your school, we may collect the following information.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Account Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "When an account is created, we may collect:",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Full name")
                        PrivacyBulletPoint("Email address, where applicable")
                        PrivacyBulletPoint("Phone number, where applicable")
                        PrivacyBulletPoint("Password or authentication information")
                        PrivacyBulletPoint("User role, such as student or teacher")
                        PrivacyBulletPoint("School")
                        PrivacyBulletPoint("Class or year group")
                        PrivacyBulletPoint("Subjects or teacher assignments")

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Learning and App Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "We may collect information related to your use of Catch Up, such as:",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Notes and documents accessed")
                        PrivacyBulletPoint("Subjects and classes accessed")
                        PrivacyBulletPoint("Recently viewed notes")
                        PrivacyBulletPoint("Saved notes, if the feature is enabled")
                        PrivacyBulletPoint("Notifications delivered through the app")
                        PrivacyBulletPoint("Basic app activity needed to provide and improve the service")

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Teacher-Uploaded Content",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "Teachers may upload educational materials such as:",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("PDF notes")
                        PrivacyBulletPoint("Class materials")
                        PrivacyBulletPoint("Diagrams")
                        PrivacyBulletPoint("Tables")
                        PrivacyBulletPoint("Images")
                        PrivacyBulletPoint("Other educational documents")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "These materials are stored so that authorized students can access them.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Payment Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "If Catch Up provides paid subscriptions or access, payment transactions may be processed through a third-party payment provider.\n\nCatch Up does not intend to store users' mobile money PINs, card PINs, or other sensitive payment credentials.\n\nPayment providers may collect and process information necessary to complete a transaction according to their own privacy policies and terms.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 3
                PrivacySectionCard(
                    title = "3. How We Use Information",
                    content = {
                        Text(
                            text = "We use collected information to:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Create and manage user accounts")
                        PrivacyBulletPoint("Verify users and their school access")
                        PrivacyBulletPoint("Provide access to appropriate classes and subjects")
                        PrivacyBulletPoint("Deliver educational notes and materials")
                        PrivacyBulletPoint("Allow teachers to share notes with their students")
                        PrivacyBulletPoint("Process subscriptions and payments")
                        PrivacyBulletPoint("Send important notifications")
                        PrivacyBulletPoint("Maintain the security of the platform")
                        PrivacyBulletPoint("Prevent unauthorized access or misuse")
                        PrivacyBulletPoint("Troubleshoot technical problems")
                        PrivacyBulletPoint("Improve the performance and functionality of Catch Up")
                        PrivacyBulletPoint("Maintain records necessary to operate the service")

                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = IndigoPurple.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "We do not sell users' personal information.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPurple,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                )

                // Section 4
                PrivacySectionCard(
                    title = "4. School and Classroom Access",
                    content = {
                        Text(
                            text = "Catch Up is designed so that users only receive access appropriate to their role and school.\n\nFor example:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Students may access notes made available to their school, class, or subjects.")
                        PrivacyBulletPoint("Teachers may upload and manage materials for classes or subjects assigned to them.")
                        PrivacyBulletPoint("School administrators may have additional administrative access where enabled.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Schools are responsible for ensuring that information they provide to Catch Up is appropriate for the intended educational use of the platform.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 5
                PrivacySectionCard(
                    title = "5. Educational Materials",
                    content = {
                        Text(
                            text = "Notes uploaded by teachers remain educational materials provided by the relevant teacher or school.\n\nCatch Up provides the technology used to store and deliver these materials and does not claim ownership of teachers' original educational content.\n\nTeachers and schools should ensure that uploaded materials do not unlawfully contain private information or copyrighted material that they do not have permission to share.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 6
                PrivacySectionCard(
                    title = "6. How We Share Information",
                    content = {
                        Text(
                            text = "We may share or provide access to information when necessary to operate Catch Up, including with:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Participating Schools",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "Schools may receive access to information necessary to administer their Catch Up accounts and educational services.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Service Providers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "We may use trusted third-party service providers for services such as:",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Database hosting")
                        PrivacyBulletPoint("Authentication")
                        PrivacyBulletPoint("File storage")
                        PrivacyBulletPoint("Cloud infrastructure")
                        PrivacyBulletPoint("Payment processing")
                        PrivacyBulletPoint("Notifications")
                        PrivacyBulletPoint("Security and technical operations")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "These providers only receive information necessary to provide their services.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Legal Requirements",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                        Text(
                            text = "We may disclose information where required by applicable law, regulation, court order, or lawful government request.\n\nWe may also disclose information where reasonably necessary to protect the security, rights, or property of Catch Up, our users, schools, or others.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 7
                PrivacySectionCard(
                    title = "7. Third-Party Services",
                    content = {
                        Text(
                            text = "Catch Up may use third-party technology providers to operate parts of the platform.\n\nThese may include services for:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Authentication")
                        PrivacyBulletPoint("Database management")
                        PrivacyBulletPoint("Cloud hosting")
                        PrivacyBulletPoint("File storage")
                        PrivacyBulletPoint("Payment processing")
                        PrivacyBulletPoint("Analytics or technical monitoring")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Third-party providers may process information according to their own privacy policies and terms.\n\nWe encourage users to review the privacy policies of third-party services where appropriate.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 8
                PrivacySectionCard(
                    title = "8. Data Security",
                    content = {
                        Text(
                            text = "We take reasonable technical and organizational measures to protect information stored and processed through Catch Up.\n\nThese measures may include:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Secure authentication")
                        PrivacyBulletPoint("Access controls")
                        PrivacyBulletPoint("Database security rules")
                        PrivacyBulletPoint("Encrypted connections")
                        PrivacyBulletPoint("Restricted administrative access")
                        PrivacyBulletPoint("Server-side protection of sensitive credentials")
                        PrivacyBulletPoint("Security monitoring and maintenance")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "However, no internet-based service can guarantee absolute security.\n\nUsers should keep their passwords and account credentials confidential and should notify the school or Catch Up if they believe their account has been accessed without authorization.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 9
                PrivacySectionCard(
                    title = "9. Children's and Students' Privacy",
                    content = {
                        Text(
                            text = "Catch Up may be used by secondary-school students, including students who may be under the age of 18.\n\nWe aim to collect only information reasonably necessary to provide the educational service.\n\nWhere required, schools, parents, guardians, or other authorized adults may be involved in the creation or management of student accounts.\n\nWe do not intentionally use student information for targeted advertising.\n\nIf a parent, guardian, student, teacher, or school believes that we have collected information that is not necessary for the service, they may contact us using the details below.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 10
                PrivacySectionCard(
                    title = "10. Data Retention",
                    content = {
                        Text(
                            text = "We retain personal information for as long as reasonably necessary to provide Catch Up and maintain legitimate school, account, security, payment, and operational records.\n\nWhen information is no longer required, we may delete, anonymize, or securely dispose of it, subject to applicable legal or operational requirements.\n\nSchools may request information about their users or request appropriate account changes through the Catch Up support process.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 11
                PrivacySectionCard(
                    title = "11. Your Privacy Rights",
                    content = {
                        Text(
                            text = "Depending on applicable law, users may have rights relating to their personal information, including the ability to:",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Request access to personal information we hold about them")
                        PrivacyBulletPoint("Request correction of inaccurate information")
                        PrivacyBulletPoint("Request deletion of information where appropriate")
                        PrivacyBulletPoint("Ask questions about how their information is used")
                        PrivacyBulletPoint("Withdraw consent where processing is based on consent, subject to applicable limitations")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Some information may need to be retained where required by law or necessary to provide the service.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 12
                PrivacySectionCard(
                    title = "12. Account Deletion",
                    content = {
                        Text(
                            text = "Users or authorized school representatives may request deletion of an account or personal information by contacting Catch Up.\n\nWhen an account is deleted, information may be removed or anonymized where appropriate.\n\nCertain records, including transaction or security records, may need to be retained for legitimate legal, accounting, fraud-prevention, or operational purposes.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 13
                PrivacySectionCard(
                    title = "13. Cookies and Similar Technologies",
                    content = {
                        Text(
                            text = "The Catch Up mobile application may use technologies necessary to maintain login sessions, security, preferences, and basic functionality.\n\nIf we introduce analytics or other tracking technologies, this Privacy Policy may be updated to explain their use.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Section 14
                PrivacySectionCard(
                    title = "14. Changes to This Privacy Policy",
                    content = {
                        Text(
                            text = "We may update this Privacy Policy when Catch Up introduces new features, changes how information is processed, or when legal or regulatory requirements change.\n\nWhen we make significant changes, we may provide notice through the application, website, or other appropriate communication channels.\n\nThe “Last Updated” date at the beginning of this policy indicates when the policy was most recently updated.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // Footer Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(color = IndigoPurple, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Catch Up Logo",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Catch Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Less time copying. More time learning.",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PrivacySectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F8FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun PrivacyBulletPoint(text: String) {
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
            color = IndigoPurple,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFF333333)
        )
    }
}
