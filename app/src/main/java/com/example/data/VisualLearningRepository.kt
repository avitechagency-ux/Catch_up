package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Repository for Cachap Visual Learning — Pinterest-Inspired Educational Discovery.
 * Manages approved educational visuals, connected articles, teacher visual uploads,
 * and the administrative approval workflow.
 */
object VisualLearningRepository {

    private const val PREFS_NAME = "cachap_visual_learning_prefs"
    private const val KEY_CUSTOM_ITEMS = "visual_items_custom"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Default Approved Educational Visuals from Learning Hub
    val DefaultApprovedVisuals: List<VisualLearningItem> = listOf(
        VisualLearningItem(
            id = "vis-bio-01",
            title = "Plant & Animal Cell Ultrastructure",
            subject = "Biology",
            targetClass = "S.4",
            author = "UNEB Senior Examiner (Biology)",
            date = "26 Oct 2026",
            drawableResId = R.drawable.img_cell_biology,
            imageUrl = "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/biology/cell_ultrastructure.jpg",
            aspectRatio = VisualAspectRatio.RATIO_3_4,
            sourceType = VisualSourceType.LEARNING_HUB,
            status = "Approved",
            isApproved = true,
            article = VisualLearningArticle(
                title = "Plant and Animal Cell Ultrastructure & Organelle Specialisation",
                introduction = "Cells are the fundamental structural and functional units of all living organisms. Under electron microscopy, internal organelles reveal specialised compartments adapted for energy transformation, protein synthesis, and genetic continuity.",
                sections = listOf(
                    ArticleSection(
                        heading = "The Nucleus & Genetic Control",
                        body = "Enclosed by a double nuclear membrane perforated with nuclear pores, the nucleus houses the nucleolus and chromatin threads (DNA wrapped around histone proteins). It directs cellular metabolism by regulating transcription of messenger RNA for protein synthesis.",
                        keyPoints = listOf(
                            "Chromatin condenses into distinct chromosomes during prophase of mitosis.",
                            "Nuclear pores allow selective transport of RNA and ribosomal subunits into the cytoplasm."
                        )
                    ),
                    ArticleSection(
                        heading = "Mitochondria: Cellular Powerhouses",
                        body = "Mitochondria possess a smooth outer membrane and an intricately folded inner membrane called cristae, which increases the surface area for the electron transport chain and ATP synthase complexes during aerobic respiration (Krebs cycle occurs in the fluid matrix).",
                        keyPoints = listOf(
                            "Site of aerobic cellular respiration yielding 36–38 ATP per glucose molecule.",
                            "Tissues with elevated metabolic rates (e.g., flight muscle, liver cells) contain thousands of mitochondria."
                        )
                    ),
                    ArticleSection(
                        heading = "Structural Differences: Plant vs Animal Cells",
                        body = "Plant cells possess a rigid cellulose cell wall outside the plasmalemma providing turgidity, large central permanent vacuoles filled with cell sap, and chloroplasts containing chlorophyll for photosynthesis. Animal cells lack cell walls and chloroplasts, possessing only small temporary vacuoles and centrosomes with centrioles for spindle formation during cell division.",
                        keyPoints = listOf(
                            "Cellulose wall prevents osmotic lysis in hypotonic solutions.",
                            "Chloroplast thylakoids house light-harvesting pigments."
                        )
                    )
                ),
                keyFacts = listOf(
                    "All cells share four core features: plasma membrane, cytoplasm, ribosomes, and DNA.",
                    "Plant turgor pressure maintains non-woody plant structure against gravity.",
                    "Endoplasmic reticulum functions with Golgi bodies for intracellular trafficking."
                ),
                curriculumTip = "UNEB Exam Tip: Always state both organelle presence AND functional significance when comparing plant vs animal micrographs."
            )
        ),
        VisualLearningItem(
            id = "vis-phy-01",
            title = "Prism Dispersion & Light Wave Refraction",
            subject = "Physics",
            targetClass = "S.4",
            author = "Senior Physics Department",
            date = "25 Oct 2026",
            drawableResId = R.drawable.img_light_optics,
            imageUrl = "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/physics/light_optics_prism.jpg",
            aspectRatio = VisualAspectRatio.RATIO_16_9,
            sourceType = VisualSourceType.LEARNING_HUB,
            status = "Approved",
            isApproved = true,
            article = VisualLearningArticle(
                title = "Wave Optics: Refraction, Snell's Law & Dispersion of White Light",
                introduction = "When light transitions across the boundary separating two transparent optical media of differing optical densities, its propagation velocity changes, causing the ray to bend. In triangular prisms, dispersion separates composite white light into its constituent spectral colors.",
                sections = listOf(
                    ArticleSection(
                        heading = "Snell's Law of Refraction",
                        body = "Snell's Law states that the ratio of the sine of the angle of incidence (i) to the sine of the angle of refraction (r) is a constant value equal to the relative refractive index (n) between the two media: n = sin(i) / sin(r) = v1 / v2. When entering a denser medium such as crown glass, the ray bends toward the normal line.",
                        keyPoints = listOf(
                            "Refractive index of crown glass is approximately 1.52.",
                            "Ray velocity decreases in optically denser media: v = c / n."
                        )
                    ),
                    ArticleSection(
                        heading = "Total Internal Reflection & Critical Angle",
                        body = "When light travels from an optically denser medium to a less dense medium (e.g., glass to air) at an incident angle greater than the critical angle (c = arcsin(1/n)), the ray cannot refract into the second medium and undergoes 100% reflection internally. This principle powers modern fiber optic telecommunications.",
                        keyPoints = listOf(
                            "Critical angle for glass-air interface is approximately 41.8°.",
                            "Zero energy loss to transmission occurs during total internal reflection."
                        )
                    ),
                    ArticleSection(
                        heading = "Dispersion into the Visible Spectrum",
                        body = "Because the refractive index of glass varies inversely with wavelength, shorter wavelengths (violet ~400 nm) travel more slowly in glass and bend through a greater deviation angle than longer wavelengths (red ~700 nm). The triangular geometry prevents reciprocal refraction at the exit face, casting the classic rainbow spectrum: Red, Orange, Yellow, Green, Blue, Indigo, Violet (ROYGBIV).",
                        keyPoints = listOf(
                            "Red light deviates the least; violet light deviates the most.",
                            "Prisms demonstrate that white light is polychromatic."
                        )
                    )
                ),
                keyFacts = listOf(
                    "Frequency of a light wave remains constant during refraction; only speed and wavelength change.",
                    "Endoscopes and telecommunication cables rely on total internal reflection.",
                    "Rainbows in nature arise from combined refraction, reflection, and dispersion within atmospheric water droplets."
                ),
                curriculumTip = "UNEB Exam Tip: In ray tracing questions, always draw normal lines perpendicular to the refracting surface and include directional arrowheads."
            )
        ),
        VisualLearningItem(
            id = "vis-chem-01",
            title = "Chemical Bonding & Electron Shell Valency",
            subject = "Chemistry",
            targetClass = "S.4",
            author = "NCDC Certified Chemistry Master",
            date = "24 Oct 2026",
            drawableResId = R.drawable.img_chem_bonding,
            imageUrl = "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/chemistry/chemical_bonding_atoms.jpg",
            aspectRatio = VisualAspectRatio.RATIO_1_1,
            sourceType = VisualSourceType.LEARNING_HUB,
            status = "Approved",
            isApproved = true,
            article = VisualLearningArticle(
                title = "Atomic Structure, Valency & Chemical Bonding Mechanisms",
                introduction = "Atoms undergo chemical bonding to attain stable noble gas electron configurations with full valence energy levels (the octet rule). The nature of bonding determines whether a substance forms crystalline lattices or covalent molecules.",
                sections = listOf(
                    ArticleSection(
                        heading = "Ionic (Electrovalent) Bonding",
                        body = "Formed between metallic and non-metallic elements through the complete transfer of one or more valence electrons from the metal atom (forming a positively charged cation) to the non-metal atom (forming a negatively charged anion). The resulting giant ionic lattice is held together by strong, omnidirectional electrostatic attractions.",
                        keyPoints = listOf(
                            "Example: Na (2.8.1) loses 1e- -> Na+ (2.8); Cl (2.8.7) gains 1e- -> Cl- (2.8.8).",
                            "High melting points and conductivity in molten or aqueous state due to mobile ions."
                        )
                    ),
                    ArticleSection(
                        heading = "Covalent Bonding & Electron Sharing",
                        body = "Occurs predominantly between non-metallic atoms where valence orbitals overlap and electron pairs are shared equally (non-polar) or unequally (polar). Each shared pair of electrons constitutes a single covalent bond (e.g., H2O, CH4, CO2). Molecular substances exhibit discrete molecules bound by weak intermolecular van der Waals forces.",
                        keyPoints = listOf(
                            "Water molecule exhibits polar covalent bonding with bent 104.5° bond angle.",
                            "Low melting and boiling points due to easily overcome intermolecular attractions."
                        )
                    ),
                    ArticleSection(
                        heading = "Metallic Bonding & Sea of Delocalised Electrons",
                        body = "In pure metals, positive metal ions are arranged in tightly packed regular arrays immersed in a pervasive 'sea' of mobile, delocalised valence electrons. This mobility accounts for the exceptional electrical and thermal conductivity, malleability, and ductility of metals.",
                        keyPoints = listOf(
                            "Delocalised electrons freely drift under an applied electric potential.",
                            "Lattice layers can slide past each other without breaking the metallic bond."
                        )
                    )
                ),
                keyFacts = listOf(
                    "Noble gases (Group VIII/0) are chemically inert because of full valence shells (He: 2; Ne, Ar: 8).",
                    "Giant covalent structures like diamond and graphite have exceptionally high melting points.",
                    "Electronegativity difference determines whether bonding is primarily ionic or covalent."
                ),
                curriculumTip = "UNEB Exam Tip: Clearly differentiate between breaking intramolecular covalent bonds (requires high energy) versus overcoming weak intermolecular forces (requires low energy)."
            )
        ),
        VisualLearningItem(
            id = "vis-geo-01",
            title = "East African Great Rift Valley & Tectonic Faulting",
            subject = "Geography",
            targetClass = "S.3",
            author = "Geography Faculty Coordinator",
            date = "24 Oct 2026",
            drawableResId = R.drawable.img_rift_valley,
            imageUrl = "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/geography/east_african_rift_valley.jpg",
            aspectRatio = VisualAspectRatio.RATIO_3_4,
            sourceType = VisualSourceType.LEARNING_HUB,
            status = "Approved",
            isApproved = true,
            article = VisualLearningArticle(
                title = "Geomorphology: Tectonic Faulting & Great Rift Valley Formation in East Africa",
                introduction = "The East African Rift System is one of the world's most dramatic active continental divergent boundaries. Extending over 6,000 kilometers from the Red Sea down through Mozambique, it carves magnificent grabens, horst mountain blocks, and volcanic depressions across Uganda and East Africa.",
                sections = listOf(
                    ArticleSection(
                        heading = "The Tensional Stress Hypothesis (Gregory's Theory)",
                        body = "Under convective forces in the upper mantle (asthenosphere), crustal plates pull apart in opposite directions. The resulting horizontal tensional stress produces two parallel normal fault lines. The central crustal block, deprived of lateral support, slips downward along the fault planes to produce a steep-sided, flat-bottomed trench known as a graben or rift valley.",
                        keyPoints = listOf(
                            "Normal faults dip outward; central block subsides under gravity.",
                            "Explains the formation of Lake Albert, Lake George, and the Western Rift Branch."
                        )
                    ),
                    ArticleSection(
                        heading = "Compressional Stress Hypothesis (Wayland's Theory)",
                        body = "Wayland proposed that crustal forces pushed inward from opposing sides, creating reverse faults inclined toward one another. The side blocks are pushed upward over the central block, or the central block is forced downward while the flanks ride over it. Later denudation reshapes the escarpments into the steep fault scarps seen today.",
                        keyPoints = listOf(
                            "Reverse faults dip inward under lateral compression.",
                            "Escarpments like the Butiaba Scarp along Lake Albert exceed 600m in relief."
                        )
                    ),
                    ArticleSection(
                        heading = "Horst Blocks & Associated Volcanic Landforms",
                        body = "Where blocks of crust are pushed upward between parallel faults relative to surrounding terrain, fault-block mountains or horsts are created. The Rwenzori Mountains ('Mountains of the Moon') in Western Uganda form an iconic crystalline non-volcanic horst standing over 5,109m above sea level, contrasting with nearby volcanic features like the Virunga chain and Mt. Elgon.",
                        keyPoints = listOf(
                            "Rwenzori is an uplifted horst block, not a volcanic peak.",
                            "Geothermal activity produces hot springs like Kitagata and Sempaya."
                        )
                    )
                ),
                keyFacts = listOf(
                    "The Western Branch houses deep tectonic lakes: Lake Tanganyika (second deepest globally) and Lake Albert.",
                    "Volcanic soils around the rift margins support intensive coffee and tea agriculture.",
                    "Active rifting splits the African Plate into the Nubian and Somalian sub-plates."
                ),
                curriculumTip = "UNEB Exam Tip: Memorise both the Tension and Compression diagrams with labeled arrows denoting stress direction and fault planes."
            )
        ),
        VisualLearningItem(
            id = "vis-math-01",
            title = "Unit Circle & Trigonometric Wave Functions",
            subject = "Math",
            targetClass = "S.4",
            author = "Senior Mathematics Faculty",
            date = "23 Oct 2026",
            drawableResId = R.drawable.img_light_optics, // Reusable geometric optics/light diagram
            aspectRatio = VisualAspectRatio.RATIO_16_9,
            sourceType = VisualSourceType.LEARNING_HUB,
            status = "Approved",
            isApproved = true,
            article = VisualLearningArticle(
                title = "Trigonometric Functions, Periodic Oscillations & Unit Circle Geometry",
                introduction = "Trigonometry bridges static geometric triangles and continuous dynamic wave motion. By tracing coordinates (cos θ, sin θ) on a unit circle of radius r = 1, sine and cosine curves emerge as periodic harmonic functions essential in mathematics, physics, and engineering.",
                sections = listOf(
                    ArticleSection(
                        heading = "The Unit Circle Foundation",
                        body = "A unit circle centered at the origin (0, 0) on Cartesian coordinates has equation x² + y² = 1. For any angle θ measured counterclockwise from the positive x-axis, the coordinates of the terminal point on the circle circumference are exactly x = cos(θ) and y = sin(θ). From the Pythagorean theorem, this immediately proves the fundamental identity cos²(θ) + sin²(θ) = 1.",
                        keyPoints = listOf(
                            "Radius = 1 unit; circumference = 2π radians = 360°.",
                            "Quadrant signs: All Students Take Chemistry (ASTC) mnemonic for positive trigonometric ratios."
                        )
                    ),
                    ArticleSection(
                        heading = "Sine and Cosine Wave Characteristics",
                        body = "Plotting y = A sin(Bx - C) + D reveals key parameters: Amplitude (|A|) measures peak deviation from equilibrium; Period (T = 2π / B) measures the horizontal span of one complete cycle; C / B gives phase shift; and D determines the vertical centerline translation.",
                        keyPoints = listOf(
                            "Standard sine curve repeats every 360° (2π radians).",
                            "Cosine function is identical to sine shifted left by 90° (π/2 radians)."
                        )
                    )
                ),
                keyFacts = listOf(
                    "Tangent function tan(θ) = sin(θ) / cos(θ) has vertical asymptotes wherever cos(θ) = 0.",
                    "Sound waves, alternating electrical currents, and planetary orbits are modeled by sinusoidal equations.",
                    "Radian measure: 1 rad = 180° / π ≈ 57.3°."
                ),
                curriculumTip = "UNEB Exam Tip: Verify whether angle values in equations require degree mode or radian mode before calculating amplitudes."
            )
        ),
        VisualLearningItem(
            id = "vis-hist-01",
            title = "Archaeological Sites & Early African Civilisations",
            subject = "History",
            targetClass = "S.4",
            author = "Department of Humanities",
            date = "22 Oct 2026",
            drawableResId = R.drawable.img_cell_biology, // Reusable illustration asset
            aspectRatio = VisualAspectRatio.RATIO_4_5,
            sourceType = VisualSourceType.LEARNING_HUB,
            status = "Approved",
            isApproved = true,
            article = VisualLearningArticle(
                title = "Iron Age Innovations, Archaeological Excavations & Ancient Nile Basin Trade",
                introduction = "Centuries before colonial contact, the Great Lakes region of East Africa developed sophisticated centralised states, iron metallurgy, and intricate trade routes that flourished around Lake Victoria, the Nile Basin, and the western rift highlands.",
                sections = listOf(
                    ArticleSection(
                        heading = "The Archaeological Enigma of Bigobyamugenyi",
                        body = "Located in Sembabule District in central Uganda, Bigobyamugenyi comprises monumental defensive earthworks spanning over 10 kilometres of ditches excavated deep into solid bedrock. Associated with the legendary Chwezi dynasty, the earthworks served as fortified cattle enclosures, administrative centres, and strategic military bastions between the 14th and 16th centuries.",
                        keyPoints = listOf(
                            "Ditch depths exceed 3 to 5 metres into hard schist bedrock.",
                            "Radiocarbon dating demonstrates settled agricultural and pastoral civilisation."
                        )
                    ),
                    ArticleSection(
                        heading = "The Spread of Iron Smelting Technology",
                        body = "The transition from the Stone Age to the Iron Age transformed agriculture, weaponry, and forest clearance across East Africa. Ancient furnace sites in Bunyoro and north-western Tanzania demonstrate that early smelters used pre-heated forced draught furnaces capable of generating temperatures exceeding 1,300°C, producing high-grade carbon steel.",
                        keyPoints = listOf(
                            "Iron hoes dramatically increased food surpluses and supported population expansion.",
                            "Iron spears empowered royal warrior regiments and kingdom expansion."
                        )
                    )
                ),
                keyFacts = listOf(
                    "Bunyoro-Kitara was the earliest large centralised empire in the Interlacustrine region.",
                    "Long-distance trade exchanged salt from Lake Katwe and barkcloth for coastal beads and shells.",
                    "Oral traditions (Chwezi legends) correlate with archaeological findings at Ntusi and Bigo."
                ),
                curriculumTip = "UNEB Exam Tip: When discussing state formation, combine archaeological evidence (earthworks, pottery) with oral traditions for maximum marks."
            )
        )
    )

