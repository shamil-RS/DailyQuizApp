package com.example.dailyquiz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizResponse(
    @SerialName("response_code")
    val responseCode: Int,
    val results: List<QuizQuestion>
)

@Serializable
data class QuizQuestion(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("incorrect_answers")
    val incorrectAnswers: List<String>
)

enum class TriviaCategory(val id: Int, val displayName: String) {
    Music(12, "Entertainment: Music"),
    Sports(21, "Sports"),
    Geography(22, "Geography"),
    History(23, "History")
}
