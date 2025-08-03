package com.example.dailyquiz.data.room.quiz_question

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuizQuestionDao {
    @Query("SELECT * FROM quiz_questions LIMIT :amount")
    suspend fun getQuestions(amount: Int): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuizQuestionEntity>)

    @Query("DELETE FROM quiz_questions")
    suspend fun clearAll()
}
