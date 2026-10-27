package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ArticleSection
import com.example.data.VisualAspectRatio
import com.example.data.VisualLearningArticle
import com.example.data.VisualLearningItem
import com.example.data.VisualLearningRepository
import com.example.data.VisualSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VisualLearningTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        VisualLearningRepository.initialize(context)
    }

    @Test
    fun `verify default visual learning items are approved and possess valid aspect ratios`() {
        val visuals = VisualLearningRepository.DefaultApprovedVisuals
        assertTrue(visuals.isNotEmpty())
        assertTrue(visuals.size >= 4)

        // All default items must be Approved for students
        visuals.forEach { item ->
            assertTrue(item.isApproved)
            assertEquals("Approved", item.status)
            assertTrue(item.title.isNotBlank())
            assertTrue(item.subject.isNotBlank())
            assertNotNull(item.article)
            assertTrue(item.article.title.isNotBlank())
            assertTrue(item.article.introduction.isNotBlank())
            assertTrue(item.article.sections.isNotEmpty())
        }

        // Verify distinct aspect ratios are represented
        val aspectRatios = visuals.map { it.aspectRatio }.toSet()
        assertTrue(aspectRatios.contains(VisualAspectRatio.RATIO_3_4))
        assertTrue(aspectRatios.contains(VisualAspectRatio.RATIO_16_9))
        assertTrue(aspectRatios.contains(VisualAspectRatio.RATIO_1_1))
    }

    @Test
    fun `verify aspect ratio float calculations match specifications`() {
        assertEquals(1.0f, VisualAspectRatio.RATIO_1_1.ratio, 0.001f)
        assertEquals(0.8f, VisualAspectRatio.RATIO_4_5.ratio, 0.001f)
        assertEquals(0.75f, VisualAspectRatio.RATIO_3_4.ratio, 0.001f)
        assertEquals(16f / 9f, VisualAspectRatio.RATIO_16_9.ratio, 0.001f)
    }

    @Test
    fun `verify subject filtering in discovery feed`() {
        // "All" filter returns all approved visuals
        val allVisuals = VisualLearningRepository.getApprovedVisuals("All")
        val nullFilterVisuals = VisualLearningRepository.getApprovedVisuals(null)
        assertEquals(allVisuals.size, nullFilterVisuals.size)
        assertTrue(allVisuals.size >= 4)

        // Biology filter returns only Biology visuals
        val bioVisuals = VisualLearningRepository.getApprovedVisuals("Biology")
        assertTrue(bioVisuals.isNotEmpty())
        assertTrue(bioVisuals.all { it.subject.equals("Biology", ignoreCase = true) })

        // Physics filter returns only Physics visuals
        val physicsVisuals = VisualLearningRepository.getApprovedVisuals("Physics")
        assertTrue(physicsVisuals.isNotEmpty())
        assertTrue(physicsVisuals.all { it.subject.equals("Physics", ignoreCase = true) })

        // Chemistry filter returns only Chemistry visuals
        val chemVisuals = VisualLearningRepository.getApprovedVisuals("Chemistry")
        assertTrue(chemVisuals.isNotEmpty())
        assertTrue(chemVisuals.all { it.subject.equals("Chemistry", ignoreCase = true) })

        // Non-existent subject returns empty list
        val nonExistent = VisualLearningRepository.getApprovedVisuals("NonExistentSubject")
        assertTrue(nonExistent.isEmpty())
    }

    @Test
    fun `verify teacher uploaded visual is unapproved and pending until admin approves`() {
        val initialApprovedCount = VisualLearningRepository.getApprovedVisuals().size

        // Teacher submits an educational visual
        val sampleArticle = VisualLearningArticle(
            title = "Enzyme Kinetics and Optimum pH",
            introduction = "Enzymes are biological catalysts that accelerate metabolic reactions without being consumed.",
            sections = listOf(
                ArticleSection(
                    heading = "Substrate Specificity & Active Site",
                    body = "The induced-fit model describes how substrate binding alters the conformational shape of the enzyme active site.",
                    keyPoints = listOf("Temperature above 45°C causes thermal denaturation.")
                )
            ),
            keyFacts = listOf("Optimal temperature for human enzymes is ~37°C.")
        )

        val submittedItem = VisualLearningRepository.submitTeacherVisual(
            context = context,
            title = "Enzyme Denaturation Curve",
            subject = "Biology",
            targetClass = "S.3",
            author = "Tr. Ssenyonga",
            imageUrl = "https://example.com/enzyme_curve.png",
            aspectRatio = VisualAspectRatio.RATIO_4_5,
            article = sampleArticle
        )

        // REQUIREMENT: Must start as Pending and NOT approved
        assertEquals("Pending", submittedItem.status)
        assertFalse(submittedItem.isApproved)
        assertEquals(VisualSourceType.TEACHER_UPLOAD, submittedItem.sourceType)

        // Student feed must NOT contain this pending item
        val studentApproved = VisualLearningRepository.getApprovedVisuals()
        assertEquals(initialApprovedCount, studentApproved.size)
        assertFalse(studentApproved.any { it.id == submittedItem.id })

        // Admin checks pending items
        val pendingList = VisualLearningRepository.getPendingTeacherUploads()
        assertTrue(pendingList.any { it.id == submittedItem.id })

        // Admin approves the item
        val approvalResult = VisualLearningRepository.approveVisualItem(context, submittedItem.id)
        assertTrue(approvalResult)

        // Now the approved item appears in the student discovery feed
        val updatedStudentApproved = VisualLearningRepository.getApprovedVisuals()
        assertEquals(initialApprovedCount + 1, updatedStudentApproved.size)
        val approvedInFeed = updatedStudentApproved.firstOrNull { it.id == submittedItem.id }
        assertNotNull(approvedInFeed)
        assertEquals("Approved", approvedInFeed?.status)
        assertTrue(approvedInFeed?.isApproved == true)
    }

    @Test
    fun `verify admin rejection keeps visual hidden from student discovery feed`() {
        val sampleArticle = VisualLearningArticle(
            title = "Incomplete Draft Article",
            introduction = "Draft introduction text.",
            sections = emptyList()
        )

        val submittedItem = VisualLearningRepository.submitTeacherVisual(
            context = context,
            title = "Draft Physics Concept",
            subject = "Physics",
            targetClass = "S.4",
            author = "Tr. Kato",
            imageUrl = null,
            aspectRatio = VisualAspectRatio.RATIO_1_1,
            article = sampleArticle
        )

        // Admin rejects the draft
        val rejected = VisualLearningRepository.rejectVisualItem(context, submittedItem.id)
        assertTrue(rejected)

        // Verified that students do NOT see the rejected item
        val studentFeed = VisualLearningRepository.getApprovedVisuals()
        assertFalse(studentFeed.any { it.id == submittedItem.id })
    }

    @Test
    fun `verify connected educational article content structure`() {
        val bioItem = VisualLearningRepository.DefaultApprovedVisuals.first { it.id == "vis-bio-01" }
        val article = bioItem.article

        assertTrue(article.title.contains("Cell Ultrastructure"))
        assertTrue(article.introduction.contains("Cells are the fundamental structural"))
        assertTrue(article.sections.any { it.heading.contains("Mitochondria") })
        assertTrue(article.keyFacts.isNotEmpty())
        assertNotNull(article.curriculumTip)
        assertTrue(article.curriculumTip!!.contains("UNEB Exam Tip"))
    }

    @Test
    fun `verify Visual Learning screen is registered in app navigation`() {
        val screens = AppScreen.values().map { it.name }
        assertTrue(screens.contains("VISUAL_LEARNING"))
        assertTrue(screens.contains("LEARNING_HUB"))
    }

    @Test
    fun `verify Visual Learning screen is authorized for both Teacher and Student roles`() {
        assertTrue(
            "Teacher MUST be authorized for VISUAL_LEARNING screen",
            com.example.data.AccountSecurityVault.isScreenAuthorized(
                AppScreen.VISUAL_LEARNING,
                com.example.data.AccountSecurityVault.UserRole.TEACHER,
                isLoggedIn = true
            )
        )
        assertTrue(
            "Student MUST be authorized for VISUAL_LEARNING screen",
            com.example.data.AccountSecurityVault.isScreenAuthorized(
                AppScreen.VISUAL_LEARNING,
                com.example.data.AccountSecurityVault.UserRole.STUDENT,
                isLoggedIn = true
            )
        )
        assertFalse(
            "Unauthenticated user must NOT be authorized for VISUAL_LEARNING screen",
            com.example.data.AccountSecurityVault.isScreenAuthorized(
                AppScreen.VISUAL_LEARNING,
                com.example.data.AccountSecurityVault.UserRole.UNAUTHENTICATED,
                isLoggedIn = false
            )
        )
    }
}
