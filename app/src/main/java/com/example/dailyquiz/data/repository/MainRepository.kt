package com.example.dailyquiz.data.repository

import com.example.dailyquiz.data.mapper.asEntity
import com.example.dailyquiz.data.mapper.asModel
import com.example.dailyquiz.data.remote.ApiService
import com.example.dailyquiz.data.room.quiz_question.QuizQuestionDao
import com.example.dailyquiz.model.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface MainRepository {
    fun getQuizQuestions(
        amount: Int,
        category: Int?,
        difficulty: String?,
        type: String
    ): Flow<List<QuizQuestion>>
}

class MainRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val quizQuestionDao: QuizQuestionDao,
) : MainRepository {

    override fun getQuizQuestions(
        amount: Int,
        category: Int?,
        difficulty: String?,
        type: String
    ): Flow<List<QuizQuestion>> = flow {
        try {
            val questionsFromApi = apiService.quizQuestions(amount, category, difficulty, type)
            quizQuestionDao.clearAll()
            quizQuestionDao.insertAll(questionsFromApi.map { it.asEntity() })
            emit(questionsFromApi)
        } catch (e: Exception) {
            // При ошибке загружаем кеш данные
            val cachedQuestions = quizQuestionDao.getQuestions(amount).map { it.asModel() }
            if (cachedQuestions.isNotEmpty()) emit(cachedQuestions)
            else throw e
        }
    }.flowOn(Dispatchers.IO)
}