    /**
     * In-memory cache of teacher-uploaded and admin-managed visuals.
     */
    private val customVisualItems = mutableListOf<VisualLearningItem>()

    /**
     * Initializes repository and loads persisted custom/teacher visual items.
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CUSTOM_ITEMS, null) ?: return
        try {
            val listType = Types.newParameterizedType(List::class.java, VisualLearningItem::class.java)
            val adapter = moshi.adapter<List<VisualLearningItem>>(listType)
            val items = adapter.fromJson(json)
            if (!items.isNullOrEmpty()) {
                customVisualItems.clear()
                customVisualItems.addAll(items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves all approved visuals for students.
     * CRITICAL REQUIREMENT: Strictly filters out any pending, rejected, or unapproved uploads.
     */
    fun getApprovedVisuals(subjectFilter: String? = null): List<VisualLearningItem> {
        val allApproved = (DefaultApprovedVisuals + customVisualItems).filter { item ->
            item.isApproved && item.status.equals("Approved", ignoreCase = true)
        }

        if (subjectFilter.isNullOrBlank() || subjectFilter.equals("All", ignoreCase = true)) {
            return allApproved
        }

        return allApproved.filter { it.subject.equals(subjectFilter, ignoreCase = true) }
    }

    /**
     * Retrieves all distinct subjects represented in the approved visuals.
     */
    fun getAvailableSubjects(): List<String> {
        val subjects = (DefaultApprovedVisuals + customVisualItems)
            .filter { it.isApproved && it.status.equals("Approved", ignoreCase = true) }
            .map { it.subject }
            .distinct()
            .sorted()
        return listOf("All") + subjects
    }

