package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Question format types supported in Cachap Quizzes.
 */
enum class QuizQuestionFormat {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SHORT_ANSWER
}

/**
 * Single Quiz Question model.
 */
data class QuizQuestion(
    val id: String,
    val questionText: String,
    val format: QuizQuestionFormat,
    val options: List<String>? = null,
    val correctAnswer: String,
    val acceptableAnswers: List<String> = emptyList(),
    val explanation: String? = null
)

/**
 * Approved Quiz model for Cachap secondary curriculum.
 */
data class Quiz(
    val id: String,
    val title: String,
    val subject: String,
    val topic: String,
    val isApproved: Boolean = true,
    val timeLimitMinutes: Int? = null,
    val color: Color = IndigoPurple,
    val questions: List<QuizQuestion>
) {
    val questionCount: Int
        get() = questions.size
}

/**
 * Recorded user answer for a question during a quiz run.
 */
data class UserQuestionAnswer(
    val questionId: String,
    val questionText: String,
    val format: QuizQuestionFormat,
    val userAnswer: String,
    val isCorrect: Boolean,
    val correctAnswer: String,
    val explanation: String? = null
)

/**
 * Saved Quiz Attempt Result.
 */
data class QuizAttemptResult(
    val quizId: String,
    val quizTitle: String,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val userAnswers: List<UserQuestionAnswer>
)

/**
 * Default approved quizzes dataset across subjects in Cachap.
 */
