package com.example.dailyquiz.data.remote

import android.text.Html
import com.example.dailyquiz.model.QuizQuestion
import com.example.dailyquiz.model.QuizResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.path
import javax.inject.Inject

interface ApiService {
    suspend fun quizQuestions(
        amount: Int,
        category: Int?,
        difficulty: String?,
        type: String
    ): List<QuizQuestion>
}

class DailyQuizApiClient @Inject constructor(
    private val client: HttpClient
) : ApiService {

    override suspend fun quizQuestions(
        amount: Int,
        category: Int?,
        difficulty: String?,
        type: String
    ): List<QuizQuestion> {
        val response: QuizResponse = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "opentdb.com"
                path("api.php")
                parameters.append("amount", amount.toString())
                parameters.append("type", type)
                category?.let { parameters.append("category", it.toString()) }
                difficulty?.let { parameters.append("difficulty", it) }
            }
        }.body()

        if (response.responseCode != 0) throw Exception("API error with response code: ${response.responseCode}")

        return response.results
    }
}

fun decodeHtml(htmlString: String): String {
    return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
}
