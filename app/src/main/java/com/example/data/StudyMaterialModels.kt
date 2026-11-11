package com.example.data

/**
 * Model representing a study material resource (DOCX / PDF documents).
 */
data class StudyMaterialResource(
    val id: String,
    val title: String,
    val subject: String,
    val targetClass: String = "S.4",
    val author: String = "UNEB Senior Examiner",
    val fileType: String = "DOCX", // "DOCX" or "PDF"
    val fileSize: String = "1.8 MB",
    val documentUrl: String? = null,
    val dateUploaded: String = "Nov 2026"
) {
    val metadata: String
        get() = "$targetClass • $subject • $fileType ($fileSize)"
}

val DefaultStudyMaterialResources: List<StudyMaterialResource> = listOf(
    StudyMaterialResource(
        id = "sm-1",
        title = "Complete Physics Mechanics & Wave Notes",
        subject = "Physics",
        targetClass = "S.4",
        author = "Tr. Okello David",
        fileType = "DOCX",
        fileSize = "2.4 MB",
        documentUrl = "https://example.com/physics_notes.docx",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-2",
        title = "Organic Chemistry Reactions & Titration Guide",
        subject = "Chemistry",
        targetClass = "S.4",
        author = "Dr. Mugisha Patrick",
        fileType = "PDF",
        fileSize = "3.1 MB",
        documentUrl = "https://example.com/chemistry_guide.pdf",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-3",
        title = "Cell Genetics & Human Physiology Summary",
        subject = "Biology",
        targetClass = "S.3",
        author = "Ms. Nalubega Sarah",
        fileType = "PDF",
        fileSize = "1.9 MB",
        documentUrl = "https://example.com/biology_genetics.pdf",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-4",
        title = "Calculus & Trigonometry Solved Revision Booklet",
        subject = "Mathematics",
        targetClass = "S.4",
        author = "Tr. Kato Emmanuel",
        fileType = "DOCX",
        fileSize = "2.8 MB",
        documentUrl = "https://example.com/math_calculus.docx",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-5",
        title = "East African History 19th Century Kingdoms Notes",
        subject = "History",
        targetClass = "S.2",
        author = "Senior Master Akena",
        fileType = "PDF",
        fileSize = "2.2 MB",
        documentUrl = "https://example.com/history_east_africa.pdf",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-6",
        title = "Physical Geography Rift Valley & Volcanicity Notes",
        subject = "Geography",
        targetClass = "S.1",
        author = "Ms. Tumusiime Grace",
        fileType = "DOCX",
        fileSize = "1.6 MB",
        documentUrl = "https://example.com/geography_physical.docx",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-7",
        title = "English Grammar, Summary Writing & Essay Guide",
        subject = "English",
        targetClass = "S.4",
        author = "Mrs. Babirye Joy",
        fileType = "PDF",
        fileSize = "2.0 MB",
        documentUrl = "https://example.com/english_guide.pdf",
        dateUploaded = "Nov 2026"
    ),
    StudyMaterialResource(
        id = "sm-8",
        title = "Microeconomics Price Theory & Market Equilibrium",
        subject = "Economics",
        targetClass = "S.5",
        author = "Mr. Mukasa Ronald",
        fileType = "DOCX",
        fileSize = "2.5 MB",
        documentUrl = "https://example.com/economics_price_theory.docx",
        dateUploaded = "Nov 2026"
    )
)
