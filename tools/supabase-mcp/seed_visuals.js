import fs from "fs";

const token = fs.readFileSync("tools/supabase-mcp/.supabase_pat", "utf-8").trim();

const items = [
  {
    title: "Plant & Animal Cell Ultrastructure",
    subject: "Biology",
    target_class: "S.4",
    author: "UNEB Senior Examiner (Biology)",
    image_url: "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/biology/cell_ultrastructure.jpg",
    aspect_ratio: "3:4",
    source_type: "LEARNING_HUB",
    status: "Approved",
    is_approved: true,
    article_title: "Plant and Animal Cell Ultrastructure & Organelle Specialisation",
    article_introduction: "Cells are the fundamental structural and functional units of all living organisms. Under electron microscopy, internal organelles reveal specialised compartments adapted for energy transformation, protein synthesis, and genetic continuity.",
    article_sections: [
      {
        heading: "The Nucleus & Genetic Control",
        body: "Enclosed by a double nuclear membrane perforated with nuclear pores, the nucleus houses the nucleolus and chromatin threads (DNA wrapped around histone proteins). It directs cellular metabolism by regulating transcription of messenger RNA for protein synthesis.",
        keyPoints: [
          "Chromatin condenses into distinct chromosomes during prophase of mitosis.",
          "Nuclear pores allow selective transport of RNA and ribosomal subunits into the cytoplasm."
        ]
      },
      {
        heading: "Mitochondria: Cellular Powerhouses",
        body: "Mitochondria possess a smooth outer membrane and an intricately folded inner membrane called cristae, which increases the surface area for the electron transport chain and ATP synthase complexes during aerobic respiration (Krebs cycle occurs in the fluid matrix).",
        keyPoints: [
          "Site of aerobic cellular respiration yielding 36–38 ATP per glucose molecule.",
          "Tissues with elevated metabolic rates (e.g., flight muscle, liver cells) contain thousands of mitochondria."
        ]
      },
      {
        heading: "Structural Differences: Plant vs Animal Cells",
        body: "Plant cells possess a rigid cellulose cell wall outside the plasmalemma providing turgidity, large central permanent vacuoles filled with cell sap, and chloroplasts containing chlorophyll for photosynthesis. Animal cells lack cell walls and chloroplasts, possessing only small temporary vacuoles and centrosomes with centrioles for spindle formation during cell division.",
        keyPoints: [
          "Cellulose wall prevents osmotic lysis in hypotonic solutions.",
          "Chloroplast thylakoids house light-harvesting pigments."
        ]
      }
    ],
    key_facts: [
      "All cells share four core features: plasma membrane, cytoplasm, ribosomes, and DNA.",
      "Plant turgor pressure maintains non-woody plant structure against gravity.",
      "Endoplasmic reticulum functions with Golgi bodies for intracellular trafficking."
    ],
    curriculum_tip: "UNEB Exam Tip: Always state both organelle presence AND functional significance when comparing plant vs animal micrographs."
  },
  {
    title: "Prism Dispersion & Light Wave Refraction",
    subject: "Physics",
    target_class: "S.4",
    author: "Senior Physics Department",
    image_url: "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/physics/light_optics_prism.jpg",
    aspect_ratio: "16:9",
    source_type: "LEARNING_HUB",
    status: "Approved",
    is_approved: true,
    article_title: "Wave Optics: Refraction, Snell's Law & Dispersion of White Light",
    article_introduction: "When light transitions across the boundary separating two transparent optical media of differing optical densities, its propagation velocity changes, causing the ray to bend. In triangular prisms, dispersion separates composite white light into its constituent spectral colors.",
    article_sections: [
      {
        heading: "Snell's Law of Refraction",
        body: "Snell's Law states that the ratio of the sine of the angle of incidence (i) to the sine of the angle of refraction (r) is a constant value equal to the relative refractive index (n) between the two media: n = sin(i) / sin(r) = v1 / v2. When entering a denser medium such as crown glass, the ray bends toward the normal line.",
        keyPoints: [
          "Refractive index of crown glass is approximately 1.52.",
          "Ray velocity decreases in optically denser media: v = c / n."
        ]
      },
      {
        heading: "Total Internal Reflection & Critical Angle",
        body: "When light travels from an optically denser medium to a less dense medium (e.g., glass to air) at an incident angle greater than the critical angle (c = arcsin(1/n)), the ray cannot refract into the second medium and undergoes 100% reflection internally. This principle powers modern fiber optic telecommunications.",
        keyPoints: [
          "Critical angle for glass-air interface is approximately 41.8°.",
          "Zero energy loss to transmission occurs during total internal reflection."
        ]
      },
      {
        heading: "Dispersion into the Visible Spectrum",
        body: "Because the refractive index of glass varies inversely with wavelength, shorter wavelengths (violet ~400 nm) travel more slowly in glass and bend through a greater deviation angle than longer wavelengths (red ~700 nm). The triangular geometry prevents reciprocal refraction at the exit face, casting the classic rainbow spectrum: Red, Orange, Yellow, Green, Blue, Indigo, Violet (ROYGBIV).",
        keyPoints: [
          "Red light deviates the least; violet light deviates the most.",
          "Prisms demonstrate that white light is polychromatic."
        ]
      }
    ],
    key_facts: [
      "Frequency of a light wave remains constant during refraction; only speed and wavelength change.",
      "Endoscopes and telecommunication cables rely on total internal reflection.",
      "Rainbows in nature arise from combined refraction, reflection, and dispersion within atmospheric water droplets."
    ],
    curriculum_tip: "UNEB Exam Tip: In ray tracing questions, always draw normal lines perpendicular to the refracting surface and include directional arrowheads."
  },
  {
    title: "Chemical Bonding & Electron Shell Valency",
    subject: "Chemistry",
    target_class: "S.4",
    author: "NCDC Certified Chemistry Master",
    image_url: "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/chemistry/chemical_bonding_atoms.jpg",
    aspect_ratio: "1:1",
    source_type: "LEARNING_HUB",
    status: "Approved",
    is_approved: true,
    article_title: "Atomic Structure, Valency & Chemical Bonding Mechanisms",
    article_introduction: "Atoms undergo chemical bonding to attain stable noble gas electron configurations with full valence energy levels (the octet rule). The nature of bonding determines whether a substance forms crystalline lattices or covalent molecules.",
    article_sections: [
      {
        heading: "Ionic (Electrovalent) Bonding",
        body: "Formed between metallic and non-metallic elements through the complete transfer of one or more valence electrons from the metal atom (forming a positively charged cation) to the non-metal atom (forming a negatively charged anion). The resulting giant ionic lattice is held together by strong, omnidirectional electrostatic attractions.",
        keyPoints: [
          "Example: Na (2.8.1) loses 1e- -> Na+ (2.8); Cl (2.8.7) gains 1e- -> Cl- (2.8.8).",
          "High melting points and conductivity in molten or aqueous state due to mobile ions."
        ]
      },
      {
        heading: "Covalent Bonding & Electron Sharing",
        body: "Occurs predominantly between non-metallic atoms where valence orbitals overlap and electron pairs are shared equally (non-polar) or unequally (polar). Each shared pair of electrons constitutes a single covalent bond (e.g., H2O, CH4, CO2). Molecular substances exhibit discrete molecules bound by weak intermolecular van der Waals forces.",
        keyPoints: [
          "Water molecule exhibits polar covalent bonding with bent 104.5° bond angle.",
          "Low melting and boiling points due to easily overcome intermolecular attractions."
        ]
      },
      {
        heading: "Metallic Bonding & Sea of Delocalised Electrons",
        body: "In pure metals, positive metal ions are arranged in tightly packed regular arrays immersed in a pervasive 'sea' of mobile, delocalised valence electrons. This mobility accounts for the exceptional electrical and thermal conductivity, malleability, and ductility of metals.",
        keyPoints: [
          "Delocalised electrons freely drift under an applied electric potential.",
          "Lattice layers can slide past each other without breaking the metallic bond."
        ]
      }
    ],
    key_facts: [
      "Noble gases (Group VIII/0) are chemically inert because of full valence shells (He: 2; Ne, Ar: 8).",
      "Giant covalent structures like diamond and graphite have exceptionally high melting points.",
      "Electronegativity difference determines whether bonding is primarily ionic or covalent."
    ],
    curriculum_tip: "UNEB Exam Tip: Clearly differentiate between breaking intramolecular covalent bonds (requires high energy) versus overcoming weak intermolecular forces (requires low energy)."
  },
  {
    title: "East African Great Rift Valley & Tectonic Faulting",
    subject: "Geography",
    target_class: "S.3",
    author: "Geography Faculty Coordinator",
    image_url: "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev/visual_learning/geography/east_african_rift_valley.jpg",
    aspect_ratio: "3:4",
    source_type: "LEARNING_HUB",
    status: "Approved",
    is_approved: true,
    article_title: "Geomorphology: Tectonic Faulting & Great Rift Valley Formation in East Africa",
    article_introduction: "The East African Rift System is one of the world's most dramatic active continental divergent boundaries. Extending over 6,000 kilometers from the Red Sea down through Mozambique, it carves magnificent grabens, horst mountain blocks, and volcanic depressions across Uganda and East Africa.",
    article_sections: [
      {
        heading: "The Tensional Stress Hypothesis (Gregory's Theory)",
        body: "Under convective forces in the upper mantle (asthenosphere), crustal plates pull apart in opposite directions. The resulting horizontal tensional stress produces two parallel normal fault lines. The central crustal block, deprived of lateral support, slips downward along the fault planes to produce a steep-sided, flat-bottomed trench known as a graben or rift valley.",
        keyPoints: [
          "Normal faults dip outward; central block subsides under gravity.",
          "Explains the formation of Lake Albert, Lake George, and the Western Rift Branch."
        ]
      },
      {
        heading: "Compressional Stress Hypothesis (Wayland's Theory)",
        body: "Wayland proposed that crustal forces pushed inward from opposing sides, creating reverse faults inclined toward one another. The side blocks are pushed upward over the central block, or the central block is forced downward while the flanks ride over it. Later denudation reshapes the escarpments into the steep fault scarps seen today.",
        keyPoints: [
          "Reverse faults dip inward under lateral compression.",
          "Escarpments like the Butiaba Scarp along Lake Albert exceed 600m in relief."
        ]
      },
      {
        heading: "Horst Blocks & Associated Volcanic Landforms",
        body: "Where blocks of crust are pushed upward between parallel faults relative to surrounding terrain, fault-block mountains or horsts are created. The Rwenzori Mountains ('Mountains of the Moon') in Western Uganda form an iconic crystalline non-volcanic horst standing over 5,109m above sea level, contrasting with nearby volcanic features like the Virunga chain and Mt. Elgon.",
        keyPoints: [
          "Rwenzori is an uplifted horst block, not a volcanic peak.",
          "Geothermal activity produces hot springs like Kitagata and Sempaya."
        ]
      }
    ],
    key_facts: [
      "The Western Branch houses deep tectonic lakes: Lake Tanganyika (second deepest globally) and Lake Albert.",
      "Volcanic soils around the rift margins support intensive coffee and tea agriculture.",
      "Active rifting splits the African Plate into the Nubian and Somalian sub-plates."
    ],
    curriculum_tip: "UNEB Exam Tip: Memorise both the Tension and Compression diagrams with labeled arrows denoting stress direction and fault planes."
  }
];

