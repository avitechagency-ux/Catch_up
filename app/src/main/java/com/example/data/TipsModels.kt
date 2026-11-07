package com.example.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

/**
 * 4 Core Tip Categories.
 */
enum class TipCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    STUDY_SMARTER("Study Smarter", Icons.Outlined.Psychology, IndigoPurple),
    MEMORY_HACKS("Memory Hacks", Icons.Outlined.TipsAndUpdates, PurpleTile),
    EXAM_SKILLS("Exam Skills", Icons.Outlined.AssignmentTurnedIn, TealTile),
    SUBJECT_TIPS("Subject Tips", Icons.Outlined.MenuBook, AmberTile)
}

/**
 * Educational Study Tip model.
 */
data class StudyTip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: TipCategory,
    val summary: String,
    val detailedExplanation: String,
    val actionableStep: String,
    val subjectTag: String? = null,
    val isApproved: Boolean = true,
    val isBookmarked: Boolean = false
)

/**
 * Today's Challenge model.
 */
data class DailyChallenge(
    val id: String = "daily-challenge-1",
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

/**
 * Repository for Tips & Daily Study Challenges.
 */
object TipsRepository {
    private const val PREFS_NAME = "cachap_tips_prefs"
    private const val KEY_TIPS = "study_tips_json"
    private const val KEY_CHALLENGE = "today_challenge_completed"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val tipsAdapter = moshi.adapter<List<StudyTip>>(
        Types.newParameterizedType(List::class.java, StudyTip::class.java)
    )

    fun getAllTips(context: Context): List<StudyTip> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TIPS, null)
        if (json.isNullOrBlank()) {
            saveTips(context, DefaultStudyTips)
            return DefaultStudyTips
        }
        return try {
            tipsAdapter.fromJson(json) ?: DefaultStudyTips
        } catch (e: Exception) {
            DefaultStudyTips
        }
    }

    fun saveTips(context: Context, tips: List<StudyTip>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val json = tipsAdapter.toJson(tips)
            prefs.edit().putString(KEY_TIPS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addTip(context: Context, tip: StudyTip) {
        val current = getAllTips(context).toMutableList()
        current.add(0, tip)
        saveTips(context, current)
    }

    fun isChallengeCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CHALLENGE, false)
    }

    fun toggleChallenge(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getBoolean(KEY_CHALLENGE, false)
        val newState = !current
        prefs.edit().putBoolean(KEY_CHALLENGE, newState).apply()
        return newState
    }

    fun getTipOfTheDay(context: Context): StudyTip {
        val tips = getAllTips(context)
        return tips.firstOrNull { it.id == "tip-1" } ?: tips.first()
    }
}

/**
 * Standard collection of evidence-based study tips tailored for secondary students.
 */
