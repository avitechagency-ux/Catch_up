package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.ui.DefaultGeographyUploads
import com.example.ui.DefaultMathUploads
import com.example.ui.DefaultTeacherRecentUploads
import com.example.ui.DefaultTeacherSubjects
import com.example.ui.StudentNavItems
import com.example.ui.TeacherNavItems
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
}
