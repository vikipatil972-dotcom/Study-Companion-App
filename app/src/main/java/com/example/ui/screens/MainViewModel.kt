package com.example.ui.screens

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.*
import com.example.ui.components.StrokeLine
import com.example.ui.components.StudyTrack
import com.example.audio.ImmersiveAudioEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MessageItem(val sender: String, val text: String, val timestamp: Long = System.currentTimeMillis())
data class QuizItem(val question: String, val options: List<String>, val correctIndex: Int, val explanation: String)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = StudyRepository(database.studyDao())

    private val prefs = application.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isUserRegistered: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun isAccountCreated(): Boolean {
        return !prefs.getString("registered_name", "").isNullOrBlank()
    }

    fun registerUser(name: String, password: String) {
        prefs.edit()
            .putString("registered_name", name.trim())
            .putString("registered_password", password.trim())
            .putString("user_name", name.trim())
            .putBoolean("is_logged_in", true)
            .apply()
        _userName.value = name.trim()
        _isLoggedIn.value = true
    }

    fun loginUser(name: String, password: String): String? {
        val trimmedName = name.trim()
        val trimmedPass = password.trim()
        if (trimmedName.isBlank() || trimmedPass.isBlank()) {
            return "Name and password are required"
        }
        val savedName = prefs.getString("registered_name", "") ?: ""
        val savedPassword = prefs.getString("registered_password", "") ?: ""

        return if (savedName.equals(trimmedName, ignoreCase = true) && savedPassword == trimmedPass) {
            prefs.edit()
                .putString("user_name", savedName)
                .putBoolean("is_logged_in", true)
                .apply()
            _userName.value = savedName
            _isLoggedIn.value = true
            null
        } else {
            "Invalid Username or Password. Please try again."
        }
    }

    fun logoutUser() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        _isLoggedIn.value = false
    }

    // --- Database Sources ---
    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topics: StateFlow<List<Topic>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val questions: StateFlow<List<Question>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Music States ---
    private val allTracks = listOf(
        StudyTrack("Lofi Focus Beat", "Chill Hop Studio", "Lofi", 184),
        StudyTrack("Cosmic Ambient Synthesizer", "Space Waves", "Night Study", 240),
        StudyTrack("Deep Forest Rain Piano", "Nature Melodies", "Instrumental", 310),
        StudyTrack("Binaural Focus Alpha Waves", "Brain Wave Labs", "Focus", 400),
        StudyTrack("Sunset Late Night Study", "Lofi Beats", "Lofi", 195),
        StudyTrack("Zen Soundscapes", "Aero Instrumental", "Instrumental", 220)
    )

    private val _filteredTracks = MutableStateFlow(allTracks)
    val filteredTracks: StateFlow<List<StudyTrack>> = _filteredTracks.asStateFlow()

    private val _activeTrack = MutableStateFlow(allTracks[0])
    val activeTrack: StateFlow<StudyTrack> = _activeTrack.asStateFlow()

    private val _musicIsPlaying = MutableStateFlow(false)
    val musicIsPlaying: StateFlow<Boolean> = _musicIsPlaying.asStateFlow()

    private val _musicTrackProgress = MutableStateFlow(0.12f)
    val musicTrackProgress: StateFlow<Float> = _musicTrackProgress.asStateFlow()

    private val _selectedMusicCategory = MutableStateFlow("All")
    val selectedMusicCategory: StateFlow<String> = _selectedMusicCategory.asStateFlow()


    // --- Study Timer States ---
    private val _timerSeconds = MutableStateFlow(1500) // Default 25 min Pomodoro
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerIsActive = MutableStateFlow(false)
    val timerIsActive: StateFlow<Boolean> = _timerIsActive.asStateFlow()

    private val _timerMaxSeconds = MutableStateFlow(1500)
    val timerMaxSeconds: StateFlow<Int> = _timerMaxSeconds.asStateFlow()


    // --- Whiteboard States ---
    private val _whiteboardStrokes = mutableStateListOf<StrokeLine>()
    val whiteboardStrokes: List<StrokeLine> get() = _whiteboardStrokes


    // --- AI Assistant Chat States ---
    private val _aiHistory = MutableStateFlow<List<MessageItem>>(
        listOf(
            MessageItem("AI Assistant", "Hello! I am your study partner powered by Gemini. Ask me to explain a syllabus topic, solve a doubt step-by-step, or convert Notes into Flashcards. Try clicking one of the shortcuts below!")
        )
    )
    val aiHistory: StateFlow<List<MessageItem>> = _aiHistory.asStateFlow()

    private val _aiIsLoading = MutableStateFlow(false)
    val aiIsLoading: StateFlow<Boolean> = _aiIsLoading.asStateFlow()


    // --- Quiz Practice States ---
    private val _quizIsActive = MutableStateFlow(false)
    val quizIsActive: StateFlow<Boolean> = _quizIsActive.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizItem>>(emptyList())
    val quizQuestions: StateFlow<List<QuizItem>> = _quizQuestions.asStateFlow()

    private val _quizCurrentIndex = MutableStateFlow(0)
    val quizCurrentIndex: StateFlow<Int> = _quizCurrentIndex.asStateFlow()

    private val _quizSelectedAnswerIndex = MutableStateFlow<Int?>(null)
    val quizSelectedAnswerIndex: StateFlow<Int?> = _quizSelectedAnswerIndex.asStateFlow()

    private val _quizAnswersChecked = MutableStateFlow(false)
    val quizAnswersChecked: StateFlow<Boolean> = _quizAnswersChecked.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()


    // --- Jobs ---
    private var timerJob: Job? = null
    private var musicJob: Job? = null


    init {
        // Init Database
        viewModelScope.launch {
            repository.loadInitialDataIfEmpty()
        }

        // Background ticker for music progress when active
        musicJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_musicIsPlaying.value) {
                    val currentVal = _musicTrackProgress.value
                    val step = 1.0f / _activeTrack.value.durationSeconds
                    val newVal = if (currentVal + step >= 1.0f) 0.0f else currentVal + step
                    _musicTrackProgress.value = newVal
                }
            }
        }
    }

    // --- Subject Operations ---
    fun addSubject(title: String, colorHex: String, iconName: String, category: String = "Core Course") {
        viewModelScope.launch {
            repository.insertSubject(Subject(title = title, colorHex = colorHex, iconName = iconName, category = category))
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // --- Topic Operations ---
    fun addTopic(subjectId: Long, title: String, noteText: String) {
        viewModelScope.launch {
            repository.insertTopic(Topic(subjectId = subjectId, title = title, noteText = noteText))
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
        }
    }

    fun saveNoteChanges(topicId: Long, noteText: String) {
        viewModelScope.launch {
            val topic = database.studyDao().getTopicById(topicId)
            if (topic != null) {
                repository.updateTopic(topic.copy(noteText = noteText, progress = topic.progress.coerceAtLeast(30)))
            }
        }
    }

    // --- Question Operations ---
    fun addQuestion(topicId: Long, questionText: String, answerText: String, importanceLevel: String = "IMPORTANT") {
        viewModelScope.launch {
            repository.insertQuestion(Question(topicId = topicId, questionText = questionText, answerText = answerText, importanceLevel = importanceLevel))
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    // --- Interactive Flashcard Operations ---
    fun addFlashcard(topicId: Long, front: String, back: String) {
        viewModelScope.launch {
            repository.insertFlashcard(Flashcard(topicId = topicId, frontText = front, backText = back))
        }
    }

    fun toggleFlashcardStatus(id: Long, mastered: Boolean) {
        viewModelScope.launch {
            repository.updateFlashcardMastery(id, mastered)
        }
    }

    fun deleteFlashcard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
        }
    }

    // --- Study timer methods ---
    fun setTimerDuration(minutes: Int) {
        _timerSeconds.value = minutes * 60
        _timerMaxSeconds.value = minutes * 60
        _timerIsActive.value = false
        timerJob?.cancel()
    }

    fun toggleFocusTimer() {
        val currentlyActive = _timerIsActive.value
        _timerIsActive.value = !currentlyActive

        if (!currentlyActive) {
            timerJob = viewModelScope.launch {
                // If study timer is turned on, also activate the music automatically! Sync rules!
                _musicIsPlaying.value = true
                ImmersiveAudioEngine.start(getApplication(), _activeTrack.value.title)
                while (_timerSeconds.value > 0) {
                    delay(1000)
                    _timerSeconds.value -= 1
                }
                // Timer finished!
                _timerIsActive.value = false
                _musicIsPlaying.value = false
                ImmersiveAudioEngine.stop()
                // Save study session minutes to Db
                repository.insertSession(StudySession(subjectId = 1, durationMinutes = _timerMaxSeconds.value / 60))
            }
        } else {
            timerJob?.cancel()
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        _timerIsActive.value = false
        _timerSeconds.value = _timerMaxSeconds.value
    }


    // --- Music Controllers ---
    fun togglePlayMusic() {
        val nextPlaying = !_musicIsPlaying.value
        _musicIsPlaying.value = nextPlaying
        if (nextPlaying) {
            ImmersiveAudioEngine.start(getApplication(), _activeTrack.value.title)
        } else {
            ImmersiveAudioEngine.stop()
        }
    }

    fun selectMusicCategory(category: String) {
        _selectedMusicCategory.value = category
        _filteredTracks.value = if (category == "All") {
            allTracks
        } else {
            allTracks.filter { it.category == category }
        }
        val match = _filteredTracks.value.firstOrNull()
        if (match != null && match != _activeTrack.value) {
            _activeTrack.value = match
            _musicTrackProgress.value = 0.0f
            if (_musicIsPlaying.value) {
                ImmersiveAudioEngine.start(getApplication(), match.title)
            }
        }
    }

    fun playNextTrack() {
        val list = _filteredTracks.value
        val index = list.indexOf(_activeTrack.value)
        val nextIndex = if (index == -1 || index >= list.size - 1) 0 else index + 1
        val match = list[nextIndex]
        _activeTrack.value = match
        _musicTrackProgress.value = 0.0f
        if (_musicIsPlaying.value) {
            ImmersiveAudioEngine.start(getApplication(), match.title)
        }
    }

    fun playPrevTrack() {
        val list = _filteredTracks.value
        val index = list.indexOf(_activeTrack.value)
        val prevIndex = if (index <= 0) list.size - 1 else index - 1
        val match = list[prevIndex]
        _activeTrack.value = match
        _musicTrackProgress.value = 0.0f
        if (_musicIsPlaying.value) {
            ImmersiveAudioEngine.start(getApplication(), match.title)
        }
    }


    // --- Whiteboard operations ---
    fun addWhiteboardStroke(stroke: StrokeLine) {
        _whiteboardStrokes.add(stroke)
    }

    fun clearWhiteboard() {
        _whiteboardStrokes.clear()
    }


    // --- AI Doubt Solver / Assistant Operations ---
    fun submitAiQuestion(question: String) {
        _aiHistory.value = _aiHistory.value + MessageItem("You", question)
        _aiIsLoading.value = true

        viewModelScope.launch {
            val response = GeminiClient.generateContent(
                "You are an education expert academic tutor helper for students. " +
                "Solve doubts step-by-step. Break down complex topics into clear explanations. " +
                "Use bullet points, bold lists, code sections, math formulas, or steps. " +
                "Here is the study query: $question"
            )
            _aiHistory.value = _aiHistory.value + MessageItem("AI Assistant", response)
            _aiIsLoading.value = false
        }
    }

    fun clearAiHistory() {
        _aiHistory.value = listOf(
            MessageItem("AI Assistant", "Workspace logs reset. What shall we study next?")
        )
    }

    // AI study cards creator (Syllabus Convert notes -> revision flashcard format!)
    fun triggerAiConvertToFlashcards(topicId: Long, noteText: String, onFinished: (String) -> Unit) {
        _aiIsLoading.value = true
        viewModelScope.launch {
            val prompt = """
                Extract 2 essential study flashcard revision items from this notes content.
                Format rules:
                Output EXACTLY and ONLY blocks in this plain structure:
                Front: [Write brief question or concept]
                Back: [Write clear, concise explanation]
                ---
                Front: [Write second question or concept]
                Back: [Write second explanation]

                Do not include markdown intro or wrapping paragraphs.
                Notes:
                $noteText
            """.trimIndent()

            val response = GeminiClient.generateContent(prompt)
            
            // Try to parse out cards
            try {
                val blocks = response.split("---")
                var cardsCreated = 0
                for (block in blocks) {
                    var front = ""
                    var back = ""
                    val lines = block.lines()
                    for (line in lines) {
                        if (line.startsWith("Front:", ignoreCase = true)) {
                            front = line.substringAfter("Front:").trim().removeSurrounding("[", "]")
                        }
                        if (line.startsWith("Back:", ignoreCase = true)) {
                            back = line.substringAfter("Back:").trim().removeSurrounding("[", "]")
                        }
                    }
                    if (front.isNotEmpty() && back.isNotEmpty()) {
                        repository.insertFlashcard(Flashcard(topicId = topicId, frontText = front, backText = back))
                        cardsCreated++
                    }
                }

                if (cardsCreated == 0) {
                    // Fallback to offline heuristic parser in case Gemini fails
                    repository.insertFlashcard(Flashcard(topicId = topicId, frontText = "Definition: Key Concept", backText = "Details: " + noteText.take(60) + "..."))
                    repository.insertFlashcard(Flashcard(topicId = topicId, frontText = "Concept Application", backText = "How to apply: " + noteText.take(45) + "..."))
                    onFinished("Generated 2 cards from notes via smart local parsing.")
                } else {
                    onFinished("Successfully generated $cardsCreated revision flashcards using Gemini API!")
                }
            } catch (e: Exception) {
                // Heuristic Fallback
                repository.insertFlashcard(Flashcard(topicId = topicId, frontText = "Definition: Note summary", backText = noteText.take(60) + "..."))
                onFinished("Offline fallback flashcard created.")
            }
            _aiIsLoading.value = false
        }
    }


    // --- Practice Arena MCQ Methods ---
    fun generateInteractiveQuiz(subjectId: Long) {
        val mockQuizzes = mapOf(
            "Quantum Physics" to listOf(
                QuizItem(
                    "Which particle represents the quantum unit of light?",
                    listOf("Electron", "Photon", "Proton", "Neutron"),
                    1,
                    "A photon is an elementary particle that is the quantum of the electromagnetic field, including light."
                ),
                QuizItem(
                    "What does de Broglie's hypothesis associate with moving physical matter?",
                    listOf("Infinite energy", "A wave function", "Absolute space", "Perfect friction"),
                    1,
                    "de Broglie proposed that any moving matter particles display pilot-wave behavior with λ = h/mv."
                ),
                QuizItem(
                    "Planck's constant relates a photon's energy strictly to its relative:",
                    listOf("Mass", "Velocity", "Frequency", "Amplitude"),
                    2,
                    "Energy equals Planck's constant times frequency (E = hf)."
                )
            ),
            "Organic Chemistry" to listOf(
                QuizItem(
                    "Which is the primary intermediate formed during electrophilic addition across alkenes?",
                    listOf("Carbanion", "Carbocation", "Free Radical", "Transition Complex"),
                    1,
                    "Markovnikov electrophilic addition forms a stable carbocation intermediate in the rate-determining step."
                ),
                QuizItem(
                    "Markovnikov's rule asserts that the nucleophile adds to which relative carbon of the double bond?",
                    listOf("The carbon with more alkyl substituents", "The carbon with more hydrogens", "The terminal carbon always", "Any carbon symmetrically"),
                    0,
                    "The electrophilic H adds to the carbon with more hydrogens, meaning the nucleophilic halogen adds to the carbon with more alkyl groups."
                )
            ),
            "Computer Science" to listOf(
                QuizItem(
                    "What describes the sorted element order of a Binary Search Tree (BST) in-order traversal?",
                    listOf("Randomly ordered", "Descending scale", "Ascending scale", "Level-by-level breadth"),
                    2,
                    "In-order traversal visits: Left subtree, Root node, then Right subtree, returning values in strictly sorted ascending order."
                ),
                QuizItem(
                    "What is the average time complexity of performing lookups in a balanced BST containing N elements?",
                    listOf("O(1)", "O(log N)", "O(N)", "O(N log N)"),
                    1,
                    "Balanced trees half their search space during every step, producing an average O(log N) lookup path."
                )
            )
        )

        // Find subject name
        viewModelScope.launch {
            val subList = subjects.value
            val match = subList.find { it.id == subjectId }
            val subTitle = match?.title ?: "Quantum Physics"
            
            val quizItems = mockQuizzes[subTitle] ?: mockQuizzes["Quantum Physics"]!!
            _quizQuestions.value = quizItems
            _quizCurrentIndex.value = 0
            _quizSelectedAnswerIndex.value = null
            _quizAnswersChecked.value = false
            _quizScore.value = 0
            _quizIsActive.value = true
        }
    }

    fun checkAndSubmitAnswer(answerIndex: Int) {
        _quizSelectedAnswerIndex.value = answerIndex
        _quizAnswersChecked.value = true
        val currentQuestion = _quizQuestions.value[_quizCurrentIndex.value]
        if (answerIndex == currentQuestion.correctIndex) {
            _quizScore.value = _quizScore.value + 1
        }
    }

    fun nextQuizStep() {
        val nextIdx = _quizCurrentIndex.value + 1
        if (nextIdx < _quizQuestions.value.size) {
            _quizCurrentIndex.value = nextIdx
            _quizSelectedAnswerIndex.value = null
            _quizAnswersChecked.value = false
        } else {
            // End Quiz Session
            _quizIsActive.value = false
            // Add a mock study session for completing this test successfully!
            viewModelScope.launch {
                repository.insertSession(StudySession(subjectId = 1, durationMinutes = 15))
            }
        }
    }

    fun exitPractice() {
        _quizIsActive.value = false
    }

    override fun onCleared() {
        timerJob?.cancel()
        musicJob?.cancel()
        ImmersiveAudioEngine.stop()
        super.onCleared()
    }
}
