package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.Subject
import com.example.data.local.Topic
import com.example.data.local.Flashcard
import com.example.data.local.Question
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val isUserRegistered by viewModel.isUserRegistered.collectAsStateWithLifecycle()

    if (!isUserRegistered) {
        OnboardingScreen(viewModel = viewModel)
    } else {
        val subjects by viewModel.subjects.collectAsStateWithLifecycle()
        val topics by viewModel.topics.collectAsStateWithLifecycle()
        val questions by viewModel.questions.collectAsStateWithLifecycle()
        val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
        val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()

        // Screen Tabs: 0 = Dashboard, 1 = Subjects & Labs, 2 = Notes & Cards, 3 = AI Doubt Solver, 4 = Practice, 5 = Study Space
        var activeTab by remember { mutableStateOf(0) }

        // Navigation Drawer or standard BottomAppBar structure for seamless transitions
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = CosmicSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_bottom_nav")
                ) {
                    val navItems = listOf(
                        Triple(0, Icons.Filled.Dashboard, "Dashboard"),
                        Triple(1, Icons.Filled.MenuBook, "Syllabus"),
                        Triple(2, Icons.Filled.Style, "Flashcards"),
                        Triple(3, Icons.Filled.AutoAwesome, "AI Assistant"),
                        Triple(4, Icons.Filled.Checklist, "Practice"),
                        Triple(5, Icons.Filled.VideoCall, "Study Room")
                    )
                    navItems.forEach { item ->
                        val index = item.first
                        val icon = item.second
                        val label = item.third
                        NavigationBarItem(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CosmicBackground,
                                selectedTextColor = CosmicTertiary,
                                indicatorColor = CosmicTertiary,
                                unselectedIconColor = CosmicOnSurface.copy(alpha = 0.5f),
                                unselectedTextColor = CosmicOnSurface.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmicBackground) // Solid Dark Base
                    .drawBehind {
                        // Circle 1: Top-left Indigo Mesh Glow (Frosted Glass Theme Accent)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x3B6366F1), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(-40.dp.toPx(), -40.dp.toPx()),
                                radius = 280.dp.toPx()
                            ),
                            radius = 280.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(-40.dp.toPx(), -40.dp.toPx())
                        )
                        // Circle 2: Right Middle Purple Mesh Glow (Frosted Glass Theme Accent)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x278B5CF6), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width + 60.dp.toPx(), size.height / 2f),
                                radius = 320.dp.toPx()
                            ),
                            radius = 320.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(size.width + 60.dp.toPx(), size.height / 2f)
                        )
                        // Circle 3: Bottom Left Blue/Cyan Mesh Glow (Frosted Glass Theme Accent)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x2A38BDF8), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height + 30.dp.toPx()),
                                radius = 300.dp.toPx()
                            ),
                            radius = 300.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height + 30.dp.toPx())
                        )
                    }
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenSwitcher"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardTab(viewModel, studySessions, subjects)
                        1 -> SubjectsSyllabusTab(viewModel, subjects, topics)
                        2 -> NotesFlashcardsTab(viewModel, subjects, topics, flashcards)
                        3 -> AiDoubtSolverTab(viewModel)
                        4 -> PracticeArenaTab(viewModel, subjects)
                        5 -> CollaborativeStudySpaceTab(viewModel)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 1️⃣ DASHBOARD TAB
