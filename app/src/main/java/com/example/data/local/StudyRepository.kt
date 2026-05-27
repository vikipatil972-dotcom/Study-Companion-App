package com.example.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StudyRepository(private val studyDao: StudyDao) {

    val allSubjects: Flow<List<Subject>> = studyDao.getAllSubjects()
    val allTopics: Flow<List<Topic>> = studyDao.getAllTopics()
    val allQuestions: Flow<List<Question>> = studyDao.getAllQuestions()
    val allFlashcards: Flow<List<Flashcard>> = studyDao.getAllFlashcards()
    val allSessions: Flow<List<StudySession>> = studyDao.getAllStudySessions()

    fun getTopicsForSubject(subjectId: Long): Flow<List<Topic>> = studyDao.getTopicsForSubject(subjectId)
    fun getQuestionsForTopic(topicId: Long): Flow<List<Question>> = studyDao.getQuestionsForTopic(topicId)
    fun getFlashcardsForTopic(topicId: Long): Flow<List<Flashcard>> = studyDao.getFlashcardsForTopic(topicId)

    suspend fun insertSubject(subject: Subject): Long = studyDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = studyDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = studyDao.deleteSubject(subject)

    suspend fun insertTopic(topic: Topic): Long = studyDao.insertTopic(topic)
    suspend fun updateTopic(topic: Topic) = studyDao.updateTopic(topic)
    suspend fun deleteTopic(topic: Topic) = studyDao.deleteTopic(topic)

    suspend fun insertQuestion(question: Question): Long = studyDao.insertQuestion(question)
    suspend fun updateQuestion(question: Question) = studyDao.updateQuestion(question)
    suspend fun deleteQuestion(question: Question) = studyDao.deleteQuestion(question)

    suspend fun insertFlashcard(flashcard: Flashcard): Long = studyDao.insertFlashcard(flashcard)
    suspend fun updateFlashcardMastery(id: Long, mastered: Boolean) = studyDao.updateFlashcardMastery(id, mastered)
    suspend fun deleteFlashcard(flashcard: Flashcard) = studyDao.deleteFlashcard(flashcard)

    suspend fun insertSession(session: StudySession): Long = studyDao.insertStudySession(session)

    // Inserts standard study mock data if empty
    suspend fun loadInitialDataIfEmpty() {
        val currentSubjects = allSubjects.first()
        if (currentSubjects.isEmpty()) {
            val sub1Id = insertSubject(Subject(title = "Quantum Physics", colorHex = "#0EA5E9", iconName = "Science", category = "Weak Areas"))
            val sub2Id = insertSubject(Subject(title = "Organic Chemistry", colorHex = "#EC4899", iconName = "Science", category = "Requires Revision"))
            val sub3Id = insertSubject(Subject(title = "Computer Science", colorHex = "#818CF8", iconName = "Code", category = "Strong Subject"))

            // Physics Topics
            val top1Id = insertTopic(Topic(
                subjectId = sub1Id,
                title = "Wave-Particle Duality",
                noteText = "De Broglie hypothesis states that matter exhibits wave-like wave and particle duality properties. Equation: λ = h / p. Where λ is wavelength, h is Planck constant (6.626e-34 J s), and p is momentum.",
                progress = 60
            ))
            insertQuestion(Question(
                topicId = top1Id,
                questionText = "State de Broglie hypothesis and derive wave-particle relation.",
                answerText = "Matter has wave properties. The de Broglie wavelength is λ = h/mv. This is proven by the double-slit electron diffraction experiment.",
                isLong = true,
                importanceLevel = "V_IMPORTANT"
            ))
            insertQuestion(Question(
                topicId = top1Id,
                questionText = "What is Planck's constant value?",
                answerText = "Planck's constant is approximately 6.626 x 10^-34 Joule-seconds (J s). Used to relate particle photo energies to their frequency.",
                isLong = false,
                importanceLevel = "IMPORTANT"
            ))
            insertFlashcard(Flashcard(topicId = top1Id, frontText = "de Broglie formula", backText = "λ = h / p (or h / mv)"))
            insertFlashcard(Flashcard(topicId = top1Id, frontText = "What is the physical interpretation of wave function squared |Ψ|^2?", backText = "The probability density of finding a particle at a given spatial location."))

            // Chemistry Topics
            val top2Id = insertTopic(Topic(
                subjectId = sub2Id,
                title = "Electrophilic Addition of Alkenes",
                noteText = "Alkenes undergo reaction with electrophiilic reagents. Hydrogen halides add across double bonds following Markovnikov's Rule (the hydrogen adds to the carbon with more hydrogen atoms to make the carbocation intermediate stable).",
                progress = 30
            ))
            insertQuestion(Question(
                topicId = top2Id,
                questionText = "State Markovnikov's Rule.",
                answerText = "In addition reactions of HX to asymmetric alkenes, the acidic hydrogen adds to the carbon with fewer alkyl groups (more hydrogens), leading to the more stable carbocation intermediate (tertiary > secondary > primary).",
                isLong = true,
                importanceLevel = "V_IMPORTANT"
            ))
            insertFlashcard(Flashcard(topicId = top2Id, frontText = "Markovnikov's Carbocation stability hierarchy", backText = "Tertiary (3°) > Secondary (2°) > Primary (1°)"))

            // Computer Science Topics
            val top3Id = insertTopic(Topic(
                subjectId = sub3Id,
                title = "Binary Search Trees (BST)",
                noteText = "A node-based binary tree data structure where the left subtree contains nodes with keys less than the node key, and the right subtree contains nodes with keys greater than the parent key.",
                progress = 85
            ))
            insertQuestion(Question(
                topicId = top3Id,
                questionText = "What is the time complexity of searching a value in a balanced BST?",
                answerText = "O(log n) in a balanced tree because of path bisection, but degrades to O(n) in a skewed (unbalanced) tree.",
                isLong = false,
                importanceLevel = "IMPORTANT"
            ))
            insertFlashcard(Flashcard(topicId = top3Id, frontText = "BST In-order Traversal Order", backText = "Left -> Root -> Right (Produces elements in strictly sorted ascending order)"))
        }
    }
}
