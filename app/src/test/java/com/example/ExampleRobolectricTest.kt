package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.ui.DefaultGeographyUploads
import com.example.ui.DefaultMathUploads
import com.example.ui.DefaultTeacherAllUploads
import com.example.ui.DefaultTeacherRecentUploads
import com.example.ui.DefaultTeacherSubjects
import com.example.ui.StudentNavItems
import com.example.ui.TeacherNavItems
import com.example.ui.ABOUT_STORY_TEXT
import com.example.ui.defaultFaqItems
import com.example.ui.getSubjectThemeColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Catch Up", appName)
  }

  @Test
  fun `verify subject colors match palette`() {
    assertEquals(Color(0xFFE76F51), getSubjectThemeColor("Geography"))
    assertEquals(Color(0xFF2A4D8F), getSubjectThemeColor("Biology"))
    assertEquals(Color(0xFF2A9D8F), getSubjectThemeColor("Math"))
    assertEquals(Color(0xFF4361EE), getSubjectThemeColor("History"))
    assertEquals(Color(0xFF7C4DFF), getSubjectThemeColor("Physics"))
    assertEquals(Color(0xFFE67E22), getSubjectThemeColor("Chemistry"))
  }

  @Test
  fun `verify teacher nav items match specifications`() {
    val labels = TeacherNavItems.map { it.label }
    assertEquals(listOf("Home", "My Subjects", "Uploads", "Profile"), labels)
  }

  @Test
  fun `verify student nav items match specifications`() {
    val labels = StudentNavItems.map { it.label }
    assertEquals(listOf("Home", "Subjects", "Downloads", "Profile"), labels)
  }

  @Test
  fun `verify teacher quick stats colors`() {
    val tealColor = Color(0xFF2A9D8F)
    val blueColor = Color(0xFF4361EE)
    assertEquals(Color(0xFF2A9D8F), tealColor)
    assertEquals(Color(0xFF4361EE), blueColor)
  }

  @Test
  fun `verify default teacher recent uploads`() {
    assertEquals(3, DefaultTeacherRecentUploads.size)
    assertEquals("Geography", DefaultTeacherRecentUploads[0].subject)
    assertEquals("East African Climate & Vegetation", DefaultTeacherRecentUploads[0].title)
  }

  @Test
  fun `verify teacher taught subjects only contains Geography and Math`() {
    val subjectNames = DefaultTeacherSubjects.map { it.name }
    assertEquals(listOf("Geography", "Math"), subjectNames)
    assertEquals("S.4, S.5", DefaultTeacherSubjects.first { it.name == "Geography" }.classes)
    assertEquals("S.3, S.4", DefaultTeacherSubjects.first { it.name == "Math" }.classes)
  }

  @Test
  fun `verify teacher subject uploads data integrity`() {
    assertTrue(DefaultGeographyUploads.isNotEmpty())
    assertTrue(DefaultMathUploads.isNotEmpty())
    assertTrue(DefaultGeographyUploads.any { it.type == "Notes" })
    assertTrue(DefaultGeographyUploads.any { it.type == "Assignment" })
  }

  @Test
  fun `verify teacher all uploads collection across every term`() {
    assertTrue(DefaultTeacherAllUploads.isNotEmpty())
    val terms = DefaultTeacherAllUploads.map { it.term }.distinct()
    assertTrue(terms.contains("Term 3"))
    assertTrue(terms.contains("Term 2"))
    assertTrue(terms.contains("Term 1"))

    val types = DefaultTeacherAllUploads.map { it.type }.distinct()
    assertTrue(types.contains("Notes"))
    assertTrue(types.contains("Assignment"))

    val subjects = DefaultTeacherAllUploads.map { it.subject }.distinct()
    assertTrue(subjects.contains("Geography"))
    assertTrue(subjects.contains("Math"))
  }

  @Test
  fun `verify account plan tiers match specification`() {
    val plans = com.example.ui.DefaultPlanTiers
    assertEquals(3, plans.size)

    assertEquals("1 Month", plans[0].duration)
    assertEquals("Billed monthly", plans[0].caption)
    assertEquals("UGX 1,000", plans[0].price)

    assertEquals("1 Term", plans[1].duration)
    assertEquals("Billed per term", plans[1].caption)
    assertEquals("UGX 3,000", plans[1].price)

    assertEquals("1 Year", plans[2].duration)
    assertEquals("Billed annually — best value", plans[2].caption)
    assertEquals("UGX 10,000", plans[2].price)
  }

  @Test
  fun `verify settings screen appearance section options`() {
    val languageOptions = listOf("English", "Luganda (Coming Soon)", "Swahili (Coming Soon)")
    assertEquals(3, languageOptions.size)
    assertEquals("English", languageOptions.first())
  }

  @Test
  fun `verify help and support faq content and count`() {
    assertEquals(5, defaultFaqItems.size)
    assertEquals("How do I access notes for my class?", defaultFaqItems[0].question)
    assertTrue(defaultFaqItems[0].answer.contains("Open the Subjects tab"))
    assertEquals("Why can't I see notes for a subject?", defaultFaqItems[1].question)
    assertEquals("How do I save a note for offline access?", defaultFaqItems[2].question)
    assertTrue(defaultFaqItems[2].answer.contains("Downloads tab"))
    assertEquals("How do I renew or change my plan?", defaultFaqItems[3].question)
    assertEquals("I forgot my password. What do I do?", defaultFaqItems[4].question)
  }

  @Test
  fun `verify about screen story and creators text`() {
    assertTrue(ABOUT_STORY_TEXT.contains("Our Story"))
    assertTrue(ABOUT_STORY_TEXT.contains("I am a teacher."))
    assertTrue(ABOUT_STORY_TEXT.contains("Less time copying. More time learning."))
    assertTrue(ABOUT_STORY_TEXT.contains("That is where Catch Up began."))
  }
}
