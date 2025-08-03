package com.example.dailyquiz.model

data class Quiz(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class OnlineQuiz(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: Int? = null
)