val DefaultStudyTips: List<StudyTip> = listOf(
    StudyTip(
        id = "tip-1",
        title = "The Spaced Repetition Formula for Long-Term Recall",
        category = TipCategory.STUDY_SMARTER,
        summary = "Review your notes at 24 hours, 3 days, and 1 week intervals for up to 80% higher exam retention.",
        detailedExplanation = "Ebbinghaus' Forgetting Curve proves that humans forget nearly 70% of newly learned facts within 48 hours unless actively reinforced. By reviewing your subject notes in spaced micro-sessions rather than marathon cramming, neural pathways are reinforced and knowledge transitions into permanent long-term memory.",
        actionableStep = "Today: Take 10 minutes to review yesterday's Biology or Chemistry diagrams before starting new topics."
    ),
    StudyTip(
        id = "tip-2",
        title = "Active Recall Over Passive Re-reading",
        category = TipCategory.STUDY_SMARTER,
        summary = "Testing yourself with questions produces 2x better exam performance than highlighting textbooks.",
        detailedExplanation = "When you simply re-read your notes, your brain experiences the illusion of competence—it feels familiar, but under exam conditions you cannot retrieve the facts. Close the textbook, take a blank sheet of paper, and write down everything you remember about the topic.",
        actionableStep = "Take one topic from this morning and write down all key definitions and formulae from memory."
    ),
    StudyTip(
        id = "tip-3",
        title = "The Feynman Technique: Teach It to a Child",
        category = TipCategory.MEMORY_HACKS,
        summary = "If you cannot explain a concept in simple everyday words, you haven't mastered it yet.",
        detailedExplanation = "Nobel laureate Richard Feynman mastered complex physics by pretending to explain them to a 10-year-old child. Whenever you encounter technical jargon (e.g. Osmosis or Snell's Law), translate it into everyday analogies. If you get stuck, that pinpoint is your knowledge gap.",
        actionableStep = "Explain how electrolysis works to a sibling or friend using only simple, non-scientific vocabulary."
    ),
    StudyTip(
        id = "tip-4",
        title = "Mnemonics for Organic Chemistry Series & Biology",
        category = TipCategory.MEMORY_HACKS,
        summary = "Use acronyms and vivid mental associations to effortlessly remember lists of 5+ items.",
        detailedExplanation = "In chemistry, remembering Alkanes: Methane, Ethane, Propane, Butane becomes effortless with 'Monkeys Eat Peanut Butter'. For taxonomy: 'King Philip Came Over For Good Soup' (Kingdom, Phylum, Class, Order, Family, Genus, Species).",
        actionableStep = "Create your own funny mnemonic for the first 10 elements of the periodic table today."
    ),
    StudyTip(
        id = "tip-5",
        title = "Decoding UNEB Command Words: Explain vs Describe vs Discuss",
        category = TipCategory.EXAM_SKILLS,
        summary = "Never lose easy marks in Section B by confusing what the examiner is asking for.",
        detailedExplanation = "1. 'State/List': Direct facts only, no explanation needed.\n2. 'Describe': State what happens step-by-step or appearance without giving reasons.\n3. 'Explain': Give the scientific/historical reason WHY and HOW.\n4. 'Discuss/Evaluate': Present two sides, compare trade-offs, and give a reasoned conclusion.",
        actionableStep = "Look at the last past paper you attempted and check if you gave reasons where the paper only asked to describe."
    ),
    StudyTip(
        id = "tip-6",
        title = "The 5-Minute Paper Scanning Rule",
        category = TipCategory.EXAM_SKILLS,
        summary = "Never begin writing immediately when the invigilator says start. Scan the entire paper first.",
        detailedExplanation = "Spend the first 5 minutes reading all questions in Section B. Pick the questions where you know the concepts best. Mark them with a pencil. This activates your subconscious memory while answering Section A.",
        actionableStep = "During your next timed practice test, time yourself 5 full minutes of reading before picking up your pen."
    ),
    StudyTip(
        id = "tip-7",
        title = "Maths: Always Show Every Intermediate Working Step",
        category = TipCategory.SUBJECT_TIPS,
        summary = "Over 60% of marks in UNEB Mathematics come from method marks (M1, M2), not the final answer.",
        detailedExplanation = "Even if your final arithmetic yields an incorrect number due to a sign error, you can still secure 4 out of 5 marks if your algebraic substitutions and formula derivations are clearly stated on the answer sheet.",
        actionableStep = "Always write the original formula first before substituting numerical values.",
        subjectTag = "Mathematics"
    ),
    StudyTip(
        id = "tip-8",
        title = "Physics: Double Check S.I. Units & Dimensional Consistency",
        category = TipCategory.SUBJECT_TIPS,
        summary = "Convert all values to standard metres, kilograms, and seconds before starting calculations.",
        detailedExplanation = "Examiners frequently provide distances in centimetres, mass in grams, or time in minutes to test candidate alertness. Failing to convert grams to kilograms (divide by 1000) causes an instant loss of all calculation marks.",
        actionableStep = "Write down unit symbols (m, kg, s, J, N, V) next to every given quantity before calculating.",
        subjectTag = "Physics"
    )
)
