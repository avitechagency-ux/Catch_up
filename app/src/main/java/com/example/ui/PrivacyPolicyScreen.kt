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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

/**
 * Dedicated Privacy Policy Page for Catch Up.
 * Reached by tapping "Privacy Policy" on Profile screens.
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
                                    text = "Cachup Privacy Policy",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Last updated: 11 October 2026",
                                    fontSize = 12.sp,
                                    color = IndigoPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 1. Introduction
                PrivacySectionCard(
                    title = "1. Introduction",
                    content = {
                        Text(
                            text = "Cachup is an educational platform built by Ssenyonga Moses to help teachers share learning materials digitally and help students access notes, revise lessons, and improve their academic experience.\n\nCachup was created to address a common challenge in classrooms: students spending too much lesson time copying notes from the board instead of concentrating on understanding what their teachers are teaching.\n\nWe value the privacy of our users and are committed to handling personal information responsibly. This Privacy Policy explains how Cachup collects, uses, stores, and shares information when students, teachers, and authorised school administrators use the platform.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 2. Information We Collect
                PrivacySectionCard(
                    title = "2. Information We Collect",
                    content = {
                        Text(
                            text = "The information we collect depends on how you use Cachup and which features are available to your account.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Account information", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = IndigoPurple)
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Name and account identification details.")
                        PrivacyBulletPoint("Email address or other login information, where applicable.")
                        PrivacyBulletPoint("Account role, such as student, teacher, or administrator.")
                        PrivacyBulletPoint("School, class, and subject information associated with your account.")

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Educational information", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = IndigoPurple)
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Notes and learning materials uploaded by teachers.")
                        PrivacyBulletPoint("Class and subject assignments.")
                        PrivacyBulletPoint("Information needed to organise and provide access to educational resources.")
                        PrivacyBulletPoint("Other learning-related information you submit through available features.")

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Payment information", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = IndigoPurple)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When you make a payment for a Cachup service, payment processing is facilitated through MarzPay.\n\nDepending on the transaction and the information returned by the payment provider, Cachup may process payment references, transaction status, amounts, timestamps, and account details needed to verify payments and manage access to paid services.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Technical information", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = IndigoPurple)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cachup and its service providers may process authentication records, security logs, diagnostic information, and other technical information necessary to operate and protect the platform.\n\nWe aim to collect only the information reasonably necessary to provide and improve our educational services.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 3. How We Use Your Information
                PrivacySectionCard(
                    title = "3. How We Use Your Information",
                    content = {
                        Text(text = "Cachup may use collected information to:", fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Create and manage user accounts.")
                        PrivacyBulletPoint("Connect students to their appropriate classes, subjects, and teachers.")
                        PrivacyBulletPoint("Allow teachers to upload and share learning materials.")
                        PrivacyBulletPoint("Help students access notes and revise lessons.")
                        PrivacyBulletPoint("Manage school accounts, roles, and permissions.")
                        PrivacyBulletPoint("Verify payments and manage access to paid features.")
                        PrivacyBulletPoint("Protect accounts and detect suspicious activity.")
                        PrivacyBulletPoint("Troubleshoot technical problems and maintain platform reliability.")
                        PrivacyBulletPoint("Respond to support requests and fulfil applicable legal obligations.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We do not use personal information for purposes unrelated to the operation of Cachup unless the appropriate legal basis or permission exists.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 4. How Learning Materials Are Accessed
                PrivacySectionCard(
                    title = "4. How Learning Materials Are Accessed",
                    content = {
                        Text(
                            text = "Cachup is designed to help students access educational materials shared by their teachers.\n\nAccess to notes and other resources may depend on a user's account, class, subject, and permissions.\n\nTeachers should ensure that they have the necessary rights and permissions to upload and share learning materials. Users should not upload confidential student records or other people's sensitive information unless the platform explicitly supports that use and the required permissions have been obtained.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 5. Database, Hosting, and Data Storage
                PrivacySectionCard(
                    title = "5. Database, Hosting, and Data Storage",
                    content = {
                        Text(
                            text = "Cachup uses third-party technology services to support its operation, including Supabase for relevant backend services such as database and authentication functionality.\n\nInformation processed through these services may be stored on infrastructure operated by the relevant providers. Depending on the features in use, service providers may process information to authenticate accounts, store records, enforce permissions, maintain security, and deliver platform functionality.\n\nWe apply appropriate access restrictions and security measures intended to protect stored information. However, no internet-based system can guarantee absolute security.\n\nUsers should protect their account credentials and report suspected unauthorised access.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 6. Payments Through MarzPay
                PrivacySectionCard(
                    title = "6. Payments Through MarzPay",
                    content = {
                        Text(
                            text = "Cachup uses MarzPay to facilitate supported mobile money payments, including MTN Mobile Money and Airtel Money transactions in Uganda, where available.\n\nWhen a payment is initiated, MarzPay and the relevant mobile money provider may process the information required to complete or verify the transaction.\n\nCachup may receive or store transaction references, payment confirmations, amounts, timestamps, and related information needed to determine whether a payment was successful and whether the relevant service should be made available.\n\nPayment processing and verification may involve secure backend operations, including server-side functions.\n\nUsers should consult MarzPay's applicable privacy terms for information about how MarzPay independently handles personal information. Cachup does not intend to collect or store mobile money PINs.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 7. How We Share Information
                PrivacySectionCard(
                    title = "7. How We Share Information",
                    content = {
                        Text(text = "Cachup does not sell users' personal information.\n\nInformation may be disclosed where necessary to:", fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Authorised school users: Provide students, teachers, and administrators with the information and learning resources their roles permit them to access.")
                        PrivacyBulletPoint("Technology providers: Operate the platform through services such as Supabase and associated hosting or infrastructure providers.")
                        PrivacyBulletPoint("Payment providers: Process and verify transactions through MarzPay and relevant mobile money services.")
                        PrivacyBulletPoint("Legal authorities: Comply with applicable laws, lawful requests, or legal obligations.")
                        PrivacyBulletPoint("Security and protection: Investigate fraud, address security incidents, or protect users and the platform.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We aim to limit disclosures to information reasonably necessary for the relevant purpose.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 8. Children's Privacy
                PrivacySectionCard(
                    title = "8. Children's Privacy",
                    content = {
                        Text(
                            text = "Cachup recognises that many of its student users may be under the age of 18 and that children's personal information requires particular care.\n\nCachup is designed to support educational activities. We aim to limit personal information collection to what is reasonably necessary for the platform's educational functions.\n\nSchools, parents, guardians, and other responsible parties should ensure that children's use of the platform is appropriate and that any consent, authorisation, or other legal requirements are met.\n\nWe do not intend to use children's personal information for unrelated advertising or profiling.\n\nWhere legally required, appropriate consent or other valid legal grounds must be established before processing children's personal information.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 9. Data Security
                PrivacySectionCard(
                    title = "9. Data Security",
                    content = {
                        Text(
                            text = "Cachup takes reasonable steps to protect personal information against unauthorised access, disclosure, alteration, loss, or destruction.\n\nThese measures may include account authentication, access restrictions, appropriate database permissions, and secure backend operations.\n\nUsers must also take reasonable steps to protect their accounts.\n\nAlthough we work to protect personal information, we cannot guarantee that any system or internet transmission will always be completely secure.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 10. Data Retention and Deletion
                PrivacySectionCard(
                    title = "10. Data Retention and Deletion",
                    content = {
                        Text(
                            text = "Cachup retains personal information for as long as reasonably necessary to provide its services, maintain school and user accounts, manage educational resources, verify transactions, address disputes, and comply with applicable legal obligations.\n\nWhen information is no longer needed, it may be deleted or anonymised where appropriate, subject to legitimate retention requirements and technical limitations such as temporary backup retention.\n\nUsers may request access to, correction of, or deletion of their personal information, subject to applicable law.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 11. Your Privacy Rights
                PrivacySectionCard(
                    title = "11. Your Privacy Rights",
                    content = {
                        Text(text = "Depending on applicable Ugandan law and other relevant legal requirements, you may have rights relating to your personal information, including the right to:", fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Request access to your personal information.")
                        PrivacyBulletPoint("Request correction of inaccurate or incomplete information.")
                        PrivacyBulletPoint("Request deletion where legally applicable.")
                        PrivacyBulletPoint("Object to or seek restriction of certain processing where applicable.")
                        PrivacyBulletPoint("Withdraw consent where processing relies on consent.")
                        PrivacyBulletPoint("Raise a complaint with the relevant data protection authority.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To exercise these rights, contact Cachup through the official support contact provided in the application.\n\nSome requests may require identity verification or involvement from an authorised school representative.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 12. Third-Party Services
                PrivacySectionCard(
                    title = "12. Third-Party Services",
                    content = {
                        Text(text = "Cachup relies on third-party services to provide certain platform functions. These services may process information according to their own privacy policies, terms, and contractual arrangements.\n\nRelevant providers include:", fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyBulletPoint("Supabase — backend services, including database and authentication functionality.")
                        PrivacyBulletPoint("MarzPay — supported payment processing and transaction verification.")
                        PrivacyBulletPoint("Relevant mobile money providers — processing mobile money transactions through the payment service.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The specific information processed depends on the features used and the requirements of each service.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 13. International Data Processing
                PrivacySectionCard(
                    title = "13. International Data Processing",
                    content = {
                        Text(
                            text = "Some third-party service providers may process or store information outside Uganda.\n\nWhere personal information is transferred across borders, Cachup aims to ensure that the transfer and processing comply with applicable data protection requirements.\n\nUsers should be aware that data protection rules may differ between jurisdictions.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 14. Changes to This Privacy Policy
                PrivacySectionCard(
                    title = "14. Changes to This Privacy Policy",
                    content = {
                        Text(
                            text = "We may update this Privacy Policy when Cachup introduces new features, changes its service providers, improves its security practices, or responds to legal requirements.\n\nMaterial changes will be communicated through an appropriate channel where required.\n\nThe latest version will be made available within the Cachup application.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF333333)
                        )
                    }
                )

                // 15. Contact and Privacy Requests
                PrivacySectionCard(
                    title = "15. Contact and Privacy Requests",
                    content = {
                        Text(
                            text = "For questions about this Privacy Policy, requests relating to personal information, or concerns about data handling, contact:\n\nPlatform: Cachup\nDeveloper: Ssenyonga Moses\nContact: Use the official support contact provided within the Cachup application.\n\nWe are committed to building technology that supports education while respecting the privacy, dignity, and security of students and teachers.\n\nCachup — Learn today. Catch up tomorrow. Move forward always.",
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
                            contentDescription = "Cachup Logo",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cachup",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Learn today. Catch up tomorrow. Move forward always.",
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
