package com.example.data

import com.example.ui.normalizeSecondaryClass
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model representing an academic stream entity in Catch Up.
 * SCHOOL -> CLASS -> STREAM -> STUDENT
 * Table: `public.streams`
 */
@JsonClass(generateAdapter = true)
data class AcademicStreamDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "school_id") val schoolId: String = "UG_SEC_01",
    @Json(name = "class_id") val classId: String, // e.g. "S.4" or "S4"
    @Json(name = "name") val name: String, // e.g. "NORTH"
    @Json(name = "normalized_name") val normalizedName: String = normalizeStream(name),
    @Json(name = "created_at") val createdAt: String = ""
)

/**
 * Trims whitespace and converts to uppercase for streams (e.g., " north " -> "NORTH").
 */
fun normalizeStream(raw: String?): String {
    if (raw == null) return ""
    return raw.trim().uppercase()
}

/**
 * Parses target_streams string into a normalized set of streams.
 * Handles formats like "ALL", "NORTH, SOUTH", "NORTH,SOUTH", "NORTH + SOUTH".
 */
fun parseTargetStreams(targetStreams: String?): Set<String> {
    if (targetStreams.isNullOrBlank()) return setOf("ALL")
    val cleaned = targetStreams.trim().uppercase()
    if (cleaned == "ALL" || cleaned == "ALL STREAMS" || cleaned.startsWith("ALL")) {
        return setOf("ALL")
    }
    return cleaned
        .split(",", ";", "+", "&")
        .map { normalizeStream(it) }
        .filter { it.isNotBlank() }
        .toSet()
}

/**
 * Formats a list or set of streams into a clean targetStreams database string.
 * Returns "ALL" if empty or contains "ALL".
 */
fun formatTargetStreams(streams: Collection<String>): String {
    if (streams.isEmpty()) return "ALL"
    val normalized = streams.map { normalizeStream(it) }.toSet()
    if ("ALL" in normalized || "ALL STREAMS" in normalized) return "ALL"
    return normalized.sorted().joinToString(",")
}

/**
 * Checks whether a given student stream matches content target streams.
 */
fun doesStreamMatch(targetStreams: String?, studentStream: String?): Boolean {
    val targets = parseTargetStreams(targetStreams)
    if ("ALL" in targets) return true
    val cleanStudentStream = normalizeStream(studentStream)
    if (cleanStudentStream.isBlank()) return true
    return cleanStudentStream in targets
}

/**
 * Central function to check if educational content (notes, assignments, live classes)
 * targets a specific student's class AND stream.
 */
fun doesContentTargetStudent(
    targetClass: String,
    targetStreams: String?,
    studentClass: String,
    studentStream: String?
): Boolean {
    val cleanStudentClass = studentClass.trim()
    val cleanTargetClass = targetClass.trim()

    // Class level check
    val classMatches = if (cleanStudentClass.isBlank() || cleanTargetClass.isBlank()) {
        true
    } else {
        normalizeSecondaryClass(cleanTargetClass) == normalizeSecondaryClass(cleanStudentClass)
    }

    if (!classMatches) return false

    // Stream level check
    return doesStreamMatch(targetStreams, studentStream)
}
