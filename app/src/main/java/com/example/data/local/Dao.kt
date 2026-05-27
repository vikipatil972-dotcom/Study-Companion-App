package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY title ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)


    // --- Topics ---
    @Query("SELECT * FROM topics ORDER BY id ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId")
    fun getTopicsForSubject(subjectId: Long): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getTopicById(id: Long): Topic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)


    // --- Questions (Important Syllabus QA) ---
    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    fun getQuestionsForTopic(topicId: Long): Flow<List<Question>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)


    // --- Flashcards (Revision Cards) ---
    @Query("SELECT * FROM flashcards ORDER BY id DESC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun getFlashcardsForTopic(topicId: Long): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Query("UPDATE flashcards SET mastered = :mastered WHERE id = :id")
    suspend fun updateFlashcardMastery(id: Long, mastered: Boolean)

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)


    // --- Study Sessions ---
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession): Long
}