// ---------------------------------------------------------
@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    sessions: List<com.example.data.local.StudySession>,
    subjects: List<Subject>
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val timerSecs by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val timerIsActive by viewModel.timerIsActive.collectAsStateWithLifecycle()
    val timerMaxSecs by viewModel.timerMaxSeconds.collectAsStateWithLifecycle()

    val isPlaying by viewModel.musicIsPlaying.collectAsStateWithLifecycle()
    val trackProgress by viewModel.musicTrackProgress.collectAsStateWithLifecycle()
    val activeTrack by viewModel.activeTrack.collectAsStateWithLifecycle()
    val musicCategory by viewModel.selectedMusicCategory.collectAsStateWithLifecycle()

    // Calculated totals
    val totalMinutes = sessions.sumOf { it.durationMinutes }
    val totalSessionsCount = sessions.size

    val textFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val todayStr = remember { textFormat.format(Date()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Greeting
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = userName,
                        fontSize = 13.sp,
                        color = CosmicTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aesthetic Study Hub",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Focus Period • $todayStr",
                        fontSize = 11.sp,
                        color = CosmicOnSurface.copy(alpha = 0.6f)
                    )
                }

                // Daily Streak Badge
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AccentOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "5 Days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange
                        )
                    }
                }
            }
        }

        // Exam Countdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = BorderStroke(1.dp, CosmicTertiary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SEMESTER CO-FINALS COUNTDOWN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CosmicTertiary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "14 Days : 06h : 30m",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Target Goal: Minimum 4.0 GPA • Keep pushing!",
                            fontSize = 11.sp,
                            color = CosmicOnSurface.copy(alpha = 0.6f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(AccentPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Countdown clock",
                            tint = AccentPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Stats Dashboard Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Studied Minutes", fontSize = 11.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                        Text("$totalMinutes mins", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CosmicTertiary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Goal: 120 mins daily", fontSize = 9.sp, color = CosmicOnSurface.copy(alpha = 0.4f))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Sessions Finished", fontSize = 11.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                        Text("$totalSessionsCount sets", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentPink)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Consistent performance", fontSize = 9.sp, color = CosmicOnSurface.copy(alpha = 0.4f))
                    }
                }
            }
        }

        // Pomodoro / Study Focus Timer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FOCUS TIME CONTROLLER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timer Circular View
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = timerSecs.toFloat() / timerMaxSecs.toFloat(),
                            modifier = Modifier.size(136.dp),
                            color = CosmicSecondary,
                            strokeWidth = 8.dp,
                            trackColor = CosmicBackground
                        )

                        val mins = timerSecs / 60
                        val secs = timerSecs % 60
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (timerIsActive) "STAY FOCUSED" else "PAUSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timerIsActive) AccentGreen else CosmicOnSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Duration Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        listOf(10, 25, 45, 60).forEach { mins ->
                            OutlinedButton(
                                onClick = { viewModel.setTimerDuration(mins) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("timer_preset_$mins"),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Text("${mins}m", fontSize = 11.sp, color = CosmicSecondary)
                            }
                        }
                    }

                    // Play/Pause & Reset buttons
                    Row {
                        Button(
                            onClick = { viewModel.toggleFocusTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (timerIsActive) AccentOrange else CosmicSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("timer_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (timerIsActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (timerIsActive) "Pause Session" else "Start Deep Focus")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.resetFocusTimer() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CosmicBackground)
                                .testTag("timer_reset_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateLeft,
                                contentDescription = "Reset Timer",
                                tint = CosmicOnBackground
                            )
                        }
                    }
                }
            }
        }

        // Motivation Music Player (Synced with Study Timer)
        item {
            Column {
                Text(
                    text = "DISCIPLINED BACKGROUND SOUNDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                MusicPlayerComponent(
                    isPlaying = isPlaying,
                    onTogglePlay = { viewModel.togglePlayMusic() },
                    trackProgress = trackProgress,
                    activeTrack = activeTrack,
                    onNextTrack = { viewModel.playNextTrack() },
                    onPrevTrack = { viewModel.playPrevTrack() },
                    onSelectCategory = { viewModel.selectMusicCategory(it) },
                    selectedCategory = musicCategory
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 2️⃣ SUBJECTS & SYLLABUS LAB TAB (With interactive animations!)
// ---------------------------------------------------------
@Composable
fun SubjectsSyllabusTab(
    viewModel: MainViewModel,
    subjects: List<Subject>,
    topics: List<Topic>
) {
    val questions by viewModel.questions.collectAsStateWithLifecycle()

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectTitle by remember { mutableStateOf("") }
    var newSubjectCategory by remember { mutableStateOf("Core Course") }
    var subjectTitleError by remember { mutableStateOf(false) }

    var selectedSubjectDetails by remember { mutableStateOf<Subject?>(null) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var newTopicTitle by remember { mutableStateOf("") }
    var newTopicSummary by remember { mutableStateOf("") }

    if (selectedSubjectDetails == null) {
        // Grid View of All Subjects
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CUSTOM SYLLABUS BUILDER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicTertiary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Your Subjects & Syllabus",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Button(
                        onClick = { showAddSubjectDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicTertiary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_subject_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Subject Cards List
            if (subjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subjects found. Click Add Subject dynamically above to start!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CosmicOnSurface.copy(alpha = 0.5f)),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(subjects) { sub ->
                    val color = remember(sub.colorHex) { 
                        try { Color(android.graphics.Color.parseColor(sub.colorHex)) } catch (e: Exception) { CosmicPrimary }
                    }
                    val currentSubTopics = topics.filter { it.subjectId == sub.id }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSubjectDetails = sub }
                            .testTag("subject_card_${sub.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (sub.iconName == "Science") Icons.Default.Science else Icons.Default.Code,
                                        contentDescription = "Subject Icon",
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = sub.category.uppercase(Locale.ROOT),
                                        fontSize = 9.sp,
                                        color = color,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sub.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${currentSubTopics.size} units in syllabus",
                                        fontSize = 12.sp,
                                        color = CosmicOnSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Syllabus Completeness Indicator
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = 0.65f,
                                    modifier = Modifier.size(36.dp),
                                    color = color,
                                    strokeWidth = 3.dp,
                                    trackColor = CosmicBackground
                                )
                                Text(
                                    text = "65%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // Integration of custom Animation Labs on general subjects
            item {
                Text(
                    text = "INTEGRATED SYSTEM CONCEPT LABS (PROCESS SIMULATIONS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                ConceptAnimator()
            }
        }
    } else {
        // Detailed Syllabus View of a Selected Subject
        val activeSubject = selectedSubjectDetails!!
        val currentTopics = topics.filter { it.subjectId == activeSubject.id }
        val subjectColor = remember(activeSubject.colorHex) {
            try { Color(android.graphics.Color.parseColor(activeSubject.colorHex)) } catch (e: Exception) { CosmicPrimary }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedSubjectDetails = null },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicSurface)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(activeSubject.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        Text("Syllabus Breakdown & Lesson Tracker", fontSize = 11.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                    }
                }
            }

            // Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Units Registered", fontSize = 11.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                            Text("${currentTopics.size} Topics Active", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = { showAddTopicDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = subjectColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_topic_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Topic", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Topic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Topics list
            if (currentTopics.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No syllabus topics found or mapped yet. Click New Topic above to insert modules!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CosmicOnSurface.copy(alpha = 0.5f)),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(currentTopics) { topic ->
                    TopicDetailCard(
                        topic = topic,
                        questions = questions,
                        viewModel = viewModel,
                        subjectColor = subjectColor
                    )
                }
            }
        }
    }

    // Dialogue: Add Subject
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddSubjectDialog = false 
                newSubjectTitle = ""
                newSubjectCategory = "Core Course"
                subjectTitleError = false
            },
            title = { Text("Create New Subject", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = CosmicSurface,
            text = {
                Column {
                    OutlinedTextField(
                        value = newSubjectTitle,
                        onValueChange = { 
                            newSubjectTitle = it
                            if (it.trim().isNotEmpty()) {
                                subjectTitleError = false
                            }
                        },
                        label = { Text("Subject Title (e.g. Physics)") },
                        isError = subjectTitleError,
                        supportingText = {
                            if (subjectTitleError) {
                                Text("Subject title cannot be empty", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (subjectTitleError) MaterialTheme.colorScheme.error else CosmicTertiary,
                            focusedLabelColor = if (subjectTitleError) MaterialTheme.colorScheme.error else CosmicTertiary,
                            unfocusedBorderColor = if (subjectTitleError) MaterialTheme.colorScheme.error else CosmicOnSurface.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_subject_title_field")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSubjectCategory,
                        onValueChange = { newSubjectCategory = it },
                        label = { Text("Course Tag / Category (e.g. Core Course)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicTertiary,
                            focusedLabelColor = CosmicTertiary,
                            unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_subject_category_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectTitle.trim().isEmpty()) {
                            subjectTitleError = true
                        } else {
                            viewModel.addSubject(
                                title = newSubjectTitle.trim(),
                                colorHex = "#818CF8",
                                iconName = "Science",
                                category = newSubjectCategory.trim().ifEmpty { "Core Course" }
                            )
                            newSubjectTitle = ""
                            newSubjectCategory = "Core Course"
                            subjectTitleError = false
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicTertiary),
                    modifier = Modifier.testTag("add_subject_confirm_btn")
                ) {
                    Text("Register Subject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddSubjectDialog = false 
                        newSubjectTitle = ""
                        newSubjectCategory = "Core Course"
                        subjectTitleError = false
                    }
                ) {
                    Text("Cancel", color = CosmicOnSurface)
                }
            }
        )
    }

    // Dialogue: Add Topic
    if (showAddTopicDialog && selectedSubjectDetails != null) {
        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Add Topic to Syllabus", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = CosmicSurface,
            text = {
                Column {
                    OutlinedTextField(
                        value = newTopicTitle,
                        onValueChange = { newTopicTitle = it },
                        label = { Text("Topic Title (e.g. Laser Physics)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            focusedLabelColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_topic_title_field")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTopicSummary,
                        onValueChange = { newTopicSummary = it },
                        label = { Text("Initial Short Notes / Summary") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            focusedLabelColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_topic_summary_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTopicTitle.isNotEmpty()) {
                            viewModel.addTopic(
                                subjectId = selectedSubjectDetails!!.id,
                                title = newTopicTitle,
                                noteText = newTopicSummary
                            )
                            newTopicTitle = ""
                            newTopicSummary = ""
                            showAddTopicDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                    modifier = Modifier.testTag("add_topic_confirm_btn")
                ) {
                    Text("Append Topic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancel", color = CosmicOnSurface)
                }
            }
        )
    }
}

// ---------------------------------------------------------
// 3️⃣ SMART NOTES & FLASHCARDS REVISION TAB
// ---------------------------------------------------------
@Composable
fun NotesFlashcardsTab(
    viewModel: MainViewModel,
    subjects: List<Subject>,
    topics: List<Topic>,
    flashcards: List<Flashcard>
) {
    var selectedTopicNotes by remember { mutableStateOf<Topic?>(null) }
    var notesDraftText by remember { mutableStateOf("") }
    
    // Voice Simulator status triggers
    var isRecordingAudioNote by remember { mutableStateOf(false) }

    // Flashcard reviewer UI status
    var flippedCardId by remember { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedTopicNotes == null) {
            // Pick notes topic to study
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "SMART REVISION ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Core Notes & Active Cards",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Write typed notes, record quick voice memos, and generate interactive revision flashcards via AI helpers.",
                        fontSize = 11.sp,
                        color = CosmicOnSurface.copy(alpha = 0.5f)
                    )
                }

                if (topics.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active notes. Map a syllabus topic inside Syllabus tab first to write notes here!",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CosmicOnSurface.copy(alpha = 0.5f)),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(topics) { topic ->
                        val subMatch = subjects.find { it.id == topic.subjectId }
                        val cardsCount = flashcards.filter { it.topicId == topic.id }.size

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTopicNotes = topic
                                    notesDraftText = topic.noteText
                                }
                                .testTag("topic_notes_card_${topic.id}"),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = subMatch?.title?.uppercase(Locale.ROOT) ?: "SYLLABUS MODULE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicTertiary
                                    )
                                    Text(
                                        text = topic.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Style,
                                            contentDescription = "Flashcard Icon",
                                            tint = CosmicSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$cardsCount revision flashcards generated",
                                            fontSize = 12.sp,
                                            color = CosmicOnSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Filled.EditNote,
                                    contentDescription = "Explore notes",
                                    tint = CosmicSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Active Notes Editor Pane & Flashcards revision
            val topic = selectedTopicNotes!!
            val topicCards = flashcards.filter { it.topicId == topic.id }
            val subMatch = subjects.find { it.id == topic.subjectId }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header back
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { selectedTopicNotes = null },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = subMatch?.title ?: "Syllabus Module",
                                    fontSize = 11.sp,
                                    color = CosmicOnSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = topic.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Smart Typed Notes Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "TYPED LECTURE NOTES SUMMARY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = notesDraftText,
                                    onValueChange = { notesDraftText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("notes_text_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CosmicSecondary,
                                        unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    placeholder = { Text("Start typing notes, details or formulas...") }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Voice note recorder simulation
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = { isRecordingAudioNote = !isRecordingAudioNote },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isRecordingAudioNote) AccentRed else CosmicBackground
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp).testTag("voice_note_rec_btn")
                                        ) {
                                            Icon(
                                                imageVector = if (isRecordingAudioNote) Icons.Default.MicOff else Icons.Default.Mic,
                                                contentDescription = "Voice note icon",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isRecordingAudioNote) "Recording..." else "Voice Memo",
                                                fontSize = 11.sp,
                                                color = if (isRecordingAudioNote) Color.White else CosmicOnSurface
                                            )
                                        }
                                        if (isRecordingAudioNote) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            LinearProgressIndicator(
                                                modifier = Modifier.width(60.dp).height(4.dp),
                                                color = AccentRed
                                            )
                                        }
                                    }

                                    // Save changes button
                                    Button(
                                        onClick = { 
                                            viewModel.saveNoteChanges(topic.id, notesDraftText)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Lesson notes successfully saved!")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp).testTag("save_notes_btn")
                                    ) {
                                        Text("Save Changes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Convert notes to flashcards generator (The "Converting Syllabus/Notes -> Q&A format" Core Engine!)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, CosmicTertiary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AI FLASHCARD SYLLABUS GENERATOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CosmicTertiary)
                                        Text("Convert lecture notes into revision cards instantly.", fontSize = 11.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.triggerAiConvertToFlashcards(topic.id, notesDraftText) { msg ->
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(msg)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicTertiary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("ai_cards_generate_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Generate", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Extract", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Revision Cards Grid List
                    item {
                        Text(
                            text = "ACTIVE REVISION CARDS FOR RETENTION (${topicCards.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicOnSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (topicCards.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No flashcards here yet. Click the 'AI Extract' helper button above to form flashcards dynamically from your notes!",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CosmicOnSurface.copy(alpha = 0.4f)),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(topicCards) { card ->
                            val isFlipped = flippedCardId == card.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { flippedCardId = if (isFlipped) null else card.id }
                                    .testTag("flashcard_${card.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFlipped) CosmicSecondary.copy(alpha = 0.15f) else CosmicSurface
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isFlipped) CosmicSecondary else CosmicOnSurface.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (isFlipped) "REVISION ANSWER / CONCEPT" else "REVISION QUESTION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFlipped) CosmicSecondary else CosmicTertiary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isFlipped) card.backText else card.frontText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isFlipped) "Tap to hide answer" else "Tap card to flip & reveal",
                                            fontSize = 10.sp,
                                            color = CosmicOnSurface.copy(alpha = 0.4f)
                                        )

                                        IconButton(
                                            onClick = { viewModel.deleteFlashcard(card) },
                                            modifier = Modifier.size(24.dp).testTag("delete_flashcard_${card.id}")
                                        ) {
                                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete card", tint = AccentRed.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 4️⃣ AI DOUBLE SOLVER / ACADEMIC ASSISTANT TAB
// ---------------------------------------------------------
@Composable
fun AiDoubtSolverTab(viewModel: MainViewModel) {
    val history by viewModel.aiHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.aiIsLoading.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }

    val shortcuts = listOf(
        "Explain de Broglie hypothesis simply for kid level",
        "Derive Markovnikov carbocation stability step-by-step",
        "Explain Average time complex of balanced BST search",
        "Synthesize an urgent exam prep timetable for Weak physics"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI ACADEMIC CO-ENGINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTertiary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Doubt Solver & Syllabus Explainer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.clearAiHistory() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CosmicSurface)
                    .testTag("ai_clear_history_btn")
            ) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear logs", tint = CosmicOnSurface)
            }
        }

        // Active Chat Log Frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CosmicSurface)
                .border(1.dp, CosmicSecondary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                history.forEach { msg ->
                    val isAi = msg.sender == "AI Assistant"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAi) 0.dp else 16.dp,
                                        bottomEnd = if (isAi) 16.dp else 0.dp
                                    )
                                )
                                .background(if (isAi) CosmicBackground else CosmicSecondary.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    if (isAi) CosmicSecondary.copy(alpha = 0.1f) else CosmicSecondary.copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = msg.sender.uppercase(Locale.ROOT),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAi) CosmicTertiary else CosmicSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CosmicTertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini is analyzing syllabus notes & formulating answers...", fontSize = 11.sp, color = CosmicTertiary)
                    }
                }
            }

            // Scroll down anchor effect
            LaunchedEffect(history.size) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pre-defined doubt prompts (The "Generate custom syllabus exam sure answers!")
        Text(
            text = "TAP QUICK SHORTCUT PROMPT QUESTION:",
            fontSize = 9.sp,
            color = CosmicOnSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shortcuts.forEach { phrase ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicSurface)
                        .clickable { viewModel.submitAiQuestion(phrase) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("ai_shortcut_${phrase.take(15)}")
                ) {
                    Text(text = phrase, fontSize = 11.sp, color = CosmicOnSurface)
                }
            }
        }

        // Active text entry bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("ai_entry_bar"),
                placeholder = { Text("Enter topic/doubt here to discuss...", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicTertiary,
                    unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputQuery.trim().isNotEmpty()) {
                        viewModel.submitAiQuestion(inputQuery.trim())
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicTertiary)
                    .testTag("ai_submit_btn"),
                enabled = !isLoading
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Submit question", tint = CosmicBackground)
            }
        }
    }
}

// ---------------------------------------------------------
// 5️⃣ PRACTICE arena TAB (Mock exams and MCQs)
// ---------------------------------------------------------
@Composable
fun PracticeArenaTab(viewModel: MainViewModel, subjects: List<Subject>) {
    val quizActive by viewModel.quizIsActive.collectAsStateWithLifecycle()
    val quizItems by viewModel.quizQuestions.collectAsStateWithLifecycle()
    val currentIndex by viewModel.quizCurrentIndex.collectAsStateWithLifecycle()
    val selectedAnsIdx by viewModel.quizSelectedAnswerIndex.collectAsStateWithLifecycle()
    val answeredChecked by viewModel.quizAnswersChecked.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!quizActive) {
            // Pick subject mock exam lobby
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text(
                        text = "EXAM PREPARATION UNIT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Practice Arena & Mock Finals",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Take interactive MCQ unit assessments to pinpoint weak areas. Your progress integrates with daily timetable scheduler priorities.",
                        fontSize = 11.sp,
                        color = CosmicOnSurface.copy(alpha = 0.5f)
                    )
                }

                item {
                    Text(
                        text = "SELECT REGISTERED SUBJECT TO START ASSESSMENT:",
                        fontSize = 9.sp,
                        color = CosmicOnSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (subjects.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                        ) {
                            Text(
                                "No active courses to test. Register classes under Syllabus tab to unlock diagnostics",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(subjects) { sub ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sub.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(sub.category, fontSize = 12.sp, color = CosmicOnSurface.copy(alpha = 0.6f))
                                }

                                Button(
                                    onClick = { viewModel.generateInteractiveQuiz(sub.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicTertiary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("start_test_${sub.title}")
                                ) {
                                    Text("Simulate MCQ Exam", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // AI DIAGNOSTIC INSIGHT CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        border = BorderStroke(1.dp, CustomThemeColors.glowAqua.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.QueryStats, contentDescription = "Stats", tint = CosmicTertiary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aesthetic AI Subject Diagnostics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Latest Diagnostics suggest priorities in: Organic Chemistry (Carbon Addition). Physical concepts are stable. Continue reviewing flashcards for 10 minutes prior to exam.",
                                fontSize = 12.sp,
                                color = CosmicOnSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            // Active Quiz Module HUD
            val currentItem = quizItems[currentIndex]

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header progress
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${currentIndex + 1} OF ${quizItems.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicTertiary
                        )

                        TextButton(
                            onClick = { viewModel.exitPractice() },
                            modifier = Modifier.testTag("exit_practice_btn")
                        ) {
                            Text("Quit Assessment", color = AccentRed)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / quizItems.size.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = CosmicTertiary,
                        trackColor = CosmicSurface
                    )
                }

                // Question Plate
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = currentItem.question,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                // Answer Options
                items(currentItem.options.size) { optIdx ->
                    val optionText = currentItem.options[optIdx]
                    val isSelected = selectedAnsIdx == optIdx
                    val isCorrect = currentItem.correctIndex == optIdx

                    val surfaceColor = when {
                        answeredChecked && isCorrect -> AccentGreen.copy(alpha = 0.2f)
                        answeredChecked && isSelected && !isCorrect -> AccentRed.copy(alpha = 0.2f)
                        isSelected -> CosmicSecondary.copy(alpha = 0.25f)
                        else -> CosmicSurface
                    }

                    val borderColor = when {
                        answeredChecked && isCorrect -> AccentGreen
                        answeredChecked && isSelected && !isCorrect -> AccentRed
                        isSelected -> CosmicSecondary
                        else -> CosmicOnSurface.copy(alpha = 0.05f)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !answeredChecked) {
                                viewModel.checkAndSubmitAnswer(optIdx)
                            }
                            .testTag("option_$optIdx"),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) CosmicTertiary else CosmicBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + optIdx).toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CosmicBackground else CosmicOnSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = optionText, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                // Answer Feedback Explanation Frame
                if (answeredChecked) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (selectedAnsIdx == currentItem.correctIndex) "✓ Correct Answer!" else "❌ Incorrect.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedAnsIdx == currentItem.correctIndex) AccentGreen else AccentRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentItem.explanation,
                                    fontSize = 12.sp,
                                    color = CosmicOnSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.nextQuizStep() },
                                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("quiz_next_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicTertiary)
                                ) {
                                    Text(
                                        text = if (currentIndex == quizItems.size - 1) "Finish Exam & Report" else "Proceed Next Question",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 6️⃣ COLLABORATIVE STUDY SPACE (Video Study Lobby & Whiteboard)
// ---------------------------------------------------------
@Composable
fun CollaborativeStudySpaceTab(viewModel: MainViewModel) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val strokes = viewModel.whiteboardStrokes
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space Header
        item {
            Column {
                Text(
                    text = "COLLABORATIVE VIRTUAL STUDY ROOM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTertiary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Quantum Prep Lobby #4",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Collaborate with peers, screen-share equations, and record concept drawings synchronously with zero distractions.",
                    fontSize = 11.sp,
                    color = CosmicOnSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Virtual Video Call Members Grid (Simulated Live Avatars!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Self frame
                Card(
                    modifier = Modifier.weight(1.0f).height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, CosmicTertiary)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Active Self", tint = CosmicTertiary, modifier = Modifier.size(28.dp))
                            Text("$userName (You)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(if (isMuted) "🔇 Muted" else "🎙️ Speaking", fontSize = 10.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                        }
                    }
                }

                // Peer Frame 1
                Card(
                    modifier = Modifier.weight(1.0f).height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Face, contentDescription = "Peer 1", tint = CosmicSecondary, modifier = Modifier.size(28.dp))
                            Text("Ananya R.", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Studying hard", fontSize = 10.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                        }
                    }
                }

                // Peer Frame 2
                Card(
                    modifier = Modifier.weight(1.0f).height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Face2, contentDescription = "Peer 2", tint = AccentPink, modifier = Modifier.size(28.dp))
                            Text("Siddharth M", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Muted", fontSize = 10.sp, color = CosmicOnSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Shared whiteboard visualizer
        item {
            WhiteboardComponent(
                strokes = strokes,
                onAddStroke = { viewModel.addWhiteboardStroke(it) },
                onClear = { viewModel.clearWhiteboard() }
            )
        }

        // Quick Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMuted) AccentRed.copy(alpha = 0.2f) else CosmicBackground)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) AccentRed else Color.White
                        )
                    }

                    // Turn off Camera
                    IconButton(
                        onClick = { isCameraOff = !isCameraOff },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCameraOff) AccentRed.copy(alpha = 0.2f) else CosmicBackground)
                    ) {
                        Icon(
                            imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Cam Toggle",
                            tint = if (isCameraOff) AccentRed else Color.White
                        )
                    }

                    // Simulated Screen Sharing toggle
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Share board screen",
                            tint = CosmicTertiary
                        )
                    }

                    // Hang up room back to space
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disconnect")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// ESTABLISHED MODERN PALETTE UTILS
// ---------------------------------------------------------
object CustomThemeColors {
    val glowIndigo = Color(0xFF6366F1)
    val glowAqua = Color(0xFF0EA5E9)
}

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val hasAccount = remember { viewModel.isAccountCreated() }
    var isLoginMode by remember { mutableStateOf(hasAccount) }

    var name by remember { mutableStateOf(if (isLoginMode) "" else "Vivek Patil") }
    var password by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground) // Match midnight blue space
            .drawBehind {
                // Circle 1: Top-left Indigo Mesh Glow (Frosted Glass Theme Accent)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3B6366F1), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(-40.dp.toPx(), -40.dp.toPx()),
                        radius = 280.dp.toPx()
                    ),
                    radius = 280.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(-40.dp.toPx(), -40.dp.toPx())
                )
                // Circle 2: Right Middle Purple Mesh Glow (Frosted Glass Theme Accent)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x278B5CF6), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width + 60.dp.toPx(), size.height / 2f),
                        radius = 320.dp.toPx()
                    ),
                    radius = 320.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width + 60.dp.toPx(), size.height / 2f)
                )
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .testTag("onboarding_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)) // Frosted translucent border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Academic Glass Icon Icon Glow
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1A6366F1))
                        .border(1.dp, Color(0x336366F1), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Academic School Icon",
                        tint = CosmicTertiary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Aesthetic Study Hub",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isLoginMode) "Sign in with your name and password to access your workspaces." else "Create your student credentials to proceed into personalized workspaces.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CosmicOnSurface.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )

                // Tab Switcher Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLoginMode) CosmicTertiary else Color.Transparent)
                            .clickable { 
                                isLoginMode = true
                                generalError = null 
                                nameError = false
                                passwordError = false
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log In",
                            color = if (isLoginMode) CosmicBackground else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isLoginMode) CosmicTertiary else Color.Transparent)
                            .clickable { 
                                isLoginMode = false
                                generalError = null 
                                nameError = false
                                passwordError = false
                                if (name.isEmpty()) name = "Vivek Patil"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            color = if (!isLoginMode) CosmicBackground else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // General Error Announcement
                if (generalError != null) {
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.trim().isNotEmpty()) {
                            nameError = false
                        }
                    },
                    label = { Text("User Name") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = "Name Icon", tint = CosmicTertiary)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Name is required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicTertiary,
                        unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.trim().isNotEmpty()) {
                            passwordError = false
                        }
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = "Password Icon", tint = CosmicTertiary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                tint = CosmicOnSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_password_input"),
                    isError = passwordError,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    supportingText = {
                        if (passwordError) {
                            Text("Password is required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicTertiary,
                        unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val isNameValid = name.trim().isNotEmpty()
                        val isPasswordValid = password.trim().isNotEmpty()
                        
                        nameError = !isNameValid
                        passwordError = !isPasswordValid

                        if (isNameValid && isPasswordValid) {
                            if (isLoginMode) {
                                val errorMsg = viewModel.loginUser(name, password)
                                if (errorMsg != null) {
                                    generalError = errorMsg
                                }
                            } else {
                                viewModel.registerUser(name, password)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_submit_button")
                ) {
                    Text(
                        text = if (isLoginMode) "Log In & Enter Space" else "Sign Up & Create Space",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TopicDetailCard(
    topic: Topic,
    questions: List<Question>,
    viewModel: MainViewModel,
    subjectColor: Color
) {
    var isEditingNotes by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf(topic.noteText) }

    var showAddQuestionInline by remember { mutableStateOf(false) }
    var newQuestionText by remember { mutableStateOf("") }
    var newAnswerText by remember { mutableStateOf("") }
    var questionError by remember { mutableStateOf(false) }
    var answerError by remember { mutableStateOf(false) }

    val topicQuestions = remember(questions, topic.id) {
        questions.filter { it.topicId == topic.id }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topic_detail_card_${topic.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Topic Icon",
                        tint = CosmicTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topic.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteTopic(topic) },
                    modifier = Modifier.size(32.dp).testTag("delete_topic_${topic.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Topic",
                        tint = AccentRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 Lesson Notes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTertiary
                    )

                    TextButton(
                        onClick = {
                            if (isEditingNotes) {
                                viewModel.saveNoteChanges(topic.id, notesDraft)
                            } else {
                                notesDraft = topic.noteText
                            }
                            isEditingNotes = !isEditingNotes
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = CosmicSecondary),
                        modifier = Modifier.height(28.dp).testTag("edit_notes_${topic.id}")
                    ) {
                        Text(
                            text = if (isEditingNotes) "Save Notes" else "Edit Notes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isEditingNotes) {
                    OutlinedTextField(
                        value = notesDraft,
                        onValueChange = { notesDraft = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_notes_field_${topic.id}"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f),
                            focusedContainerColor = CosmicBackground.copy(alpha = 0.5f),
                            unfocusedContainerColor = CosmicBackground.copy(alpha = 0.3f)
                        ),
                        placeholder = { Text("Write detailed instructions, lessons, or notes here...", fontSize = 13.sp) }
                    )
                } else {
                    Text(
                        text = topic.noteText.ifEmpty { "No notes or summary written yet. Click 'Edit Notes' to supplement lessons!" },
                        fontSize = 13.sp,
                        color = if (topic.noteText.isEmpty()) CosmicOnSurface.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Questions Block (Syllabus QA)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Important Syllabus Q&A",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicSecondary
                    )

                    TextButton(
                        onClick = { showAddQuestionInline = !showAddQuestionInline },
                        colors = ButtonDefaults.textButtonColors(contentColor = CosmicSecondary),
                        modifier = Modifier.height(28.dp).testTag("toggle_add_qa_${topic.id}")
                    ) {
                        Text(
                            text = if (showAddQuestionInline) "Hide Form" else "Add Q&A",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Question Inline Creator Form
                if (showAddQuestionInline) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Register Question & Answer", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)

                        OutlinedTextField(
                            value = newQuestionText,
                            onValueChange = { 
                                newQuestionText = it
                                if (it.trim().isNotEmpty()) questionError = false
                            },
                            label = { Text("Question Text (e.g. State Ohm's Law)", fontSize = 12.sp) },
                            isError = questionError,
                            modifier = Modifier.fillMaxWidth().testTag("add_question_field_${topic.id}"),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CosmicSecondary,
                                unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f)
                            )
                        )

                        OutlinedTextField(
                            value = newAnswerText,
                            onValueChange = { 
                                newAnswerText = it
                                if (it.trim().isNotEmpty()) answerError = false
                            },
                            label = { Text("Detailed Answer Text", fontSize = 12.sp) },
                            isError = answerError,
                            modifier = Modifier.fillMaxWidth().testTag("add_answer_field_${topic.id}"),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CosmicSecondary,
                                unfocusedBorderColor = CosmicOnSurface.copy(alpha = 0.3f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    newQuestionText = ""
                                    newAnswerText = ""
                                    questionError = false
                                    answerError = false
                                    showAddQuestionInline = false
                                }
                            ) {
                                Text("Discard", color = CosmicOnSurface, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val isQValid = newQuestionText.trim().isNotEmpty()
                                    val isAValid = newAnswerText.trim().isNotEmpty()
                                    questionError = !isQValid
                                    answerError = !isAValid

                                    if (isQValid && isAValid) {
                                        viewModel.addQuestion(
                                            topicId = topic.id,
                                            questionText = newQuestionText.trim(),
                                            answerText = newAnswerText.trim()
                                        )
                                        newQuestionText = ""
                                        newAnswerText = ""
                                        showAddQuestionInline = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("submit_qa_btn_${topic.id}")
                            ) {
                                Text("Add Important Q&A", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Questions Listing
                if (topicQuestions.isEmpty()) {
                    Text(
                        text = "No recorded study questions for this unit yet.",
                        fontSize = 12.sp,
                        color = CosmicOnSurface.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        topicQuestions.forEach { qa ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x05FFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0x05FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ Question:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicSecondary
                                    )

                                    IconButton(
                                        onClick = { viewModel.deleteQuestion(qa) },
                                        modifier = Modifier.size(24.dp).testTag("delete_qa_${qa.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Q&A",
                                            tint = AccentRed.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = qa.questionText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                // Solid Custom Divider (100% compilation bulletproof)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0x0AFFFFFF))
                                )

                                Text(
                                    text = "🔑 Standard Answer:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CosmicOnSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                                )

                                Text(
                                    text = qa.answerText,
                                    fontSize = 13.sp,
                                    color = CosmicOnSurface.copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
