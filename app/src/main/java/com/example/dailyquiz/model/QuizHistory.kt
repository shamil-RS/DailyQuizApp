package com.example.dailyquiz.model

import java.util.Date

data class QuizHistory(
    val id: String,
    val timestamp: Date,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val source: String,
    val quizTitle: String? = null,
    val answers: List<AnswerDetail>
)

data class AnswerDetail(
    val question: String,
    val userAnswer: String,
    val options: List<String>,
    val correctAnswer: String,
    val isCorrect: Boolean
) 