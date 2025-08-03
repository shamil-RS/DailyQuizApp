package com.example.dailyquiz.data.mapper

import com.example.dailyquiz.data.room.quiz_question.QuizQuestionEntity
import com.example.dailyquiz.model.QuizQuestion
import kotlinx.serialization.json.Json

fun QuizQuestion.asEntity(): QuizQuestionEntity = QuizQuestionEntity(
    category = category,
    type = type,
    difficulty = difficulty,
    question = question,
    correctAnswer = correctAnswer,
    incorrectAnswersJson = incorrectAnswers.toJson()
)

fun QuizQuestionEntity.asModel(): QuizQuestion = QuizQuestion(
    category = category,
    type = type,
    difficulty = difficulty,
    question = question,
    correctAnswer = correctAnswer,
    incorrectAnswers = incorrectAnswersJson.toStringList()
)

fun List<String>.toJson(): String = Json.encodeToString(this)
fun String.toStringList(): List<String> = Json.decodeFromString(this)
