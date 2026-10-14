package com.example

import com.example.ui.ConnectedStudent
import com.example.data.LiveClassDto
import com.example.data.doesContentTargetStudent
import com.example.data.normalizeStream
import com.example.data.parseTargetStreams
import com.example.ui.SubjectUploadItem
import com.example.ui.TopicItem
import com.example.ui.normalizeSecondaryClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AcademicGroupArchitectureTest {

    @Test
    fun `test stream normalization removes whitespace and converts to uppercase`() {
        assertEquals("NORTH", normalizeStream("north"))
        assertEquals("NORTH", normalizeStream("North"))
        assertEquals("NORTH", normalizeStream(" NORTH "))
        assertEquals("SOUTH", normalizeStream("south"))
        assertEquals("EAST", normalizeStream("East"))
    }

    @Test
    fun `test class normalization formats class names consistently`() {
        assertEquals("S.4", normalizeSecondaryClass("S4"))
        assertEquals("S.4", normalizeSecondaryClass("S.4"))
        assertEquals("S.4", normalizeSecondaryClass("Senior 4"))
        assertEquals("S.5", normalizeSecondaryClass("s5"))
    }

    @Test
    fun `test parseTargetStreams handles ALL and specific stream lists`() {
        assertEquals(setOf("ALL"), parseTargetStreams("ALL"))
        assertEquals(setOf("ALL"), parseTargetStreams("All Streams"))
        assertEquals(setOf("NORTH", "SOUTH"), parseTargetStreams("NORTH,SOUTH"))
        assertEquals(setOf("NORTH", "EAST"), parseTargetStreams("NORTH + EAST"))
    }

    @Test
    fun `test exact acceptance test scenario for student filtering and content targeting`() {
        // 1. Create students
        val john = ConnectedStudent(
            id = "std_john",
            studentName = "John Doe",
            studentClass = "S.4",
            subjectName = "Physics",
            teacherIdCode = "TR1234",
            connectedDate = "Oct 07, 2026",
            stream = normalizeStream("NORTH")
        )
        val mary = ConnectedStudent(
            id = "std_mary",
            studentName = "Mary Nam",
            studentClass = "S.4",
            subjectName = "Physics",
            teacherIdCode = "TR1234",
            connectedDate = "Oct 07, 2026",
            stream = normalizeStream("NORTH")
        )
        val peter = ConnectedStudent(
            id = "std_peter",
            studentName = "Peter Kato",
            studentClass = "S.4",
            subjectName = "Physics",
            teacherIdCode = "TR1234",
            connectedDate = "Oct 07, 2026",
            stream = normalizeStream("SOUTH")
        )
        val sarah = ConnectedStudent(
            id = "std_sarah",
            studentName = "Sarah Opio",
            studentClass = "S.5",
            subjectName = "Physics",
            teacherIdCode = "TR1234",
            connectedDate = "Oct 07, 2026",
            stream = normalizeStream("NORTH")
        )

        val connectedStudentsList = listOf(john, mary, peter, sarah)

        // 2. Teacher Students Page Filtering Tests
        val s4NorthStudents = connectedStudentsList.filter {
            normalizeSecondaryClass(it.studentClass) == normalizeSecondaryClass("S4") &&
                normalizeStream(it.stream) == normalizeStream("NORTH")
        }
        assertEquals(2, s4NorthStudents.size)
        assertTrue(s4NorthStudents.any { it.studentName == "John Doe" })
        assertTrue(s4NorthStudents.any { it.studentName == "Mary Nam" })

        val s4SouthStudents = connectedStudentsList.filter {
            normalizeSecondaryClass(it.studentClass) == normalizeSecondaryClass("S4") &&
                normalizeStream(it.stream) == normalizeStream("SOUTH")
        }
        assertEquals(1, s4SouthStudents.size)
        assertEquals("Peter Kato", s4SouthStudents.first().studentName)

        val s5NorthStudents = connectedStudentsList.filter {
            normalizeSecondaryClass(it.studentClass) == normalizeSecondaryClass("S5") &&
                normalizeStream(it.stream) == normalizeStream("NORTH")
        }
        assertEquals(1, s5NorthStudents.size)
        assertEquals("Sarah Opio", s5NorthStudents.first().studentName)

        // 3. Create Note: S4, NORTH only
        val noteS4North = SubjectUploadItem(
            id = "note_01",
            title = "Mechanics",
            subject = "Physics",
            targetClass = "S.4",
            targetStreams = "NORTH",
            type = "Notes",
            dateUploaded = "Oct 07, 2026"
        )

        assertTrue(doesContentTargetStudent(noteS4North.targetClass, noteS4North.targetStreams, john.studentClass, john.stream))
        assertTrue(doesContentTargetStudent(noteS4North.targetClass, noteS4North.targetStreams, mary.studentClass, mary.stream))
        assertFalse(doesContentTargetStudent(noteS4North.targetClass, noteS4North.targetStreams, peter.studentClass, peter.stream))
        assertFalse(doesContentTargetStudent(noteS4North.targetClass, noteS4North.targetStreams, sarah.studentClass, sarah.stream))

        // 4. Create Assignment: S4, NORTH + SOUTH
        val assignmentS4NorthSouth = SubjectUploadItem(
            id = "assign_01",
            title = "Thermodynamics Problems",
            subject = "Physics",
            targetClass = "S.4",
            targetStreams = "NORTH,SOUTH",
            type = "Assignment",
            dateUploaded = "Oct 07, 2026"
        )

        assertTrue(doesContentTargetStudent(assignmentS4NorthSouth.targetClass, assignmentS4NorthSouth.targetStreams, john.studentClass, john.stream))
        assertTrue(doesContentTargetStudent(assignmentS4NorthSouth.targetClass, assignmentS4NorthSouth.targetStreams, mary.studentClass, mary.stream))
        assertTrue(doesContentTargetStudent(assignmentS4NorthSouth.targetClass, assignmentS4NorthSouth.targetStreams, peter.studentClass, peter.stream))
        assertFalse(doesContentTargetStudent(assignmentS4NorthSouth.targetClass, assignmentS4NorthSouth.targetStreams, sarah.studentClass, sarah.stream))

        // 5. Create Live Class: S4, NORTH only
        val liveClassS4North = LiveClassDto(
            id = "live_01",
            teacherId = "TR1234",
            classId = "S.4",
            targetStreams = "NORTH",
            subjectId = "Physics",
            title = "Waves and Optics Discussion",
            scheduledDate = "2026-10-07",
            scheduledStartTime = "14:00"
        )

        assertTrue(doesContentTargetStudent(liveClassS4North.classId, liveClassS4North.targetStreams, john.studentClass, john.stream))
        assertTrue(doesContentTargetStudent(liveClassS4North.classId, liveClassS4North.targetStreams, mary.studentClass, mary.stream))
        assertFalse(doesContentTargetStudent(liveClassS4North.classId, liveClassS4North.targetStreams, peter.studentClass, peter.stream))
        assertFalse(doesContentTargetStudent(liveClassS4North.classId, liveClassS4North.targetStreams, sarah.studentClass, sarah.stream))

        // 6. Create Note: S4, All Streams
        val noteS4AllStreams = SubjectUploadItem(
            id = "note_02",
            title = "Electricity & Magnetism",
            subject = "Physics",
            targetClass = "S.4",
            targetStreams = "ALL",
            type = "Notes",
            dateUploaded = "Oct 07, 2026"
        )

        assertTrue(doesContentTargetStudent(noteS4AllStreams.targetClass, noteS4AllStreams.targetStreams, john.studentClass, john.stream))
        assertTrue(doesContentTargetStudent(noteS4AllStreams.targetClass, noteS4AllStreams.targetStreams, mary.studentClass, mary.stream))
        assertTrue(doesContentTargetStudent(noteS4AllStreams.targetClass, noteS4AllStreams.targetStreams, peter.studentClass, peter.stream))
        assertFalse(doesContentTargetStudent(noteS4AllStreams.targetClass, noteS4AllStreams.targetStreams, sarah.studentClass, sarah.stream))
    }

    @Test
    fun `test canonical class mapping and stream dynamic grouping`() {
        assertEquals("S.1", com.example.ui.toCanonicalClassId("Senior One"))
        assertEquals("S.2", com.example.ui.toCanonicalClassId("Senior Two"))
        assertEquals("S.3", com.example.ui.toCanonicalClassId("Senior Three"))
        assertEquals("S.4", com.example.ui.toCanonicalClassId("Senior Four"))
        assertEquals("S.5", com.example.ui.toCanonicalClassId("Senior Five"))
        assertEquals("S.6", com.example.ui.toCanonicalClassId("Senior Six"))

        val student1 = ConnectedStudent(
            id = "s1",
            studentName = "Alice",
            studentClass = "Senior One",
            subjectName = "Physics",
            teacherIdCode = "K7M4P9",
            connectedDate = "Oct 12, 2026",
            stream = "BLUE"
        )
        val student2 = ConnectedStudent(
            id = "s2",
            studentName = "Bob",
            studentClass = "S.1",
            subjectName = "Physics",
            teacherIdCode = "K7M4P9",
            connectedDate = "Oct 12, 2026",
            stream = "BLUE"
        )
        val student3 = ConnectedStudent(
            id = "s3",
            studentName = "Charlie",
            studentClass = "Senior One",
            subjectName = "Physics",
            teacherIdCode = "K7M4P9",
            connectedDate = "Oct 12, 2026",
            stream = "WHITE"
        )

        val students = listOf(student1, student2, student3)
        val s1Students = students.filter { com.example.ui.toCanonicalClassId(it.studentClass) == "S.1" }
        assertEquals(3, s1Students.size)

        val streamGroups = s1Students.groupBy { normalizeStream(it.stream) }
        assertEquals(2, streamGroups.size)
        assertEquals(2, streamGroups["BLUE"]?.size)
        assertEquals(1, streamGroups["WHITE"]?.size)

        // Verifying filtering to specific stream only returns that stream's students
        val blueStreamStudents = s1Students.filter { normalizeStream(it.stream) == "BLUE" }
        assertEquals(listOf("Alice", "Bob"), blueStreamStudents.map { it.studentName })

        val whiteStreamStudents = s1Students.filter { normalizeStream(it.stream) == "WHITE" }
        assertEquals(listOf("Charlie"), whiteStreamStudents.map { it.studentName })
    }
}