function escapeLiteral(str) {
  if (str === null || str === undefined) return "NULL";
  return "'" + String(str).replace(/'/g, "''") + "'";
}

async function run() {
  for (const item of items) {
    const query = `
      INSERT INTO public.visual_learning_items (
        title, subject, target_class, author, image_url, aspect_ratio, source_type, status, is_approved,
        article_title, article_introduction, article_sections, key_facts, curriculum_tip
      ) VALUES (
        ${escapeLiteral(item.title)},
        ${escapeLiteral(item.subject)},
        ${escapeLiteral(item.target_class)},
        ${escapeLiteral(item.author)},
        ${escapeLiteral(item.image_url)},
        ${escapeLiteral(item.aspect_ratio)},
        ${escapeLiteral(item.source_type)},
        ${escapeLiteral(item.status)},
        ${item.is_approved},
        ${escapeLiteral(item.article_title)},
        ${escapeLiteral(item.article_introduction)},
        ${escapeLiteral(JSON.stringify(item.article_sections))}::jsonb,
        ${escapeLiteral(JSON.stringify(item.key_facts))}::jsonb,
        ${escapeLiteral(item.curriculum_tip)}
      );
    `;

    const res = await fetch("https://api.supabase.com/v1/projects/xzutgxiwexxhmpqtybaf/database/query", {
      method: "POST",
      headers: {
        Authorization: "Bearer " + token,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ query })
    });

    const data = await res.json();
    console.log("Seeded:", item.title, "Status:", res.status, "Response:", data);
  }
}

run().catch(console.error);
