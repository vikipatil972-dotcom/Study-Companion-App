package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val colorHex: String = "#4F46E5",
    val iconName: String = "Book",
    val category: String = "Science" // Weak Subject tracker or tagging
)

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val noteText: String = "",
    val noteAudioPath: String? = null,
    val isPrivate: Boolean = false,
    val progress: Int = 0 // 0 to 100
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val questionText: String,
    val answerText: String,
    val isLong: Boolean = false,
    val importanceLevel: String = "REGULAR" // "REGULAR", "IMPORTANT", "V_IMPORTANT"
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val frontText: String,
    val backText: String,
    val mastered: Boolean = false
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
