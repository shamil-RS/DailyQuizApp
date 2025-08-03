package com.example.dailyquiz.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dailyquiz.data.room.quiz_question.QuizQuestionDao
import com.example.dailyquiz.data.room.quiz_question.QuizQuestionEntity

@Database(entities = [QuizQuestionEntity::class], version = 1)
abstract class DailyQuizDatabase : RoomDatabase() {
    abstract fun quizQuestionDao() : QuizQuestionDao
}