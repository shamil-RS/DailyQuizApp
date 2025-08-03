package com.example.dailyquiz

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.dailyquiz.model.AnswerDetail
import com.example.dailyquiz.model.QuizHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.UUID

class ScoreManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "quiz_scores", Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveHighScore(percentage: Int) {
        val currentHighScore = getHighScore()
        if (percentage > currentHighScore) {
            sharedPreferences.edit { putInt("high_score_percentage", percentage) }
        }
    }

    fun getHighScore(): Int {
        return sharedPreferences.getInt("high_score_percentage", 0)
    }

    fun clearHighScore() {
        sharedPreferences.edit { remove("high_score_percentage") }
    }

    fun saveQuizHistory(
        score: Int,
        totalQuestions: Int,
        percentage: Int,
        source: String,
        quizTitle: String? = null,
        answers: List<AnswerDetail>
    ) {
        val history = QuizHistory(
            id = UUID.randomUUID().toString(),
            timestamp = Date(),
            score = score,
            totalQuestions = totalQuestions,
            percentage = percentage,
            source = source,
            quizTitle = quizTitle,
            answers = answers
        )

        val histories = getQuizHistories().toMutableList()
        histories.add(0, history)

        val json = gson.toJson(histories)
        sharedPreferences.edit { putString("quiz_histories", json) }
    }

    fun getQuizHistories(): List<QuizHistory> {
        val json = sharedPreferences.getString("quiz_histories", "[]")
        val type = object : TypeToken<List<QuizHistory>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun clearQuizHistories() {
        sharedPreferences.edit { remove("quiz_histories") }
    }

    fun deleteQuizHistoryById(id: String) {
        val updatedHistories = getQuizHistories().filter { it.id != id }
        val json = gson.toJson(updatedHistories)
        sharedPreferences.edit { putString("quiz_histories", json) }
    }
} 