val DefaultApprovedQuizzes: List<Quiz> = listOf(
    // -------------------------------------------------------------
    // MATHEMATICS
    // -------------------------------------------------------------
    Quiz(
        id = "math-q1",
        title = "Algebra & Linear Equations Challenge",
        subject = "Math",
        topic = "Algebra",
        isApproved = true,
        timeLimitMinutes = 10,
        color = TealTile,
        questions = listOf(
            QuizQuestion(
                id = "m1-1",
                questionText = "Solve for x: 3x - 7 = 14",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("x = 5", "x = 7", "x = 8", "x = 21"),
                correctAnswer = "x = 7",
                explanation = "Adding 7 to both sides gives 3x = 21. Dividing by 3 yields x = 7."
            ),
            QuizQuestion(
                id = "m1-2",
                questionText = "The slope of a vertical line is zero.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "False",
                explanation = "The slope of a vertical line is undefined, whereas a horizontal line has a slope of zero."
            ),
            QuizQuestion(
                id = "m1-3",
                questionText = "What is the value of 2^4 - 6?",
                format = QuizQuestionFormat.SHORT_ANSWER,
                correctAnswer = "10",
                acceptableAnswers = listOf("10", "10.0"),
                explanation = "2^4 = 16. Subtracting 6 gives 16 - 6 = 10."
            )
        )
    ),
    Quiz(
        id = "math-q2",
        title = "Quadratic Equations & Factors",
        subject = "Math",
        topic = "Quadratic Functions",
        isApproved = true,
        timeLimitMinutes = 15,
        color = PurpleTile,
        questions = listOf(
            QuizQuestion(
                id = "m2-1",
                questionText = "What are the roots of x^2 - 5x + 6 = 0?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("x = 2 and x = 3", "x = -2 and x = -3", "x = 1 and x = 6", "x = -1 and x = 5"),
                correctAnswer = "x = 2 and x = 3",
                explanation = "Factoring (x - 2)(x - 3) = 0 gives x = 2 and x = 3."
            ),
            QuizQuestion(
                id = "m2-2",
                questionText = "The discriminant b^2 - 4ac determine the nature of the roots.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "True",
                explanation = "If b^2 - 4ac > 0, there are two distinct real roots. If equal to 0, one real root."
            )
        )
    ),

    // -------------------------------------------------------------
    // PHYSICS
    // -------------------------------------------------------------
    Quiz(
        id = "phys-q1",
        title = "Newton's Laws & Force Mechanics",
        subject = "Physics",
        topic = "Mechanics",
        isApproved = true,
        timeLimitMinutes = 12,
        color = IndigoPurple,
        questions = listOf(
            QuizQuestion(
                id = "p1-1",
                questionText = "Which law states that F = m * a?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("Newton's First Law", "Newton's Second Law", "Newton's Third Law", "Law of Gravitation"),
                correctAnswer = "Newton's Second Law",
                explanation = "Newton's Second Law states that force equals mass times acceleration."
            ),
            QuizQuestion(
                id = "p1-2",
                questionText = "Weight is a scalar quantity.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "False",
                explanation = "Weight is a force pointing towards the Earth's center, making it a vector quantity."
            ),
            QuizQuestion(
                id = "p1-3",
                questionText = "What is the SI unit of Force?",
                format = QuizQuestionFormat.SHORT_ANSWER,
                correctAnswer = "Newton",
                acceptableAnswers = listOf("Newton", "N", "newton", "newtons"),
                explanation = "Force is measured in Newtons (N) in the SI system."
            )
        )
    ),

    // -------------------------------------------------------------
    // BIOLOGY
    // -------------------------------------------------------------
    Quiz(
        id = "bio-q1",
        title = "Cell Structure, Organelles & Functions",
        subject = "Biology",
        topic = "Cytology",
        isApproved = true,
        timeLimitMinutes = 10,
        color = NavyTile,
        questions = listOf(
            QuizQuestion(
                id = "b1-1",
                questionText = "Which organelle is known as the powerhouse of the cell?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("Nucleus", "Ribosome", "Mitochondrion", "Golgi Apparatus"),
                correctAnswer = "Mitochondrion",
                explanation = "Mitochondria produce ATP through cellular respiration."
            ),
            QuizQuestion(
                id = "b1-2",
                questionText = "Plant cells possess a cell wall made of cellulose.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "True",
                explanation = "Plant cell walls are composed primarily of cellulose microfibrils."
            ),
            QuizQuestion(
                id = "b1-3",
                questionText = "Name the green pigment in plant chloroplasts responsible for absorbing light energy.",
                format = QuizQuestionFormat.SHORT_ANSWER,
                correctAnswer = "Chlorophyll",
                acceptableAnswers = listOf("Chlorophyll", "chlorophyll"),
                explanation = "Chlorophyll captures sunlight energy required for photosynthesis."
            )
        )
    ),

    // -------------------------------------------------------------
    // CHEMISTRY
    // -------------------------------------------------------------
    Quiz(
        id = "chem-q1",
        title = "Periodic Table & Atomic Structure",
        subject = "Chemistry",
        topic = "Atomic Theory",
        isApproved = true,
        timeLimitMinutes = 15,
        color = AmberTile,
        questions = listOf(
            QuizQuestion(
                id = "c1-1",
                questionText = "What is the atomic number of Carbon?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("6", "12", "14", "8"),
                correctAnswer = "6",
                explanation = "Carbon has 6 protons, giving it an atomic number of 6."
            ),
            QuizQuestion(
                id = "c1-2",
                questionText = "Isotopes of an element have the same number of neutrons but different number of protons.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "False",
                explanation = "Isotopes have the SAME number of protons but a DIFFERENT number of neutrons."
            )
        )
    ),

    // -------------------------------------------------------------
    // ENGLISH
    // -------------------------------------------------------------
    Quiz(
        id = "eng-q1",
        title = "Grammar, Tenses & Active/Passive Voice",
        subject = "English",
        topic = "Grammar Mastery",
        isApproved = true,
        timeLimitMinutes = 10,
        color = CoralTile,
        questions = listOf(
            QuizQuestion(
                id = "e1-1",
                questionText = "Choose the correct passive form of: 'The teacher marked the exam papers.'",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf(
                    "The exam papers were marked by the teacher.",
                    "The exam papers are marked by the teacher.",
                    "The exam papers have been marked by the teacher.",
                    "The teacher was marking the exam papers."
                ),
                correctAnswer = "The exam papers were marked by the teacher.",
                explanation = "Simple past active ('marked') becomes simple past passive ('were marked')."
            ),
            QuizQuestion(
                id = "e1-2",
                questionText = "'Neither of the candidates was selected' is grammatically correct.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "True",
                explanation = "'Neither' takes a singular verb ('was')."
            )
        )
    ),

    // -------------------------------------------------------------
    // GEOGRAPHY
    // -------------------------------------------------------------
    Quiz(
        id = "geog-q1",
        title = "East African Physical Features & Climate",
        subject = "Geography",
        topic = "Physical Geography",
        isApproved = true,
        timeLimitMinutes = 10,
        color = BlueTile,
        questions = listOf(
            QuizQuestion(
                id = "g1-1",
                questionText = "Which is the highest mountain in East Africa?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("Mount Elgon", "Mount Kenya", "Mount Kilimanjaro", "Rwenzori Mountains"),
                correctAnswer = "Mount Kilimanjaro",
                explanation = "Mount Kilimanjaro in Tanzania is the highest peak in Africa at 5,895 meters."
            ),
            QuizQuestion(
                id = "g1-2",
                questionText = "Lake Victoria is a crater lake formed by volcanic eruption.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "False",
                explanation = "Lake Victoria is a downwarped depression lake, not a volcanic crater lake."
            )
        )
    ),

    // -------------------------------------------------------------
    // HISTORY
    // -------------------------------------------------------------
    Quiz(
        id = "hist-q1",
        title = "East African Kingdoms & Colonial Era",
        subject = "History",
        topic = "Regional History",
        isApproved = true,
        timeLimitMinutes = 12,
        color = DeepMaroon,
        questions = listOf(
            QuizQuestion(
                id = "h1-1",
                questionText = "Which year was the Buganda Agreement signed?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("1894", "1900", "1962", "1888"),
                correctAnswer = "1900",
                explanation = "The Buganda Agreement was signed in 1900 between Sir Harry Johnston and Buganda regents."
            ),
            QuizQuestion(
                id = "h1-2",
                questionText = "Uganda gained independence on October 9th, 1962.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "True",
                explanation = "Uganda gained independence from British rule on 9th October 1962."
            )
        )
    ),

    // -------------------------------------------------------------
    // ICT
    // -------------------------------------------------------------
    Quiz(
        id = "ict-q1",
        title = "Computer Fundamentals & Networking",
        subject = "ICT",
        topic = "Computer Systems",
        isApproved = true,
        timeLimitMinutes = 10,
        color = TealTile,
        questions = listOf(
            QuizQuestion(
                id = "i1-1",
                questionText = "What does RAM stand for?",
                format = QuizQuestionFormat.MULTIPLE_CHOICE,
                options = listOf("Read Access Memory", "Random Access Memory", "Rapid Action Module", "Remote Access Media"),
                correctAnswer = "Random Access Memory",
                explanation = "RAM stands for Random Access Memory, which is volatile main memory."
            ),
            QuizQuestion(
                id = "i1-2",
                questionText = "CPU is often described as the brain of the computer.",
                format = QuizQuestionFormat.TRUE_FALSE,
                options = listOf("True", "False"),
                correctAnswer = "True",
                explanation = "The Central Processing Unit (CPU) executes instructions and controls processing."
            )
        )
    )
)
