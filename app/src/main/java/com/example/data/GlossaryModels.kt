package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

/**
 * Educational Glossary Term model.
 */
data class GlossaryTerm(
    val id: String = UUID.randomUUID().toString(),
    val term: String,
    val subject: String,
    val targetClass: String = "S.1-S.6",
    val definition: String,
    val exampleUsage: String? = null,
    val isApproved: Boolean = true
)

/**
 * Central repository for the Educational Glossary.
 */
object GlossaryRepository {
    private const val PREFS_NAME = "cachap_glossary_prefs"
    private const val KEY_TERMS = "glossary_terms_json"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val termsAdapter = moshi.adapter<List<GlossaryTerm>>(
        Types.newParameterizedType(List::class.java, GlossaryTerm::class.java)
    )

    fun getAllTerms(context: Context): List<GlossaryTerm> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TERMS, null)
        if (json.isNullOrBlank()) {
            saveTerms(context, DefaultGlossaryTerms)
            return DefaultGlossaryTerms
        }
        return try {
            termsAdapter.fromJson(json) ?: DefaultGlossaryTerms
        } catch (e: Exception) {
            DefaultGlossaryTerms
        }
    }

    fun saveTerms(context: Context, terms: List<GlossaryTerm>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val json = termsAdapter.toJson(terms)
            prefs.edit().putString(KEY_TERMS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addTerm(context: Context, term: GlossaryTerm) {
        val current = getAllTerms(context).toMutableList()
        current.add(0, term)
        saveTerms(context, current)
    }

    fun updateTerm(context: Context, updated: GlossaryTerm) {
        val current = getAllTerms(context).map {
            if (it.id == updated.id) updated else it
        }
        saveTerms(context, current)
    }

    fun deleteTerm(context: Context, termId: String) {
        val current = getAllTerms(context).filter { it.id != termId }
        saveTerms(context, current)
    }
}

/**
 * Curated secondary curriculum glossary terms across core Ugandan & East African subjects.
 */
val DefaultGlossaryTerms: List<GlossaryTerm> = listOf(
    // MATHEMATICS
    GlossaryTerm(
        term = "Asymptote",
        subject = "Mathematics",
        targetClass = "S.4",
        definition = "A straight line that a curve approaches more and more closely as coordinates head towards infinity, without ever actually intersecting it.",
        exampleUsage = "The curve y = 1/x has the x-axis and y-axis as horizontal and vertical asymptotes."
    ),
    GlossaryTerm(
        term = "Differentiation",
        subject = "Mathematics",
        targetClass = "S.4",
        definition = "The mathematical process of finding the rate of change of a function with respect to an independent variable (the gradient function dy/dx).",
        exampleUsage = "Differentiating s(t) with respect to time yields instantaneous velocity v(t)."
    ),
    GlossaryTerm(
        term = "Hypotenuse",
        subject = "Mathematics",
        targetClass = "S.2",
        definition = "The longest side of a right-angled triangle, situated directly opposite the 90-degree angle.",
        exampleUsage = "According to Pythagoras' theorem, a² + b² = c², where c represents the hypotenuse."
    ),

    // PHYSICS
    GlossaryTerm(
        term = "Refraction",
        subject = "Physics",
        targetClass = "S.3",
        definition = "The change in direction of propagation of a wave when it passes obliquely from one medium to another in which its speed is different.",
        exampleUsage = "A straight pencil immersed partially in a beaker of water appears broken due to the refraction of light."
    ),
    GlossaryTerm(
        term = "Electromotive Force (e.m.f.)",
        subject = "Physics",
        targetClass = "S.4",
        definition = "The total electrical energy converted from chemical (or mechanical) energy per unit charge as it passes through a source of electric current.",
        exampleUsage = "A fresh dry cell produces an e.m.f. of approximately 1.5 Volts before internal resistance causes terminal voltage drop."
    ),
    GlossaryTerm(
        term = "Total Internal Reflection",
        subject = "Physics",
        targetClass = "S.4",
        definition = "The complete reflection of a light ray at the boundary of two media when the ray travels from a denser to a rarer medium at an angle exceeding the critical angle.",
        exampleUsage = "Optical fibres used in modern internet communications rely entirely on total internal reflection to transmit data."
    ),

    // CHEMISTRY
    GlossaryTerm(
        term = "Allotropy",
        subject = "Chemistry",
        targetClass = "S.3",
        definition = "The existence of an element in two or more different physical forms in the same state, differing in crystal lattice or molecular structure.",
        exampleUsage = "Diamond and graphite are crystalline allotropes of pure carbon with vastly different hardness and conductivity."
    ),
    GlossaryTerm(
        term = "Catalyst",
        subject = "Chemistry",
        targetClass = "S.2",
        definition = "A substance that increases the rate of a chemical reaction by providing an alternative reaction pathway of lower activation energy, without being consumed.",
        exampleUsage = "Finely divided iron acts as a heterogeneous catalyst in the industrial Haber process for ammonia synthesis."
    ),
    GlossaryTerm(
        term = "Electrolysis",
        subject = "Chemistry",
        targetClass = "S.4",
        definition = "The chemical decomposition of an ionic compound (electrolyte) in molten or aqueous state brought about by the passage of direct electric current.",
        exampleUsage = "During the electrolysis of acidified water, hydrogen gas evolves at the cathode and oxygen at the anode in a 2:1 ratio."
    ),

    // BIOLOGY
    GlossaryTerm(
        term = "Osmosis",
        subject = "Biology",
        targetClass = "S.1",
        definition = "The movement of water molecules from a region of higher water potential to a region of lower water potential across a semi-permeable membrane.",
        exampleUsage = "Plant root hair cells absorb water from soil moisture primarily through osmosis."
    ),
    GlossaryTerm(
        term = "Enzyme",
        subject = "Biology",
        targetClass = "S.2",
        definition = "A biological protein catalyst synthesized by living cells that accelerates biochemical metabolic reactions without being altered in the process.",
        exampleUsage = "Salivary amylase hydrolyzes dietary starch into maltose within the human mouth."
    ),
    GlossaryTerm(
        term = "Homeostasis",
        subject = "Biology",
        targetClass = "S.4",
        definition = "The maintenance of a constant internal physiological environment in an organism despite external changes.",
        exampleUsage = "Sweating and vasodilation of peripheral blood vessels help maintain mammalian body temperature at 37°C."
    ),

    // GEOGRAPHY
    GlossaryTerm(
        term = "Representative Fraction (R.F.)",
        subject = "Geography",
        targetClass = "S.1",
        definition = "A map scale expressed as a unitless numerical ratio or fraction comparing distance on the map to actual ground distance.",
        exampleUsage = "An R.F. of 1:50,000 indicates that 1 centimetre on the map represents 50,000 centimetres (500 metres) on the ground."
    ),
    GlossaryTerm(
        term = "Oxbow Lake",
        subject = "Geography",
        targetClass = "S.3",
        definition = "A crescent-shaped lake formed when a wide meander of a river is cut off from the main river channel during peak flood stages.",
        exampleUsage = "Lake Ruizi and river meander cut-offs in Western Uganda are prime physical geography examples of oxbow lakes."
    ),
    GlossaryTerm(
        term = "Horst Mountain",
        subject = "Geography",
        targetClass = "S.2",
        definition = "A raised fault block bounded by normal faults resulting from compressional or tensional forces during continental rifting.",
        exampleUsage = "The Rwenzori mountain range (Mountains of the Moon) is Africa's largest horst or block mountain."
    ),

    // HISTORY
    GlossaryTerm(
        term = "Indirect Rule",
        subject = "History",
        targetClass = "S.3",
        definition = "A British colonial administrative system spearheaded by Lord Lugard where existing indigenous hierarchical structures and chiefs were retained to implement colonial policies.",
        exampleUsage = "The 1900 Buganda Agreement consolidated indirect rule by conferring freehold land titles (Mailo) to local chiefs."
    ),
    GlossaryTerm(
        term = "Scramble for Africa",
        subject = "History",
        targetClass = "S.2",
        definition = "The invasion, occupation, and annexation of African territory by European powers between 1881 and 1914, formalized at the 1884 Berlin Conference.",
        exampleUsage = "The Anglo-German Treaty of 1890 partitioned East Africa, placing Uganda and Kenya under the British sphere of influence."
    ),

    // ENGLISH
    GlossaryTerm(
        term = "Alliteration",
        subject = "English",
        targetClass = "S.1",
        definition = "The repetition of the same consonant sound at the beginning of closely connected words in poetry or prose for rhythmic and musical effect.",
        exampleUsage = "‘Peter Piper picked a peck of pickled peppers’ demonstrates pervasive voiceless plosive alliteration."
    ),
    GlossaryTerm(
        term = "Metaphor",
        subject = "English",
        targetClass = "S.2",
        definition = "A figure of speech in which a word or phrase is applied to an object or action to which it is not literally applicable, without using ‘like’ or ‘as’.",
        exampleUsage = "In Okot p'Bitek's Song of Lawino, cultural alienation is vividly portrayed through poignant African metaphors."
    ),

    // ECONOMICS
    GlossaryTerm(
        term = "Opportunity Cost",
        subject = "Economics",
        targetClass = "S.5",
        definition = "The cost of an alternative that must be forgone in order to pursue a certain action; the value of the next best alternative sacrificed.",
        exampleUsage = "Choosing to allocate national budget revenue to road infrastructure carries an opportunity cost in deferred hospital construction."
    ),
    GlossaryTerm(
        term = "Inflation",
        subject = "Economics",
        targetClass = "S.5",
        definition = "A persistent and noticeable general increase in the price level of goods and services in an economy over a given period of time.",
        exampleUsage = "Cost-push inflation occurs when rising fuel import costs force transportation and manufacturing prices upward across Uganda."
    )
)
