package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class VerifiedTeacher(
    val teacherName: String,
    val teacherIdCode: String,
    val subjectsTaught: String,
    val isVerified: Boolean = true
)

/**
 * Central registry that monitors, validates, and resolves Teacher ID codes across Cachap.
 * Prevents arbitrary spamming of random unmonitored codes and ensures students always see
 * verified teacher credentials before connecting.
 */
object TeacherCodeRegistry {

    private const val PREFS_NAME = "cachap_monitored_teacher_codes"
    private const val KEY_REGISTRY = "monitored_teachers_v1"

    // Default monitored platform teacher
    val DEFAULT_VERIFIED_TEACHER = VerifiedTeacher(
        teacherName = "Mr. David Okello",
        teacherIdCode = "K7M4P9",
        subjectsTaught = "Geography, Mathematics, Social Studies"
    )

    // In-memory cache of monitored teachers (code -> VerifiedTeacher)
    private val monitoredCodes = ConcurrentHashMap<String, VerifiedTeacher>().apply {
        put("K7M4P9", DEFAULT_VERIFIED_TEACHER)
    }

    /**
     * Registers and monitors a teacher ID code with teacher details.
     */
    fun registerCode(
        code: String,
        teacherName: String,
        subjectsTaught: String = "Ugandan Secondary Curriculum",
        context: Context? = null
    ) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length != 6) return
        val cleanName = teacherName.trim().ifBlank { "Teacher" }
        val cleanSubjects = subjectsTaught.trim().ifBlank { "Ugandan Secondary Curriculum" }

        val record = VerifiedTeacher(
            teacherName = cleanName,
            teacherIdCode = cleanCode,
            subjectsTaught = cleanSubjects,
            isVerified = true
        )
        monitoredCodes[cleanCode] = record

        context?.let { ctx ->
            runCatching {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val current = prefs.getString(KEY_REGISTRY, "") ?: ""
                val line = "$cleanCode||$cleanName||$cleanSubjects"
                val updatedLines = current.split(";;")
                    .filter { it.isNotBlank() && !it.startsWith("$cleanCode||") }
                    .toMutableList()
                updatedLines.add(0, line)
                prefs.edit().putString(KEY_REGISTRY, updatedLines.joinToString(";;")).apply()
            }
        }
    }

    /**
     * Loads persisted monitored teacher codes into cache.
     */
    fun initFromStorage(context: Context) {
        runCatching {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_REGISTRY, "") ?: ""
            if (saved.isNotBlank()) {
                saved.split(";;").forEach { entry ->
                    val parts = entry.split("||")
                    if (parts.size >= 3) {
                        val c = parts[0].trim().uppercase()
                        val n = parts[1].trim()
                        val s = parts[2].trim()
                        if (c.length == 6) {
                            monitoredCodes[c] = VerifiedTeacher(n, c, s, true)
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves a 6-character Teacher ID code against the monitored registry, local cache,
     * active teacher accounts, and Supabase users table.
     * Returns null if the code is not registered, preventing spamming of unknown codes.
     */
    suspend fun resolveTeacher(context: Context?, rawCode: String): VerifiedTeacher? =
        withContext(Dispatchers.IO) {
            val code = rawCode.trim().uppercase()
            if (code.length != 6) return@withContext null
            if (!code.all { it in 'A'..'Z' || it in '0'..'9' }) return@withContext null

            // 1. Check in-memory monitored registry
            monitoredCodes[code]?.let { return@withContext it }

            // 2. Check persistent preferences if context is available
            context?.let { ctx ->
                initFromStorage(ctx)
                monitoredCodes[code]?.let { return@withContext it }

                // Check active teacher profile stored in app prefs
                val appPrefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val activeTeacherCode = appPrefs.getString("teacher_id_code", "")?.trim()?.uppercase()
                if (!activeTeacherCode.isNullOrBlank() && activeTeacherCode == code) {
                    val activeName = appPrefs.getString("teacher_name", "")?.trim()?.ifBlank { "Teacher" } ?: "Teacher"
                    val activeSubjects = appPrefs.getString("teacher_subjects", "")?.trim()?.ifBlank { "Secondary Curriculum" } ?: "Secondary Curriculum"
                    val verified = VerifiedTeacher(activeName, code, activeSubjects, true)
                    registerCode(code, activeName, activeSubjects, ctx)
                    return@withContext verified
                }
            }

            // 3. Fallback check for default monitored code
            if (code == "K7M4P9") {
                return@withContext DEFAULT_VERIFIED_TEACHER
            }

            // 4. Query Supabase users and teacher_subjects tables
            if (SupabaseConfig.isConfigured()) {
                try {
                    val user = SupabaseRepository.fetchTeacherByCode(code)
                    if (user != null) {
                        val name = user.fullName.ifBlank { "Teacher ${user.phoneNumber.takeLast(4)}" }
                        val subjectsList = SupabaseRepository.fetchTeacherSubjectsFromSupabase(code)
                        val subjectsStr = if (subjectsList.isNotEmpty()) {
                            subjectsList.joinToString(", ") { it.subjectName }
                        } else {
                            user.subjectsTaught.ifBlank { "Secondary Curriculum" }
                        }
                        val verified = VerifiedTeacher(name, code, subjectsStr, true)
                        registerCode(code, name, subjectsStr, context)
                        return@withContext verified
                    }
                } catch (_: Exception) { }
            }

            // Not found in any monitored registry
            null
        }

    /**
     * Returns all currently monitored teachers for admin/debug inspection.
     */
    fun getAllMonitoredTeachers(): List<VerifiedTeacher> {
        return monitoredCodes.values.toList()
    }
}