    /**
     * Retrieves a single visual learning item by ID.
     */
    fun getVisualById(id: String): VisualLearningItem? {
        return (DefaultApprovedVisuals + customVisualItems).firstOrNull { it.id == id }
    }

    /**
     * Teacher uploads a new educational visual.
     * CRITICAL APPROVAL RULE: Newly submitted teacher uploads default to 'Pending'
     * and isApproved = false, so they DO NOT appear to students until admin approves them.
     */
    fun submitTeacherVisual(
        context: Context,
        title: String,
        subject: String,
        targetClass: String,
        author: String,
        imageUrl: String?,
        drawableResId: Int? = null,
        aspectRatio: VisualAspectRatio,
        article: VisualLearningArticle
    ): VisualLearningItem {
        val newItem = VisualLearningItem(
            id = "vis-upload-${System.currentTimeMillis()}",
            title = title.trim(),
            subject = subject.trim(),
            targetClass = targetClass.trim(),
            author = author.trim(),
            date = "26 Oct 2026",
            drawableResId = drawableResId,
            imageUrl = imageUrl,
            aspectRatio = aspectRatio,
            sourceType = VisualSourceType.TEACHER_UPLOAD,
            status = "Pending",
            isApproved = false,
            article = article
        )

        customVisualItems.add(newItem)
        persistCustomItems(context)
        return newItem
    }

