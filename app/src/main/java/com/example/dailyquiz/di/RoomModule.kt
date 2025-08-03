package com.example.dailyquiz.di

import android.content.Context
import androidx.room.Room
import com.example.dailyquiz.data.room.DailyQuizDatabase
import com.example.dailyquiz.data.room.quiz_question.QuizQuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Provides
    @Singleton
    fun providesAppDatabase(
        @ApplicationContext context: Context
    ): DailyQuizDatabase = Room.databaseBuilder(
        context, DailyQuizDatabase::class.java, "app_database"
    ).fallbackToDestructiveMigration(false).build()

    @Provides
    @Singleton
    fun providesCharacterDao(
        database: DailyQuizDatabase
    ): QuizQuestionDao = database.quizQuestionDao()
}