    /**
     * Admin approves a teacher-uploaded visual item.
     * Once approved, it appears immediately in the student Visual Learning feed.
     */
    fun approveVisualItem(context: Context, itemId: String): Boolean {
        val index = customVisualItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = customVisualItems[index]
            customVisualItems[index] = item.copy(
                status = "Approved",
                isApproved = true
            )
            persistCustomItems(context)
            return true
        }
        return false
    }

    /**
     * Admin rejects a teacher-uploaded visual item.
     */
    fun rejectVisualItem(context: Context, itemId: String): Boolean {
        val index = customVisualItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = customVisualItems[index]
            customVisualItems[index] = item.copy(
                status = "Rejected",
                isApproved = false
            )
            persistCustomItems(context)
            return true
        }
        return false
    }

    /**
     * Returns pending teacher visual uploads for admin moderation.
     */
    fun getPendingTeacherUploads(): List<VisualLearningItem> {
        return customVisualItems.filter {
            it.sourceType == VisualSourceType.TEACHER_UPLOAD &&
            it.status.equals("Pending", ignoreCase = true)
        }
    }

    private fun persistCustomItems(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val listType = Types.newParameterizedType(List::class.java, VisualLearningItem::class.java)
            val adapter = moshi.adapter<List<VisualLearningItem>>(listType)
            val json = adapter.toJson(customVisualItems)
            prefs.edit().putString(KEY_CUSTOM_ITEMS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
